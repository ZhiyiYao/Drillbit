package drillbit.neighbors.distance;

import java.util.concurrent.ConcurrentHashMap;

public class EuclideanDistance extends Distance {
    public EuclideanDistance(ConcurrentHashMap<String, String> options) {
        super(options);
    }

    @Override
    public double evaluate(double[] vec1, double[] vec2) {

        double squaredDistance = 0;

        for (int i = 0; i < Math.min(vec1.length, vec2.length); i++) {
            double temp = vec1[i] - vec2[i];
            squaredDistance += temp * temp;
        }

        return Math.sqrt(squaredDistance);
    }
}
