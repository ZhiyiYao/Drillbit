package drillbit.neighbors.weight;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.concurrent.ConcurrentHashMap;

public abstract class Weight {
    public Weight (@Nonnull final ConcurrentHashMap<String, String> options) {
    }

    public abstract double evaluate(@Nonnegative double distance);
}
