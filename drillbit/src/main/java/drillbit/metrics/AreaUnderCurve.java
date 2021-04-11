package drillbit.metrics;

import drillbit.utils.math.MathUtils;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import java.util.ArrayList;
import javax.annotation.Nonnull;

public class AreaUnderCurve implements Metric {
    protected ArrayList<Double> labels;
    protected ArrayList<Double> scores;
    protected ArrayList<Point> curve;

    // For command line options
    boolean optionProcessed = false;

    // For computation of AUC
    private int batch;
    private CurveType curveType;
    private BinaryMetricHelper helper;

    public enum CurveType {
        ROC, PR
    }

    public AreaUnderCurve() {
        labels = new ArrayList<>();
        scores = new ArrayList<>();
        curve = new ArrayList<>();
        helper = new BinaryMetricHelper();
    }

    @Override
    public Options getOptions() {
        Options opts = new Options();

        opts.addOption("type", "curve_type", true, "type of curve");

        return opts;
    }

    @Override
    public CommandLine processOptions(@Nonnull CommandLine cl) {
        if (cl.hasOption("type")) {
            String typeString = cl.getOptionValue("type");
            if (typeString.equalsIgnoreCase("roc")) {
                curveType = CurveType.ROC;
            }
            else if (typeString.equalsIgnoreCase("pr")) {
                curveType = CurveType.PR;
            }
            else {
                throw new IllegalArgumentException(String.format("Invalid curve type of %s", typeString));
            }
        }
        else {
            curveType = CurveType.ROC;
        }

        return cl;
    }

    @Override
    public void add(double label, double score, @Nonnull final String commandLine) {
        if (!optionProcessed) {
            CommandLine cl = parseOptions(commandLine);
            processOptions(cl);
            optionProcessed = true;
        }
        labels.add(label);
        scores.add(score);
    }

    @Override
    public void add(@Nonnull String label, @Nonnull String predicted, @Nonnull final String commandLine) {
        throw new UnsupportedOperationException("String parameters are not valid in AUC computation.");
    }

    @Override
    public Object output() {
        computeBatch();
        plotCurve();
        return auc();
    }

    @Override
    public void showHelp(Options opts) {
        //TODO: implement show help here
    }

    @Override
    public void reset() {
        scores.clear();
        labels.clear();
        curve.clear();
    }

    public double auc() {
        double auc = curve.get(0).getX() * (curve.get(0).getY() / 2) + (1 - curve.get(batch - 1).getX()) * (curve.get(batch - 1).getY() + 1) / 2;

        for (int i = 0; i < batch - 1; i++) {
            auc += (-curve.get(i).getX() + curve.get(i + 1).getX()) * (curve.get(i).getY() + curve.get(i + 1).getY()) / 2;
        }

        return auc;
    }

    // Make sure the integral point can cover all scores
    private void computeBatch() {
        double intv = MathUtils.minInterval(scores);
        batch = (int) Math.ceil(1 / intv) + 1;
    }

    private void plotCurve() {
        curve.clear();
        if (curveType == CurveType.ROC) {
            for (int i = 0; i < batch; i++) {
                helper.update(labels, scores, (double) (i + 1) / (batch + 2));
                curve.add(new Point(helper.ftp(), helper.rtp()));
                helper.clear();
            }
        }
        else {
            for (int i = 0; i < batch; i++) {
                helper.update(labels, scores, (double) (i + 1) / (batch + 2));
                curve.add(new Point(helper.recall(), helper.precision()));
                helper.clear();
            }
        }

        curve.sort((p1, p2) -> p1.getX() < p2.getY() ? 1 : 0);
    }

    public static class Point {
        private double x;
        private double y;

        public Point(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public double getX() {
            return x;
        }

        public void setX(double x) {
            this.x = x;
        }

        public double getY() {
            return y;
        }

        public void setY(double y) {
            this.y = y;
        }
    }

}
