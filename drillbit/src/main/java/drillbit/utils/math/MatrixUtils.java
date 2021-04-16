//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package drillbit.utils.math;

import drillbit.utils.common.Conditions;
import drillbit.utils.lang.mutable.MutableInt;
import drillbit.utils.math.builders.MatrixBuilder;

import java.util.Arrays;
import java.util.Comparator;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;


public final class MatrixUtils {
    private MatrixUtils() {
    }

    @Nonnull
    public static Matrix shuffle(@Nonnull Matrix m, @Nonnull int[] indices) {
        Conditions.checkArgument(m.numRows() <= indices.length, "m.numRow() `" + m.numRows() + "` MUST be equals to or less than |swapIndices| `" + indices.length + "`");
        final MatrixBuilder builder = m.builder();
        VectorOperation proc = new VectorOperation() {
            public void apply(int col, double value) {
                builder.nextColumn(col, value);
            }
        };

        for(int i = 0; i < indices.length; ++i) {
            int idx = indices[i];
            m.eachNonNullInRow(idx, proc);
            builder.nextRow();
        }

        return builder.buildMatrix();
    }

//    public static int whichMax(@Nonnull IntMatrix matrix, @Nonnegative int row) {
//        final MutableInt m = new MutableInt(-2147483648);
//        final MutableInt which = new MutableInt(-1);
//        matrix.eachInRow(row, new VectorOperation() {
//            public void apply(int i, int value) {
//                if (value > m.getValue()) {
//                    m.setValue(value);
//                    which.setValue(i);
//                }
//
//            }
//        }, false);
//        return which.getValue();
//    }

    @Nonnull
    public static CSRMatrix coo2csr(@Nonnull int[] rows, @Nonnull int[] cols, @Nonnull double[] data, @Nonnegative int numRows, @Nonnegative int numCols, boolean sortColumns) {
        int nnz = data.length;
        Conditions.checkArgument(rows.length == nnz);
        Conditions.checkArgument(cols.length == nnz);
        int[] rowPointers = new int[numRows + 1];
        int[] colIndices = new int[nnz];
        double[] values = new double[nnz];
        coo2csr(rows, cols, data, rowPointers, colIndices, values, numRows, numCols, nnz);
        if (sortColumns) {
            sortIndices(rowPointers, colIndices, values);
        }

        return new CSRMatrix(rowPointers, colIndices, values, numCols);
    }

    private static void coo2csr(@Nonnull int[] rows, @Nonnull int[] cols, @Nonnull double[] data, @Nonnull int[] rowPointers, @Nonnull int[] colIndices, @Nonnull double[] values, @Nonnegative int numRows, @Nonnegative int numCols, int nnz) {
        int i;
        for(i = 0; i < nnz; ++i) {
            ++rowPointers[rows[i]];
        }

        i = 0;

        int last;
        int tmp;
        for(last = 0; i < numRows; ++i) {
            tmp = rowPointers[i];
            rowPointers[i] = last;
            last += tmp;
        }

        rowPointers[numRows] = nnz;

        for(i = 0; i < nnz; ++i) {
            last = rows[i];
            tmp = rowPointers[last];
            colIndices[tmp] = cols[i];
            values[tmp] = data[i];
            int var10002 = rowPointers[last]++;
        }

        i = 0;

        for(last = 0; i <= numRows; ++i) {
            tmp = rowPointers[i];
            rowPointers[i] = last;
            last = tmp;
        }

    }

    private static void coo2csr(@Nonnull int[] rows, @Nonnull int[] cols, @Nonnull float[] data, @Nonnull int[] rowPointers, @Nonnull int[] colIndices, @Nonnull float[] values, @Nonnegative int numRows, @Nonnegative int numCols, int nnz) {
        int i;
        for(i = 0; i < nnz; ++i) {
            ++rowPointers[rows[i]];
        }

        i = 0;

        int last;
        int tmp;
        for(last = 0; i < numRows; ++i) {
            tmp = rowPointers[i];
            rowPointers[i] = last;
            last += tmp;
        }

        rowPointers[numRows] = nnz;

        for(i = 0; i < nnz; ++i) {
            last = rows[i];
            tmp = rowPointers[last];
            colIndices[tmp] = cols[i];
            values[tmp] = data[i];
            int var10002 = rowPointers[last]++;
        }

        i = 0;

        for(last = 0; i <= numRows; ++i) {
            tmp = rowPointers[i];
            rowPointers[i] = last;
            last = tmp;
        }

    }

