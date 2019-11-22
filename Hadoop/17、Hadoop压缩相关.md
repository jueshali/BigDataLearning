# Hadoop压缩

## 概述

压缩的实质是用时间换空间。Hadoop中在处理大数据时，会遇到I/O和网络传输资源不够的情况，此时可以通过压缩的方法通过增加计算减少IO。
因此是否使用压缩遵循运算密集，不用压缩，IO密集使用压缩，当然，除了压缩以外combiner也能减少磁盘IO;
Hadoop框架在运行过程中，能够自动识别文件的扩展名，判断文件采用的压缩格式，自动压缩和解压缩，在有些压缩格式下，采用压缩后文件的处理和不采用压缩的文件的处理逻辑一致。

## MR支持的压缩编码

- DEFLATE，Hadoop自带，不可切分，换成压缩格式后的处理方式和普通文本的处理方式一致
- Gzip,Hadoop自带，不可切分，换成压缩格式后的处理方式和普通文本的处理方式一致
- bzip2,Hadoop自带，并且可以切分，换成压缩格式后的处理方式和普通文本的处理方式一致，压缩解压缩比较慢，压缩效率高
- LZO,hadoop不自带，需要安装，可以切分，但是需要建立索引，指定输入格式
- Snappy,Hadoop不自带，不可以切分，但是压缩解压缩速度极快。

使用场景，不支持切分的应当使压缩后文件的大小不超过一个切片为好。Bzip适用于文件极大时,Lzo同样适用于文件极大的情况。Snappy适用于作为一个mapreduce的输出和另一个MapReduce的输入。

## 可以压缩的三个位置

1. 输入端：直接对将要输入的文件压缩。
2. Mapper输出端：当数据量过大造成网络传输过多，就可以在Mapper输出端指定压缩技术
3. Reduce输出端：Reduce端压缩就是为了减少磁盘空间，同时如果有下一个Mapper的话，可以提高效率。

这三个压缩位置可以在Driver中进行配置。

```java
// 开启map端输出压缩
configuration.setBoolean("mapreduce.map.output.compress", true);
// 设置map端输出压缩方式
configuration.setClass("mapreduce.map.output.compress.codec", BZip2Codec.class, CompressionCodec.class);
```

```java
// 设置reduce端输出压缩开启
FileOutputFormat.setCompressOutput(job, true);
// 设置压缩的方式
FileOutputFormat.setOutputCompressorClass(job, BZip2Codec.class); 
```
