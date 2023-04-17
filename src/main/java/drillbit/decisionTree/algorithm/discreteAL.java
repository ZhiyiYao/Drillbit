package drillbit.decisionTree.algorithm;

import drillbit.FeatureValue;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public abstract class discreteAL implements decisionAL {
    private final Evaluation eva;

    public discreteAL(Evaluation e) {
        eva = e;
    }

    @Override
    public ConcurrentHashMap<Integer, ArrayList<Double>>
    getFeature(ArrayList<Integer> availFeature, ArrayList<ArrayList<FeatureValue>> features,
               ArrayList<Double> target) {
        //mark samples belongs classes
        ArrayList<ConcurrentHashMap<Double, ArrayList<Double>>> classNum;
        ConcurrentHashMap<Double, ArrayList<Double>> nodeLabels;
        int sampleNum = features.size(), featureNum = availFeature.size();

        //initialize
        classNum = new ArrayList<>();
        nodeLabels = new ConcurrentHashMap<>();
        for (int i = 0; i < featureNum; i++) {
            classNum.add(new ConcurrentHashMap<>());
        }

        //divide samples into classes
        for (int i = 0; i < sampleNum; i++) {
            ArrayList<FeatureValue> temp = features.get(i);
            int t = target.get(i).intValue();
            //System.out.println(t);

            //labels
            ArrayList<Double> labelStatics = nodeLabels.computeIfAbsent((double) 0, k -> new ArrayList<>());
            while (labelStatics.size() <= t) {
                labelStatics.add(0.);
            }
            labelStatics.set(t, labelStatics.get(t) + 1);

            for (int j = 0; j < featureNum; j++) {
                FeatureValue fv = temp.get(availFeature.get(j));
                double v = fv.getValueAsDouble();
                ArrayList<Double> statics = classNum.get(j).computeIfAbsent(v, k -> new ArrayList<>());

                while (statics.size() <= t) {
                    statics.add(0.);
                }
                statics.set(t, statics.get(t) + 1);
                classNum.get(j).putIfAbsent(v, statics);
            }
        }
        //System.out.println(classNum.get(2));

        int featureIndex = -1;
        double H = -eva.getEvaluation(nodeLabels, sampleNum, 0), temp, maxValue = 0;
        ArrayList<Double> r;
        for (int i = 0; i < featureNum; i++) {
            temp = eva.getEvaluation(classNum.get(i), sampleNum, H);
            //System.out.println(temp);
            if (maxValue < temp) {
                maxValue = temp;
                featureIndex = i;
            }
        }

        if (featureIndex != -1) {
            r = new ArrayList<>(classNum.get(featureIndex).keySet());

            ConcurrentHashMap<Integer, ArrayList<Double>> featureSet = new ConcurrentHashMap<>();
            featureSet.put(availFeature.get(featureIndex), r);
            return featureSet;
        } else {
            return null;
        }
    }
}
