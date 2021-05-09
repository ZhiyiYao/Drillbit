package drillbit.test;

import drillbit.data.Dataset;
import drillbit.data.DigitsDataset;
import drillbit.data.IrisDataset;

public class TestDataset {
    public static void main(String[] args) {
        testIris();
        testDigits();
    }

    public static void testIris() {
        Dataset dataset = new IrisDataset();
        dataset.processOptions("-n_samples 30");

        for (int i = 0; i < 30; i++) {
            System.out.println(dataset.loadOneSample());
        }
    }

    public static void testDigits() {
        Dataset dataset = new DigitsDataset();
        dataset.processOptions("-n_samples 300");

        for (int i = 0; i < 300; i++) {
            System.out.println(dataset.loadOneSample());
        }
    }
}
