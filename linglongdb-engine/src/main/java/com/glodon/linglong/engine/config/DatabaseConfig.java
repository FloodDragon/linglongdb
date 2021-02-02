package com.glodon.linglong.engine.config;

import com.glodon.linglong.base.common.Crypto;
import com.glodon.linglong.base.common.Utils;
import com.glodon.linglong.base.exception.DatabaseException;
import com.glodon.linglong.base.io.FileFactory;
import com.glodon.linglong.base.io.OpenOption;
import com.glodon.linglong.base.io.PageArray;
import com.glodon.linglong.engine.core.frame.Database;
import com.glodon.linglong.engine.core.frame.Index;
import com.glodon.linglong.engine.core.LocalDatabase;
import com.glodon.linglong.engine.core.page.PageCache;
import com.glodon.linglong.engine.core.page.PartitionedPageCache;
import com.glodon.linglong.engine.event.EventListener;
import com.glodon.linglong.engine.event.EventPrinter;
import com.glodon.linglong.engine.event.EventType;
import com.glodon.linglong.engine.extend.RecoveryHandler;
import com.glodon.linglong.engine.extend.ReplicationManager;
import com.glodon.linglong.engine.extend.TransactionHandler;
import com.glodon.linglong.engine.core.lock.LockUpgradeRule;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

/**
 * @author Stereo
 */
public final class DatabaseConfig implements Cloneable, Serializable {
    private static final long serialVersionUID = 1L;

    File mBaseFile;

    public File getBaseFile() {
        return mBaseFile;
    }

    boolean mMkdirs;

    public boolean isMkdirs() {
        return mMkdirs;
    }

    File[] mDataFiles;
    boolean mMapDataFiles;
    transient PageArray mDataPageArray;

    public PageArray getDataPageArray() {
        return mDataPageArray;
    }

    FileFactory mFileFactory;

    public FileFactory getFileFactory() {
        return mFileFactory;
    }

    long mMinCachedBytes;

    public void setMinCachedBytes(long mMinCachedBytes) {
        this.mMinCachedBytes = mMinCachedBytes;
    }

    public long getMinCachedBytes() {
        return mMinCachedBytes;
    }

    long mMaxCachedBytes;

    public void setMaxCachedBytes(long mMaxCachedBytes) {
        this.mMaxCachedBytes = mMaxCachedBytes;
    }

    public long getMaxCachedBytes() {
        return mMaxCachedBytes;
    }

    transient RecoveryHandler mRecoveryHandler;

    public RecoveryHandler getRecoveryHandler() {
        return mRecoveryHandler;
    }

    long mSecondaryCacheSize;

    public void setSecondaryCacheSize(long mSecondaryCacheSize) {
        this.mSecondaryCacheSize = mSecondaryCacheSize;
    }

    public long getSecondaryCacheSize() {
        return mSecondaryCacheSize;
    }

    DurabilityMode mDurabilityMode;

    public DurabilityMode getDurabilityMode() {
        return mDurabilityMode;
    }

    LockUpgradeRule mLockUpgradeRule;

    public LockUpgradeRule getLockUpgradeRule() {
        return mLockUpgradeRule;
    }

    long mLockTimeoutNanos;

    public long getLockTimeoutNanos() {
        return mLockTimeoutNanos;
    }

    long mCheckpointRateNanos;

    public long getCheckpointRateNanos() {
        return mCheckpointRateNanos;
    }

    long mCheckpointSizeThreshold;

    public long getCheckpointSizeThreshold() {
        return mCheckpointSizeThreshold;
    }

    long mCheckpointDelayThresholdNanos;

    public long getCheckpointDelayThresholdNanos() {
        return mCheckpointDelayThresholdNanos;
    }

    int mMaxCheckpointThreads;

    public int getMaxCheckpointThreads() {
        return mMaxCheckpointThreads;
    }

    transient EventListener mEventListener;

    public EventListener getEventListener() {
        return mEventListener;
    }

    public void setEventListener(EventListener mEventListener) {
        this.mEventListener = mEventListener;
    }

    BiConsumer<Database, Index> mIndexOpenListener;

    public BiConsumer<Database, Index> getIndexOpenListener() {
        return mIndexOpenListener;
    }

    boolean mFileSync;
    boolean mReadOnly;

    public boolean isReadOnly() {
        return mReadOnly;
    }

    int mPageSize;

    public int getPageSize() {
        return mPageSize;
    }

    Boolean mDirectPageAccess;

    public void setDirectPageAccess(Boolean mDirectPageAccess) {
        this.mDirectPageAccess = mDirectPageAccess;
    }

    boolean mCachePriming;

