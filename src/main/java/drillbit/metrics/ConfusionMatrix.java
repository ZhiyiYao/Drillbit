package drillbit.metrics;

import drillbit.metrics.utils.PrettyPrinter;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class ConfusionMatrix implements Metric {
    ArrayList<String> labels;
    ConcurrentHashMap<String, ConcurrentHashMap<String, Integer>> confusionMatrix;

    boolean optionProcessed;

    public ConfusionMatrix() {
        optionProcessed = false;
    }

    @Override
    public Options getOptions() {
        return new Options();
    }

    @Override
    public CommandLine processOptions(@Nonnull final CommandLine cl) {
        confusionMatrix = new ConcurrentHashMap<>();
        labels = new ArrayList<>();
        return cl;
    }

    @Override
    public void add(double label, double score, @Nonnull String commandLine) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void add(@Nonnull String actual, @Nonnull String predicted, @Nonnull String commandLine) {
        if (!optionProcessed) {
            CommandLine cl = parseOptions(commandLine);
            processOptions(cl);
            optionProcessed = true;
        }

        actual = actual.trim();
        predicted = predicted.trim();

        if (getLabelIndex(actual) == -1) {
            labels.add(actual);
            confusionMatrix.put(actual, new ConcurrentHashMap<>());
        }
        if (getLabelIndex(predicted) == -1) {
            labels.add(predicted);
            confusionMatrix.put(predicted, new ConcurrentHashMap<>());
        }

        ConcurrentHashMap<String, Integer> counts = confusionMatrix.get(actual);
        counts.put(predicted, counts.getOrDefault(predicted, 0) + 1);
        confusionMatrix.put(actual, counts);
    }

    @Override
    public Object output() {
        return PrettyPrinter.printIntegerMatrix(labels, labels, confusionMatrix);
    }

    @Override
    public void reset() {
        labels.clear();
        confusionMatrix.clear();
    }

    @Override
    public void showHelp(Options opts) {
        // TODO: complete help message of confusion matrix.
    }

    private int getLabelIndex(String label) {
        for (int i = 0; i < labels.size(); i++) {
            if (label.equals(labels.get(i))) {
                return i;
            }
        }

        return -1;
    }
}
