# Hive介绍

## What is hive

The Apache Hive ™ data warehouse software facilitates reading, writing, and managing large datasets residing in distributed storage using SQL. Structure can be projected onto data already in storage（基于数据建表）. A command line tool and JDBC driver are provided to connect users to Hive.

基于Hadoop用使用SQL分析在分布式系统上存储的结构化的数据的数据仓库软件，Hive通过要分析的数据建表。注意：

1. Hive不是关系型数据库
2. Hive不适用于OLTP,是OLAP
3. Hive不是实时查询和行级别更新的语言
4. 处理在HDFS上存储的数据\
5. 由于Hive处理的数据存储在HDFS上，HDFS不支持随机写，因此Hive默认不支持update操作，常用于查询
6. hive是在线分析处理软件，重视分析，一般很少提供写的功能，也基本没有事务。
7. hive的本质就是MR程序

## Hive的架构原理

![hive.jpg](Hive架构.png)

用户通过CLI和JDBC对数据进行操作。之后都进入Driver，通过解析，编译，优化和执行后将SQL语句转化为MapReduce语句执行。SQL处理时通过MetaStore读取数据的存储位置，各个数据表之间的个关系等等。

## Hive和关系性数据库的区别

1. 查询语言：Hive使用了类SQL的语言。
2. 数据存储的位置：所有 Hive 的数据都是存储在 HDFS 中的。而数据库则可以将数据保存在块设备或者本地文件系统中。在Hive中通过关系型数据库存储了元数据
3. 数据更新：而数据仓库的内容是读多写少的。因此，Hive中不建议对数据的改写，所有的数据都是在加载的时候确定好的。
4. 索引：索引的原理不一样。一个用于检索，一个用于过滤
5. 执行延迟：大数据Hive要低。
6. 执行方式：Hive中大多数查询的执行是通过 Hadoop 提供的 MapReduce 来实现的。而数据库通常有自己的执行引擎。
7. 可扩展性：Hive可以基于Hadoop进行扩展。
8. 数据规模：。。。

## Hive的数据

Hive的库就是HDFS上的一个目录！默认使用default库，default对应的目录/user/hive/warehouse。\
Hive的表就是库中的一个子目录！\
Hive中的数据，必须存储在指定的表目录中，以文件的形式存在！\
数据必须是结构化的数据，在创建表的时候，表需要根据数据格式来创建！\
直接上传数据也可以被识别。\
在Hive中可以指定数据的分割符，默认使用^A,在建表时可以使用 row format delimited fields terminated by ','\

## 元数据的存储

在hive中库，表的元数据是存储在关系型数据库之中，默认使用derby作为元数据存储的数据库。其有以下特点

- derby大小不超过1m，可以作为项目的内嵌数据库。
- hive在hive命令启动的目录会创建或者读取metastore_db作为元数据的存储目录。如果两次打开的目录不同，hive的表信息等数据也完全不同。
- derby不支持多个derby示例使用同一个库

为解决以上问题可以将hive的

将元数据的存储位置修改到在mysql中存储！
步骤：

1. ①安装mysql
2. ②修改配置，在配置文件中配置mysql作为元数据的存储
3. ③将mysql的jdbc驱动，拷贝到hive的lib目录下
4. ④在mysql中创建一个库，这个库用来存放hive产生的元数据
5. ⑤给hive提供一个有权限访问此库的用户

metastore库中

- dbs：数据库的信息
- tbs：数据表的信息
- columns_v2: 存放的字段信息
- partitions：分区信息
