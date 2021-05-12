package drillbit.test;

import com.google.protobuf.InvalidProtocolBufferException;
import drillbit.classification.GeneralClassificationLearner;
import drillbit.classification.multiclass.SoftmaxRegressionLearner;
import drillbit.data.BostonDataset;
import drillbit.data.Dataset;
import drillbit.data.FeatureHelper;
import drillbit.data.IrisDataset;
import drillbit.neighbors.KNeighborsClassificationLearner;
import drillbit.regression.GeneralRegressionLearner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

public class TestLearner {
    public static void main(String[] args) {
//        testShuffle();
//        testSoftmaxRegression();
        testLinearRegression();
    }

    public static void testLinearRegression() {
        ArrayList<String> featureArray = new ArrayList<>();
        ArrayList<String> targetArray = new ArrayList<>();
        BostonDataset dataset = new BostonDataset();
        dataset.loadAllSamples(featureArray, targetArray);
        Dataset.shuffle(featureArray, targetArray);

        ArrayList<String> trainFeature = new ArrayList<>(featureArray.subList(0, 400));
        ArrayList<String> trainTarget = new ArrayList<>(targetArray.subList(0, 400));
        ArrayList<String> testFeature = new ArrayList<>(featureArray.subList(400, 506));
        ArrayList<String> testTarget = new ArrayList<>(targetArray.subList(400, 506));

        GeneralRegressionLearner learner = new GeneralRegressionLearner();
        for (int i = 0; i < trainFeature.size(); i++) {
            learner.add(FeatureHelper.addBias(FeatureHelper.addIndex(trainFeature.get(i))), trainTarget.get(i));
        }

        byte[] bytes = learner.output("-dense -iters 1 -regularization l2 -opt sgd -loss squaredloss -eta0 0.000001");

        GeneralRegressionLearner newLearner = new GeneralRegressionLearner();
        try {
            newLearner.fromByteArray(bytes);
        }
        catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < testFeature.size(); i++) {
            double d = (double) newLearner.predict(FeatureHelper.addBias(FeatureHelper.addIndex(testFeature.get(i))), "");
            System.out.println("predicted: " + d + ", actual: " + testTarget.get(i));
        }
    }

    public static void testSoftmaxRegression() {
        ArrayList<String> featureArray = new ArrayList<>();
        ArrayList<String> targetArray = new ArrayList<>();
        IrisDataset dataset = new IrisDataset();
        dataset.loadAllSamples(featureArray, targetArray);
        Dataset.shuffle(featureArray, targetArray);

        ArrayList<String> trainFeature = new ArrayList<>(featureArray.subList(0, 100));
        ArrayList<String> trainTarget = new ArrayList<>(targetArray.subList(0, 100));
        ArrayList<String> testFeature = new ArrayList<>(featureArray.subList(100, 150));
        ArrayList<String> testTarget = new ArrayList<>(targetArray.subList(100, 150));

        SoftmaxRegressionLearner learner = new SoftmaxRegressionLearner();
        for (int i = 0; i < trainFeature.size(); i++) {
            learner.add(FeatureHelper.addBias(FeatureHelper.addIndex(trainFeature.get(i))), trainTarget.get(i));
        }
        byte[] bytes = learner.output("-dense -iters 200");

        SoftmaxRegressionLearner newLearner = new SoftmaxRegressionLearner();
        try {
            newLearner.fromByteArray(bytes);
        }
        catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }

        int correct = 0;
        for (int i = 0; i < testFeature.size(); i++) {
            String predicted = (String) newLearner.predict(FeatureHelper.addBias(FeatureHelper.addIndex(testFeature.get(i))), "");
            String actual = testTarget.get(i);
            System.out.println("predicted: " + predicted + ", actual: " + actual);
            if (predicted.equals(actual)) {
                correct++;
            }
        }
        System.out.println("corrected: " + correct);
        System.out.println("accuracy: " + (double) correct / testFeature.size());

    }

    public static void testKNNClassification() {
        ArrayList<String> featureArray = new ArrayList<>();
        ArrayList<String> targetArray = new ArrayList<>();
        IrisDataset dataset = new IrisDataset();
        dataset.loadAllSamples(featureArray, targetArray);
        Dataset.shuffle(featureArray, targetArray);

        ArrayList<String> trainFeature = new ArrayList<>(featureArray.subList(0, 100));
        ArrayList<String> trainTarget = new ArrayList<>(targetArray.subList(0, 100));
        ArrayList<String> testFeature = new ArrayList<>(featureArray.subList(100, 150));
        ArrayList<String> testTarget = new ArrayList<>(targetArray.subList(100, 150));

        KNeighborsClassificationLearner learner = new KNeighborsClassificationLearner();
        for (int i = 0; i < trainFeature.size(); i++) {
            learner.add(FeatureHelper.addBias(FeatureHelper.addIndex(trainFeature.get(i))), trainTarget.get(i));
        }
        byte[] bytes = learner.output("-dense -dims 5 -k 3 -solver brute -weight inverse -distance pnorm -p 10");

        KNeighborsClassificationLearner newLearner = new KNeighborsClassificationLearner();
        try {
            newLearner.fromByteArray(bytes);
        }
        catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }

        int correct = 0;
        for (int i = 0; i < testFeature.size(); i++) {
            String predicted = (String) newLearner.predict(FeatureHelper.addBias(FeatureHelper.addIndex(testFeature.get(i))),
                    "");
//            int predicted = (int) newLearner.predict(FeatureHelper.addBias(FeatureHelper.addIndex(featureArray.get(i))), "-solver brute -return_index");
            String actual = testTarget.get(i);
            System.out.println("predicted: " + predicted + ", actual: " + actual);
            if (predicted.equals(actual)) {
                correct++;
            }
        }

        System.out.println("corrected: " + correct);
        System.out.println("accuracy: " + (double) correct / testFeature.size());
    }
}
