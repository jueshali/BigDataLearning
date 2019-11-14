# HDFS

## 什么是HDFS

它是一个**分布式文件系统**，适合一次写入多次读出。

不支持文件的随机读写，支持对文件的追加。原因：HDFS在存储文件时，以块的形式存储。如果随机写入，为保证块的顺序，所有内容都要后移（类似于数组），第一块写入一个文件，后面所有块的内容都要后移，会造成大量的网络Io（在hadoop中是非常宝贵的资源）和磁盘Io。而追加的话，直接在最后一块上追加即可。

## HDFS文件的块

在Hadoop中一个文件被存放在多个块之中，每个块默认的大小为128M,如果一个文件不足128m，它同样占据一个块，如果超过128m它将被分割，除最后一个块以外，每一块的大小都是128m，最后一个块的大小小于128m。

### 为什么是128m

首先128m是一个人为定义的值，在Hadoop中可以通过修改hdfs.site.xml进行修改,应当设置为512byte的倍数。因为HDFS在传输时，每512bytes校验一次/

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
  
1. ：hadoop运行在廉价机器，太大，如果一个块在传输时出错，重传的代价太高
2. ：分块读取和下载时，块太大不适应分块读取，造成资源的浪费，例如读400M到600m时，要读一个G太浪费了。
3. ：map阶段一次默认处理一个块（一次处理一个块map的效率高），如果切片太大，map阶段的运行效率低。

- 过小

1. ：最直接的影响就是会增加NN的负担，NN中存储数据的元数据，过小的块会导致块的数量激增，NN需要处理的元数据信息过多，服役能力下降。元数据信息有（权限信息 Last Modified Replication Block SizeN ame -rw-r--r-- jueshali supergroup 273.81 MB tiem 备份数 文件大小 文件名），还有就是每一块在哪台机器上。
2. ：块太小，每次读取一个块都要一定的寻址时间，效率低。

- ps hadoop不适合存储小文件

1. ：小文件太多，NN受不了
2. ：小文件在读写时，性价比太低，量少信息量低。

## HDFS的Shell操作

hadoop fs + 命令

输入 hadoop fs会出现如下信息（命令）

```bash
Usage: hadoop fs [generic options]
    [-appendToFile <localsrc> ... <dst>]    \\向文件追加
    *[-cat [-ignoreCrc] <src> ...]   \\查看文件
    [-checksum <src> ...]       \\计算文件校验
    *[-chgrp [-R] GROUP PATH...] \\changeGroup
    [-chmod [-R] <MODE[,MODE]... | OCTALMODE> PATH...]  \\权限设置
    [-chown [-R] [OWNER][:[GROUP]] PATH...] \\所有者设置
    *[-copyFromLocal [-f] [-p] [-l] <localsrc> ... <dst>]\\从本地上传
    *[-copyToLocal [-p] [-ignoreCrc] [-crc] <src> ... <localdst>]\\从hdfs下载到本地
    [-count [-q] [-h] <path> ...]\\计数
    [-cp [-f] [-p | -p[topax]] <src> ... <dst>]\\复制
    [-createSnapshot <snapshotDir> [<snapshotName>]]\\ 对一个snapshottable目录创建snapshot,snapshot可以用于数据的backup，避免用户错误和灾难恢复
    [-deleteSnapshot <snapshotDir> <snapshotName>]\\ 删除快照
    [-df [-h] [<path> ...]]\\ 显示磁盘使用情况
    [-du [-s] [-h] <path> ...]\\ 显示目录或者文件的大小
    [-expunge]\\接受一个源目录和一个目标文件作为输入，并且将源目录中所有的文件连接成本地目标文件。？
    [-find <path> ... <expression> ...]\\
    [-get [-p] [-ignoreCrc] [-crc] <src> ... <localdst>]\\下载
    [-getfacl [-R] <path>]\\获取副本数
    [-getmerge [-nl] <src> <localdst>]\\下载后合并
    [-help [cmd ...]]\\帮助
    [-ls [-d] [-h] [-R] [<path> ...]]\\ 类似于ls
    [-mkdir [-p] <path> ...]\\创建文件夹
    [-moveFromLocal <localsrc> ... <dst>]\\移动
    [-moveToLocal <src> <localdst>]\\移动
    [-mv <src> ... <dst>]\\移动允许多个源路径
    [-put [-f] [-p] [-l] <localsrc> ... <dst>]\\上传
    [-renameSnapshot <snapshotDir> <oldName> <newName>]\\快照命名
    *[-rm [-f] [-r|-R] [-skipTrash] <src> ...]删除
    [-rmdir [--ignore-fail-on-non-empty] <dir> ...]\\删除文件夹
    *[-setfacl [-R] [{-b|-k} {-m|-x <acl_spec>} <path>]|[--set <acl_spec> <path>]]\\改变一个文件的副本系数。-R选项用于递归改变目录下所有文件的副本系数。
    [-setfattr {-n name [-v value] | -x name} <path>]\\
    [-setrep [-R] [-w] <rep> <path> ...]\\
    [-stat [format] <path> ...]\\返回指定路径的统计信息。
    [-tail [-f] <file>]\\将文件尾部1K字节的内容输出到stdout。支持-f选项，行为和Unix中一致。
    [-test -[defsz] <path>]\\检查文件的信息，-e，-z，-d
    [-text [-ignoreCrc] <src> ...]\\将源文件输出为文本格式。允许的格式是zip
    [-touchz <path> ...]\\创建一个0字节的空文件。
    [-truncate [-w] <length> <path> ...]\\
    [-usage [cmd ...]]\\
```

