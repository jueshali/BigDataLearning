# ReduceTask分析

## Copy

从多个MapTask处拉取同一个区的所有文件.

## 归并排序

将这些文件归并排序(之前的在MapTask中已经排好序了)

## Reducer一次读取一组

一次读取一组的方法是调用GroupingComparator(Key,KeyNext);如果返回0则认为Key和KeyNext为一组,

默认的GroupingCompartor为排序用的Comparator,但是在许多需要对数据分组的情况下,需要额外定义一个GroupingCompartor.定义时需要两步.

- 继承WritableComparator使其不重复

```java
  public class GroupCompare extends WritableComparator {
    //WritableComparator




    @Override
    public int compare(WritableComparable a, WritableComparable b) {
        OrderBean o1 = (OrderBean) a;
        OrderBean o2 = (OrderBean) b;

        return o1.getOrderId().compareTo(o2.getOrderId());
    }
}
 ```

- 在Driver中编写代码配置`job.setSortComparatorClass(GroupCompare.class);`

## 输出格式

### TextOutPutFormat

默认的输出格式为TextOutputFormat,它把每条记录写为文本,调用Key和value的`toString`方法写出.key value直接隔一个`\t`.

### 自定义OutPutFormat

自定义其实也不是特别难,就是当调用一次context.write就相当于调用MyRecorderWriter()一次.

```java
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 项目名：wordcount
 * 描述：自定义RecorderWriter
 *
 * @author : Lpc
 * @date : 2019-11-19 21:04
 **/
public class MyRecorderWriter  extends RecordWriter<String, NullWritable> {
    private Path baiduPath;
    private Path otherPath;

    private OutputStream aos;
    private OutputStream bos;

    private FileSystem fs;

    public MyRecorderWriter(TaskAttemptContext job) throws IOException{
        Configuration conf = job.getConfiguration();
        fs = FileSystem.get(conf);
        baiduPath = new Path("H:/a.log");
        otherPath = new Path("H:/b.log");

        aos = fs.create(baiduPath);
        bos = fs.create(otherPath);
    }

    @Override
    public void close(TaskAttemptContext context) throws IOException, InterruptedException {
        IOUtils.closeStream(aos);
        IOUtils.closeStream(bos);
        fs.close();
    }

    @Override
    public void write(String key, NullWritable value) throws IOException, InterruptedException {
        if (key.contains("baidu")){
            aos.write(key.getBytes());
            aos.write("/n".getBytes());
        }else {
            bos.write(key.getBytes());
            bos.write("/n".getBytes());
        }
    }
}

```
