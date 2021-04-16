package drillbit.tree;

import drillbit.parameter.Node;
import drillbit.tree.utils.TreeUtils;
import drillbit.tree.utils.VariableOrder;
import drillbit.utils.collections.arrays.SparseIntArray;
import drillbit.utils.collections.lists.IntArrayList;
import drillbit.utils.function.Consumer;
import drillbit.utils.function.IntPredicate;
import drillbit.utils.lang.ArrayUtils;
import drillbit.utils.lang.mutable.MutableInt;
import drillbit.utils.math.*;

import drillbit.utils.random.PRNG;
import drillbit.utils.random.RandomNumberGeneratorFactory;
import drillbit.utils.sampling.IntReservoirSampler;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import org.roaringbitmap.IntConsumer;
import org.roaringbitmap.RoaringBitmap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.PriorityQueue;

public final class DecisionTree {
    private static final Log logger = LogFactory.getLog(DecisionTree.class);

    /**
     * Training dataset.
     */
    @Nonnull
    private final Matrix X;
    /**
     * class labels.
     */
    @Nonnull
    private final int[] y;
    /**
     * The samples for training this node. Note that samples[i] is the number of sampling of
     * dataset[i]. 0 means that the datum is not included and values of greater than 1 are possible
     * because of sampling with replacement.
     */
    @Nonnull
    private final int[] samples;
    /**
     * An index of training values. Initially, order[j] is a set of indices that iterate through the
     * training values for attribute j in ascending order. During training, the array is rearranged
     * so that all values for each leaf node occupy a contiguous range, but within that range they
     * maintain the original ordering. Note that only numeric attributes will be sorted; non-numeric
     * attributes will have a null in the corresponding place in the array.
     */
    @Nonnull
    private final VariableOrder order;
    /**
     * An index that maps their current position in the {@link #order} to their original locations
     * in {@link #samples}.
     */
    @Nonnull
    private final int[] _sampleIndex;
    /**
     * The attributes of independent variable.
     */
    @Nonnull
    private final RoaringBitmap nominalAttributes;
    /**
     * Variable importance. Every time a split of a node is made on variable the (GINI, information
     * gain, etc.) impurity criterion for the two descendant nodes is less than the parent node.
     * Adding up the decreases for each individual variable over the tree gives a simple measure of
     * variable importance.
     */
    @Nonnull
    private final Vector importance;
    /**
     * The root of the regression tree
     */
    @Nonnull
    private final Node root;
    /**
     * The maximum number of the tree depth
     */
    private final int maxDepth;
    /**
     * The splitting rule.
     */
    @Nonnull
    private final SplitRule splitRule;
    /**
     * The number of classes.
     */
    private final int k;
    /**
     * The number of input variables to be used to determine the decision at a node of the tree.
     */
    private final int nVariable;
    /**
     * The number of instances in a node below which the tree will not split.
     */
    private final int minSamplesSplit;
    /**
     * The minimum number of samples in a leaf node.
     */
    private final int minSamplesLeaf;
    /**
     * The random number generator.
     */
    @Nonnull
    private final PRNG rnd;

    /**
     * The criterion to choose variable to split instances.
     */
    public enum SplitRule {
        /**
         * Used by the CART algorithm, Gini impurity is a measure of how often a randomly chosen
         * element from the set would be incorrectly labeled if it were randomly labeled according
         * to the distribution of labels in the subset. Gini impurity can be computed by summing the
         * probability of each item being chosen times the probability of a mistake in categorizing
         * that item. It reaches its minimum (zero) when all cases in the node fall into a single
         * target category.
         */
        GINI,
        /**
         * Used by the ID3, C4.5 and C5.0 tree generation algorithms.
         */
        ENTROPY,
        /**
         * Classification error.
         */
        CLASSIFICATION_ERROR
    }

    private final class TrainNode implements Comparable<TrainNode> {
         // The associated regression tree node.
        @Nonnull
        final Node node;

        // Depth of the node in the tree
        final int depth;

