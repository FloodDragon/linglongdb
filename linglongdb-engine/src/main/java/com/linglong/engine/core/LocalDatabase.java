package com.linglong.engine.core;

import com.linglong.base.common.Crypto;
import com.linglong.base.common.LHashTable;
import com.linglong.base.common.ShutdownHook;
import com.linglong.base.common.Utils;
import com.linglong.base.concurrent.Latch;
import com.linglong.base.exception.*;
import com.linglong.base.io.FileFactory;
import com.linglong.base.io.MappedPageArray;
import com.linglong.base.io.OpenOption;
import com.linglong.base.io.PageArray;
import com.linglong.base.common.KeyComparator;
import com.linglong.engine.config.DatabaseConfig;
import com.linglong.engine.config.DurabilityMode;
import com.linglong.engine.core.page.*;
import com.linglong.engine.core.repl.ReplRedoController;
import com.linglong.engine.core.repl.ReplRedoDecoder;
import com.linglong.engine.core.repl.ReplRedoEngine;
import com.linglong.engine.core.repl.ReplRedoWriter;
import com.linglong.engine.core.tx.RedoLog;
import com.linglong.engine.core.temp.TempFileManager;
import com.linglong.engine.core.temp.TempTree;
import com.linglong.engine.event.EventType;
import com.linglong.engine.event.SafeEventListener;
import com.linglong.engine.extend.RecoveryHandler;
import com.linglong.engine.extend.ReplicationManager;
import com.linglong.engine.extend.TransactionHandler;

import java.io.*;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;

import com.linglong.engine.observer.CompactionObserver;
import com.linglong.engine.observer.VerificationObserver;
import com.linglong.engine.core.frame.*;
import com.linglong.engine.core.lock.*;
import com.linglong.engine.core.tx.*;

import static java.lang.System.arraycopy;
import static java.util.Arrays.fill;

/**
 * 数据库实现
 *
 * @author Stereo
 */
final public class LocalDatabase extends AbstractDatabase {
    private static final int DEFAULT_CACHED_NODES = 1000;
    private static final int MIN_CACHED_NODES = 5;

    private static final long PRIMER_MAGIC_NUMBER = 4943712973215968399L;

    private static final String INFO_FILE_SUFFIX = ".info";
    private static final String LOCK_FILE_SUFFIX = ".lock";
    public static final String PRIMER_FILE_SUFFIX = ".primer";
    public static final String REDO_FILE_SUFFIX = ".redo.";

    private static int nodeCountFromBytes(long bytes, int pageSize) {
        if (bytes <= 0) {
            return 0;
        }
        pageSize += DirectPageOps.NODE_OVERHEAD;
        bytes += pageSize - 1;
        if (bytes <= 0) {
            // Overflow.
            return Integer.MAX_VALUE;
        }
        long count = bytes / pageSize;
        return count <= Integer.MAX_VALUE ? (int) count : Integer.MAX_VALUE;
    }

    private static long byteCountFromNodes(int nodes, int pageSize) {
        return nodes * (long) (pageSize + DirectPageOps.NODE_OVERHEAD);
    }

    private static final int ENCODING_VERSION = 20130112;

    private static final int I_ENCODING_VERSION = 0;
    private static final int I_ROOT_PAGE_ID = I_ENCODING_VERSION + 4;
    private static final int I_MASTER_UNDO_LOG_PAGE_ID = I_ROOT_PAGE_ID + 8;
    private static final int I_TRANSACTION_ID = I_MASTER_UNDO_LOG_PAGE_ID + 8;
    private static final int I_CHECKPOINT_NUMBER = I_TRANSACTION_ID + 8;
    private static final int I_REDO_TXN_ID = I_CHECKPOINT_NUMBER + 8;
    private static final int I_REDO_POSITION = I_REDO_TXN_ID + 8;
    private static final int I_REPL_ENCODING = I_REDO_POSITION + 8;
    private static final int HEADER_SIZE = I_REPL_ENCODING + 8;

    private static final int DEFAULT_PAGE_SIZE = 4096;
    private static final int MINIMUM_PAGE_SIZE = 512;
    private static final int MAXIMUM_PAGE_SIZE = 65536;

    private static final int OPEN_REGULAR = 0, OPEN_DESTROY = 1, OPEN_TEMP = 2;

    final com.linglong.engine.event.EventListener mEventListener;

    final TransactionHandler mCustomTxnHandler;

    public TransactionHandler getCustomTxnHandler() {
        return mCustomTxnHandler;
    }

    final RecoveryHandler mRecoveryHandler;

    public RecoveryHandler getRecoveryHandler() {
        return mRecoveryHandler;
    }

    private LHashTable.Obj<LocalTransaction> mRecoveredTransactions;

    private final File mBaseFile;
    private final boolean mReadOnly;
    private final LockedFile mLockFile;

    final DurabilityMode mDurabilityMode;
    final long mDefaultLockTimeoutNanos;
    public final LockManager mLockManager;
    private final ThreadLocal<SoftReference<LocalTransaction>> mLocalTransaction;
    final RedoWriter mRedoWriter;
    final PageDb mPageDb;
    final int mPageSize;

    private final PagePool mSparePagePool;

    private final Object mArena;
    private final NodeContext[] mNodeContexts;

    private final CommitLock mCommitLock;

    private byte mCommitState;

    private volatile byte mInitialReadState = Node.CACHED_CLEAN;

    private volatile long mCommitHeader = DirectPageOps.p_null();
    private static final AtomicLongFieldUpdater<LocalDatabase> cCommitHeaderUpdater =
            AtomicLongFieldUpdater.newUpdater(LocalDatabase.class, "mCommitHeader");
    private UndoLog mCommitMasterUndoLog;

    private volatile int mCheckpointFlushState = CHECKPOINT_NOT_FLUSHING;

    private static final int CHECKPOINT_FLUSH_PREPARE = -2, CHECKPOINT_NOT_FLUSHING = -1;

    private final Tree mRegistry;

    static final byte KEY_TYPE_INDEX_NAME = 0; // prefix for name to id mapping
    static final byte KEY_TYPE_INDEX_ID = 1; // prefix for id to name mapping
    static final byte KEY_TYPETree_ID_MASK = 2; // full key for random tree id mask
    static final byte KEY_TYPE_NEXTTree_ID = 3; // full key for tree id sequence
    static final byte KEY_TYPE_TRASH_ID = 4; // prefix for id to name mapping of trash

    private final Tree mRegistryKeyMap;

    private final Latch mOpenTreesLatch;
    private final Map<byte[], TreeRef> mOpenTrees;
    private final LHashTable.Obj<TreeRef> mOpenTreesById;
    private final ReferenceQueue<Tree> mOpenTreesRefQueue;
    private final BiConsumer<Database, Index> mIndexOpenListener;

    private final Node[] mNodeMapTable;
    private final Latch[] mNodeMapLatches;

    final int mMaxKeySize;
    final int mMaxEntrySize;
    final int mMaxFragmentedEntrySize;

    private volatile FragmentedTrash mFragmentedTrash;

    private final long[] mFragmentInodeLevelCaps;

    private final TransactionContext[] mTxnContexts;

    private final ReentrantLock mCheckpointLock = new ReentrantLock(true);

    private long mLastCheckpointNanos;

    private final Checkpointer mCheckpointer;

    final TempFileManager mTempFileManager;

    public TempFileManager getTempFileManager() {
        return mTempFileManager;
    }

    final boolean mFullyMapped;

    private volatile ExecutorService mSorterExecutor;

    private Tree mCursorRegistry;

    private volatile int mClosed;
    private volatile Throwable mClosedCause;

    private static final AtomicIntegerFieldUpdater<LocalDatabase>
            cClosedUpdater = AtomicIntegerFieldUpdater.newUpdater(LocalDatabase.class, "mClosed");

    public static LocalDatabase open(DatabaseConfig config) throws IOException {
        config = config.clone();
        LocalDatabase db = new LocalDatabase(config, OPEN_REGULAR);
        try {
            db.finishInit(config);
            return db;
        } catch (Throwable e) {
            Utils.closeQuietly(db);
            throw e;
        }
    }

    public static LocalDatabase destroy(DatabaseConfig config) throws IOException {
        config = config.clone();
        if (config.isReadOnly()) {
            throw new IllegalArgumentException("Cannot destroy read-only database");
        }
        LocalDatabase db = new LocalDatabase(config, OPEN_DESTROY);
        try {
            db.finishInit(config);
            return db;
        } catch (Throwable e) {
            Utils.closeQuietly(db);
            throw e;
        }
    }

    public static Tree openTemp(TempFileManager tfm, DatabaseConfig config) throws IOException {
        File file = tfm.createTempFile();
        config.baseFile(file);
        config.dataFile(file);
        config.createFilePath(false);
        config.durabilityMode(DurabilityMode.NO_FLUSH);
        LocalDatabase db = new LocalDatabase(config, OPEN_TEMP);
        tfm.register(file, db);
        db.mCheckpointer.start(false);
        return db.mRegistry;
    }

