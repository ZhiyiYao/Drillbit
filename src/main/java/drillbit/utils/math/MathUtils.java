package drillbit.utils.math;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

public class MathUtils {

    private static final double LOG2 = java.lang.Math.log(2);

    public static int nextPow2(@Nonnull int index) {
        int bits = 0;
        while (index != 0) {
            bits++;
            index >>= 1;
        }
        return (1 << bits);
    }

    public static double sigmoid(@Nonnull final double d) {
        return d;
    }

    public static ArrayList<Double> softmax(@Nonnull final ArrayList<Double> ds) {
        double sumExp = 0;
        ArrayList<Double> results = new ArrayList<>();
        for (Double d : ds) {
            double result = Math.exp(Math.max(Math.min(d, 23.d), -23.d));
            results.add(result);
            sumExp += result;
        }

        for (int i = 0; i < results.size(); i++) {
            results.set(i, results.get(i) / sumExp);
        }

        return results;
    }

    public static double logistic(@Nonnull final double d) {
        double d1 = Math.max(Math.min(d, 23.d), -23.d);
        return 1.d / (1.d + Math.exp(-d1));
    }

    public static double square(final double d) {
        return d * d;
    }

    public static double clip(final double v, final double min, final double max) {
        return Math.max(Math.min(v, max), min);
    }

    @Nonnull
    public static int[] permutation(@Nonnegative final int start, @Nonnegative final int size) {
        final int[] perm = new int[size];
        for (int i = 0; i < size; i++) {
            perm[i] = start + i;
        }
        return perm;
    }

    @Nonnull
    public static int[] permutation(@Nonnegative final int size) {
        final int[] perm = new int[size];
        for (int i = 0; i < size; i++) {
            perm[i] = i;
        }
        return perm;
    }

    public static int bitsRequired(int value) {
        int bits = 0;
        while (value != 0) {
            bits++;
            value >>= 1;
        }
        return bits;
    }

    public static int whichMax(double... x) {
        double m = Double.NEGATIVE_INFINITY;
        int which = 0;

        for (int i = 0; i < x.length; i++) {
            if (x[i] > m) {
                m = x[i];
                which = i;
            }
        }

        return which;
    }

    public static int whichMax(ArrayList<Double> x) {
        double m = Double.NEGATIVE_INFINITY;
        int which = 0;

        for (int i = 0; i < x.size(); i++) {
            double xi = x.get(i);
            if (xi > m) {
                m = xi;
                which = i;
            }
        }

        return which;
    }

    public static int whichMax(int... x) {
        int m = x[0];

        for (int n : x) {
            if (n > m) {
                m = n;
            }
        }

        return m;
    }

    public static double log2(double x) {
        return Math.log(x) / LOG2;
    }

    public static double probit(double p, double range) {
        if (range <= 0) {
            throw new IllegalArgumentException("range must be > 0: " + range);
        }
        if (p == 0) {
            return -range;
        }
        if (p == 1) {
            return range;
        }
        double v = probit(p);
        if (v < 0) {
            return Math.max(v, -range);
        } else {
            return Math.min(v, range);
        }
    }

    public static double probit(double p) {
        if (p < 0 || p > 1) {
            throw new IllegalArgumentException("p must be in [0,1]");
        }
        return Math.sqrt(2.d) * inverseErf(2.d * p - 1.d);
    }

