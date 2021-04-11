package drillbit.test;

import drillbit.dataset.Dataset;
import drillbit.dataset.IrisDataset;

public class TestDataset {
    public static void main(String[] args) {
        testIris();
    }

    public static void testIris() {
        Dataset dataset = new IrisDataset();
        dataset.processOptions("");
    }
}
