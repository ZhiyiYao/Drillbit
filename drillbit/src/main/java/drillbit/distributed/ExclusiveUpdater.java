package drillbit.distributed;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

public class ExclusiveUpdater {
    public static void exclusiveUpdate(String fileName, Updater updater, Object o) {
        FileChannel channel = null;
        FileLock lock = null;
        try {
            RandomAccessFile file = new RandomAccessFile(fileName, "rw");
            channel = file.getChannel();
            lock = channel.tryLock(0, Long.MAX_VALUE, false);

            byte[] readBytes = new byte[(int) channel.size()];
            ByteBuffer readBuffer = ByteBuffer.wrap(readBytes);

            int readSize = channel.read(readBuffer);
            if (readSize > 0) {
                updater.write(readBytes);
            }

            updater.update(o);

            byte[] writeBytes = updater.read();
            ByteBuffer writeBuffer = ByteBuffer.allocate(writeBytes.length);
            writeBuffer.put(writeBytes, 0, writeBytes.length);
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

    public interface Updater {
        void write(byte[] bytes);

        byte[] read();

        void update(Object o);
    }
}