    private LocalDatabase(DatabaseConfig config, int openMode) throws IOException {
        mEventListener = SafeEventListener.makeSafe(config.getEventListener());
        config.setEventListener(mEventListener);
        mIndexOpenListener = config.getIndexOpenListener();

        mCustomTxnHandler = config.getTxnHandler();
        mRecoveryHandler = config.getRecoveryHandler();

        mBaseFile = config.getBaseFile();
        mReadOnly = config.isReadOnly();
        final File[] dataFiles = config.dataFiles();
        int pageSize = config.getPageSize();
        boolean explicitPageSize = true;
        if (pageSize <= 0) {
            config.pageSize(pageSize = DEFAULT_PAGE_SIZE);
            explicitPageSize = false;
        } else if (pageSize < MINIMUM_PAGE_SIZE) {
            throw new IllegalArgumentException
                    ("Page size is too small: " + pageSize + " < " + MINIMUM_PAGE_SIZE);
        } else if (pageSize > MAXIMUM_PAGE_SIZE) {
            throw new IllegalArgumentException
                    ("Page size is too large: " + pageSize + " > " + MAXIMUM_PAGE_SIZE);
        } else if ((pageSize & 1) != 0) {
            throw new IllegalArgumentException
                    ("Page size must be even: " + pageSize);
        }

        int minCache, maxCache;
        cacheSize:
        {
            long minCachedBytes = Math.max(0, config.getMinCachedBytes());
            long maxCachedBytes = Math.max(0, config.getMaxCachedBytes());

            if (maxCachedBytes == 0) {
                maxCachedBytes = minCachedBytes;
                if (maxCachedBytes == 0) {
                    minCache = maxCache = DEFAULT_CACHED_NODES;
                    break cacheSize;
                }
            }

            if (minCachedBytes > maxCachedBytes) {
                throw new IllegalArgumentException
                        ("Minimum cache size exceeds maximum: " +
                                minCachedBytes + " > " + maxCachedBytes);
            }

            minCache = nodeCountFromBytes(minCachedBytes, pageSize);
            maxCache = nodeCountFromBytes(maxCachedBytes, pageSize);

            minCache = Math.max(MIN_CACHED_NODES, minCache);
            maxCache = Math.max(MIN_CACHED_NODES, maxCache);
        }

        config.setMinCachedBytes(byteCountFromNodes(minCache, pageSize));
        config.setMaxCachedBytes(byteCountFromNodes(maxCache, pageSize));

        mDurabilityMode = config.getDurabilityMode();
        mDefaultLockTimeoutNanos = config.getLockTimeoutNanos();
        mLockManager = new LockManager(this, config.getLockUpgradeRule(), mDefaultLockTimeoutNanos);
        mLocalTransaction = new ThreadLocal<>();

        final int procCount = Runtime.getRuntime().availableProcessors();
        {
            int capacity = Utils.roundUpPower2(maxCache);
            if (capacity < 0) {
                capacity = 0x40000000;
            }
            int latches = Math.min(capacity, Utils.roundUpPower2(procCount * 16));
            mNodeMapTable = new Node[capacity];
            mNodeMapLatches = new Latch[latches];
            for (int i = 0; i < latches; i++) {
                mNodeMapLatches[i] = new Latch();
            }
        }

        if (mBaseFile != null && !mReadOnly && config.isMkdirs()) {
            FileFactory factory = config.getFileFactory();

            final boolean baseDirectoriesCreated;
            File baseDir = mBaseFile.getParentFile();
            if (factory == null) {
                baseDirectoriesCreated = baseDir.mkdirs();
            } else {
                baseDirectoriesCreated = factory.createDirectories(baseDir);
            }

            if (!baseDirectoriesCreated && !baseDir.exists()) {
                throw new FileNotFoundException("Could not create directory: " + baseDir);
            }

            if (dataFiles != null) {
                for (File f : dataFiles) {
                    final boolean dataDirectoriesCreated;
                    File dataDir = f.getParentFile();
                    if (factory == null) {
                        dataDirectoriesCreated = dataDir.mkdirs();
                    } else {
                        dataDirectoriesCreated = factory.createDirectories(dataDir);
                    }

                    if (!dataDirectoriesCreated && !dataDir.exists()) {
                        throw new FileNotFoundException("Could not create directory: " + dataDir);
                    }
                }
            }
        }

        try {
            if (mBaseFile == null || openMode == OPEN_TEMP) {
                mLockFile = null;
            } else {
                File lockFile = new File(mBaseFile.getPath() + LOCK_FILE_SUFFIX);

                FileFactory factory = config.getFileFactory();
                if (factory != null && !mReadOnly) {
                    factory.createFile(lockFile);
                }

                mLockFile = new LockedFile(lockFile, mReadOnly);
            }

            if (openMode == OPEN_DESTROY) {
                deleteRedoLogFiles();
            }

            final long cacheInitStart = System.nanoTime();

            PageCache cache = config.pageCache(mEventListener);

            if (cache != null) {
                config.setSecondaryCacheSize(cache.capacity());
            }

            boolean fullyMapped = false;

            com.linglong.engine.event.EventListener debugListener = null;
            if (config.getDebugOpen() != null) {
                debugListener = mEventListener;
            }

            if (dataFiles == null) {
                PageArray dataPageArray = config.getDataPageArray();
                if (dataPageArray == null) {
                    mPageDb = new NonPageDb(pageSize, cache);
                } else {
                    dataPageArray = dataPageArray.open();
                    Crypto crypto = config.getCrypto();
                    mPageDb = DurablePageDb.open
                            (debugListener, dataPageArray, cache, crypto, openMode == OPEN_DESTROY);
                    fullyMapped = crypto == null && cache == null
                            && dataPageArray instanceof MappedPageArray;
                }
            } else {
                EnumSet<OpenOption> options = config.createOpenOptions();

                PageDb pageDb;
                try {
                    pageDb = DurablePageDb.open
                            (debugListener, explicitPageSize, pageSize,
                                    dataFiles, config.getFileFactory(), options,
                                    cache, config.getCrypto(), openMode == OPEN_DESTROY);
                } catch (FileNotFoundException e) {
                    if (!mReadOnly) {
                        throw e;
                    }
                    pageDb = new NonPageDb(pageSize, cache);
                }

                mPageDb = pageDb;
            }

            mFullyMapped = fullyMapped;

            config.pageSize(pageSize = mPageSize = mPageDb.pageSize());

            // config.setDirectPageAccess(false);
            config.setDirectPageAccess(true);

            if (mBaseFile != null && openMode != OPEN_TEMP && !mReadOnly) {
                File infoFile = new File(mBaseFile.getPath() + INFO_FILE_SUFFIX);

                FileFactory factory = config.getFileFactory();
                if (factory != null) {
                    factory.createFile(infoFile);
                }

                BufferedWriter w = new BufferedWriter
                        (new OutputStreamWriter(new FileOutputStream(infoFile),
                                StandardCharsets.UTF_8));

                try {
                    config.writeInfo(w);
                } finally {
                    w.close();
                }
            }

            mCommitLock = mPageDb.commitLock();

            if (mEventListener != null) {
                mEventListener.notify(EventType.CACHE_INIT_BEGIN,
                        "Initializing %1$d cached nodes", minCache);
            }

            NodeContext[] contexts;
            try {
                arenaAlloc:
                {
                    if (mFullyMapped) {
                        mArena = null;
                        break arenaAlloc;
                    }

                    try {
                        mArena = DirectPageOps.p_arenaAlloc(pageSize, minCache);
                    } catch (IOException e) {
                        OutOfMemoryError oom = new OutOfMemoryError();
                        oom.initCause(e);
                        throw oom;
                    }
                }

                long usedRate = Utils.roundUpPower2((long) Math.ceil(maxCache / 32768)) - 1;

                int stripes = Utils.roundUpPower2(procCount * 4);

                int stripeSize;
                while (true) {
                    stripeSize = maxCache / stripes;
                    if (stripes <= 1 || stripeSize >= 100) {
                        break;
                    }
                    stripes >>= 1;
                }

                int rem = maxCache % stripes;

                contexts = new NodeContext[stripes];

                for (int i = 0; i < stripes; i++) {
                    int size = stripeSize;
                    if (rem > 0) {
                        size++;
                        rem--;
                    }
                    contexts[i] = new NodeContext(this, usedRate, size);
                }

                stripeSize = minCache / stripes;
                rem = minCache % stripes;

                for (NodeContext context : contexts) {
                    int size = stripeSize;
                    if (rem > 0) {
                        size++;
                        rem--;
                    }
                    context.initialize(mArena, size);
                }
            } catch (OutOfMemoryError e) {
                contexts = null;
                OutOfMemoryError oom = new OutOfMemoryError
                        ("Unable to allocate the minimum required number of cached nodes: " +
                                minCache + " (" + (minCache * (long) (pageSize + DirectPageOps.NODE_OVERHEAD)) + " bytes)");
                oom.initCause(e.getCause());
                throw oom;
            }

            mNodeContexts = contexts;

            if (mEventListener != null) {
                double duration = (System.nanoTime() - cacheInitStart) / 1_000_000_000.0;
                mEventListener.notify(EventType.CACHE_INIT_COMPLETE,
                        "Cache initialization completed in %1$1.3f seconds",
                        duration, TimeUnit.SECONDS);
            }

            mTxnContexts = new TransactionContext[procCount * 4];
            for (int i = 0; i < mTxnContexts.length; i++) {
                mTxnContexts[i] = new TransactionContext(mTxnContexts.length, 4096);
            }
            ;

            mSparePagePool = new PagePool(mPageSize, procCount, mPageDb.isDirectIO());

            mCommitLock.acquireExclusive();
            try {
                mCommitState = Node.CACHED_DIRTY_0;
            } finally {
                mCommitLock.releaseExclusive();
            }

            byte[] header = new byte[HEADER_SIZE];
            mPageDb.readExtraCommitData(header);

            //db分析: 读取树的根节点
            Node rootNode = loadRegistryRoot(config, header);
            //db分析: 此处是加载树的根节点，以及构建数结构
            if (config.getReplManager() != null) {
                mRegistry = new TxnTree(this, Tree.REGISTRY_ID, null, rootNode);
            } else {
                mRegistry = new Tree(this, Tree.REGISTRY_ID, null, rootNode);

            }
            mOpenTreesLatch = new Latch();
            if (openMode == OPEN_TEMP) {
                mOpenTrees = Collections.emptyMap();
                mOpenTreesById = new LHashTable.Obj<>(0);
                mOpenTreesRefQueue = null;
            } else {
                mOpenTrees = new ConcurrentSkipListMap<>(KeyComparator.THE);
                mOpenTreesById = new LHashTable.Obj<>(16);
                mOpenTreesRefQueue = new ReferenceQueue<>();
            }

            long txnId = Utils.decodeLongLE(header, I_TRANSACTION_ID);
            if (txnId < 0) {
                throw new CorruptDatabaseException("Invalid transaction id: " + txnId);
            }

            long redoNum = Utils.decodeLongLE(header, I_CHECKPOINT_NUMBER);
            long redoPos = Utils.decodeLongLE(header, I_REDO_POSITION);
            long redoTxnId = Utils.decodeLongLE(header, I_REDO_TXN_ID);

            if (debugListener != null) {
                debugListener.notify(EventType.DEBUG, "MASTER_UNDO_LOG_PAGE_ID: %1$d",
                        Utils.decodeLongLE(header, I_MASTER_UNDO_LOG_PAGE_ID));
                debugListener.notify(EventType.DEBUG, "TRANSACTION_ID: %1$d", txnId);
                debugListener.notify(EventType.DEBUG, "CHECKPOINT_NUMBER: %1$d", redoNum);
                debugListener.notify(EventType.DEBUG, "REDO_TXN_ID: %1$d", redoTxnId);
                debugListener.notify(EventType.DEBUG, "REDO_POSITION: %1$d", redoPos);
            }

            if (openMode == OPEN_TEMP) {
                mRegistryKeyMap = null;
            } else {
                mRegistryKeyMap = openInternalTree(Tree.REGISTRY_KEY_MAP_ID, true, config);
                if (debugListener != null) {
                    Cursor c = indexRegistryById().newCursor(Transaction.BOGUS);
                    try {
                        for (c.first(); c.key() != null; c.next()) {
                            long indexId = Utils.decodeLongBE(c.key(), 0);
                            String nameStr = new String(c.value(), StandardCharsets.UTF_8);
                            debugListener.notify(EventType.DEBUG, "Index: id=%1$d, name=%2$s",
                                    indexId, nameStr);
                        }
                    } finally {
                        c.reset();
                    }
                }
            }

            Tree cursorRegistry = null;
            if (openMode != OPEN_TEMP) {
                Tree tree = openInternalTree(Tree.FRAGMENTED_TRASH_ID, false, config);
                if (tree != null) {
                    mFragmentedTrash = new FragmentedTrash(tree);
                }
                cursorRegistry = openInternalTree(Tree.CURSOR_REGISTRY_ID, false, config);
            }

            mMaxEntrySize = ((pageSize - Node.TN_HEADER_SIZE) * 3) >> 2;

            mMaxFragmentedEntrySize = (pageSize - Node.TN_HEADER_SIZE - (2 + 3 + 2 + 3)) >> 1;

            mMaxKeySize = Math.min(16383, mMaxFragmentedEntrySize - (2 + 11));

            mFragmentInodeLevelCaps = calculateInodeLevelCaps(mPageSize);

            long recoveryStart = 0;
            if (mBaseFile == null) {
                mRedoWriter = null;
                mCheckpointer = null;
            } else if (openMode == OPEN_TEMP) {
                mRedoWriter = null;
                mCheckpointer = new Checkpointer(this, config, mNodeContexts.length);
            } else {
                if (debugListener != null) {
                    mCheckpointer = null;
                } else {
                    mCheckpointer = new Checkpointer(this, config, mNodeContexts.length);
                }

                if (mEventListener != null) {
                    mEventListener.notify(EventType.RECOVERY_BEGIN, "Database recovery begin");
                    recoveryStart = System.nanoTime();
                }

                LHashTable.Obj<LocalTransaction> txns = new LHashTable.Obj<>(16);
                {
                    long masterNodeId = Utils.decodeLongLE(header, I_MASTER_UNDO_LOG_PAGE_ID);
                    if (masterNodeId != 0) {
                        if (mEventListener != null) {
                            mEventListener.notify
                                    (EventType.RECOVERY_LOAD_UNDO_LOGS, "Loading undo logs");
                        }

                        UndoLog master = UndoLog.recoverMasterUndoLog(this, masterNodeId);

                        boolean trace = debugListener != null &&
                                Boolean.TRUE.equals(config.getDebugOpen().get("traceUndo"));

                        master.recoverTransactions
                                (debugListener, trace, txns, LockMode.UPGRADABLE_READ, 0);
                    }
                }

                LHashTable.Obj<TreeCursor> cursors = new LHashTable.Obj<>(4);
                if (cursorRegistry != null) {
                    Cursor c = cursorRegistry.newCursor(Transaction.BOGUS);
                    for (c.first(); c.key() != null; c.next()) {
                        long cursorId = Utils.decodeLongBE(c.key(), 0);
                        long indexId = Utils.decodeLongBE(c.value(), 0);
                        Tree tree = (Tree) anyIndexById(indexId);
                        TreeCursor cursor = new TreeCursor(tree);
                        cursor.mKeyOnly = true;
                        cursor.mCursorId = cursorId;
                        cursors.insert(cursorId).value = cursor;
                    }
                    cursorRegistry.forceClose();
                }

                if (mCustomTxnHandler != null) {
                    mCustomTxnHandler.setCheckpointLock(this, mCommitLock);
                }

                ReplicationManager rm = config.getReplManager();
                if (rm != null) {
                    if (mEventListener != null) {
                        mEventListener.notify(EventType.REPLICATION_DEBUG,
                                "Starting at: %1$d", redoPos);
                    }

                    rm.start(redoPos);

                    if (mReadOnly) {
                        mRedoWriter = null;

                        if (debugListener != null &&
                                Boolean.TRUE.equals(config.getDebugOpen().get("traceRedo"))) {
                            RedoEventPrinter printer = new RedoEventPrinter
                                    (debugListener, EventType.DEBUG);
                            new ReplRedoDecoder(rm, redoPos, redoTxnId, new Latch()).run(printer);
                        }
                    } else {
                        ReplRedoEngine engine = new ReplRedoEngine
                                (rm, config.getMaxReplicaThreads(), this, txns, cursors);
                        mRedoWriter = engine.initWriter(redoNum);

                        config.setReplRecoveryStartNanos(recoveryStart);
                        config.setReplInitialTxnId(redoTxnId);
                    }
                } else {
                    applyCachePrimer(config);

                    final long logId = redoNum;

                    if (mReadOnly) {
                        mRedoWriter = null;

                        if (debugListener != null &&
                                Boolean.TRUE.equals(config.getDebugOpen().get("traceRedo"))) {
                            RedoEventPrinter printer = new RedoEventPrinter
                                    (debugListener, EventType.DEBUG);

                            RedoLog replayLog = new RedoLog(config, logId, redoPos);

                            replayLog.replay
                                    (printer, debugListener, EventType.RECOVERY_APPLY_REDO_LOG,
                                            "Applying redo log: %1$d");
                        }
                    } else {
                        for (int i = 1; i <= 2; i++) {
                            RedoLog.deleteOldFile(config.getBaseFile(), logId - i);
                        }

                        RedoLogApplier applier = new RedoLogApplier(this, txns, cursors);
                        RedoLog replayLog = new RedoLog(config, logId, redoPos);

                        Set<File> redoFiles = replayLog.replay
                                (applier, mEventListener, EventType.RECOVERY_APPLY_REDO_LOG,
                                        "Applying redo log: %1$d");

                        boolean doCheckpoint = !redoFiles.isEmpty();

                        redoTxnId = applier.getHighestTxnId();
                        if (redoTxnId != 0) {
                            if (txnId == 0 || (redoTxnId - txnId) > 0) {
                                txnId = redoTxnId;
                            }
                        }

                        if (txns.size() > 0) {
                            if (mEventListener != null) {
                                mEventListener.notify
                                        (EventType.RECOVERY_PROCESS_REMAINING,
                                                "Processing remaining transactions");
                            }

                            txns.traverse(entry -> {
                                return entry.value.recoveryCleanup(true);
                            });

                            if (shouldInvokeRecoveryHandler(txns)) {
                                mRecoveredTransactions = txns;
                            }

                            doCheckpoint = true;
                        }

                        applier.resetCursors();

                        mRedoWriter = new RedoLog(config, replayLog, mTxnContexts[0]);

                        // TODO: 如果在检查点完成之前抛出任何异常

                        if (doCheckpoint) {
                            resetTransactionContexts(txnId);
                            txnId = -1;

                            checkpoint(true, 0, 0);

                            for (File file : redoFiles) {
                                file.delete();
                            }
                        }

                        emptyAllFragmentedTrash(true);
                    }
                    recoveryComplete(recoveryStart);
                }
            }

            if (txnId >= 0) {
                resetTransactionContexts(txnId);
            }

            if (mBaseFile == null || openMode == OPEN_TEMP || debugListener != null) {
                mTempFileManager = null;
            } else {
                mTempFileManager = new TempFileManager(mBaseFile, config.getFileFactory());
            }
        } catch (Throwable e) {
            Utils.closeQuietly(this);
            throw e;
        }
    }