## HDFS的java程序进行操作

操作之前的配置

- windows本机要有Hadoop：安装在一个纯英文文件目录下，并设置环境变量，环境变量设置到/bin就ok了
- idea开启后要配置Maven和建立一个log4j.properties

```xml
<!--配置依赖-->
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>lpc.com</groupId>
    <artifactId>mapreduce</artifactId>
    <version>1.0-SNAPSHOT</version>
    <dependencies>
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>RELEASE</version>
        </dependency>
        <dependency>
            <groupId>org.apache.logging.log4j</groupId>
            <artifactId>log4j-core</artifactId>
            <version>2.8.2</version>
        </dependency>
        <dependency>
            <groupId>org.apache.hadoop</groupId>
            <artifactId>hadoop-common</artifactId>
            <version>2.7.2</version>
        </dependency>
        <dependency>
            <groupId>org.apache.hadoop</groupId>
            <artifactId>hadoop-client</artifactId>
            <version>2.7.2</version>
        </dependency>
        <dependency>
            <groupId>org.apache.hadoop</groupId>
            <artifactId>hadoop-hdfs</artifactId>
            <version>2.7.2</version>
        </dependency>
    </dependencies>

</project>
```

```properties
# 配置日志
log4j.rootLogger=INFO, stdout
log4j.appender.stdout=org.apache.log4j.ConsoleAppender
log4j.appender.stdout.layout=org.apache.log4j.PatternLayout
log4j.appender.stdout.layout.ConversionPattern=%d %p [%c] - %m%n
log4j.appender.logfile=org.apache.log4j.FileAppender
log4j.appender.logfile.File=target/spring.log
log4j.appender.logfile.layout=org.apache.log4j.PatternLayout
log4j.appender.logfile.layout.ConversionPattern=%d %p [%c] - %m%n
```

### HDFS初始操作和结束操作

在使用java程序对HDFS进行操作时，我们首先要创造一个FileSystem的对象，这个对象可以通过类方法.get()获得，输入的参数就是文件系统的配置信息，配置信息从配置文件或者代码中读取。在使用后作为一个文件系统需要对其进行关闭

