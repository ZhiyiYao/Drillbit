package drillbit.utils.math;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.Arrays;

public class DenseVector implements Vector {
    @Nonnull
    private final double[] values;
    private final int size;

    public DenseVector() {
        super();
        values = new double[32];
        size = 32;
    }

    public DenseVector(@Nonnull double... values) {
        super();
        this.values = values;
        size = values.length;
    }

    public DenseVector(@Nonnegative int size) {
        super();
        values = new double[size];
        this.size = size;
    }

    private void checkIndex(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException(String.format("Index %d is out of bound, should be in [0, %d]", index, size - 1));
        }
    }

    @Override
    public double get(int index) {
        checkIndex(index);

        return values[index];
    }

    @Override
    public double get(int index, double defaultValue) {
        try {
            checkIndex(index);
        } catch (IndexOutOfBoundsException e) {
            if (index >= size) {
                return defaultValue;
            }
            throw e;
        }

        return values[index];
    }

    @Override
    public void set(int index, double value) {
        checkIndex(index);

        values[index] = value;
    }

    @Override
    public void incr(int index, double delta) {
        checkIndex(index);

        values[index] += delta;
    }

    @Override
    public void decr(int index, double delta) {
        checkIndex(index);

        values[index] -= delta;
    }

    @Override
    public void each(@Nonnull final VectorOperation procedure) {
        for (int i = 0; i < values.length; i++) {
            procedure.apply(i, values[i]);
        }
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void clear() {
        Arrays.fill(values, 0.d);
    }

    @Override
    public double[] toArray() {
        return values;
    }
}
