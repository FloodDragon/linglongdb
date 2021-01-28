package com.glodon.linglong.engine.core.frame;

import com.glodon.linglong.engine.core.Node;

import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * @author Stereo
 */
@SuppressWarnings("serial")
public class CursorFrame extends AtomicReference<CursorFrame> {
    static final int SPIN_LIMIT = Runtime.getRuntime().availableProcessors() > 1 ? 1 << 10 : 0;

    private static final CursorFrame REBIND_FRAME = new CursorFrame();

    static final AtomicReferenceFieldUpdater<Node, CursorFrame>
            cLastUpdater = AtomicReferenceFieldUpdater.newUpdater
            (Node.class, CursorFrame.class, "mLastCursorFrame");

    volatile CursorFrame mPrevCousin;

    Node mNode;
    int mNodePos;

    CursorFrame mParentFrame;

    byte[] mNotFoundKey;

    CursorFrame() {
    }

    CursorFrame(CursorFrame parentFrame) {
        mParentFrame = parentFrame;
    }

    final Node acquireShared() {
        Node node = mNode;
        while (true) {
            node.acquireShared();
            Node actualNode = mNode;
            if (actualNode == node) {
                return actualNode;
            }
            node.releaseShared();
            node = actualNode;
        }
    }

    final Node tryAcquireShared() {
        Node node = mNode;
        while (node.tryAcquireShared()) {
            Node actualNode = mNode;
            if (actualNode == node) {
                return actualNode;
            }
            node.releaseShared();
            node = actualNode;
        }
        return null;
    }

    final Node acquireExclusive() {
        Node node = mNode;
        while (true) {
            node.acquireExclusive();
            Node actualNode = mNode;
            if (actualNode == node) {
                return actualNode;
            }
            node.releaseExclusive();
            node = actualNode;
        }
    }

    final Node acquireExclusiveIfBound() {
        Node node = mNode;
        while (node != null) {
            node.acquireExclusive();
            Node actualNode = mNode;
            if (actualNode == node) {
                return actualNode;
            }
            node.releaseExclusive();
            node = actualNode;
        }
        return null;
    }

    final Node tryAcquireExclusive() {
        Node node = mNode;
        while (node.tryAcquireExclusive()) {
            Node actualNode = mNode;
            if (actualNode == node) {
                return actualNode;
            }
            node.releaseExclusive();
            node = actualNode;
        }
        return null;
    }

    final void adjustParentPosition(int amount) {
        CursorFrame parent = mParentFrame;
        if (parent != null) {
            parent.mNodePos += amount;
        }
    }

    final void bind(Node node, int nodePos) {
        mNode = node;
        mNodePos = nodePos;

        this.set(this);

        for (int trials = SPIN_LIMIT; ; ) {
            CursorFrame last = node.mLastCursorFrame;
            mPrevCousin = last;
            if (last == null) {
                if (cLastUpdater.compareAndSet(node, null, this)) {
                    return;
                }
            } else if (last.get() == last && last.compareAndSet(last, this)) {

                while (node.mLastCursorFrame != last) ;

                node.mLastCursorFrame = this;
                return;
            }

            if (--trials < 0) {
                Thread.yield();
                trials = SPIN_LIMIT << 1;
            }
        }
    }

    final void bindOrReposition(Node node, int nodePos) {
        if (mNode == null) {
            bind(node, nodePos);
        } else if (mNode == node) {
            mNodePos = nodePos;
        } else {
            throw new IllegalStateException();
        }
    }

    final void rebind(Node node, int nodePos) {
        if (unbind(REBIND_FRAME)) {
            bind(node, nodePos);
        }
    }

    private boolean unbind(CursorFrame to) {
        for (int trials = SPIN_LIMIT; ; ) {
            CursorFrame n = this.get();

            if (n == null) {
                return false;
            }

            if (n == this) {
                Node node = mNode;
                if (node != null && node.mLastCursorFrame == this && this.compareAndSet(n, to)) {
                    if (node != mNode || node.mLastCursorFrame != this) {
                        this.set(n);
                    } else {
                        CursorFrame p;
                        do {
                            p = this.mPrevCousin;
                        } while (p != null && (p.get() != this || !p.compareAndSet(this, p)));
                        node.mLastCursorFrame = p;
                        return true;
                    }
                }
            } else {
                if (n.mPrevCousin == this && this.compareAndSet(n, to)) {
                    CursorFrame p;
                    do {
                        p = this.mPrevCousin;
                    } while (p != null && (p.get() != this || !p.compareAndSet(this, n)));
                    n.mPrevCousin = p;
                    return true;
                }
            }

            if (--trials < 0) {
                Thread.yield();
                trials = SPIN_LIMIT << 1;
            }
        }
    }

    final CursorFrame tryLock(CursorFrame lock) {
        for (int trials = SPIN_LIMIT; ; ) {
            CursorFrame n = this.get();

            if (n == null) {
                return null;
            }

            if (n == this) {
                Node node = mNode;
                if (node != null && node.mLastCursorFrame == this && this.compareAndSet(n, lock)) {
                    if (node != mNode || node.mLastCursorFrame != this) {
                        this.set(n);
                    } else {
                        return n;
                    }
                }
            } else {
                if (n.mPrevCousin == this && this.compareAndSet(n, lock)) {
                    return n;
                }
            }

            if (--trials < 0) {
                Thread.yield();
                trials = SPIN_LIMIT << 1;
            }
        }
    }

    final CursorFrame tryLockPrevious(CursorFrame lock) {
        CursorFrame p;
        do {
            p = this.mPrevCousin;
        } while (p != null && (p.get() != this || !p.compareAndSet(this, lock)));
        return p;
    }

    final void unlock(CursorFrame n) {
        this.set(n);
    }

    final CursorFrame pop() {
        unbind(null);
        CursorFrame parent = mParentFrame;
        mNode = null;
        mParentFrame = null;
        mNotFoundKey = null;
        return parent;
    }

    static void popAll(CursorFrame frame) {
        do {
            frame = frame.mNode == null ? frame.mParentFrame : frame.pop();
        } while (frame != null);
    }

    final void popChilden(CursorFrame child) {
        do {
            child = child.pop();
        } while (child != this);
    }

    final void copyInto(CursorFrame dest) {
        Node node = acquireShared();
        CursorFrame parent = mParentFrame;

        if (parent != null) {
            node.releaseShared();
            CursorFrame parentCopy = new CursorFrame();

            while (true) {
                if (parent != null) {
                    parent.copyInto(parentCopy);
                }

                node = acquireShared();
                final CursorFrame actualParent = mParentFrame;

                if (actualParent == parent) {
                    if (parent != null) {
                        dest.mParentFrame = parentCopy;
                    }
                    break;
                }

                node.releaseShared();
                popAll(parentCopy);
                parent = actualParent;
            }
        }

        dest.mNotFoundKey = mNotFoundKey;
        dest.bind(node, mNodePos);
        node.releaseShared();
    }

    @Override
    public final String toString() {
        return Utils.toMiniString(this);
    }
}
