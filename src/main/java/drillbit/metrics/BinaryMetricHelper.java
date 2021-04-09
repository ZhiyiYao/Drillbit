package drillbit.metrics;

import java.util.ArrayList;

public class BinaryMetricHelper implements MetricHelper {
    private int fn, tn, fp, tp;

    private String trueLabel;
    private String falseLabel;

    public void update(ArrayList<Double> labels, ArrayList<Double> scores, double threshold) {
        for (int i = 0; i < scores.size(); i++) {
            double label = labels.get(i);
            double score = scores.get(i);

            tp += label == 1 && score > threshold ? 1 : 0;
            tn += label == 1 && score <= threshold ? 1 : 0;
            fp += label != 1 && score > threshold ? 1 : 0;
            fn = label != 1 && score <= threshold ? 1 : 0;
        }
    }

    @Override
    public void update(ArrayList<String> label, ArrayList<String> predicted, ArrayList<String> labels) {
        throw new UnsupportedOperationException("");
    }

    public double ftp() {
        return (double) fp / (fp + tn);
    }

    public double rtp() {
        return (double) tp / (tp + fn);
    }

    public double precision() {
        return (double) tp / (tp + fp);
    }

    public double recall() {
        return (double) tp / (tp + fn);
    }

    public void clear() {
        fn = 0;
        tn = 0;
        fp = 0;
        tp = 0;
    }
}
