package drillbit.tree.utils;

import drillbit.tree.DecisionTree;
import drillbit.utils.algorithm.QuickSort;
import drillbit.utils.collections.arrays.SparseIntArray;
import drillbit.utils.collections.lists.DoubleArrayList;
import drillbit.utils.collections.lists.IntArrayList;
import drillbit.utils.common.Conditions;
import drillbit.utils.math.*;
import drillbit.utils.parser.ObjectParser;
import drillbit.utils.random.PRNG;
import drillbit.utils.random.RandomNumberGeneratorFactory;
import org.roaringbitmap.RoaringBitmap;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;

public class TreeUtils {
    public static final byte NUMERIC = (byte) 1;

    public static final byte NOMINAL = (byte) 2;

    @Nonnull
    public static RoaringBitmap resolveAttributes(@Nullable final String opt)
            throws IllegalArgumentException {
        final RoaringBitmap attr = new RoaringBitmap();
        if (opt == null) {
            return attr;
        }
        final String[] opts = opt.split(",");
        final int size = opts.length;
        for (int i = 0; i < size; i++) {
            final String type = opts[i];
            if ("C".equals(type)) {
                attr.add(i);
            } else {
                throw new IllegalArgumentException("Unsupported attribute type: " + type);
            }
        }
        return attr;
    }

    /**
     * @param opt comma separated list of zero-start indexes
     */
    @Nonnull
    public static RoaringBitmap parseNominalAttributeIndicies(@Nullable final String opt) throws IllegalArgumentException {
        final RoaringBitmap attr = new RoaringBitmap();
        if (opt == null) {
            return attr;
        }
        for (String s : opt.split(",")) {
            if (MathUtils.isDigits(s)) {
                int index = ObjectParser.parseInt(s);
                attr.add(index);
            } else {
                throw new IllegalArgumentException("Expected integer but got " + s);
            }
        }
        return attr;
    }

    @Nonnull
    public static VariableOrder sort(@Nonnull final RoaringBitmap nominalAttrs,
                                     @Nonnull final Matrix x, @Nonnull final int[] samples) {
        final int n = x.numRows();
        final int p = x.numColumns();

        final SparseIntArray[] index = new SparseIntArray[p];
        if (x.isSparse()) {
            int initSize = n / 10;
            final DoubleArrayList dlist = new DoubleArrayList(initSize);
            final IntArrayList ilist = new IntArrayList(initSize);
            final VectorOperation proc = new VectorOperation() {
                @Override
                public void apply(final int i, final double v) {
                    if (samples[i] == 0) {
                        return;
                    }
                    dlist.add(v);
                    ilist.add(i);
                }
            };

            final ColumnMajorMatrix x2 = x.toColumnMajorMatrix();
            for (int j = 0; j < p; j++) {
                if (nominalAttrs.contains(j)) {
                    continue; // nop for categorical columns
                }
                // sort only numerical columns
                x2.eachNonNullInColumn(j, proc);
                if (ilist.isEmpty()) {
                    continue;
                }
                int[] rowPtrs = ilist.toArray();
                QuickSort.sort(dlist.array(), rowPtrs, rowPtrs.length);
                index[j] = new SparseIntArray(rowPtrs);
                dlist.clear();
                ilist.clear();
            }
        } else {
            final DoubleArrayList dlist = new DoubleArrayList(n);
            final IntArrayList ilist = new IntArrayList(n);
            for (int j = 0; j < p; j++) {
                if (nominalAttrs.contains(j)) {
                    continue; // nop for categorical columns
                }
                // sort only numerical columns
                for (int i = 0; i < n; i++) {
                    if (samples[i] == 0) {
                        continue;
                    }
                    double x_ij = x.get(i, j);
                    dlist.add(x_ij);
                    ilist.add(i);
                }
                if (ilist.isEmpty()) {
                    continue;
                }
                int[] rowPtrs = ilist.toArray();
                QuickSort.sort(dlist.array(), rowPtrs, rowPtrs.length);
                index[j] = new SparseIntArray(rowPtrs);
                dlist.clear();
                ilist.clear();
            }
        }

        return new VariableOrder(index);
    }

    @Nonnull
    public static int[] classLabels(@Nonnull final int[] y) throws IllegalArgumentException {
        final int[] labels = MathUtils.unique(y);
        Arrays.sort(labels);

        if (labels.length < 2) {
            throw new IllegalArgumentException("Only one class.");
        }
        for (int i = 0; i < labels.length; i++) {
            if (labels[i] < 0) {
                throw new IllegalArgumentException("Negative class label: " + labels[i]);
            }
            if (i > 0 && (labels[i] - labels[i - 1]) > 1) {
                throw new IllegalArgumentException("Missing class: " + (labels[i - 1] + 1));
            }
        }

        return labels;
    }

