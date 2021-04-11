package drillbit.metrics;

import java.util.ArrayList;

public interface MetricHelper {
    void update(ArrayList<Double> label, ArrayList<Double> score, double threshold);

    void update(ArrayList<String> label, ArrayList<String> predicted, ArrayList<String> labels);

    void clear();
}
