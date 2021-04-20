package drillbit.neighbors.weight;

import javax.annotation.Nonnegative;

public class UniformWeight implements Weight {
    @Override
    public double evaluate(@Nonnegative double distance) {
        return 1.d;
    }
}
