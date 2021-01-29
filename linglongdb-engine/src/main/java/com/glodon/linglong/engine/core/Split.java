package com.glodon.linglong.engine.core;

import com.glodon.linglong.base.common.Utils;
import com.glodon.linglong.engine.core.frame.CursorFrame;

import java.io.IOException;

/**
 * @author Stereo
 */
public final class Split {
    final boolean mSplitRight;
    private final Node mSibling;

    private byte[] mFullKey;
    private byte[] mActualKey; // might be fragmented

    public Split(boolean splitRight, Node sibling) {
        mSplitRight = splitRight;
        mSibling = sibling;
    }

    final void setKey(Split split) {
        mFullKey = split.mFullKey;
        mActualKey = split.mActualKey;
    }

    final void setKey(Tree tree, byte[] fullKey) throws IOException {
        setKey(tree.mDatabase, fullKey);
    }

    final void setKey(LocalDatabase db, byte[] fullKey) throws IOException {
        byte[] actualKey = fullKey;

        if (Node.calculateAllowedKeyLength(db, fullKey) < 0) {
            actualKey = db.fragmentKey(fullKey);
        }

        mFullKey = fullKey;
        mActualKey = actualKey;
    }

    final byte[] fragmentedKey() {
        return mFullKey == mActualKey ? null : mActualKey;
    }

    final int compare(byte[] key) {
        return Utils.compareUnsigned(key, 0, key.length, mFullKey, 0, mFullKey.length);
    }

    final Node selectNode(Node node, byte[] key) {
        Node sibling = mSibling;
        sibling.acquireShared();

        Node left, right;
        if (mSplitRight) {
            left = node;
            right = sibling;
        } else {
            left = sibling;
            right = node;
        }

        if (compare(key) < 0) {
            right.releaseShared();
            return left;
        } else {
            left.releaseShared();
            return right;
        }
    }

    final int adjustBindPosition(int pos) {
        if (!mSplitRight) {
            Node sibling = latchSibling();
            pos += sibling.highestPos() + 2;
            sibling.releaseShared();
        }

        return pos;
    }

    final byte[] retrieveLeafValue(Node node, int pos) throws IOException {
        if (mSplitRight) {
            int highestPos = node.highestPos();
            if (pos > highestPos) {
                Node sibling = latchSibling();
                try {
                    return sibling.retrieveLeafValue(pos - highestPos - 2);
                } finally {
                    sibling.releaseShared();
                }
            }
        } else {
            Node sibling = latchSibling();
            try {
                int highestPos = sibling.highestPos();
                if (pos <= highestPos) {
                    return sibling.retrieveLeafValue(pos);
                }
                pos = pos - highestPos - 2;
            } finally {
                sibling.releaseShared();
            }
        }

        return node.retrieveLeafValue(pos);
    }

    final int binarySearchLeaf(Node node, byte[] key) throws IOException {
        Node sibling = latchSibling();

        Node left, right;
        if (mSplitRight) {
            left = node;
            right = sibling;
        } else {
            left = sibling;
            right = node;
        }

        int searchPos;
        if (compare(key) < 0) {
            searchPos = left.binarySearch(key);
        } else {
            int highestPos = left.highestLeafPos();
            searchPos = right.binarySearch(key);
            if (searchPos < 0) {
                searchPos = searchPos - highestPos - 2;
            } else {
                searchPos = highestPos + 2 + searchPos;
            }
        }

        sibling.releaseShared();

        return searchPos;
    }

    final int highestPos(Node node) {
        int pos;
        Node sibling = latchSibling();
        if (node.isLeaf()) {
            pos = node.highestLeafPos() + 2 + sibling.highestLeafPos();
        } else {
            pos = node.highestInternalPos() + 2 + sibling.highestInternalPos();
        }
        sibling.releaseShared();
        return pos;
    }

    final Node latchSibling() {
        Node sibling = mSibling;
        sibling.acquireShared();
        return sibling;
    }

    public final Node latchSiblingEx() {
        Node sibling = mSibling;
        sibling.acquireExclusive();
        return sibling;
    }

    public final void rebindFrame(CursorFrame frame, Node sibling) {
        int pos = frame.getNodePos();

        if (mSplitRight) {
            Node frameNode = frame.getNode();
            if (frameNode == null) {
                return;
            }

            int highestPos = frameNode.highestPos();

            if (pos >= 0) {
                if (pos <= highestPos) {
                    // Nothing to do.
                } else {
                    frame.rebind(sibling, pos - highestPos - 2);
                }
                return;
            }

            pos = ~pos;

            if (pos <= highestPos) {
                // Nothing to do.
                return;
            }

            if (pos == highestPos + 2) {
                byte[] key = frame.getNotFoundKey();
                if (key == null || compare(key) < 0) {
                    // Nothing to do.
                    return;
                }
            }

            frame.rebind(sibling, ~(pos - highestPos - 2));
        } else {
            int highestPos = sibling.highestPos();

            if (pos >= 0) {
                if (pos <= highestPos) {
                    frame.rebind(sibling, pos);
                } else {
                    frame.setNodePos(pos - highestPos - 2);
                }
                return;
            }

            pos = ~pos;

            if (pos <= highestPos) {
                frame.rebind(sibling, ~pos);
                return;
            }

            if (pos == highestPos + 2) {
                byte[] key = frame.getNotFoundKey();
                if (key == null) {
                    return;
                }
                if (compare(key) < 0) {
                    frame.rebind(sibling, ~pos);
                    return;
                }
            }

            frame.setNodePos(~(pos - highestPos - 2));
        }
    }

    final int splitKeyEncodedLength() {
        byte[] actualKey = mActualKey;
        if (actualKey == mFullKey) {
            return Node.calculateKeyLength(actualKey);
        } else {
            return 2 + actualKey.length;
        }
    }

    final int copySplitKeyToParent(final long dest, final int destLoc) {
        byte[] actualKey = mActualKey;
        if (actualKey == mFullKey) {
            return Node.encodeNormalKey(actualKey, dest, destLoc);
        } else {
            return Node.encodeFragmentedKey(actualKey, dest, destLoc);
        }
    }
}
