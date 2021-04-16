package drillbit.knn.distance;

import java.util.ArrayList;

public class EuclideanDistance implements Distance {
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
