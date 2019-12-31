# kafka面试问题

听过kafka在面试中经常被问到为此整理了20个问题及其答案~

## 

1. Kafka中的ISR(InSyncRepli)、OSR(OutSyncRepli)、AR(AllRepli)又代表什么？

    ISR:同步队列副本：This is the subset of the replicas list that is currently alive and caught-up to the leader.caught-up的意思就是该follower的消息基本能与leader保持一致，如果leader出现故障，新的leader将从isr中选出。

    OSR:不同步队列副本，不是ISR中的副本就使OSR中的副本，OSR中的副本与ISR中的leader联系上之后，

    AR:所有副本。

2. Kafka中的HW、LEO等分别代表什么？

    LEO是指每个分区的每个副本最后一条消息的offset
    HW，一个分区中所有ISR中副本的最小的offset

3. Kafka中是怎么体现消息顺序性的？

    每条消息写入Leader时offset是递增的，早进入的消息offset小，后进入的消息offset大。读数据时一个分区内的消息按照offset由小到大的顺序读取。由此体现了分区内的消息具有顺序性，但是一个topic的多条消息之间不具有顺序性。

4. Kafka中的分区器、序列化器、拦截器是否了解？它们之间的处理顺序是什么？

    拦截器-->序列化器-->分区器

    拦截器:拦截器可以对读入的每条消息进行拦截和处理。
序列化器：同flume,hdfs一样，消息的传输需要序列化
分区器：为每条记录分配一个区，默认是轮询分配，自定义分区器时是对拦截器处理和序列化器处理后的数据进行分区。

5. Kafka生产者客户端的整体结构是什么样子的？使用了几个线程来处理？分别是什么？
   
    整体结构分为两个线程main()和sender()和一个线程共享变量RecordAccumulator。

main线程负责对数据的拦截
```java
TopicPartition tp = null;
        try {
            // first make sure the metadata for the topic is available
           somecode...
            try {
                //序列化key
                serializedKey = keySerializer.serialize(record.topic(), record.headers(), record.key());
            } catch (ClassCastException cce) {
                  somecode...
            }
            byte[] serializedValue;
            try {
                //序列化value
                serializedValue = valueSerializer.serialize(record.topic(), record.headers(), record.value());
            } catch (ClassCastException cce) {
                 somecode...
            }
            // 分区
            int partition = partition(record, serializedKey, serializedValue, cluster);
            tp = new TopicPartition(record.topic(), partition);

            setReadOnly(record.headers());
            Header[] headers = record.headers().toArray();

            somecode...
            //写入线程共享变量
            RecordAccumulator.RecordAppendResult result = accumulator.append(tp, timestamp, serializedKey,
                    serializedValue, headers, interceptCallback, remainingWaitMs);
                    //如果一个batch满了或者创建了一个新的batch，那就唤醒sender
            if (result.batchIsFull || result.newBatchCreated) {

                log.trace("Waking up the sender since topic {} partition {} is either full or getting a new batch", record.topic(), partition);
                this.sender.wakeup();
            }
            return result.future;
            //
```

6. “消费组中的消费者个数如果超过topic的分区，那么就会有消费者消费不到数据”这句话是否正确？

   正确，一个组中的消费者不能同时消费一个分区，如果消费者过多，多的消费者将不能消费到数据。

7.  消费者提交消费位移时提交的是当前消费到的最新消息的offset还是offset+1？

    offset+1。

8.  有哪些情形会造成重复消费

    重复消费发生的源头是消费者已经消费了，但是还没来得及提交offset

9.  有哪些情形会造成漏消费

    漏消费发生的源头是提交了offset，但是还没来得及消费数据就挂了。

10. 当你使用kafka-topics.sh创建（删除）了一个topic之后，Kafka背后会执行什么逻辑？
    - 在zookeeper中得/brokers/topics下创建一个新得topic节点
    - 出发Controller得监听程序
    - 由Controller负责topic得创建工作，并更新metadata cache

11. topic得分区数可不可以增加，怎么增加
    topic得分区数可以增加，但是不能减少。增加命令为`kafka-topics.sh --zookeeper localhost:2181/kafka --alter --topic topic-config --partitions 3`

12. kafka由内部得topic嘛？如果由是什么，有什么用
    _consumer_offsets, 保存消费者得offset

13. Kafka分区分配得概念
    一个topic多个分区，一个消费者组多个消费者，故需要将分区分配个消费者(roundrobin、range)

14. kafka的日志目录结构
    每个分区对应一个文件夹，文件夹的命名为topic-0，topic-1，内部为.log和.index文件.log和index文件一一对应

15. 如果我指定了一个offset,kafkacontroller怎么查找到对应的消息

    分三步，第一步看文件名，例如offset为5，寻找一个区间包括5的index文件。第二步，从这个index文件中找到offset为5的消息在log文件中的位置。第三步，从log文件中取消息

16. 聊一聊kafka controller的作用
    它负责管理整个集群中所有分区和副本的状态。当某个分区的leader副本出现故障时，由controller负责为该分区选举新的leader副本。当检测到某个分区的ISR集合发生变化时，由controller负责通知所有broker更新其元数据信息。当使用kafka-topics.sh脚本为某个topic增加分区数量时，同样还是由控制器负责分区的重新分配。


17. Kafka消息是采用Pull模式，还是Push模式
    在producer阶段,是向broker用Push模式，在consumer阶段,是向broker用Pull模式，在Pull模式下,consumer可以根据自身速率选择如何拉取数据,避免了低速率的consumer发生崩溃的问题，但缺点是,consumer要时不时的去询问broker是否有新数据,容易发生死循环,内存溢出，因此设计了一个timeout

18. kafka中有哪些地方需要选举？选举策略又有哪些、
    controller和leader需要选举

    当broker启动时，会尝试会去创建/controller节点，创建成功即成为controller。如果该controller死亡，/controller节点会释放，由新的broker创建此节点成为新的controller.

19. 失效副本是什么，有哪些应对措施

    osr中的副本，如果与leader通信后，会尝试与leader同步，同步的策略是首先将当前记录的hw之后的消息删除，然后与leader同步，当与leader基本同步之后（存储的消息的offset大于当前isr中的hw），就重新回到isr之中

20. kafka的哪些设计让他具有高性能
    1. 顺序读写
    2. 分区
    3. 0复制
    4. 分页

21. Kafka分区保证顺序
    使用Deque=>在发送B时发生错误，在回滚时从头部写入                                                              