    private void debugTest(Tree tree) {
        try {
            //******************************************** 读取TreeIndex目录 辅助读取内容后期删除掉 ********************************************
            Cursor namesCursor = tree.newCursor(null);
            try {
                namesCursor.first();
                byte[] key;
                while ((key = namesCursor.key()) != null) {
                    byte[] value = namesCursor.value();
                    System.out.println("key=" + Arrays.toString(key));
                    System.out.println("key string=" + new String(key));
                    if (value != null && value.length > 0) {
                        System.out.println("value=" + Arrays.toString(value));
                        System.out.println("value string=" + new String(value));
                    }
                    System.out.println("********************************************************************************************");
                    namesCursor.next();
                }
            } finally {
                namesCursor.reset();
                namesCursor.close();

            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void finishInit(DatabaseConfig config) throws IOException {
        if (mCheckpointer == null) {
            return;
        }

        mCheckpointer.register(new RedoClose(this));
        mCheckpointer.register(mTempFileManager);

        if (mRedoWriter instanceof ReplRedoWriter) {
            applyCachePrimer(config);
        }

        if (config.isCachePriming() && mPageDb.isDurable() && !mReadOnly) {
            mCheckpointer.register(new ShutdownPrimer(this));
        }

        Tree trashed = openNextTrashedTree(null);

        if (trashed != null) {
            Thread deletion = new Thread
                    (new Deletion(trashed, true, mEventListener), "IndexDeletion");
            deletion.setDaemon(true);
            deletion.start();
        }

        if (mRecoveryHandler != null) {
            mRecoveryHandler.init(this);
        }

        boolean initialCheckpoint = false;

        if (mRedoWriter instanceof ReplRedoController) {
            ReplRedoController controller = (ReplRedoController) mRedoWriter;

            try {
                controller.ready(config.getReplInitialTxnId(), new ReplicationManager.Accessor() {
                    @Override
                    public void notify(EventType type, String message, Object... args) {
                        com.linglong.engine.event.EventListener listener = mEventListener;
                        if (listener != null) {
                            listener.notify(type, message, args);
                        }
                    }

                    @Override
                    public Database database() {
                        return LocalDatabase.this;
                    }

                    @Override
                    public long control(byte[] message) throws IOException {
                        return writeControlMessage(message);
                    }
                });
            } catch (Throwable e) {
                Utils.closeQuietly(this, e);
                throw e;
            }

            recoveryComplete(config.getReplRecoveryStartNanos());
            initialCheckpoint = true;
        }

        mCheckpointer.start(initialCheckpoint);

        LHashTable.Obj<LocalTransaction> txns = mRecoveredTransactions;
        if (txns != null) {
            new Thread(() -> {
                invokeRecoveryHandler(txns, mRedoWriter);
            }).start();
            mRecoveredTransactions = null;
        }
    }

    /**
     * 写入控制信息
     *
     * @param message
     * @return
     * @throws IOException
     */
    private long writeControlMessage(byte[] message) throws IOException {
        CommitLock.Shared shared = mCommitLock.acquireShared();
        try {
            RedoWriter redo = txnRedoWriter();
            TransactionContext context = anyTransactionContext();
            //写入控制信息到重做文件(集群复制下同步到其他节点)
            long commitPos = context.redoControl(redo, message);

            redo.commitSync(context, commitPos);

            try {
                //集群复制下调用controlMessageReceived接收控制信息
                ((ReplRedoController) mRedoWriter).getManager().control(commitPos, message);
            } catch (Throwable e) {
                Utils.closeQuietly(this, e);
                throw e;
            }

            return commitPos;
        } finally {
            shared.release();
        }
    }

    private void applyCachePrimer(DatabaseConfig config) {
        if (mPageDb.isDurable()) {
            File primer = primerFile();
            try {
                if (config.isCachePriming() && primer.exists()) {
                    if (mEventListener != null) {
                        mEventListener.notify(EventType.RECOVERY_CACHE_PRIMING,
                                "Cache priming");
                    }
                    FileInputStream fin;
                    try {
                        fin = new FileInputStream(primer);
                        try (InputStream bin = new BufferedInputStream(fin)) {
                            applyCachePrimer(bin);
                        } catch (IOException e) {
                            fin.close();
                        }
                    } catch (IOException e) {
                    }
                }
            } finally {
                if (!mReadOnly) {
                    primer.delete();
                }
            }
        }
    }

    public boolean shouldInvokeRecoveryHandler(LHashTable.Obj<LocalTransaction> txns) {
        if (txns != null && txns.size() != 0) {
            if (mRecoveryHandler != null) {
                return true;
            }
            if (mEventListener != null) {
                mEventListener.notify
                        (EventType.RECOVERY_NO_HANDLER,
                                "No handler is installed for processing the remaining " +
                                        "two-phase commit transactions: %1$d", txns.size());
            }
        }

        return false;
    }

    public void invokeRecoveryHandler(LHashTable.Obj<LocalTransaction> txns, RedoWriter redo) {
        RecoveryHandler handler = mRecoveryHandler;

        txns.traverse(entry -> {
            LocalTransaction txn = entry.value;
            txn.recoverPrepared
                    (redo, mDurabilityMode, LockMode.UPGRADABLE_READ, mDefaultLockTimeoutNanos);

            try {
                handler.recover(txn);
            } catch (Throwable e) {
                if (!isClosed()) {
                    com.linglong.engine.event.EventListener listener = mEventListener;
                    if (listener == null) {
                        Utils.uncaught(e);
                    } else {
                        listener.notify(EventType.RECOVERY_HANDLER_UNCAUGHT,
                                "Uncaught exception from recovery handler: %1$s", e);
                    }
                }
            }

            return true;
        });
    }

    static class ShutdownPrimer extends ShutdownHook.Weak<LocalDatabase> {
        ShutdownPrimer(LocalDatabase db) {
            super(db);
        }

        @Override
        public void doShutdown(LocalDatabase db) {
            if (db.mReadOnly) {
                return;
            }

            File primer = db.primerFile();

            FileOutputStream fout;
            try {
                fout = new FileOutputStream(primer);
                try {
                    try (OutputStream bout = new BufferedOutputStream(fout)) {
                        db.createCachePrimer(bout);
                    }
                } catch (IOException e) {
                    fout.close();
                    primer.delete();
                }
            } catch (IOException e) {
            }
        }
    }

    File primerFile() {
        return new File(mBaseFile.getPath() + PRIMER_FILE_SUFFIX);
    }

    private void recoveryComplete(long recoveryStart) {
        if (mEventListener != null) {
            double duration = (System.nanoTime() - recoveryStart) / 1_000_000_000.0;
            mEventListener.notify(EventType.RECOVERY_COMPLETE,
                    "Recovery completed in %1$1.3f seconds",
                    duration, TimeUnit.SECONDS);
        }
    }

    private void deleteRedoLogFiles() throws IOException {
        if (mBaseFile != null && !mReadOnly) {
            Utils.deleteNumberedFiles(mBaseFile, REDO_FILE_SUFFIX);
        }
    }

    @Override
    public Index findIndex(byte[] name) throws IOException {
        return openTree(name.clone(), false);
    }

    @Override
    public Index openIndex(byte[] name) throws IOException {
        return openTree(name.clone(), true);
    }

    @Override
    public Index indexById(long id) throws IOException {
        return indexById(null, id);
    }

    Index indexById(Transaction txn, long id) throws IOException {
        if (Tree.isInternal(id)) {
            throw new IllegalArgumentException("Invalid id: " + id);
        }

        Index index;

        CommitLock.Shared shared = mCommitLock.acquireShared();
        try {
            if ((index = lookupIndexById(id)) != null) {
                return index;
            }

            byte[] idKey = new byte[9];
            idKey[0] = KEY_TYPE_INDEX_ID;
            Utils.encodeLongBE(idKey, 1, id);

            byte[] name;

            if (txn != null) {
                name = mRegistryKeyMap.load(txn, idKey);
            } else {
                Locker locker = mRegistryKeyMap.lockExclusiveLocal
                        (idKey, LockManager.hash(mRegistryKeyMap.getId(), idKey));
                try {
                    name = mRegistryKeyMap.load(Transaction.BOGUS, idKey);
                } finally {
                    locker.unlock();
                }
            }

            if (name == null) {
                checkClosed();
                return null;
            }

            byte[] treeIdBytes = new byte[8];
            Utils.encodeLongBE(treeIdBytes, 0, id);

            index = openTree(txn, treeIdBytes, name, false);
        } catch (Throwable e) {
            DatabaseException.rethrowIfRecoverable(e);
            throw Utils.closeOnFailure(this, e);
        } finally {
            shared.release();
        }

        if (index == null) {
            throw new DatabaseException("Unable to find index in registry");
        }

        return index;
    }

    private Tree lookupIndexById(long id) {
        mOpenTreesLatch.acquireShared();
        try {
            LHashTable.ObjEntry<TreeRef> entry = mOpenTreesById.get(id);
            return entry == null ? null : entry.value.get();
        } finally {
            mOpenTreesLatch.releaseShared();
        }
    }

    public Index anyIndexById(long id) throws IOException {
        return anyIndexById(null, id);
    }

    public Index anyIndexById(Transaction txn, long id) throws IOException {
        if (id == Tree.REGISTRY_KEY_MAP_ID) {
            return mRegistryKeyMap;
        } else if (id == Tree.FRAGMENTED_TRASH_ID) {
            return fragmentedTrash().mTrash;
        }
        return indexById(txn, id);
    }

    @Override
    public void renameIndex(Index index, byte[] newName) throws IOException {
        renameIndex(index, newName.clone(), 0);
    }

    public void renameIndex(final Index index, final byte[] newName, final long redoTxnId)
            throws IOException {
        final Tree tree = accessTree(index);

        final byte[] idKey, trashIdKey;
        final byte[] oldName, oldNameKey;
        final byte[] newNameKey;

        final LocalTransaction txn;

        final Node root = tree.mRoot;
        root.acquireExclusive();
        try {
            if (root.mPage == DirectPageOps.p_closedTreePage()) {
                throw new ClosedIndexException();
            }

            if (Tree.isInternal(tree.mId)) {
                throw new IllegalStateException("Cannot rename an internal index");
            }

            oldName = tree.mName;

            if (oldName == null) {
                throw new IllegalStateException("Cannot rename a temporary index");
            }

            if (Arrays.equals(oldName, newName)) {
                return;
            }

            idKey = newKey(KEY_TYPE_INDEX_ID, tree.mIdBytes);
            trashIdKey = newKey(KEY_TYPE_TRASH_ID, tree.mIdBytes);
            oldNameKey = newKey(KEY_TYPE_INDEX_NAME, oldName);
            newNameKey = newKey(KEY_TYPE_INDEX_NAME, newName);

            txn = newNoRedoTransaction(redoTxnId);
            try {
                txn.lockTimeout(-1, null);
                txn.lockExclusive(mRegistryKeyMap.mId, idKey);
                txn.lockExclusive(mRegistryKeyMap.mId, trashIdKey);
                if (Utils.compareUnsigned(oldNameKey, newNameKey) <= 0) {
                    txn.lockExclusive(mRegistryKeyMap.mId, oldNameKey);
                    txn.lockExclusive(mRegistryKeyMap.mId, newNameKey);
                } else {
                    txn.lockExclusive(mRegistryKeyMap.mId, newNameKey);
                    txn.lockExclusive(mRegistryKeyMap.mId, oldNameKey);
                }
            } catch (Throwable e) {
                txn.reset();
                throw e;
            }
        } finally {
            root.releaseExclusive();
        }

        try {
            Cursor c = mRegistryKeyMap.newCursor(txn);
            try {
                c.autoload(false);

                c.find(trashIdKey);
                if (c.value() != null) {
                    throw new IllegalStateException("Index is deleted");
                }

                c.find(newNameKey);
                if (c.value() != null) {
                    throw new IllegalStateException("New name is used by another index");
                }

                c.store(tree.mIdBytes);
            } finally {
                c.reset();
            }

            if (redoTxnId == 0 && txn.getRedo() != null) {
                txn.durabilityMode(mDurabilityMode.alwaysRedo());

                long commitPos;
                CommitLock.Shared shared = mCommitLock.acquireShared();
                try {
                    txn.check();
                    commitPos = txn.getContext().redoRenameIndexCommitFinal
                            (txn.getRedo(), txn.txnId(), tree.mId, newName, txn.durabilityMode());
                } finally {
                    shared.release();
                }

                if (commitPos != 0) {
                    txn.getRedo().txnCommitSync(txn, commitPos);
                }
            }

            mRegistryKeyMap.delete(txn, oldNameKey);
            mRegistryKeyMap.store(txn, idKey, newName);

            mOpenTreesLatch.acquireExclusive();
            try {
                txn.commit();

                tree.mName = newName;
                mOpenTrees.put(newName, mOpenTrees.remove(oldName));
            } finally {
                mOpenTreesLatch.releaseExclusive();
            }
        } catch (IllegalStateException e) {
            throw e;
        } catch (Throwable e) {
            DatabaseException.rethrowIfRecoverable(e);
            throw Utils.closeOnFailure(this, e);
        } finally {
            txn.reset();
        }
    }

    private Tree accessTree(Index index) {
        try {
            Tree tree;
            if ((tree = ((Tree) index)).mDatabase == this) {
                return tree;
            }
        } catch (ClassCastException e) {
        }
        throw new IllegalArgumentException("Index belongs to a different database");
    }

    @Override
    public Runnable deleteIndex(Index index) throws IOException {
        return accessTree(index).drop(false);
    }

    public Runnable replicaDeleteTree(long treeId) throws IOException {
        byte[] treeIdBytes = new byte[8];
        Utils.encodeLongBE(treeIdBytes, 0, treeId);

        Tree trashed = openTrashedTree(treeIdBytes, false);

        return new Deletion(trashed, false, null);
    }

    Runnable deleteTree(Tree tree, CommitLock.Shared shared) throws IOException {
        try {
            if (!(tree instanceof TempTree) && !moveToTrash(tree.mId, tree.mIdBytes)) {
                throw new ClosedIndexException();
            }
        } finally {
            shared.release();
        }

        Node root = tree.close(true, true);
        if (root == null) {
            throw new ClosedIndexException();
        }

        Tree trashed = newTreeInstance(tree.mId, tree.mIdBytes, tree.mName, root);

        return new Deletion(trashed, false, null);
    }

    void quickDeleteTemporaryTree(Tree tree) throws IOException {
        mOpenTreesLatch.acquireExclusive();
        try {
            TreeRef ref = mOpenTreesById.removeValue(tree.mId);
            if (ref == null || ref.get() != tree) {
                return;
            }
            ref.clear();
        } finally {
            mOpenTreesLatch.releaseExclusive();
        }

        Node root = tree.mRoot;

        prepare:
        {
            CommitLock.Shared shared = mCommitLock.acquireShared();
            try {
                root.acquireExclusive();
                if (!root.hasKeys()) {
                    prepareToDelete(root);
                    root.releaseExclusive();
                    break prepare;
                }
                root.releaseExclusive();
            } finally {
                shared.release();
            }

            tree.deleteAll();
        }

        removeFromTrash(tree, root);
    }

    private Tree openNextTrashedTree(byte[] lastIdBytes) throws IOException {
        return openTrashedTree(lastIdBytes, true);
    }

    private Tree openTrashedTree(byte[] idBytes, boolean next) throws IOException {
        View view = mRegistryKeyMap.viewPrefix(new byte[]{KEY_TYPE_TRASH_ID}, 1);

        if (idBytes == null) {
            Cursor c = view.newCursor(Transaction.BOGUS);
            try {
                for (c.first(); c.key() != null; c.next()) {
                    byte[] name = c.value();
                    if (name.length != 0) {
                        name[0] |= 0x80;
                        c.store(name);
                    }
                }
            } finally {
                c.reset();
            }
        }

        byte[] treeIdBytes, name, rootIdBytes;

        Cursor c = view.newCursor(Transaction.BOGUS);
        try {
            if (idBytes == null) {
                c.first();
            } else if (next) {
                c.findGt(idBytes);
            } else {
                c.find(idBytes);
            }

            while (true) {
                treeIdBytes = c.key();

                if (treeIdBytes == null) {
                    return null;
                }

                rootIdBytes = mRegistry.load(Transaction.BOGUS, treeIdBytes);

                if (rootIdBytes == null) {
                    c.store(null);
                } else {
                    name = c.value();
                    if (name[0] < 0) {
                        break;
                    }
                }

                if (next) {
                    c.next();
                } else {
                    return null;
                }
            }
        } finally {
            c.reset();
        }

        long rootId = rootIdBytes.length == 0 ? 0 : Utils.decodeLongLE(rootIdBytes, 0);

        if ((name[0] & 0x7f) == 0) {
            name = null;
        } else {
            byte[] actual = new byte[name.length - 1];
            System.arraycopy(name, 1, actual, 0, actual.length);
            name = actual;
        }

        long treeId = Utils.decodeLongBE(treeIdBytes, 0);

        return newTreeInstance(treeId, treeIdBytes, name, loadTreeRoot(treeId, rootId));
    }

    private class Deletion implements Runnable {
        private Tree mTrashed;
        private final boolean mResumed;
        private final com.linglong.engine.event.EventListener mListener;

        Deletion(Tree trashed, boolean resumed, com.linglong.engine.event.EventListener listener) {
            mTrashed = trashed;
            mResumed = resumed;
            mListener = listener;
        }

        @Override
        public synchronized void run() {
            while (mTrashed != null) {
                delete();
            }
        }

        private void delete() {
            if (mListener != null) {
                mListener.notify(EventType.DELETION_BEGIN,
                        "Index deletion " + (mResumed ? "resumed" : "begin") +
                                ": %1$d, name: %2$s",
                        mTrashed.getId(), mTrashed.getNameString());
            }

            final byte[] idBytes = mTrashed.mIdBytes;

            try {
                long start = System.nanoTime();

                if (mTrashed.deleteAll()) {
                    Node root = mTrashed.close(true, false);
                    removeFromTrash(mTrashed, root);
                } else {
                    return;
                }

                if (mListener != null) {
                    double duration = (System.nanoTime() - start) / 1_000_000_000.0;
                    mListener.notify(EventType.DELETION_COMPLETE,
                            "Index deletion complete: %1$d, name: %2$s, " +
                                    "duration: %3$1.3f seconds",
                            mTrashed.getId(), mTrashed.getNameString(), duration);
                }
            } catch (IOException e) {
                if (!isClosed() && mListener != null) {
                    mListener.notify
                            (EventType.DELETION_FAILED,
                                    "Index deletion failed: %1$d, name: %2$s, exception: %3$s",
                                    mTrashed.getId(), mTrashed.getNameString(), Utils.rootCause(e));
                }
                Utils.closeQuietly(mTrashed);
                return;
            } finally {
                mTrashed = null;
            }

            if (mResumed) {
                try {
                    mTrashed = openNextTrashedTree(idBytes);
                } catch (IOException e) {
                    if (!isClosed() && mListener != null) {
                        mListener.notify
                                (EventType.DELETION_FAILED,
                                        "Unable to resume deletion: %1$s", Utils.rootCause(e));
                    }
                    return;
                }
            }
        }
    }

    @Override
    public Tree newTemporaryIndex() throws IOException {
        CommitLock.Shared shared = mCommitLock.acquireShared();
        try {
            return newTemporaryTree(false);
        } finally {
            shared.release();
        }
    }

    Tree newTemporaryTree(boolean preallocate) throws IOException {
        checkClosed();

        cleanupUnreferencedTrees();

        long treeId;
        byte[] treeIdBytes = new byte[8];

        long rootId;
        byte[] rootIdBytes;

        if (preallocate) {
            rootId = mPageDb.allocPage();
            rootIdBytes = new byte[8];
        } else {
            rootId = 0;
            rootIdBytes = Utils.EMPTY_BYTES;
        }

        try {
            do {
                treeId = nextTreeId(true);
                Utils.encodeLongBE(treeIdBytes, 0, treeId);
            } while (!mRegistry.insert(Transaction.BOGUS, treeIdBytes, rootIdBytes));

            Transaction createTxn = newNoRedoTransaction();
            try {
                createTxn.lockTimeout(-1, null);
                byte[] trashIdKey = newKey(KEY_TYPE_TRASH_ID, treeIdBytes);
                if (!mRegistryKeyMap.insert(createTxn, trashIdKey, new byte[1])) {
                    throw new DatabaseException("Unable to register temporary index");
                }
                createTxn.commit();
            } finally {
                createTxn.reset();
            }

            Node root;
            if (rootId != 0) {
                root = allocLatchedNode(rootId, NodeContext.MODE_UNEVICTABLE);
                root.mId = rootId;
                try {
                    if (mFullyMapped) {
                        root.mPage = mPageDb.dirtyPage(rootId);
                    }
                    root.mContext.addDirty(root, mCommitState);
                } catch (Throwable e) {
                    root.releaseExclusive();
                    throw e;
                }
            } else {
                root = loadTreeRoot(treeId, 0);
            }

            try {
                Tree tree = new TempTree(this, treeId, treeIdBytes, root);
                TreeRef treeRef = new TreeRef(tree, mOpenTreesRefQueue);

                mOpenTreesLatch.acquireExclusive();
                try {
                    mOpenTreesById.insert(treeId).value = treeRef;
                } finally {
                    mOpenTreesLatch.releaseExclusive();
                }

                return tree;
            } catch (Throwable e) {
                if (rootId != 0) {
                    root.releaseExclusive();
                }
                throw e;
            }
        } catch (Throwable e) {
            try {
                mRegistry.delete(Transaction.BOGUS, treeIdBytes);
            } catch (Throwable e2) {
                // Panic.
                throw Utils.closeOnFailure(this, e);
            }
            if (rootId != 0) {
                try {
                    mPageDb.recyclePage(rootId);
                } catch (Throwable e2) {
                    Utils.suppress(e, e2);
                }
            }
            throw e;
        }
    }

    @Override
    public View indexRegistryByName() throws IOException {
        return mRegistryKeyMap.viewPrefix(new byte[]{KEY_TYPE_INDEX_NAME}, 1).viewUnmodifiable();
    }

    @Override
    public View indexRegistryById() throws IOException {
        return mRegistryKeyMap.viewPrefix(new byte[]{KEY_TYPE_INDEX_ID}, 1).viewUnmodifiable();
    }

    @Override
    public Transaction newTransaction() {
        return doNewTransaction(mDurabilityMode);
    }

    @Override
    public Transaction newTransaction(DurabilityMode durabilityMode) {
        return doNewTransaction(durabilityMode == null ? mDurabilityMode : durabilityMode);
    }

    private LocalTransaction doNewTransaction(DurabilityMode durabilityMode) {
        RedoWriter redo = txnRedoWriter();
        return new LocalTransaction
                (this, redo, durabilityMode, LockMode.UPGRADABLE_READ, mDefaultLockTimeoutNanos);
    }

    private LocalTransaction newAlwaysRedoTransaction() {
        return doNewTransaction(mDurabilityMode.alwaysRedo());
    }

    private LocalTransaction newNoRedoTransaction() {
        return doNewTransaction(DurabilityMode.NO_REDO);
    }

    private LocalTransaction newNoRedoTransaction(long redoTxnId) {
        return redoTxnId == 0 ? newNoRedoTransaction() :
                new LocalTransaction(this, redoTxnId, LockMode.UPGRADABLE_READ,
                        mDefaultLockTimeoutNanos);
    }

    LocalTransaction threadLocalTransaction(DurabilityMode durabilityMode) {
        SoftReference<LocalTransaction> txnRef = mLocalTransaction.get();
        LocalTransaction txn;
        if (txnRef == null || (txn = txnRef.get()) == null) {
            txn = doNewTransaction(durabilityMode);
            mLocalTransaction.set(new SoftReference<>(txn));
        } else {
            txn.setRedo(txnRedoWriter());
            txn.setDurabilityMode(durabilityMode);
            txn.setLockMode(LockMode.UPGRADABLE_READ);
            txn.setLockTimeoutNanos(mDefaultLockTimeoutNanos);
        }
        return txn;
    }

    void removeThreadLocalTransaction() {
        mLocalTransaction.remove();
    }

    RedoWriter txnRedoWriter() {
        RedoWriter redo = mRedoWriter;
        if (redo != null) {
            redo = redo.txnRedoWriter();
        }
        return redo;
    }

    private void resetTransactionContexts(long txnId) {
        for (TransactionContext txnContext : mTxnContexts) {
            txnContext.resetTransactionId(txnId++);
        }
    }

    public TransactionContext anyTransactionContext() {
        return selectTransactionContext(ThreadLocalRandom.current().nextInt());
    }

    public TransactionContext selectTransactionContext(LocalTransaction txn) {
        return selectTransactionContext(txn.hashCode());
    }

    private TransactionContext selectTransactionContext(int num) {
        return mTxnContexts[(num & 0x7fffffff) % mTxnContexts.length];
    }

    @Override
    public long preallocate(long bytes) throws IOException {
        if (!isClosed() && mPageDb.isDurable()) {
            int pageSize = mPageSize;
            long pageCount = (bytes + pageSize - 1) / pageSize;
            if (pageCount > 0) {
                pageCount = mPageDb.allocatePages(pageCount);
                if (pageCount > 0) {
                    try {
                        checkpoint(true, 0, 0);
                    } catch (Throwable e) {
                        DatabaseException.rethrowIfRecoverable(e);
                        Utils.closeQuietly(this, e);
                        throw e;
                    }
                }
                return pageCount * pageSize;
            }
        }
        return 0;
    }

    @Override
    public Sorter newSorter(Executor executor) throws IOException {
        if (executor == null && (executor = mSorterExecutor) == null) {
            mOpenTreesLatch.acquireExclusive();
            try {
                checkClosed();
                executor = mSorterExecutor;
                if (executor == null) {
                    ExecutorService es = Executors.newCachedThreadPool(r -> {
                        Thread t = new Thread(r);
                        t.setDaemon(true);
                        t.setName("Sorter-" + Long.toUnsignedString(t.getId()));
                        return t;
                    });
                    mSorterExecutor = es;
                    executor = es;
                }
            } finally {
                mOpenTreesLatch.releaseExclusive();
            }
        }

        return new ParallelSorter(this, executor);
    }

    @Override
    public void capacityLimit(long bytes) {
        mPageDb.pageLimit(bytes < 0 ? -1 : (bytes / mPageSize));
    }

    @Override
    public long capacityLimit() {
        long pageLimit = mPageDb.pageLimit();
        return pageLimit < 0 ? -1 : (pageLimit * mPageSize);
    }

    @Override
    public void capacityLimitOverride(long bytes) {
        mPageDb.pageLimitOverride(bytes < 0 ? -1 : (bytes / mPageSize));
    }

    @Override
    public Snapshot beginSnapshot() throws IOException {
        if (!(mPageDb.isDurable())) {
            throw new UnsupportedOperationException("Snapshot only allowed for durable databases");
        }
        checkClosed();
        DurablePageDb pageDb = (DurablePageDb) mPageDb;
        return pageDb.beginSnapshot(this);
    }

    public static Database restoreFromSnapshot(DatabaseConfig config, InputStream in) throws IOException {
        if (config.isReadOnly()) {
            throw new IllegalArgumentException("Cannot restore into a read-only database");
        }

        config = config.clone();
        PageDb restored;

        File[] dataFiles = config.dataFiles();
        if (dataFiles == null) {
            PageArray dataPageArray = config.getDataPageArray();

            if (dataPageArray == null) {
                throw new UnsupportedOperationException
                        ("Restore only allowed for durable databases");
            }

            dataPageArray = dataPageArray.open();
            dataPageArray.setPageCount(0);

            Utils.deleteNumberedFiles(config.getBaseFile(), REDO_FILE_SUFFIX);

            restored = DurablePageDb.restoreFromSnapshot(dataPageArray, null, config.getCrypto(), in);
        } else {
            for (File f : dataFiles) {
                f.delete();
                if (config.isMkdirs()) {
                    f.getParentFile().mkdirs();
                }
            }

            FileFactory factory = config.getFileFactory();
            EnumSet<OpenOption> options = config.createOpenOptions();

            Utils.deleteNumberedFiles(config.getBaseFile(), REDO_FILE_SUFFIX);

            int pageSize = config.getPageSize();
            if (pageSize <= 0) {
                pageSize = DEFAULT_PAGE_SIZE;
            }

            restored = DurablePageDb.restoreFromSnapshot
                    (pageSize, dataFiles, factory, options, null, config.getCrypto(), in);
        }

        try {
            restored.close();
        } finally {
            restored.delete();
        }

        return Database.open(config);
    }

    @Override
    public void createCachePrimer(OutputStream out) throws IOException {
        if (!(mPageDb.isDurable())) {
            throw new UnsupportedOperationException
                    ("Cache priming only allowed for durable databases");
        }

        out = ((DurablePageDb) mPageDb).encrypt(out);

        DataOutputStream dout = new DataOutputStream(out);

        dout.writeLong(PRIMER_MAGIC_NUMBER);

        for (TreeRef treeRef : mOpenTrees.values()) {
            Tree tree = treeRef.get();
            if (tree != null && !Tree.isInternal(tree.mId)) {
                byte[] name = tree.mName;
                dout.writeInt(name.length);
                dout.write(name);
                tree.writeCachePrimer(dout);
            }
        }

        dout.writeInt(-1);
    }

    @Override
    public void applyCachePrimer(InputStream in) throws IOException {
        if (!(mPageDb.isDurable())) {
            throw new UnsupportedOperationException
                    ("Cache priming only allowed for durable databases");
        }

        in = ((DurablePageDb) mPageDb).decrypt(in);

        DataInput din;
        if (in instanceof DataInput) {
            din = (DataInput) in;
        } else {
            din = new DataInputStream(in);
        }

        long magic = din.readLong();
        if (magic != PRIMER_MAGIC_NUMBER) {
            throw new DatabaseException("Wrong cache primer magic number: " + magic);
        }

        while (true) {
            int len = din.readInt();
            if (len < 0) {
                break;
            }
            byte[] name = new byte[len];
            din.readFully(name);
            Tree tree = openTree(name, false);
            if (tree != null) {
                tree.applyCachePrimer(din);
            } else {
                Tree.skipCachePrimer(din);
            }
        }
    }

    @Override
    public Stats stats() {
        Stats stats = new Stats();

        stats.pageSize = mPageSize;

        CommitLock.Shared shared = mCommitLock.acquireShared();
        try {
            long cursorCount = 0;
            int openTreesCount = 0;

            for (TreeRef treeRef : mOpenTrees.values()) {
                Tree tree = treeRef.get();
                if (tree != null) {
                    openTreesCount++;
                    cursorCount += tree.mRoot.countCursors();
                }
            }

            cursorCount += mRegistry.mRoot.countCursors();
            cursorCount += mRegistryKeyMap.mRoot.countCursors();

            FragmentedTrash trash = mFragmentedTrash;
            if (trash != null) {
                cursorCount += trash.mTrash.mRoot.countCursors();
            }

            Tree cursorRegistry = mCursorRegistry;
            if (cursorRegistry != null) {
                cursorCount += cursorRegistry.mRoot.countCursors();
            }

            stats.openIndexes = openTreesCount;
            stats.cursorCount = cursorCount;

            PageDb.Stats pstats = mPageDb.stats();
            stats.freePages = pstats.freePages;
            stats.totalPages = pstats.totalPages;

            stats.lockCount = mLockManager.numLocksHeld();

            for (TransactionContext txnContext : mTxnContexts) {
                txnContext.addStats(stats);
            }
        } finally {
            shared.release();
        }

        for (NodeContext context : mNodeContexts) {
            stats.cachedPages += context.nodeCount();
            stats.dirtyPages += context.dirtyCount();
        }

        if (stats.dirtyPages > stats.totalPages) {
            stats.dirtyPages = stats.totalPages;
        }

        return stats;
    }

    static class RedoClose extends ShutdownHook.Weak<LocalDatabase> {
        RedoClose(LocalDatabase db) {
            super(db);
        }

        @Override
        public void doShutdown(LocalDatabase db) {
            db.redoClose(RedoOps.OP_SHUTDOWN, null);
        }
    }

    /**
     * @param op OP_CLOSE or OP_SHUTDOWN
     */
    private void redoClose(byte op, Throwable cause) {
        RedoWriter redo = mRedoWriter;
        if (redo == null) {
            return;
        }

        redo.closeCause(cause);
        redo = redo.txnRedoWriter();
        redo.closeCause(cause);

        try {
            // NO_FLUSH now behaves like NO_SYNC.
            redo.alwaysFlush(true);
        } catch (IOException e) {
            // Ignore.
        }

        try {
            TransactionContext context = anyTransactionContext();
            context.redoTimestamp(redo, op);
            context.flush();

            redo.force(true);
        } catch (IOException e) {
            // Ignore.
        }

        if (op == RedoOps.OP_CLOSE) {
            Utils.closeQuietly(redo);
        }
    }

    @Override
    public void flush() throws IOException {
        flush(0); // flush only
    }

    @Override
    public void sync() throws IOException {
        flush(1); // flush and sync
    }

    /**
     * @param level 0: flush only, 1: flush and sync, 2: flush and sync metadata
     */
    private void flush(int level) throws IOException {
        if (!isClosed() && mRedoWriter != null) {
            mRedoWriter.flush();
            if (level > 0) {
                mRedoWriter.force(level > 1);
            }
        }
    }

    @Override
    public void checkpoint() throws IOException {
        while (!isClosed() && mPageDb.isDurable()) {
            try {
                checkpoint(false, 0, 0);
                return;
            } catch (UnmodifiableReplicaException e) {
                // Retry.
                Thread.yield();
            } catch (Throwable e) {
                DatabaseException.rethrowIfRecoverable(e);
                Utils.closeQuietly(this, e);
                throw e;
            }
        }
    }

    @Override
    public void suspendCheckpoints() {
        Checkpointer c = mCheckpointer;
        if (c != null) {
            c.suspend();
        }
    }

    @Override
    public void resumeCheckpoints() {
        Checkpointer c = mCheckpointer;
        if (c != null) {
            c.resume();
        }
    }

    @Override
    public boolean compactFile(CompactionObserver observer, double target) throws IOException {
        if (target < 0 || target > 1) {
            throw new IllegalArgumentException("Illegal compaction target: " + target);
        }

        if (target == 0) {
            return true;
        }

        long targetPageCount;
        mCheckpointLock.lock();
        try {
            PageDb.Stats stats = mPageDb.stats();
            long usedPages = stats.totalPages - stats.freePages;
            targetPageCount = Math.max(usedPages, (long) (usedPages / target));

            long reserve;
            {
                long freed = stats.totalPages - targetPageCount;

                freed *= Utils.calcUnsignedVarLongLength(stats.totalPages << 1);

                reserve = freed / (mPageSize - (8 + 8));

                reserve += 6;
            }

            targetPageCount += reserve;

            if (targetPageCount >= stats.totalPages && targetPageCount >= mPageDb.pageCount()) {
                return true;
            }

            if (!mPageDb.compactionStart(targetPageCount)) {
                return false;
            }
        } finally {
            mCheckpointLock.unlock();
        }

        boolean completed = mPageDb.compactionScanFreeList();

        if (completed) {
            checkpoint();

            if (observer == null) {
                observer = new CompactionObserver();
            }

            final long highestNodeId = targetPageCount - 1;
            final CompactionObserver fobserver = observer;

            completed = scanAllIndexes(tree -> {
                return tree.compactTree(tree.observableView(), highestNodeId, fobserver);
            });

            checkpoint(true, 0, 0);

            if (completed && mPageDb.compactionScanFreeList()) {
                if (!mPageDb.compactionVerify() && mPageDb.compactionScanFreeList()) {
                    checkpoint(true, 0, 0);
                }
            }
        }

        mCheckpointLock.lock();
        try {
            completed &= mPageDb.compactionEnd();
            checkpoint(true, 0, 0);
            mPageDb.compactionReclaim();
            checkpoint(true, 0, 0);

            if (completed) {
                return mPageDb.truncatePages();
            }
        } finally {
            mCheckpointLock.unlock();
        }

        return false;
    }

    @Override
    public boolean verify(VerificationObserver observer) throws IOException {
        if (false) {
            mPageDb.scanFreeList(id -> System.out.println(id));
        }

        if (observer == null) {
            observer = new VerificationObserver();
        }

        final boolean[] passedRef = {true};
        final VerificationObserver fobserver = observer;

        scanAllIndexes(tree -> {
            Index view = tree.observableView();
            fobserver.setFailed(false);
            boolean keepGoing = tree.verifyTree(view, fobserver);
            passedRef[0] &= !fobserver.isFailed();
            if (keepGoing) {
                keepGoing = fobserver.indexComplete(view, !fobserver.isFailed(), null);
            }
            return keepGoing;
        });

        return passedRef[0];
    }

    @FunctionalInterface
    interface ScanVisitor {
        boolean apply(Tree tree) throws IOException;
    }

    private boolean scanAllIndexes(ScanVisitor visitor) throws IOException {
        if (!visitor.apply(mRegistry)) {
            return false;
        }
        if (!visitor.apply(mRegistryKeyMap)) {
            return false;
        }

        FragmentedTrash trash = mFragmentedTrash;
        if (trash != null) {
            if (!visitor.apply(trash.mTrash)) {
                return false;
            }
        }

        Tree cursorRegistry = mCursorRegistry;
        if (cursorRegistry != null) {
            if (!visitor.apply(cursorRegistry)) {
                return false;
            }
        }

        Cursor all = indexRegistryByName().newCursor(null);
        try {
            for (all.first(); all.key() != null; all.next()) {
                long id = Utils.decodeLongBE(all.value(), 0);

                Index index = indexById(id);
                if (index instanceof Tree && !visitor.apply((Tree) index)) {
                    return false;
                }
            }
        } finally {
            all.reset();
        }

        return true;
    }

    @Override
    public void close(Throwable cause) throws IOException {
        close(cause, false);
    }

    @Override
    public void shutdown() throws IOException {
        close(null, mPageDb.isDurable());
    }

    private void close(Throwable cause, boolean shutdown) throws IOException {
        if (!cClosedUpdater.compareAndSet(this, 0, 1)) {
            return;
        }

        if (cause != null) {
            mClosedCause = cause;
            Throwable rootCause = Utils.rootCause(cause);
            if (mEventListener == null) {
                Utils.uncaught(rootCause);
            } else {
                mEventListener.notify(EventType.PANIC_UNHANDLED_EXCEPTION,
                        "Closing database due to unhandled exception: %1$s",
                        rootCause);
            }
        }

        boolean lockedCheckpointer = false;
        final Checkpointer c = mCheckpointer;

        try {
            if (shutdown) {
                mCheckpointLock.lock();
                lockedCheckpointer = true;
                checkpoint(true, 0, 0);
                if (c != null) {
                    c.close(cause);
                }
            } else {
                if (c != null) {
                    c.close(cause);
                }

                if (mCheckpointLock.tryLock()) {
                    lockedCheckpointer = true;
                } else if (cause == null && !(mRedoWriter instanceof ReplRedoController)) {
                    mCheckpointLock.lock();
                    lockedCheckpointer = true;
                }
            }
        } finally {
            Thread ct = c == null ? null : c.interrupt();

            if (lockedCheckpointer) {
                mCheckpointLock.unlock();

                if (ct != null) {
                    try {
                        ct.join();
                    } catch (InterruptedException e) {
                        // Ignore.
                    }
                }
            }
        }

        try {
            CommitLock lock = mCommitLock;

            if (mOpenTrees != null) {
                final ArrayList<TreeRef> trees;
                if (lock != null) {
                    lock.acquireExclusive();
                }
                try {
                    mOpenTreesLatch.acquireExclusive();
                    try {
                        trees = new ArrayList<>(mOpenTreesById.size());

                        mOpenTreesById.traverse(entry -> {
                            trees.add(entry.value);
                            return true;
                        });

                        mOpenTrees.clear();
                    } finally {
                        mOpenTreesLatch.releaseExclusive();
                    }
                } finally {
                    if (lock != null) {
                        lock.releaseExclusive();
                    }
                }

                for (TreeRef ref : trees) {
                    Tree tree = ref.get();
                    if (tree != null) {
                        tree.forceClose();
                    }
                }

                FragmentedTrash trash = mFragmentedTrash;
                if (trash != null) {
                    mFragmentedTrash = null;
                    trash.mTrash.forceClose();
                }

                if (mCursorRegistry != null) {
                    mCursorRegistry.forceClose();
                }

                if (mRegistryKeyMap != null) {
                    mRegistryKeyMap.forceClose();
                }

                if (mRegistry != null) {
                    mRegistry.forceClose();
                }
            }

            if (lock != null) {
                lock.acquireExclusive();
            }
            try {
                if (mSorterExecutor != null) {
                    mSorterExecutor.shutdown();
                    mSorterExecutor = null;
                }

                if (mNodeContexts != null) {
                    for (NodeContext context : mNodeContexts) {
                        if (context != null) {
                            context.delete();
                        }
                    }
                }

                if (mTxnContexts != null) {
                    for (TransactionContext txnContext : mTxnContexts) {
                        if (txnContext != null) {
                            txnContext.deleteUndoLogs();
                        }
                    }
                }

                nodeMapDeleteAll();

                redoClose(RedoOps.OP_CLOSE, cause);

                IOException ex = null;
                ex = Utils.closeQuietly(ex, mPageDb, cause);
                ex = Utils.closeQuietly(ex, mTempFileManager, cause);

                if (shutdown && mBaseFile != null && !mReadOnly) {
                    deleteRedoLogFiles();
                    new File(mBaseFile.getPath() + INFO_FILE_SUFFIX).delete();
                    ex = Utils.closeQuietly(ex, mLockFile, cause);
                    new File(mBaseFile.getPath() + LOCK_FILE_SUFFIX).delete();
                } else {
                    ex = Utils.closeQuietly(ex, mLockFile, cause);
                }

                if (mLockManager != null) {
                    mLockManager.close();
                }

                if (ex != null) {
                    throw ex;
                }
            } finally {
                if (lock != null) {
                    lock.releaseExclusive();
                }
            }
        } finally {
            if (mPageDb != null) {
                mPageDb.delete();
            }
            if (mSparePagePool != null) {
                mSparePagePool.delete();
            }
            deleteCommitHeader();
            DirectPageOps.p_arenaDelete(mArena);
        }
    }

    private void deleteCommitHeader() {
        DirectPageOps.p_delete(cCommitHeaderUpdater.getAndSet(this, DirectPageOps.p_null()));
    }

    @Override
    public boolean isClosed() {
        return mClosed != 0;
    }

    public void checkClosed() throws DatabaseException {
        if (isClosed()) {
            String message = "Closed";
            Throwable cause = mClosedCause;
            if (cause != null) {
                message += "; " + Utils.rootCause(cause);
            }
            throw new DatabaseException(message, cause);
        }
    }

    public Throwable closedCause() {
        return mClosedCause;
    }

    void treeClosed(Tree tree) {
        mOpenTreesLatch.acquireExclusive();
        try {
            TreeRef ref = mOpenTreesById.getValue(tree.mId);
            if (ref != null && ref.get() == tree) {
                ref.clear();
                if (tree.mName != null) {
                    mOpenTrees.remove(tree.mName);
                }
                mOpenTreesById.remove(tree.mId);
            }
        } finally {
            mOpenTreesLatch.releaseExclusive();
        }
    }

    private boolean moveToTrash(long treeId, byte[] treeIdBytes) throws IOException {
        final byte[] idKey = newKey(KEY_TYPE_INDEX_ID, treeIdBytes);
        final byte[] trashIdKey = newKey(KEY_TYPE_TRASH_ID, treeIdBytes);

        final LocalTransaction txn = newAlwaysRedoTransaction();

        try {
            txn.lockTimeout(-1, null);

            if (mRegistryKeyMap.load(txn, trashIdKey) != null) {
                return false;
            }

            byte[] treeName = mRegistryKeyMap.exchange(txn, idKey, null);

            if (treeName == null) {
                mRegistryKeyMap.store(txn, trashIdKey, new byte[1]);
            } else {
                byte[] nameKey = newKey(KEY_TYPE_INDEX_NAME, treeName);
                mRegistryKeyMap.remove(txn, nameKey, treeIdBytes);
                nameKey[0] = 1;
                mRegistryKeyMap.store(txn, trashIdKey, nameKey);
            }

            if (txn.getRedo() != null) {
                txn.durabilityMode(mDurabilityMode.alwaysRedo());

                long commitPos;
                CommitLock.Shared shared = mCommitLock.acquireShared();
                try {
                    txn.check();
                    commitPos = txn.getContext().redoDeleteIndexCommitFinal
                            (txn.getRedo(), txn.txnId(), treeId, txn.durabilityMode());
                } finally {
                    shared.release();
                }

                if (commitPos != 0) {
                    txn.getRedo().txnCommitSync(txn, commitPos);
                }
            }

            txn.commit();
        } catch (Throwable e) {
            DatabaseException.rethrowIfRecoverable(e);
            throw Utils.closeOnFailure(this, e);
        } finally {
            txn.reset();
        }

        return true;
    }

    void removeFromTrash(Tree tree, Node root) throws IOException {
        byte[] trashIdKey = newKey(KEY_TYPE_TRASH_ID, tree.mIdBytes);

        CommitLock.Shared shared = mCommitLock.acquireShared();
        try {
            if (root != null) {
                root.acquireExclusive();
                if (root.mPage == DirectPageOps.p_closedTreePage()) {
                    // Database has been closed.
                    root.releaseExclusive();
                    return;
                }
                deleteNode(root);
            }
            mRegistryKeyMap.delete(Transaction.BOGUS, trashIdKey);
            mRegistry.delete(Transaction.BOGUS, tree.mIdBytes);
        } catch (Throwable e) {
            throw Utils.closeOnFailure(this, e);
        } finally {
            shared.release();
        }
    }

    void removeGraftedTempTree(Tree tree) throws IOException {
        try {
            mOpenTreesLatch.acquireExclusive();
            try {
                TreeRef ref = mOpenTreesById.removeValue(tree.mId);
                if (ref != null && ref.get() == tree) {
                    ref.clear();
                }
            } finally {
                mOpenTreesLatch.releaseExclusive();
            }
            byte[] trashIdKey = newKey(KEY_TYPE_TRASH_ID, tree.mIdBytes);
            mRegistryKeyMap.delete(Transaction.BOGUS, trashIdKey);
            mRegistry.delete(Transaction.BOGUS, tree.mIdBytes);
        } catch (Throwable e) {
            throw Utils.closeOnFailure(this, e);
        }
    }

    Tree openCursorRegistry() throws IOException {
        Tree cursorRegistry = mCursorRegistry;
        if (cursorRegistry == null) {
            mOpenTreesLatch.acquireExclusive();
            try {
                if ((cursorRegistry = mCursorRegistry) == null) {
                    mCursorRegistry = cursorRegistry =
                            openInternalTree(Tree.CURSOR_REGISTRY_ID, true);
                }
            } finally {
                mOpenTreesLatch.releaseExclusive();
            }
        }

        return cursorRegistry;
    }

    void registerCursor(Tree cursorRegistry, TreeCursor cursor) throws IOException {
        try {
            byte[] cursorIdBytes = new byte[8];
            Utils.encodeLongBE(cursorIdBytes, 0, cursor.mCursorId);
            cursorRegistry.store(Transaction.BOGUS, cursorIdBytes, cursor.mTree.mIdBytes);
        } catch (Throwable e) {
            try {
                cursor.unregister();
            } catch (Throwable e2) {
                Utils.suppress(e, e2);
            }
            throw e;
        }
    }

    public void unregisterCursor(TreeCursor cursor) {
        try {
            byte[] cursorIdBytes = new byte[8];
            Utils.encodeLongBE(cursorIdBytes, 0, cursor.mCursorId);
            openCursorRegistry().store(Transaction.BOGUS, cursorIdBytes, null);
            cursor.mCursorId = 0;
        } catch (Throwable e) {
            // Database is borked, cleanup later.
        }
    }

    private Node loadTreeRoot(final long treeId, final long rootId) throws IOException {
        if (rootId == 0) {
            Node rootNode = allocLatchedNode(treeId, NodeContext.MODE_UNEVICTABLE);

            try {
                // rootNode.asEmptyRoot();
                if (mFullyMapped) {
                    rootNode.mPage = DirectPageOps.p_nonTreePage();
                    rootNode.mId = 0;
                    rootNode.mCachedState = Node.CACHED_CLEAN;
                } else {
                    rootNode.asEmptyRoot();
                }
                return rootNode;
            } finally {
                rootNode.releaseExclusive();
            }
        } else {
            Node rootNode = nodeMapGetAndRemove(rootId);

            if (rootNode != null) {
                try {
                    rootNode.makeUnevictable();
                    return rootNode;
                } finally {
                    rootNode.releaseExclusive();
                }
            }

            rootNode = allocLatchedNode(rootId, NodeContext.MODE_UNEVICTABLE);

            try {
                try {
                    rootNode.read(this, rootId);
                } finally {
                    rootNode.releaseExclusive();
                }
                return rootNode;
            } catch (Throwable e) {
                rootNode.makeEvictableNow();
                throw e;
            }
        }
    }

    private Node loadRegistryRoot(DatabaseConfig config, byte[] header) throws IOException {
        int version = Utils.decodeIntLE(header, I_ENCODING_VERSION);

        if (config.getDebugOpen() != null) {
            mEventListener.notify(EventType.DEBUG, "ENCODING_VERSION: %1$d", version);
        }

        long rootId;
        if (version == 0) {
            rootId = 0;
            mInitialReadState = Node.CACHED_DIRTY_0;
        } else {
            if (version != ENCODING_VERSION) {
                throw new CorruptDatabaseException("Unknown encoding version: " + version);
            }

            long replEncoding = Utils.decodeLongLE(header, I_REPL_ENCODING);

            if (config.getDebugOpen() != null) {
                mEventListener.notify(EventType.DEBUG, "REPL_ENCODING: %1$d", replEncoding);
            }

            ReplicationManager rm = config.getReplManager();

            if (rm == null) {
                if (replEncoding != 0) {
                    throw new DatabaseException
                            ("Database must be configured with a replication manager, " +
                                    "identified by: " + replEncoding);
                }
            } else {
                if (replEncoding == 0) {
                    throw new DatabaseException
                            ("Database was created initially without a replication manager");
                }
                long expectedReplEncoding = rm.encoding();
                if (replEncoding != expectedReplEncoding) {
                    throw new DatabaseException
                            ("Database was created initially with a different replication manager, " +
                                    "identified by: " + replEncoding);
                }
            }

            rootId = Utils.decodeLongLE(header, I_ROOT_PAGE_ID);

            if (config.getDebugOpen() != null) {
                mEventListener.notify(EventType.DEBUG, "ROOT_PAGE_ID: %1$d", rootId);
            }
        }

        return loadTreeRoot(0, rootId);
    }

    private Tree openInternalTree(long treeId, boolean create) throws IOException {
        return openInternalTree(treeId, create, null);
    }

    private Tree openInternalTree(long treeId, boolean create, DatabaseConfig config)
            throws IOException {
        CommitLock.Shared shared = mCommitLock.acquireShared();
        try {
            checkClosed();

            byte[] treeIdBytes = new byte[8];
            Utils.encodeLongBE(treeIdBytes, 0, treeId);
            byte[] rootIdBytes = mRegistry.load(Transaction.BOGUS, treeIdBytes);
            long rootId;
            if (rootIdBytes != null) {
                rootId = Utils.decodeLongLE(rootIdBytes, 0);
            } else {
                if (!create) {
                    return null;
                }
                rootId = 0;
            }

            Node root = loadTreeRoot(treeId, rootId);

            if (config != null && config.getReplManager() != null) {
                return new TxnTree(this, treeId, treeIdBytes, root);
            }

            return newTreeInstance(treeId, treeIdBytes, null, root);
        } finally {
            shared.release();
        }
    }

    private Tree openTree(byte[] name, boolean create) throws IOException {
        return openTree(null, null, name, create);
    }

    private Tree openTree(Transaction findTxn, byte[] treeIdBytes, byte[] name, boolean create)
            throws IOException {
        Tree tree = quickFindIndex(name);
        if (tree == null) {
            CommitLock.Shared shared = mCommitLock.acquireShared();
            try {
                tree = doOpenTree(findTxn, treeIdBytes, name, create);
            } finally {
                shared.release();
            }
        }
        return tree;
    }

    private Tree doOpenTree(Transaction findTxn, byte[] treeIdBytes, byte[] name, boolean create)
            throws IOException {
        checkClosed();

        cleanupUnreferencedTrees();

        byte[] nameKey = newKey(KEY_TYPE_INDEX_NAME, name);

        if (treeIdBytes == null) {
            treeIdBytes = mRegistryKeyMap.load(findTxn, nameKey);
        }

        long treeId;
        // Is non-null if tree was created.
        byte[] idKey;

        if (treeIdBytes != null) {
            // Tree already exists.
            idKey = null;
            treeId = Utils.decodeLongBE(treeIdBytes, 0);
        } else if (!create) {
            return null;
        } else create:{
            // Transactional find supported only for opens that do not create.
            if (findTxn != null) {
                throw new AssertionError();
            }

            Transaction createTxn = null;

            mOpenTreesLatch.acquireExclusive();
            try {
                treeIdBytes = mRegistryKeyMap.load(null, nameKey);
                if (treeIdBytes != null) {
                    // Another thread created it.
                    idKey = null;
                    treeId = Utils.decodeLongBE(treeIdBytes, 0);
                    break create;
                }

                treeIdBytes = new byte[8];

                // Non-transactional operations are critical, in that any failure is treated as
                // non-recoverable.
                boolean critical = true;
                try {
                    do {
                        critical = false;
                        treeId = nextTreeId(false);
                        Utils.encodeLongBE(treeIdBytes, 0, treeId);
                        critical = true;
                    } while (!mRegistry.insert(Transaction.BOGUS, treeIdBytes, Utils.EMPTY_BYTES));

                    critical = false;

                    try {
                        idKey = newKey(KEY_TYPE_INDEX_ID, treeIdBytes);

                        if (mRedoWriter instanceof ReplRedoController) {
                            // Confirmation is required when replicated.
                            createTxn = newTransaction(DurabilityMode.SYNC);
                        } else {
                            createTxn = newAlwaysRedoTransaction();
                        }

                        createTxn.lockTimeout(-1, null);

                        // Insert order is important for the indexById method to work reliably.
                        if (!mRegistryKeyMap.insert(createTxn, idKey, name)) {
                            throw new DatabaseException("Unable to insert index id");
                        }
                        if (!mRegistryKeyMap.insert(createTxn, nameKey, treeIdBytes)) {
                            throw new DatabaseException("Unable to insert index name");
                        }
                    } catch (Throwable e) {
                        critical = true;
                        try {
                            if (createTxn != null) {
                                createTxn.reset();
                            }
                            mRegistry.delete(Transaction.BOGUS, treeIdBytes);
                            critical = false;
                        } catch (Throwable e2) {
                            Utils.suppress(e, e2);
                        }
                        throw e;
                    }
                } catch (Throwable e) {
                    if (!critical) {
                        DatabaseException.rethrowIfRecoverable(e);
                    }
                    throw Utils.closeOnFailure(this, e);
                }
            } finally {
                mOpenTreesLatch.releaseExclusive();
            }

            if (createTxn != null) {
                try {
                    createTxn.commit();
                } catch (Throwable e) {
                    try {
                        createTxn.reset();
                        mRegistry.delete(Transaction.BOGUS, treeIdBytes);
                    } catch (Throwable e2) {
                        Utils.suppress(e, e2);
                        throw Utils.closeOnFailure(this, e);
                    }
                    DatabaseException.rethrowIfRecoverable(e);
                    throw Utils.closeOnFailure(this, e);
                }
            }
        }

        Transaction txn = threadLocalTransaction(DurabilityMode.NO_REDO);
        try {
            txn.lockTimeout(-1, null);

            if (txn.lockCheck(mRegistry.getId(), treeIdBytes) != LockResult.UNOWNED) {
                throw new LockFailureException("Index open listener self deadlock");
            }

            byte[] rootIdBytes = mRegistry.load(txn, treeIdBytes);

            Tree tree = quickFindIndex(name);
            if (tree != null) {
                return tree;
            }

            long rootId = (rootIdBytes == null || rootIdBytes.length == 0) ? 0
                    : Utils.decodeLongLE(rootIdBytes, 0);

            Node root = loadTreeRoot(treeId, rootId);

            tree = newTreeInstance(treeId, treeIdBytes, name, root);

            try {
                if (mIndexOpenListener != null) {
                    mIndexOpenListener.accept(this, tree);
                }

                TreeRef treeRef = new TreeRef(tree, mOpenTreesRefQueue);

                mOpenTreesLatch.acquireExclusive();
                try {
                    mOpenTrees.put(name, treeRef);
                    try {
                        mOpenTreesById.insert(treeId).value = treeRef;
                    } catch (Throwable e) {
                        mOpenTrees.remove(name);
                        throw e;
                    }
                } finally {
                    mOpenTreesLatch.releaseExclusive();
                }
            } catch (Throwable e) {
                tree.close();
                throw e;
            }
            return tree;
        } catch (Throwable e) {
            if (idKey != null) {
                try {
                    mRegistryKeyMap.delete(null, idKey);
                    mRegistryKeyMap.delete(null, nameKey);
                    mRegistry.delete(Transaction.BOGUS, treeIdBytes);
                } catch (Throwable e2) {
                    // Ignore.
                }
            }
            throw e;
        } finally {
            txn.reset();
        }
    }

    private Tree newTreeInstance(long id, byte[] idBytes, byte[] name, Node root) {
        Tree tree;
        if (mRedoWriter instanceof ReplRedoWriter) {
            tree = new TxnTree(this, id, idBytes, root);
        } else {
            tree = new Tree(this, id, idBytes, root);
        }
        tree.mName = name;
        return tree;
    }

    private long nextTreeId(boolean temporary) throws IOException {

        Transaction txn;
        if (temporary) {
            txn = newNoRedoTransaction();
        } else {
            txn = newAlwaysRedoTransaction();
        }

        try {
            txn.lockTimeout(-1, null);

            long treeIdMask;
            {
                byte[] key = {KEY_TYPETree_ID_MASK};
                byte[] treeIdMaskBytes = mRegistryKeyMap.load(txn, key);

                if (treeIdMaskBytes == null) {
                    treeIdMaskBytes = new byte[8];
                    ThreadLocalRandom.current().nextBytes(treeIdMaskBytes);
                    mRegistryKeyMap.store(txn, key, treeIdMaskBytes);
                }

                treeIdMask = Utils.decodeLongLE(treeIdMaskBytes, 0);
            }

            byte[] key = {KEY_TYPE_NEXTTree_ID};
            byte[] nextTreeIdBytes = mRegistryKeyMap.load(txn, key);

            if (nextTreeIdBytes == null) {
                nextTreeIdBytes = new byte[8];
            }
            long nextTreeId = Utils.decodeLongLE(nextTreeIdBytes, 0);

            if (temporary) {
                treeIdMask = ~treeIdMask;
            }

            long treeId;
            do {
                treeId = Utils.scramble((nextTreeId++) ^ treeIdMask);
            } while (Tree.isInternal(treeId));

            Utils.encodeLongLE(nextTreeIdBytes, 0, nextTreeId);
            mRegistryKeyMap.store(txn, key, nextTreeIdBytes);
            txn.commit();

            return treeId;
        } finally {
            txn.reset();
        }
    }

    private Tree quickFindIndex(byte[] name) throws IOException {
        TreeRef treeRef;
        mOpenTreesLatch.acquireShared();
        try {
            treeRef = mOpenTrees.get(name);
            if (treeRef == null) {
                return null;
            }
            Tree tree = treeRef.get();
            if (tree != null) {
                return tree;
            }
        } finally {
            mOpenTreesLatch.releaseShared();
        }

        cleanupUnreferencedTree(treeRef);
        return null;
    }

    private void cleanupUnreferencedTrees() throws IOException {
        final ReferenceQueue<Tree> queue = mOpenTreesRefQueue;
        if (queue == null) {
            return;
        }
        try {
            while (true) {
                Reference<? extends Tree> ref = queue.poll();
                if (ref == null) {
                    break;
                }
                if (ref instanceof TreeRef) {
                    cleanupUnreferencedTree((TreeRef) ref);
                }
            }
        } catch (Exception e) {
            if (!isClosed()) {
                throw e;
            }
        }
    }

    private void cleanupUnreferencedTree(TreeRef ref) throws IOException {
        Node root = ref.mRoot;
        root.acquireShared();
        try {
            mOpenTreesLatch.acquireExclusive();
            try {
                LHashTable.ObjEntry<TreeRef> entry = mOpenTreesById.get(ref.mId);
                if (entry == null || entry.value != ref) {
                    return;
                }
                if (ref.mName != null) {
                    mOpenTrees.remove(ref.mName);
                }
                mOpenTreesById.remove(ref.mId);
                root.makeEvictableNow();
                if (root.mId != 0) {
                    nodeMapPut(root);
                }
            } finally {
                mOpenTreesLatch.releaseExclusive();
            }
        } finally {
            root.releaseShared();
        }
    }

    private static byte[] newKey(byte type, byte[] payload) {
        byte[] key = new byte[1 + payload.length];
        key[0] = type;
        arraycopy(payload, 0, key, 1, payload.length);
        return key;
    }

    public int pageSize() {
        return mPageSize;
    }

    private int pageSize(long page) {
        return mPageSize;
    }

    public CommitLock commitLock() {
        return mCommitLock;
    }

    Node nodeMapGetShared(long nodeId) {
        int hash = Long.hashCode(nodeId);
        while (true) {
            Node node = nodeMapGet(nodeId, hash);
            if (node == null) {
                return null;
            }
            node.acquireShared();
            if (nodeId == node.mId) {
                return node;
            }
            node.releaseShared();
        }
    }

    Node nodeMapGetExclusive(long nodeId) {
        int hash = Long.hashCode(nodeId);
        while (true) {
            Node node = nodeMapGet(nodeId, hash);
            if (node == null) {
                return null;
            }
            node.acquireExclusive();
            if (nodeId == node.mId) {
                return node;
            }
            node.releaseExclusive();
        }
    }

    public Node nodeMapGet(final long nodeId) {
        return nodeMapGet(nodeId, Long.hashCode(nodeId));
    }

    Node nodeMapGet(final long nodeId, final int hash) {
        final Node[] table = mNodeMapTable;
        Node node = table[hash & (table.length - 1)];
        if (node != null) {
            int limit = 100;
            do {
                if (node.mId == nodeId) {
                    return node;
                }
            } while ((node = node.mNodeMapNext) != null && --limit != 0);
        }

        final Latch[] latches = mNodeMapLatches;
        final Latch latch = latches[hash & (latches.length - 1)];
        latch.acquireShared();

        node = table[hash & (table.length - 1)];
        while (node != null) {
            if (node.mId == nodeId) {
                latch.releaseShared();
                return node;
            }
            node = node.mNodeMapNext;
        }

        latch.releaseShared();
        return null;
    }

    public void nodeMapPut(final Node node) {
        nodeMapPut(node, Long.hashCode(node.mId));
    }

    void nodeMapPut(final Node node, final int hash) {
        final Latch[] latches = mNodeMapLatches;
        final Latch latch = latches[hash & (latches.length - 1)];
        latch.acquireExclusive();

        final Node[] table = mNodeMapTable;
        final int index = hash & (table.length - 1);
        Node e = table[index];
        while (e != null) {
            if (e == node) {
                latch.releaseExclusive();
                return;
            }
            if (e.mId == node.mId) {
                latch.releaseExclusive();
                throw new AssertionError("Already in NodeMap: " + node + ", " + e + ", " + hash);
            }
            e = e.mNodeMapNext;
        }

        node.mNodeMapNext = table[index];
        table[index] = node;

        latch.releaseExclusive();
    }

    Node nodeMapPutIfAbsent(final Node node) {
        final int hash = Long.hashCode(node.mId);
        final Latch[] latches = mNodeMapLatches;
        final Latch latch = latches[hash & (latches.length - 1)];
        latch.acquireExclusive();

        final Node[] table = mNodeMapTable;
        final int index = hash & (table.length - 1);
        Node e = table[index];
        while (e != null) {
            if (e.mId == node.mId) {
                latch.releaseExclusive();
                return e;
            }
            e = e.mNodeMapNext;
        }

        node.mNodeMapNext = table[index];
        table[index] = node;

        latch.releaseExclusive();
        return null;
    }

    void nodeMapReplace(final Node oldNode, final Node newNode) {
        final int hash = Long.hashCode(oldNode.mId);
        final Latch[] latches = mNodeMapLatches;
        final Latch latch = latches[hash & (latches.length - 1)];
        latch.acquireExclusive();

        newNode.mNodeMapNext = oldNode.mNodeMapNext;

        final Node[] table = mNodeMapTable;
        final int index = hash & (table.length - 1);
        Node e = table[index];
        if (e == oldNode) {
            table[index] = newNode;
        } else while (e != null) {
            Node next = e.mNodeMapNext;
            if (next == oldNode) {
                e.mNodeMapNext = newNode;
                break;
            }
            e = next;
        }

        oldNode.mNodeMapNext = null;

        latch.releaseExclusive();
    }

    boolean nodeMapRemove(final Node node) {
        return nodeMapRemove(node, Long.hashCode(node.mId));
    }

    boolean nodeMapRemove(final Node node, final int hash) {
        boolean found = false;

        final Latch[] latches = mNodeMapLatches;
        final Latch latch = latches[hash & (latches.length - 1)];
        latch.acquireExclusive();

        final Node[] table = mNodeMapTable;
        final int index = hash & (table.length - 1);
        Node e = table[index];
        if (e == node) {
            found = true;
            table[index] = e.mNodeMapNext;
        } else while (e != null) {
            Node next = e.mNodeMapNext;
            if (next == node) {
                found = true;
                e.mNodeMapNext = next.mNodeMapNext;
                break;
            }
            e = next;
        }

        node.mNodeMapNext = null;
        latch.releaseExclusive();
        return found;
    }

    Node nodeMapLoadFragment(long nodeId) throws IOException {
        Node node = nodeMapGetShared(nodeId);

        if (node != null) {
            node.used(ThreadLocalRandom.current());
            return node;
        }

        node = allocLatchedNode(nodeId);
        node.mId = nodeId;

        while (true) {
            Node existing = nodeMapPutIfAbsent(node);
            if (existing == null) {
                break;
            }

            existing.acquireShared();
            if (nodeId == existing.mId) {
                node.mId = 0;
                node.unused();
                return existing;
            }
            existing.releaseShared();
        }

        try {
            // node.type(TYPE_FRAGMENT);
            readNode(node, nodeId);
        } catch (Throwable t) {
            nodeMapRemove(node);
            node.mId = 0;
            node.releaseExclusive();
            throw t;
        }
        node.downgrade();

        return node;
    }

    Node nodeMapLoadFragmentExclusive(long nodeId, boolean read) throws IOException {
        Node node = nodeMapGetExclusive(nodeId);

        if (node != null) {
            node.used(ThreadLocalRandom.current());
            return node;
        }

        node = allocLatchedNode(nodeId);
        node.mId = nodeId;

        while (true) {
            Node existing = nodeMapPutIfAbsent(node);
            if (existing == null) {
                break;
            }
            existing.acquireExclusive();
            if (nodeId == existing.mId) {
                node.mId = 0;
                node.unused();
                return existing;
            }
            existing.releaseExclusive();
        }

        try {
            // node.type(TYPE_FRAGMENT);
            if (read) {
                readNode(node, nodeId);
            }
        } catch (Throwable t) {
            nodeMapRemove(node);
            node.mId = 0;
            node.releaseExclusive();
            throw t;
        }

        return node;
    }

    public Node nodeMapGetAndRemove(long nodeId) {
        Node node = nodeMapGetExclusive(nodeId);
        if (node != null) {
            nodeMapRemove(node);
        }
        return node;
    }

    void nodeMapDeleteAll() {
        start:
        while (true) {
            for (Latch latch : mNodeMapLatches) {
                latch.acquireExclusive();
            }

            try {
                for (int i = mNodeMapTable.length; --i >= 0; ) {
                    Node e = mNodeMapTable[i];
                    if (e != null) {
                        if (!e.tryAcquireExclusive()) {
                            // 防止死锁。
                            continue start;
                        }
                        try {
                            e.doDelete(this);
                        } finally {
                            e.releaseExclusive();
                        }
                        Node next;
                        while ((next = e.mNodeMapNext) != null) {
                            e.mNodeMapNext = null;
                            e = next;
                        }
                        mNodeMapTable[i] = null;
                    }
                }
            } finally {
                for (Latch latch : mNodeMapLatches) {
                    latch.releaseExclusive();
                }
            }

            return;
        }
    }

    final Node latchToChild(Node parent, int childPos) throws IOException {
        return latchChild(parent, childPos, Node.OPTION_PARENT_RELEASE_SHARED);
    }

    final Node latchChildRetainParent(Node parent, int childPos) throws IOException {
        return latchChild(parent, childPos, 0);
    }

    final Node latchChild(Node parent, int childPos, int option) throws IOException {
        long childId = parent.retrieveChildRefId(childPos);
        Node childNode = nodeMapGetShared(childId);

        tryFind:
        if (childNode != null) {
            checkChild:
            {
                evictChild:
                if (childNode.mCachedState != Node.CACHED_CLEAN
                        && parent.mCachedState == Node.CACHED_CLEAN
                        && parent.mId > 1) {

                    if (!childNode.tryUpgrade()) {
                        childNode.releaseShared();
                        childNode = nodeMapGetExclusive(childId);
                        if (childNode == null) {
                            break tryFind;
                        }
                        if (childNode.mCachedState == Node.CACHED_CLEAN) {
                            childNode.downgrade();
                            break evictChild;
                        }
                    }

                    if (option == Node.OPTION_PARENT_RELEASE_SHARED) {
                        parent.releaseShared();
                    }

                    try {
                        childNode.write(mPageDb);
                    } catch (Throwable e) {
                        childNode.releaseExclusive();
                        if (option == 0) {
                            parent.releaseShared();
                        }
                        throw e;
                    }

                    childNode.mCachedState = Node.CACHED_CLEAN;
                    childNode.downgrade();
                    break checkChild;
                }

                if (option == Node.OPTION_PARENT_RELEASE_SHARED) {
                    parent.releaseShared();
                }
            }

            childNode.used(ThreadLocalRandom.current());
            return childNode;
        }

        return parent.loadChild(this, childId, option);
    }

    final Node latchChildRetainParentEx(Node parent, int childPos, boolean required)
            throws IOException {
        long childId = parent.retrieveChildRefId(childPos);

        Node childNode;
        while (true) {
            childNode = nodeMapGet(childId);

            if (childNode != null) {
                if (required) {
                    childNode.acquireExclusive();
                } else if (!childNode.tryAcquireExclusive()) {
                    return null;
                }
                if (childId == childNode.mId) {
                    break;
                }
                childNode.releaseExclusive();
                continue;
            }

            return parent.loadChild(this, childId, Node.OPTION_CHILD_ACQUIRE_EXCLUSIVE);
        }

        if (childNode.mCachedState != Node.CACHED_CLEAN
                && parent.mCachedState == Node.CACHED_CLEAN
                && parent.mId > 1) {

            try {
                childNode.write(mPageDb);
            } catch (Throwable e) {
                childNode.releaseExclusive();
                parent.releaseExclusive();
                throw e;
            }
            childNode.mCachedState = Node.CACHED_CLEAN;
        }

        childNode.used(ThreadLocalRandom.current());
        return childNode;
    }

    Node allocLatchedNode(long anyNodeId) throws IOException {
        return allocLatchedNode(anyNodeId, 0);
    }

    public Node allocLatchedNode(long anyNodeId, int mode) throws IOException {
        mode |= mPageDb.allocMode();

        NodeContext[] contexts = mNodeContexts;
        int listIx = ((int) anyNodeId) & (contexts.length - 1);
        IOException fail = null;

        for (int trial = 1; trial <= 3; trial++) {
            for (int i = 0; i < contexts.length; i++) {
                try {
                    Node node = contexts[listIx].tryAllocLatchedNode(trial, mode);
                    if (node != null) {
                        return node;
                    }
                } catch (IOException e) {
                    if (fail == null) {
                        fail = e;
                    }
                }
                if (--listIx < 0) {
                    listIx = contexts.length - 1;
                }
            }

            checkClosed();

            CommitLock.Shared shared = mCommitLock.acquireShared();
            try {
                cleanupUnreferencedTrees();
            } finally {
                shared.release();
            }
        }

        if (fail == null && mPageDb.isDurable()) {
            throw new CacheExhaustedException();
        } else if (fail instanceof DatabaseFullException) {
            throw fail;
        } else {
            throw new DatabaseFullException(fail);
        }
    }

    Node allocDirtyNode() throws IOException {
        return allocDirtyNode(0);
    }

    public Node allocDirtyNode(int mode) throws IOException {
        Node node = mPageDb.allocLatchedNode(this, mode);

        if (mFullyMapped) {
            node.mPage = mPageDb.dirtyPage(node.mId);
        }
        node.mContext.addDirty(node, mCommitState);
        return node;
    }

    Node allocDirtyFragmentNode() throws IOException {
        Node node = allocDirtyNode();
        nodeMapPut(node);
        // node.type(TYPE_FRAGMENT);
        return node;
    }

    public boolean isMutable(Node node) {
        return node.mCachedState == mCommitState && node.mId > 1;
    }

    boolean shouldMarkDirty(Node node) {
        return node.mCachedState != mCommitState && node.mId >= 0;
    }

    boolean markDirty(Tree tree, Node node) throws IOException {
        if (node.mCachedState == mCommitState || node.mId < 0) {
            return false;
        } else {
            doMarkDirty(tree, node);
            return true;
        }
    }

    boolean markFragmentDirty(Node node) throws IOException {
        if (node.mCachedState == mCommitState) {
            return false;
        } else {
            if (node.mCachedState != Node.CACHED_CLEAN) {
                node.write(mPageDb);
            }

            long newId = mPageDb.allocPage();
            long oldId = node.mId;

            if (oldId != 0) {
                boolean removed = nodeMapRemove(node, Long.hashCode(oldId));

                try {
                    mPageDb.deletePage(oldId, false);
                } catch (Throwable e) {
                    if (removed) {
                        try {
                            nodeMapPut(node);
                        } catch (Throwable e2) {
                            Utils.suppress(e, e2);
                        }
                    }
                    try {
                        mPageDb.recyclePage(newId);
                    } catch (Throwable e2) {
                        // Panic.
                        Utils.suppress(e, e2);
                        close(e);
                    }
                    throw e;
                }
            }

            dirty(node, newId);
            nodeMapPut(node);
            return true;
        }
    }

    public void markUnmappedDirty(Node node) throws IOException {
        if (node.mCachedState != mCommitState) {
            node.write(mPageDb);

            long newId = mPageDb.allocPage();
            long oldId = node.mId;

            try {
                mPageDb.deletePage(oldId, false);
            } catch (Throwable e) {
                try {
                    mPageDb.recyclePage(newId);
                } catch (Throwable e2) {
                    // Panic.
                    Utils.suppress(e, e2);
                    close(e);
                }
                throw e;
            }

            dirty(node, newId);
        }
    }

    void doMarkDirty(Tree tree, Node node) throws IOException {
        if (node.mCachedState != Node.CACHED_CLEAN) {
            node.write(mPageDb);
        }

        long newId = mPageDb.allocPage();
        long oldId = node.mId;

        try {
            if (node == tree.mRoot) {
                storeTreeRootId(tree, newId);
            }
        } catch (Throwable e) {
            try {
                mPageDb.recyclePage(newId);
            } catch (Throwable e2) {
                // Panic.
                Utils.suppress(e, e2);
                close(e);
            }
            throw e;
        }

        if (oldId != 0) {
            boolean removed = nodeMapRemove(node, Long.hashCode(oldId));

            try {
                // TODO: 这会挂起I/O；如果deletePage会阻塞，请释放锁
                mPageDb.deletePage(oldId, false);
            } catch (Throwable e) {
                if (removed) {
                    try {
                        nodeMapPut(node);
                    } catch (Throwable e2) {
                        Utils.suppress(e, e2);
                    }
                }
                try {
                    if (node == tree.mRoot) {
                        storeTreeRootId(tree, oldId);
                    }
                    mPageDb.recyclePage(newId);
                } catch (Throwable e2) {
                    // Panic.
                    Utils.suppress(e, e2);
                    close(e);
                }
                throw e;
            }
        }

        dirty(node, newId);
        nodeMapPut(node);
    }

    private void storeTreeRootId(Tree tree, long id) throws IOException {
        if (tree.mIdBytes != null) {
            byte[] encodedId = new byte[8];
            Utils.encodeLongLE(encodedId, 0, id);
            mRegistry.store(Transaction.BOGUS, tree.mIdBytes, encodedId);
        }
    }

    private void dirty(Node node, long newId) throws IOException {
        if (mFullyMapped) {
            if (node.mPage == DirectPageOps.p_nonTreePage()) {
                node.mPage = mPageDb.dirtyPage(newId);
                node.asEmptyRoot();
            } else if (node.mPage != DirectPageOps.p_closedTreePage()) {
                node.mPage = mPageDb.copyPage(node.mId, newId); // copy on write
            }
        }

        node.mId = newId;
        node.mContext.addDirty(node, mCommitState);
    }

    void swapIfDirty(Node oldNode, Node newNode) {
        oldNode.mContext.swapIfDirty(oldNode, newNode);
    }

    public void redirty(Node node) throws IOException {
        /*P*/ // [|
        if (mFullyMapped) {
            mPageDb.dirtyPage(node.mId);
        }
        /*P*/ // ]
        node.mContext.addDirty(node, mCommitState);
    }

    void deleteNode(Node node) throws IOException {
        deleteNode(node, true);
    }

    public void deleteNode(Node node, boolean canRecycle) throws IOException {
        prepareToDelete(node);
        finishDeleteNode(node, canRecycle);
    }

    public void prepareToDelete(Node node) throws IOException {
        if (node.mCachedState == mCheckpointFlushState) {
            try {
                node.write(mPageDb);
            } catch (Throwable e) {
                node.releaseExclusive();
                throw e;
            }
        }
    }

    void finishDeleteNode(Node node) throws IOException {
        finishDeleteNode(node, true);
    }

    void finishDeleteNode(Node node, boolean canRecycle) throws IOException {
        try {
            long id = node.mId;

            if (id != 0) {
                boolean removed = nodeMapRemove(node, Long.hashCode(id));

                try {
                    if (canRecycle && node.mCachedState == mCommitState) {
                        mPageDb.recyclePage(id);
                    } else {
                        mPageDb.deletePage(id, true);
                    }
                } catch (Throwable e) {
                    if (removed) {
                        try {
                            nodeMapPut(node);
                        } catch (Throwable e2) {
                            Utils.suppress(e, e2);
                        }
                    }
                    throw e;
                }

                node.mId = -id;
            }

            node.mCachedState = Node.CACHED_CLEAN;
        } catch (Throwable e) {
            node.releaseExclusive();
            close(e);
            throw e;
        }
        node.unused();
    }

    final byte[] fragmentKey(byte[] key) throws IOException {
        return fragment(key, key.length, mMaxKeySize);
    }

    final byte[] fragment(final byte[] value, final long vlength, int max)
            throws IOException {
        return fragment(value, vlength, max, 65535);
    }

    final byte[] fragment(final byte[] value, final long vlength, int max, int maxInline)
            throws IOException {
        final int pageSize = mPageSize;
        long pageCount = vlength / pageSize;
        final int remainder = (int) (vlength % pageSize);

        if (vlength >= 65536) {
            max -= (1 + 4 + 6);
        } else if (pageCount == 0 && remainder <= (max - (1 + 2 + 2))) {
            byte[] newValue = new byte[(1 + 2 + 2) + (int) vlength];
            newValue[0] = 0x02; // ff=0, i=1, p=0
            Utils.encodeShortLE(newValue, 1, (int) vlength);     // full length
            Utils.encodeShortLE(newValue, 1 + 2, (int) vlength); // inline length
            Utils.arrayCopyOrFill(value, 0, newValue, (1 + 2 + 2), (int) vlength);
            return newValue;
        } else {
            max -= (1 + 2 + 6);
        }

        if (max < 0) {
            return null;
        }

        long pointerSpace = pageCount * 6;

        byte[] newValue;
        final int inline;
        if (remainder <= max && remainder <= maxInline
                && (pointerSpace <= (max + 6 - (inline = remainder == 0 ? 0 : 2) - remainder))) {
            byte header = (byte) inline;
            final int offset;
            if (vlength < (1L << (2 * 8))) {
                // (2 byte length field)
                offset = 1 + 2;
            } else if (vlength < (1L << (4 * 8))) {
                header |= 0x04; // ff = 1 (4 byte length field)
                offset = 1 + 4;
            } else if (vlength < (1L << (6 * 8))) {
                header |= 0x08; // ff = 2 (6 byte length field)
                offset = 1 + 6;
            } else {
                header |= 0x0c; // ff = 3 (8 byte length field)
                offset = 1 + 8;
            }

            int poffset = offset + inline + remainder;
            newValue = new byte[poffset + (int) pointerSpace];
            if (pageCount > 0) {
                if (value == null) {
                    fill(newValue, poffset, poffset + ((int) pageCount) * 6, (byte) 0);
                } else {
                    try {
                        int voffset = remainder;
                        while (true) {
                            Node node = allocDirtyFragmentNode();
                            try {
                                Utils.encodeInt48LE(newValue, poffset, node.mId);
                                DirectPageOps.p_copyFromArray(value, voffset, node.mPage, 0, pageSize);
                                if (pageCount == 1) {
                                    break;
                                }
                            } finally {
                                node.releaseExclusive();
                            }
                            pageCount--;
                            poffset += 6;
                            voffset += pageSize;
                        }
                    } catch (DatabaseException e) {
                        if (!e.isRecoverable()) {
                            close(e);
                        } else {
                            try {
                                while ((poffset -= 6) >= (offset + inline + remainder)) {
                                    deleteFragment(Utils.decodeUnsignedInt48LE(newValue, poffset));
                                }
                            } catch (Throwable e2) {
                                Utils.suppress(e, e2);
                                close(e);
                            }
                        }
                        throw e;
                    }
                }
            }

            newValue[0] = header;

            if (remainder != 0) {
                Utils.encodeShortLE(newValue, offset, remainder);
                Utils.arrayCopyOrFill(value, 0, newValue, offset + 2, remainder);
            }
        } else {
            pageCount++;
            pointerSpace += 6;

            byte header;
            final int offset;
            if (vlength < (1L << (2 * 8))) {
                header = 0x00; // ff = 0, i=0
                offset = 1 + 2;
            } else if (vlength < (1L << (4 * 8))) {
                header = 0x04; // ff = 1, i=0
                offset = 1 + 4;
            } else if (vlength < (1L << (6 * 8))) {
                header = 0x08; // ff = 2, i=0
                offset = 1 + 6;
            } else {
                header = 0x0c; // ff = 3, i=0
                offset = 1 + 8;
            }

            if (pointerSpace <= (max + 6)) {
                newValue = new byte[offset + (int) pointerSpace];
                if (pageCount > 0) {
                    if (value == null) {
                        fill(newValue, offset, offset + ((int) pageCount) * 6, (byte) 0);
                    } else {
                        int poffset = offset;
                        try {
                            int voffset = 0;
                            while (true) {
                                Node node = allocDirtyFragmentNode();
                                try {
                                    Utils.encodeInt48LE(newValue, poffset, node.mId);
                                    long page = node.mPage;
                                    if (pageCount > 1) {
                                        DirectPageOps.p_copyFromArray(value, voffset, page, 0, pageSize);
                                    } else {
                                        DirectPageOps.p_copyFromArray(value, voffset, page, 0, remainder);
                                        DirectPageOps.p_clear(page, remainder, pageSize(page));
                                        break;
                                    }
                                } finally {
                                    node.releaseExclusive();
                                }
                                pageCount--;
                                poffset += 6;
                                voffset += pageSize;
                            }
                        } catch (DatabaseException e) {
                            if (!e.isRecoverable()) {
                                close(e);
                            } else {
                                try {
                                    while ((poffset -= 6) >= offset) {
                                        deleteFragment(Utils.decodeUnsignedInt48LE(newValue, poffset));
                                    }
                                } catch (Throwable e2) {
                                    Utils.suppress(e, e2);
                                    close(e);
                                }
                            }
                            throw e;
                        }
                    }
                }
            } else {
                header |= 0x01;
                newValue = new byte[offset + 6];
                if (value == null) {
                    Utils.encodeInt48LE(newValue, offset, 0);
                } else {
                    int levels = calculateInodeLevels(vlength);
                    Node inode = allocDirtyFragmentNode();
                    try {
                        Utils.encodeInt48LE(newValue, offset, inode.mId);
                        writeMultilevelFragments(levels, inode, value, 0, vlength);
                        inode.releaseExclusive();
                    } catch (DatabaseException e) {
                        if (!e.isRecoverable()) {
                            close(e);
                        } else {
                            try {
                                deleteMultilevelFragments(levels, inode, vlength);
                            } catch (Throwable e2) {
                                Utils.suppress(e, e2);
                                close(e);
                            }
                        }
                        throw e;
                    } catch (Throwable e) {
                        close(e);
                        throw e;
                    }
                }
            }

            newValue[0] = header;
        }

        if (vlength < (1L << (2 * 8))) {
            Utils.encodeShortLE(newValue, 1, (int) vlength);
        } else if (vlength < (1L << (4 * 8))) {
            Utils.encodeIntLE(newValue, 1, (int) vlength);
        } else if (vlength < (1L << (6 * 8))) {
            Utils.encodeInt48LE(newValue, 1, vlength);
        } else {
            Utils.encodeLongLE(newValue, 1, vlength);
        }

        return newValue;
    }

    int calculateInodeLevels(long vlength) {
        long[] caps = mFragmentInodeLevelCaps;
        int levels = 0;
        while (levels < caps.length) {
            if (vlength <= caps[levels]) {
                break;
            }
            levels++;
        }
        return levels;
    }

    static long decodeFullFragmentedValueLength(int header, long fragmented, int off) {
        switch ((header >> 2) & 0x03) {
            default:
                return DirectPageOps.p_ushortGetLE(fragmented, off);
            case 1:
                return DirectPageOps.p_intGetLE(fragmented, off) & 0xffffffffL;
            case 2:
                return DirectPageOps.p_uint48GetLE(fragmented, off);
            case 3:
                return DirectPageOps.p_longGetLE(fragmented, off);
        }
    }

    private void writeMultilevelFragments(int level, Node inode,
                                          byte[] value, int voffset, long vlength)
            throws IOException {
        long page = inode.mPage;
        level--;
        long levelCap = levelCap(level);

        int childNodeCount = childNodeCount(vlength, levelCap);

        int poffset = 0;
        try {
            for (int i = 0; i < childNodeCount; i++) {
                Node childNode = allocDirtyFragmentNode();
                DirectPageOps.p_int48PutLE(page, poffset, childNode.mId);
                poffset += 6;

                int len = (int) Math.min(levelCap, vlength);
                if (level <= 0) {
                    long childPage = childNode.mPage;
                    DirectPageOps.p_copyFromArray(value, voffset, childPage, 0, len);
                    DirectPageOps.p_clear(childPage, len, pageSize(childPage));
                    childNode.releaseExclusive();
                } else {
                    try {
                        writeMultilevelFragments(level, childNode, value, voffset, len);
                    } finally {
                        childNode.releaseExclusive();
                    }
                }

                vlength -= len;
                voffset += len;
            }
        } finally {
            DirectPageOps.p_clear(page, poffset, pageSize(page));
        }
    }

    private static int childNodeCount(long vlength, long levelCap) {
        int count = (int) ((vlength + (levelCap - 1)) / levelCap);
        if (count < 0) {
            count = childNodeCountOverflow(vlength, levelCap);
        }
        return count;
    }

    private static int childNodeCountOverflow(long vlength, long levelCap) {
        return BigInteger.valueOf(vlength).add(BigInteger.valueOf(levelCap - 1))
                .divide(BigInteger.valueOf(levelCap)).intValue();
    }

    byte[] reconstructKey(long fragmented, int off, int len) throws IOException {
        try {
            return reconstruct(fragmented, off, len);
        } catch (LargeValueException e) {
            throw new LargeKeyException(e.getLength(), e.getCause());
        }
    }

    byte[] reconstruct(long fragmented, int off, int len) throws IOException {
        return reconstruct(fragmented, off, len, null);
    }

    byte[] reconstruct(long fragmented, int off, int len, long[] stats)
            throws IOException {
        int header = DirectPageOps.p_byteGet(fragmented, off++);
        len--;

        long vLen;
        switch ((header >> 2) & 0x03) {
            default:
                vLen = DirectPageOps.p_ushortGetLE(fragmented, off);
                break;

            case 1:
                vLen = DirectPageOps.p_intGetLE(fragmented, off);
                if (vLen < 0) {
                    vLen &= 0xffffffffL;
                    if (stats == null) {
                        throw new LargeValueException(vLen);
                    }
                }
                break;

            case 2:
                vLen = DirectPageOps.p_uint48GetLE(fragmented, off);
                if (vLen > Integer.MAX_VALUE && stats == null) {
                    throw new LargeValueException(vLen);
                }
                break;

            case 3:
                vLen = DirectPageOps.p_longGetLE(fragmented, off);
                if (vLen < 0 || (vLen > Integer.MAX_VALUE && stats == null)) {
                    throw new LargeValueException(vLen);
                }
                break;
        }

        {
            int vLenFieldSize = 2 + ((header >> 1) & 0x06);
            off += vLenFieldSize;
            len -= vLenFieldSize;
        }

        byte[] value;
        if (stats != null) {
            stats[0] = vLen;
            value = null;
        } else {
            try {
                value = new byte[(int) vLen];
            } catch (OutOfMemoryError e) {
                throw new LargeValueException(vLen, e);
            }
        }

        int vOff = 0;
        if ((header & 0x02) != 0) {
            int inLen = DirectPageOps.p_ushortGetLE(fragmented, off);
            off += 2;
            len -= 2;
            if (value != null) {
                DirectPageOps.p_copyToArray(fragmented, off, value, vOff, inLen);
            }
            off += inLen;
            len -= inLen;
            vOff += inLen;
            vLen -= inLen;
        }

        long pagesRead = 0;

        if ((header & 0x01) == 0) {
            while (len >= 6) {
                long nodeId = DirectPageOps.p_uint48GetLE(fragmented, off);
                off += 6;
                len -= 6;
                int pLen;
                if (nodeId == 0) {
                    pLen = Math.min((int) vLen, mPageSize);
                } else {
                    Node node = nodeMapLoadFragment(nodeId);
                    pagesRead++;
                    try {
                        long page = node.mPage;
                        pLen = Math.min((int) vLen, pageSize(page));
                        if (value != null) {
                            DirectPageOps.p_copyToArray(page, 0, value, vOff, pLen);
                        }
                    } finally {
                        node.releaseShared();
                    }
                }
                vOff += pLen;
                vLen -= pLen;
            }
        } else {
            long inodeId = DirectPageOps.p_uint48GetLE(fragmented, off);
            if (inodeId != 0) {
                Node inode = nodeMapLoadFragment(inodeId);
                pagesRead++;
                int levels = calculateInodeLevels(vLen);
                pagesRead += readMultilevelFragments(levels, inode, value, vOff, vLen);
            }
        }

        if (stats != null) {
            stats[1] = pagesRead;
        }

        return value;
    }

    private long readMultilevelFragments(int level, Node inode,
                                         byte[] value, int voffset, long vlength)
            throws IOException {
        try {
            long pagesRead = 0;

            long page = inode.mPage;
            level--;
            long levelCap = levelCap(level);

            int childNodeCount = childNodeCount(vlength, levelCap);

            for (int poffset = 0, i = 0; i < childNodeCount; poffset += 6, i++) {
                long childNodeId = DirectPageOps.p_uint48GetLE(page, poffset);
                int len = (int) Math.min(levelCap, vlength);

                if (childNodeId != 0) {
                    Node childNode = nodeMapLoadFragment(childNodeId);
                    pagesRead++;
                    if (level <= 0) {
                        if (value != null) {
                            DirectPageOps.p_copyToArray(childNode.mPage, 0, value, voffset, len);
                        }
                        childNode.releaseShared();
                    } else {
                        pagesRead += readMultilevelFragments
                                (level, childNode, value, voffset, len);
                    }
                }

                vlength -= len;
                voffset += len;
            }

            return pagesRead;
        } finally {
            inode.releaseShared();
        }
    }

    void deleteFragments(long fragmented, int off, int len)
            throws IOException {
        int header = DirectPageOps.p_byteGet(fragmented, off++);
        len--;

        long vLen;
        if ((header & 0x01) == 0) {
            vLen = 0;
        } else {
            switch ((header >> 2) & 0x03) {
                default:
                    vLen = DirectPageOps.p_ushortGetLE(fragmented, off);
                    break;
                case 1:
                    vLen = DirectPageOps.p_intGetLE(fragmented, off) & 0xffffffffL;
                    break;
                case 2:
                    vLen = DirectPageOps.p_uint48GetLE(fragmented, off);
                    break;
                case 3:
                    vLen = DirectPageOps.p_longGetLE(fragmented, off);
                    break;
            }
        }

        {
            int vLenFieldSize = 2 + ((header >> 1) & 0x06);
            off += vLenFieldSize;
            len -= vLenFieldSize;
        }

        if ((header & 0x02) != 0) {
            int inLen = 2 + DirectPageOps.p_ushortGetLE(fragmented, off);
            off += inLen;
            len -= inLen;
        }

        if ((header & 0x01) == 0) {
            while (len >= 6) {
                long nodeId = DirectPageOps.p_uint48GetLE(fragmented, off);
                off += 6;
                len -= 6;
                deleteFragment(nodeId);
            }
        } else {
            long inodeId = DirectPageOps.p_uint48GetLE(fragmented, off);
            if (inodeId != 0) {
                Node inode = removeInode(inodeId);
                int levels = calculateInodeLevels(vLen);
                deleteMultilevelFragments(levels, inode, vLen);
            }
        }
    }

    private void deleteMultilevelFragments(int level, Node inode, long vlength)
            throws IOException {
        long page = inode.mPage;
        level--;
        long levelCap = levelCap(level);

        int childNodeCount = childNodeCount(vlength, levelCap);
        long[] childNodeIds = new long[childNodeCount];
        for (int poffset = 0, i = 0; i < childNodeCount; poffset += 6, i++) {
            childNodeIds[i] = DirectPageOps.p_uint48GetLE(page, poffset);
        }
        deleteNode(inode);

        if (level <= 0) for (long childNodeId : childNodeIds) {
            deleteFragment(childNodeId);
        }
        else for (long childNodeId : childNodeIds) {
            long len = Math.min(levelCap, vlength);
            if (childNodeId != 0) {
                Node childNode = removeInode(childNodeId);
                deleteMultilevelFragments(level, childNode, len);
            }
            vlength -= len;
        }
    }

    private Node removeInode(long nodeId) throws IOException {
        Node node = nodeMapGetAndRemove(nodeId);
        if (node == null) {
            node = allocLatchedNode(nodeId, NodeContext.MODE_UNEVICTABLE);
            // node.type(TYPE_FRAGMENT);
            readNode(node, nodeId);
        }
        return node;
    }

    void deleteFragment(long nodeId) throws IOException {
        if (nodeId != 0) {
            Node node = nodeMapGetAndRemove(nodeId);
            if (node != null) {
                deleteNode(node);
            } else try {
                if (mInitialReadState != Node.CACHED_CLEAN) {
                    mPageDb.recyclePage(nodeId);
                } else {
                    mPageDb.deletePage(nodeId, true);
                }
            } catch (Throwable e) {
                close(e);
                throw e;
            }
        }
    }

    private static long[] calculateInodeLevelCaps(int pageSize) {
        long[] caps = new long[10];
        long cap = pageSize;
        long scalar = pageSize / 6; // 6-byte pointers

        int i = 0;
        while (i < caps.length) {
            caps[i++] = cap;
            long next = cap * scalar;
            if (next / scalar != cap) {
                caps[i++] = Long.MAX_VALUE;
                break;
            }
            cap = next;
        }

        if (i < caps.length) {
            long[] newCaps = new long[i];
            arraycopy(caps, 0, newCaps, 0, i);
            caps = newCaps;
        }

        return caps;
    }

    long levelCap(int level) {
        return mFragmentInodeLevelCaps[level];
    }

    private void emptyAllFragmentedTrash(boolean checkpoint) throws IOException {
        FragmentedTrash trash = mFragmentedTrash;
        if (trash != null && trash.emptyAllTrash(mEventListener) && checkpoint) {
            checkpoint(false, 0, 0);
        }
    }

    public FragmentedTrash fragmentedTrash() throws IOException {
        FragmentedTrash trash = mFragmentedTrash;
        if (trash != null) {
            return trash;
        }
        mOpenTreesLatch.acquireExclusive();
        try {
            if ((trash = mFragmentedTrash) != null) {
                return trash;
            }
            Tree tree = openInternalTree(Tree.FRAGMENTED_TRASH_ID, true);
            return mFragmentedTrash = new FragmentedTrash(tree);
        } finally {
            mOpenTreesLatch.releaseExclusive();
        }
    }

    long removeSparePage() {
        return mSparePagePool.remove();
    }

    void addSparePage(long page) {
        mSparePagePool.add(page);
    }

    void readNode(Node node, long id) throws IOException {
        // mPageDb.readPage(id, node.mPage);
        if (mFullyMapped) {
            node.mPage = mPageDb.directPagePointer(id);
        } else {
            mPageDb.readPage(id, node.mPage);
        }
        node.mId = id;
        node.mCachedState = mInitialReadState;
    }

    @Override
    public com.linglong.engine.event.EventListener eventListener() {
        return mEventListener;
    }

    @Override
    public void checkpoint(boolean force, long sizeThreshold, long delayThresholdNanos)
            throws IOException {
        mCheckpointLock.lock();
        try {
            if (isClosed()) {
                return;
            }

            cleanupUnreferencedTrees();

            final Node root = mRegistry.mRoot;

            long nowNanos = System.nanoTime();

            if (!force) {
                thresholdCheck:
                {
                    if (delayThresholdNanos == 0) {
                        break thresholdCheck;
                    }

                    if (delayThresholdNanos > 0 &&
                            ((nowNanos - mLastCheckpointNanos) >= delayThresholdNanos)) {
                        break thresholdCheck;
                    }

                    if (mRedoWriter == null || mRedoWriter.shouldCheckpoint(sizeThreshold)) {
                        break thresholdCheck;
                    }
                    flush(2);
                    return;
                }

                treeCheck:
                {
                    root.acquireShared();
                    try {
                        if (root.mCachedState != Node.CACHED_CLEAN) {
                            break treeCheck;
                        }
                    } finally {
                        root.releaseShared();
                    }
                    flush(2);
                    return;
                }
            }

            mLastCheckpointNanos = nowNanos;

            if (mEventListener != null) {
                mEventListener.notify(EventType.CHECKPOINT_BEGIN, "Checkpoint begin");
            }

            boolean resume = true;

            long header = mCommitHeader;
            UndoLog masterUndoLog = mCommitMasterUndoLog;

            if (header == DirectPageOps.p_null()) {
                header = DirectPageOps.p_calloc(mPageDb.pageSize(), mPageDb.isDirectIO());
                resume = false;
                if (masterUndoLog != null) {
                    // TODO: 存储设备已满后，关闭时候抛出？
                    throw new AssertionError();
                }
            }

            final RedoWriter redo = mRedoWriter;

            try {
                int hoff = mPageDb.extraCommitDataOffset();
                DirectPageOps.p_intPutLE(header, hoff + I_ENCODING_VERSION, ENCODING_VERSION);

                if (redo != null) {
                    redo.checkpointPrepare();
                }

                while (true) {
                    mCommitLock.acquireExclusive();
                    if (root.tryAcquireShared()) {
                        break;
                    }
                    mCommitLock.releaseExclusive();
                }

                mCheckpointFlushState = CHECKPOINT_FLUSH_PREPARE;

                if (!resume) {
                    DirectPageOps.p_longPutLE(header, hoff + I_ROOT_PAGE_ID, root.mId);
                }

                final long redoNum, redoPos, redoTxnId;
                if (redo == null) {
                    redoNum = 0;
                    redoPos = 0;
                    redoTxnId = 0;
                } else {
                    redo.checkpointSwitch(mTxnContexts);
                    redoNum = redo.checkpointNumber();
                    redoPos = redo.checkpointPosition();
                    redoTxnId = redo.checkpointTransactionId();
                }

                DirectPageOps.p_longPutLE(header, hoff + I_CHECKPOINT_NUMBER, redoNum);
                DirectPageOps.p_longPutLE(header, hoff + I_REDO_TXN_ID, redoTxnId);
                DirectPageOps.p_longPutLE(header, hoff + I_REDO_POSITION, redoPos);
                DirectPageOps.p_longPutLE(header, hoff + I_REPL_ENCODING, redo == null ? 0 : redo.encoding());

                long txnId = 0;
                final long masterUndoLogId;

                if (resume) {
                    masterUndoLogId = masterUndoLog == null ? 0 : masterUndoLog.topNodeId();
                } else {
                    byte[] workspace = null;

                    for (TransactionContext txnContext : mTxnContexts) {
                        txnId = txnContext.higherTransactionId(txnId);

                        synchronized (txnContext) {
                            if (txnContext.hasUndoLogs()) {
                                if (masterUndoLog == null) {
                                    masterUndoLog = new UndoLog(this, 0);
                                }
                                workspace = txnContext.writeToMaster(masterUndoLog, workspace);
                            }
                        }
                    }

                    if (masterUndoLog == null) {
                        masterUndoLogId = 0;
                    } else {
                        masterUndoLogId = masterUndoLog.persistReady();
                        if (masterUndoLogId == 0) {
                            masterUndoLog = null;
                        }
                    }

                    mCommitMasterUndoLog = masterUndoLog;
                }

                DirectPageOps.p_longPutLE(header, hoff + I_TRANSACTION_ID, txnId);
                DirectPageOps.p_longPutLE(header, hoff + I_MASTER_UNDO_LOG_PAGE_ID, masterUndoLogId);

                mPageDb.commit(resume, header, (boolean resume_, long header_) -> {
                    flush(resume_, header_);
                });
            } catch (Throwable e) {
                if (mCommitHeader != header) {
                    DirectPageOps.p_delete(header);
                }

                if (mCheckpointFlushState == CHECKPOINT_FLUSH_PREPARE) {
                    mCheckpointFlushState = CHECKPOINT_NOT_FLUSHING;
                    root.releaseShared();
                    mCommitLock.releaseExclusive();
                    if (redo != null) {
                        redo.checkpointAborted();
                    }
                }

                throw e;
            }

            deleteCommitHeader();
            mCommitMasterUndoLog = null;

            if (masterUndoLog != null) {
                CommitLock.Shared shared = mCommitLock.acquireShared();
                try {
                    if (!isClosed()) {
                        shared = masterUndoLog.doTruncate(mCommitLock, shared, false);
                    }
                } finally {
                    shared.release();
                }
            }

            if (mRedoWriter != null) {
                mRedoWriter.checkpointFinished();
            }

            if (mEventListener != null) {
                double duration = (System.nanoTime() - mLastCheckpointNanos) / 1_000_000_000.0;
                mEventListener.notify(EventType.CHECKPOINT_COMPLETE,
                        "Checkpoint completed in %1$1.3f seconds",
                        duration, TimeUnit.SECONDS);
            }
        } finally {
            mCheckpointLock.unlock();
        }
    }

    private void flush(final boolean resume, final long header) throws IOException {
        Object custom = mCustomTxnHandler;
        if (custom != null) {
            custom = mCustomTxnHandler.checkpointStart(this);
        }

        int stateToFlush = mCommitState;

        if (resume) {
            if (header != mCommitHeader) {
                throw new AssertionError();
            }
            stateToFlush ^= 1;
        } else {
            if (mInitialReadState != Node.CACHED_CLEAN) {
                mInitialReadState = Node.CACHED_CLEAN;
            }
            mCommitState = (byte) (stateToFlush ^ 1);
            mCommitHeader = header;
        }

        mCheckpointFlushState = stateToFlush;

        mRegistry.mRoot.releaseShared();
        mCommitLock.releaseExclusive();

        if (mRedoWriter != null) {
            mRedoWriter.checkpointStarted();
        }

        if (mEventListener != null) {
            mEventListener.notify(EventType.CHECKPOINT_FLUSH, "Flushing all dirty nodes");
        }

        try {
            mCheckpointer.flushDirty(mNodeContexts, stateToFlush);

            if (mRedoWriter != null) {
                mRedoWriter.checkpointFlushed();
            }

            if (mCustomTxnHandler != null) {
                mCustomTxnHandler.checkpointFinish(this, custom);
            }
        } finally {
            mCheckpointFlushState = CHECKPOINT_NOT_FLUSHING;
        }

        if (mEventListener != null) {
            mEventListener.notify(EventType.CHECKPOINT_SYNC, "Forcibly persisting all changes");
        }
    }

    public static long readRedoPosition(long header, int offset) {
        return DirectPageOps.p_longGetLE(header, offset + I_REDO_POSITION);
    }
}
