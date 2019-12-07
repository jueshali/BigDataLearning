# RegionServer 和写流程

StoreFile:保存实际数据的物理文件，StoreFile以Hfile的形式存储在HDFS上。每个Store会有一个或多个StoreFile（HFile），数据在每个StoreFile中都是有序的。

MemStore:写缓存，由于HFile中的数据要求是有序的，所以数据是先存储在MemStore中，排好序后，等到达刷写时机才会刷写到HFile，每次刷写都会形成一个新的HFile。

WAL:write Ahead log,由于数据要经MemStore排序后才能刷写到HFile，但把数据保存在内存中会有很高的概率导致数据丢失，为了解决这个问题，数据会先写在一个叫做Write-Ahead logfile的文件中，然后再写入MemStore中。所以在系统出现故障的时候，数据可以通过这个日志文件重建。

BlockCache:读缓存，每次查询出的数据会缓存在BlockCache中，方便下次查询

## 写流程

- Client先向zookeeper获取meta表位于那个RegionServer
- 访问对应的RegionServer，获取meta表，根据请求查询出目标数据在哪个region。表将该表的region信息缓存在客户端
- 与目标region进行通讯
- 将数据写入WAL
- 将数据写入MemStore,数据会在MemStore排序
- 向客户端发送ack消息

## 写请求源码

- 尽可能获得多的锁
- 更新时间戳，如果没有指定添加Regionserver所在的时间的时间说
- 创建WAL edit对象
- 将最新的编辑信息追加到WAL对象中，暂不同步到磁盘
- 获取最新的mvcc版本号
- 写入memstore,只是写入，但是没有滚动mvcc，要等wal写完后才滚动。不滚动的话memstore中的数据是隐藏的。是无法scan到的
- 将WAL对象buffer中的数据同步到磁盘
- 移动MvCc
- 如果前面出现了异常，就将wal回滚。