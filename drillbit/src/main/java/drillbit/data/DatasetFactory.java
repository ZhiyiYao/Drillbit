package drillbit.data;

import javax.annotation.Nonnull;

public class DatasetFactory {
    public static Dataset create(@Nonnull final String datasetName) {
        if ("iris".equalsIgnoreCase(datasetName)) {
            return new IrisDataset();
        }
        else if ("digits".equalsIgnoreCase(datasetName)) {
            return new DigitsDataset();
        }
        else if ("boston".equalsIgnoreCase(datasetName)) {
            return new BostonDataset();
        }
        else {
            throw new IllegalArgumentException(String.format("Invalid dataset name of %s", datasetName));
        }
    }
}
