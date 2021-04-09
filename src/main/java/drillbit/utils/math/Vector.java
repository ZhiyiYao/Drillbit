package drillbit.utils.math;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

public interface Vector {
    double get(@Nonnegative int index);

    double get(@Nonnegative int index, double defaultValue);

    void set(@Nonnegative int index, double value);

    void incr(@Nonnegative int index, double delta);

    void decr(@Nonnegative int index, double delta);

    public void each(@Nonnull VectorOperation operation);

    int size();

    void clear();

    @Nonnull
    double[] toArray();
}
