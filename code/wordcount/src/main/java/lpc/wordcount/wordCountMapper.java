package lpc.wordcount;

import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.MRJobConfig;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

/**
 * 项目名：wordcount
 * 描述：wordcount的mapper程序
 *
 * @author : Lpc
 * @date : 2019-11-16 13:38
 **/
public class wordCountMapper extends Mapper<LongWritable,Text,Text,IntWritable> {

    private Text outKey = new Text();
    private IntWritable outValue = new IntWritable(1);
    

    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException{
        String[] words = value.toString().split(" ");
        for (String word : words) {
            outKey.set(word);
            context.write(outKey,outValue);
        }

    }
}
