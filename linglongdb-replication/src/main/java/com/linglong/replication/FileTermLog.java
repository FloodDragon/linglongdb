package com.linglong.replication;

import com.linglong.base.common.Utils;
import com.linglong.base.concurrent.Latch;
import com.linglong.base.concurrent.Worker;
import com.linglong.io.*;

import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.locks.LockSupport;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Stereo
 */
final class FileTermLog extends Latch implements TermLog {
    private static final int MAX_CACHED_SEGMENTS = 10;
    private static final int MAX_CACHED_WRITERS = 10;
    private static final int MAX_CACHED_READERS = 10;

    private static final int EOF = -1;
    private static final int WAIT_TERM_END = EOF;
    private static final int WAIT_TIMEOUT = -2;

    private static final ThreadLocal<DelayedWaiter> cLocalDelayed = new ThreadLocal<>();

    private final Worker mWorker;
    private final File mBase;
    private final long mLogPrevTerm;
    private final long mLogTerm;
    private final long mLogStartIndex;

    private long mLogCommitIndex;
    private long mLogHighestIndex;
    private long mLogContigIndex;
    private long mLogEndIndex;

    private final NavigableSet<LKey<Segment>> mSegments;

    private final PriorityQueue<SegmentWriter> mNonContigWriters;

    private final LCache<Segment> mSegmentCache;
    private final LCache<SegmentWriter> mWriterCache;
    private final LCache<SegmentReader> mReaderCache;

    private final PriorityQueue<Delayed> mCommitTasks;

    private final Latch mSyncLatch;
    private final Latch mDirtyLatch;
    private Segment mFirstDirty;
    private Segment mLastDirty;

    private boolean mLogClosed;

    static TermLog newTerm(Worker worker, File base, long prevTerm, long term,
                           long startIndex, long commitIndex) {
        base = checkBase(base);

        FileTermLog termLog = new FileTermLog
                (worker, base, prevTerm, term, startIndex, commitIndex, startIndex, null);

        return termLog;
    }

    static TermLog openTerm(Worker worker, File base, long prevTerm, long term,
                            long startIndex, long commitIndex, long highestIndex,
                            List<String> segmentFileNames) {
        base = checkBase(base);

        if (segmentFileNames == null) {
            String[] namesArray = base.getParentFile().list();

            if (namesArray != null && namesArray.length != 0) {
                Pattern p = Pattern.compile(base.getName() + "\\." + term + "\\.\\d+(?:\\.\\d+)?");

                segmentFileNames = new ArrayList<>();
                for (String name : namesArray) {
                    if (p.matcher(name).matches()) {
                        segmentFileNames.add(name);
                    }
                }
            }
        }

        FileTermLog termLog = new FileTermLog
                (worker, base, prevTerm, term,
                        startIndex, commitIndex, highestIndex, segmentFileNames);

        return termLog;
    }

    static File checkBase(File base) {
        base = base.getAbsoluteFile();

        if (base.isDirectory()) {
            throw new IllegalArgumentException("Base file is a directory: " + base);
        }

        if (!base.getParentFile().exists()) {
            throw new IllegalArgumentException("Parent file doesn't exist: " + base);
        }

        return base;
    }

    private FileTermLog(Worker worker, File base, long prevTerm, long term,
                        long startIndex, final long commitIndex, final long highestIndex,
                        List<String> segmentFileNames) {
        if (term < 0) {
            throw new IllegalArgumentException("Illegal term: " + term);
        }

        if (commitIndex > highestIndex) {
            throw new IllegalArgumentException("Commit index is higher than highest index: " +
                    commitIndex + " > " + highestIndex);
        }

        mWorker = worker;
        mBase = base;
        mLogTerm = term;

        mSegments = new ConcurrentSkipListSet<>();

        if (segmentFileNames != null && segmentFileNames.size() != 0) {
            File parent = base.getParentFile();

            Pattern p = Pattern.compile(base.getName() + "\\." + term + "\\.(\\d+)(?:\\.(\\d+))?");

            boolean anyMatches = false;

            for (String name : segmentFileNames) {
                Matcher m = p.matcher(name);

                if (!m.matches()) {
                    continue;
                }

                anyMatches = true;

                long start = Long.parseLong(m.group(1));

                String prevTermStr = m.group(2);

                if (prevTermStr != null) {
                    long parsedPrevTerm = Long.parseLong(prevTermStr);
                    if (prevTerm < 0) {
                        prevTerm = parsedPrevTerm;
                    } else if (prevTerm != parsedPrevTerm) {
                        throw new IllegalStateException
                                ("Mismatched previous term: " + prevTerm + " != " + prevTermStr);
                    }
                }

                long maxLength = Math.max(maxSegmentLength(), new File(parent, name).length());
                mSegments.add(new Segment(start, maxLength));
            }

            if (prevTerm < 0 && anyMatches) {
                prevTerm = term;
            }
        }

        if (prevTerm < 0) {
            throw new IllegalStateException("Unable to determine previous term");
        }

        if (startIndex == -1) {
            if (mSegments.isEmpty()) {
                throw new IllegalStateException("No segment files exist for term: " + term);
            }
            startIndex = ((Segment) mSegments.first()).mStartIndex;
        } else if (startIndex < highestIndex) {
            Segment first;
            if (mSegments.isEmpty()
                    || (first = (Segment) mSegments.first()).mStartIndex > startIndex) {
                throw new IllegalStateException
                        ("Missing start segment: " + startIndex + ", term=" + term);
            }
        }

        mLogPrevTerm = prevTerm;
        mLogStartIndex = startIndex;
        mLogCommitIndex = commitIndex;
        mLogHighestIndex = highestIndex;
        mLogContigIndex = highestIndex;
        mLogEndIndex = Long.MAX_VALUE;

        forEachSegment(mSegments.tailSet(new LKey.Finder<>(startIndex)), (seg, next) -> {
            if (seg.mStartIndex >= highestIndex) {
                return false;
            }
            if (next != null) {
                File file = seg.file();
                long segHighest = seg.mStartIndex + file.length();
                if (segHighest < highestIndex && segHighest < next.mStartIndex) {
                    throw new IllegalStateException("Incomplete segment: " + file);
                }
            }
            return true;
        });

        forEachSegment(mSegments, (seg, next) -> {
            if (next != null
                    && seg.endIndex() > next.mStartIndex
                    && seg.setEndIndex(next.mStartIndex)
                    && seg.file().length() > seg.mMaxLength) {
                try {
                    seg.truncate();
                } catch (IOException e) {
                    throw Utils.rethrow(e);
                }
            }
            return true;
        });

        forEachSegment(mSegments, (seg, next) -> {
            if (seg.endIndex() <= mLogStartIndex || seg.mStartIndex >= mLogHighestIndex) {
                seg.file().delete();
                mSegments.remove(seg);
            }
            return true;
        });

        mSegmentCache = new LCache<>(MAX_CACHED_SEGMENTS);
        mWriterCache = new LCache<>(MAX_CACHED_WRITERS);
        mReaderCache = new LCache<>(MAX_CACHED_READERS);

        mSyncLatch = new Latch();
        mDirtyLatch = new Latch();

        // TODO: 按需分配；完成时为空
        mNonContigWriters = new PriorityQueue<>();
        mCommitTasks = new PriorityQueue<>();
    }

