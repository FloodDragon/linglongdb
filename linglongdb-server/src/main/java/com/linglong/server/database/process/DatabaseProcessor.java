package com.linglong.server.database.process;

import com.linglong.base.concurrent.RWLock;
import com.linglong.engine.config.DatabaseConfig;
import com.linglong.engine.config.DurabilityMode;
import com.linglong.engine.core.frame.Database;
import com.linglong.engine.core.frame.Index;
import com.linglong.engine.core.frame.Scanner;
import com.linglong.engine.core.tx.Transaction;
import com.linglong.engine.core.updater.Updater;
import com.linglong.engine.event.ReplicationEventListener;
import com.linglong.replication.DatabaseReplicator;
import com.linglong.replication.Role;
import com.linglong.replication.confg.ReplicatorConfig;
import com.linglong.server.config.LinglongdbProperties;
import com.linglong.server.config.RpcServerProperties;
import com.linglong.server.database.exception.TxnNotFoundException;
import com.linglong.server.utils.MixAll;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.io.File;
import java.util.AbstractMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * 数据库处理器
 * <p>
 * Created by liuj-ai on 2021/3/22.
 */
public class DatabaseProcessor implements InitializingBean, DisposableBean {

    private final static String LINGLONGDB_DATA = "data";
    /* 数据库角色 */
    private Role role;
    /* 数据库基础文件目录 */
    private File baseFile;
    private Database database;
    /* 持久模式 */
    private DurabilityMode durabilityMode;
    private DatabaseConfig databaseConfig;
    private ReplicatorConfig replicatorConfig;
    /* 领导节点协调器 */
    private LeaderCoordinator leaderCoordinator;
    /* 数据库复制器 */
    private DatabaseReplicator databaseReplicator;
    /* 事务、索引锁 */
    private final RWLock txnLock = new RWLock();
    private final IndexMap indexMap = new IndexMap();
    /* txnid ->  Transaction */
    private final Map<Long, Transaction> txnMap = new LinkedHashMap<>();
    /* pid -> ProcessIterator */
    private final Map<String, ProcessIterator> processIteratorMap = new ConcurrentHashMap<>();

    private RpcServerProperties rpcServerProperties;
    private LinglongdbProperties linglongdbProperties;
    private ReplicationEventListener replicationEventListener;

