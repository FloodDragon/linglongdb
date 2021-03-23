package com.linglong.server.database.process;

import com.linglong.base.concurrent.RWLock;
import com.linglong.engine.config.DatabaseConfig;
import com.linglong.engine.config.DurabilityMode;
import com.linglong.engine.core.frame.Database;
import com.linglong.engine.core.frame.Index;
import com.linglong.engine.core.tx.Transaction;
import com.linglong.engine.event.ReplicationEventListener;
import com.linglong.replication.DatabaseReplicator;
import com.linglong.replication.Role;
import com.linglong.replication.confg.ReplicatorConfig;
import com.linglong.server.config.LinglongdbProperties;
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
        this.role = Role.getRole(linglongdbProperties.getReplicaRole());
        if (this.role == null) {
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
            return StringUtils.isBlank(indexName) ? null : (indexMap.containsKey(indexName) ? indexMap.get(indexName) : new OpenIndex().process(new _IndexName().name(indexName)));
        } finally {
            indexLock.releaseShared();
        }
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

        public _IndexName name(String name) {
            this.indexName = name;
            return this;
        }
    }

    public static class _Txn {
        long txnId;
        boolean isCommit;

        public _Txn txnId(long txnId) {
            this.txnId = txnId;
            return this;
        }

        public _Txn commit() {
            this.isCommit = true;
            return this;
        }

        public _Txn rollback() {
            this.isCommit = false;
            return this;
        }
    }

    public static class _Variable extends _IndexName {
        byte[] key;
        byte[] value;
        byte[] oldValue;
        boolean openTxn;
        byte[] lowKey;
        byte[] highKey;
        long count;

        public _Variable key(byte[] key) {
            this.key = key;
            return this;
        }

        public _Variable value(byte[] value) {
            this.value = value;
            return this;
        }

        public _Variable oldValue(byte[] value) {
            this.oldValue = oldValue;
            return this;
        }

        public _Variable lowKey(byte[] lowKey) {
            this.lowKey = lowKey;
            return this;
        }

        public _Variable highKey(byte[] highKey) {
            this.highKey = highKey;
            return this;
        }

        public _Variable count(long count) {
            this.count = count;
            return this;
        }

        public _Variable openTxn() {
            this.openTxn = true;
            return this;
        }

    }

    public abstract class KeyValueHandler implements Processor<_Variable, Boolean> {
        @Override
        public Boolean doProcess(_Variable kvContent) throws Exception {
            final Index index = StringUtils.isBlank(kvContent.indexName) ? null : (indexMap.containsKey(kvContent.indexName) ? indexMap.get(kvContent.indexName) : new OpenIndex().process(new _IndexName().name(kvContent.indexName)));
            if (index != null) {
                final Transaction txn = kvContent.openTxn ? null : new OpenTxn().process(null);
                try {
                    Boolean r = doHandle(index, txn, kvContent);
                    if (txn != null) {
                        new TxnCommitOrRollback().process(r ? new _Txn().txnId(txn.getId()).commit() : new _Txn().txnId(txn.getId()).rollback());
                    }
                    return r;
                } catch (Exception ex) {
                    if (txn != null) {
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

        protected abstract boolean doHandle(Index index, Transaction txn, _Variable kvContent) throws Exception;
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
            Index idx = indexMap.get(indexName.indexName);
            if (idx == null) {
                idx = database.openIndex(indexName.indexName);
                indexMap.put(idx.getNameString(), idx);
            }
            return idx;
        }

        @Override
        public void before() {
            while (!indexLock.tryUpgrade()) ;
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
            indexLock.acquireExclusive();
        }
    }

    /**
     * 开启事务
     */
    public class OpenTxn implements Processor<DurabilityMode, Transaction> {
        @Override
        public Transaction doProcess(DurabilityMode mode) throws Exception {
            Transaction transaction = mode == null ? database.newTransaction() : database.newTransaction(mode);
            transactionMap.put(transaction.getId(), transaction);
            return transaction;
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
                if (txn.isCommit) {
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
    public class KeyValueStore extends KeyValueHandler {

        @Override
        protected boolean doHandle(Index index, Transaction txn, _Variable kvContent) throws Exception {
            index.store(txn, kvContent.key, kvContent.value);
            return true;
        }
    }

    /**
     * KV插入
     */
    public class KeyValueInsert extends KeyValueHandler {

        @Override
        protected boolean doHandle(Index index, Transaction txn, _Variable kvContent) throws Exception {
            return index.insert(txn, kvContent.key, kvContent.value);
        }
    }

    /**
     * KV替换
     */
    public class KeyValueReplace extends KeyValueHandler {

        @Override
        protected boolean doHandle(Index index, Transaction txn, _Variable kvContent) throws Exception {
            return index.replace(txn, kvContent.key, kvContent.value);
        }
    }

    /**
     * KV更新
     */
    public class KeyValueUpdate extends KeyValueHandler {

        @Override
        protected boolean doHandle(Index index, Transaction txn, _Variable kvContent) throws Exception {
            return index.update(txn, kvContent.key, kvContent.oldValue, kvContent.value);
        }
    }

    /**
     * KV删除
     */
    public class KeyValueDelete extends KeyValueHandler {

        @Override
        protected boolean doHandle(Index index, Transaction txn, _Variable kvContent) throws Exception {
            return index.delete(txn, kvContent.key);
        }
    }

    /**
     * KV移除
     */
    public class KeyValueRemove extends KeyValueHandler {

        @Override
        protected boolean doHandle(Index index, Transaction txn, _Variable kvContent) throws Exception {
            return index.remove(txn, kvContent.key, kvContent.value);
        }
    }

    /**
     * KV交换
     */
    public class KeyValueExchange extends KeyValueHandler {

        @Override
        protected boolean doHandle(Index index, Transaction txn, _Variable kvContent) throws Exception {
            kvContent.oldValue(index.exchange(txn, kvContent.key, kvContent.value));
            return true;
        }
    }

    /**
     * KV是否存在
     */
    public class KeyValueExists extends KeyValueHandler {

        @Override
        protected boolean doHandle(Index index, Transaction txn, _Variable kvContent) throws Exception {
            return index.exists(txn, kvContent.key);

        }
    }

    /**
     * 加载KV
     */
    public class KeyValueLoad extends KeyValueHandler {

        @Override
        protected boolean doHandle(Index index, Transaction txn, _Variable kvContent) throws Exception {
            kvContent.value(index.load(txn, kvContent.key));
            return true;
        }
    }

    /**
     * 计算count
     */
    public class KeyValueCount extends KeyValueHandler {

        @Override
        protected boolean doHandle(Index index, Transaction txn, _Variable kvContent) throws Exception {
            kvContent.count(index.count(kvContent.lowKey, kvContent.highKey));
            return true;
        }
    }
}
