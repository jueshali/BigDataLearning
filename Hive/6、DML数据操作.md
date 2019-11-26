# DML数据操作

## 数据导入

在数据导入之前首先需要通过create语句创建表.

### **load**

```SQL
load data [local] inpath "path"  overwrite|into table table_name [partition(col="value")];
```

local表示从本地文件系统中上传数据（类似 dfs -put）到表所在的目录，如果没有该参数，那表示是从HDFS中移动数据到表所在的目录（类似 dfs -mv）.

overwrite into和 into是二选一的参数，overwrite into表示覆盖写，之前所有的数据将被删除，而into表示追加写入。注hive(hdfs)不支持随机写

### **insert**

insert的就是插入记录,要通过mapreduce程序插入,除了可以删除固定的数据,还可以插入从其他表查询到的数据.

- 插入一条数据`insert into table table_name values(col1val,col2val)`;
- 通过查询插入数据`insert into table student select 语句`
- 多插入模式

```sql
from 表1
insert table 表2 select *
insert table 表3 select *;
```

### as select

`create table table_name as select * from 表2`,直接根据查询的结果创建表.

### import

 IMPORT [[EXTERNAL] TABLE new_or_original_tablename [PARTITION (part_column="value"[, ...])]]
  FROM 'source_path'
  [LOCATION 'import_target_path']

- 在导入时，如果目标表不存在，自动创建目标表或分区
- 如果导入的表已经存在，此时会执行检查，检查目标表和导出的表的元数据是否匹配
- 目标表和导入的表的metastore信息必须一致！
- 目标表存在，还没有添加分区，那么必须为空
- 目标表存在，且是分区表，那么导入的分区必须不存在

总结： 要么不创建目标表，让hive自动帮你创建！如果目标表已经存在，必须保证表的结构和要导入的表的结构一致！且要导入的分区，不能存在！

## 数据导出

### insert导出

insert overwrite local directory "path" select查询语句;
同样的没有overwrite就是将数据导入到hdfs上.

### Hadoop命令导出到本地

直接用cli客户端dfs命令将目录下的数据get到本地,hive shell命令也可以

### Export导出到HDFS上

`export table table_name to '/user';`

1. export可以将表或分区的数据和元数据一并导出到HDFS的目录中！
1. 这个目录可以被移动到不同的HDFS或不同的Hive中，再使用import命令将导出的表导入到hive中！
1. 如果是分区表，支持只导出部分分区！导入和导出和使用的元数据存储的RDMS(数据库)无关！
