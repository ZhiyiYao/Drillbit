package drillbit.decisionTree.algorithm;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public interface Evaluation {
    double getEvaluation(ConcurrentHashMap<Double, ArrayList<Double>> statics, int sampleNum, double H);
}
