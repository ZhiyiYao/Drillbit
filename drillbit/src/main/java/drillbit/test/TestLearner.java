package drillbit.test;

import com.google.protobuf.InvalidProtocolBufferException;
import drillbit.classification.GeneralClassificationLearner;
import drillbit.dataset.IrisDataset;

import java.util.ArrayList;

public class TestLearner {
    public static void main(String[] args) {
        try {
            testBinaryClassification();
        }
        catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
        testIris();
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

    public static void testIris() {
        ArrayList<String> featureArray = new ArrayList<>();
        ArrayList<String> targetArray = new ArrayList<>();
        IrisDataset dataset = new IrisDataset();
        dataset.loadAllSamples(featureArray, targetArray);
    }
}
