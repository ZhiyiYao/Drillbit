package drillbit.data;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import javax.annotation.Nonnull;
import java.util.ArrayList;

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

    void shuffle(ArrayList<String> features, ArrayList<String> targets);
}
