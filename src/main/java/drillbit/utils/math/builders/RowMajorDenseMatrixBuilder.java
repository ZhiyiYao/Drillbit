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

import drillbit.utils.math.RowMajorDenseMatrix2d;
import drillbit.utils.math.SparseVector;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public final class RowMajorDenseMatrixBuilder extends MatrixBuilder {

    @Nonnull
    private final List<double[]> rows;
    @Nonnull
    private final SparseVector rowProbe;
    private int maxNumColumns;
    private int nnz;

    public RowMajorDenseMatrixBuilder(@Nonnegative int initSize) {
        super();
        this.rows = new ArrayList<>(initSize);
        this.maxNumColumns = 0;
        this.nnz = 0;
        this.rowProbe = new SparseVector();
    }

    @Override
    public RowMajorDenseMatrixBuilder nextColumn(@Nonnegative final int col, final double value) {
        checkColIndex(col);

        this.maxNumColumns = Math.max(col + 1, maxNumColumns);
        if (value == 0.d) {
            return this;
        }
        rowProbe.set(col, value);
        nnz++;
        return this;
    }

    @Override
    public RowMajorDenseMatrixBuilder nextRow() {
        double[] row = rowProbe.toArray();
        rowProbe.clear();
        rows.add(row);
        //this.maxNumColumns = Math.max(row.length, maxNumColumns);
        return this;
    }

    @Override
    public void nextRow(@Nonnull double[] row) {
        for (double v : row) {
            if (v != 0.d) {
                nnz++;
            }
        }
        rows.add(row);
        this.maxNumColumns = Math.max(row.length, maxNumColumns);
    }

    @Override
    public RowMajorDenseMatrix2d buildMatrix() {
        int numRows = rows.size();
        double[][] data = rows.toArray(new double[numRows][]);
        return new RowMajorDenseMatrix2d(data, maxNumColumns, nnz);
    }
}