    private static void forEachSegment(SortedSet<LKey<Segment>> segments,
                                       BiFunction<Segment, Segment, Boolean> pairConsumer) {
        Iterator<LKey<Segment>> it = segments.iterator();

        if (it.hasNext()) {
            Segment seg = (Segment) it.next();
            while (true) {
                Segment next;
                if (it.hasNext()) {
                    next = (Segment) it.next();
                } else {
                    next = null;
                }
                if (!pairConsumer.apply(seg, next)) {
                    return;
                }
                if (next == null) {
                    break;
                }
                seg = next;
            }
        }
    }

    @Override
    public long prevTerm() {
        return mLogPrevTerm;
    }

    @Override
    public long term() {
        return mLogTerm;
    }

    @Override
    public long startIndex() {
        return mLogStartIndex;
    }

    @Override
    public long prevTermAt(long index) {
        return index <= mLogStartIndex ? mLogPrevTerm : mLogTerm;
    }

    @Override
    public boolean compact(long startIndex) throws IOException {
        acquireShared();
        boolean full = startIndex >= mLogEndIndex;
        releaseShared();

        Iterator<LKey<Segment>> it = mSegments.iterator();
        while (it.hasNext()) {
            Segment seg = (Segment) it.next();

            long endIndex = seg.endIndex();
            if (endIndex > startIndex) {
                break;
            }

            seg.acquireExclusive();
            try {
                seg.close(true);
            } finally {
                seg.releaseExclusive();
            }

            File segFile = seg.file();

            if (!segFile.delete() && segFile.exists()) {
                break;
            }

            it.remove();

            mSegmentCache.remove(seg.cacheKey());
        }

        return full && mSegments.isEmpty();
    }

    @Override
    public long potentialCommitIndex() {
        acquireShared();
        long index = mLogCommitIndex;
        releaseShared();
        return index;
    }

    @Override
    public long endIndex() {
        acquireShared();
        long index = mLogEndIndex;
        releaseShared();
        return index;
    }

    @Override
    public void captureHighest(LogInfo info) {
        info.mTerm = mLogTerm;
        acquireShared();
        doCaptureHighest(info);
        releaseShared();
    }

    private void doCaptureHighest(LogInfo info) {
        info.mHighestIndex = mLogHighestIndex;
        info.mCommitIndex = doAppliableCommitIndex();
    }

    long appliableCommitIndex() {
        acquireShared();
        long commitIndex = doAppliableCommitIndex();
        releaseShared();
        return commitIndex;
    }

    private long doAppliableCommitIndex() {
        return Math.min(mLogCommitIndex, mLogHighestIndex);
    }

    @Override
    public void commit(long commitIndex) {
        acquireExclusive();
        if (commitIndex > mLogCommitIndex) {
            long endIndex = mLogEndIndex;
            if (commitIndex > endIndex) {
                commitIndex = endIndex;
            }
            mLogCommitIndex = commitIndex;
            if (mLogHighestIndex < commitIndex) {
                mLogHighestIndex = Math.min(commitIndex, mLogContigIndex);
            }
            notifyCommitTasks(doAppliableCommitIndex());
            return;
        }
        releaseExclusive();
    }

    @Override
    public long waitForCommit(long index, long nanosTimeout) throws InterruptedIOException {
        return waitForCommit(index, nanosTimeout, this);
    }