    private static void sortIndices(@Nonnull int[] majorAxisPointers, @Nonnull int[] minorAxisIndices, @Nonnull double[] values) {
        int numRows = majorAxisPointers.length - 1;
        if (numRows > 1) {
            for(int i = 0; i < numRows; ++i) {
                int rowStart = majorAxisPointers[i];
                int rowEnd = majorAxisPointers[i + 1];
                int numCols = rowEnd - rowStart;
                if (numCols != 0) {
                    if (numCols < 0) {
                        throw new IllegalArgumentException("numCols SHOULD be greater than zero. numCols = rowEnd - rowStart = " + rowEnd + " - " + rowStart + " = " + numCols + " at i=" + i);
                    }

                    MatrixUtils.IntDoublePair[] pairs = new MatrixUtils.IntDoublePair[numCols];
                    int jj = rowStart;

                    int n;
                    for(n = 0; jj < rowEnd; ++n) {
                        pairs[n] = new MatrixUtils.IntDoublePair(minorAxisIndices[jj], values[jj]);
                        ++jj;
                    }

                    Arrays.sort(pairs, new Comparator<MatrixUtils.IntDoublePair>() {
                        public int compare(MatrixUtils.IntDoublePair x, MatrixUtils.IntDoublePair y) {
                            return Integer.compare(x.key, y.key);
                        }
                    });
                    jj = rowStart;

                    for(n = 0; jj < rowEnd; ++n) {
                        MatrixUtils.IntDoublePair tmp = pairs[n];
                        minorAxisIndices[jj] = tmp.key;
                        values[jj] = tmp.value;
                        ++jj;
                    }
                }
            }

        }
    }

    private static void sortIndices(@Nonnull int[] majorAxisPointers, @Nonnull int[] minorAxisIndices, @Nonnull float[] values) {
        int numRows = majorAxisPointers.length - 1;
        if (numRows > 1) {
            for(int i = 0; i < numRows; ++i) {
                int rowStart = majorAxisPointers[i];
                int rowEnd = majorAxisPointers[i + 1];
                int numCols = rowEnd - rowStart;
                if (numCols != 0) {
                    if (numCols < 0) {
                        throw new IllegalArgumentException("numCols SHOULD be greater than or equal to zero. numCols = rowEnd - rowStart = " + rowEnd + " - " + rowStart + " = " + numCols + " at i=" + i);
                    }

                    MatrixUtils.IntFloatPair[] pairs = new MatrixUtils.IntFloatPair[numCols];
                    int jj = rowStart;

                    int n;
                    for(n = 0; jj < rowEnd; ++n) {
                        pairs[n] = new MatrixUtils.IntFloatPair(minorAxisIndices[jj], values[jj]);
                        ++jj;
                    }

                    Arrays.sort(pairs, new Comparator<MatrixUtils.IntFloatPair>() {
                        public int compare(MatrixUtils.IntFloatPair x, MatrixUtils.IntFloatPair y) {
                            return Integer.compare(x.key, y.key);
                        }
                    });
                    jj = rowStart;

                    for(n = 0; jj < rowEnd; ++n) {
                        MatrixUtils.IntFloatPair tmp = pairs[n];
                        minorAxisIndices[jj] = tmp.key;
                        values[jj] = tmp.value;
                        ++jj;
                    }
                }
            }

        }
    }

    private static final class IntFloatPair {
        final int key;
        final float value;

        IntFloatPair(int key, float value) {
            this.key = key;
            this.value = value;
        }
    }

    private static final class IntDoublePair {
        final int key;
        final double value;

        IntDoublePair(int key, double value) {
            this.key = key;
            this.value = value;
        }
    }
}
