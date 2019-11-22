# Hadoop调优相关

针对MR的优化，主要是针对MR在运行期间频繁的磁盘IO和网络IO的优化为主题，进行优化！

1. 使用压缩
2. 条件允许，可以使用Combiner
3. 增大MapTask中缓冲区的大小和溢写的阀值
4. 增大合并时一次性合并的片段数
5. 调大ReduceTask端shuffle进程使用的内存比例
6. 开启MapTask和ReduceTask共存

针对小文件的优化，围绕将多个小文件合并，节省NN中的内存使用

1. 从源头解决，在上传时，提前将多个小文件打包，归档
2. 如果小文件已经在HDFS上，使用hadoop archieve进行归档
3. 在运行MR时，可以使用CombineTextInputFormat将多个小文件规划到一个切片