    /**
     * 等待日志提交
     *
     * @param index
     * @param nanosTimeout
     * @param waiter
     * @return
     * @throws InterruptedIOException
     */
    long waitForCommit(long index, long nanosTimeout, Object waiter)
            throws InterruptedIOException {
        boolean exclusive = false;
        acquireShared();
        while (true) {
            long commitIndex = doAppliableCommitIndex();
            if (commitIndex >= index) {
                release(exclusive);
                return commitIndex;
            }
            if (index > mLogEndIndex || mLogClosed) {
                release(exclusive);
                return WAIT_TERM_END;
            }
            if (exclusive || tryUpgrade()) {
                break;
            }
            releaseShared();
            acquireExclusive();
            exclusive = true;
        }

        DelayedWaiter dwaiter;
        try {
            dwaiter = cLocalDelayed.get();
            if (dwaiter == null) {
                dwaiter = new DelayedWaiter(index, Thread.currentThread(), waiter);
                cLocalDelayed.set(dwaiter);
            } else {
                dwaiter.mCounter = index;
                dwaiter.mWaiter = waiter;
                dwaiter.mAppliableIndex = 0;
            }

            mCommitTasks.add(dwaiter);
        } finally {
            releaseExclusive();
        }

        long endNanos = nanosTimeout > 0 ? (System.nanoTime() + nanosTimeout) : 0;

        while (true) {
            if (nanosTimeout < 0) {
                LockSupport.park(waiter);
            } else {
                LockSupport.parkNanos(waiter, nanosTimeout);
            }
            if (Thread.interrupted()) {
                throw new InterruptedIOException();
            }
            long commitIndex = dwaiter.mAppliableIndex;
            if (commitIndex < 0) {
                return commitIndex;
            }
            if (commitIndex >= index) {
                return commitIndex;
            }
            if (nanosTimeout == 0
                    || (nanosTimeout > 0 && (nanosTimeout = endNanos - System.nanoTime()) <= 0)) {
                return WAIT_TIMEOUT;
            }
        }
    }

    void signalClosed(Object waiter) {
        acquireShared();
        try {
            for (Delayed delayed : mCommitTasks) {
                if (delayed instanceof DelayedWaiter) {
                    DelayedWaiter dwaiter = (DelayedWaiter) delayed;
                    if (dwaiter.mWaiter == waiter) {
                        dwaiter.run(WAIT_TERM_END);
                    }
                }
            }
        } finally {
            releaseShared();
        }
    }

    static class DelayedWaiter extends Delayed {
        final Thread mThread;
        Object mWaiter;
        volatile long mAppliableIndex;

        DelayedWaiter(long index, Thread thread, Object waiter) {
            super(index);
            mThread = thread;
            mWaiter = waiter;
        }

        @Override
        protected void doRun(long index) {
            mWaiter = null;
            mAppliableIndex = index;
            LockSupport.unpark(mThread);
        }
    }

    @Override
    public void uponCommit(Delayed task) {
        if (task == null) {
            throw new NullPointerException();
        }

        acquireShared();

        if (tryUponCommit(task, false)) {
            return;
        }

        if (!tryUpgrade()) {
            releaseShared();
            acquireExclusive();
            if (tryUponCommit(task, true)) {
                return;
            }
        }

        try {
            mCommitTasks.add(task);
        } finally {
            releaseExclusive();
        }
    }

    private boolean tryUponCommit(Delayed task, boolean exclusive) {
        long commitIndex = doAppliableCommitIndex();
        long waitFor = task.mCounter;

        if (commitIndex < waitFor) {
            if (mLogClosed || waitFor > mLogEndIndex) {
                commitIndex = WAIT_TERM_END;
            } else {
                return false;
            }
        }

        release(exclusive);
        task.run(commitIndex);

        return true;
    }

    @Override
    public void finishTerm(long endIndex) {
        List<Delayed> removedTasks;

        acquireExclusive();
        try {
            long commitIndex = mLogCommitIndex;
            if (endIndex < commitIndex && commitIndex > mLogStartIndex) {
                throw new IllegalStateException
                        ("Cannot finish term below commit index: " + endIndex + " < " + commitIndex);
            }

            if (endIndex >= mLogEndIndex) {
                mLogEndIndex = endIndex;
                return;
            }

            for (LKey<Segment> key : mSegments) {
                Segment segment = (Segment) key;
                segment.acquireExclusive();
                boolean shouldTruncate = segment.setEndIndex(endIndex);
                segment.releaseExclusive();
                if (shouldTruncate && !mLogClosed) {
                    truncate(segment);
                }
            }

            mLogEndIndex = endIndex;

            if (endIndex < mLogContigIndex) {
                mLogContigIndex = endIndex;
            }

            if (endIndex < mLogHighestIndex) {
                mLogHighestIndex = endIndex;
            }

            if (!mNonContigWriters.isEmpty()) {
                Iterator<SegmentWriter> it = mNonContigWriters.iterator();
                while (it.hasNext()) {
                    SegmentWriter writer = it.next();
                    if (writer.mWriterStartIndex >= endIndex) {
                        it.remove();
                    } else if (endIndex < writer.mWriterHighestIndex) {
                        writer.mWriterHighestIndex = endIndex;
                    }
                }
            }

            removedTasks = new ArrayList<>();

            mCommitTasks.removeIf(task -> {
                if (task.mCounter > endIndex) {
                    removedTasks.add(task);
                    return true;
                }
                return false;
            });
        } finally {
            releaseExclusive();
        }

        for (Delayed task : removedTasks) {
            task.run(WAIT_TERM_END);
        }
    }

    @Override
    public long checkForMissingData(long contigIndex, IndexRange results) {
        acquireShared();
        try {
            if (contigIndex < mLogStartIndex || mLogContigIndex == contigIndex) {
                long expectedIndex = mLogEndIndex;
                if (expectedIndex == Long.MAX_VALUE) {
                    expectedIndex = mLogCommitIndex;
                }

                long missingStartIndex = mLogContigIndex;

                if (!mNonContigWriters.isEmpty()) {
                    SegmentWriter[] writers = mNonContigWriters.toArray
                            (new SegmentWriter[mNonContigWriters.size()]);
                    Arrays.sort(writers);

                    for (SegmentWriter writer : writers) {
                        long missingEndIndex = writer.mWriterStartIndex;
                        if (missingStartIndex < missingEndIndex) {
                            results.range(missingStartIndex, missingEndIndex);
                        }
                        missingStartIndex = writer.mWriterIndex;
                    }
                }

                if (missingStartIndex < expectedIndex) {
                    results.range(missingStartIndex, expectedIndex);
                }
            }

            return mLogContigIndex;
        } finally {
            releaseShared();
        }
    }