        // The lower bound (inclusive) in the order array of the samples belonging to this node.
        final int low;

        // The upper bound (exclusive) in the order array of the samples belonging to this node.
        final int high;

        // The number of samples
        final int samples;

        @Nullable
        int[] constFeatures;

        public TrainNode(@Nonnull Node node, int depth, int low, int high, int samples) {
            this(node, depth, low, high, samples, new int[0]);
        }

        public TrainNode(@Nonnull Node node, int depth, int low, int high, int samples,
                         @Nonnull int[] constFeatures) {
            if (low >= high) {
                throw new IllegalArgumentException(
                        "Unexpected condition was met. low=" + low + ", high=" + high);
            }
            this.node = node;
            this.depth = depth;
            this.low = low;
            this.high = high;
            this.samples = samples;
            this.constFeatures = constFeatures;
        }

        @Override
        public int compareTo(TrainNode a) {
            return (int) Math.signum(a.node.score - node.score);
        }

        /**
         * Finds the best attribute to split on at the current node.
         *
         * @return true if a split exists to reduce squared error, false otherwise.
         */
        public boolean findBestSplit() {
            // avoid split if tree depth is larger than threshold
            if (depth >= maxDepth) {
                return false;
            }
            // avoid split if the number of samples is less than threshold
            if (samples <= minSamplesSplit) {
                return false;
            }

            // Sample count in each class.
            final int[] count = new int[k];
            final boolean pure = countSamples(count);
            if (pure) {// if all instances have same label, stop splitting.
                return false;
            }

            final int[] constFeatures_ = this.constFeatures; // this.constFeatures may be replace in findBestSplit but it's accepted
            final double impurity = impurity(count, samples, splitRule);
            final int[] falseCount = new int[k];
            for (int varJ : variableIndex()) {
                if (ArrayUtils.contains(constFeatures_, varJ)) {
                    continue; // skip constant features
                }
                final Node split = findBestSplit(samples, count, falseCount, impurity, varJ);
                if (split.score > node.score) {
                    node.feature = split.feature;
                    node.feature = split.feature;
                    node.value = split.value;
                    node.score = split.score;
                }
            }

            return node.feature != -1;
        }

        @Nonnull
        private int[] variableIndex() {
            final Matrix xTemp = X;
            final IntReservoirSampler sampler = new IntReservoirSampler(nVariable, rnd.nextLong());
            if (xTemp.isSparse()) {
                // sample columns from sampled examples
                final RoaringBitmap cols = new RoaringBitmap();
                final VectorOperation proc = new VectorOperation() {
                    public void apply(final int col) {
                        cols.add(col);
                    }
                };
                final int[] sampleIndex = _sampleIndex;
                for (int i = low, end = high; i < end; i++) {
                    int row = sampleIndex[i];
                    assert (DecisionTree.this.samples[row] != 0) : row;
                    xTemp.eachColumnIndexInRow(row, proc);
                }
                cols.forEach(new IntConsumer() {
                    public void accept(final int k) {
                        sampler.add(k);
                    }
                });
            } else {
                final int ncols = xTemp.numColumns();
                for (int i = 0; i < ncols; i++) {
                    sampler.add(i);
                }
            }
            return sampler.getSample();
        }

        private boolean countSamples(@Nonnull final int[] count) {
            final int[] sampleIndex = _sampleIndex;
            final int[] samples = DecisionTree.this.samples;
            final int[] y = DecisionTree.this.y;

            boolean pure = true;

            for (int i = low, end = high, label = -1; i < end; i++) {
                int index = sampleIndex[i];
                int y_i = y[index];
                count[y_i] += samples[index];

                if (label == -1) {
                    label = y_i;
                } else if (y_i != label) {
                    pure = false;
                }
            }

            return pure;
        }

