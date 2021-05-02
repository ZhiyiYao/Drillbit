package drillbit.test;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import java.io.IOException;

public class TestFileIO {
    public static void main(String[] args) throws IOException {
//        testHDFS();
    }

    public static void testHDFS() throws IOException {
//        Configuration conf = new Configuration();
//        conf.set("fs.defaultFS", "hdfs://localhost:8047");
//        FileSystem fs = null;
//        try {
//            fs = FileSystem.getLocal(conf);
//        }
//        catch (IOException e) {
//            e.printStackTrace();
//            throw e;
//        }
//        Path path = new Path("hdfs://localhost:8047/.tmp/temp.data");
//        FSDataOutputStream out = fs.create(path);
//        for (int i = 0; i < 100; i++) {
//            out.write("hello".getBytes());
//        }
//        out.close();
//
//        FSDataInputStream in = fs.open(path);
//        byte[] b = new byte[20];
//        in.read(b);
    }
}
