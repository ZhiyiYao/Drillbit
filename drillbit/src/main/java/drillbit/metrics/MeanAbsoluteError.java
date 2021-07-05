package drillbit.metrics;

import drillbit.utils.parser.StringParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import javax.annotation.Nonnull;

public class MeanAbsoluteError implements Metric {
    double cumulativeAbsoluteError;
    int count;

    public MeanAbsoluteError() {
        cumulativeAbsoluteError = 0.d;
        count = 0;
    }

    @Override
    public Options getOptions() {
        return new Options();
    }

    @Override
    public CommandLine processOptions(@Nonnull CommandLine cl) {
        // Do nothing
        return cl;
    }

    @Override
    public void add(double label, double score, @Nonnull String commandLine) {
        cumulativeAbsoluteError += Math.abs(label - score);
        count++;
    }

    @Override
    public void add(@Nonnull String label, @Nonnull String predicted, @Nonnull String commandLine) {
        add(StringParser.parseDouble(label, 0), StringParser.parseDouble(predicted, 0), commandLine);
    }

    @Override
    public Object output() {
        return cumulativeAbsoluteError / count;
    }

    @Override
    public void reset() {
        cumulativeAbsoluteError = 0.d;
        count = 0;
    }

    @Override
    public void showHelp(Options opts) {
        // Do nothing
    }
}
