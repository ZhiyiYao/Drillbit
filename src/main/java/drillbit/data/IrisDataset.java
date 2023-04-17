package drillbit.data;

import drillbit.utils.common.Conditions;
import drillbit.utils.parser.StringParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import javax.annotation.Nonnull;
import java.util.ArrayList;

public class IrisDataset implements Dataset {
    public boolean optionProcessed;
    private ArrayList<String> features;
    private ArrayList<String> targets;

    public IrisDataset() {
        features = new ArrayList<>();
        targets = new ArrayList<>();
        loadAllSamples(features, targets);
        optionProcessed = false;
    }

    @Override
    public void loadAllSamples(@Nonnull ArrayList<String> featureArray, @Nonnull ArrayList<String> targetArray) {
        String datasetString = Dataset.getRawDataset("/dataset/iris.data");
        featureArray.clear();
        targetArray.clear();
        String[] rows = datasetString.split("\\n");
        for (String row : rows) {
            int index = row.lastIndexOf(",");
            featureArray.add("[" + row.substring(0, index) + "]");
            targetArray.add(row.substring(index + 1));
        }
    }

    @Override
    public String loadOneSample() {
        String featureAndTarget = "[";
        if (features.size() >= 1 && targets.size() >= 1) {
            featureAndTarget += features.get(0) + ", ";
            featureAndTarget += targets.get(0) + "]";
        } else {
            throw new UnsupportedOperationException("No enough samples in dataset.");
        }
        features.remove(0);
        targets.remove(0);

        return featureAndTarget;
    }

    @Override
    public void processOptions(String options) {
        int nSamples;
        boolean shuffle;

        CommandLine cl = parseOptions(options);

        shuffle = !cl.hasOption("not_shuffle");

        final int MAX_N_SAMPLES = 150;
        nSamples = StringParser.parseInt(cl.getOptionValue("n_samples"), MAX_N_SAMPLES);
        Conditions.checkArgument(0 <= nSamples && nSamples <= MAX_N_SAMPLES, String.format("Invalid sample number of %d", nSamples));

        features = new ArrayList<>(features.subList(0, nSamples));
        targets = new ArrayList<>(targets.subList(0, nSamples));

        if (shuffle) {
            Dataset.shuffle(features, targets);
        }

        optionProcessed = true;
    }

    @Nonnull
    @Override
    public Options getOptions() {
        Options opts = new Options();

        opts.addOption("n_samples", true, "assign number of samples to be generated");
        opts.addOption("not_shuffle", false, "do not shuffle samples");

        return opts;
    }

    @Override
    public String getDatasetDescription() {
        return "Iris dataset: {\n" +
                "   type: classification,\n" +
                "   n_classes: 3,\n" +
                "   max_n_samples: 150,\n" +
                "   features: [\n" +
                "       sepal_length,\n" +
                "       sepal_width,\n" +
                "       petal_length,\n" +
                "       petal_width\n" +
                "   ],\n" +
                "   labels: [\n" +
                "       Iris-setosa,\n" +
                "       Iris-versicolor,\n" +
                "       Iris-virginica\n" +
                "   ]\n" +
                "}";
    }
}