    @Override
    public LogWriter openWriter(long startIndex) {
        SegmentWriter writer = mWriterCache.remove(startIndex);

        if (writer == null) {
            writer = new SegmentWriter();

            acquireExclusive();
            try {
                writer.mWriterPrevTerm = startIndex == mLogStartIndex ? mLogPrevTerm : mLogTerm;
                writer.mWriterStartIndex = startIndex;
                writer.mWriterIndex = startIndex;

                if (startIndex > mLogContigIndex && startIndex < mLogEndIndex) {
                    mNonContigWriters.add(writer);
                }
            } finally {
                releaseExclusive();
            }
        }

        return writer;
    }

    @Override
    public LogReader openReader(long startIndex) {
        SegmentReader reader = mReaderCache.remove(startIndex);

        if (reader == null) {
            acquireShared();
            long prevTerm = startIndex <= mLogStartIndex ? mLogPrevTerm : mLogTerm;
            releaseShared();
            reader = new SegmentReader(prevTerm, startIndex);
        }

        return reader;
    }

    @Override
    public void sync() throws IOException {
        IOException ex = null;

        mSyncLatch.acquireExclusive();
        doSync:
        {
            mDirtyLatch.acquireExclusive();

            Segment segment = mFirstDirty;
            if (segment == null) {
                mDirtyLatch.releaseExclusive();
                break doSync;
            }

            Segment last = mLastDirty;
            Segment next = segment.mNextDirty;

            mFirstDirty = next;
            if (next == null) {
                mLastDirty = null;
            } else {
                segment.mNextDirty = null;
            }

            mDirtyLatch.releaseExclusive();

            while (true) {
                try {
                    segment.sync();
                } catch (IOException e) {
                    if (ex == null) {
                        ex = e;
                    }
                }

                if (segment == last) {
                    break doSync;
                }

                segment = next;

                mDirtyLatch.acquireExclusive();

                next = segment.mNextDirty;
                mFirstDirty = next;
                if (next == null) {
                    mLastDirty = null;
                } else {
                    segment.mNextDirty = null;
                }

                mDirtyLatch.releaseExclusive();
            }
        }

        mSyncLatch.releaseExclusive();

        if (ex != null) {
            throw ex;
        }
    }

    @Override
    public void close() throws IOException {
        mSyncLatch.acquireShared();
        try {
            acquireExclusive();
            try {
                mWorker.join(false);
                mLogClosed = true;

                for (LKey<Segment> key : mSegments) {
                    Segment segment = (Segment) key;
                    segment.acquireExclusive();
                    try {
                        segment.close(true);
                    } finally {
                        segment.releaseExclusive();
                    }
                }

                for (Delayed delayed : mCommitTasks) {
                    delayed.run(WAIT_TERM_END);
                }

                mCommitTasks.clear();
            } finally {
                releaseExclusive();
            }
        } finally {
            mSyncLatch.releaseShared();
        }
    }

    @Override
    public String toString() {
        return "TermLog: {prevTerm=" + mLogPrevTerm + ", term=" + mLogTerm +
                ", startIndex=" + mLogStartIndex + ", commitIndex=" + mLogCommitIndex +
                ", highestIndex=" + mLogHighestIndex + ", contigIndex=" + mLogContigIndex +
                ", endIndex=" + mLogEndIndex + '}';
    }

    void addToDirtyList(Segment segment) {
        mDirtyLatch.acquireExclusive();
        Segment last = mLastDirty;
        if (last == null) {
            mFirstDirty = segment;
        } else {
            last.mNextDirty = segment;
        }
        mLastDirty = segment;
        mDirtyLatch.releaseExclusive();
    }

    Segment segmentForWriting(long index) throws IOException {
        LKey<Segment> key = new LKey.Finder<>(index);

        acquireExclusive();
        find:
        try {
            if (index >= mLogEndIndex) {
                releaseExclusive();
                return null;
            }

            Segment startSegment = (Segment) mSegments.floor(key);

            if (startSegment != null && index < startSegment.endIndex()) {
                mSegmentCache.remove(startSegment.cacheKey());
                cRefCountUpdater.getAndIncrement(startSegment);
                return startSegment;
            }

            if (index < Math.max(mLogStartIndex, doAppliableCommitIndex())) {
                return null;
            }

            if (mLogClosed) {
                throw new IOException("Closed");
            }

            long maxLength = maxSegmentLength();
            long startIndex = index;
            if (startSegment != null) {
                startIndex = startSegment.endIndex()
                        + ((index - startSegment.endIndex()) / maxLength) * maxLength;
            }

            Segment nextSegment = (Segment) mSegments.higher(key);
            long endIndex = nextSegment == null ? mLogEndIndex : nextSegment.mStartIndex;
            maxLength = Math.min(maxLength, endIndex - startIndex);

            Segment segment = new Segment(startIndex, maxLength);
            mSegments.add(segment);
            return segment;
        } finally {
            releaseExclusive();
        }
    }

    private long maxSegmentLength() {
        // 1, 2, 4, 8, 16, 32 or 64 MiB
        return (1024 * 1024L) << Math.min(6, mSegments.size());
    }

