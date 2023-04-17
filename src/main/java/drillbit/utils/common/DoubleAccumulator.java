package drillbit.utils.common;

public final class DoubleAccumulator {

    private double value;

    // always be greater than 0
    private int count;

    public DoubleAccumulator(double v) {
        this.value = v;
        this.count = 1;
    }

    public void add(double v) {
        this.value += v;
        this.count++;
    }

    public double get() {
        assert (count >= 1) : count;
        return (value / (double) count);
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}

