package drillbit.neighbors.weight;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.concurrent.ConcurrentHashMap;

public class InverseWeight extends Weight {
    public InverseWeight(@Nonnull final ConcurrentHashMap<String, String> options) {
        super(options);
    }

    @Override
    public double evaluate(@Nonnegative double distance) {
        if (distance != 0.d) {
            return 1 / distance;
        }
        else {
            return Double.MAX_VALUE;
        }
    }
}
