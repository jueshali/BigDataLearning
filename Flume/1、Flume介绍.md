# Flume介绍

## Flume是什么

Flume是一个高可用，高可靠，分布式的海量日志采集（可以多源采集），聚合，传输的系统，该系统基于流式架构，灵活简单。

## Flume有什么组成

### Agent

一个Agent是一个JVM进程，它通过event的形式将数据从源头送至目的地。Agent内部由Source，channel和Sink组成

### Source

Source是负责接收数据到Flume Agent的组件。Source组件可以处理各种类型各种格式的日志数据，常见的Source类型包括

#### Avro Source
Listens on Avro port and receives events from external Avro client streams. When paired with the built-in Avro Sink on another (previous hop) Flume agent, it can create tiered collection topologies. 主要的作用是可以用于多个Agent的串联。在使用时需要定义端口和host两个属性


#### Netcat Source
绑定一个netcat端口，可以收集这个端口收到的消息。在使用时需配置端口和host两个属性

```java
a1.sources=r1a1.channels=c1
a1.sources.r1.type=netcat
a1.sources.r1.bind=0.0.0.0
a1.sources.r1.port=6666
a1.sources.r1.channels=c1
```
#### exec soucrce
>exec soucrce可以通过执行一个linux命令，获取这个命令产生的数据，将数据封装为event!
	>>特点： 
    >>1. 要求命令必须是一个可以持续产生数据的命令，例如tail -f 或cat 
	>>2. 不能使用date这些只会返回一条结果就结束的命令！
	>>3. 一旦linux进程停止，source也会自动停止！
	>>4. 和其他的异步source一样，存放丢失数据的风险！如果可以在application端自动缓存数据，可以解决风险！

```java
a1.sources=r1
a1.channels=c1
a1.sources.r1.type=exec
a1.sources.r1.command=tail -F /var/log/secure
a1.sources.r1.channels=c1
```

#### SpoolingDir source
>SpoolingDir source可以通过监听一个目录中，新产生的文件，将文件中的内容封装为event!	
	>>特点：
    >>1. SpoolingDir source需要区别哪些是历史文件(已经处理过的文件)，哪些是新文件
	>>2. 可以指定策略（删除或重命名）！
	>>3. SpoolingDir中不能存入与之前同名的文件，并且存入的文件应当是封闭的。如果存入的文件被修改，或者有了与之前同名的文件，就会报错
    >>4. 所以在实际中，可以使用SpoolingDir在特定时间点一次存入一段时间的日志。

#### TailDir source
>TailDirSouce可以以接近实时的速度tail指定文件夹中文件新增的行	
	>>特点：
    >>1. 可靠，不会丢数据，TailDir维护了一个Json格式的文件，该文件记录了当前已经读取到多少行。

#### Avro source
>Avro source可以绑定一个端口，接受avro格式的数据！
	>>可以使用flume提供的avro-client向指定的端口发送数据，avrosource也可以收到！
### Sink

sink负责从channel中拉取数据，之后根据不同的目的地，将数据写入到指定的外部存储设备或传输到下一个flume进程！

#### Logger sink

logger sink主要用于测试和调试，将event使用logger输出到控制台或文件中！一次输出一个16字节。

```java
a1.channels=c1
a1.sinks=k1
a1.sinks.k1.type=logger
a1.sinks.k1.channel=c1
```

#### hdfs sink
>可以将event写入到hdfs!
    >>特点
    >>1. 支持文件的自动滚动(基于文件的大小|基于时间|基于event的数量),企业中一般基于文件大小，设置一个块为好。
	>>2. 可以在路径上设置转义序列，这个转义序列会在event上传时，被自动替换.一旦在路径上使用了基于时间的转义序列，要求event的header中必须带有timestamp=时间戳！可以使用TimestampInterceptor为event自动添加上此属性，或可以指定useLocalTimpStamp=true!
	>>3. 支持普通的文件和sequncefile两种格式，这两种格式都支持压缩
	>>4. 支持根据主机名或时间，对数据进行分桶或分区等操作.通过文件的自动滚动设置。  hdfs.roll*** 和hdfs.round***,(hdfs.roundValue多久划一次),(hdfs.roundUnit单位)	
	>>5. 要求flumeagent运行的机器，必须已经安装了hadoop或者持有和hdfs进行通信的jar包

```java
a1.channels=c1
a1.sinks=k1
a1.sinks.k1.type=hdfs
a1.sinks.k1.channel=c1
a1.sinks.k1.hdfs.path=/flume/events/%y-%m-%d/%H%M%S
a1.sinks.k1.hdfs.filePrefix=events-
a1.sinks.k1.hdfs.round=true
a1.sinks.k1.hdfs.roundValue=10
a1.sinks.k1.hdfs.roundUnit=minute
```

#### avro sink
>将event按avro写出，需要指定端口和路径。用于与其他agent连接。

### Channel

Channel是source和sink之间的缓冲，可以允许source和sink运行在不同的速率之上。Flume带有Memory Channel和File Channel两种类型的Channel，

>1. Memory Channel是内存中的队列。Memory Channel在不需要关心数据丢失的情景下适用。如果需要关心数据丢失，那么Memory Channel就不应该使用，因为程序死亡、机器宕机或者重启都会导致数据丢失。
>2. File Channel将所有事件写到磁盘。因此在程序关闭或机器宕机的情况下不会丢失数据。


### Flume的其他相关概念

#### Event

Event是Flume数据传输的基本单元，Flume以Event的形式将数据从源头送至目的地。Event由Header和Body两部分组成，Header用来存放该event的一些属性，为K-V结构，Body用来存放该条数据，形式为字节数组。

#### Channel 选择器

从source中写出的文件可以通过Channel选择器写出到多个Channel。Flume提供了两种Channel选择器：
1. Replicating Channel Selector(默认)，将source封装的event复制到当前source对接的所有Channel之中。process先调用拦截器，再将处理后的event发送给对应的channel。
2. Multiplexing Channel Selector将event发送到指定的channel!根据event中的header中指定key的映射，将数据发送到指定的channel! 可以通过拦截器对header加参数例如`a1.sources.r1.interceptors=i1`

#### sinkProcessor
当多个sink从同一个channel拉取数据，徐哟Sink Processors选择一个sink取数据！默认只允许一个sink

1. FailoverSinkProcessor,它维护了一组有优先级的sink!一旦发现当前sink故障，将sink移动到一个池中，暂时冷却！一旦重试后，发现当前sink又可以继续工作，将sink移动回live pool中！
2. LoadBalanceSinkProcessor：它从存活的sink中，使用round_rabin或random算法，随机挑选一个sink消费数据！
