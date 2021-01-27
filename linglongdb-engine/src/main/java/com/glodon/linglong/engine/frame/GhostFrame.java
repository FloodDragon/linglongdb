package com.glodon.linglong.engine.frame;


import com.glodon.linglong.engine.concurrent.Latch;
import com.glodon.linglong.engine.core.Database;
import com.glodon.linglong.engine.lock.Lock;

/**
 * @author Stereo
 */
class GhostFrame extends CursorFrame {
    /**
     * @param latch latch which guards the lock; might be briefly released and re-acquired
     * @param lock lock which references this ghost frame
     */
    void action(Database db, Latch latch, Lock lock) {
        byte[] key = lock.mKey;
        boolean unlatched = false;

        CommitLock.Shared shared = db.commitLock().tryAcquireShared();
        if (shared == null) {
            // Release lock management latch to prevent deadlock.
            latch.releaseExclusive();
            unlatched = true;
            shared = db.commitLock().acquireShared();
        }

        // Note: Unlike regular frames, ghost frames cannot be unbound (popAll) from the node
        // after the node latch is released. If the node latch is released before the frame is
        // unbound, another thread can then evict the node and unbind the ghost frame instances
        // concurrently, which isn't thread-safe and can corrupt the cursor frame list.

        doDelete: try {
            _Node node = this.mNode;
            if (node != null) latchNode: {
                if (!unlatched) {
                    while (node.tryAcquireExclusive()) {
                        _Node actualNode = this.mNode;
                        if (actualNode == node) {
                            break latchNode;
                        }
                        node.releaseExclusive();
                        node = actualNode;
                        if (node == null) {
                            break latchNode;
                        }
                    }

                    // Release lock management latch to prevent deadlock.
                    latch.releaseExclusive();
                    unlatched = true;
                }

                node = this.acquireExclusiveIfBound();
            }

            if (node == null) {
                // Will need to delete the slow way.
            } else if (!db.isMutable(node)) {
                // _Node cannot be dirtied without a full cursor, so delete the slow way.
                popAll(this);
                node.releaseExclusive();
            } else {
                // Frame is still valid and node is mutable, so perform a quick delete.

                int pos = this.mNodePos;
                if (pos < 0) {
                    // Already deleted.
                    popAll(this);
                    node.releaseExclusive();
                    break doDelete;
                }

                _Split split = node.mSplit;
                if (split == null) {
                    try {
                        if (node.hasLeafValue(pos) == null) {
                            // Ghost still exists, so delete it.
                            node.deleteLeafEntry(pos);
                            node.postDelete(pos, key);
                        }
                    } finally {
                        popAll(this);
                        node.releaseExclusive();
                    }
                } else {
                    _Node sibling;
                    try {
                        sibling = split.latchSiblingEx();
                    } catch (Throwable e) {
                        popAll(this);
                        node.releaseExclusive();
                        throw e;
                    }

                    try {
                        split.rebindFrame(this, sibling);

                        _Node actualNode = this.mNode;
                        int actualPos = this.mNodePos;

                        if (actualNode.hasLeafValue(actualPos) == null) {
                            // Ghost still exists, so delete it.
                            actualNode.deleteLeafEntry(actualPos);
                            // Fix existing frames on original node. Other than potentially the
                            // ghost frame, no frames exist on the sibling.
                            node.postDelete(pos, key);
                        }
                    } finally {
                        // Pop the frames before releasing the latches, preventing other
                        // threads from observing a frame bound to the sibling too soon.
                        popAll(this);
                        sibling.releaseExclusive();
                        node.releaseExclusive();
                    }
                }

                break doDelete;
            }

            // Delete the ghost the slow way. Open the index, and then search for the ghost.

            if (!unlatched) {
                // Release lock management latch to prevent deadlock.
                latch.releaseExclusive();
                unlatched = true;
            }

            while (true) {
                Index ix = db.anyIndexById(lock.mIndexId);
                if (!(ix instanceof _Tree)) {
                    // Assume index was deleted.
                    break;
                }
                _TreeCursor c = new _TreeCursor((_Tree) ix);
                if (c.deleteGhost(key)) {
                    break;
                }
                // Reopen a closed index.
            }
        } catch (Throwable e) {
            // Exception indicates that database is borked. Ghost will get cleaned up when
            // database is re-opened.
            shared.release();
            if (!unlatched) {
                // Release lock management latch to prevent deadlock.
                latch.releaseExclusive();
            }
            try {
                Utils.closeQuietly(lock.mOwner.getDatabase(), e);
            } finally {
                latch.acquireExclusive();
            }
            return;
        }

        shared.release();
        if (unlatched) {
            latch.acquireExclusive();
        }
    }
}
