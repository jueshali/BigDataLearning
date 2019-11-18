package lpc.wordcount;


import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;


/**
 * 项目名：wordcount
 * 描述：自定义的driver文件
 *
 * @author : Lpc
 * @date : 2019-11-16 14:35
 **/
public class wordCountDriver {
    public static void main (String[] args) throws Exception{
        Configuration conf = new Configuration();
        conf.set("fs.defaultFS","hdfs://hadoop101:9000");

        FileSystem fs = FileSystem.get(conf);

        Path inputPath = new Path("/HDFS.txt");
        Path outputPath = new Path("/output1");

        if(fs.exists(outputPath)){
            fs.delete(outputPath,true);
        }
        // 1创建Job
        Job job = Job.getInstance(conf,"wc");


        // 2配置job
        // 如果是本地模式，不需要调用此方法，如果再yarn上运行，需要调用
        // 高所YARN,job中执行的class文件所在的jar包再哪里。
        //job.setJarByClass(wordCountDriver.class);
        // 指定jar包的名称
        // job.setJar("myWC.jar");
        //指定目标jar包是哪个类所在的jar包
        //job.setJarByClass(wordCountDriver.class);
        //设置其他参数，job的输入目录，输出目录，

        job.setMapperClass(wordCountMapper.class);
        job.setReducerClass(wordCountReducer.class);

        FileInputFormat.setInputPaths(job,inputPath);
        FileOutputFormat.setOutputPath(job,outputPath);
        job.setNumReduceTasks(3);
        //如果涉及序列化，
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(IntWritable.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);

        job.waitForCompletion(true);

    }
}
