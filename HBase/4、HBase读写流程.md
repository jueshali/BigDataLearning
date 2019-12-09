# HBase读写流程

## HBase写流程

1. 客户端连接配置文件中指定的zookeeper，请求获取meta表所在的RegionServer
2. zookeeper回复客户端zookeeper所需要的RegionServer表，
3. 客户端访问对于的RegionServer，获取meta表，根据读请求的rowkey，获取目标数据位于哪个RegionServer所在的Region中。如果是第一次访问，会将该tabl的Region信息和Meta表的位置信息缓存。
4. 与目标Region Server通讯
5. 将数据顺序写入到WAL之中
6. 将数据写入对应的Memstore之中，数据将在MemStore排序
7. 向客户端发送ack请求
8. 等达到MemStore的刷写时机之后，会将数据刷写到HFile之中。

在第五步和第六步之中，事情的发展不仅仅只是先写入WAL再写入MemStore，其中存在mvcc的滚动和事务操作。

- 尽可能获得多的锁
- 更新时间戳，如果没有指定添加Regionserver所在的时间的时间戳
- 创建WAL edit对象
- 将最新的编辑信息追加到WAL对象中，暂不同步到磁盘
- 获取最新的mvcc版本号
- 写入memstore,只是写入，但是没有滚动mvcc，要等wal写完后才滚动。不滚动的话memstore中的数据是隐藏的。是无法scan到的
- 将WAL对象buffer中的数据同步到磁盘
- 移动MvCc
- 如果前面出现了异常，就将wal回滚。

## HBase读流程

1. 客户端连接配置文件中指定的zookeeper，请求获取meta表所在的RegionServer
2. zookeeper回复客户端zookeeper所需要的RegionServer表，
3. 客户端访问对于的RegionServer，获取meta表，根据读请求的rowkey，获取目标数据位于哪个RegionServer所在的Region中。如果是第一次访问，会将该tabl的Region信息和Meta表的位置信息缓存。
4. 与目标Region Server通讯，发起get请求 
5. 在请求regionServer后，找到要查询的region，找到对象region的多个store对象进行查询
6. 在查询时，每个store会初始化两种scanner
   - memstoreScanner:负责扫描当前store中的memstore
   - 多个storeFileScanner，负责扫描当前store已经生成的所有storefile.
   - 之后将同一行，同一列下的多个cell中取出，timestamp最大的cell返回。
7. 在返回时，在命中的storefile中，取一个block(64K)存储到blockchche之中
8. 每个RegionServer有一个blockcache对象，默认大小为当前rs所在堆的40%，默认采用LRU算法进行内存的回收。
    - 如果下次查询，根据rowkey和region命中了blockcache中已经缓存的block,不会再扫描storefile.而是直接读取memstore和bloack cache进行合并。


