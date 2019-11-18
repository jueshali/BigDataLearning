package lpc.wordcount;

import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

/**
 * 项目名：wordcount
 * 描述：wordCount的Reduce的实现
 *
 * @author : Lpc
 * @date : 2019-11-16 14:00
 **/

public class wordCountReducer extends Reducer<Text,IntWritable,Text,IntWritable> {

    private int sum;

    @Override
    protected void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
        sum = 0;
        for (IntWritable value : values) {
            sum=sum+1;
        }
        context.write(key,new IntWritable(sum));
    }
}
