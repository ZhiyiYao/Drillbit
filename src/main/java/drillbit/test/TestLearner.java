package drillbit.test;

import com.google.protobuf.InvalidProtocolBufferException;
import drillbit.DNN.NeuralNetworkLearner;
import drillbit.SVM.SupportVectorMachineLearner;
import drillbit.classification.multiclass.SoftmaxRegressionLearner;
import drillbit.data.*;
import drillbit.decisionTree.DecisionTreeLearner;
import drillbit.metrics.MeanAbsoluteError;
import drillbit.metrics.MeanSquaredError;
import drillbit.neighbors.KNeighborsClassificationLearner;
import drillbit.regression.GeneralRegressionLearner;
import drillbit.utils.parser.StringParser;

import java.util.ArrayList;

public class TestLearner {
    public static void main(String[] args) {
//        testShuffle();
//        testSoftmaxRegression();
//        testDecisionTree();
//        testSVM();
//        testLinearRegression1();
//        testKNNClassification();
        testClassificationNetwork();
    }

    public static void testClassificationNetwork() {
        long start, end;
        ArrayList<String> featureArray = new ArrayList<>();
        ArrayList<String> targetArray = new ArrayList<>();
        DigitsDataset dataset = new DigitsDataset();
        dataset.loadAllSamples(featureArray, targetArray);
        Dataset.shuffle(featureArray, targetArray);

        ArrayList<String> trainFeature = new ArrayList<>(featureArray.subList(0, 700));
        ArrayList<String> trainTarget = new ArrayList<>(targetArray.subList(0, 700));
        ArrayList<String> testFeature = new ArrayList<>(featureArray.subList(700, 1000));
        ArrayList<String> testTarget = new ArrayList<>(targetArray.subList(700, 1000));

        start = System.currentTimeMillis();
        NeuralNetworkLearner learner = new NeuralNetworkLearner();
        for (int i = 0; i < trainFeature.size(); i++) {
            learner.add(FeatureHelper.addBias(FeatureHelper.addIndex(trainFeature.get(i))), trainTarget.get(i));
        }
        byte[] bytes = learner.output("-lr 0.001 -op ADAM -batch_size 10 -epoch 10 -layers 3");
        end = System.currentTimeMillis();

        NeuralNetworkLearner newLearner = new NeuralNetworkLearner();
        try {
            newLearner.fromByteArray(bytes);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }

        int correct = 0;
        for (int i = 0; i < testFeature.size(); i++) {
            String predicted = (String) newLearner.predict(FeatureHelper.addBias(FeatureHelper.addIndex(testFeature.get(i))), "");
            String actual = testTarget.get(i);
            //System.out.println("predicted: " + predicted + ", actual: " + actual);
            if (predicted.equals(actual)) {
                correct++;
            }
        }
        System.out.println("corrected: " + correct);
        System.out.println("accuracy: " + (double) correct / testFeature.size());
        System.out.println("time: " + (end - start));

    }

    public static void testRegressionNetwork() {
        ArrayList<String> trainFeature = new ArrayList<>(),
                testFeature = new ArrayList<>(),
                trainTarget = new ArrayList<>(),
                testTarget = new ArrayList<>();

        for (int i = 0; i < 200; i++) {
            double x1 = Math.random(),
                    x2 = Math.random(), x3 = Math.random(), x4 = Math.random();
            double y1 = 2 * x1 - x2 + Math.random() / 10;
            double y2 = 2 * x3 - x4 + Math.random() / 10;

            trainFeature.add("[0:1.0, 1:" + x1 + ", 2:" + x2 + "]");
            trainTarget.add(Double.toString(y1));

            testFeature.add("[0:1.0, 1:" + x3 + ", 2:" + x4 + "]");
            testTarget.add(Double.toString(y2));
        }

        NeuralNetworkLearner learner = new NeuralNetworkLearner();
        for (int i = 0; i < trainFeature.size(); i++) {
            learner.add(FeatureHelper.addBias(FeatureHelper.addIndex(trainFeature.get(i))), trainTarget.get(i));
        }

        byte[] bytes = learner.output("-op ADAM -type regression -batch_size 10 -epoch 10 -layers 1 -layer_params [4,16,64,16]");

        NeuralNetworkLearner newLearner = new NeuralNetworkLearner();
        try {
            newLearner.fromByteArray(bytes);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < testFeature.size(); i++) {
            double d = StringParser.parseDouble((String) newLearner.predict(FeatureHelper.addBias(FeatureHelper.addIndex(testFeature.get(i))), ""), 0);

            System.out.println("predicted: " + d + ", actual: " + testTarget.get(i));
        }
    }

//    public static void testDecisionTree(){
//        ArrayList<String> featureArray = new ArrayList<>();
//        ArrayList<String> targetArray = new ArrayList<>();
//        IrisDataset dataset = new IrisDataset();
//        dataset.loadAllSamples(featureArray, targetArray);
//        Dataset.shuffle(featureArray, targetArray);
//
//        ArrayList<String> trainFeature = new ArrayList<>(featureArray.subList(0, 100));
//        ArrayList<String> trainTarget = new ArrayList<>(targetArray.subList(0, 100));
//        ArrayList<String> testFeature = new ArrayList<>(featureArray.subList(100, 150));
//        ArrayList<String> testTarget = new ArrayList<>(targetArray.subList(100, 150));
//        //System.out.println(trainTarget);
//
//        DecisionTreeLearner learner = new DecisionTreeLearner();
//        for (int i = 0; i < trainFeature.size(); i++) {
//            learner.add(FeatureHelper.addBias(FeatureHelper.addIndex(trainFeature.get(i))), trainTarget.get(i));
//        }
//
//        byte[] bytes = learner.output("-criterion ID3");
//
//        DecisionTreeLearner newLearner = new DecisionTreeLearner();
//        try {
//            newLearner.fromByteArray(bytes);
//        } catch (InvalidProtocolBufferException e) {
//            e.printStackTrace();
//        }
//
//        for (int i = 0; i < testFeature.size(); i++) {
//            String d = (String) newLearner.predict(FeatureHelper.addBias(FeatureHelper.addIndex(testFeature.get(i))), "");
//
//            System.out.println("predicted: " + d + ", actual: " + testTarget.get(i));
//        }
//    }

