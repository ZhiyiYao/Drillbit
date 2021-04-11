package drillbit.utils.learner;

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
        return (float) (value / count);
    }

}

