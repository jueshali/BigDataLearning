# Hiveb表的DDL

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

#### external

指定表是外部表还是管理表。默认创建的表是管理表，管理表在被删除时，Hive也会将这个表中的数据删除。而外部表在被删除时，删除该表并不会删除这个表中的数据，但是描述表的元数据信息会被删除。外部表和内部表可以通过`alter table table_name set tblproperties('EXTERNAL'='FALSE');`进行修改。

#### PARTITIONED

分区实际上对应HDFS文件系统上的独立文件夹，该文件夹下是该分区所有的数据文件。Hive中的分区就是分目录，把一个大的数据集根据需要分割成小的数据集。这样做的好处是，mapreduce在进行操作使用到where语句时，map可以从where指定的文件夹下读取数据，而不需要读取所有数据后再处理。

#### CLUSTERED 和 SORTED



用于分桶。

### 删

### 改

### 查
