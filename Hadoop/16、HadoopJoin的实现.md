# HadoopJoin的实现

Join操作可以将两个或多个文件组合起来，组合的两张表之间的某些属性具有相关关系。在MapReduce中，可以通过MapJoin和ReduceJoin实现类似Join的操作。

## ReduceJoin

ReduceJoin的Join操作发生在Reduce端，而Map只需实现文件的读取。

```java
package lpc.reduceJoin1;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

import java.io.IOException;

/**
 * 项目名：wordcount
 * 描述：ReduceJoin的Mapper实现,在Mapper阶段只实现将数据读入到Bean之中，由于连接的两个表的属性不同，所以建立的Bean需要有两个表的属性
 *
 * @author : Lpc
 * @date : 2019-11-20 09:30
 **/
public class JoinMapper extends Mapper<LongWritable, Text,JoinBean, NullWritable> {

    private JoinBean keyOut = new JoinBean();
    private String filename;

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        FileSplit fileSplit = (FileSplit) context.getInputSplit();
        filename = fileSplit.getPath().getName();

    }

    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {

        keyOut.setSource(filename);
        String[] words = value.toString().split("\t");
        if(filename.equals("order.txt")){
            keyOut.setOrderId(words[0]);
            keyOut.setPid(words[1]);
            keyOut.setAmount(words[2]);
            keyOut.setPname("nodata");
        }else {
            keyOut.setPid(words[0]);
            keyOut.setPname(words[1]);
            keyOut.setOrderId("nodata");
            keyOut.setAmount("nodata");
        }

        context.write(keyOut,NullWritable.get());
    }
}


import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 项目名：wordcount
 * 描述：ReduceJoin的Reducer实现
 *
 * @author : Lpc
 * @date : 2019-11-20 09:30
 **/
public class JoinReducer extends Reducer<JoinBean, NullWritable,JoinBean,NullWritable> {

    private List<JoinBean> list = new ArrayList<JoinBean>();
    private Map<String,String> pidDatas = new HashMap<String, String>();
    String fileName;

    //在setup阶段获取filenam;建立一个数组
    @Override
    protected void setup(Context context) throws IOException, InterruptedException {

    }

    @Override
    protected void reduce(JoinBean key, Iterable<NullWritable> values, Context context) throws IOException, InterruptedException {

        for (NullWritable value : values) {
            if (key.getSource().equals("order.txt")){
                JoinBean joinBean = new JoinBean();
                joinBean.setOrderId(key.getOrderId());
                joinBean.setAmount(key.getAmount());
                joinBean.setPid(key.getPid());
                joinBean.setPname(key.getPname());
                joinBean.setSource(key.getSource());
                list.add(joinBean);
            }else {
                pidDatas.put(key.getPid(),key.getPname());
            }
        }

    }

    @Override
    protected void cleanup(Context context) throws IOException, InterruptedException {
        for (JoinBean joinBean : list) {
            joinBean.setPname(pidDatas.get(joinBean.getPid()));
            context.write(joinBean,NullWritable.get());
        }
    }

}
```

## MapJoin

在上面的ReduceJoin中需要占用过大的资源，是不是有更好的方法，用的资源更少？有！MapJoin,都不需要Reduce阶段，但是怎么让Map一次读两个文件呢？通过HDFS手动读， 

```java
//hadoop提供了分布式缓存的方法，可以将一个文件写到缓存中，当有Task需要时就可以下载这个缓存的文件，并且只会下载一次，非常高效。
//使用这个缓存需要提前上传这个文件并且让这个文件在所有机器可读，还可以缓存归档文件
//在Driver中编写
job.addCacheFile(uri);

```java
package lpc.MapperJoin;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

import java.io.*;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * 项目名：wordcount
 * 描述：ReduceJoin的Mapper实现
 *
 * @author : Lpc
 * @date : 2019-11-20 09:30
 **/
public class JoinMapper extends Mapper<LongWritable, Text,NullWritable ,JoinBean > {

    private JoinBean outValue = new JoinBean();
    private Map<String,String> pdDatas = new HashMap<String, String>();

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        URI[] cacheFiles = context.getCacheFiles();

        for (URI cacheFile : cacheFiles) {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(cacheFile)), "utf-8"));
            String line= null;
            while (!StringUtils.isEmpty(line=bufferedReader.readLine())){
                String[] split = line.split("\t");
                pdDatas.put(split[0],split[1]);

            }
        }
    }

    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
        //从cache中读取内容
        String[] split = value.toString().split("\t");
        outValue.setOrderId(split[0]);
        outValue.setAmount(split[2]);
        outValue.setPname(pdDatas.get(split[1]));
        context.write(NullWritable.get(),outValue);
    }
}
```
