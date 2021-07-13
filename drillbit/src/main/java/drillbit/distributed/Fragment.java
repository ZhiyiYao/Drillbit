package drillbit.distributed;

import com.google.protobuf.InvalidProtocolBufferException;
import drillbit.protobuf.DistributedPb;

import java.io.*;


public class Fragment {
    private static final String DRILLBIT_LOCK = ".lock";
    private static final String DRILLBIT_DESCRIPTOR = ".descriptor";
    private static final String DRILLBIT_SERVER = ".server";
    private static final String DRILLBIT_SLAVE = ".slave";
    private static final String[] FILE_LIST = new String[]{DRILLBIT_LOCK, DRILLBIT_DESCRIPTOR, DRILLBIT_SERVER, DRILLBIT_SLAVE};

    FragmentDescriptor descriptor;
    Type type;

    enum Type {
        Server, Slave;
    }

    public Fragment(FragmentDescriptor descriptor) {
        this.descriptor = descriptor;
        this.type = null;
    }

    public void acquireType() {
        boolean exists = false;
        try {
            File file = new File(DRILLBIT_LOCK);
            exists = file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        type = exists ? Type.Server : Type.Slave;
    }

    private void updateDescriptorList() {
        ExclusiveUpdater.exclusiveUpdate(DRILLBIT_DESCRIPTOR, new DescriptorListUpdater(), descriptor);
    }

    public void register() {
        updateDescriptorList();

        if (type == null) {
            acquireType();
        }

        if (type == Type.Server) {
            writeServer();
        }
        else {
            updateSlaveList();
        }
    }

    private void updateSlaveList() {
        ExclusiveUpdater.exclusiveUpdate(DRILLBIT_SLAVE, new SlaveListUpdater(), descriptor.id);
    }

    private void writeServer() {
        ExclusiveWriter.exclusiveWrite(DRILLBIT_SERVER, new byte[] {(byte) descriptor.id});
    }

    public void clear() {
        for (String fileName : FILE_LIST) {
            File file = new File(fileName);
            if (file.exists()) {
                file.delete();
            }
        }
    }

    private static class DescriptorListUpdater implements ExclusiveUpdater.Updater {
        private final DistributedPb.Descriptors.Builder descriptorListBuilder;
        private final DistributedPb.Descriptors.Descriptor.Builder descriptorBuilder;

        public DescriptorListUpdater() {
            descriptorListBuilder = DistributedPb.Descriptors.newBuilder();
            descriptorBuilder = DistributedPb.Descriptors.Descriptor.newBuilder();
        }

        @Override
        public void write(byte[] bytes) {
            try {
                descriptorListBuilder.mergeFrom(bytes);
            }
            catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            }
        }

        @Override
        public byte[] read() {
            descriptorListBuilder.addDescriptor(descriptorBuilder.build());
            return descriptorListBuilder.build().toByteArray();
        }

        @Override
        public void update(Object o) {
            FragmentDescriptor descriptor = (FragmentDescriptor) o;

            descriptorBuilder.clear();
            descriptorBuilder.setId(descriptor.id);
            descriptorBuilder.setFile(descriptor.file);
            descriptorBuilder.setCount(descriptor.count);
        }
    }

    private static class SlaveListUpdater implements ExclusiveUpdater.Updater {
        private final DistributedPb.Slaves.Builder slaveListBuilder;

        public SlaveListUpdater() {
            slaveListBuilder = DistributedPb.Slaves.newBuilder();
        }

        @Override
        public void write(byte[] bytes) {
            try {
                slaveListBuilder.mergeFrom(bytes);
            }
            catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            }
        }

        @Override
        public byte[] read() {
            return slaveListBuilder.build().toByteArray();
        }

        @Override
        public void update(Object o) {
            slaveListBuilder.addId((Integer) o);
        }
    }
}
