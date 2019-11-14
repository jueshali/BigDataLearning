# HDFS

## 什么是HDFS

它是一个**分布式文件系统**，适合一次写入多次读出。

不支持文件的随机读写，支持对文件的追加。原因：HDFS在存储文件时，以块的形式存储。如果随机写入，为保证块的顺序，所有内容都要后移（类似于数组），第一块写入一个文件，后面所有块的内容都要后移，会造成大量的网络Io（在hadoop中是非常宝贵的资源）和磁盘Io。而追加的话，直接在最后一块上追加即可。

## HDFS文件的块

在Hadoop中一个文件被存放在多个块之中，每个块默认的大小为128M,如果一个文件不足128m，他同样占据一个块，如果超过128m它将被分割，除最后一个块以外，每一块的大小都是128m，最后一个块的大小小于128m。

### 为什么是128m

首先128m是一个人为定义的值，在Hadoop中可以通过修改hdfs.site.xml进行修改,考虑到校验机制，一定要是4bytes的倍数。

```xml
<property>
  <name>dfs.blocksize</name>
  <value>134217728</value>
  <description>
      The default block size for new files, in bytes.
      You can use the following suffix (case insensitive):
      k(kilo), m(mega), g(giga), t(tera), p(peta), e(exa) to specify the size (such as 128k, 512m, 1g, etc.),
      Or provide complete size in bytes (such as 134217728 for 128 MB).
  </description>
</property>
```

一个块是存储在磁盘上，因此在读写时，需要先寻址再读写。研究表明，一次最有性价比的传输是：寻址时间/传输时间=1%。寻址时间为10ms，因此传输时间控制在1s为好，在现有磁盘速度为100M/s时，设置128是最好的，如果磁盘读写速度达到500m，那可以设置512m;

但无论磁盘速度如何，块大小都不能过大或过小
  
- 过大
  
1. :hadoop运行在廉价机器，太大，如果一个块在传输时出错，重传的代价太高
2. ：
3. ：map阶段一次默认处理一个块（一次处理一个块map的效率高），如果切片太大，map阶段的运行效率低。

- 过小

1. ：最直接的影响就是会增加NN的负担，NN中存储数据的元数据，过小的块会导致块的数量激增，NN需要处理的元数据信息过多，服役能力下降。元数据信息有（权限信息 Last Modified Replication Block SizeN ame -rw-r--r-- jueshali supergroup 273.81 MB tiem 备份数 文件大小 文件名），还有就是每一块在那台机器上。
2. ：块太小，每次读取一个块都要一定的寻址时间，效率低。

- ps hadoop不适合存储小文件

1. ：小文件太多，NN受不了
2. ：小文件在读写时，性价比太低，量少信息量低。

## HDFS的Shell操作

hadoop fs + 命令

## HDFS的java程序进行操作

