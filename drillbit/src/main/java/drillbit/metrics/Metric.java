package drillbit.metrics;

import drillbit.optimizer.OptimizerOptions;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import javax.annotation.Nonnull;

public interface Metric {
    Options getOptions();

    default CommandLine parseOptions(String optionValue) {
        String[] args = optionValue.split("\\s+");
        Options opts = getOptions();
        opts.addOption("help", false, "Show metric help");

        final CommandLine cl;
        try {
            DefaultParser parser = new DefaultParser();
            cl = parser.parse(opts, args);
        } catch (IllegalArgumentException | ParseException e) {
            throw new IllegalArgumentException(e);
        }

        if (cl.hasOption("help")) {
            showHelp(opts);
        }

        return cl;
    }

    CommandLine processOptions(@Nonnull final CommandLine cl);

    void add(double label, double score, @Nonnull String commandLine);

    void add(@Nonnull final String label, @Nonnull final String predicted, @Nonnull final String commandLine);

    Object output();

    void reset();

    void showHelp(Options opts);
}