        /**
         * Finds the best split cutoff for attribute j at the current node.
         *
         * @param n the number instances in this node.
         * @param count the sample count in each class.
         * @param falseCount an array to store sample count in each class for false child node.
         * @param impurity the impurity of this node.
         * @param j the attribute index to split on.
         */
        private Node findBestSplit(final int n, final int[] count, final int[] falseCount,
                                   final double impurity, final int j) {
            final int[] samples = DecisionTree.this.samples;
            final int[] sampleIndex = _sampleIndex;
            final Matrix xTemp = X;
            final int[] y = DecisionTree.this.y;
            final int classes = k;

            final Node splitNode = new Node();

            if (nominalAttributes.contains(j)) {
                final Int2ObjectMap<int[]> trueCount = new Int2ObjectOpenHashMap<int[]>();

                int countNaN = 0;
                for (int i = low, end = high; i < end; i++) {
                    final int index = sampleIndex[i];
                    final int numSamples = samples[index];
                    if (numSamples == 0) {
                        continue;
                    }

                    final double v = xTemp.get(index, j, Double.NaN);
                    if (Double.isNaN(v)) {
                        countNaN++;
                        continue;
                    }
                    int x_ij = (int) v;

                    int[] tc_x = trueCount.get(x_ij);
                    if (tc_x == null) {
                        tc_x = new int[classes];
                        trueCount.put(x_ij, tc_x);
                    }
                    int y_i = y[index];
                    tc_x[y_i] += numSamples;
                }
                final int countDistinctX = trueCount.size() + (countNaN == 0 ? 0 : 1);
                if (countDistinctX <= 1) { // mark as a constant feature
                    this.constFeatures = ArrayUtils.sortedArraySet(constFeatures, j);
                }

                for (Int2ObjectMap.Entry<int[]> e : trueCount.int2ObjectEntrySet()) {
                    final int l = e.getIntKey();
                    final int[] trueCount_l = e.getValue();

                    final int tc = MathUtils.sum(trueCount_l);
                    final int fc = n - tc;

                    // skip splitting this feature.
                    if (tc < minSamplesSplit || fc < minSamplesSplit) {
                        continue;
                    }

                    for (int k = 0; k < classes; k++) {
                        falseCount[k] = count[k] - trueCount_l[k];
                    }

                    final double gain =
                            impurity - (double) tc / n * impurity(trueCount_l, tc, splitRule)
                                    - (double) fc / n * impurity(falseCount, fc, splitRule);

                    if (gain > splitNode.score) {
                        // new best split
                        splitNode.feature = j;
                        splitNode.numerical = false;
                        splitNode.value = l;
                        splitNode.score = gain;
                    }
                }
            } else {
                final int[] trueCount = new int[classes];
                final MutableInt countNaN = new MutableInt(0);
                final MutableInt replaceCount = new MutableInt(0);

                order.eachNonNullInColumn(j, low, high, new Consumer() {
                    double prevx = Double.NaN, lastx = Double.NaN;
                    int prevy = -1;

                    @Override
                    public void accept(int pos, final int i) {
                        final int numSamples = samples[i];
                        if (numSamples == 0) {
                            return;
                        }

                        final double x_ij = xTemp.get(i, j, Double.NaN);
                        if (Double.isNaN(x_ij)) {
                            countNaN.incr();
                            return;
                        }
                        if (lastx != x_ij) {
                            lastx = x_ij;
                            replaceCount.incr();
                        }

                        final int y_i = y[i];
                        if (Double.isNaN(prevx) || x_ij == prevx || y_i == prevy) {
                            prevx = x_ij;
                            prevy = y_i;
                            trueCount[y_i] += numSamples;
                            return;
                        }

                        final int tc = MathUtils.sum(trueCount);
                        final int fc = n - tc;

                        // skip splitting this feature.
                        if (tc < minSamplesSplit || fc < minSamplesSplit) {
                            prevx = x_ij;
                            prevy = y_i;
                            trueCount[y_i] += numSamples;
                            return;
                        }

                        for (int l = 0; l < classes; l++) {
                            falseCount[l] = count[l] - trueCount[l];
                        }

                        final double gain =
                                impurity - (double) tc / n * impurity(trueCount, tc, splitRule)
                                        - (double) fc / n * impurity(falseCount, fc, splitRule);

                        if (gain > splitNode.score) {
                            // new best split
                            splitNode.feature = j;
                            splitNode.numerical = true;
                            splitNode.value = (x_ij + prevx) / 2.d;
                            splitNode.score = gain;
                        }

                        prevx = x_ij;
                        prevy = y_i;
                        trueCount[y_i] += numSamples;
                    }//apply()
                });

                final int countDistinctX = replaceCount.get() + (countNaN.get() == 0 ? 0 : 1);
                if (countDistinctX <= 1) { // mark as a constant feature
                    this.constFeatures = ArrayUtils.sortedArraySet(constFeatures, j);
                }
            }

            return splitNode;
        }