    public DatabaseProcessor(LinglongdbProperties linglongdbProperties,
                             RpcServerProperties rpcServerProperties,
                             ReplicationEventListener replicationEventListener) {
        File file = new File(linglongdbProperties.getBaseDir());
        if (file.isFile()) {
            throw new IllegalArgumentException("linglongdb base dir must be directory: " + file.getAbsolutePath());
        } else {
            this.baseFile = new File(file, LINGLONGDB_DATA);
        }
        this.durabilityMode = DurabilityMode.getDurabilityMode(linglongdbProperties.getDurabilityMode());
        if (this.durabilityMode == null) {
            throw new IllegalArgumentException("linglongdb durability mode error");
        }
        if (!MixAll.checkPowerOfTwo(linglongdbProperties.getPageSize())) {
            throw new IllegalArgumentException("linglongdb page size must be greater than zero and power of 2");
        }
        if (linglongdbProperties.getLockTimeout() <= 0) {
            throw new IllegalArgumentException("linglongdb lock timeout must be greater than zero");
        }
        if (!MixAll.checkPowerOfTwo(linglongdbProperties.getCheckpointSizeThreshold())) {
            throw new IllegalArgumentException("linglongdb checkpoint size threshold must be greater than zero and power of 2");
        }
        if (linglongdbProperties.getCheckpointDelayThreshold() <= 0) {
            throw new IllegalArgumentException("linglongdb checkpoint delay threshold must be greater than zero");
        }
        if (linglongdbProperties.getMaxCheckpointThreads() <= 0) {
            throw new IllegalArgumentException("linglongdb checkpoint max threads must be greater than zero");
        }
        if (linglongdbProperties.getMinCacheSize() <= 0) {
            throw new IllegalArgumentException("linglongdb min cache size must be greater than zero");
        }
        if (linglongdbProperties.getMaxCacheSize() <= 0) {
            throw new IllegalArgumentException("linglongdb max cache size must be greater than zero");
        }
        if (linglongdbProperties.isReplicaEnabled() && (this.role = Role.getRole(linglongdbProperties.getReplicaRole())) == null) {
            throw new IllegalArgumentException("linglongdb replica role error");
        }
        this.rpcServerProperties = rpcServerProperties;
        this.linglongdbProperties = linglongdbProperties;
        this.replicationEventListener = replicationEventListener;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        //创建数据库配置
        this.databaseConfig = new DatabaseConfig()
                .baseFile(baseFile)
                .pageSize(linglongdbProperties.getPageSize())
                .lockTimeout(linglongdbProperties.getLockTimeout(), TimeUnit.MILLISECONDS)
                .checkpointRate(linglongdbProperties.getCheckpointRate(), TimeUnit.MILLISECONDS)
                .checkpointSizeThreshold(linglongdbProperties.getCheckpointSizeThreshold())
                .checkpointDelayThreshold(linglongdbProperties.getCheckpointDelayThreshold(), TimeUnit.MILLISECONDS)
                .maxCheckpointThreads(linglongdbProperties.getMaxCheckpointThreads())
                .minCacheSize(linglongdbProperties.getMinCacheSize())
                .maxCacheSize(linglongdbProperties.getMaxCacheSize())
                .durabilityMode(durabilityMode);
        if (linglongdbProperties.isReplicaEnabled()) {
            //打开数据库复制
            this.replicatorConfig = new ReplicatorConfig()
                    .groupToken(linglongdbProperties.getReplicaGroupToken())
                    .localPort(linglongdbProperties.getReplicaPort())
                    .localRole(role)
                    .eventListener(replicationEventListener)
                    .baseFile(baseFile);

            //设置集群复制发现地址
            if (!CollectionUtils.isEmpty(linglongdbProperties.getReplicaSeedAddresses())) {
                for (String replicaSeedAddress : linglongdbProperties.getReplicaSeedAddresses()) {
                    replicatorConfig.addSeed(replicaSeedAddress);
                }
            }
            //开启集群复制器
            this.databaseReplicator = DatabaseReplicator.open(replicatorConfig);
            this.databaseConfig.replicate(databaseReplicator);
            this.database = Database.open(this.databaseConfig);
            this.leaderCoordinator = new LeaderCoordinator(databaseReplicator, rpcServerProperties);
        } else {
            this.database = Database.open(this.databaseConfig);
        }
    }

    @Override
    public void destroy() throws Exception {
        if (this.database != null) {
            this.database.close();
            this.new CleanIndex().process(null);
        }
    }

    public boolean isReplicaEnabled() {
        return linglongdbProperties.isReplicaEnabled();
    }

    public Database getDatabase() {
        return database;
    }

    public Role getRole() {
        return role;
    }

    public DurabilityMode getDurabilityMode() {
        return durabilityMode;
    }

    public LeaderCoordinator getLeaderCoordinator() {
        return leaderCoordinator;
    }

    protected IndexEntry findIndex(String indexName) throws Exception {
        return findIndex(new IndexName().indexName(indexName));
    }

    protected boolean existsIndex(IndexName indexName) throws Exception {
        return findIndex(indexName) != null;
    }

    public boolean existsIndex(String indexName) throws Exception {
        return findIndex(indexName) != null;
    }

    private IndexEntry findIndex(IndexName indexName) throws Exception {
        if (indexName == null || StringUtils.isBlank(indexName.idxName)) {
            return null;
        } else {
            IndexEntry entry;
            if ((entry = indexMap.find(indexName.idxName)) == null) {
                return new FindIndex().process(indexName);
            } else {
                return entry;
            }
        }
    }

    public TxnOptions findTxn(long txnId) throws Exception {
        try {
            txnLock.acquireShared();
            return new TxnOptions().txnId(txnId).transaction(txnMap.get(txnId));
        } finally {
            txnLock.releaseShared();
        }
    }

