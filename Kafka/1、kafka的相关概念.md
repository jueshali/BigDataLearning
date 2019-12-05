# kafka的相关概念

## 什么是Kafka

kafka是一个分布式的基于发布/订阅模式的消息队列，主要应用于大数据实时处理领域。在Kafka中，Kafka对消息的保存根据topic进行分类，发送消息的称为Producer，消息接收者称为Consumer。一个server成为一个broker，kafka依赖zookeeper保存一些元数据信息

kafka具有高容错，高效的特点。

- Publish and subscribe to streams of records, similar to a message queue or enterprise messaging system.
- Store streams of records in a fault-tolerant durable way.
- Process streams of records as they occur.

kafka的应用

- Building real-time streaming data pipelines that reliably get data between systems or applications
- Building real-time streaming applications that transform or react to the streams of data

## Kafka中的概念

### Broker

一台kafka的服务器就是一个Broker，多个broker组成一个cluster.

### Topic

kafka可以根据不同的业务需求将不同的数据存放在不同的topic中，topic是一个逻辑上的概念，在物理存盘上，topic对应文件夹的前缀。Kafka中的Topics总是多订阅者模式，一个topic可以拥有一个或者多个消费者订阅其上的数据。一个topic可以分布式的存储在多个kafka broker中。

### Partition

每个topic有多个分区，通过分区的设计，topic可以不断的进行扩展。使用分区有两个好处，一是topic可以不断的扩展，即一个topic的多个分区可以分布式的存储在多个broker之中。二是在消费者消费数据时，可以并行的处理。kafka可以保证一个partition中的数据有序，但不能保证一个topic中读出的数据有序。例如：A-1(1,3) A-2(2,4)

### Offset

每条消息通过offset标识记录的有序，offset是线性递增的，消费者可以通过offset从任意位置读取数据。

### 副本机制

日志的分区在kakfa集群的服务器上，每个服务器在处理数据和请求时，共享这些分区，每一个分区都会在已配置的服务器上进行备份，生成多个副本，保证容错性。每一个分区都有一个Leader和follwers。与zookpeer等不同，在kafka中，对于分区的所有写请求都由leader处理，多个follower从leader处拿数据。

### Producer

消息生产者，就是向kafka broker发消息的客户端。生产者负责将记录分配到topic的指定 partition（分区）中。

### Consumer

消息消费者，向kafka broker取消息的客户端。每个消费者都要维护自己读取数据的offset。

### Consumer Group

每个消费者都会使用一个消费组名称来进行标识。同一个组中的不同的消费者实例，可以分布在多个进程或多个机器上。同一个组中的不同consumer只能同时读一个topic的不同分区。总之就是一个组中的consumer读的数据互斥，不同组的consumer读的数据互不干扰。

### Record和offset

kafka中的record在生产时必须指定topic和value属性，可以选择指定partition，key,ts等。每个record在进入到分区后，会生成唯一的offSet,

offset可以让消费者在消费时，自由灵活的从任意位置消费

