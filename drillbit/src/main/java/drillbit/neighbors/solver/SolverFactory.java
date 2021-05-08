package drillbit.neighbors.solver;

import javax.annotation.Nonnull;
import java.util.concurrent.ConcurrentHashMap;

public class SolverFactory {
    public static Solver create(@Nonnull final ConcurrentHashMap<String, String> options) {
        String solverName = options.get("solver");
        if ("brute".equalsIgnoreCase(solverName)) {
            return new BruteSolver(options);
        }
        else if ("kdtree".equalsIgnoreCase(solverName)) {
            return new KDTreeSolver(options);
        }
        else if ("balltree".equalsIgnoreCase(solverName)) {
            return new BallTreeSolver(options);
        }
        else {
            throw new IllegalArgumentException("Unsupported solver: " + solverName);
        }
    }
}
