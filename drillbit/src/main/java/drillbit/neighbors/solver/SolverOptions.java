package drillbit.neighbors.solver;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

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
        opts.addOption("p", true, "");
    }

    public static void processOptions(@Nullable CommandLine cl, @Nonnull ConcurrentHashMap<String, String> options) {
        if (cl == null) {
            return;
        }

        for (Option opt : cl.getOptions()) {
            String optName = opt.getLongOpt();
            if (optName == null) {
                optName = opt.getOpt();
            }
            if (opt.getValue() != null) {
                options.put(optName, opt.getValue());
            }
        }
    }
}
