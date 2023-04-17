package drillbit.decisionTree.algorithm;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class discreteGini implements Evaluation {
    public double getEvaluation(ConcurrentHashMap<Double, ArrayList<Double>> statics, int sampleNum, double H) {
        double result = 0;

        for (ArrayList<Double> v : statics.values()) {
            int sum = 0;
            double oneResult = 1;
            double p;
            for (Double i : v) {
                sum += i;
            }

            for (Double i : v) {
                p = i / (double) sum;
                oneResult -= Math.pow(p, 2);
            }

            result += oneResult * (double) sum / sampleNum;
        }

        return H - result;
    }
}
