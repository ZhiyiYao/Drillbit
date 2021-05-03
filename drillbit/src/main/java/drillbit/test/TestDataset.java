package drillbit.test;

import drillbit.data.Dataset;
import drillbit.data.FeatureHelper;
import drillbit.data.IrisDataset;

public class TestDataset {
    public static void main(String[] args) {
        testFeatureHelper();
    }

//    public static void testIris() {
//        Dataset dataset = new IrisDataset();
//        dataset.processOptions("");
//    }

    public static void testFeatureHelper() {
        String feature1 = FeatureHelper.addBias(FeatureHelper.addIndex("[0, 1, 3:2, 123:0.2, 2, 3, fe#123, fsd:12]"));
        System.out.println(feature1);
        String feature2 = FeatureHelper.addIndex(FeatureHelper.addBias("[0, 1, 2:2, 123:0.2, 2, 3, fe#123, fsd:12]"));
        System.out.println(feature2);
    }
}
