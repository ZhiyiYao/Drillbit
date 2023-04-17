package drillbit.decisionTree.algorithm;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class discreteC45 implements Evaluation {
    public double getEvaluation(ConcurrentHashMap<Double, ArrayList<Double>> statics, int sampleNum, double H) {
        double result = 0, HA = 0;

        for (ArrayList<Double> v : statics.values()) {
            double sum = 0;
            double oneResult = 0;
            double p, q;
            for (Double i : v) {
                sum += i;
            }

            for (Double i : v) {
                if (i != 0) {
                    p = i / (double) sum;
                    oneResult += -p * Math.log(p);
                }
            }

            //HA
            q = sum / (double) sampleNum;
            HA += -q * Math.log(q);

            result += oneResult * sum / sampleNum;
        }

        return (H - result) / HA;
    }
}
