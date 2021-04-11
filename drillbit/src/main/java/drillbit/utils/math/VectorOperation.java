package drillbit.utils.math;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

public abstract class VectorOperation {

    public VectorOperation() {}

    public void apply(@Nonnegative int i, @Nonnegative int j, float value) {
        apply(i, j, (double) value);
    }

    public void apply(@Nonnegative int i, @Nonnegative int j, double value) {}

    public void apply(@Nonnegative int i, float value) {
        apply(i, (double) value);
    }

    public void apply(@Nonnegative int i, double value) {}

    public void apply(@Nonnegative int i, int value) {}

    public void apply(@Nonnegative int i) {}

    public void apply(@Nonnegative int i, @Nonnull int[] values) {}

}

