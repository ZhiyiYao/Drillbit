package drillbit.neighbors.weight;

import javax.annotation.Nonnull;
import java.util.concurrent.ConcurrentHashMap;

public final class WeightFactory {
    public static Weight create(@Nonnull final ConcurrentHashMap<String, String> options) {
        String weightName = options.get("weight");

        if ("uniform".equalsIgnoreCase(weightName)) {
            return new UniformWeight(options);
        } else if ("inverse".equals(weightName)) {
            return new InverseWeight(options);
        } else {
            throw new IllegalArgumentException("Unsupported weight: " + weightName);
        }
    }
}
