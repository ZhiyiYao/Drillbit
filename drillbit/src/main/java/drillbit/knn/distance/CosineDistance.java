package drillbit.knn.distance;

import java.util.ArrayList;

public class CosineDistance implements Distance {
    @Override
    public double evaluate(double[] vec1, double[] vec2) {
        double product = 0;
        double norm1 = 0;
        double norm2 = 0;

        for (int i = 0; i < Math.min(vec1.length, vec2.length); i++) {
            product += vec1[i] * vec1[i];
            norm1 += vec1[i] * vec1[i];
            norm2 += vec2[i] * vec2[i];
        }

        return product / (Math.sqrt(norm1)) * (Math.sqrt(norm2));
    }
}
