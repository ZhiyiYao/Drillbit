package drillbit.data;

import org.apache.commons.cli.Options;

import javax.annotation.Nonnull;
import java.util.ArrayList;

public class RegressionDataset implements Dataset {
    ArrayList<String> features;
    ArrayList<String> targets;

    boolean optionProcessed;

    public RegressionDataset() {
        features = new ArrayList<>();
        targets = new ArrayList<>();
        loadAllSamples(features, targets);
        optionProcessed = false;
    }

    @Override
    public String getDatasetDescription() {
        return null;
    }

    @Override
    public void loadAllSamples(@Nonnull ArrayList<String> featureArray, @Nonnull ArrayList<String> targetArray) {

    }

    @Override
    public String loadOneSample() {
        return null;
    }

    @Override
    public void processOptions(String options) {

    }

    @Nonnull
    @Override
    public Options getOptions() {
        return null;
    }
}