```java
    @Before
    public void init() throws IOException {
        // 在创建时会读取配置文件，配置文件不只一个，读取顺序为（1）客户端代码中设置的值 >（2）ClassPath下的用户自定义配置文件 >（3）然后是服务器的默认配置
        Configuration conf = new Configuration();
        //这里设置fs.defaultFS的值为hdfs。后面的没设置就是默认的本地文件系统，
        conf.set("fs.defaultFS","hdfs://hadoop101:9000");
        fs =  FileSystem.get(conf);
        //建立一个本地的文件系统用于测试
        fsLocal  = FileSystem.get(new Configuration());
    }
```

```java
    @After
    public void close() throws Exception{
        IOUtils.closeStream(in);
        IOUtils.closeStream(out);
        if (fs!=null){
            fs.close();
        }

        if (fsLocal!=null){
            fsLocal.close();
        }
    }
```

### HDFS文件上传和下载

文件的上传下载实际上从代码上看比较容易，调用copyFromLocalFile 和CopytoLocalFile即可

```java
@Test
public void uploacalToHdfs() throws Exception{
        Path src = new Path("H:/testfile.txt");
        Path des = new Path("/hadoopday2/input");
        // 第一个false是：是否删除源文件，第二个ture为是否对目标文件进行覆盖
        fs.copyFromLocalFile(false, true,src,des);
    }

@Test
public void downloadFromHdfs() throws Exception{
    Path src = new Path("/hadoopday2/input");
    Path des = new Path("H:/input1");
    //第一个false为是否删除源文件，第二个false
    fs.copyToLocalFile(false,src,des,false);
    fs.copyToLocalFile();
}
```

### 通过流进行可操作的文件上传和下载

这一部分是通过自定义的流进行文件的上传和下载，在fs本身提供的方法也是通过流进行的操作,代码如下

```java
public static boolean copy(FileSystem srcFS, FileStatus srcStatus,
                             FileSystem dstFS, Path dst,
                             boolean deleteSource,
                             boolean overwrite,
                             Configuration conf) throws IOException {
    Path src = srcStatus.getPath();
    dst = checkDest(src.getName(), dstFS, dst, overwrite);
    if (srcStatus.isDirectory()) {
      checkDependencies(srcFS, src, dstFS, dst);
      if (!dstFS.mkdirs(dst)) {
        return false;
      }
      FileStatus contents[] = srcFS.listStatus(src);
      for (int i = 0; i < contents.length; i++) {
        copy(srcFS, contents[i], dstFS,
             new Path(dst, contents[i].getPath().getName()),
             deleteSource, overwrite, conf);
      }
    } else {
      InputStream in=null;
      OutputStream out = null;
      try {
        in = srcFS.open(src);
        out = dstFS.create(dst, overwrite);
        IOUtils.copyBytes(in, out, conf, true);
      } catch (IOException e) {
        IOUtils.closeStream(out);
        IOUtils.closeStream(in);
        throw e;
      }
    }
    if (deleteSource) {
      return srcFS.delete(src, true);
    } else {
      return true;
    }
  
  }
```

从这里可以看出来，核心代码是`IOUtils.copyBytes(in, out, conf, true);`这个IOUtils我自己还实现过，底层就是`in.read()和out.write()`，当然其中还有关闭，判断为空，使用shuffle,综合以上，我实现的自定以上传下载如下。

```java
   @Test
    public void myDownload() throws Exception{
        //流操作
        in = fs.open(new Path("/HDFS.txt"));
        out = fsLocal.create(new Path("H:/HDFSfromHDFS.txt"));
        byte [] buffer = new byte[1024];
        for (int i = 0; i < 10; i++) {
            System.out.println(i);
            in.read(buffer);
            out.write(buffer);
        }
    }

    /**
     * 用于测试一定大小的数据的上传
     * @throws Exception
     */
    @Test
    public void myUpload() throws Exception{
        in = fsLocal.open(new Path("H:/HDFS笔记.txt"));
        out = fs.create(new Path("/HDFS.txt"));

        //  IOUtils.copyBytes(in,out,2048);

        byte[] buffer =new byte[1024];
        for (int i = 0; i <3; i++) {
            in.read(buffer);
            out.write(buffer);
        }
    }
```

