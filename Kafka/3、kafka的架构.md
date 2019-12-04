# kafka的架构

## kafka文件存储机制

Kafka中消息是以topic进行分类的，生产者生产消息，消费者消费消息，都是面向topic的。每个partition对应一个log文件，该log文件中存储的就是producer生产的数据，Producer生产的数据会不断追加到log文件的末端。为了防止log过大，kafka采取了分片和索引机制。
- 分片是指把每一个partition分成多个segment文件。当一个segment文件过大时（默认1g），并发生滚动。生成一个新的文件，文件的命名是按照文件中的第一条消息的offset.
- 引入索引文件的目的就是便于利用二分查找快速定位message位置,定位message时先看文件名，在利用索引进行定位消息。

## kafka生产者

### 分区
>生产数据时分区的原因
>>- 方便再集群中扩展：每个Partition可以通过调整以适应它所在的机器，而一个topic又可以有多个Partition组成，因此整个集群就可以适应任意大小的数据了；
>>- 可以提高并发:可以按照Partitin为单位进行读写
> 分区的原则
>>- 可以直接指定分区号
>>- 没有指定分区号，但指定key值时按照key的hash值进行分区
>>- 既没有partition也没有Key值得情况下，生成一个随机数，之后在这个随机数递增的基础上分区。

### 数据可靠性的保证

>副本数据同步策略
>- 在Zookeeper中，为保证数据的可靠性，其采取一个半数机制，在超过半数的机器写数据成功后，Leader会告诉第一步与客户端通信的服务器写数据成功。但是在kafka之中，采用的是一个需要所有follwer都同步之后，才发送ack.这样做最大的坏处是集群中出现故障节点时会导致Leader迟迟能发送ack。

>ISR
>- 为解决上一同步策略中的问题，Leader维护了一个ISR，意为与leader保持同步的follower集合。如果follower长时间没有向leader同步数据 ，那这个follower将不被保存在ISR之中。在副本数据同步时，只需要在ISR之中的节点同步了leader就会发送ack消息。

>OSR
>- 与ISR对应，follower不被保存在ISR之中就会保存在OSR之中。ISR+OSR就是AR

>ack应答机制
>>- acks=0 producer不等待broker的ack，这一操作提供了一个最低的延迟，broker一接收到还没有写入磁盘就已经返回，当broker故障时有可能丢失数据；（producer一直发，如果发的时候leader坏了，那数据就会丢失）
>>- acks=1 producer等待broker的ack，partition的leader落盘成功后返回ack，如果在follower同步成功之前leader故障，那么将会丢失数据；(leader怀了，还没来得及同步，但是由于producer收到了ack，那这个ack指示的数据就不会再follower成为leader时重新发送，发生了数据丢失)
>>- acks=-1 producer等待broker的ack，partition的leader和follower全部落盘成功后才返回ack。但是如果在follower同步完成后，broker发送ack之前，leader发生故障，那么会造成数据重复。（虽然同步了，但是还没告诉producer我收到了，producer重新发送就会重复了数据。）

> 语义
>>- at least once: acks=all
>>- at most once acks=0
>>- exactly once idempotent+ at least once .开启`enable.idempotence=true`即可，在broker端对producer的记录进行缓存，缓存<producerID,Partition,SequcenceNum>
> 故障处理细节
>> follower故障，follower发生故障后会被临时踢出ISR，待该follower恢复后，follower会读取本地磁盘记录的上次的HW,并将log文件高于此HW的部分截取掉，从HW开始向leader进行同步，等该follower 的LEO大于等于当前partition的HW，就可以重新加入ISR.
>> leader故障:leader发生故障之后，会从ISR中选出一个新的leader，之后，为保证多个副本之间的数据一致性，其余的follower会先将各自的log文件高于HW的部分截掉，然后从新的leader同步数据
>> LEO（log end offset）指得是一个副本中最后一个offset。HW（leader_epoch)指得是所有ISR副本LEO的最小值。在Leader和follower共同工作时，为防止出现从leader读入的数据多余follower已经存储的数据
>>> 例如消费者从leader读到5了，follower才存入3，此时leader故障，follower成为新的leader。读的数据就出问题了，因此，在kafka中只有Hw之前的数据才对消费者暴露。

### 消费者

消费者读数据是采取拉数据的方式，此方式好处是可以适应不同速率的消费者。缺点是当队列中没有数据时消费者会拉取到空数据。针对这个问题，Kafka的消费者在消费时会传入一个时长参数，如果当前没有数据可供消费，consumer会等待一段时间（timeout）后再返回。

#### 消费的分区分配策略

当消费者消费时只指定了主题（topic），没有指定分区时，系统会自动为当前consumer_group的多个consumer自动分配分区。分区的分配有两种策略：

- range: 针对每一个topic，n=分区数/消费者数，m=分区数/消费者数的余数，那么，前m个分配n+1个，之后的分配n个。
- round_robin：将所有的Topic和Partition按照字典顺序排序，然后对每个Consumer进行轮询分配，如果轮询到的消费者订阅了该topic则分配一个分区，否则直接跳过。

#### offset的维护

在kafka中，由于消费者在消费时可能会出现断电等故障，因此consumer需要记录自己消费到哪个offset，以便故障时恢复。如果consumer的分区是由kafka分配的时,这个offser由kafka维护。如果consumer指定了分区和offset(只指定分区时,offset默认值为latest)，那么kafka将不会维持offset.

### kafa高效读写数据的原因

- 顺序读写
- 0复制技术
- 分页读写

### zookeeper在kafka中的作用

Controller的选举以及Controller的管理工作都是基于zookeeper的