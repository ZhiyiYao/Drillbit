package drillbit.neighbors.weight;

import javax.annotation.Nonnull;

public final class WeightFactory {
    public static Weight getWeight(@Nonnull final String name) {
        if ("uniform".equalsIgnoreCase(name)) {
            return new UniformWeight();
        }
        else if ("inverse".equals(name)) {
            return new InverseWeight();
        }
        else {
            throw new IllegalArgumentException("Unsupported weight: " + name);
        }
    }
}
