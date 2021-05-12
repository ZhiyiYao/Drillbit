package drillbit.parameter;

import com.google.protobuf.InvalidProtocolBufferException;
import drillbit.utils.math.Vector;

import javax.annotation.Nullable;

public abstract class Coordinates {
    StorageType storageType;
    int sz, ndim;

    public Coordinates(StorageType storageType) {
        this.storageType = storageType;
    }

    enum StorageType {
        Dense, Sparse
    }

    public int nDim() {
        return ndim;
    }

    public int size() {
        return sz;
    }

    public abstract void add(Vector coordinate);

    public abstract void add(double[] coordinate);

    public abstract void set(int row, int col, double value);

    public abstract void set(int row, Vector coordinate);

    public abstract void set(int row, double[] coordinate);

    public abstract double get(int row, int col);

    public abstract double[] get(int row);

    @Nullable
    public abstract byte[] toByteArray();

    public abstract Coordinates fromByteArray(byte[] byteArray) throws InvalidProtocolBufferException;
}
