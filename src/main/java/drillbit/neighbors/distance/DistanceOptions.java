package drillbit.neighbors.distance;

import org.apache.commons.cli.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class DistanceOptions {
    @Nonnull
    public static ConcurrentHashMap<String, String> create() {
        ConcurrentHashMap<String, String> opts = new ConcurrentHashMap<String, String>();

        opts.put("distance", "euclidean");

        return opts;
    }

    public static void setup(@Nonnull Options opts) {
        opts.addOption("distance", true, "Distance metric used to compute the distance");
        opts.addOption("p", true, "P used to compute p-norm");
    }

    public static String optionsToString(@Nonnull final ConcurrentHashMap<String, String> options) {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String> entry : options.entrySet()) {
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

        ConcurrentHashMap<String, String> distanceOptions = create();
        processOptions(cl, distanceOptions);

        return distanceOptions;
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