    @Nonnull
    public static DecisionTree.SplitRule resolveSplitRule(@Nullable String ruleName) {
        if ("gini".equalsIgnoreCase(ruleName)) {
            return DecisionTree.SplitRule.GINI;
        } else if ("entropy".equalsIgnoreCase(ruleName)) {
            return DecisionTree.SplitRule.ENTROPY;
        } else if ("classification_error".equalsIgnoreCase(ruleName)) {
            return DecisionTree.SplitRule.CLASSIFICATION_ERROR;
        } else {
            return DecisionTree.SplitRule.GINI;
        }
    }

    public static int computeNumInputVars(final float numVars, @Nonnull final Matrix x) {
        final int numInputVars;
        if (numVars <= 0.f) {
            int dims = x.numColumns();
            numInputVars = (int) Math.ceil(Math.sqrt(dims));
        } else if (numVars > 0.f && numVars <= 1.f) {
            numInputVars = (int) (numVars * x.numColumns());
        } else {
            numInputVars = (int) numVars;
        }
        return numInputVars;
    }

    public static long generateSeed() {
        return Thread.currentThread().getId() * System.nanoTime();
    }

    public static void shuffle(@Nonnull final int[] x, @Nonnull final PRNG rnd) {
        for (int i = x.length; i > 1; i--) {
            int j = rnd.nextInt(i);
            swap(x, i - 1, j);
        }
    }

    @Nonnull
    public static Matrix shuffle(@Nonnull final Matrix x, @Nonnull final int[] y, long seed) {
        final int numRows = x.numRows();
        if (numRows != y.length) {
            throw new IllegalArgumentException(
                    "x.length (" + numRows + ") != y.length (" + y.length + ')');
        }
        if (seed == -1L) {
            seed = generateSeed();
        }

        final PRNG rnd = RandomNumberGeneratorFactory.createPRNG(seed);
        if (x.swappable()) {
            for (int i = numRows; i > 1; i--) {
                int j = rnd.nextInt(i);
                int k = i - 1;
                x.swap(k, j);
                swap(y, k, j);
            }
            return x;
        } else {
            final int[] indices = MathUtils.permutation(numRows);
            for (int i = numRows; i > 1; i--) {
                int j = rnd.nextInt(i);
                int k = i - 1;
                swap(indices, k, j);
                swap(y, k, j);
            }
            return MatrixUtils.shuffle(x, indices);
        }
    }

    @Nonnull
    public static Matrix shuffle(@Nonnull final Matrix x, @Nonnull final double[] y,
                                 @Nonnull long seed) {
        final int numRows = x.numRows();
        if (numRows != y.length) {
            throw new IllegalArgumentException(
                    "x.length (" + numRows + ") != y.length (" + y.length + ')');
        }
        if (seed == -1L) {
            seed = generateSeed();
        }

        final PRNG rnd = RandomNumberGeneratorFactory.createPRNG(seed);
        if (x.swappable()) {
            for (int i = numRows; i > 1; i--) {
                int j = rnd.nextInt(i);
                int k = i - 1;
                x.swap(k, j);
                swap(y, k, j);
            }
            return x;
        } else {
            final int[] indices = MathUtils.permutation(numRows);
            for (int i = numRows; i > 1; i--) {
                int j = rnd.nextInt(i);
                int k = i - 1;
                swap(indices, k, j);
                swap(y, k, j);
            }
            return MatrixUtils.shuffle(x, indices);
        }
    }

    /**
     * Swap two elements of an array.
     */
    private static void swap(final int[] x, final int i, final int j) {
        int s = x[i];
        x[i] = x[j];
        x[j] = s;
    }

    /**
     * Swap two elements of an array.
     */
    private static void swap(final double[] x, final int i, final int j) {
        double s = x[i];
        x[i] = x[j];
        x[j] = s;
    }

    public static boolean containsNumericType(@Nonnull final Matrix x, final RoaringBitmap attributes) {
        int numColumns = x.numColumns();
        int numCategoricalCols = attributes.getCardinality();
        return numColumns != numCategoricalCols; // contains at least one numerical column
    }

    @Nonnull
    public static String resolveFeatureName(final int index, @Nullable final String[] names) {
        if (names == null) {
            return "feature#" + index;
        }
        if (index >= names.length) {
            return "feature#" + index;
        }
        return names[index];
    }

    @Nonnull
    public static String resolveName(final int index, @Nullable final String[] names) {
        if (names == null) {
            return String.valueOf(index);
        }
        if (index >= names.length) {
            return String.valueOf(index);
        }
        return names[index];
    }

    /**
     * Generates an evenly distributed range of hue values in the HSV color scale.
     *
     * @return colors
     */
    public static double[] getColorBrew(@Nonnegative int n) {
        Conditions.checkArgument(n >= 1);

        final double hue_step = 360.d / n;

        final double[] colors = new double[n];
        for (int i = 0; i < n; i++) {
            colors[i] = i * hue_step / 360.d;
        }
        return colors;
    }

}
