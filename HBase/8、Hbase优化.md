# HBase优化

## 高可用

高可用就是设置master的备份，数据的备份由Hdfs保持了~master挂掉，不影响hbase数据的增删改查。配置的方法就是在conf下创建backup-masters文件，并且写入host就可以了

## 预分区

良好的分区可以实现数据的负载均衡。与hive不同，hbase的分区需要预先确定。每个region维护了一个startRow和endRowKey.如果加入的数据符合某个region维护的rowkey范围，则该数据交给这个region维护。因此我们可以将数据所要投放的分区预先规划后，使得之后的数据能比较均匀的分布在各个region中。指定分区的方法如下

- `create 'table1','info','partition1',SPLITS => ['1000','2000','3000','4000']`
- `create 'table2','info','partition2',{NUMREGIONS => 15, SPLITALGO => 'HexStringSplit'}`
- `create 'table3','partition3',SPLITS_FILE => 'splits.txt'` splits.txt中存了类似 ['1000','2000','3000','4000']
- javaapi创建。

## RowKey设计

在设定好分区后，需要良好的RowKey才能更好的实现负载均衡

## 内存优化

HBase操作过程中需要大量的内存开销，毕竟Table是可以缓存在内存中的，一般会分配整个可用内存的70%给HBase的Java堆。但是不建议分配非常大的堆内存，因为GC过程持续太久会导致RegionServer处于长期不可用状态，一般16~48G内存就可以了，如果因为框架占用内存过高导致系统内存不足，框架一样会被系统服务拖死。

## 基础优化

1. 开启压缩`mapreduce.map.output.compress，mapreduce.map.output.compress.codec`
2. 设置RPC监听数量
3. 优化HStore文件大小,如果单个region文件过大，在map阶段时会减少该任务的执行时间。因此可以尝试减小单个Region的大小HBase.hregion.max.filesize
4. 优化Hbase客户端缓存。`HBase.client.write.buffer`
5. 优化DataNode允许的最大文件打开数`dfs.datanode.max.transfer.threads`

