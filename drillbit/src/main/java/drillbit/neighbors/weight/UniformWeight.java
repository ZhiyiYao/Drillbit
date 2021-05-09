package drillbit.neighbors.weight;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.concurrent.ConcurrentHashMap;

public class UniformWeight extends Weight {
    public UniformWeight(@Nonnull final ConcurrentHashMap<String, String> options) {
        super(options);
    }

    @Override
    public double evaluate(@Nonnegative double distance) {
        return 1.d;
    }
}
