package drillbit.neighbors.distance;

import java.util.concurrent.ConcurrentHashMap;

public class ChebyshevDistance extends Distance {
    public ChebyshevDistance(ConcurrentHashMap<String, String> options) {
        super(options);
    }

    @Override
    public double evaluate(double[] vec1, double[] vec2) {
        double distance = 0;

        for (int i = 0; i < Math.min(vec1.length, vec2.length); i++) {
            distance = Math.max(distance, Math.abs(vec1[i] - vec2[i]));
        }

        return distance;
    }
}
