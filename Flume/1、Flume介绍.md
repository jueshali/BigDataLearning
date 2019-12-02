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

#### exec soucrce
>exec soucrce可以通过执行一个linux命令，获取这个命令产生的数据，将数据封装为event!
	>>特点： 
    >>1. 要求命令必须是一个可以持续产生数据的命令，例如tail -f 或cat 
	>>2. 不能使用date这些只会返回一条结果就结束的命令！
	>>3. 一旦linux进程停止，source也会自动停止！
	>>4. 和其他的异步source一样，存放丢失数据的风险！如果可以在application端自动缓存数据，可以解决风险！

#### SpoolingDir source
>SpoolingDir source可以通过监听一个目录中，新产生的文件，将文件中的内容封装为event!	
	>>特点：
    >>1. SpoolingDir source需要区别哪些是历史文件(已经处理过的文件)，哪些是新文件
	>>2. 可以指定策略（删除或重命名）！
	>>3. SpoolingDir中不能存入与之前同名的文件，并且存入的文件应当是封闭的。如果存入的文件被修改，或者有了与之前同名的文件，就会报错
    >>4. 所以在实际中，可以使用SpoolingDir在特定时间点一次存入一段时间的日志。	

### Sink

sink负责从channel中拉取数据，之后根据不同的目的地，将数据写入到指定的外部存储设备或传输到下一个flume进程！

#### Logger sink

#### HDFS sink

#### avro sink

### Channel

#### Memory Channel

#### File Channel