    Segment segmentForReading(long index) throws IOException {
        LKey<Segment> key = new LKey.Finder<>(index);

        acquireExclusive();
        try {
            Segment segment = (Segment) mSegments.floor(key);
            if (segment != null && index < segment.endIndex()) {
                cRefCountUpdater.getAndIncrement(segment);
                return segment;
            }

            long commitIndex = Math.max(mLogStartIndex, doAppliableCommitIndex());

            if (index < commitIndex) {
                throw new IllegalStateException
                        ("Index is too low: " + index + " < " + commitIndex);
            }
        } finally {
            releaseExclusive();
        }

        return null;
    }

    void writeFinished(SegmentWriter writer, long currentIndex, long highestIndex) {
        acquireExclusive();

        long commitIndex = mLogCommitIndex;
        if (highestIndex < commitIndex) {
            long allowedHighestIndex = Math.min(commitIndex, mLogContigIndex);
            if (highestIndex < allowedHighestIndex) {
                highestIndex = allowedHighestIndex;
            }
        }

        long endIndex = mLogEndIndex;
        if (currentIndex > endIndex) {
            currentIndex = endIndex;
        }
        if (highestIndex > endIndex) {
            highestIndex = endIndex;
        }

        writer.mWriterIndex = currentIndex;

        if (currentIndex > writer.mWriterStartIndex) {
            writer.mWriterPrevTerm = mLogTerm;
        }

        if (highestIndex > writer.mWriterHighestIndex) {
            writer.mWriterHighestIndex = highestIndex;
        }

        long contigIndex = mLogContigIndex;
        if (writer.mWriterStartIndex <= contigIndex) {
            if (currentIndex > contigIndex) {
                contigIndex = currentIndex;

                while (true) {
                    SegmentWriter next = mNonContigWriters.peek();
                    if (next == null || next.mWriterStartIndex > contigIndex) {
                        break;
                    }

                    SegmentWriter removed = mNonContigWriters.remove();
                    assert removed == next;

                    if (next.mWriterIndex > contigIndex) {
                        contigIndex = next.mWriterIndex;
                    }

                    long nextHighest = next.mWriterHighestIndex;
                    if (nextHighest > highestIndex && highestIndex <= contigIndex) {
                        highestIndex = nextHighest;
                    }
                }

                mLogContigIndex = contigIndex;
            }

            applyHighest:
            {
                if (contigIndex == endIndex || contigIndex <= commitIndex) {
                    highestIndex = contigIndex;
                } else if (highestIndex > contigIndex) {
                    break applyHighest;
                }
                if (highestIndex > mLogHighestIndex) {
                    mLogHighestIndex = highestIndex;
                    doCaptureHighest(writer);
                    notifyCommitTasks(doAppliableCommitIndex());
                    return;
                }
            }
        }

        doCaptureHighest(writer);
        releaseExclusive();
    }

    /**
     * 触发(write/commit)
     * notifyCommitTasks换线等待提交的读线程
     *
     * @param commitIndex
     */
    private void notifyCommitTasks(long commitIndex) {
        PriorityQueue<Delayed> tasks = mCommitTasks;

        while (true) {
            Delayed task = tasks.peek();
            if (task == null || commitIndex < task.mCounter) {
                releaseExclusive();
                return;
            }
            Delayed removed = tasks.remove();
            assert removed == task;
            boolean empty = tasks.isEmpty();
            releaseExclusive();
            task.run(commitIndex);
            if (empty) {
                return;
            }
            acquireExclusive();
            commitIndex = doAppliableCommitIndex();
        }
    }

    void release(SegmentWriter writer, boolean recycle) {
        if (recycle) {
            writer = mWriterCache.add(writer);
        }

        if (writer != null) {
            Segment segment = writer.mWriterSegment;
            if (segment != null) {
                writer.mWriterSegment = null;
                unreferenced(segment);
            }
        }
    }

    void release(SegmentReader reader, boolean recycle) {
        if (recycle) {
            reader = mReaderCache.add(reader);
        }

        if (reader != null) {
            Segment segment = reader.mReaderSegment;
            if (segment != null) {
                reader.mReaderSegment = null;
                unreferenced(segment);
            }
        }
    }

    void unreferenced(Segment segment) {
        if (cRefCountUpdater.getAndDecrement(segment) > 0) {
            return;
        }

        Segment toClose = mSegmentCache.add(segment);

        Worker.Task task = new Worker.Task() {
            @Override
            public void run() {
                try {
                    doUnreferenced(segment, toClose);
                } catch (IOException e) {
                    uncaught(e);
                }
            }
        };

        synchronized (mWorker) {
            mWorker.enqueue(task);
        }
    }

    void doUnreferenced(Segment segment, Segment toClose) throws IOException {
        segment.acquireExclusive();
        try {
            if (segment.mRefCount < 0) {
                segment.unmap();
            }
        } finally {
            segment.releaseExclusive();
        }

        if (toClose != null) {
            toClose.acquireExclusive();
            try {
                if (toClose.mRefCount < 0) {
                    toClose.close(false);
                } else {
                    toClose.unmap();
                }
            } finally {
                toClose.releaseExclusive();
            }
        }
    }

    void truncate(Segment segment) {
        Worker.Task task = new Worker.Task() {
            @Override
            public void run() {
                cRefCountUpdater.getAndIncrement(segment);

                try {
                    segment.truncate();
                } catch (IOException e) {
                    uncaught(e);
                }

                if (cRefCountUpdater.getAndDecrement(segment) <= 0) {
                    try {
                        doUnreferenced(segment, mSegmentCache.add(segment));
                    } catch (IOException e) {
                        uncaught(e);
                    }
                }
            }
        };

        synchronized (mWorker) {
            mWorker.enqueue(task);
        }
    }

