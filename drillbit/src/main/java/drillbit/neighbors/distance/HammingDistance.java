package drillbit.neighbors.distance;

import java.util.concurrent.ConcurrentHashMap;

public class HammingDistance extends Distance {
    public HammingDistance(ConcurrentHashMap<String, String> options) {
        super(options);
    }

    @Override
    public double evaluate(double[] vec1, double[] vec2) {
        long result = 0;

        for (int i = 0; i < Math.min(vec1.length, vec2.length); i++) {
            result += Long.bitCount(((long) vec1[i]) ^ ((long) vec2[i]));
        }

        return (double) result;
    }
}
