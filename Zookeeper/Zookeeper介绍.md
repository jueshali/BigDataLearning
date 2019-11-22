# Zookeeper介绍

## Zookeeper是什么

ZooKeeper是一个分布式数据一致性解决方案，分布式应用程序可以基于 ZooKeeper 实现诸如数据发布/订阅、负载均衡、命名服务、分布式协调/通知、集群管理、Master 选举、分布式锁和分布式队列等功能。它底层只提供两个功能1、管理用户程序提交的数据。2提供对用户数据节点进行监听的服务。

## ZooKeeper 的一些重要概念

- Session 指的是 ZooKeeper 服务器与客户端会话。在 ZooKeeper 中，一个客户端连接是指客户端和服务器之间的一个 TCP 长连接。客户端启动的时候，首先会与服务器建立一个 TCP 连接，从第一次连接建立开始，客户端会话的生命周期也开始了。通过这个连接，客户端能够通过心跳检测（tickTime）与服务器保持有效的会话，也能够向Zookeeper服务器发送请求并接受响应，同时还能够通过该连接接收来自服务器的Watch事件通知.
- Znode是数据单元，Zookeeper将所有数据存储在内存中，数据模型是一棵树（Znode Tree)，由斜杠（/）的进行分割的路径，就是一个Znode。在Zookeeper中，node可以分为持久节点和临时节点两类。所谓持久节点是指一旦这个ZNode被创建了，除非主动进行ZNode的移除操作，否则这个ZNode将一直保存在Zookeeper上。而临时节点就不一样了，它的生命周期和客户端会话绑定，一旦客户端会话失效，那么这个客户端创建的所有临时节点都会被移除。共有四种节点（PERSISTENT，PERSISTENT_SEQUENTIAL，EPHEMERAL，EPHEMERAL_SEQUENTIAL）
- 对应于每个ZNode，Zookeeper 都会为其维护一个叫作 Stat 的数据结构，Stat中记录了这个 ZNode 的三个数据版本，分别是version（当前ZNode的版本）、cversion（当前ZNode子节点的版本）和 cversion（当前ZNode的ACL版本（权限信息））。
- Watcher（事件监听器），是Zookeeper中的一个很重要的特性。Zookeeper允许用户在指定节点上注册一些Watcher，并且在一些特定事件触发的时候，ZooKeeper服务端会将事件通知到感兴趣的客户端上去，该机制是Zookeeper实现分布式协调服务的重要特性。

## ZooKeeper 的特点

1. 一致性：zookeeper中的数据按照顺序分批入库，且最终一致！
2. 原子性：一次数据更新要么成功，要么失败。
3. 单一视图：client无论连接到哪个ZK节点，数据都是一致的。
4. 可靠性：每次对zk的操作状态都会保存到服务端，每个server保存一份相同的数据副本。
5. 更新请求顺序进行，来自同一个client的更新请求按其发送顺序依次执行。
6. 实时性，在一定时间范围内，client能读到最新数据。

## ZooKeeper 集群角色介绍

Zookeeper虽然在配置文件中并没有指定master和slave。但是，zookeeper工作时，是有一个节点为leader，其他则为follower，Leader是通过内部的选举机制临时产生的。选举时只要有半数机器投票给某一台机器那这台就可以当选为Leader.选举时，id号设置大的成为Leader的可能性更高。

- Leader：负责进行投票的发起和决议，更新系统状态
- follower：用于接收客户请求并向客户端返回结果，在选举Leader过程中参与投票

## Zookeeper 读写数据流程

## ZooKeeper 配置和常见命令

Zookeeper的安装解压即可，需要配置的只要两个地方。

1. 根据模板文件创建zoo.cfg，配置dataDir和集群的ip信息   server.1 = $host_name1:2888:3888 server.2 = $host_name2:2888:3888
2. 在配置的dataDir下建立一个myid文件写入$host_name1机器下，server后的id.
3. 配置环境变量

命令

- ls path --> [watch] --> 查节点
- stat -->path  -->查状态
- ls2 -->path -->[watch] -->查节点和状态
- get --> path -->[watch] -->查数据
- create --> path -->建立节点
- set path  -->data -->设置数据
- delete --> path -->删除节点
