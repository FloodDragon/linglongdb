package com.linglong.server.database.process;

import com.linglong.base.concurrent.LatchCondition;
import com.linglong.base.concurrent.RWLock;
import com.linglong.base.io.OpenOption;
import com.linglong.engine.config.DatabaseConfig;
import com.linglong.engine.config.DurabilityMode;
import com.linglong.engine.core.frame.Database;
import com.linglong.engine.core.frame.Filter;
import com.linglong.engine.core.frame.Index;
import com.linglong.engine.core.tx.Transaction;
import com.linglong.engine.event.ReplicationEventListener;
import com.linglong.replication.DatabaseReplicator;
import com.linglong.replication.Role;
import com.linglong.replication.confg.ReplicatorConfig;
import com.linglong.server.config.LinglongdbProperties;
import com.linglong.server.database.exception.TxnNotFoundException;
import com.linglong.server.utils.MixAll;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 数据库处理器
 * <p>
 * Created by liuj-ai on 2021/3/22.
 */
public class DatabaseProcessor implements InitializingBean, DisposableBean {

    private final static String LINGLONGDB_DATA = "data";
    private Role role;
    private File baseFile;
    private Database database;
    private DurabilityMode durabilityMode;
    private DatabaseConfig databaseConfig;
    private ReplicatorConfig replicatorConfig;
    private DatabaseReplicator databaseReplicator;
    private LeaderCoordinator leaderCoordinator;
    /* txnid ->  Transaction*/
    private final Map<Long, Transaction> transactionMap = new ConcurrentHashMap<>();

    private final RWLock indexLock = new RWLock();
    /* index name -> Index */
    private final Map<String, Index> indexMap = new HashMap<>();


    private LinglongdbProperties linglongdbProperties;
    private ReplicationEventListener replicationEventListener;

    public DatabaseProcessor(LinglongdbProperties linglongdbProperties, ReplicationEventListener replicationEventListener) {
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

    public DatabaseReplicator getDatabaseReplicator() {
        return databaseReplicator;
    }

    public Role getRole() {
        return role;
    }

    public DurabilityMode getDurabilityMode() {
        return durabilityMode;
    }

    public Index findIndex(String indexName) throws Exception {
        try {
            indexLock.acquireShared();
            return StringUtils.isBlank(indexName) ? null : (indexMap.containsKey(indexName) ? indexMap.get(indexName) : new FindIndex().process(new _IndexName().indexName(indexName)));
        } finally {
            indexLock.releaseShared();
        }
    }

    public _Txn findTxn(long txnId) throws Exception {
        return new _Txn().txnId(txnId).transaction(transactionMap.get(txnId));
    }

    public interface Processor<T, R> {

        R doProcess(T t) throws Exception;

        default R process(T t) throws Exception {
            try {
                before();
                return doProcess(t);
            } finally {
                after();
            }
        }

        default void before() {
        }

        default void after() {
        }
    }

    public static class _IndexName {
        String indexName;

        public _IndexName indexName(String name) {
            this.indexName = name;
            return this;
        }
    }

    public static class _Txn {
        Long txnId;
        boolean willCommit;
        Transaction transaction;

        public _Txn txnId(Long txnId) {
            this.txnId = txnId;
            return this;
        }

        public _Txn commit() {
            this.willCommit = true;
            return this;
        }

        public _Txn rollback() {
            this.willCommit = false;
            return this;
        }

        public _Txn transaction(Transaction transaction) {
            this.transaction = transaction;
            return this;
        }
    }

    public static class _Options extends _IndexName {
        byte[] key;
        byte[] value;
        byte[] oldValue;
        byte[] lowKey;
        byte[] highKey;
        long count;

        //新事务
        boolean newTxn;
        //已开启事务
        Long openedTxnId;
        boolean openedTxn;
        //驱逐
        Filter filter;

        public _Options key(byte[] key) {
            this.key = key;
            return this;
        }

        public _Options value(byte[] value) {
            this.value = value;
            return this;
        }

        public _Options oldValue(byte[] value) {
            this.oldValue = oldValue;
            return this;
        }

        public _Options lowKey(byte[] lowKey) {
            this.lowKey = lowKey;
            return this;
        }

        public _Options highKey(byte[] highKey) {
            this.highKey = highKey;
            return this;
        }

        public _Options count(long count) {
            this.count = count;
            return this;
        }

        public _Options newTxn() {
            this.newTxn = true;
            this.openedTxn = false;
            this.openedTxnId = null;
            return this;
        }

        public _Options indexName(String name) {
            super.indexName = name;
            return this;
        }

        public _Options txn(Long txnId) {
            this.openedTxn = true;
            this.newTxn = false;
            this.openedTxnId = txnId;
            return this;
        }

        public _Options filter(Filter filter) {
            this.filter = filter;
            return this;
        }
    }

    protected abstract class AbsIndexHandler implements Processor<_Options, Boolean> {
        @Override
        public Boolean doProcess(_Options options) throws Exception {
            final Index index = StringUtils.isBlank(options.indexName) ?
                    null : (indexMap.containsKey(options.indexName) ? indexMap.get(options.indexName) : new OpenIndex().process(options));
            if (index != null) {
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
                        new TxnCommitOrRollback().process(r ? new _Txn().txnId(txn.getId()).commit() : new _Txn().txnId(txn.getId()).rollback());
                    }
                    return r;
                } catch (Exception ex) {
                    if (autoCommitOrRollback) {
                        new TxnCommitOrRollback().process(new _Txn().txnId(txn.getId()).rollback());
                    }
                    throw ex;
                }
            } else {
                return Boolean.FALSE;
            }
        }

