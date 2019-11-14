/**
 * 项目名：mapreduce
 * 描述：使用java完成对HDFS的操作
 *
 * @author : Lpc
 * @date : 2019-11-13 14:36
 **/

import org.apache.hadoop.conf.Configuration;

import org.apache.hadoop.fs.*;
import org.apache.hadoop.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class TestHDFS {


    private FileSystem fs;
    private FSDataInputStream in =null;
    private FSDataOutputStream out = null;
    FileSystem fsLocal;
    public static void main(String[] args) {

    }


    @Before
    public void init() throws IOException {
        Configuration conf = new Configuration();
        conf.set("fs.defaultFS","hdfs://hadoop101:9000");

        fs =  FileSystem.get(conf);
        //建立一个本地的文件系统用于测试
        fsLocal  = FileSystem.get(new Configuration());

    }


    @Test
    public void testMkdir() throws Exception{

        fs.mkdirs(new Path("/hadoopday2/newinput"));
    }

    @Test
    public void uploadToHdfs() throws Exception{
        Path src = new Path("H:/复习.txt");
        Path des = new Path("/hadoopday2/input");

        fs.copyFromLocalFile(false, true,src,des);
    }

    @Test
    public void downloadFromHdfs() throws Exception{
        Path src = new Path("/hadoopday2/input");
        Path des = new Path("H:/input1");
        fs.copyToLocalFile(false,src,des,false);
    }
//    public void

    @Test
    public void deleteDir() throws Exception{
        Path dir = new Path("/hadoopday2/newinput");
        fs.delete(dir,false);

    }

    @Test
    public void changeName() throws Exception{
        Path dir = new Path("/jdk-8u121-linux-x64.tar.gz");

        fs.rename(dir,new Path("jdk.gz"));
    }

    @Test
    public void fileStatus() throws Exception{
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

    @Test
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

    @Test
    public void downWithBlock() throws Exception{
        Path src = new Path("jdk.gz");
        String basic = "H:/block";
        in = fs.open(src);
        byte[] buffer = new byte[1024];
        long offset=  fs.getFileStatus(src).getBlockSize();
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


}
