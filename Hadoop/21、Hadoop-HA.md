# Hadoop-HA

HA(High Available)，即高可用（7*24小时不中断服务），在Hadoop中，主要是要保证NN和RM的高可用。因此HA可以分为HDFS的HA和YARN的HA

## HDFS的HA

HDFS HA功能通过配置Active/Standby两个NameNodes实现在集群中对NameNode的热备来解决上述问题。如果出现故障，如机器崩溃或机器需要升级维护，这时可通过此种方式将NameNode很快的切换到另外一台机器。

### 实现方法

双NameNode消除单点故障。两个NN分为Active和Standby两种状态，这两个NN各自保存一份元数据，对于元数据的读写只有对Active状态的NN才可以。但Active状态的NN发生错误后，可以手动将Standby状态的NN转化为Active状态。

### 自动故障转移工作机制

- 一个NN与一个Failover controller保持通信。在启动时两个NN的Failover controller在zookeeper上创建一个临时会话。
- 当Failover controller它ping不同一致通信的NN时会认为该NN假死，于是断开Zookeeper上的会话，同时通知另一个NN的Failover controller，其调用SSH命令杀死假死的NN
- 在杀死假死的NN后在本机将本机的NN的状态切换为Active。同时在Zookeeper上创建一个临时节点

### 实现要点

- 元数据管理方式需要改变
- 需要一个状态管理功能模块
- 必须保证两个NameNode之间能够ssh无密码登录
- 隔离（Fence），即同一时刻仅仅有一个NameNode对外提供服务
