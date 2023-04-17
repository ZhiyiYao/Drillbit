package drillbit.test;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Inter process communication in Java using memory mapped file
 *
 * @author WINDOWS 8
 */

public class TestProcess {

    public static void main(String args[]) throws IOException, InterruptedException {

        RandomAccessFile rd = new RandomAccessFile("mapped.txt", "rw");

        FileChannel fc = rd.getChannel();
        MappedByteBuffer mem = fc.map(FileChannel.MapMode.READ_WRITE, 0, 1000);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (int i = 1; i < 20; i++) {
            mem.put((byte) i);
            System.out.println("Process 1 : " + (byte) i);
            Thread.sleep(1); // time to allow CPU cache refreshed
        }
    }
}