package drillbit.neighbors.utils;

public class Counter {
    private final String label;
    private int count;

    public Counter(String label) {
        this.label = label;
        this.count = 0;
    }

    public int getCount() {
        return count;
    }

    public String getLabel() {
        return label;
    }

    public Counter incr() {
        count++;
        return this;
    }
}
