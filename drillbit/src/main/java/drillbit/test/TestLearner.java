package drillbit.test;

import com.google.protobuf.InvalidProtocolBufferException;
import drillbit.Learner;
import drillbit.classification.GeneralClassificationLearner;
import drillbit.classification.multiclass.SoftmaxRegressionLearner;
import drillbit.data.Dataset;
import drillbit.data.FeatureHelper;
import drillbit.data.IrisDataset;
import drillbit.neighbors.KNeighborClassificationLearner;
import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

public class TestLearner {
    public static void main(String[] args) {
//        testShuffle();
//        testSoftmaxRegression();
        testKNNClassification();
    }

    public static void testBinaryClassification() throws InvalidProtocolBufferException {
        String commandLine = "-iters 500 -loss logloss -regularization l2 -chk_cv";
        GeneralClassificationLearner learner = new GeneralClassificationLearner();
        double x, y;
        String feature;
        String target;
        for (int i = 0; i < 200; i++) {
            x = Math.random() * 2 - 3;
            y = Math.random() - 2;
            feature = String.format("[0:1, 1:%f, two:%f]", x, y);
            target = "1";
            learner.add(feature, target);
            x = Math.random() + 3;
            y = Math.random() * 3 + 5;
            feature = String.format("[0:1, 1:%f, two:%f]", x, y);
            target = "-1";
            learner.add(feature, target);
        }
        byte[] learnerByte = learner.output(commandLine);
        GeneralClassificationLearner newLearner = (GeneralClassificationLearner) (new GeneralClassificationLearner()).fromByteArray(learnerByte);

        x = Math.random() * 2 - 3;
        y = Math.random() - 2;

//        System.out.println(String.format("x = %f, y = %f, result = %f", x, y, result));

        x = Math.random() + 3;
        y = Math.random() * 3 + 5;

//        System.out.println(String.format("x = %f, y = %f, result = %f", x, y, result));
    }

    public static void testShuffle() {
        ArrayList<Integer> s1 = new ArrayList<>();
        ArrayList<Integer> s2 = new ArrayList<>();
        Random rnd = new Random();
        for (int i = 0; i < 10; i++) {
            s1.add(i);
            s2.add(i);
        }
        Collections.shuffle(s1, rnd);
        Collections.shuffle(s2, rnd);
        System.out.println(Arrays.toString(s1.toArray()));
        System.out.println(Arrays.toString(s2.toArray()));
    }

    public static void testIris() {
        ArrayList<String> featureArray = new ArrayList<>();
        ArrayList<String> targetArray = new ArrayList<>();
        IrisDataset dataset = new IrisDataset();
        dataset.loadAllSamples(featureArray, targetArray);
    }

    public static void testSoftmaxRegression() {
        ArrayList<String> featureArray = new ArrayList<>();
        ArrayList<String> targetArray = new ArrayList<>();
        IrisDataset dataset = new IrisDataset();
        dataset.loadAllSamples(featureArray, targetArray);
        Dataset.shuffle(featureArray, targetArray);

        SoftmaxRegressionLearner learner = new SoftmaxRegressionLearner();
        for (int i = 0; i < featureArray.size(); i++) {
            learner.add(FeatureHelper.addBias(FeatureHelper.addIndex(featureArray.get(i))), targetArray.get(i));
        }
        byte[] bytes = learner.output("-dense -solver brute");

        SoftmaxRegressionLearner newLearner = new SoftmaxRegressionLearner();
        try {
            newLearner.fromByteArray(bytes);
        }
        catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }

        int correct = 0;
        for (int i = 0; i < featureArray.size(); i++) {
            String predicted = (String) newLearner.predict(FeatureHelper.addBias(FeatureHelper.addIndex(featureArray.get(i))), "-return_proba");
            String actual = targetArray.get(i);
            System.out.println("predicted: " + predicted + ", actual: " + actual);
//            if (predicted.equals(actual)) {
//                correct++;
//            }
        }
//        System.out.println("corrected: " + correct);
//        System.out.println("accuracy: " + (double) correct / featureArray.size());

    }

    public static void testKNNClassification() {
        ArrayList<String> featureArray = new ArrayList<>();
        ArrayList<String> targetArray = new ArrayList<>();
        IrisDataset dataset = new IrisDataset();
        dataset.loadAllSamples(featureArray, targetArray);
        Dataset.shuffle(featureArray, targetArray);

        KNeighborClassificationLearner learner = new KNeighborClassificationLearner();
        for (int i = 0; i < featureArray.size(); i++) {
            learner.add(FeatureHelper.addBias(FeatureHelper.addIndex(featureArray.get(i))), targetArray.get(i));
        }
        byte[] bytes = learner.output("-dense -dims 5 -solver brute");

        KNeighborClassificationLearner newLearner = new KNeighborClassificationLearner();
        try {
            newLearner.fromByteArray(bytes);
        }
        catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }

        int correct = 0;
        for (int i = 0; i < featureArray.size(); i++) {
            String predicted = (String) newLearner.predict(FeatureHelper.addBias(FeatureHelper.addIndex(featureArray.get(i))), "-n_neighbors 4 -metric chebyshev -solver brute");
//            int predicted = (int) newLearner.predict(FeatureHelper.addBias(FeatureHelper.addIndex(featureArray.get(i))), "-solver brute -return_index");
            String actual = targetArray.get(i);
            System.out.println("predicted: " + predicted + ", actual: " + actual);
            if (predicted.equals(actual)) {
                correct++;
            }
        }

        System.out.println("corrected: " + correct);
        System.out.println("accuracy: " + (double) correct / featureArray.size());
    }
}
