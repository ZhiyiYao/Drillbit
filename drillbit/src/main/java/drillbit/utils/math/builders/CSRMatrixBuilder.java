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

import drillbit.utils.math.CSRMatrix;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Compressed Sparse Row Matrix builder.
 */
public final class CSRMatrixBuilder extends MatrixBuilder {

    private final boolean sortRequired;

    @Nonnull
    private final ArrayList<Integer> rowPointers;
    @Nonnull
    private final ArrayList<Integer> columnIndices;
    @Nonnull
    private final ArrayList<Double> values;

    @Nonnull
    private final List<ColValue> colCache;

    private int maxNumColumns;

    public CSRMatrixBuilder(@Nonnegative int initSize) {
        this(initSize, true);
    }

    public CSRMatrixBuilder(@Nonnegative int initSize, boolean sortRequired) {
        super();
        this.sortRequired = sortRequired;
        this.rowPointers = new ArrayList<>(initSize + 1);
        rowPointers.add(0);
        this.columnIndices = new ArrayList<>(initSize);
        this.values = new ArrayList<>(initSize);
        this.colCache = new ArrayList<>(32);
        this.maxNumColumns = 0;
    }

    @Override
    public CSRMatrixBuilder nextRow() {
        if (sortRequired) {
            Collections.sort(colCache);
        }
        for (ColValue e : colCache) {
            columnIndices.add(e.col);
            values.add(e.value);
        }
        colCache.clear();

        int ptr = values.size();
        rowPointers.add(ptr);
        return this;
    }

    @Override
    public CSRMatrixBuilder nextColumn(@Nonnegative int col, double value) {
        checkColIndex(col);

        this.maxNumColumns = Math.max(col + 1, maxNumColumns);
        if (value == 0.d) {
            return this;
        }

        colCache.add(new ColValue(col, value));
        return this;
    }

    @Override
    public CSRMatrix buildMatrix() {
        int[] rowPointerList = new int[rowPointers.size()];
        int[] columnIndexList = new int[columnIndices.size()];
        double[] valueList = new double[values.size()];

        for (int i = 0; i < rowPointers.size(); i++) {
            rowPointerList[i] = rowPointers.get(i);
        }

        for (int i = 0; i < columnIndices.size(); i++) {
            columnIndexList[i] = columnIndices.get(i);
        }

        for (int i = 0; i < values.size(); i++) {
            valueList[i] = values.get(i);
        }

        CSRMatrix matrix = new CSRMatrix(rowPointerList, columnIndexList, valueList, maxNumColumns);

        return matrix;
    }

    private static final class ColValue implements Comparable<ColValue> {
        final int col;
        final double value;

        ColValue(int col, double value) {
            this.col = col;
            this.value = value;
        }

        @Override
        public int compareTo(ColValue o) {
            return Integer.compare(col, o.col);
        }

        @Override
        public String toString() {
            return "[column=" + col + ", value=" + value + ']';
        }

    }

}