        @Override
        public void before() {
            indexLock.acquireShared();
        }

        @Override
        public void after() {
            indexLock.releaseShared();
        }

        protected abstract boolean doHandle(Index index, Transaction txn, _Options options) throws Exception;
    }

    /**
     * 打开索引
     */
    public class OpenIndex implements Processor<_IndexName, Index> {
        @Override
        public Index doProcess(_IndexName indexName) throws Exception {
            if (StringUtils.isBlank(indexName.indexName)) {
                return null;
            }
            Index idx;
            if ((idx = indexMap.get(indexName.indexName)) == null) {
                idx = database.openIndex(indexName.indexName);
                indexMap.put(idx.getNameString(), idx);
            }
            return idx;
        }

        @Override
        public void before() {
            indexLock.upgrade();
        }

        @Override
        public void after() {
            indexLock.downgrade();
        }
    }

    /**
     * 查找索引
     */
    public class FindIndex implements Processor<_IndexName, Index> {
        @Override
        public Index doProcess(_IndexName indexName) throws Exception {
            if (StringUtils.isBlank(indexName.indexName)) {
                return null;
            }
            Index idx = indexMap.get(indexName.indexName);
            if (idx == null && (idx = database.findIndex(indexName.indexName)) != null) {
                indexMap.put(idx.getNameString(), idx);
            }
            return idx;
        }

        @Override
        public void before() {
            indexLock.upgrade();
        }

        @Override
        public void after() {
            indexLock.downgrade();
        }
    }

    /**
     * 清理索引
     */
    private class CleanIndex implements Processor<String[], Void> {
        @Override
        public Void doProcess(String[] names) throws Exception {
            if (indexMap.isEmpty()) {
                return null;
            }
            if (names == null || names.length == 0) {
                for (Map.Entry<String, Index> entry : indexMap.entrySet()) {
                    entry.getValue().close();
                }
                indexMap.clear();
            } else {
                for (String name : names) {
                    Index index = indexMap.remove(name);
                    if (index != null) {
                        index.close();
                    }
                }
            }
            return null;
        }

        @Override
        public void before() {
            indexLock.acquireExclusive();
        }

        @Override
        public void after() {
            indexLock.releaseExclusive();
        }
    }

    /**
     * 开启事务
     */
    public class OpenTxn implements Processor<DurabilityMode, _Txn> {
        @Override
        public _Txn doProcess(DurabilityMode mode) throws Exception {
            Transaction transaction = mode == null ? database.newTransaction() : database.newTransaction(mode);
            transactionMap.put(transaction.getId(), transaction);
            return new _Txn().txnId(transaction.getId()).transaction(transaction);
        }
    }

    /**
     * 事务提交或回滚
     */
    public class TxnCommitOrRollback implements Processor<_Txn, Boolean> {
        @Override
        public Boolean doProcess(_Txn txn) throws Exception {
            Transaction transaction = transactionMap.remove(txn.txnId);
            if (transaction != null) {
                if (txn.willCommit) {
                    transaction.commit();
                } else {
                    transaction.reset();
                }
                return Boolean.TRUE;
            } else {
                return Boolean.FALSE;
            }
        }
    }

    /**
     * KV存储
     */
    public class KeyValueStore extends AbsIndexHandler {

        @Override
        protected boolean doHandle(Index index, Transaction txn, _Options options) throws Exception {
            index.store(txn, options.key, options.value);
            return true;
        }
    }

    /**
     * KV插入
     */
    public class KeyValueInsert extends AbsIndexHandler {

        @Override
        protected boolean doHandle(Index index, Transaction txn, _Options options) throws Exception {
            return index.insert(txn, options.key, options.value);
        }
    }

    /**
     * KV替换
     */
    public class KeyValueReplace extends AbsIndexHandler {

        @Override
        protected boolean doHandle(Index index, Transaction txn, _Options options) throws Exception {
            return index.replace(txn, options.key, options.value);
        }
    }

    /**
     * KV更新
     */
    public class KeyValueUpdate extends AbsIndexHandler {

        @Override
        protected boolean doHandle(Index index, Transaction txn, _Options options) throws Exception {
            return index.update(txn, options.key, options.oldValue, options.value);
        }
    }

    /**
     * KV删除
     */
    public class KeyValueDelete extends AbsIndexHandler {

        @Override
        protected boolean doHandle(Index index, Transaction txn, _Options options) throws Exception {
            return index.delete(txn, options.key);
        }
    }

    /**
     * KV移除
     */
    public class KeyValueRemove extends AbsIndexHandler {

        @Override
        protected boolean doHandle(Index index, Transaction txn, _Options options) throws Exception {
            return index.remove(txn, options.key, options.value);
        }
    }

    /**
     * KV交换
     */
    public class KeyValueExchange extends AbsIndexHandler {

        @Override
        protected boolean doHandle(Index index, Transaction txn, _Options options) throws Exception {
            options.oldValue(index.exchange(txn, options.key, options.value));
            return true;
        }
    }

    /**
     * KV是否存在
     */
    public class KeyValueExists extends AbsIndexHandler {

        @Override
        protected boolean doHandle(Index index, Transaction txn, _Options options) throws Exception {
            return index.exists(txn, options.key);
        }
    }

    /**
     * 加载KV
     */
    public class KeyValueLoad extends AbsIndexHandler {

        @Override
        protected boolean doHandle(Index index, Transaction txn, _Options options) throws Exception {
            options.value(index.load(txn, options.key));
            return true;
        }
    }

    /**
     * 计算count
     */
    public class IndexCount extends AbsIndexHandler {

        @Override
        protected boolean doHandle(Index index, Transaction txn, _Options options) throws Exception {
            options.count(index.count(options.lowKey, options.highKey));
            return true;
        }
    }

    /**
     * 索引删除
     */
    public class IndexDelete implements Processor<String, Boolean> {

        @Override
        public Boolean doProcess(String indexName) throws Exception {
            Index index = StringUtils.isBlank(indexName) ? null : (indexMap.containsKey(indexName) ? indexMap.get(indexName) : new FindIndex().process(new _IndexName().indexName(indexName)));
            if (index != null) {
                try {
                    indexLock.upgrade();
                    database.deleteIndex(index);
                    indexMap.remove(indexName);
                } finally {
                    indexLock.downgrade();
                }
                return Boolean.TRUE;
            } else {
                return Boolean.FALSE;
            }
        }

        @Override
        public void before() {
            indexLock.acquireShared();
        }

        @Override
        public void after() {
            indexLock.releaseShared();
        }
    }

    /**
     * 索引状态
     */
    public class IndexStats implements Processor<_Options, Index.Stats> {

        @Override
        public Index.Stats doProcess(_Options options) throws Exception {
            Index index = StringUtils.isBlank(options.indexName) ? null : (indexMap.containsKey(options.indexName) ? indexMap.get(options.indexName) : new FindIndex().process(options));
            return index != null ? index.analyze(options.lowKey, options.highKey) : null;
        }

        @Override
        public void before() {
            indexLock.acquireShared();
        }

        @Override
        public void after() {
            indexLock.releaseShared();
        }
    }

    /**
     * 索引驱逐
     */
    public class IndexEvict extends AbsIndexHandler {

        @Override
        protected boolean doHandle(Index index, Transaction txn, _Options options) throws Exception {
            options.count(index.evict(txn, options.lowKey, options.highKey, options.filter, true));
            return Boolean.TRUE;
        }
    }
}