代码不难，指得注意的是open可以创建一个输入流，creare可以创建一个输出流。使用seek可以设置流的偏移量

### HDFS文件信息的查看

在HDFS中，文件的元数据包括文件长度，块大小，复本，修改时间，所有者和权限信息，这些信息封装在FileStatus对象之中。该对象可以通过`FileSystem.getFileStatus()`获得。获得一个目录下的所有文件使用`fs.listStatus(dir)`我的代码为打印两层目录，可以使用递归扩展

```java
 public void Status() throws Exception{
        // 查看文件的信息
        Path dir = new Path("/user/Administrator/jdk.gz");
        FileStatus fileStatus = fs.getFileStatus(dir);
        System.out.println("是否文件夹："+ fileStatus.isDirectory());
        System.out.println("长度是"+fileStatus.getLen());
        System.out.println("块大小是"+fileStatus.getBlockSize());
        //获取文件名
        System.out.println("文件名是"+fileStatus.getPath().getName());
    }

    @Test
    public void blockStatus() throws Exception{
        Path dir = new Path("/user/Administrator/jdk.gz");
        LocatedFileStatus status;
        //调用listLocatedStatus方法会返回一个迭代器，迭代器中的存的就是LocatedFileStatus对象；
        RemoteIterator<LocatedFileStatus> statuss =   fs.listLocatedStatus(dir);
        while (statuss.hasNext()){
            status = statuss.next();

            BlockLocation[] blockLocations = status.getBlockLocations();
            for (BlockLocation blockLocation:blockLocations) {
                System.out.println(blockLocation);
            }
            System.out.println(status.isDirectory()?"是目录":"不是目录");
        }
    }

      public void Statuss() throws Exception{
        Path dir = new Path("/hadoopday2");
        FileStatus[] fileStatusess = fs.listStatus(dir);
        for (FileStatus filestatus :
                fileStatusess) {
            System.out.println(filestatus.getPath().getName());
            if(filestatus.isDirectory()){
                Path nextDir = filestatus.getPath();
                FileStatus[] nextFileStatusess = fs.listStatus(nextDir);
                for (FileStatus nextFilestatus :
                        nextFileStatusess) {
                    System.out.println("----"+nextFilestatus.getPath().getName());
                }
            }
        }

    }

```

### HDFS块信息的处理

块和备份我认识HDFS文件系统中及其重要的一部分，通过listLocatedStatus可以获取block列表的迭代器，`BlockLocation`对象中主要存储块所在的位置，大小，偏移量等。

```java
    @Test
    public void blockStatus() throws Exception{
        Path dir = new Path("/user/Administrator/jdk.gz");
        LocatedFileStatus status;
        RemoteIterator<LocatedFileStatus> statuss =   fs.listLocatedStatus(dir);
        while (statuss.hasNext()){
            status = statuss.next();

            BlockLocation[] blockLocations = status.getBlockLocations();
            for (BlockLocation blockLocation:blockLocations) {
                System.out.println(blockLocation);
            }
            System.out.println(status.isDirectory()?"是目录":"不是目录");
        }
    }
```

### 练习，HDFS的分块下载

```java
 public void downWithBlock() throws Exception{
        Path src = new Path("jdk.gz");
        String basic = "H:/block";
        in = fs.open(src);
        byte[] buffer = new byte[1024];
        //获取块大小，
        long offset=  fs.getFileStatus(src).getBlockSize();
        //获取块数量
        int blockNum = fs.listLocatedStatus(src).next().getBlockLocations().length;

        System.out.println(offset + "" + blockNum);

        for (int i = 0; i < blockNum; i++) {
            in.seek(i*offset);
            Path des = new Path(basic+i);
            out = fsLocal.create(des);
            if (i==blockNum-1){
                IOUtils.copyBytes(in,out,1024);
            }else{
                for (int j = 0;j < (int)offset/1024;j++){
                    in.read(buffer);
                    out.write(buffer);
                }
            }
        }
    }
```