    void uncaught(IOException e) {
        if (!mLogClosed) {
            acquireShared();
            boolean closed = mLogClosed;
            releaseShared();
            if (closed) {
                Utils.uncaught(e);
            }
        }
    }

    final class SegmentWriter extends LogWriter
            implements LKey<SegmentWriter>, LCache.Entry<SegmentWriter> {
        long mWriterPrevTerm;
        long mWriterStartIndex;
        long mWriterIndex;
        long mWriterHighestIndex;
        Segment mWriterSegment;

        private volatile boolean mWriterClosed;

        private SegmentWriter mCacheNext;
        private SegmentWriter mCacheMoreUsed;
        private SegmentWriter mCacheLessUsed;

        @Override
        public long key() {
            return mWriterStartIndex;
        }

        @Override
        long prevTerm() {
            return mWriterPrevTerm;
        }

        @Override
        public long term() {
            return mLogTerm;
        }

        @Override
        public long termStartIndex() {
            return FileTermLog.this.startIndex();
        }

        @Override
        public long termEndIndex() {
            return FileTermLog.this.endIndex();
        }

        @Override
        public long index() {
            return mWriterIndex;
        }

        @Override
        public long commitIndex() {
            return FileTermLog.this.appliableCommitIndex();
        }

        @Override
        public int write(byte[] data, int offset, int length, long highestIndex)
                throws IOException {
            long index = mWriterIndex;
            Segment segment = mWriterSegment;

            if (segment == null) {
                segment = segmentForWriting(index);
                if (segment == null) {
                    return 0;
                }
                mWriterSegment = segment;
            }

            int total = 0;

            while (true) {
                int amt = segment.write(index, data, offset, length);
                index += amt;
                total += amt;
                length -= amt;
                if (length <= 0) {
                    break;
                }
                offset += amt;
                mWriterSegment = null;
                unreferenced(segment);
                segment = segmentForWriting(index);
                if (segment == null) {
                    break;
                }
                mWriterSegment = segment;
            }

            writeFinished(this, index, highestIndex);

            return total;
        }

        private Segment segmentForWriting(long index) throws IOException {
            if (mWriterClosed) {
                throw new IOException("Closed");
            }
            return FileTermLog.this.segmentForWriting(index);
        }

        @Override
        public long waitForCommit(long index, long nanosTimeout) throws InterruptedIOException {
            return FileTermLog.this.waitForCommit(index, nanosTimeout, this);
        }

        @Override
        void uponCommit(Delayed task) {
            FileTermLog.this.uponCommit(task);
        }

        @Override
        void release() {
            FileTermLog.this.release(this, true);
        }

        @Override
        public void close() {
            mWriterClosed = true;
            FileTermLog.this.release(this, false);
            signalClosed(this);
        }

        @Override
        public long cacheKey() {
            return mWriterIndex;
        }

        @Override
        public SegmentWriter cacheNext() {
            return mCacheNext;
        }

        @Override
        public void cacheNext(SegmentWriter next) {
            mCacheNext = next;
        }

        @Override
        public SegmentWriter cacheMoreUsed() {
            return mCacheMoreUsed;
        }

        @Override
        public void cacheMoreUsed(SegmentWriter more) {
            mCacheMoreUsed = more;
        }

        @Override
        public SegmentWriter cacheLessUsed() {
            return mCacheLessUsed;
        }

        @Override
        public void cacheLessUsed(SegmentWriter less) {
            mCacheLessUsed = less;
        }

        @Override
        public String toString() {
            return "LogWriter: {prevTerm=" + mWriterPrevTerm + ", term=" + term() +
                    ", startIndex=" + mWriterStartIndex + ", index=" + mWriterIndex +
                    ", highestIndex=" + mWriterHighestIndex +
                    ", segment=" + mWriterSegment + '}';
        }
    }

    final class SegmentReader implements LogReader, LCache.Entry<SegmentReader> {
        private long mReaderPrevTerm;
        private long mReaderIndex;
        private long mReaderCommitIndex;
        private long mReaderContigIndex;
        Segment mReaderSegment;

        private volatile boolean mReaderClosed;

        private SegmentReader mCacheNext;
        private SegmentReader mCacheMoreUsed;
        private SegmentReader mCacheLessUsed;

        SegmentReader(long prevTerm, long index) {
            mReaderPrevTerm = prevTerm;
            mReaderIndex = index;
        }

        @Override
        public long prevTerm() {
            return mReaderPrevTerm;
        }

        @Override
        public long term() {
            return mLogTerm;
        }

        @Override
        public long termStartIndex() {
            return FileTermLog.this.startIndex();
        }

        @Override
        public long termEndIndex() {
            return FileTermLog.this.endIndex();
        }

        @Override
        public long index() {
            return mReaderIndex;
        }

        @Override
        public int read(byte[] buf, int offset, int length) throws IOException {
            long index = mReaderIndex;
            long commitIndex = mReaderCommitIndex;
            long avail = commitIndex - index;

            if (avail <= 0) {
                if (length == 0) {
                    return 0;
                }
                commitIndex = waitForCommit(index + 1, -1, this);
                if (commitIndex < 0) {
                    if (mReaderClosed) {
                        throw new IOException("Closed");
                    }
                    return EOF;
                }
                mReaderCommitIndex = commitIndex;
                avail = commitIndex - index;
            }

            return doRead(index, buf, offset, (int) Math.min(length, avail));
        }

