# Cli参数介绍

## 启动时参数

- --define：为cli提供一个变量，例如 a = person; 在cli客户端使用时就可以使用${a}卸下伪装。
- --database：指定初始化连接哪个库
- -e：使用客户段执行一条sql语句
- -f：多条sql，可以编写在一个文件中，文件中的sql会按照顺序执行！
- hiveconf  在hive cli启动时，定义一组配置相关的属性！这个属性会覆盖hive-site.xml和hive-default.xml中同名的属性！
- -i 启动hive时，先执行指定文件中的sql，不退出cli
- -S,--silent 静默模式，只显示结果集

## 客户端命令

- dfs执行hadoopfs命令
- cat查看变量
- hive中输入的所有历史命令都会记录在.hivehistory中。hive退出时记录

## hive配置

加入表头和使用的库名称

```xml
<property>
    <name>hive.cli.print.header</name>
    <value>true</value>
</property>

<property>
    <name>hive.cli.print.current.db</name>
    <value>true</value>
</property>
```

修改某人仓库

```xml
<property>
<name>hive.metastore.warehouse.dir</name>
<value>/user/hive/warehouse</value>
<description>location of default database for the warehouse</description>
</property>
```