        /**
         * Split the node into two children nodes. Returns true if split success.
         *
         * @return true if split occurred. false if the node is set to leaf.
         */
        public boolean split(@Nullable final PriorityQueue<TrainNode> nextSplits) {
            if (node.feature < 0) {
                throw new IllegalStateException("Split a node with invalid feature.");
            }

            final IntPredicate goesLeft = getPredicate();

            // split samples
            final int tc, fc, pivot;
            final double[] trueChildPosteriori = new double[k],
                    falseChildPosteriori = new double[k];
            {
                MutableInt tc_ = new MutableInt(0);
                MutableInt fc_ = new MutableInt(0);
                pivot = splitSamples(tc_, fc_, trueChildPosteriori, falseChildPosteriori, goesLeft);
                tc = tc_.get();
                fc = fc_.get();
            }

            if (tc < minSamplesLeaf || fc < minSamplesLeaf) {
                node.markAsLeafNode();
                return false;
            }

            for (int i = 0; i < k; i++) {
                trueChildPosteriori[i] /= tc; // divide by zero never happens
                falseChildPosteriori[i] /= fc;
            }

            partitionOrder(low, pivot, high, goesLeft);

            int leaves = 0;

            node.trueChild = new Node(trueChildPosteriori);
            TrainNode trueChild =
                    new TrainNode(node.trueChild, depth + 1, low, pivot, tc, constFeatures.clone());
            node.falseChild = new Node(falseChildPosteriori);
            TrainNode falseChild =
                    new TrainNode(node.falseChild, depth + 1, pivot, high, fc, constFeatures);
            this.constFeatures = null;

            if (tc >= minSamplesSplit && trueChild.findBestSplit()) {
                if (nextSplits != null) {
                    nextSplits.add(trueChild);
                } else {
                    if (trueChild.split(null) == false) {
                        leaves++;
                    }
                }
            } else {
                leaves++;
            }

            if (fc >= minSamplesSplit && falseChild.findBestSplit()) {
                if (nextSplits != null) {
                    nextSplits.add(falseChild);
                } else {
                    if (falseChild.split(null) == false) {
                        leaves++;
                    }
                }
            } else {
                leaves++;
            }

            // Prune meaningless branches
            if (leaves == 2) {// both left and right child are leaf node
                if (node.trueChild.output == node.falseChild.output) {// found a meaningless branch
                    node.markAsLeafNode();
                    return false;
                }
            }

            importance.incr(node.feature, node.score);
            if (nextSplits == null) {
                // For depth-first splitting, a posteriori is not needed for non-leaf nodes
                node.posteriori = null;
            }

            return true;
        }

        /**
         * @return Pivot to split samples
         */
        private int splitSamples(@Nonnull final MutableInt tc, @Nonnull final MutableInt fc,
                                 @Nonnull final double[] trueChildPosteriori,
                                 @Nonnull final double[] falseChildPosteriori,
                                 @Nonnull final IntPredicate goesLeft) {
            final int[] sampleIndex = _sampleIndex;
            final int[] samples = DecisionTree.this.samples;
            final int[] y = DecisionTree.this.y;

            int pivot = low;
            for (int k = low, end = high; k < end; k++) {
                final int i = sampleIndex[k];
                final int numSamples = samples[i];
                final int yi = y[i];
                if (goesLeft.test(i)) {
                    tc.addValue(numSamples);
                    trueChildPosteriori[yi] += numSamples;
                    pivot++;
                } else {
                    fc.addValue(numSamples);
                    falseChildPosteriori[yi] += numSamples;
                }
            }
            return pivot;
        }