    public KeyValueOptions newOptions() {
        return new KeyValueOptions();
    }

    protected abstract class AbsIndexHandler implements Processor<KeyValueOptions, Boolean> {

        @Override
        public Boolean doProcess(KeyValueOptions options) throws Exception {
            if (options == null) {
                return Boolean.FALSE;
            }
            final IndexEntry index = getIndex(options);
            if (index != null) {
                index.sharedLock();
                try {
                    if (existsIndex(options)) {
                        Transaction txn;
                        if (options.newTxn) {
                            txn = new OpenTxn().process(null).transaction;
                        } else if (options.openedTxn) {
                            txn = options.openedTxnId != null ? findTxn(options.openedTxnId).transaction : null;
                            if (txn == null) {
                                throw new TxnNotFoundException(options.openedTxnId);
                            }
                        } else {
                            txn = null;
                        }
                        boolean autoCommitOrRollback = (txn != null && options.newTxn && !options.openedTxn);
                        try {
                            Boolean r = doHandle(index, txn, options);
                            if (autoCommitOrRollback) {
                                new TxnCommitOrRollback().process(r ? new TxnOptions().txnId(txn.getId()).commit() : new TxnOptions().txnId(txn.getId()).rollback());
                            }
                            return r;
                        } catch (Exception ex) {
                            if (autoCommitOrRollback) {
                                new TxnCommitOrRollback().process(new TxnOptions().txnId(txn.getId()).rollback());
                            }
                            throw ex;
                        }
                    } else {
                        return Boolean.FALSE;
                    }
                } finally {
                    index.sharedUnLock();
                }
            } else {
                return Boolean.FALSE;
            }
        }

        protected IndexEntry getIndex(IndexName indexName) throws Exception {
            return findIndex(indexName);
        }

        protected abstract boolean doHandle(IndexEntry index, Transaction txn, KeyValueOptions options) throws Exception;
    }

    /**
     * 打开索引
     */
    public class OpenIndex implements Processor<IndexName, IndexEntry> {
        @Override
        public IndexEntry doProcess(IndexName indexName) throws Exception {
            if (indexName == null || StringUtils.isBlank(indexName.idxName)) {
                return null;
            }
            IndexEntry entry;
            if ((entry = indexMap.find(indexName.idxName)) == null) {
                Index index = database.openIndex(indexName.idxName);
                return indexMap.put(index);
            } else {
                return entry;
            }
        }
    }

    /**
     * 查找索引
     */
    public class FindIndex implements Processor<IndexName, IndexEntry> {
        @Override
        public IndexEntry doProcess(IndexName indexName) throws Exception {
            if (indexName == null || StringUtils.isBlank(indexName.idxName)) {
                return null;
            }
            Index idx;
            IndexEntry index = indexMap.find(indexName.idxName);
            if (index == null && (idx = database.findIndex(indexName.idxName)) != null) {
                return indexMap.put(idx);
            } else {
                return index;
            }
        }
    }

    /**
     * 清理索引
     */
    private class CleanIndex implements Processor<String[], Void> {
        @Override
        public Void doProcess(String[] names) throws Exception {
            indexMap.clean(names);
            return null;
        }
    }

    /**
     * 开启事务
     */
    public class OpenTxn implements Processor<DurabilityMode, TxnOptions> {
        @Override
        public TxnOptions doProcess(DurabilityMode mode) throws Exception {
            Transaction transaction = mode == null ? database.newTransaction() : database.newTransaction(mode);
            try {
                txnLock.acquireExclusive();
                txnMap.put(transaction.getId(), transaction);
                return new TxnOptions().txnId(transaction.getId()).transaction(transaction);
            } finally {
                txnLock.releaseExclusive();
            }
        }
    }

    /**
     * 事务提交或回滚
     */
    public class TxnCommitOrRollback implements Processor<TxnOptions, Boolean> {

