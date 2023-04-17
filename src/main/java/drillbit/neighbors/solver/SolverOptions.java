package drillbit.neighbors.solver;

import org.apache.commons.cli.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.ConcurrentHashMap;

public final class SolverOptions {
    @Nonnull
    public static ConcurrentHashMap<String, String> create() {
        ConcurrentHashMap<String, String> opts = new ConcurrentHashMap<String, String>();

        opts.put("solver", "kdtree");

        return opts;
    }

    public static void setup(@Nonnull Options opts) {
        opts.addOption("solver", true, "Solver used to solve n neighbors problem");

        // Leaf size for KDTree and BallTree
        opts.addOption("leaf_size", true, "Size of leaf nodes");
    }

    public static String optionsToString(@Nonnull final ConcurrentHashMap<String, String> options) {
        StringBuilder builder = new StringBuilder();
        for (ConcurrentHashMap.Entry<String, String> entry : options.entrySet()) {
            builder.append(" -");
            builder.append(entry.getKey());
            builder.append(" ");
            builder.append(entry.getValue());
        }

        return builder.toString().trim();
    }

    public static ConcurrentHashMap<String, String> parse(@Nonnull final String optionValue) {
        String[] args = optionValue.split("\\s+");

        Options opts = new Options();
        setup(opts);

        CommandLine cl;
        try {
            DefaultParser parser = new DefaultParser();
            cl = parser.parse(opts, args);
        } catch (IllegalArgumentException | ParseException e) {
            throw new IllegalArgumentException(e);
        }

        ConcurrentHashMap<String, String> solverOptions = create();
        processOptions(cl, solverOptions);

        return solverOptions;
    }

    public static void processOptions(@Nullable CommandLine cl, @Nonnull ConcurrentHashMap<String, String> options) {
        if (cl == null) {
            return;
        }

        ConcurrentHashMap<String, String> opts = create();
        for (Option opt : cl.getOptions()) {
            String optName = opt.getLongOpt();
            if (optName == null) {
                optName = opt.getOpt();
            }
            if (opts.containsKey(optName) && opt.getValue() != null) {
                options.put(optName, opt.getValue());
            }
        }
    }
}
