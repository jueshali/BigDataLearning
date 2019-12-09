# Hbase和MR

通过HBase的相关JavaAPI，我们可以实现伴随HBase操作的MapReduce过程，如使用MapReduce将数据从本地文件系统导入到HBase之中。或者使用MapReduce对Hbase中的一些表进行分析。

## 准备

1. 首先要配置好hadoop和Hbase的环境变量
2. 在hadoop-env.sh中在for循环后加上`export HADOOP_CLASSPATH=$HADOOP_CLASSPATH:/opt/module/HBase/lib/*`，是将Hbase中的jar包放入hadoop的运行环境之中

## 使用mapreduce复制student表

1. 创建student表
2. 建立mapper,Hbase提供了读取Hbase中数据的tableMapper.
3. 建立Reducer,只要提供好输入输出格式就行了

Mapper

```java
package cn;

import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableMapper;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;
import java.io.StringWriter;

/**
 * 项目名：hbaseMR
 * 描述：自定义mapper。
 *
 * @author : Lpc
 * @date : 2019-12-09 18:00
 *
 * 总目标：从hbase中的student表读入数据，通过MR写入Hbase的student_copy表之中
 *
 * 自定义Hbase的mapper
 *
 * map--reduce
 * InputFormat:Hbase提供了TableInputFormat作为HBase数据源的输入格式，默认每个region切一片
 * Mapper：定义输入的key-value,map(),输出的key-value
 *
 * Mapper：如果从HBase中读取数据，一般需要继承TableMapper，定义输入的Key-value。
 * key：ImmutableBytesWritable
 * value:Result(单行数据的value)
 *
 *
 **/
public class HbaseMapper extends TableMapper<Text, Put> {


    @Override
    protected void map(ImmutableBytesWritable key, Result value, Context context) throws IOException, InterruptedException {
        //获取一行中所有的单元格
        Put out_value = new Put(key.get());
        Text out_key = new Text();
        Cell[] cells = value.rawCells();

        for (Cell cell : cells) {
            if ("age".equals(Bytes.toString(CellUtil.cloneQualifier(cell))) &&"info".equals(Bytes.toString(CellUtil.cloneFamily(cell)))){
                out_value.add(cell);
            }

        }
        out_key.set(key.toString());
        context.write(out_key,out_value);
    }
}
```

啥也不要写，没有什么重要的操作

Reducer

```java
package cn;

import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.mapreduce.TableReducer;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;

/**
 * 项目名：hbaseMR
 * 描述：自定义Reducer
 *
 * @author : Lpc
 * @date : 2019-12-09 18:26
 **/
public class HbaseReducer  extends TableReducer<Text, Put, NullWritable> {
}

```


Driver

```java
package cn;

import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.hbase.client.Connection;
import java.io.IOException;

/**
 * 项目名：hbaseMR
 * 描述：自定义Driver
 *
 * @author : Lpc
 * @date : 2019-12-09 18:28
 **/
public class HbaseDriver  {

    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {


        //创建表
        Connection connection = ConnectionFactory.createConnection();

        Admin admin = connection.getAdmin();

        HTableDescriptor student3 = new HTableDescriptor(TableName.valueOf("student3"));

        student3.addFamily(new HColumnDescriptor("cf1"));
        admin.createTable(student3);


        Configuration entries = HBaseConfiguration.create();

        Job job = Job.getInstance();
        job.setJobName("job1");

        Scan scan = new Scan();

        //设置输入输出目录
         TableMapReduceUtil.initTableMapperJob(Bytes.toBytes("student"),scan,HbaseMapper.class, Text.class,Put.class,job);

        TableMapReduceUtil.initTableReducerJob("student3", HbaseReducer.class,job);
        if(admin!=null){admin.close();}
        
        if(connenction!=null){connection.close();}
        

        //运行
        job.waitForCompletion(true);
    }

}
```