package ssdut.training.mapreduce.inputformat;

import java.io.IOException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.FixedLengthInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class FixedLengthInput {
	public static class FixedLengthInputMapper extends Mapper<LongWritable, BytesWritable, LongWritable, BytesWritable> {		
		public void map(LongWritable key, BytesWritable value, Context context ) 
				throws IOException, InterruptedException {
			context.write(key, value);
	    }
	}
  
	public static class FixedLengthInputReducer extends Reducer<LongWritable,BytesWritable,LongWritable,BytesWritable> {
		public void reduce(LongWritable key, Iterable<BytesWritable> values, Context context) 
				throws IOException, InterruptedException {
			for (BytesWritable val : values) {
				context.write(key, val);
			}
	    }
	}

	public static void main(String[] args) throws Exception {		
		//1.设置HDFS配置信息
		String namenode_ip = "192.168.17.10";
		String hdfs = "hdfs://" + namenode_ip + ":9000";			
		Configuration conf = new Configuration();
		conf.set("fs.defaultFS", hdfs);
		conf.set("mapreduce.app-submission.cross-platform", "true");
		conf.setInt(FixedLengthInputFormat.FIXED_RECORD_LENGTH, 13);
		
		//2.设置MapReduce作业配置信息
		String jobName = "FixedLengthInput";					//作业名称
		Job job = Job.getInstance(conf, jobName);
		job.setJarByClass(FixedLengthInput.class);				//指定运行时作业类
		job.setJar("export\\FixedLengthInput.jar");				//指定本地jar包
		job.setMapperClass(FixedLengthInputMapper.class);		//指定Mapper类
		job.setMapOutputKeyClass(LongWritable.class);			//设置Mapper输出Key类型
		job.setMapOutputValueClass(BytesWritable.class);		//设置Mapper输出Value类型
		job.setReducerClass(FixedLengthInputReducer.class);		//指定Reducer类
		job.setOutputKeyClass(LongWritable.class);				//设置Reduce输出Key类型
		job.setOutputValueClass(BytesWritable.class); 			//设置Reduce输出Value类型
		
		job.setInputFormatClass(FixedLengthInputFormat.class);	//设置输入格式化类
		
		//3.设置作业输入和输出路径
		String dataDir = "/expr/fixedinput/data";			//实验数据目录	
		String outputDir = "/expr/fixedinput/output";		//实验输出目录
		Path inPath = new Path(hdfs + dataDir);
		Path outPath = new Path(hdfs + outputDir);
		FileInputFormat.addInputPath(job, inPath);
		FileOutputFormat.setOutputPath(job, outPath);
		FileSystem fs = FileSystem.get(conf);
		if(fs.exists(outPath)) {
			fs.delete(outPath, true);
		}
		
		//4.运行作业
		System.out.println("Job: " + jobName + " is running...");
		if(job.waitForCompletion(true)) {
			System.out.println("success!");
			System.exit(0);
		} else {
			System.out.println("failed!");
			System.exit(1);
		}
	}

}