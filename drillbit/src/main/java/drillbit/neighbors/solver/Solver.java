package drillbit.neighbors.solver;

import drillbit.neighbors.distance.Distance;
import drillbit.neighbors.utils.Score;
import drillbit.parameter.Coordinates;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public abstract class Solver {
    public Solver(ConcurrentHashMap<String, String> options) {

    }

    ConcurrentHashMap<String, String> getOptions() {
        return new ConcurrentHashMap<>();
    }

    public abstract void build(@Nonnull final ArrayList<String> labels, @Nonnull final ArrayList<Coordinates> coordinatesList);

    public abstract int solveIndex(int k, Distance metric, double[] vec);

    public abstract String solveLabel(int k, Distance metric, double[] vec);

    abstract ArrayList<Score> solveNeighbors(int k, Distance metric, double[] vec);
}