        @Override
        public int tryRead(byte[] buf, int offset, int length) throws IOException {
            long index = mReaderIndex;
            long commitIndex = mReaderCommitIndex;
            long avail = commitIndex - index;

            if (avail <= 0) {
                FileTermLog.this.acquireShared();
                commitIndex = doAppliableCommitIndex();
                long endIndex = mLogEndIndex;
                FileTermLog.this.releaseShared();

                mReaderCommitIndex = commitIndex;
                avail = commitIndex - index;

                if (avail <= 0) {
                    return commitIndex == endIndex ? EOF : 0;
                }
            }

            return doRead(index, buf, offset, (int) Math.min(length, avail));
        }

        @Override
        public int tryReadAny(byte[] buf, int offset, int length) throws IOException {
            long index = mReaderIndex;
            long contigIndex = mReaderContigIndex;
            long avail = contigIndex - index;

            if (avail <= 0) {
                FileTermLog.this.acquireShared();
                contigIndex = mLogContigIndex;
                long endIndex = mLogEndIndex;
                FileTermLog.this.releaseShared();

                mReaderContigIndex = contigIndex;
                avail = contigIndex - index;

                if (avail <= 0) {
                    return contigIndex == endIndex ? EOF : 0;
                }
            }

            return doRead(index, buf, offset, (int) Math.min(length, avail));
        }

        private int doRead(long index, byte[] buf, int offset, int length) throws IOException {
            Segment segment = mReaderSegment;
            if (segment == null) {
                if (length == 0) {
                    return 0;
                }
                segment = segmentForReading(index);
                if (segment == null) {
                    return EOF;
                }
                mReaderSegment = segment;
                mReaderPrevTerm = term();
            }

            int amt = segment.read(index, buf, offset, length);

            if (amt <= 0) {
                if (length == 0) {
                    return 0;
                }
                mReaderSegment = null;
                unreferenced(segment);
                segment = segmentForReading(index);
                if (segment == null) {
                    return EOF;
                }
                mReaderSegment = segment;
                amt = segment.read(index, buf, offset, length);
            }

            mReaderIndex = index + amt;
            return amt;
        }

        private Segment segmentForReading(long index) throws IOException {
            if (mReaderClosed) {
                throw new IOException("Closed");
            }
            return FileTermLog.this.segmentForReading(index);
        }

        @Override
        public void release() {
            FileTermLog.this.release(this, true);
        }

        @Override
        public void close() {
            mReaderClosed = true;
            FileTermLog.this.release(this, false);
            signalClosed(this);
        }

        @Override
        public long cacheKey() {
            return mReaderIndex;
        }

        @Override
        public SegmentReader cacheNext() {
            return mCacheNext;
        }

        @Override
        public void cacheNext(SegmentReader next) {
            mCacheNext = next;
        }

        @Override
        public SegmentReader cacheMoreUsed() {
            return mCacheMoreUsed;
        }

        @Override
        public void cacheMoreUsed(SegmentReader more) {
            mCacheMoreUsed = more;
        }

        @Override
        public SegmentReader cacheLessUsed() {
            return mCacheLessUsed;
        }

        @Override
        public void cacheLessUsed(SegmentReader less) {
            mCacheLessUsed = less;
        }

        @Override
        public String toString() {
            return "LogReader: {term=" + mLogTerm + ", index=" + mReaderIndex +
                    ", segment=" + mReaderSegment + '}';
        }
    }

    static final AtomicIntegerFieldUpdater<Segment> cRefCountUpdater =
            AtomicIntegerFieldUpdater.newUpdater(Segment.class, "mRefCount");

    static final AtomicIntegerFieldUpdater<Segment> cDirtyUpdater =
            AtomicIntegerFieldUpdater.newUpdater(Segment.class, "mDirty");

    final class Segment extends Latch implements LKey<Segment>, LCache.Entry<Segment> {
        private static final int OPEN_HANDLE_COUNT = 8;

        final long mStartIndex;
        volatile long mMaxLength;
        volatile int mRefCount;
        private FileIO mFileIO;
        private boolean mSegmentClosed;

        volatile int mDirty;
        Segment mNextDirty;

        private Segment mCacheNext;
        private Segment mCacheMoreUsed;
        private Segment mCacheLessUsed;

        Segment(long startIndex, long maxLength) {
            mStartIndex = startIndex;
            mMaxLength = maxLength;
        }

        File file() {
            long prevTerm = prevTermAt(mStartIndex);
            long term = term();

            StringBuilder b = new StringBuilder();
            b.append(mBase.getPath()).append('.');

            b.append(term).append('.').append(mStartIndex);

            if (prevTerm != term) {
                b.append('.').append(prevTerm);
            }

            return new File(b.toString());
        }

        @Override
        public long key() {
            return mStartIndex;
        }

        long endIndex() {
            return mStartIndex + mMaxLength;
        }

        int write(long index, byte[] data, int offset, int length) throws IOException {
            index -= mStartIndex;
            if (index < 0) {
                return 0;
            }
            long amt = Math.min(mMaxLength - index, length);
            if (amt <= 0) {
                return 0;
            }
            length = (int) amt;

            FileIO io = mFileIO;

            while (true) {
                if (io != null || (io = fileIO()) != null) tryWrite:{
                    try {
                        io.write(index, data, offset, length);
                    } catch (IOException e) {
                        acquireExclusive();
                        if (mFileIO != io) {
                            break tryWrite;
                        }
                        releaseExclusive();
                        throw e;
                    }

                    if (mDirty == 0 && cDirtyUpdater.getAndSet(this, 1) == 0) {
                        addToDirtyList(this);
                    }

                    amt = Math.min(mMaxLength - index, length);

                    if (length > amt) {
                        // Wrote too much.
                        length = (int) Math.max(0, amt);
                        truncate();
                    }

                    return length;
                }

                try {
                    amt = Math.min(mMaxLength - index, length);
                    if (amt <= 0) {
                        return 0;
                    }
                    io = openForWriting();
                } finally {
                    releaseExclusive();
                }

                if (io == null) {
                    return 0;
                }

                io.map();
                length = (int) amt;
            }
        }