    public static double inverseErf(final double x) {

        // beware that the logarithm argument must be
        // computed as (1.0 - x) * (1.0 + x),
        // it must NOT be simplified as 1.0 - x * x as this
        // would induce rounding errors near the boundaries +/-1
        double w = -Math.log((1.0 - x) * (1.0 + x));
        double p;

        if (w < 6.25) {
            w = w - 3.125;
            p = -3.6444120640178196996e-21;
            p = -1.685059138182016589e-19 + p * w;
            p = 1.2858480715256400167e-18 + p * w;
            p = 1.115787767802518096e-17 + p * w;
            p = -1.333171662854620906e-16 + p * w;
            p = 2.0972767875968561637e-17 + p * w;
            p = 6.6376381343583238325e-15 + p * w;
            p = -4.0545662729752068639e-14 + p * w;
            p = -8.1519341976054721522e-14 + p * w;
            p = 2.6335093153082322977e-12 + p * w;
            p = -1.2975133253453532498e-11 + p * w;
            p = -5.4154120542946279317e-11 + p * w;
            p = 1.051212273321532285e-09 + p * w;
            p = -4.1126339803469836976e-09 + p * w;
            p = -2.9070369957882005086e-08 + p * w;
            p = 4.2347877827932403518e-07 + p * w;
            p = -1.3654692000834678645e-06 + p * w;
            p = -1.3882523362786468719e-05 + p * w;
            p = 0.0001867342080340571352 + p * w;
            p = -0.00074070253416626697512 + p * w;
            p = -0.0060336708714301490533 + p * w;
            p = 0.24015818242558961693 + p * w;
            p = 1.6536545626831027356 + p * w;
        } else if (w < 16.0) {
            w = Math.sqrt(w) - 3.25;
            p = 2.2137376921775787049e-09;
            p = 9.0756561938885390979e-08 + p * w;
            p = -2.7517406297064545428e-07 + p * w;
            p = 1.8239629214389227755e-08 + p * w;
            p = 1.5027403968909827627e-06 + p * w;
            p = -4.013867526981545969e-06 + p * w;
            p = 2.9234449089955446044e-06 + p * w;
            p = 1.2475304481671778723e-05 + p * w;
            p = -4.7318229009055733981e-05 + p * w;
            p = 6.8284851459573175448e-05 + p * w;
            p = 2.4031110387097893999e-05 + p * w;
            p = -0.0003550375203628474796 + p * w;
            p = 0.00095328937973738049703 + p * w;
            p = -0.0016882755560235047313 + p * w;
            p = 0.0024914420961078508066 + p * w;
            p = -0.0037512085075692412107 + p * w;
            p = 0.005370914553590063617 + p * w;
            p = 1.0052589676941592334 + p * w;
            p = 3.0838856104922207635 + p * w;
        } else if (!Double.isInfinite(w)) {
            w = Math.sqrt(w) - 5.0;
            p = -2.7109920616438573243e-11;
            p = -2.5556418169965252055e-10 + p * w;
            p = 1.5076572693500548083e-09 + p * w;
            p = -3.7894654401267369937e-09 + p * w;
            p = 7.6157012080783393804e-09 + p * w;
            p = -1.4960026627149240478e-08 + p * w;
            p = 2.9147953450901080826e-08 + p * w;
            p = -6.7711997758452339498e-08 + p * w;
            p = 2.2900482228026654717e-07 + p * w;
            p = -9.9298272942317002539e-07 + p * w;
            p = 4.5260625972231537039e-06 + p * w;
            p = -1.9681778105531670567e-05 + p * w;
            p = 7.5995277030017761139e-05 + p * w;
            p = -0.00021503011930044477347 + p * w;
            p = -0.00013871931833623122026 + p * w;
            p = 1.0103004648645343977 + p * w;
            p = 4.8499064014085844221 + p * w;
        } else {
            // this branch does not appears in the original code, it
            // was added because the previous branch does not handle
            // x = +/-1 correctly. In this case, w is positive infinity
            // and as the first coefficient (-2.71e-11) is negative.
            // Once the first multiplication is done, p becomes negative
            // infinity and remains so throughout the polynomial evaluation.
            // So the branch above incorrectly returns negative infinity
            // instead of the correct positive infinity.
            p = Double.POSITIVE_INFINITY;
        }

        return p * x;
    }

    public static boolean isFinite(final double v) {
        return (v > Double.NEGATIVE_INFINITY) & (v < Double.POSITIVE_INFINITY);
    }

    public static boolean isFinite(final float v) {
        return (v > Float.NEGATIVE_INFINITY) & (v < Float.POSITIVE_INFINITY);
    }

    public static boolean isDigits(String str) {
        if (str == null || str.length() == 0) {
            return false;
        }
        for (int i = 0, len = str.length(); i < len; i++) {
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static double minInterval(ArrayList<Double> values) {
        List<Double> sortedValues = values.stream().sorted().collect(Collectors.toList());
        ArrayList<Double> intervals = new ArrayList<>();
        for (int i = 0; i < sortedValues.size() - 1; i++) {
            intervals.add(sortedValues.get(i + 1) - sortedValues.get(i));
        }

        return Collections.min(intervals);
    }

    public static double minInterval(double[] values) {
        ArrayList<Double> newValues = new ArrayList<>();
        for (double value : values) {
            newValues.add(value);
        }

        return minInterval(newValues);
    }

    public static int max(int... x) {
        int m = x[0];

        for (int n : x) {
            if (n > m) {
                m = n;
            }
        }

        return m;
    }

    public static int sum(int[] x) {
        double sum = 0.0;

        for (int n : x) {
            sum += n;
        }

        if (sum > Integer.MAX_VALUE || sum < -Integer.MAX_VALUE) {
            throw new ArithmeticException("Sum overflow: " + sum);
        }

        return (int) sum;
    }

    public static int[] unique(int[] x) {
        HashSet<Integer> hash = new HashSet<Integer>();
        for (int i = 0; i < x.length; i++) {
            hash.add(x[i]);
        }

        int[] y = new int[hash.size()];

        Iterator<Integer> keys = hash.iterator();
        for (int i = 0; i < y.length; i++) {
            y[i] = keys.next();
        }

        return y;
    }


    public static double sign(final double x) {
        if (x < 0.d) {
            return -1.d;
        } else if (x > 0.d) {
            return 1.d;
        }
        return 0; // 0 or NaN
    }
}
