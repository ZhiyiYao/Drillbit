package drillbit.decisionTree.algorithm;

import drillbit.FeatureValue;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public interface decisionAL {
    ConcurrentHashMap<Integer, ArrayList<Double>>
    getFeature(ArrayList<Integer> availFeature, ArrayList<ArrayList<FeatureValue>> features,
               ArrayList<Double> target);
}