        private void partitionOrder(final int low, final int pivot, final int high,
                                    @Nonnull final IntPredicate goesLeft) {
            final int[] buf = new int[high - pivot];
            order.eachRow(new Consumer() {
                @Override
                public void accept(int col, @Nonnull final SparseIntArray row) {
                    partitionArray(row, low, pivot, high, goesLeft, buf);
                }
            });
            partitionArray(_sampleIndex, low, pivot, high, goesLeft, buf);
        }

        @Nonnull
        private IntPredicate getPredicate() {
            if (node.numerical) {
                return new IntPredicate() {
                    @Override
                    public boolean test(int i) {
                        return X.get(i, node.feature, Double.NaN) <= node.value;
                    }
                };
            } else {
                return new IntPredicate() {
                    @Override
                    public boolean test(int i) {
                        return X.get(i, node.feature, Double.NaN) == node.value;
                    }
                };
            }
        }

    }

    public DecisionTree(@Nullable RoaringBitmap nominalAttrs, @Nonnull Matrix x, @Nonnull int[] y,
                        int numSamplesLeaf) {
        this(nominalAttrs, x, y, x.numColumns(), Integer.MAX_VALUE, numSamplesLeaf, 2, 1, null, SplitRule.GINI, null);
    }

    public DecisionTree(@Nullable RoaringBitmap nominalAttrs, @Nullable Matrix x, @Nullable int[] y,
                        int numSamplesLeaf, @Nullable PRNG rand) {
        this(nominalAttrs, x, y, x.numColumns(), Integer.MAX_VALUE, numSamplesLeaf, 2, 1, null, SplitRule.GINI, rand);
    }

    public DecisionTree(@Nonnull RoaringBitmap nominalAttributes, @Nonnull Matrix x, @Nonnull int[] y, int numVars, int maxDepth, int maxLeafNodes, int minSamplesSplit, int minSamplesLeaf, @Nullable int[] samples, @Nonnull SplitRule rule, @Nullable PRNG rand) {
        checkArgument(x, y, numVars, maxDepth, maxLeafNodes, minSamplesSplit, minSamplesLeaf);

        this.X = x;
        this.y = y;

        this.k = MathUtils.max(y) + 1;
        if (k < 2) {
            throw new IllegalArgumentException("Only one class or negative class labels.");
        }

        if (nominalAttributes == null) {
            nominalAttributes = new RoaringBitmap();
        }
        this.nominalAttributes = nominalAttributes;

        this.nVariable = numVars;
        this.maxDepth = maxDepth;
        // min_sample_leaf >= 2 is satisfied iff min_sample_split >= 4
        // So, split only happens when samples in intermediate nodes has >= 2 * min_sample_leaf nodes.
        if (minSamplesSplit < minSamplesLeaf * 2) {
            if (logger.isInfoEnabled()) {
                logger.info(String.format(
                        "min_sample_leaf = %d replaces min_sample_split = %d with min_sample_split = %d",
                        minSamplesLeaf, minSamplesSplit, minSamplesLeaf * 2));
            }
            minSamplesSplit = minSamplesLeaf * 2;
        }
        this.minSamplesSplit = minSamplesSplit;
        this.minSamplesLeaf = minSamplesLeaf;
        this.splitRule = rule;
        this.importance = x.isSparse() ? new SparseVector() : new DenseVector(x.numColumns());
        this.rnd = (rand == null) ? RandomNumberGeneratorFactory.createPRNG() : rand;

        final int n = y.length;
        final int[] count = new int[k];
        final int[] sampleIndex;
        int totalNumSamples = 0;
        if (samples == null) {
            samples = new int[n];
            sampleIndex = new int[n];
            for (int i = 0; i < n; i++) {
                samples[i] = 1;
                count[y[i]]++;
                sampleIndex[i] = i;
            }
            totalNumSamples = n;
        } else {
            final IntArrayList positions = new IntArrayList(n);
            for (int i = 0; i < n; i++) {
                final int sample = samples[i];
                if (sample != 0) {
                    count[y[i]] += sample;
                    positions.add(i);
                    totalNumSamples += sample;
                }
            }
            sampleIndex = positions.toArray(true);
        }
        this.samples = samples;
        this.order = TreeUtils.sort(nominalAttributes, x, samples);
        this._sampleIndex = sampleIndex;

        final double[] posteriori = new double[k];
        for (int i = 0; i < k; i++) {
            posteriori[i] = (double) count[i] / n;
        }
        this.root = new Node(MathUtils.whichMax(count), posteriori);

        final TrainNode trainRoot = new TrainNode(root, 1, 0, _sampleIndex.length, totalNumSamples);
        if (maxLeafNodes == Integer.MAX_VALUE) { // depth-first split
            if (trainRoot.findBestSplit()) {
                trainRoot.split(null);
            }
        } else { // best-first split
            // Priority queue for best-first tree growing.
            final PriorityQueue<TrainNode> nextSplits = new PriorityQueue<TrainNode>();
            // Now add splits to the tree until max tree size is reached
            if (trainRoot.findBestSplit()) {
                nextSplits.add(trainRoot);
            }
            // Pop best leaf from priority queue, split it, and push
            // children nodes into the queue if possible.
            for (int leaves = 1; leaves < maxLeafNodes; leaves++) {
                // parent is the leaf to split
                TrainNode node = nextSplits.poll();
                if (node == null) {
                    break;
                }
                if (!node.split(nextSplits)) { // Split the parent node into two children nodes
                    leaves--;
                }
            }
            pruneRedundantLeaves(root, importance);
        }
    }

