package drillbit.dmlmanager;

import drillbit.utils.common.DoubleAccumulator;

import java.util.concurrent.ConcurrentHashMap;

public interface SynData {
    void addData(SynData data);

    ConcurrentHashMap<Object, DoubleAccumulator> getAccumulation(String label);

    byte[] getBytes();

    void fromBytes(byte[] data);

    void clear();
}