        @Override
        public Boolean doProcess(TxnOptions txn) throws Exception {
            Transaction transaction;
            try {
                txnLock.acquireExclusive();
                transaction = txnMap.remove(txn.txnId);
            } finally {
                txnLock.releaseExclusive();
            }
            if (transaction != null) {
                if (txn.willCommit) {
                    transaction.commit();
                } else {
                    transaction.reset();
                }
                return Boolean.TRUE;
            } else {
                throw new TxnNotFoundException(txn.getTxnId());
            }
        }
    }

    /**
     * KV存储
     */
    public class KeyValueStore extends AbsIndexHandler {

        @Override
        protected boolean doHandle(IndexEntry index, Transaction txn, KeyValueOptions options) throws Exception {
            if (options.key != null && options.value != null) {
                index.getIndex().store(txn, options.key, options.value);
                return Boolean.TRUE;
            } else {
                return Boolean.FALSE;
            }
        }

        @Override
        protected IndexEntry getIndex(IndexName indexName) throws Exception {
            IndexEntry entry;
            if ((entry = super.getIndex(indexName)) == null) {
                entry = new OpenIndex().process(indexName);
            }
            return entry;
        }
    }

    /**
     * KV插入
     */
    public class KeyValueInsert extends KeyValueStore {

        @Override
        protected boolean doHandle(IndexEntry index, Transaction txn, KeyValueOptions options) throws Exception {
            return options.key != null && options.value != null ? index.getIndex().insert(txn, options.key, options.value) : Boolean.FALSE;
        }
    }

    /**
     * KV替换
     */
    public class KeyValueReplace extends AbsIndexHandler {

        @Override
        protected boolean doHandle(IndexEntry index, Transaction txn, KeyValueOptions options) throws Exception {
            return options.key != null ? index.getIndex().replace(txn, options.key, options.value) : Boolean.FALSE;
        }
    }

    /**
     * KV更新
     */
    public class KeyValueUpdate extends AbsIndexHandler {

        @Override
        protected boolean doHandle(IndexEntry index, Transaction txn, KeyValueOptions options) throws Exception {
            return options.key != null ? index.getIndex().update(txn, options.key, options.oldValue, options.value) : Boolean.FALSE;
        }
    }

    /**
     * KV删除
     */
    public class KeyValueDelete extends AbsIndexHandler {

        @Override
        protected boolean doHandle(IndexEntry index, Transaction txn, KeyValueOptions options) throws Exception {
            return options.key != null ? index.getIndex().delete(txn, options.key) : Boolean.FALSE;
        }
    }

    /**
     * KV移除
     */
    public class KeyValueRemove extends AbsIndexHandler {

        @Override
        protected boolean doHandle(IndexEntry index, Transaction txn, KeyValueOptions options) throws Exception {
            if (options.key != null && options.value != null) {
                return index.getIndex().remove(txn, options.key, options.value);
            } else {
                return Boolean.FALSE;
            }
        }
    }

    /**
     * KV交换
     */
    public class KeyValueExchange extends AbsIndexHandler {

        @Override
        protected boolean doHandle(IndexEntry index, Transaction txn, KeyValueOptions options) throws Exception {
            if (options.key != null && options.value != null) {
                options.oldValue(index.getIndex().exchange(txn, options.key, options.value));
                return Boolean.TRUE;
            } else {
                return Boolean.FALSE;
            }
        }
    }

    /**
     * KV是否存在
     */
    public class KeyValueExists extends AbsIndexHandler {

        @Override
        protected boolean doHandle(IndexEntry index, Transaction txn, KeyValueOptions options) throws Exception {
            return options.key != null ? index.getIndex().exists(txn, options.key) : Boolean.FALSE;
        }
    }

    /**
     * 加载KV
     */
    public class KeyValueLoad extends AbsIndexHandler {

        @Override
        protected boolean doHandle(IndexEntry index, Transaction txn, KeyValueOptions options) throws Exception {
            if (options.key != null) {
                options.value(index.getIndex().load(txn, options.key));
                return Boolean.TRUE;
            } else {
                return Boolean.FALSE;
            }
        }
    }