    private static void partitionArray(@Nonnull final SparseIntArray a, final int low, final int pivot, final int high, @Nonnull final IntPredicate goesLeft, @Nonnull final int[] buf) {
        final int[] rowIndexes = a.keys();
        final int[] rowPtrs = a.values();
        final int size = a.size();

        final int startPos = ArrayUtils.insertionPoint(rowIndexes, size, low);
        final int endPos = ArrayUtils.insertionPoint(rowIndexes, size, high);
        int pos = startPos, k = 0, j = low;
        for (int i = startPos; i < endPos; i++) {
            final int rowPtr = rowPtrs[i];
            if (goesLeft.test(rowPtr)) {
                rowIndexes[pos] = j;
                rowPtrs[pos] = rowPtr;
                pos++;
                j++;
            } else {
                if (k >= buf.length) {
                    throw new IndexOutOfBoundsException(String.format(
                            "low=%d, pivot=%d, high=%d, a.size()=%d, buf.length=%d, i=%d, j=%d, k=%d, startPos=%d, endPos=%d\na=%s\nbuf=%s",
                            low, pivot, high, a.size(), buf.length, i, j, k, startPos, endPos,
                            a.toString(), Arrays.toString(buf)));
                }
                buf[k++] = rowPtr;
            }
        }
        for (int i = 0; i < k; i++) {
            rowIndexes[pos] = pivot + i;
            rowPtrs[pos] = buf[i];
            pos++;
        }
        if (pos != endPos) {
            throw new IllegalStateException(
                    String.format("pos=%d, startPos=%d, endPos=%d, k=%d\na=%s", pos, startPos, endPos,
                            k, a.toString()));
        }
    }

