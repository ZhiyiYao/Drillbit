package drillbit.utils.math;

import drillbit.utils.parser.ObjectParser;

import javax.annotation.Nonnull;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;

public class SparseVector implements Vector {
    private final ConcurrentHashMap<Integer, Double> values;

    public SparseVector() {
        values = new ConcurrentHashMap<>();
    }

    public SparseVector(@Nonnull double... values) {
        this.values = new ConcurrentHashMap<>();
        for (int i = 0; i < values.length; i++) {
            this.values.put(i, values[i]);
        }
    }

    @Override
    public double get(int index) {
        return values.get(index);
    }

    @Override
    public double get(int index, double defaultValue) {
        return values.getOrDefault(index, defaultValue);
    }

    @Override
    public void set(int index, double value) {
        values.put(index, value);
    }

    @Override
    public void incr(int index, double delta) {
        values.put(index, values.get(index) + delta);
    }

    @Override
    public void decr(int index, double delta) {
        values.put(index, values.get(index) - delta);
    }

    @Override
    public void each(@Nonnull final VectorOperation operation) {
        Enumeration<Integer> keys = values.keys();
        int index;
        while (keys.hasMoreElements()) {
            index = keys.nextElement();
            operation.apply(index, values.get(index));
        }
    }

    @Override
    public int size() {
        return values.size();
    }

    @Override
    public void clear() {
        values.clear();
    }

    @Nonnull
    @Override
    public double[] toArray() {
        Object[] valueArray = values.values().toArray();
        double[] result = new double[valueArray.length];
        for (int i = 0; i < valueArray.length; i++) {
            result[i] = ObjectParser.parseDouble(valueArray[i]);
        }
        return result;
    }
}