    public boolean isCachePriming() {
        return mCachePriming;
    }

    //ReplicatorConfig mReplConfig;
    transient ReplicationManager mReplManager;

    public ReplicationManager getReplManager() {
        return mReplManager;
    }

    int mMaxReplicaThreads;

    public int getMaxReplicaThreads() {
        return mMaxReplicaThreads;
    }

    transient Crypto mCrypto;

    public Crypto getCrypto() {
        return mCrypto;
    }

    transient TransactionHandler mTxnHandler;

    public TransactionHandler getTxnHandler() {
        return mTxnHandler;
    }

    Map<String, ? extends Object> mDebugOpen;

    public Map<String, ? extends Object> getDebugOpen() {
        return mDebugOpen;
    }

    transient long mReplRecoveryStartNanos;

    public void setReplRecoveryStartNanos(long mReplRecoveryStartNanos) {
        this.mReplRecoveryStartNanos = mReplRecoveryStartNanos;
    }

    public long getReplRecoveryStartNanos() {
        return mReplRecoveryStartNanos;
    }

    transient long mReplInitialTxnId;

    public void setReplInitialTxnId(long mReplInitialTxnId) {
        this.mReplInitialTxnId = mReplInitialTxnId;
    }

    public long getReplInitialTxnId() {
        return mReplInitialTxnId;
    }

    public DatabaseConfig() {
        createFilePath(true);
        durabilityMode(null);
        lockTimeout(1, TimeUnit.SECONDS);
        checkpointRate(1, TimeUnit.SECONDS);
        checkpointSizeThreshold(1024 * 1024);
        checkpointDelayThreshold(1, TimeUnit.MINUTES);
    }

    public DatabaseConfig baseFile(File file) {
        mBaseFile = file == null ? null : abs(file);
        return this;
    }

    public DatabaseConfig baseFilePath(String path) {
        mBaseFile = path == null ? null : abs(new File(path));
        return this;
    }

    public DatabaseConfig createFilePath(boolean mkdirs) {
        mMkdirs = mkdirs;
        return this;
    }

    public DatabaseConfig dataFile(File file) {
        dataFiles(file);
        return this;
    }

    public DatabaseConfig dataFiles(File... files) {
        if (files == null || files.length == 0) {
            mDataFiles = null;
        } else {
            File[] dataFiles = new File[files.length];
            for (int i = 0; i < files.length; i++) {
                dataFiles[i] = abs(files[i]);
            }
            mDataFiles = dataFiles;
            mDataPageArray = null;
        }
        return this;
    }

    public DatabaseConfig mapDataFiles(boolean mapped) {
        mMapDataFiles = mapped;
        return this;
    }

    public DatabaseConfig dataPageArray(PageArray array) {
        mDataPageArray = array;
        if (array != null) {
            int expected = mDataPageArray.pageSize();
            if (mPageSize != 0 && mPageSize != expected) {
                throw new IllegalArgumentException
                        ("Page size doesn't match data page array: " + mPageSize + " != " + expected);
            }
            mDataFiles = null;
            mPageSize = expected;
        }
        return this;
    }

    public DatabaseConfig fileFactory(FileFactory factory) {
        mFileFactory = factory;
        return this;
    }

    public DatabaseConfig minCacheSize(long minBytes) {
        mMinCachedBytes = minBytes;
        return this;
    }

    public DatabaseConfig maxCacheSize(long maxBytes) {
        mMaxCachedBytes = maxBytes;
        return this;
    }

    public DatabaseConfig secondaryCacheSize(long size) {
        if (size < 0) {
            // Reserve use of negative size.
            throw new IllegalArgumentException();
        }
        mSecondaryCacheSize = size;
        return this;
    }

    public DatabaseConfig durabilityMode(DurabilityMode durabilityMode) {
        if (durabilityMode == null) {
            durabilityMode = DurabilityMode.SYNC;
        }
        mDurabilityMode = durabilityMode;
        return this;
    }

    public DatabaseConfig lockUpgradeRule(LockUpgradeRule lockUpgradeRule) {
        if (lockUpgradeRule == null) {
            lockUpgradeRule = LockUpgradeRule.STRICT;
        }
        mLockUpgradeRule = lockUpgradeRule;
        return this;
    }

    public DatabaseConfig lockTimeout(long timeout, TimeUnit unit) {
        mLockTimeoutNanos = Utils.toNanos(timeout, unit);
        return this;
    }

    public DatabaseConfig checkpointRate(long rate, TimeUnit unit) {
        mCheckpointRateNanos = Utils.toNanos(rate, unit);
        return this;
    }

    public DatabaseConfig checkpointSizeThreshold(long bytes) {
        mCheckpointSizeThreshold = bytes;
        return this;
    }