    public static void testSVM() {
        long start, end;
        ArrayList<String> featureArray = new ArrayList<>();
        ArrayList<String> targetArray = new ArrayList<>();
        IrisDataset dataset = new IrisDataset();
        dataset.loadAllSamples(featureArray, targetArray);
        Dataset.shuffle(featureArray, targetArray);

        ArrayList<String> trainFeature = new ArrayList<>(featureArray.subList(0, 150));
        ArrayList<String> trainTarget = new ArrayList<>(targetArray.subList(0, 150));
        ArrayList<String> testFeature = new ArrayList<>(featureArray.subList(0, 150));
        ArrayList<String> testTarget = new ArrayList<>(targetArray.subList(0, 150));

        start = System.currentTimeMillis();
        SupportVectorMachineLearner learner = new SupportVectorMachineLearner();
        for (int i = 0; i < trainFeature.size(); i++) {
            learner.add(FeatureHelper.addBias(FeatureHelper.addIndex(trainFeature.get(i))), trainTarget.get(i));
        }
        byte[] bytes = learner.output("");

        end = System.currentTimeMillis();

        SupportVectorMachineLearner newLearner = new SupportVectorMachineLearner();
        try {
            newLearner.fromByteArray(bytes);
        } catch (InvalidProtocolBufferException e) {
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
        System.out.println("time: " + (end - start));
    }


    public static void testDecisionTree() {
        long start, end;
        ArrayList<String> featureArray = new ArrayList<>();
        ArrayList<String> targetArray = new ArrayList<>();
        IrisDataset dataset = new IrisDataset();
        dataset.loadAllSamples(featureArray, targetArray);
        Dataset.shuffle(featureArray, targetArray);

        ArrayList<String> trainFeature = new ArrayList<>(featureArray.subList(0, 100));
        ArrayList<String> trainTarget = new ArrayList<>(targetArray.subList(0, 100));
        ArrayList<String> testFeature = new ArrayList<>(featureArray.subList(100, 150));
        ArrayList<String> testTarget = new ArrayList<>(targetArray.subList(100, 150));

        start = System.currentTimeMillis();
        DecisionTreeLearner learner = new DecisionTreeLearner();
        for (int i = 0; i < trainFeature.size(); i++) {
            learner.add(FeatureHelper.addBias(FeatureHelper.addIndex(trainFeature.get(i))), trainTarget.get(i));
        }
        byte[] bytes = learner.output("-criterion ID3");
        end = System.currentTimeMillis();

        DecisionTreeLearner newLearner = new DecisionTreeLearner();
        try {
            newLearner.fromByteArray(bytes);
        } catch (InvalidProtocolBufferException e) {
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
        System.out.println("time: " + (end - start));
    }

    public static void testLinearRegression() {
        ArrayList<String> trainFeature = new ArrayList<>(),
                testFeature = new ArrayList<>(),
                trainTarget = new ArrayList<>(),
                testTarget = new ArrayList<>();

        for (int i = 0; i < 200; i++) {
            double x1 = Math.random(),
                    x2 = Math.random(), x3 = Math.random(), x4 = Math.random();
            double y1 = 2 * x1 - x2 + Math.random() / 10;
            double y2 = 2 * x3 - x4 + Math.random() / 10;

            trainFeature.add("[0:1.0, 1:" + x1 + ", 2:" + x2 + "]");
            trainTarget.add(Double.toString(y1));

            testFeature.add("[0:1.0, 1:" + x3 + ", 2:" + x4 + "]");
            testTarget.add(Double.toString(y2));
        }

        GeneralRegressionLearner learner = new GeneralRegressionLearner();
        for (int i = 0; i < trainFeature.size(); i++) {
            learner.add(FeatureHelper.addBias(FeatureHelper.addIndex(trainFeature.get(i))), trainTarget.get(i));
        }

        byte[] bytes = learner.output("-dense -iters 500 -regularization l2 -opt sgd -loss squaredloss -eta0 0.01");

        GeneralRegressionLearner newLearner = new GeneralRegressionLearner();
        try {
            newLearner.fromByteArray(bytes);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < testFeature.size(); i++) {
            double d = (double) newLearner.predict(FeatureHelper.addBias(FeatureHelper.addIndex(testFeature.get(i))), "");

            System.out.println("predicted: " + d + ", actual: " + testTarget.get(i));
        }
    }

    public static void testLinearRegression1() {
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

        byte[] bytes = learner.output("-dense -iters 100 -regularization l2 -opt sgd -loss squaredloss -eta0 0.01");

        GeneralRegressionLearner newLearner = new GeneralRegressionLearner();
        try {
            newLearner.fromByteArray(bytes);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }

        MeanSquaredError mse = new MeanSquaredError();
        MeanAbsoluteError mae = new MeanAbsoluteError();
        for (int i = 0; i < testFeature.size(); i++) {
            double d = (double) newLearner.predict(FeatureHelper.addBias(FeatureHelper.addIndex(testFeature.get(i))), "");
            mse.add(testTarget.get(i), Double.toString(d), "");
            mae.add(testTarget.get(i), Double.toString(d), "");
            System.out.println("predicted: " + d + ", actual: " + testTarget.get(i));
        }

        System.out.println("mae: " + mae.output());
        System.out.println("mse: " + mse.output());
    }

    public static void testSoftmaxRegression() {
        long start, end;
        ArrayList<String> featureArray = new ArrayList<>();
        ArrayList<String> targetArray = new ArrayList<>();
        DigitsDataset dataset = new DigitsDataset();
        dataset.loadAllSamples(featureArray, targetArray);
        Dataset.shuffle(featureArray, targetArray);

        ArrayList<String> trainFeature = new ArrayList<>(featureArray.subList(0, 700));
        ArrayList<String> trainTarget = new ArrayList<>(targetArray.subList(0, 700));
        ArrayList<String> testFeature = new ArrayList<>(featureArray.subList(700, 1000));
        ArrayList<String> testTarget = new ArrayList<>(targetArray.subList(700, 1000));

        start = System.currentTimeMillis();
        SoftmaxRegressionLearner learner = new SoftmaxRegressionLearner();
        for (int i = 0; i < trainFeature.size(); i++) {
            learner.add(FeatureHelper.addBias(FeatureHelper.addIndex(trainFeature.get(i))), trainTarget.get(i));
        }
        byte[] bytes = learner.output("-dense -iters 50 -eta0 0.1");
        end = System.currentTimeMillis();

        SoftmaxRegressionLearner newLearner = new SoftmaxRegressionLearner();
        try {
            newLearner.fromByteArray(bytes);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }

        int correct = 0;
        for (int i = 0; i < testFeature.size(); i++) {
            String predicted = (String) newLearner.predict(FeatureHelper.addBias(FeatureHelper.addIndex(testFeature.get(i))), "");
            String actual = testTarget.get(i);
            //System.out.println("predicted: " + predicted + ", actual: " + actual);
            if (predicted.equals(actual)) {
                correct++;
            }
        }
        System.out.println("corrected: " + correct);
        System.out.println("accuracy: " + (double) correct / testFeature.size());
        System.out.println("time: " + (end - start));

    }

    public static void testKNNClassification() {
        long start, end;
        ArrayList<String> featureArray = new ArrayList<>();
        ArrayList<String> targetArray = new ArrayList<>();
        DigitsDataset dataset = new DigitsDataset();
        dataset.loadAllSamples(featureArray, targetArray);
        Dataset.shuffle(featureArray, targetArray);

        ArrayList<String> trainFeature = new ArrayList<>(featureArray.subList(0, 700));
        ArrayList<String> trainTarget = new ArrayList<>(targetArray.subList(0, 700));
        ArrayList<String> testFeature = new ArrayList<>(featureArray.subList(700, 1000));
        ArrayList<String> testTarget = new ArrayList<>(targetArray.subList(700, 1000));

        start = System.currentTimeMillis();
        KNeighborsClassificationLearner learner = new KNeighborsClassificationLearner();
        for (int i = 0; i < trainFeature.size(); i++) {
            learner.add(FeatureHelper.addBias(FeatureHelper.addIndex(trainFeature.get(i))), trainTarget.get(i));
        }
        byte[] bytes = learner.output("-dense -dims 65 -k 5 -solver brute -weight inverse -distance pnorm -p 2");
        end = System.currentTimeMillis();

        KNeighborsClassificationLearner newLearner = new KNeighborsClassificationLearner();
        try {
            newLearner.fromByteArray(bytes);
        } catch (InvalidProtocolBufferException e) {
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
        System.out.println("time: " + (end - start));
    }
}
