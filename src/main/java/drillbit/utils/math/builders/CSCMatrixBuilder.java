/*
 * Copyright 2019 and onwards Makoto Yui
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package drillbit.utils.math.builders;

import drillbit.utils.math.CSCMatrix;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;

public final class CSCMatrixBuilder extends MatrixBuilder {

    @Nonnull
    private final ArrayList<Integer> rows;
    @Nonnull
    private final ArrayList<Integer> cols;
    @Nonnull
    private final ArrayList<Double> values;

    private int row;
    private int maxNumColumns;

    public CSCMatrixBuilder(int initSize) {
        super();
        this.rows = new ArrayList<>(row);
        this.cols = new ArrayList<>(maxNumColumns);
        this.values = new ArrayList<>();
        this.row = 0;
        this.maxNumColumns = 0;
    }

    @Override
    public CSCMatrixBuilder nextRow() {
        row++;
        return this;
    }

    @Override
    public CSCMatrixBuilder nextColumn(@Nonnegative final int col, final double value) {
        checkColIndex(col);

        rows.add(row);
        cols.add(col);
        values.add(value);
        this.maxNumColumns = Math.max(col + 1, maxNumColumns);
        return this;
    }

    @Override
    public CSCMatrix buildMatrix() {
        if (rows.isEmpty() || cols.isEmpty()) {
            throw new IllegalStateException("No element in the matrix");
        }

        final int[] columnIndices = new int[cols.size()];
        final int[] rowsIndices = new int[rows.size()];
        final double[] valuesArray = new double[values.size()];

        for (int i = 0; i < cols.size(); i++) {
            columnIndices[i] = cols.get(i);
        }

        for (int i = 0; i < rows.size(); i++) {
            rowsIndices[i] = rows.get(i);
        }

        for (int i = 0; i < values.size(); i++) {
            valuesArray[i] = values.get(i);
        }

        // convert to column major
        final int nnz = valuesArray.length;
        SortObj[] sortObjs = new SortObj[nnz];
        for (int i = 0; i < nnz; i++) {
            sortObjs[i] = new SortObj(columnIndices[i], rowsIndices[i], valuesArray[i]);
        }
        Arrays.sort(sortObjs);
        for (int i = 0; i < nnz; i++) {
            columnIndices[i] = sortObjs[i].columnIndex;
            rowsIndices[i] = sortObjs[i].rowsIndex;
            valuesArray[i] = sortObjs[i].value;
        }
        sortObjs = null;

        final int[] columnPointers = new int[maxNumColumns + 1];
        int prevCol = -1;
        for (int j = 0; j < columnIndices.length; j++) {
            int currCol = columnIndices[j];
            if (currCol != prevCol) {
                columnPointers[currCol] = j;
                prevCol = currCol;
            }
        }
        columnPointers[maxNumColumns] = nnz; // nnz

        return new CSCMatrix(columnPointers, rowsIndices, valuesArray, row, maxNumColumns);
    }

    private static final class SortObj implements Comparable<SortObj> {
        final int columnIndex;
        final int rowsIndex;
        final double value;

        SortObj(int columnIndex, int rowsIndex, double value) {
            this.columnIndex = columnIndex;
            this.rowsIndex = rowsIndex;
            this.value = value;
        }

        @Override
        public int compareTo(SortObj o) {
            return Integer.compare(columnIndex, o.columnIndex);
        }
    }

}
