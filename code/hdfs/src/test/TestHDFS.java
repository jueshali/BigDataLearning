package com.atguigu.hdfs.test;

import static org.junit.Assert.*;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.BlockLocation;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.RemoteIterator;
import org.apache.hadoop.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/*
 * 1. 使用Java程序完成对HDFS的操作
 * ①创建目录                 hadoop fs   -mkdir 路径
 * ②上传文件
 * ③下载文件
 * ④查询文件的属性信息
 * ⑤判断文件是否是目录
 * 
 * 2. FileSystem: 这是一个文件系统客户端的抽象基类
 * 		两种实现：  LocalFileSystem： 本地文件系统
 * 				DistributedFileSystem： 分布式文件系统
 * 
 *      具体哪种实现取决于fs.defaultFS参数
 *      
 * 3. Configuration: Configuration代表当前Hadoop的配置对象！
 * 			所有配置文件中编写的name-value信息，都会读入Configuration对象中！
 * 
 * 4. FileStatus: 文件的状态(属性)信息
 * 		通过Path.getName()获取文件名！
 * 		
 * 5. LocatedFileStatus
 * 			包含文件所有块的信息及块在哪些DN上可用！
 * 				getBlockLocations()： 获取所有块的信息！
 * 			
 * 	    
 */
public class TestHDFS {
	
	private Configuration conf;
	
	private FileSystem fs;
	
	@Before
	public void init() throws Exception {
		
		conf=new Configuration();
		
		//conf.set("fs.defaultFS", "hdfs://hadoop101:9000");
		
				// 创建一个文件系统客户端对象
				//FileSystem fs=FileSystem.get(conf);
		 fs=FileSystem.get(new URI("hdfs://hadoop101:9000"),conf,"jack");
	}
	
	@After
	public void close() throws Exception {
		
		if (fs !=null) {
			fs.close();
		}
		
	}
	
	@Test
	public void testMkdir() throws Exception {
		
		fs.mkdirs(new Path("/eclipse1"));

		
	}
	
	@Test
	public void testUpLoad() throws Exception {
		
		fs.copyFromLocalFile(false, true, new Path("e:/上午笔记.txt"), new Path("/"));
		
	}
	
	@Test
	public void testDownload() throws Exception {
		
		fs.copyToLocalFile(false, new Path("/上午笔记.txt"), new Path("e:/"), true);
		
	}
	
	@Test
	public void ifADir() throws Exception {
		
		//Instead reuse the FileStatus returned by getFileStatus() or listStatus() methods.
		//System.out.println(fs.isDirectory(new Path("/hadoopdata")));
		FileStatus fileStatus = fs.getFileStatus(new Path("/上午笔记.txt"));
		
		//path=hdfs://hadoop101:9000/上午笔记.txt
		System.out.println(fileStatus.isDirectory() ? "是目录" : "不是目录！" );
		
		System.out.println("文件名:"+fileStatus.getPath().getName());
		
	}
	
	@Test
	public void listFileStatus() throws Exception {
		
		//FileStatus[] listStatus = fs.listStatus(new Path("/hadoopdata"));
		
		RemoteIterator<LocatedFileStatus> statuss = fs.listLocatedStatus(new Path("/eclipse-jee-mars-2-linux-gtk-x86_64.tar.gz"));
		
		while(statuss.hasNext()) {
			
			LocatedFileStatus status = statuss.next();
			
			System.out.println(status.isDirectory() ? "是目录" : "不是目录！" );
			
			System.out.println("文件名:"+status.getPath().getName());
			
			BlockLocation[] blockLocations = status.getBlockLocations();
			
			for (BlockLocation blockLocation : blockLocations) {
				
				System.out.println("--------------一块-----------");
				
				System.out.println(blockLocation);
				
			}
			
			
		}
	}
	
	//定制化上传：  只上传文件的一部分！
	/*
	 *  InputStream in=null;
      OutputStream out = null;
      try {
      
        in = srcFS.open(src);  // 获取目标路径的输入流
        out = dstFS.create(dst, overwrite);  //获取目标路径的输出流
        IOUtils.copyBytes(in, out, conf, true);
        
        数据的上传和下载，使用两个文件系统分别根据上传和下载获取输入和输出流，完成流中数据的拷贝即可
	 */
	@Test
	public void testCustomUpload() throws Exception {
		
		Path src = new Path("e:/上午笔记.txt");
		
		// 获取输入和输出流
		FileSystem srcFS=FileSystem.get(new Configuration());
				
		FSDataInputStream in = srcFS.open(src);
		FSDataOutputStream out = fs.create(new Path("/file"), true);
		
		// 流的数据拷贝      buffSize不能超过4096
		IOUtils.copyBytes(in, out, 4096, true);
		
		
	}
	
	@Test
	public void testCustomPartUpload() throws Exception {
		
		Path src = new Path("e:/复习.txt");
		
		// 获取输入和输出流
		FileSystem srcFS=FileSystem.get(new Configuration());
				
		FSDataInputStream in = srcFS.open(src);
	
		FSDataOutputStream out = fs.create(new Path("/file1"), true);
		
		// 手动控制  传2k
		//IOUtils.copyBytes(in, out, 4096, true);
		byte [] buffer=new byte[1024];
		
		for (int i = 0; i < 2; i++) {
			
			in.read(buffer);
			out.write(buffer);
			
		}
		
		//关流
		IOUtils.closeStream(in);
		IOUtils.closeStream(out);
		
		
	}
	
	// 自定义下载  下载第N个块
	@Test
	public void testCustomPartDownload() throws Exception {
		
		Path src = new Path("/eclipse-jee-mars-2-linux-gtk-x86_64.tar.gz");
		
		// 获取输入和输出流
		FileSystem localFS=FileSystem.get(new Configuration());
				
		 FSDataOutputStream out = localFS.create(new Path("e:/block5"), true);
		
		 FSDataInputStream in = fs.open(src);
		 
		 byte [] buffer=new byte[1024];
		 
		 // 将输入流的起始位置定位到指定偏移量
		 in.seek(67108864*4);
			
		/*for (int i = 0; i < 1024 * 64; i++) {
				
				in.read(buffer);
				out.write(buffer);
				
		}
			
			//关流
		IOUtils.closeStream(in);
		IOUtils.closeStream(out);*/
		 IOUtils.copyBytes(in, out, 4096, true);
		 
		
	}
	
	

	

}
