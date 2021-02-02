package com.glodon.linglong.engine.core;


import com.glodon.linglong.base.common.Utils;
import com.glodon.linglong.base.concurrent.Latch;
import com.glodon.linglong.engine.core.*;
import com.glodon.linglong.engine.core.frame.Index;
import com.glodon.linglong.engine.core.lock.CommitLock;
import com.glodon.linglong.engine.core.lock.Lock;

/**
 * @author Stereo
 */
public class GhostFrame extends CursorFrame {
    public void action(LocalDatabase db, Latch latch, Lock lock) {
        byte[] key = lock.getKey();
        boolean unlatched = false;

        CommitLock.Shared shared = db.commitLock().tryAcquireShared();
        if (shared == null) {
            // 防止死锁。
            latch.releaseExclusive();
            unlatched = true;
            shared = db.commitLock().acquireShared();
        }

        doDelete:
        try {
            Node node = this.mNode;
            if (node != null) latchNode:{
                if (!unlatched) {
                    while (node.tryAcquireExclusive()) {
                        Node actualNode = this.mNode;
                        if (actualNode == node) {
                            break latchNode;
                        }
                        node.releaseExclusive();
                        node = actualNode;
                        if (node == null) {
                            break latchNode;
                        }
                    }

                    // 防止死锁。
                    latch.releaseExclusive();
                    unlatched = true;
                }

                node = this.acquireExclusiveIfBound();
            }

            if (node == null) {
            } else if (!db.isMutable(node)) {
                popAll(this);
                node.releaseExclusive();
            } else {
                int pos = this.mNodePos;
                if (pos < 0) {
                    // Already deleted.
                    popAll(this);
                    node.releaseExclusive();
                    break doDelete;
                }

                Split split = node.getSplit();
                if (split == null) {
                    try {
                        if (node.hasLeafValue(pos) == null) {
                            node.deleteLeafEntry(pos);
                            node.postDelete(pos, key);
                        }
                    } finally {
                        popAll(this);
                        node.releaseExclusive();
                    }
                } else {
                    Node sibling;
                    try {
                        sibling = split.latchSiblingEx();
                    } catch (Throwable e) {
                        popAll(this);
                        node.releaseExclusive();
                        throw e;
                    }

                    try {
                        split.rebindFrame(this, sibling);

                        Node actualNode = this.mNode;
                        int actualPos = this.mNodePos;

                        if (actualNode.hasLeafValue(actualPos) == null) {
                            actualNode.deleteLeafEntry(actualPos);
                            node.postDelete(pos, key);
                        }
                    } finally {
                        popAll(this);
                        sibling.releaseExclusive();
                        node.releaseExclusive();
                    }
                }

                break doDelete;
            }

            if (!unlatched) {
                latch.releaseExclusive();
                unlatched = true;
            }

            while (true) {
                Index ix = db.anyIndexById(lock.getIndexId());
                if (!(ix instanceof Tree)) {
                    break;
                }
                TreeCursor c = new TreeCursor((Tree) ix);
                if (c.deleteGhost(key)) {
                    break;
                }
            }
        } catch (Throwable e) {
            shared.release();
            if (!unlatched) {
                latch.releaseExclusive();
            }
            try {
                Utils.closeQuietly(lock.getOwner().getDatabase(), e);
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
