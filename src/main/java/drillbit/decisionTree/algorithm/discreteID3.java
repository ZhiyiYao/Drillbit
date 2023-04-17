package drillbit.decisionTree.algorithm;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class discreteID3 implements Evaluation {
    public double getEvaluation(ConcurrentHashMap<Double, ArrayList<Double>> statics, int sampleNum, double H) {
        double result = 0;

        for (ArrayList<Double> v : statics.values()) {
            int sum = 0;
            double oneResult = 0;
            double p;
            for (Double i : v) {
                sum += i;
            }

            for (Double i : v) {
                if (i != 0) {
                    p = i / (double) sum;
                    oneResult += -p * Math.log(p);
                }
            }

            result += oneResult * (double) sum / sampleNum;
        }
        //System.out.println(result);

        return H - result;
    }
}
