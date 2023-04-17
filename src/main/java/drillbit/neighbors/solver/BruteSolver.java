package drillbit.neighbors.solver;

import drillbit.neighbors.distance.Distance;
import drillbit.neighbors.utils.Counter;
import drillbit.neighbors.utils.Score;
import drillbit.neighbors.weight.Weight;
import drillbit.parameter.Coordinates;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class BruteSolver extends Solver {
    ArrayList<String> labels;
    ArrayList<Coordinates> coordinatesList;
    int nClasses;

    public BruteSolver(ConcurrentHashMap<String, String> options) {
        super(options);
    }

    @Override
    public ConcurrentHashMap<String, String> getOptions() {
        return super.getOptions();
    }

    @Override
    public void build(@Nonnull final ArrayList<String> labels, @Nonnull final ArrayList<Coordinates> coordinatesList) {
        this.labels = new ArrayList<>();
        this.labels.addAll(labels);
        this.coordinatesList = new ArrayList<>();
        this.coordinatesList.addAll(coordinatesList);
        nClasses = labels.size();
    }

    @Override
    public int solveIndex(int k, Distance metric, Weight weight, double[] vec) {
        ArrayList<Score> scores = solveNeighbors(k, metric, weight, vec);
        ArrayList<Counter> counters = new ArrayList<>();

        for (Score score : scores) {
            String label = score.getLabel();
            int index = getCounterIndex(counters, label);

            if (index == -1) {
                Counter counter = new Counter(label);
                counter.incr(weight.evaluate(score.getDistance()));
                counters.add(counter);
            } else {
                Counter counter = counters.get(index);
                counter.incr(weight.evaluate(score.getDistance()));
                counters.set(index, counter);
            }
        }
        counters.sort((c1, c2) -> c1.getCount() < c2.getCount() ? 1 : -1);

        return getLabelIndex(counters.get(0).getLabel());
    }

    @Override
    public String solveLabel(int k, Distance metric, Weight weight, double[] vec) {
        ArrayList<Score> scores = solveNeighbors(k, metric, weight, vec);
        ArrayList<Counter> counters = new ArrayList<>();

        for (Score score : scores) {
            String label = score.getLabel();
            int index = getCounterIndex(counters, label);

            if (index == -1) {
                Counter counter = new Counter(label);
                counter.incr(weight.evaluate(score.getDistance()));
                counters.add(counter);
            } else {
                Counter counter = counters.get(index);
                counter.incr(weight.evaluate(score.getDistance()));
                counters.set(index, counter);
            }
        }
        counters.sort((c1, c2) -> c1.getCount() < c2.getCount() ? 1 : -1);

        return counters.get(0).getLabel();
    }

    @Override
    public ArrayList<Score> solveNeighbors(int n, Distance metric, Weight weight, double[] vec) {
        ArrayList<Score> scores = new ArrayList<>();
        double maxDistance = 0;

        for (int i = 0; i < nClasses; i++) {
            String label = labels.get(i);
            Coordinates coordinates = coordinatesList.get(i);
            for (int j = 0; j < coordinates.size(); j++) {
                double distance = metric.evaluate(vec, coordinates.get(j));
                if (scores.size() < n) {
                    scores.add(new Score(label, distance, vec));
                    maxDistance = Math.max(distance, maxDistance);
                } else if (distance < maxDistance) {
                    scores.add(new Score(label, distance, vec));
                    scores.sort((s1, s2) -> s1.getDistance() > s2.getDistance() ? 1 : -1);
                    scores.remove(n);
                    maxDistance = scores.get(n - 1).getDistance();
                }
            }
        }

        return scores;
    }

    private int getCounterIndex(ArrayList<Counter> counters, String label) {
        for (int i = 0; i < counters.size(); i++) {
            if (counters.get(i).getLabel().equals(label)) {
                return i;
            }
        }
        return -1;
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
