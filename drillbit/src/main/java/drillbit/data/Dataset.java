package drillbit.data;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.util.Random;

public interface Dataset {
    String getDatasetDescription();

    void loadAllSamples(ArrayList<String> featureArray, ArrayList<String> targetArray);

    String loadOneSample();

    void processOptions(String options);

    default CommandLine parseOptions(@Nonnull String optionValue) {
        String[] args = optionValue.split("\\s+");
        Options opts = getOptions();
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

    @Nonnull
    Options getOptions();

    static void shuffle(ArrayList<String> features, ArrayList<String> targets) {
        if (features.size() != targets.size()) {
            throw new IllegalArgumentException("Size of input features and targets does not match");
        }
        int size = features.size();

        Random rnd = new Random();
        for (int i = size; i > 1; i--) {
            int index = rnd.nextInt(i);
            Collections.swap(features, i - 1, index);
            Collections.swap(targets, i - 1, index);
        }
    }

    default void showHelp() {
        System.out.println(getDatasetDescription());
    }

    static String getRawDataset(String dataset) {
        StringBuilder builder = new StringBuilder();
        DatasetReader reader = (new DatasetReader()).load(dataset);
        while (true) {
            String line = reader.readLine();
            if (line == null) {
                break;
            }
            builder.append(line).append("\n");
        }

        return builder.toString();
    }
}