    private static void partitionArray(@Nonnull final int[] a, final int low, final int pivot,
                                       final int high, @Nonnull final IntPredicate goesLeft, @Nonnull final int[] buf) {
        int j = low;
        int k = 0;
        for (int i = low; i < high; i++) {
            if (i >= a.length) {
                throw new IndexOutOfBoundsException(String.format(
                        "low=%d, pivot=%d, high=%d, a.length=%d, buf.length=%d, i=%d, j=%d, k=%d", low,
                        pivot, high, a.length, buf.length, i, j, k));
            }
            final int rowPtr = a[i];
            if (goesLeft.test(rowPtr)) {
                a[j++] = rowPtr;
            } else {
                if (k >= buf.length) {
                    throw new IndexOutOfBoundsException(String.format(
                            "low=%d, pivot=%d, high=%d, a.length=%d, buf.length=%d, i=%d, j=%d, k=%d",
                            low, pivot, high, a.length, buf.length, i, j, k));
                }
                buf[k++] = rowPtr;
            }
        }
        if (k != high - pivot || j != pivot) {
            throw new IndexOutOfBoundsException(
                    String.format("low=%d, pivot=%d, high=%d, a.length=%d, buf.length=%d, j=%d, k=%d",
                            low, pivot, high, a.length, buf.length, j, k));
        }
        System.arraycopy(buf, 0, a, pivot, k);
    }

    private static double impurity(@Nonnull final int[] count, final int n, @Nonnull final SplitRule rule) {
        double impurity = 0.0;

        switch (rule) {
            case GINI: {
                impurity = 1.0;
                for (int count_i : count) {
                    if (count_i > 0) {
                        double p = (double) count_i / n;
                        impurity -= p * p;
                    }
                }
                break;
            }
            case ENTROPY: {
                for (int count_i : count) {
                    if (count_i > 0) {
                        double p = (double) count_i / n;
                        impurity -= p * MathUtils.log2(p);
                    }
                }
                break;
            }
            case CLASSIFICATION_ERROR: {
                impurity = 0.d;
                for (int count_i : count) {
                    if (count_i > 0) {
                        impurity = Math.max(impurity, (double) count_i / n);
                    }
                }
                impurity = Math.abs(1.d - impurity);
                break;
            }
        }

        return impurity;
    }

    private static void indent(final StringBuilder builder, final int depth) {
        for (int i = 0; i < depth; i++) {
            builder.append("  ");
        }
    }

    private static void pruneRedundantLeaves(@Nonnull final Node node, @Nonnull Vector importance) {
        if (node.isLeafNode()) {
            return;
        }

        // The children might not be leaves now, but might collapse into leaves given the chance.
        pruneRedundantLeaves(node.trueChild, importance);
        pruneRedundantLeaves(node.falseChild, importance);

        if (node.trueChild.isLeafNode() && node.falseChild.isLeafNode() && node.trueChild.output == node.falseChild.output) {
            node.trueChild = null;
            node.falseChild = null;
            importance.decr(node.feature, node.score);
        } else {
            // a posteriori is not needed for non-leaf nodes
            node.posteriori = null;
        }
    }

    private static void checkArgument(@Nonnull Matrix x, @Nonnull int[] y, int numVars, int maxDepth, int maxLeafNodes, int minSamplesSplit, int minSamplesLeaf) {
        if (x.numRows() != y.length) {
            throw new IllegalArgumentException(
                    String.format("The sizes of X and Y don't match: %d != %d", x.numRows(), y.length));
        }
        if (y.length == 0) {
            throw new IllegalArgumentException("No training example given");
        }
        if (numVars <= 0 || numVars > x.numColumns()) {
            throw new IllegalArgumentException(
                    "Invalid number of variables to split on at a node of the tree: " + numVars);
        }
        if (maxDepth < 2) {
            throw new IllegalArgumentException("maxDepth should be greater than 1: " + maxDepth);
        }
        if (maxLeafNodes < 2) {
            throw new IllegalArgumentException("Invalid maximum leaves: " + maxLeafNodes);
        }
        if (minSamplesSplit < 2) {
            throw new IllegalArgumentException(
                    "Invalid minimum number of samples required to split an internal node: "
                            + minSamplesSplit);
        }
        if (minSamplesLeaf < 1) {
            throw new IllegalArgumentException(
                    "Invalid minimum size of leaf nodes: " + minSamplesLeaf);
        }
    }


}