    public DatabaseConfig checkpointDelayThreshold(long delay, TimeUnit unit) {
        mCheckpointDelayThresholdNanos = Utils.toNanos(delay, unit);
        return this;
    }

    public DatabaseConfig maxCheckpointThreads(int num) {
        mMaxCheckpointThreads = num;
        return this;
    }

    public DatabaseConfig eventListener(EventListener listener) {
        mEventListener = listener;
        return this;
    }

    public DatabaseConfig indexOpenListener(BiConsumer<Database, Index> listener) {
        mIndexOpenListener = listener;
        return this;
    }

    public DatabaseConfig syncWrites(boolean fileSync) {
        mFileSync = fileSync;
        return this;
    }

    /*
    public DatabaseConfig readOnly(boolean readOnly) {
        mReadOnly = readOnly;
        return this;
    }
    */

    public DatabaseConfig pageSize(int size) {
        if (mDataPageArray != null) {
            int expected = mDataPageArray.pageSize();
            if (expected != size) {
                throw new IllegalArgumentException
                        ("Page size doesn't match data page array: " + size + " != " + expected);
            }
        }
        mPageSize = size;
        return this;
    }

    public DatabaseConfig directPageAccess(boolean direct) {
        mDirectPageAccess = direct;
        return this;
    }

    public DatabaseConfig cachePriming(boolean priming) {
        mCachePriming = priming;
        return this;
    }

    /* TODO 复制配置需要优化
    public DatabaseConfig replicate(ReplicatorConfig config) {
        mReplConfig = config;
        mReplManager = null;
        return this;
    }

    public DatabaseConfig replicate(ReplicationManager manager) {
        mReplManager = manager;
        mReplConfig = null;
        return this;
    }
    */

    public DatabaseConfig maxReplicaThreads(int num) {
        mMaxReplicaThreads = num;
        return this;
    }

    public DatabaseConfig recoveryHandler(RecoveryHandler handler) {
        mRecoveryHandler = handler;
        return this;
    }

    public DatabaseConfig encrypt(Crypto crypto) {
        mCrypto = crypto;
        return this;
    }

    public DatabaseConfig customTransactionHandler(TransactionHandler handler) {
        mTxnHandler = handler;
        return this;
    }

    public TransactionHandler getCustomTransactionHandler() {
        return mTxnHandler;
    }

    public void debugOpen(PrintStream out, Map<String, ? extends Object> properties)
            throws IOException {
        if (out == null) {
            out = System.out;
        }

        if (properties == null) {
            properties = Collections.emptyMap();
        }

        DatabaseConfig config = clone();

        config.eventListener(new EventPrinter(out));
        config.mReadOnly = true;
        config.mDebugOpen = properties;

        if (config.mDirectPageAccess == null) {
            config.directPageAccess(false);
        }

        Database.open(config).close();
    }

    @Override
    public DatabaseConfig clone() {
        try {
            return (DatabaseConfig) super.clone();
        } catch (CloneNotSupportedException e) {
            throw Utils.rethrow(e);
        }
    }

    public PageCache pageCache(EventListener listener) {
        long size = mSecondaryCacheSize;
        if (size <= 0) {
            return null;
        }

        if (listener != null) {
            listener.notify(EventType.CACHE_INIT_BEGIN,
                    "Initializing %1$d bytes for secondary cache", size);
        }

        return new PartitionedPageCache(size, mPageSize);

    }

    public File[] dataFiles() {
        if (mReplManager != null) {
            long encoding = mReplManager.encoding();
            if (encoding == 0) {
                throw new IllegalArgumentException
                        ("Illegal replication manager encoding: " + encoding);
            }
        }

        File[] dataFiles = mDataFiles;
        if (mBaseFile == null) {
            if (dataFiles != null && dataFiles.length > 0) {
                throw new IllegalArgumentException
                        ("Cannot specify data files when no base file is provided");
            }
            return null;
        }

        if (mBaseFile.isDirectory()) {
            throw new IllegalArgumentException("Base file is a directory: " + mBaseFile);
        }

        if (mDataPageArray != null) {
            // Return after the base file checks have been performed.
            return null;
        }

        if (dataFiles == null || dataFiles.length == 0) {
            dataFiles = new File[]{new File(mBaseFile.getPath() + ".db")};
        }

        for (File dataFile : dataFiles) {
            if (dataFile.isDirectory()) {
                throw new IllegalArgumentException("Data file is a directory: " + dataFile);
            }
        }

        return dataFiles;
    }

