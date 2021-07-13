package drillbit.distributed;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

public class ExclusiveWriter {
    public static void exclusiveWrite(String fileName, byte[] bytes) {
        FileChannel channel = null;
        FileLock lock = null;
        try {
            RandomAccessFile file = new RandomAccessFile(fileName, "rw");
            channel = file.getChannel();
            lock = channel.tryLock(0, Long.MAX_VALUE, false);

            ByteBuffer writeBuffer = ByteBuffer.allocate(bytes.length);
            writeBuffer.put(bytes, 0, bytes.length);
            writeBuffer.flip();
            while (writeBuffer.hasRemaining()) {
                channel.write(writeBuffer, 0);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                assert lock != null;
                lock.release();
                channel.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
