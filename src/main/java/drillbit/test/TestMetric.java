package drillbit.test;

import com.google.protobuf.InvalidProtocolBufferException;
import drillbit.classification.multiclass.SoftmaxRegressionLearner;
import drillbit.data.Dataset;
import drillbit.data.DigitsDataset;
import drillbit.data.FeatureHelper;
import drillbit.metrics.ConfusionMatrix;

import java.util.ArrayList;

public class TestMetric {
    public static void main(String[] args) {
        testConfusionMatrix();
    }

    public static void testConfusionMatrix() {
        ArrayList<String> featureArray = new ArrayList<>();
        ArrayList<String> targetArray = new ArrayList<>();
        DigitsDataset dataset = new DigitsDataset();
        dataset.loadAllSamples(featureArray, targetArray);
        Dataset.shuffle(featureArray, targetArray);

        ArrayList<String> trainFeature = new ArrayList<>(featureArray.subList(0, 1000));
        ArrayList<String> trainTarget = new ArrayList<>(targetArray.subList(0, 1000));
        ArrayList<String> testFeature = new ArrayList<>(featureArray.subList(1000, 1797));
        ArrayList<String> testTarget = new ArrayList<>(targetArray.subList(1000, 1797));

        SoftmaxRegressionLearner learner = new SoftmaxRegressionLearner();
        for (int i = 0; i < trainFeature.size(); i++) {
            learner.add(FeatureHelper.addBias(FeatureHelper.addIndex(trainFeature.get(i))), trainTarget.get(i));
        }
        byte[] bytes = learner.output("-dense -iters 30");

        SoftmaxRegressionLearner newLearner = new SoftmaxRegressionLearner();
        try {
            newLearner.fromByteArray(bytes);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }

        ConfusionMatrix cm = new ConfusionMatrix();
        for (int i = 0; i < testFeature.size(); i++) {
            String predicted = (String) newLearner.predict(FeatureHelper.addBias(FeatureHelper.addIndex(testFeature.get(i))), "");
            String actual = testTarget.get(i);
            cm.add(actual, predicted, "");
        }

        System.out.println(cm.output());
    }
}
