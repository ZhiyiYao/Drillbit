package drillbit.neighbors.weight;

import javax.annotation.Nonnull;

public class WeightFactory {
    public static Weight getWeight(@Nonnull final String weight) {
        if ("uniform".equalsIgnoreCase(weight)) {
            return new UniformWeight();
        }
        else if ("inverse".equals(weight)) {
            return new InverseWeight();
        }
        else {
            throw new IllegalArgumentException("Unsupported weight: " + weight);
        }
    }
}
