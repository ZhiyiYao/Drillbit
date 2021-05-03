package drillbit.data;

import drillbit.optimizer.OptimizerOptions;
import drillbit.utils.common.Conditions;
import drillbit.utils.parser.StringParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

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
        String datasetString = getRawDataset();
        featureArray.clear();
        targetArray.clear();
        String[] rows = datasetString.split("\\n");
        int index;
        for (String row : rows) {
            index = row.lastIndexOf(",");
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
        }
        else {
            throw new UnsupportedOperationException("No enough samples in dataset.");
        }
        features.remove(0);
        targets.remove(0);

        return featureAndTarget;
    }

    @Override
    public void processOptions(String options) {
        boolean shuffle = true;
        int nSamples = 150;

        CommandLine cl = parseOptions(options);

        if (cl.hasOption("not_shuffle")) {
            shuffle = false;
        }

        if (cl.hasOption("nsamples")) {
            int nSamplesAssigned = StringParser.parseInt(cl.getOptionValue("nsamples"), 0);
            Conditions.checkArgument(0 <= nSamplesAssigned && nSamplesAssigned <= nSamples, String.format("Invalid sample number of %d", nSamplesAssigned));
            nSamples = nSamplesAssigned;
        }

        if (shuffle) {
            shuffle(features, targets);
        }

        ArrayList<String> newFeatures = new ArrayList<>();
        ArrayList<String> newTargets = new ArrayList<>();
        for (String feature : features.subList(0, nSamples)) {
            newFeatures.add(feature);
        }

        for (String target : targets.subList(0, nSamples)) {
            newTargets.add(target);
        }

        features = newFeatures;
        targets = newTargets;

        optionProcessed = true;
    }

    @Nonnull
    @Override
    public Options getOptions() {
        Options options = new Options();
        options.addOption("nsamples", "number_of_samples", true, "assign number of samples to be generated");
        options.addOption("not_shuffle", "not_shullfe_samples", false, "do not shuffle samples");

        return options;
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
    public void shuffle(ArrayList<String> features, ArrayList<String> targets) {
        Random rnd = new Random();
        Collections.shuffle(features, rnd);
        Collections.shuffle(targets, rnd);
    }

    @Override
    public String getRawDataset() {
        return "5.1,3.5,1.4,0.2,Iris-setosa\n" +
                "4.9,3.0,1.4,0.2,Iris-setosa\n" +
                "4.7,3.2,1.3,0.2,Iris-setosa\n" +
                "4.6,3.1,1.5,0.2,Iris-setosa\n" +
                "5.0,3.6,1.4,0.2,Iris-setosa\n" +
                "5.4,3.9,1.7,0.4,Iris-setosa\n" +
                "4.6,3.4,1.4,0.3,Iris-setosa\n" +
                "5.0,3.4,1.5,0.2,Iris-setosa\n" +
                "4.4,2.9,1.4,0.2,Iris-setosa\n" +
                "4.9,3.1,1.5,0.1,Iris-setosa\n" +
                "5.4,3.7,1.5,0.2,Iris-setosa\n" +
                "4.8,3.4,1.6,0.2,Iris-setosa\n" +
                "4.8,3.0,1.4,0.1,Iris-setosa\n" +
                "4.3,3.0,1.1,0.1,Iris-setosa\n" +
                "5.8,4.0,1.2,0.2,Iris-setosa\n" +
                "5.7,4.4,1.5,0.4,Iris-setosa\n" +
                "5.4,3.9,1.3,0.4,Iris-setosa\n" +
                "5.1,3.5,1.4,0.3,Iris-setosa\n" +
                "5.7,3.8,1.7,0.3,Iris-setosa\n" +
                "5.1,3.8,1.5,0.3,Iris-setosa\n" +
                "5.4,3.4,1.7,0.2,Iris-setosa\n" +
                "5.1,3.7,1.5,0.4,Iris-setosa\n" +
                "4.6,3.6,1.0,0.2,Iris-setosa\n" +
                "5.1,3.3,1.7,0.5,Iris-setosa\n" +
                "4.8,3.4,1.9,0.2,Iris-setosa\n" +
                "5.0,3.0,1.6,0.2,Iris-setosa\n" +
                "5.0,3.4,1.6,0.4,Iris-setosa\n" +
                "5.2,3.5,1.5,0.2,Iris-setosa\n" +
                "5.2,3.4,1.4,0.2,Iris-setosa\n" +
                "4.7,3.2,1.6,0.2,Iris-setosa\n" +
                "4.8,3.1,1.6,0.2,Iris-setosa\n" +
                "5.4,3.4,1.5,0.4,Iris-setosa\n" +
                "5.2,4.1,1.5,0.1,Iris-setosa\n" +
                "5.5,4.2,1.4,0.2,Iris-setosa\n" +
                "4.9,3.1,1.5,0.1,Iris-setosa\n" +
                "5.0,3.2,1.2,0.2,Iris-setosa\n" +
                "5.5,3.5,1.3,0.2,Iris-setosa\n" +
                "4.9,3.1,1.5,0.1,Iris-setosa\n" +
                "4.4,3.0,1.3,0.2,Iris-setosa\n" +
                "5.1,3.4,1.5,0.2,Iris-setosa\n" +
                "5.0,3.5,1.3,0.3,Iris-setosa\n" +
                "4.5,2.3,1.3,0.3,Iris-setosa\n" +
                "4.4,3.2,1.3,0.2,Iris-setosa\n" +
                "5.0,3.5,1.6,0.6,Iris-setosa\n" +
                "5.1,3.8,1.9,0.4,Iris-setosa\n" +
                "4.8,3.0,1.4,0.3,Iris-setosa\n" +
                "5.1,3.8,1.6,0.2,Iris-setosa\n" +
                "4.6,3.2,1.4,0.2,Iris-setosa\n" +
                "5.3,3.7,1.5,0.2,Iris-setosa\n" +
                "5.0,3.3,1.4,0.2,Iris-setosa\n" +
                "7.0,3.2,4.7,1.4,Iris-versicolor\n" +
                "6.4,3.2,4.5,1.5,Iris-versicolor\n" +
                "6.9,3.1,4.9,1.5,Iris-versicolor\n" +
                "5.5,2.3,4.0,1.3,Iris-versicolor\n" +
                "6.5,2.8,4.6,1.5,Iris-versicolor\n" +
                "5.7,2.8,4.5,1.3,Iris-versicolor\n" +
                "6.3,3.3,4.7,1.6,Iris-versicolor\n" +
                "4.9,2.4,3.3,1.0,Iris-versicolor\n" +
                "6.6,2.9,4.6,1.3,Iris-versicolor\n" +
                "5.2,2.7,3.9,1.4,Iris-versicolor\n" +
                "5.0,2.0,3.5,1.0,Iris-versicolor\n" +
                "5.9,3.0,4.2,1.5,Iris-versicolor\n" +
                "6.0,2.2,4.0,1.0,Iris-versicolor\n" +
                "6.1,2.9,4.7,1.4,Iris-versicolor\n" +
                "5.6,2.9,3.6,1.3,Iris-versicolor\n" +
                "6.7,3.1,4.4,1.4,Iris-versicolor\n" +
                "5.6,3.0,4.5,1.5,Iris-versicolor\n" +
                "5.8,2.7,4.1,1.0,Iris-versicolor\n" +
                "6.2,2.2,4.5,1.5,Iris-versicolor\n" +
                "5.6,2.5,3.9,1.1,Iris-versicolor\n" +
                "5.9,3.2,4.8,1.8,Iris-versicolor\n" +
                "6.1,2.8,4.0,1.3,Iris-versicolor\n" +
                "6.3,2.5,4.9,1.5,Iris-versicolor\n" +
                "6.1,2.8,4.7,1.2,Iris-versicolor\n" +
                "6.4,2.9,4.3,1.3,Iris-versicolor\n" +
                "6.6,3.0,4.4,1.4,Iris-versicolor\n" +
                "6.8,2.8,4.8,1.4,Iris-versicolor\n" +
                "6.7,3.0,5.0,1.7,Iris-versicolor\n" +
                "6.0,2.9,4.5,1.5,Iris-versicolor\n" +
                "5.7,2.6,3.5,1.0,Iris-versicolor\n" +
                "5.5,2.4,3.8,1.1,Iris-versicolor\n" +
                "5.5,2.4,3.7,1.0,Iris-versicolor\n" +
                "5.8,2.7,3.9,1.2,Iris-versicolor\n" +
                "6.0,2.7,5.1,1.6,Iris-versicolor\n" +
                "5.4,3.0,4.5,1.5,Iris-versicolor\n" +
                "6.0,3.4,4.5,1.6,Iris-versicolor\n" +
                "6.7,3.1,4.7,1.5,Iris-versicolor\n" +
                "6.3,2.3,4.4,1.3,Iris-versicolor\n" +
                "5.6,3.0,4.1,1.3,Iris-versicolor\n" +
                "5.5,2.5,4.0,1.3,Iris-versicolor\n" +
                "5.5,2.6,4.4,1.2,Iris-versicolor\n" +
                "6.1,3.0,4.6,1.4,Iris-versicolor\n" +
                "5.8,2.6,4.0,1.2,Iris-versicolor\n" +
                "5.0,2.3,3.3,1.0,Iris-versicolor\n" +
                "5.6,2.7,4.2,1.3,Iris-versicolor\n" +
                "5.7,3.0,4.2,1.2,Iris-versicolor\n" +
                "5.7,2.9,4.2,1.3,Iris-versicolor\n" +
                "6.2,2.9,4.3,1.3,Iris-versicolor\n" +
                "5.1,2.5,3.0,1.1,Iris-versicolor\n" +
                "5.7,2.8,4.1,1.3,Iris-versicolor\n" +
                "6.3,3.3,6.0,2.5,Iris-virginica\n" +
                "5.8,2.7,5.1,1.9,Iris-virginica\n" +
                "7.1,3.0,5.9,2.1,Iris-virginica\n" +
                "6.3,2.9,5.6,1.8,Iris-virginica\n" +
                "6.5,3.0,5.8,2.2,Iris-virginica\n" +
                "7.6,3.0,6.6,2.1,Iris-virginica\n" +
                "4.9,2.5,4.5,1.7,Iris-virginica\n" +
                "7.3,2.9,6.3,1.8,Iris-virginica\n" +
                "6.7,2.5,5.8,1.8,Iris-virginica\n" +
                "7.2,3.6,6.1,2.5,Iris-virginica\n" +
                "6.5,3.2,5.1,2.0,Iris-virginica\n" +
                "6.4,2.7,5.3,1.9,Iris-virginica\n" +
                "6.8,3.0,5.5,2.1,Iris-virginica\n" +
                "5.7,2.5,5.0,2.0,Iris-virginica\n" +
                "5.8,2.8,5.1,2.4,Iris-virginica\n" +
                "6.4,3.2,5.3,2.3,Iris-virginica\n" +
                "6.5,3.0,5.5,1.8,Iris-virginica\n" +
                "7.7,3.8,6.7,2.2,Iris-virginica\n" +
                "7.7,2.6,6.9,2.3,Iris-virginica\n" +
                "6.0,2.2,5.0,1.5,Iris-virginica\n" +
                "6.9,3.2,5.7,2.3,Iris-virginica\n" +
                "5.6,2.8,4.9,2.0,Iris-virginica\n" +
                "7.7,2.8,6.7,2.0,Iris-virginica\n" +
                "6.3,2.7,4.9,1.8,Iris-virginica\n" +
                "6.7,3.3,5.7,2.1,Iris-virginica\n" +
                "7.2,3.2,6.0,1.8,Iris-virginica\n" +
                "6.2,2.8,4.8,1.8,Iris-virginica\n" +
                "6.1,3.0,4.9,1.8,Iris-virginica\n" +
                "6.4,2.8,5.6,2.1,Iris-virginica\n" +
                "7.2,3.0,5.8,1.6,Iris-virginica\n" +
                "7.4,2.8,6.1,1.9,Iris-virginica\n" +
                "7.9,3.8,6.4,2.0,Iris-virginica\n" +
                "6.4,2.8,5.6,2.2,Iris-virginica\n" +
                "6.3,2.8,5.1,1.5,Iris-virginica\n" +
                "6.1,2.6,5.6,1.4,Iris-virginica\n" +
                "7.7,3.0,6.1,2.3,Iris-virginica\n" +
                "6.3,3.4,5.6,2.4,Iris-virginica\n" +
                "6.4,3.1,5.5,1.8,Iris-virginica\n" +
                "6.0,3.0,4.8,1.8,Iris-virginica\n" +
                "6.9,3.1,5.4,2.1,Iris-virginica\n" +
                "6.7,3.1,5.6,2.4,Iris-virginica\n" +
                "6.9,3.1,5.1,2.3,Iris-virginica\n" +
                "5.8,2.7,5.1,1.9,Iris-virginica\n" +
                "6.8,3.2,5.9,2.3,Iris-virginica\n" +
                "6.7,3.3,5.7,2.5,Iris-virginica\n" +
                "6.7,3.0,5.2,2.3,Iris-virginica\n" +
                "6.3,2.5,5.0,1.9,Iris-virginica\n" +
                "6.5,3.0,5.2,2.0,Iris-virginica\n" +
                "6.2,3.4,5.4,2.3,Iris-virginica\n" +
                "5.9,3.0,5.1,1.8,Iris-virginica";
    }

    @Override
    public String getDatasetDescription() {
        return null;
    }

    @Override
    public void showHelp() {
        System.out.println(getDatasetDescription());
    }
}
