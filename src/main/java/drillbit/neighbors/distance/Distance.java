package drillbit.neighbors.distance;

import javax.annotation.Nonnull;
import java.util.concurrent.ConcurrentHashMap;

public abstract class Distance {
    public Distance(@Nonnull final ConcurrentHashMap<String, String> options) {
    }

    public abstract double evaluate(double[] vec1, double[] vec2);
}
