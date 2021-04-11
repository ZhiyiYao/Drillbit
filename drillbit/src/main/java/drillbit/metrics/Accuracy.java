package drillbit.metrics;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import javax.annotation.Nonnull;

public class Accuracy implements Metric {
    private MetricHelper helper;

    private int threshold;

    public enum AccuracyType {
        BIN, MUL
    }

    @Override
    public Options getOptions() {
        return null;
    }

    @Override
    public CommandLine processOptions(@Nonnull CommandLine cl) {
        return null;
    }

    @Override
    public void add(double label, double score, @Nonnull String commandLine) {

    }

    @Override
    public void add(@Nonnull String label, @Nonnull String predicted, @Nonnull String commandLine) {

    }

    @Override
    public Object output() {
        return 0;
    }

    @Override
    public void reset() {

    }

    @Override
    public void showHelp(Options opts) {

    }
}
