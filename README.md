Linglongdb
============

# 简介：
高吞吐、低延迟、集群高一致性、可自愈的数据库/引擎。

性能方面：向 KV数据库(RocksDB、LevelDB、Redis) 看齐，即：低延迟；

持久化方面：向RDBS数据库(MySQL) 看齐，即：支持MySQL 具备的多副本、高可用、ACID 事务。

即可凭借数据库引擎定制分布式数据库，也可以作为独立数据库，同时也可作为嵌入式数据库。


# 数据库模块说明
- **linglongdb-base(数据库基础库)**
- **linglongdb-replication(数据库集群复制)**   
- **linglongdb-engine(数据库引擎)**
- **linglongdb-sql(数据库SQL解析与重写)**
- **linglongdb-rpc(数据库网络通信)**
- **linglongdb-protocol(数据库集群通信协议)** 正在设计
- **linglongdb-server(数据库服务器)** 正在开发
- **linglongdb-client(数据库客户端)** 正在开发
- **linglongdb-jdbc(数据库jdbc客户端)** 正在规划