        int read(long index, byte[] buf, int offset, int length) throws IOException {
            index -= mStartIndex;
            if (index < 0) {
                throw new IllegalArgumentException();
            }
            long amt = Math.min(mMaxLength - index, length);
            if (amt <= 0) {
                return 0;
            }
            length = (int) amt;

            FileIO io = mFileIO;

            while (true) {
                if (io != null || (io = fileIO()) != null) tryRead:{
                    try {
                        io.read(index, buf, offset, length);
                        return length;
                    } catch (IOException e) {
                        acquireExclusive();
                        if (mFileIO == io) {
                            releaseExclusive();
                            throw e;
                        }
                    }
                }

                try {
                    amt = Math.min(mMaxLength - index, length);
                    if (amt <= 0) {
                        return 0;
                    }
                    io = openForReading();
                } finally {
                    releaseExclusive();
                }

                length = (int) amt;
            }
        }

        private FileIO fileIO() {
            acquireShared();
            FileIO io = mFileIO;
            if (io != null) {
                releaseShared();
            } else if (!tryUpgrade()) {
                releaseShared();
                acquireExclusive();
                io = mFileIO;
                if (io != null) {
                    releaseExclusive();
                }
            }
            return io;
        }

        FileIO openForWriting() throws IOException {
            FileIO io = mFileIO;

            if (io == null) {
                if (mSegmentClosed) {
                    if (mLogClosed) {
                        throw new IOException("Closed");
                    }
                    return null;
                }
                EnumSet<OpenOption> options = EnumSet.of(OpenOption.CLOSE_DONTNEED);
                int handles = 1;
                if (mMaxLength > 0) {
                    options.add(OpenOption.CREATE);
                    handles = OPEN_HANDLE_COUNT;
                }
                io = FileIO.open(file(), options, handles);
                try {
                    io.setLength(mMaxLength, LengthOption.PREALLOCATE_OPTIONAL);
                } catch (IOException e) {
                    Utils.closeQuietly(io);
                    throw e;
                }
                mFileIO = io;
            }

            return io;
        }

        FileIO openForReading() throws IOException {
            FileIO io = mFileIO;

            if (io == null) {
                if (mSegmentClosed) {
                    throw new IOException("Closed");
                }
                EnumSet<OpenOption> options = EnumSet.of(OpenOption.CLOSE_DONTNEED);
                int handles = 1;
                if (mMaxLength > 0) {
                    handles = OPEN_HANDLE_COUNT;
                }
                mFileIO = io = FileIO.open(file(), options, handles);
            }

            return io;
        }

        boolean setEndIndex(long endIndex) {
            long start = mStartIndex;
            if ((start + mMaxLength) <= endIndex) {
                return false;
            }
            mMaxLength = Math.max(0, endIndex - start);
            return true;
        }

        void sync() throws IOException {
            if (mDirty != 0 && cDirtyUpdater.getAndSet(this, 0) != 0) {
                cRefCountUpdater.getAndIncrement(this);
                try {
                    doSync();
                } catch (IOException e) {
                    if (mDirty == 0 && cDirtyUpdater.getAndSet(this, 1) == 0) {
                        addToDirtyList(this);
                    }
                    throw e;
                } finally {
                    unreferenced(this);
                }
            }
        }

        private void doSync() throws IOException {
            FileIO io = mFileIO;

            while (true) {
                if (io != null || (io = fileIO()) != null) trySync:{
                    try {
                        io.sync(false);
                        return;
                    } catch (IOException e) {
                        acquireExclusive();
                        if (mFileIO == io) {
                            releaseExclusive();
                            throw e;
                        }
                    }
                }

                try {
                    if (mMaxLength == 0 || (io = openForWriting()) == null) {
                        return;
                    }
                } finally {
                    releaseExclusive();
                }
            }
        }

        void truncate() throws IOException {
            FileIO io;
            long maxLength;

            acquireExclusive();
            try {
                maxLength = mMaxLength;
                if (maxLength == 0) {
                    close(true);
                    io = null;
                } else if ((io = openForWriting()) == null) {
                    return;
                }
            } finally {
                releaseExclusive();
            }

            if (io == null) {
                file().delete();
            } else {
                io.setLength(maxLength);
            }
        }

        void unmap() throws IOException {
            if (mFileIO != null) {
                mFileIO.unmap();
            }
        }

        void close(boolean permanent) throws IOException {
            if (mFileIO != null) {
                mFileIO.close();
                mFileIO = null;
            }
            if (permanent) {
                mSegmentClosed = true;
            }
        }

        @Override
        public long cacheKey() {
            return key();
        }

        @Override
        public Segment cacheNext() {
            return mCacheNext;
        }

        @Override
        public void cacheNext(Segment next) {
            mCacheNext = next;
        }

        @Override
        public Segment cacheMoreUsed() {
            return mCacheMoreUsed;
        }

        @Override
        public void cacheMoreUsed(Segment more) {
            mCacheMoreUsed = more;
        }

        @Override
        public Segment cacheLessUsed() {
            return mCacheLessUsed;
        }

        @Override
        public void cacheLessUsed(Segment less) {
            mCacheLessUsed = less;
        }

        @Override
        public String toString() {
            return "Segment: {file=" + file() + ", startIndex=" + mStartIndex +
                    ", maxLength=" + mMaxLength + '}';
        }
    }
}
