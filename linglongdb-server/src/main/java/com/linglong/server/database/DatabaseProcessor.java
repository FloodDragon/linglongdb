package com.linglong.server.database;

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
import com.linglong.server.database.coordinator.LeaderCoordinator;
import com.linglong.server.utils.Actor;
import com.linglong.server.utils.MixAll;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 数据库处理器
 * <p>
 * Created by liuj-ai on 2021/3/22.
 */
public class DatabaseProcessor extends Actor implements InitializingBean, DisposableBean {

    private final static String LINGLONGDB_DATA = "data";
    private Role role;
    private File baseFile;
    private Database database;
    private DurabilityMode durabilityMode;
    private DatabaseConfig databaseConfig;
    private ReplicatorConfig replicatorConfig;
    private DatabaseReplicator databaseReplicator;
    private LeaderCoordinator leaderCoordinator;

    private Map<byte[], Index> indexBytesMap = new HashMap<>();
    private Map<String, Index> indexNameMap = new HashMap<>();
    private Map<Long, Transaction> transactionMap = new HashMap<>();

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

    @Override
    protected void doAct() throws InterruptedException {
        //TODO 清理空闲的Index
        //TODO 清理事务
    }

    public interface Processor<T, R> {
        R doProcess(T t) throws Exception;
    }

    public static class _Index {
        String name;
        byte[] nameBytes;

        public _Index name(String name) {
            this.name = name;
            return this;
        }

        public _Index name(byte[] nameBytes) {
            this.nameBytes = nameBytes;
            return this;
        }
    }

    public static class _Txn {
        DurabilityMode durabilityMode;

        public _Txn mode(DurabilityMode durabilityMode) {
            this.durabilityMode = durabilityMode;
            return this;
        }
    }

    public static class _KeyValue extends _Index {
        Index index;
        byte[] key;
        byte[] value;

        public _KeyValue index(Index index) {
            this.index = index;
            return this;
        }

        public _KeyValue key(byte[] key) {
            this.key = key;
            return this;
        }

        public _KeyValue value(byte[] value) {
            this.value = value;
            return this;
        }
    }

    public final class FindIndex implements Processor<_Index, Index> {
        @Override
        public Index doProcess(_Index index) throws Exception {
            return StringUtils.isBlank(index.name) ? database.findIndex(index.name) : index.nameBytes != null ? database.findIndex(index.nameBytes) : null;
        }
    }

    public final class OpenIndex implements Processor<_Index, Index> {
        @Override
        public Index doProcess(_Index index) throws Exception {
            return StringUtils.isBlank(index.name) ? database.openIndex(index.name) : index.nameBytes != null ? database.openIndex(index.nameBytes) : null;
        }
    }

    private final class CloseIndex implements Processor<Index, Void> {
        @Override
        public Void doProcess(Index index) throws Exception {
            index.close();
            return null;
        }
    }

    public final class OpenTxn implements Processor<_Txn, Transaction> {
        @Override
        public Transaction doProcess(_Txn txn) throws Exception {
            return txn == null ? database.newTransaction() : txn.durabilityMode == null ? database.newTransaction() : database.newTransaction(txn.durabilityMode);
        }
    }

    public final class KeyValueInsert implements Processor<_KeyValue, Boolean> {
        @Override
        public Boolean doProcess(_KeyValue keyValue) throws Exception {
            return null;
        }
    }
}
