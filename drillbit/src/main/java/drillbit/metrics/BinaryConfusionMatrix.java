package drillbit.metrics;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.jetbrains.annotations.NotNull;

public class BinaryConfusionMatrix implements Metric {
    private boolean optionProcessed;

    private ConfusionMatrixtype confusionMatrixtype;

    public enum ConfusionMatrixtype {
        BIN, MUL
    }

    @Override
    public Options getOptions() {
        return null;
    }

    @Override
    public CommandLine processOptions(@NotNull CommandLine cl) {
        return null;
    }

    @Override
    public void add(double label, double score, @NotNull String commandLine) {

    }

    @Override
    public void add(@NotNull String label, @NotNull String predicted, @NotNull String commandLine) {

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
