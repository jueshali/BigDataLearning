# Hadoop优化

map端数据倾斜，输入文件有不可分割的压缩包
reduce端数据倾斜：分区后某个区的数据过多

## 输入

1. 在执行MR之前。提前将小文件合并，压缩成可切片的格式
2. 使用CombineTextInputFormat

## map阶段

3. 减少溢写次数：通过调整缓冲区的大小及sort.spill.percent（溢写阈值）参数值，增大触发spill的内存上限、
4. 减少合并次数：调整io.sort.factor，一次merge合并的文件数。增大merge的文件数，减少Merge的次数
5. 在不影响业务逻辑的前提下，先进行combine处理，减少IO

## Reduce阶段

6. 设置map与Reduce共存。减少Reduce的等待时间

## IO问题

7. 采用数据压缩的方式，减少网络IO的时间。安装snappy编码器
8. 使用sequenceFile二进制文件

## 数据倾斜问题


9. map端的数据倾斜，控制切片，提前处理好。
10. Reduce端的数据倾斜，通过抽样调查，查看样本分布，将可能有大量数据的分区可以再进一步分区
11. 如果小文件过多，可以开启JVM重用.

## 常用调优参数

配置部分调优参数，例如mapreduce.reduce.memory.mb,mapreduce.map.memory.mb,yarn-site中的参数。





