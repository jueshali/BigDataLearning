# Hive的DDL

## 对库的操作

    库式HDFS上的一个目录，默认使用default，每创建一个库，默认在/user/hive/warehose/库名.db的目录，在hql中不区分大小写，除了属性名以外。

### 增

create database [if not exists] databsename\
[COMMENT database_comment]  // 库的注释\
[LOCATION hdfs_path]  //库在hdfs上存放的位置，如果不指定默认存放/user/hive/warehouse\
[WITH DBPROPERTIES (property_name=property_value, ...)];//定义库中的一些属性

### 删

drop database 库名。只能删除空库\
drop database cascade:把表一起删除\

### 改

只能修改一些属性信息。Location, dbproperties, owner\
alter database name set dbproperties(""="");\
修改时同名的属性会覆盖，不同名的新增

### 查

show databases：查看所有的库\
desc database name:查看库的信息\
desc database extended name:查看库的详细信息\
use 库名：使用库名
