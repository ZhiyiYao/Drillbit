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

import drillbit.utils.math.ColumnMajorDenseMatrix2d;
import drillbit.utils.math.SparseVector;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.Map;

public final class ColumnMajorDenseMatrixBuilder extends MatrixBuilder {

    @Nonnull
    private final Int2ObjectMap<SparseVector> col2rows;
    private int row;
    private int maxNumColumns;
    private int nnz;

    public ColumnMajorDenseMatrixBuilder(int initSize) {
        this.col2rows = new Int2ObjectOpenHashMap<SparseVector>(initSize);
        this.row = 0;
        this.maxNumColumns = 0;
        this.nnz = 0;
    }

    @Override
    public ColumnMajorDenseMatrixBuilder nextRow() {
        row++;
        return this;
    }

    @Override
    public ColumnMajorDenseMatrixBuilder nextColumn(@Nonnegative final int col,
            final double value) {
        checkColIndex(col);

        this.maxNumColumns = Math.max(col + 1, maxNumColumns);
        if (value == 0.d) {
            return this;
        }

        SparseVector rows = col2rows.get(col);
        if (rows == null) {
            rows = new SparseVector(4);
            col2rows.put(col, rows);
        }
        rows.set(row, value);
        nnz++;
        return this;
    }

    @Override
    public ColumnMajorDenseMatrix2d buildMatrix() {
        final double[][] data = new double[maxNumColumns][];

        for (Map.Entry<Integer, SparseVector> e : col2rows.entrySet()) {
            int col = e.getKey();
            SparseVector rows = e.getValue();
            data[col] = rows.toArray();
        }

        return new ColumnMajorDenseMatrix2d(data, row, nnz);
    }

}
