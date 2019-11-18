# Hadoop核心架构简述

- [Hadoop核心架构简述](#hadoop%e6%a0%b8%e5%bf%83%e6%9e%b6%e6%9e%84%e7%ae%80%e8%bf%b0)
  - [HDFS](#hdfs)
    - [Namenode(1个)](#namenode1%e4%b8%aa)
    - [Datanode（n个）](#datanoden%e4%b8%aa)
  - [Yarn](#yarn)
    - [ResourceManager(1个)](#resourcemanager1%e4%b8%aa)
    - [NodeManager（n个）](#nodemanagern%e4%b8%aa)
  - [MapReduce](#mapreduce)
    - [Map阶段](#map%e9%98%b6%e6%ae%b5)
    - [Reduce阶段](#reduce%e9%98%b6%e6%ae%b5)

## HDFS

HDFS负责大数据文件的读写！

HDFS的运行需要由若干不同角色的进程一起启动后组成！

HDFS由namenode和datanode组成。

### Namenode(1个)

1. 处理客户端的请求，
2. 负责管理存储在HDFS上的元数据（元数据是对文件属性的说明信息，例如文件名什么的）
3. 用于接收DataNode信息的上报，分配DataNode任务
4. 分配和记录每个文件存储的块列表
5. 有时候会有SecondryNamenode,2NN和NN是完全不同的。

### Datanode（n个）

1. 负责具体数据的存储和备份
2. 定期上报NN，告诉NN我还活着

## Yarn

Yarn负责集群计算资源的调度！
Yarn的运行需要由不同角色的进程一起启动后组成
Yarn由ResourceManager和NodeManager组成

### ResourceManager(1个)

1. 负责接收客户端提交的job
2. 负责整个集群所有计算资源的管理和调度
3. 负责与NM进行通信，分配任务等。

### NodeManager（n个）

1. 负责当前所运行的机器所拥有的计算资源的管理和调度
2. 定期和RM进行通信，上报资源的使用情况
3. 从RM接收任务，为任务分配计算资源

## MapReduce

MapReduce是一个编程模型，按照这一编程模型编写的程序被称为一个job，这个job在运行时可以分为Map阶段和Reduce阶段。

### Map阶段

将总的任务，切分，切分为若干小的任务，每个任务分配到一台机器运行！每个任务在执行时，需要启动一个进程负责Map阶段任务的执行。这个进程称为MapTask。在造高达前，类似于将汽车拆分各个零件。

### Reduce阶段

负责Map阶段所有运行结果的合并。也需要一个进程负责整个Reduce阶段任务的执行，这个进程称为ReduceTask. 在将汽车拆分为零件后，将零件组成新的高达。

一个Job由若干MapTask和若干ReduceTask组成。每个Task都需要申请计算资源！
每个Job在启动时，会先启动一个MRAppMaster进程，由此进程和YARN进行资源的申请！
YARN在收到每个Job需要启动N个Task的申请后，让NM进程来领取任务，领取后为每个Task分配计算资源！
分配计算资源时，为了保证每个Task计算资源在当前Task使用期间，不被其他的Task所抢占，
需要将每个Task所已经分配的计算资源进行隔离，NM会使用Container对Task的计算资源进行封装，封装后的资源，暂时被冻结，其他Task无法使用。（资源主要是HDFS的磁盘资源和Yarn的Cpu资源）
