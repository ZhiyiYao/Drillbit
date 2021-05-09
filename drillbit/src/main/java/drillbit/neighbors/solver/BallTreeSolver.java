package drillbit.neighbors.solver;

import drillbit.neighbors.distance.Distance;
import drillbit.neighbors.utils.Score;
import drillbit.neighbors.weight.Weight;
import drillbit.parameter.Coordinates;
import drillbit.utils.parser.StringParser;
import org.apache.commons.cli.CommandLine;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class BallTreeSolver extends Solver {
    int leafSize;
    double p;

    BallTreeSolver(ConcurrentHashMap<String, String> options) {
        super(options);
        leafSize = StringParser.parseInt(options.get("leaf_size"), 10);
        p = StringParser.parseDouble(options.get("p"), 1.d);
    }

    @Override
    ConcurrentHashMap<String, String> getOptions() {
        ConcurrentHashMap<String, String> opts = super.getOptions();

        opts.put("leaf_size", "10");
        opts.put("p", "1");

        return opts;
    }

    @Override
    public void build(@Nonnull ArrayList<String> labels, @Nonnull ArrayList<Coordinates> coordinatesList) {
    }

    @Override
    public int solveIndex(int k, Distance metric, Weight weight, double[] vec) {
        return 0;
    }

    @Override
    public String solveLabel(int k, Distance metric, Weight weight, double[] vec) {
        return null;
    }

    @Override
    ArrayList<Score> solveNeighbors(int k, Distance metric, Weight weight, double[] vec) {
        return null;
    }
}