    /**
     * 计算count
     */
    public class IndexCount extends AbsIndexHandler {

        @Override
        protected boolean doHandle(IndexEntry index, Transaction txn, KeyValueOptions options) throws Exception {
            options.count(index.getIndex().count(options.lowKey, options.highKey));
            return true;
        }
    }

    /**
     * 索引删除
     */
    public class IndexDelete implements Processor<IndexName, Boolean> {

        @Override
        public Boolean doProcess(IndexName indexName) throws Exception {
            IndexEntry index = findIndex(indexName);
            if (index != null) {
                index.exclusiveLock();
                try {
                    if (existsIndex(indexName)) {
                        database.deleteIndex(index.getIndex()).run();
                        indexMap.clean(indexName.idxName);
                        database.openIndex(indexName.idxName).drop();
                        return Boolean.TRUE;
                    } else {
                        return Boolean.FALSE;
                    }
                } finally {
                    index.exclusiveUnLock();
                }
            } else {
                return Boolean.FALSE;
            }
        }
    }

    /**
     * 索引状态
     */
    public class IndexStats implements Processor<KeyValueOptions, Index.Stats> {

        @Override
        public Index.Stats doProcess(KeyValueOptions options) throws Exception {
            IndexEntry index = findIndex(options);
            if (index != null) {
                index.sharedUnLock();
                try {
                    if (existsIndex(options)) {
                        return index.getIndex().analyze(options.lowKey, options.highKey);
                    } else {
                        return null;
                    }
                } finally {
                    index.sharedUnLock();
                }
            } else {
                return null;
            }
        }
    }

    /**
     * 索引驱逐
     */
    public class IndexEvict extends AbsIndexHandler {

        @Override
        protected boolean doHandle(IndexEntry index, Transaction txn, KeyValueOptions options) throws Exception {
            options.count(index.getIndex().evict(txn, options.lowKey, options.highKey, options.evictFilter, true));
            return Boolean.TRUE;
        }
    }

    /**
     * 索引重命名
     */
    public class IndexRename implements Processor<IndexName, Boolean> {

        @Override
        public Boolean doProcess(IndexName indexName) throws Exception {
            IndexEntry index = findIndex(indexName);
            if (index != null && StringUtils.isNotBlank(indexName.newName)) {
                index.exclusiveLock();
                try {
                    if (existsIndex(indexName)) {
                        database.renameIndex(index.getIndex(), indexName.newName);
                        indexMap.clean(indexName.idxName);
                        indexName.idxName = indexName.newName;
                        findIndex(indexName);
                        return Boolean.TRUE;
                    } else {
                        return Boolean.FALSE;
                    }
                } finally {
                    index.exclusiveUnLock();
                }
            } else {
                return Boolean.FALSE;
            }
        }
    }

    /**
     * 索引扫描(返回是否还有可扫描数据)
     */
    public class IndexKeyValueScan extends AbsIndexHandler {

        @Override
        protected boolean doHandle(IndexEntry index, Transaction txn, KeyValueOptions options) throws Exception {
            Consumer<Map.Entry<byte[], byte[]>> consumer = options.getScanFunc();
            Scanner scanner = index.getIndex().newScanner(txn);
            try {
                if (consumer != null) {
                    scanner.scanAll(((key, value) ->
                            consumer.accept(new AbstractMap.SimpleEntry<byte[], byte[]>(key, value))
                    ));
                }
                return true;
            } finally {
                scanner.close();
            }
        }
    }

    /**
     * 索引批量更新
     */
    public class IndexKeyValueUpdater extends AbsIndexHandler {
        @Override
        protected boolean doHandle(IndexEntry index, Transaction txn, KeyValueOptions options) throws Exception {
            Updater updater = index.getIndex().newUpdater(txn);
            updater.updateAll((k, v) -> {
                if (options.updater != null) {
                    return options.updater.apply(k, v);
                } else {
                    return null;
                }
            });
            updater.close();
            return Boolean.TRUE;
        }
    }
}
