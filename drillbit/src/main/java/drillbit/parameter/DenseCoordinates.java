package drillbit.parameter;

import com.google.protobuf.InvalidProtocolBufferException;
import drillbit.protobuf.ParameterPb;
import drillbit.utils.math.Vector;

import javax.annotation.Nullable;
import java.util.ArrayList;

public class DenseCoordinates extends Coordinates {
    ArrayList<double[]> coordinates;

    public DenseCoordinates(int ndim) {
        super(StorageType.Dense);

        this.sz = 0;
        this.ndim = ndim;
        coordinates = new ArrayList<>();
    }

    @Override
    public void add(Vector coordinate) {
        set(sz, coordinate);
    }

    @Override
    public void add(double[] coordinate) {
        set(sz, coordinate);
    }

    @Override
    public void set(int row, int col, double value) {
        if (-1 < row && row < sz) {
            double[] coordinate = coordinates.get(row);
            if (-1 < col && col < ndim) {
                coordinate[col] = value;
                coordinates.set(row, coordinate);
            }
            else {
                throw new IllegalArgumentException();
            }
        }
        else if (row >= sz) {
            for (int i = sz; i < 2 * sz; i++) {
                coordinates.add(new double[ndim]);
            }
            set(row, col, value);
        }
        else {
            throw new IllegalArgumentException("");
        }
        sz = coordinates.size();
    }

    @Override
    public void set(int row, Vector coordinate) {
        set(row, coordinate.toArray());
    }

    @Override
    public void set(int row, double[] coordinate) {
        if (-1 < row && row < sz) {
            coordinates.set(row, coordinate);
        }
        else if (row >= sz) {
            for (int i = sz; i < row; i++) {
                coordinates.add(new double[ndim]);
            }
            coordinates.add(coordinate);
        }
        else {
            throw new IllegalArgumentException("");
        }
        sz = coordinates.size();
    }

    @Override
    public double get(int row, int col) {
        if (-1 < row && row < sz && -1 < col && col < ndim) {
            return coordinates.get(row)[col];
        }
        else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public double[] get(int row) {
        if (-1 < row && row < sz) {
            return coordinates.get(row);
        }
        else {
            throw new IllegalArgumentException();
        }
    }

    @Nullable
    @Override
    public byte[] toByteArray() {
        ParameterPb.DenseCoordinates.Builder builder = ParameterPb.DenseCoordinates.newBuilder();
        for (int i = 0; i < sz; i++) {
            ParameterPb.DenseCoordinates.Row.Builder rowBuilder = ParameterPb.DenseCoordinates.Row.newBuilder();
            double[] row = coordinates.get(i);

            for (int j = 0; j < ndim; j++) {
                rowBuilder.addElement(row[j]);
            }
            builder.addCoordinate(rowBuilder.build());
        }

        return builder.build().toByteArray();
    }

    @Override
    public Coordinates fromByteArray(byte[] byteArray) throws InvalidProtocolBufferException {
        ParameterPb.DenseCoordinates denseCoordinates;
        try {
            denseCoordinates = ParameterPb.DenseCoordinates.parseFrom(byteArray);
        }
        catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
            throw e;
        }

        sz = denseCoordinates.getCoordinateCount();
        ndim = denseCoordinates.getCoordinate(0).getElementCount();
        coordinates.clear();
        for (int i = 0; i < sz; i++) {
            double[] coordinate = new double[ndim];
            ParameterPb.DenseCoordinates.Row row = denseCoordinates.getCoordinate(i);
            for (int j = 0; j < ndim; j++) {
                coordinate[j] = row.getElement(j);
            }
            coordinates.add(coordinate);
        }

        return this;
    }
}
