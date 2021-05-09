package drillbit.neighbors.utils;

public class Counter {
    private final String label;
    private double count;

    public Counter(String label) {
        this.label = label;
        this.count = 0;
    }

    public double getCount() {
        return count;
    }

    public String getLabel() {
        return label;
    }

    public Counter incr(double d) {
        count += d;
        return this;
    }

    public Counter incr() {
        count += 1;
        return this;
    }
}
