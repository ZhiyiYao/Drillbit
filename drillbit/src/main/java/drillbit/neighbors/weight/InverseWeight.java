package drillbit.neighbors.weight;

import javax.annotation.Nonnegative;

public class InverseWeight implements Weight {
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