    public EnumSet<OpenOption> createOpenOptions() {
        EnumSet<OpenOption> options = EnumSet.noneOf(OpenOption.class);
        options.add(OpenOption.RANDOM_ACCESS);
        if (mReadOnly) {
            options.add(OpenOption.READ_ONLY);
        }
        if (mFileSync) {
            options.add(OpenOption.SYNC_IO);
        }
        if (mMapDataFiles) {
            options.add(OpenOption.MAPPED);
        }
        if (mMkdirs) {
            options.add(OpenOption.CREATE);
        }
        return options;
    }

    public void writeInfo(BufferedWriter w) throws IOException {
        String pid = ManagementFactory.getRuntimeMXBean().getName();
        String user;
        try {
            user = System.getProperty("user.name");
        } catch (SecurityException e) {
            user = null;
        }

        Map<String, String> props = new TreeMap<>();

        if (pid != null) {
            set(props, "lastOpenedByProcess", pid);
        }
        if (user != null) {
            set(props, "lastOpenedByUser", user);
        }

        set(props, "baseFile", mBaseFile);
        set(props, "createFilePath", mMkdirs);
        set(props, "mapDataFiles", mMapDataFiles);

        if (mDataFiles != null && mDataFiles.length > 0) {
            if (mDataFiles.length == 1) {
                set(props, "dataFile", mDataFiles[0]);
            } else {
                StringBuilder b = new StringBuilder();
                b.append('[');
                for (int i = 0; i < mDataFiles.length; i++) {
                    if (i > 0) {
                        b.append(", ");
                    }
                    b.append(mDataFiles[i]);
                }
                b.append(']');
                set(props, "dataFiles", b);
            }
        }

        set(props, "minCacheSize", mMinCachedBytes);
        set(props, "maxCacheSize", mMaxCachedBytes);
        set(props, "secondaryCacheSize", mSecondaryCacheSize);
        set(props, "durabilityMode", mDurabilityMode);
        set(props, "lockTimeoutNanos", mLockTimeoutNanos);
        set(props, "checkpointRateNanos", mCheckpointRateNanos);
        set(props, "checkpointSizeThreshold", mCheckpointSizeThreshold);
        set(props, "checkpointDelayThresholdNanos", mCheckpointDelayThresholdNanos);
        set(props, "syncWrites", mFileSync);
        set(props, "pageSize", mPageSize);
        set(props, "directPageAccess", mDirectPageAccess);
        set(props, "cachePriming", mCachePriming);

        w.write('#');
        w.write(Database.class.getName());
        w.newLine();

        w.write('#');
        w.write(java.time.ZonedDateTime.now().toString());
        w.newLine();

        for (Map.Entry<String, String> line : props.entrySet()) {
            w.write(line.getKey());
            w.write('=');
            w.write(line.getValue());
            w.newLine();
        }
    }

    private static void set(Map<String, String> props, String name, Object value) {
        if (value != null) {
            props.put(name, value.toString());
        }
    }

    private static File abs(File file) {
        return file.getAbsoluteFile();
    }

    public final Database open(boolean destroy, InputStream restore) throws IOException {
        boolean openedReplicator = false;
        /* 复制配置需要优化
        if (mReplConfig != null && mReplManager == null
                && mBaseFile != null && !mBaseFile.isDirectory()) {
            if (mEventListener != null) {
                mReplConfig.eventListener(new ReplicationEventListener(mEventListener));
            }
            mReplConfig.baseFilePath(mBaseFile.getPath() + ".repl");
            mReplConfig.createFilePath(mMkdirs);
            mReplManager = DatabaseReplicator.open(mReplConfig);
            openedReplicator = true;
        }
        */

        try {
            return doOpen(destroy, restore);
        } catch (Throwable e) {
            if (openedReplicator) {
                try {
                    mReplManager.close();
                } catch (Throwable e2) {
                    Utils.suppress(e, e2);
                }
                mReplManager = null;
            }

            throw e;
        }
    }

    private Database doOpen(boolean destroy, InputStream restore) throws IOException {
        if (!destroy && restore == null && mReplManager != null) shouldRestore:{
            File[] dataFiles = dataFiles();
            if (dataFiles == null) {
                break shouldRestore;
            }

            for (File file : dataFiles)
                if (file.exists()) {
                    break shouldRestore;
                }

            restore = mReplManager.restoreRequest(mEventListener);
        }

        try {
            if (restore != null) {
                return LocalDatabase.restoreFromSnapshot(this, restore);
            } else if (destroy) {
                return LocalDatabase.destroy(this);
            } else {
                return LocalDatabase.open(this);
            }
        } catch (Throwable throwable) {
            throwable = Utils.rootCause(throwable);
            throw Utils.rethrow(throwable);
        }
    }
}
