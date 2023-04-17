package drillbit.neighbors.distance;

import java.util.concurrent.ConcurrentHashMap;

public class CosineDistance extends Distance {
    public CosineDistance(ConcurrentHashMap<String, String> options) {
        super(options);
    }

    @Override
    public double evaluate(double[] vec1, double[] vec2) {
        double product = 0;
        double norm1 = 0;
        double norm2 = 0;

        for (int i = 0; i < Math.min(vec1.length, vec2.length); i++) {
            product += vec1[i] * vec2[i];
            norm1 += vec1[i] * vec1[i];
            norm2 += vec2[i] * vec2[i];
        }

        return product / (Math.sqrt(norm1)) * (Math.sqrt(norm2));
    }
}
