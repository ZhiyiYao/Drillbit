package drillbit.utils.random;

import javax.annotation.Nonnegative;

/**
 * @link https://en.wikipedia.org/wiki/Pseudorandom_number_generator
 */
public interface PRNG {

    /**
     * Returns a random integer in [0, n).
     */
    int nextInt(@Nonnegative int n);

    int nextInt();

    long nextLong();

    double nextDouble();

}

