# kafka的安装配置和启动

## kafka的安装

1. kafka基于zookeeper,在kafka的server.properties中配置zk的通信地址
2. 在每台broker的server.properties中配置其唯一id

## kafka集群启动操作

> 集群操作
> 
>启动server
> 
> >kafka-server-start.sh -daemon server.properties
> 
> 停止server
> 
> >kafka-server-stop.sh
>  
> 启动生产者
> 
> >  kafka-console-producer.sh  --broker-list xxx --topic xxx
> 
> 启动消费者
> > kafka-console-consumer.sh --bootstrap-server hadoop101:9092 --topic xxx <--formbegining>\[--partition XX --offset]
> >>注意--formbegining和--offset 0在有数据回收的情况下不一定等价。

## kafka主题操作

> 主题操作
> 
>  增
> >kafka-topics.sh  --zookeeper xx --create --topic xx --partition x --replicas x
> 
> 删
> >- kafka-topics.sh  --zookeeper XX:2181  --delete --topic xx
> >- 在执行后，立刻删除zk中的元数据，真正存储的数据被标记为删除。
> 
> 改
>> kafka-topics.sh  --zookeeper xx --alter --topic xx --partitions x
>> 只能改分区的分配策略，以及增加分区数量
> 
> 查
> >	kafka-topics.sh  --zookeeper xx	 --list 查看所有主题
> > kafka-topics.sh --zookeeper xx --describe --topic jk 

