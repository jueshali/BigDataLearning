# HBase的刷写,合并和分割

## 刷写

Hbase中，刷写指的是将MemStore中的数据写入到StoreFile中，每一次刷写都会在StoreFile中生成一个新的StoreFile文件。Memstore存在的主要作用是在将数据写入Hbase中时使得数据有序。刷写分为主动刷写和被动刷写。主动刷写在shell客户端调用`flush`命令即可。而被动刷写在以下三种情形下会发生：

1. 当某个memstore的大小达到了hbase.hregion.memstore.flush.size（默认值128M），其所在region的所有memstore都会刷写。原因是同一个region中的不同store存储的是一个表中的不同的列簇，因此一条记录很有可能在多个store中都会存储，因此一个store存满了，可以认为其他的store也会存储。
2. 当region server中memstore的总大小达到java_heapsize*hbase.regionserver.global.memstore.size（默认值0.4）*hbase.regionserver.global.memstore.size.lower.limit（默认值0.95），region会按照其所有memstore的大小顺序（由大到小）依次进行刷写。直到region server中所有memstore的总大小减小到上述值以下。不要让堆满了
3. 到达自动刷写的时间，也会触发memstore flush。自动刷新的时间间隔由该属性进行配置hbase.regionserver.optionalcacheflushinterval（默认1小时）。
4. 当WAL文件的数量超过hbase.regionserver.max.logs，region会按照时间顺序依次进行刷写。

## StoreFile Compaction

由于Hbase依赖于HDFS进行存储，而HDFS只支持追加写。所以在Hbase中的增，删，改在实际上都是一个put操作，在HDFS上新增了一个记录（刷写后）。新增的记录有着与之前不同的版本号（时间戳）。在经过不断的CUD.HDFS上的小文件数量不断增加。这是不符合HDFS的存储理念的。解决这一问题的途径是对小文件进行一次合并操作。HBase提供了两种Compaction。major compaction和minor compaction

Minor Compaction会将临近的若干个较小的HFile合并成一个较大的HFile，但不会清理过期和删除的数据。Major Compaction会将一个Store下的所有的HFile合并成一个大HFile，并且会清理掉过期和删除的数据。

由于每次刷写都会生成一个文件，因此多个Hfile中有不同版本的数据是非常正常的事情，因此，在进行Compaction时，需要遍历所有的文件，因此需要经常的进行合并，减少majorCompation的数量。


## Region Split

默认情况下，每张表只有一个分区，随着数据的不断写入，Region会自动进行拆分，刚拆分时，两个子Region都位于当前的Region Server,但为了实现负载均衡，之后Hbase会将某个Region转移给其他的RegionServer.

- 获取切分的初始化阀值initialSize ，默认为 2*128M（memstore达到flush的限制）
- 计算当前rs中，某个表所拥有的region数,tableRegions
- 0<= tableRegions < 100 时，region切分的阀值：  2*128M*tableRegions*tableRegions*tableRegions 和 10G对比，取最小值！tableRegions > =100，默认10G为切分阀值！
