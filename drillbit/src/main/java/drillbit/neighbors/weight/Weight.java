package drillbit.neighbors.weight;

import javax.annotation.Nonnegative;

public interface Weight {
    double evaluate(@Nonnegative double distance);
}
