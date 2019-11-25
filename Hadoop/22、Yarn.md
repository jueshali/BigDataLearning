# Yarn

## Yarn的基本架构和工作流程

由前面所说，Yarn由ResourceManager、NodeManager、ApplicationMaster和Container等组件构成。四个组件的作用如下

### RM

1. 处理客户端的请求
2. 和NM通信，监控其运行状况
3. 启动或监控ApplicationMaster
4. 负责整合集群计算资源的分配和调度

### NM

1. 管理单个节点上的资源
2. 负责处理来自RM上的请求
3. 处理来自Application的请求

### ApplictaionMaster

1. 负责数据的切分
2. 为应用程序申请资源并分配任务

### Container

是抽象的一个任务在一个Container上运行，用于防止一个任务运行时资源被其他任务占用

## 工作机制

1. Mr程序提交到客户端所在的节点
2. 客户端向RM申请一个Application
3. RM返回Application资源提交路径以及返回Application——id
4. 提交本地资源（配置文件，Jar包，分片等）
5. 资源提交完毕后，客户端向RM申请运行ApplicationMaster.jobrunner
6. RM将用户的请求初始化一个Task
7. 一个NM领取任务，并生成一个Container，包装运行的资源
8. 下载job的资源到NM，并创建一个MRAppmaster。
9. NM向RM申请运行一个MapTask容器
10. MRAppmaster将分别申请MapTask和ReduceTask的运行容器
11. NM运行阶段。

## 调度器

调度以Task为基本单位

### FIFO调度器

FIFO包装先提交的Task先申请到资源，后提交的后申请资源，当出现一个大任务时，可能出现后面的小任务被卡住。资源利用率低！不能满足多样化Job处理的需求

### 容量调度器

有多条队列，每条队列都有FIFO.根据Job提交到队列的顺序为Job的task分配资源！

1. 每个队列，用户，Job都可以配置使用资源的容量(最低限制，最高限制)
2. 空闲队列冗余的资源，可以临时借调给其他队列使用一旦空闲队列，提交了新的job，借调的资源会被锁定，在使用完后立刻归还
3. 权限保证。每个用户只能查看自己提交job的信息
4. 配置灵活。集群管理员可以指定队列的管理员等！所有参数可以在线修改，及时生效！动态管理！

### 公平调度器

对容量调度器在调度策略上的改进！

1. 和容量调度器类似！多个FIFO队列组成！ 
2. 每个队列中，每个Job获取到资源的数量是平等的！
3. 小的Job占优势，可以获取到自己需要的足够资源！ 大的Job占劣势，不能获取到自己需要的足够资源！但是不至于饿死，可以使用部分资源启动部分task!
