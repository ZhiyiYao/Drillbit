package drillbit.data;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public interface Dataset {

    String getRawDataset();

    String getDatasetDescription();

    void showHelp();

    void loadAllSamples(ArrayList<String> featureArray, ArrayList<String> targetArray);

    String loadOneSample();

    void processOptions(String options);

    @Nonnull
    Options getOptions();

    CommandLine parseOptions(@Nonnull final String optionValue);

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
}
