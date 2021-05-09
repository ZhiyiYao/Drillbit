package drillbit.data;

import drillbit.optimizer.OptimizerOptions;
import drillbit.utils.common.Conditions;
import drillbit.utils.parser.StringParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import javax.annotation.Nonnull;
import java.util.*;

public class IrisDataset implements Dataset {
    private ArrayList<String> features;

    private ArrayList<String> targets;

    public boolean optionProcessed;

    public IrisDataset() {
        features = new ArrayList<>();
        targets = new ArrayList<>();
        loadAllSamples(features, targets);
        optionProcessed = false;
    }

    @Override
    public void loadAllSamples(ArrayList<String> featureArray, ArrayList<String> targetArray) {
        String datasetString = Dataset.getRawDataset("/dataset/iris.txt");
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
    public CommandLine parseOptions(@Nonnull String optionValue) {
        String[] args = optionValue.split("\\s+");
        Options opts = getOptions();
        OptimizerOptions.setup(opts);
        opts.addOption("help", false, "Show function help");

        final CommandLine cl;
        try {
            DefaultParser parser = new DefaultParser();
            cl = parser.parse(opts, args);
        } catch (IllegalArgumentException | ParseException e) {
            throw new IllegalArgumentException(e);
        }

        if (cl.hasOption("help")) {
            this.showHelp();
        }

        return cl;
    }


    @Override
    public String getDatasetDescription() {
        return "Iris dataset: {\n" +
                "   n_classes: 3,\n" +
                "   max_n_samples: 150,\n" +
                "   labels: [\n" +
                "       Iris-setosa,\n" +
                "       Iris-versicolor,\n" +
                "       Iris-virginica\n" +
                "   ]\n" +
                "}";
    }
}
