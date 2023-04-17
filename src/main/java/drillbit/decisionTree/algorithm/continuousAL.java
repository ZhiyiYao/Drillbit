package drillbit.decisionTree.algorithm;

import drillbit.FeatureValue;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public abstract class continuousAL implements decisionAL {
    private final Evaluation eva;

    public continuousAL(Evaluation e) {
        eva = e;
    }

    @Override
    public ConcurrentHashMap<Integer, ArrayList<Double>>
    getFeature(ArrayList<Integer> availFeature,
               ArrayList<ArrayList<FeatureValue>> features, ArrayList<Double> target) {
        //mark samples belongs classes
        ConcurrentHashMap<Double, ArrayList<Double>> classNum;
        ConcurrentHashMap<Double, ArrayList<Double>> nodeLabels;
        int sampleNum = features.size(), featureNum = availFeature.size();

        //initialize
        nodeLabels = new ConcurrentHashMap<>();

        for (int i = 0; i < sampleNum; i++) {
            int t = target.get(i).intValue();
            //labels
            ArrayList<Double> labelStatics = nodeLabels.computeIfAbsent((double) 0, k -> new ArrayList<>());
            while (labelStatics.size() <= t) {
                labelStatics.add(0.);
            }
            labelStatics.set(t, labelStatics.get(t) + 1);
        }
        double H = -eva.getEvaluation(nodeLabels, sampleNum, 0);
        //System.out.println(H);
        //divide samples into classes and evaluation
        int flagIndex = -1;
        double flagValue = 0;
        double maxValue = 0;
        for (int i = 0; i < featureNum; i++) {
            int currentFeature = availFeature.get(i);

            for (int j = 0; j < sampleNum; j++) {
                classNum = new ConcurrentHashMap<>();
                ArrayList<FeatureValue> tag = features.get(j);
                FeatureValue tagFV = tag.get(currentFeature);
                double tagV = tagFV.getValueAsDouble();
//                if(j==0){
//                    System.out.println(target);
//                }

                for (int k = 0; k < sampleNum; k++) {
                    FeatureValue fv = features.get(k).get(currentFeature);
                    double v = fv.getValueAsDouble();
                    double cl = 1;
                    int t = target.get(k).intValue();

                    if (v < tagV) {
                        cl = -1;
                    }

                    ArrayList<Double> statics = classNum.computeIfAbsent(cl, n -> new ArrayList<>());

                    while (statics.size() <= t) {
                        statics.add(0.);
                    }
                    statics.set(t, statics.get(t) + 1);
                }

                double cur = eva.getEvaluation(classNum, sampleNum, H);
                //System.out.println(cur);
                if (cur > maxValue) {
                    maxValue = cur;
                    //System.out.println(cur);
                    flagIndex = currentFeature;
                    flagValue = tagV;
                }
            }
        }

        if (flagIndex != -1) {
            ArrayList<Double> r = new ArrayList<>();
            r.add(flagValue);

            ConcurrentHashMap<Integer, ArrayList<Double>> featureSet = new ConcurrentHashMap<>();
            featureSet.put(flagIndex, r);
            return featureSet;
        } else {
            return null;
        }
    }
}
