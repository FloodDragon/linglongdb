package com.glodon.lingling.base.concurrent;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

/**
 * @author Stereo
 */
public class LatchCondition {
    private static final ThreadLocal<Node> cLocalNode = new ThreadLocal<>();

    Node mHead;
    Node mTail;

    public final boolean isEmpty() {
        return mHead == null;
    }

    public final int await(Latch latch) {
        return await(latch, -1, 0);
    }

    public final int await(Latch latch, long timeout, TimeUnit unit) {
        long nanosTimeout, nanosEnd;
        if (timeout <= 0) {
            nanosTimeout = timeout;
            nanosEnd = 0;
        } else {
            nanosTimeout = unit.toNanos(timeout);
            nanosEnd = System.nanoTime() + nanosTimeout;
        }
        return await(latch, nanosTimeout, nanosEnd);
    }

    public final int await(Latch latch, long nanosTimeout) {
        long nanosEnd = nanosTimeout <= 0 ? 0 : (System.nanoTime() + nanosTimeout);
        return await(latch, nanosTimeout, nanosEnd);
    }

    public final int await(Latch latch, long nanosTimeout, long nanosEnd) {
        try {
            return await(latch, localNode(Node.WAITING), nanosTimeout, nanosEnd);
        } catch (Throwable e) {
            return -1;
        }
    }

    /**
     * @return -1 if interrupted, 0 if timed out, 1 if signaled
     */
    public final int awaitShared(Latch latch, long nanosTimeout, long nanosEnd) {
        try {
            return await(latch, localNode(Node.WAITING_SHARED), nanosTimeout, nanosEnd);
        } catch (Throwable e) {
            return -1;
        }
    }

    private Node localNode(int waitState) {
        Node node = cLocalNode.get();
        if (node == null) {
            node = new Node(Thread.currentThread());
            cLocalNode.set(node);
        }
        node.mWaitState = waitState;
        return node;
    }

    private int await(Latch latch, Node node, long nanosTimeout, long nanosEnd) {
        enqueue(node);

        if (nanosTimeout < 0) {
            while (true) {
                latch.releaseExclusive();
                LockSupport.park(this);
                latch.acquireExclusive();
                int result = node.resumed(this);
                if (result != 0) {
                    return result;
                }
            }
        } else {
            while (true) {
                latch.releaseExclusive();
                LockSupport.parkNanos(this, nanosTimeout);
                latch.acquireExclusive();
                int result = node.resumed(this);
                if (result != 0) {
                    return result;
                }
                if (nanosTimeout == 0 || (nanosTimeout = nanosEnd - System.nanoTime()) <= 0) {
                    node.remove(this);
                    return 0;
                }
            }
        }
    }

    private void enqueue(Node node) {
        Node tail = mTail;
        if (tail == null) {
            mHead = node;
        } else {
            tail.mNext = node;
            node.mPrev = tail;
        }
        mTail = node;
    }

    public final void signal() {
        Node head = mHead;
        if (head != null) {
            head.signal();
        }
    }

    public final boolean signalRelease(Latch latch) {
        Node head = mHead;
        if (head != null) {
            if (head == mTail) {
                head.signal();
                latch.releaseExclusive();
            } else {
                head.remove(this);
                latch.releaseExclusive();
                LockSupport.unpark(head.mWaiter);
            }
            return true;
        } else {
            return false;
        }
    }

    public final boolean signalNext() {
        Node head = mHead;
        if (head == null) {
            return false;
        }
        head.signal();
        return true;
    }

    public final void signalAll() {
        Node node = mHead;
        while (node != null) {
            node.signal();
            node = node.mNext;
        }
    }

    public final void signalShared() {
        Node head = mHead;
        if (head != null && head.mWaitState == Node.WAITING_SHARED) {
            head.signal();
        }
    }

    public final boolean signalSharedRelease(Latch latch) {
        Node head = mHead;
        if (head != null && head.mWaitState == Node.WAITING_SHARED) {
            if (head == mTail) {
                head.signal();
                latch.releaseExclusive();
            } else {
                head.remove(this);
                latch.releaseExclusive();
                LockSupport.unpark(head.mWaiter);
            }
            return true;
        } else {
            return false;
        }
    }

    public final boolean signalNextShared() {
        Node head = mHead;
        if (head == null) {
            return false;
        }
        if (head.mWaitState == Node.WAITING_SHARED) {
            head.signal();
        }
        return true;
    }

    public final void clear() {
        Node node = mHead;
        while (node != null) {
            if (node.mWaitState >= Node.WAITING) {
                if (node.mWaiter instanceof Thread) {
                    ((Thread) node.mWaiter).interrupt();
                }
            }
            node.mPrev = null;
            Node next = node.mNext;
            node.mNext = null;
            node = next;
        }
        mHead = null;
        mTail = null;
    }

    static class Node {
        final Thread mWaiter;

        static final int REMOVED = 0, SIGNALED = 1, WAITING = 2, WAITING_SHARED = 3;
        int mWaitState;

        Node mPrev;
        Node mNext;

        Node(Thread waiter) {
            mWaiter = waiter;
        }

        final int resumed(LatchCondition queue) {
            if (mWaitState < WAITING) {
                if (mWaitState != REMOVED) {
                    remove(queue);
                }
                return 1;
            }

            if (mWaiter.isInterrupted()) {
                Thread.interrupted();
                remove(queue);
                return -1;
            }

            return 0;
        }

        final void signal() {
            mWaitState = SIGNALED;
            LockSupport.unpark(mWaiter);
        }

        final void remove(LatchCondition queue) {
            Node prev = mPrev;
            Node next = mNext;
            if (prev == null) {
                if ((queue.mHead = next) == null) {
                    queue.mTail = null;
                } else {
                    next.mPrev = null;
                }
            } else {
                if ((prev.mNext = next) == null) {
                    queue.mTail = prev;
                } else {
                    next.mPrev = prev;
                }
                mPrev = null;
            }
            mNext = null;

            mWaitState = REMOVED;
        }
    }
}
