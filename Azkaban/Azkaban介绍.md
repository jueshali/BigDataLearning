# Azkaban介绍

Azkaban是一个批量工作流任务调度器，主要用于在一个工作流内以一个特定的顺序运行一组工作和流程，它的配置是通过简单的key:value对的方式，通过配置中的Dependencies 来设置依赖关系。

## Azkaban的组成

Azkaban可以自定义插件，一般而言，Azkaban由三部分组成。
- AzkabanWebServer是整个Azkaban工作流系统的主要管理者，它负责用户登录认证、project管理、定时执行工作流、跟踪工作流执行进度等一系列任务。
- 负责具体的工作流的提交、执行，它们通过mysql数据库来协调任务的执行。
- 关系型数据库（MySQL）：存储大部分执行流状态，AzkabanWebServer和AzkabanExecutorServer都需要访问数据库

## Azkaban安装

1. 将以上三个组件解压
2. 数据库的配置，启动数据库，并创建一个用户和azkaban表，use azkaban，然后使用source "../create-all-sql***.sql"创建azkaban运行时需要的所有数据表
3. web配置，主要配置azkaan.properties,配置全部使用绝对路径。`web.resource.dir` ,`default.timezone.id`, `user.manager.xml.file`，`executor.global.properties`, `mysql.host`, `mysql.user`,`mysql.password`,`jetty.**`
4. 配置jetty时需要自己生成keystore文件,最后在`azkaban-users.xml`之中添加一个用户，配置权限
5. Executor配置，修改`azkaban.properties`。只要是配置sql相关的配置。将`executor.global.properties`配置为全路径

## Azkaban的使用

编写job文件

```job
#step1.job
type=command        //命令类型。可以是javaprocess和command
dependencies=start  //依赖，此处意为需要执行start后才能执行step1
command=start-hdfs.sh //具体的命令
```

```
#Java.job
type=javaprocess
java.class=azkaban.JavaJob
classpath=lib/*
```

将Job打包为zip文件上传到指定的页面，点吧点吧就能使用了。