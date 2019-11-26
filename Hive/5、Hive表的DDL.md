# Hive表的DDL

## 对表的操作

### 增

```Sql
CREATE [TEMPORARY] [EXTERNAL] TABLE [IF NOT EXISTS] [db_name.]table_name    -- (Note: TEMPORARY available in Hive 0.14.0 and later)
  [(col_name data_type [column_constraint_specification] [COMMENT col_comment], ... [constraint_specification])]
  [COMMENT table_comment]
  [PARTITIONED BY (col_name data_type [COMMENT col_comment], ...)]
  [CLUSTERED BY (col_name, col_name, ...) [SORTED BY (col_name [ASC|DESC], ...)] INTO num_buckets BUCKETS]
  [SKEWED BY (col_name, col_name, ...)                  -- (Note: Available in Hive 0.10.0 and later)]
     ON ((col_value, col_value, ...), (col_value, col_value, ...), ...)
     [STORED AS DIRECTORIES]
  [
   [ROW FORMAT row_format]
   [STORED AS file_format]
     | STORED BY 'storage.handler.class.name' [WITH SERDEPROPERTIES (...)]  -- (Note: Available in Hive 0.6.0 and later)
  ]
  [LOCATION hdfs_path]
  [TBLPROPERTIES (property_name=property_value, ...)]   -- (Note: Available in Hive 0.6.0 and later)
  [AS select_statement];   -- (Note: Available in Hive 0.5.0 and later; not supported for external tables)
```

#### EXTERNAL

指定表是外部表还是管理表。默认创建的表是管理表，管理表在被删除时，Hive也会将这个表中的数据删除。而外部表在被删除时，删除该表并不会删除这个表中的数据，但是描述表的元数据信息会被删除。外部表和内部表可以通过`alter table table_name set tblproperties('EXTERNAL'='FALSE'|'TRUE');`进行修改。

#### PARTITIONED

分区实际上对应HDFS文件系统上的独立文件夹，该文件夹下是该分区所有的数据文件。Hive中的分区就是分目录，把一个大的数据集根据需要分割成小的数据集。这样做的好处是，mapreduce在进行操作使用到where语句时，map可以从where指定的文件夹下读取数据，而不需要读取所有数据后再处理。

##### 分区的增删改查

- 增 `alter table table_name add partition(Column_name="value");`
- 删 `alter table table_name drop partition(Column_name="value");`
- 改 分区没必要改，没有价值
- 查 `show partitions dept_partition`

##### 添加数据到指定分区

在hive中可以有多级分区，在多级分区的情况下，hive的数据文件一定要放在最里层的分区目录下，否则hive将无法读取数据。同时如果用户直接上传数据到分区表中，可以通过以下三种操作创建分区之间的连接

- 上传数据后修复`msck repair table table_name;`
- 上传数据后添加分区；例如上传数据到/a分区，之后再通过`alter table table_name add partition(Column_name="value");`进行添加。
- 创建文件夹后load到分区。直接`load data [local] inpath "path" into table table_name partition(Column_name="value")`。load之后数据就分好区了。`value`既可以是已经存在分区，也可以新建。

#### CLUSTERED BY和 SORTED BY

用于聚簇，其原理和MR中的分区一致，将表中的数据分散到多个文件之中。默认使用Hash分区。使用分桶的意义在于抽样查询，实际多个文件中的数据分桶随机分就OK了。

- 分区的命令为`create table table_name (id int,name string) clustered by(id) sorted by (name desc) into 4 buckets row format delimited files terminated by "";`
- 分桶指定的列名应该再前面要创建
- 为将数据分桶，读入数据时通过mapreduce读入才可以，也就是说需要运行insert语句最佳。
- 再cli客户端或者配置文件中须设置`hive.enforce.bucketing=true`,`hive.enforce.sort=true;`
- 抽样查询：`select * from stu_buck tablesample(bucket X out of Y on 分桶字段)`从第X可分区开始按照Y个为一个间镉进行抽样查询，其中Y必须时总桶数的因子或者倍数。

### 删

truncate table 表名：表必须是管理表。该命令时清空表中的数据，而只有清空管理表中的数据才有意义。
 drop table 表名：删除表，如果是管理表，数据也会被删除，如果是外部表，只会删除表结构。

### 改

- 更新列名字
`ALTER TABLE table_name CHANGE [COLUMN] col_old_name col_new_name column_type [COMMENT col_comment] [FIRST|AFTER column_name]`
- 增加和替换列
`ALTER TABLE table_name ADD|REPLACE COLUMNS (col_name data_type [COMMENT col_comment])` 使用REPLACE_COLUMNS会将原本所有的列删除。

### 查

- 查询表结构`desc table_name`;

### 其他增

create table 表名 like 表名; //参照后面这个表创建一个新表，里面是没有数据的。\
create table 表名 as select 查询语句 ;//将后面表的查询出的数据用于创建表。只是数据而已。像分割符之类的数据都没有.
