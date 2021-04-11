package drillbit.parameter;

import com.google.protobuf.InvalidProtocolBufferException;
import drillbit.utils.math.MathUtils;
import drillbit.utils.math.DenseVector;
import drillbit.utils.math.Matrix;
import drillbit.utils.math.Vector;
import drillbit.utils.math.VectorOperation;
import drillbit.utils.random.PRNG;
import io.netty.util.internal.ConcurrentSet;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.PriorityQueue;

public class Tree {
}
//public class Tree {
//
//    private static final Log logger = LogFactory.getLog(Tree.class);
//
//    /**
//     * Training dataset.
//     */
//    @Nonnull
//    private final Matrix X;
//    /**
//     * class labels.
//     */
//    @Nonnull
//    private final int[] y;
//    /**
//     * The samples for training this node. Note that samples[i] is the number of sampling of
//     * dataset[i]. 0 means that the datum is not included and values of greater than 1 are possible
//     * because of sampling with replacement.
//     */
//    @Nonnull
//    private final int[] samples;
//    /**
//     * An index of training values. Initially, order[j] is a set of indices that iterate through the
//     * training values for attribute j in ascending order. During training, the array is rearranged
//     * so that all values for each leaf node occupy a contiguous range, but within that range they
//     * maintain the original ordering. Note that only numeric attributes will be sorted; non-numeric
//     * attributes will have a null in the corresponding place in the array.
//     */
//    @Nonnull
//    private final VariableOrder order;
//    /**
//     * An index that maps their current position in the {@link #order} to their original locations
//     * in {@link #samples}.
//     */
//    @Nonnull
//    private final int[] _sampleIndex;
//    /**
//     * The attributes of independent variable.
//     */
//    @Nonnull
//    private final ConcurrentSet<Integer> nominalAttributes;
//    /**
//     * Variable importance. Every time a split of a node is made on variable the (GINI, information
//     * gain, etc.) impurity criterion for the two descendant nodes is less than the parent node.
//     * Adding up the decreases for each individual variable over the tree gives a simple measure of
//     * variable importance.
//     */
//    @Nonnull
//    private final Vector importance;
//    /**
//     * The root of the regression tree
//     */
//    @Nonnull
//    private final Node root;
//    /**
//     * The maximum number of the tree depth
//     */
//    private final int maxDepth;
//    /**
//     * The splitting rule.
//     */
//    @Nonnull
//    private final SplitRule splitRule;
//    /**
//     * The number of classes.
//     */
//    private final int k;
//    /**
//     * The number of input variables to be used to determine the decision at a node of the tree.
//     */
//    private final int nVariable;
//    /**
//     * The number of instances in a node below which the tree will not split.
//     */
//    private final int minSamplesSplit;
//    /**
//     * The minimum number of samples in a leaf node.
//     */
//    private final int minSamplesLeaf;
//    /**
//     * The random number generator.
//     */
//    @Nonnull
//    private final PRNG rnd;
//
//    public Tree(@Nonnull Matrix x) {
//        X = x;
//    }
//
//    /**
//     * The criterion to choose variable to split instances.
//     */
//    public static enum SplitRule {
//        /**
//         * Used by the CART algorithm, Gini impurity is a measure of how often a randomly chosen
//         * element from the set would be incorrectly labeled if it were randomly labeled according
//         * to the distribution of labels in the subset. Gini impurity can be computed by summing the
//         * probability of each item being chosen times the probability of a mistake in categorizing
//         * that item. It reaches its minimum (zero) when all cases in the node fall into a single
//         * target category.
//         */
//        GINI,
//        /**
//         * Used by the ID3, C4.5 and C5.0 tree generation algorithms.
//         */
//        ENTROPY,
//        /**
//         * Classification error.
//         */
//        CLASSIFICATION_ERROR
//    }
//
//    /**
//     * Classification tree node.
//     */
//    public static class Node {
//
//        //Predicted class label for this node.
//        int output = -1;
//
//        // A posteriori probability based on sample ratios in this node.
//        @Nullable
//        double[] posteriori = null;
//
//        // The split feature for this node.
//        int feature = -1;
//
//        // The type of split feature
//        boolean numerical = true;
//
//        // The split value.
//        double value = Double.NaN;
//
//        // Reduction in splitting criterion.
//        double score = 0.0;
//
//        public Node trueChild = null;
//
//        public Node falseChild = null;
//
//        public Node() {}
//
//        public Node(@Nonnull double[] posteriori) {
//            this.output = MathUtils.whichMax(posteriori);
//            this.posteriori = posteriori;
//        }
//
//        public Node(int output, @Nonnull double[] posteriori) {
//            this.output = output;
//            this.posteriori = posteriori;
//        }
//
//        private boolean isLeafNode() {
//            return trueChild == null && falseChild == null;
//        }
//
//        private void markAsLeafNode() {
//            this.feature = -1;
//            this.value = Double.NaN;
//            this.score = 0.0;
//            this.trueChild = null;
//            this.falseChild = null;
//        }
//
//        public int predict(@Nonnull final double[] x) {
//            return predict(new DenseVector(x));
//        }
//
//        public int predict(@Nonnull final DenseVector x) {
//            if (isLeafNode()) {
//                return output;
//            } else {
//                if (numerical) {
//                    if (x.get(feature, Double.NaN) <= value) {
//                        return trueChild.predict(x);
//                    } else {
//                        return falseChild.predict(x);
//                    }
//                } else {
//                    if (x.get(feature, Double.NaN) == value) {
//                        return trueChild.predict(x);
//                    } else {
//                        return falseChild.predict(x);
//                    }
//                }
//            }
//        }
//
//        public void exportJavascript(@Nonnull final StringBuilder builder,
//                                     @Nullable final String[] featureNames, @Nullable final String[] classNames,
//                                     final int depth) {
//            if (isLeafNode()) {
//                indent(builder, depth);
//                builder.append("").append(resolveName(output, classNames)).append(";\n");
//            } else {
//                indent(builder, depth);
//                if () {
//                    if (featureNames == null) {
//                        builder.append("if( x[")
//                                .append(splitFeature)
//                                .append("] <= ")
//                                .append(splitValue)
//                                .append(" ) {\n");
//                    } else {
//                        builder.append("if( ")
//                                .append(resolveFeatureName(splitFeature, featureNames))
//                                .append(" <= ")
//                                .append(splitValue)
//                                .append(" ) {\n");
//                    }
//                } else {
//                    if (featureNames == null) {
//                        builder.append("if( x[")
//                                .append(splitFeature)
//                                .append("] == ")
//                                .append(splitValue)
//                                .append(" ) {\n");
//                    } else {
//                        builder.append("if( ")
//                                .append(resolveFeatureName(splitFeature, featureNames))
//                                .append(" == ")
//                                .append(splitValue)
//                                .append(" ) {\n");
//                    }
//                }
//                trueChild.exportJavascript(builder, featureNames, classNames, depth + 1);
//                indent(builder, depth);
//                builder.append("} else  {\n");
//                falseChild.exportJavascript(builder, featureNames, classNames, depth + 1);
//                indent(builder, depth);
//                builder.append("}\n");
//            }
//        }
//
//        public byte[] toByteArray() {
//            return build().toByteArray();
//        }
//
//        public Node fromByteArray(@Nonnull final byte[] byteArray) throws InvalidProtocolBufferException {
//            DecisionTreePb.Node node = null;
//            try {
//                node = DecisionTreePb.Node.parseFrom(byteArray);
//            }
//            catch (InvalidProtocolBufferException e) {
//                e.printStackTrace();
//                throw e;
//            }
//
//            return parse(node);
//        }
//
//        private DecisionTreePb.Node build() {
//            DecisionTreePb.Node.Builder builder = DecisionTreePb.Node.newBuilder();
//            builder.setOutput(output);
//
//            assert posteriori != null;
//            for (double value : posteriori) {
//                builder.addPosteriori(value);
//            }
//
//            builder.setFeature(feature);
//            builder.setNumerical(numerical);
//            builder.setValue(value);
//
//            if (trueChild != null) {
//                builder.setTrueChild(trueChild.build());
//            }
//
//            if (falseChild != null) {
//                builder.setFalseChild(falseChild.build());
//            }
//
//            return builder.build();
//        }
//
//        private Node parse(@Nonnull final DecisionTreePb.Node node) {
//            output = node.getOutput();
//
//            List<Double> posterioriList = node.getPosterioriList();
//            posteriori = new double[posterioriList.size()];
//            for (int i = 0; i < posterioriList.size(); i++) {
//                posteriori[i] = posterioriList.get(i);
//            }
//
//            feature = node.getFeature();
//            numerical = node.getNumerical();
//            value = node.getValue();
//
//            if (node.hasTrueChild()) {
//                trueChild = (new Node()).parse(node.getTrueChild());
//            }
//            else {
//                trueChild = null;
//            }
//
//            if (node.hasFalseChild()) {
//                falseChild = (new Node()).parse(node.getFalseChild());
//            }
//            else {
//                falseChild = null;
//            }
//
//            return this;
//        }
//    }
//
//    private final class TrainNode implements Comparable<TrainNode> {
//         // The associated regression tree node.
//        @Nonnull
//        final Node node;
//
//        // Depth of the node in the tree
//        final int depth;
//
//        // The lower bound (inclusive) in the order array of the samples belonging to this node.
//        final int low;
//
//        // The upper bound (exclusive) in the order array of the samples belonging to this node.
//        final int high;
//
//        // The number of samples
//        final int samples;
//
//        @Nullable
//        int[] constFeatures;
//
//        public TrainNode(@Nonnull Node node, int depth, int low, int high, int samples) {
//            this(node, depth, low, high, samples, new int[0]);
//        }
//
//        public TrainNode(@Nonnull Node node, int depth, int low, int high, int samples,
//                         @Nonnull int[] constFeatures) {
//            if (low >= high) {
//                throw new IllegalArgumentException(
//                        "Unexpected condition was met. low=" + low + ", high=" + high);
//            }
//            this.node = node;
//            this.depth = depth;
//            this.low = low;
//            this.high = high;
//            this.samples = samples;
//            this.constFeatures = constFeatures;
//        }
//
//        @Override
//        public int compareTo(TrainNode a) {
//            return (int) Math.signum(a.node.score - node.score);
//        }
//
//        /**
//         * Finds the best attribute to split on at the current node.
//         *
//         * @return true if a split exists to reduce squared error, false otherwise.
//         */
//        public boolean findBestSplit() {
//            // avoid split if tree depth is larger than threshold
//            if (depth >= maxDepth) {
//                return false;
//            }
//            // avoid split if the number of samples is less than threshold
//            if (samples <= minSamplesSplit) {
//                return false;
//            }
//
//            // Sample count in each class.
//            final int[] count = new int[k];
//            final boolean pure = countSamples(count);
//            if (pure) {// if all instances have same label, stop splitting.
//                return false;
//            }
//
//            final int[] constFeatures_ = this.constFeatures; // this.constFeatures may be replace in findBestSplit but it's accepted
//            final double impurity = impurity(count, samples, splitRule);
//            final int[] falseCount = new int[k];
//            for (int varJ : variableIndex()) {
//                if (ArrayUtils.contains(constFeatures_, varJ)) {
//                    continue; // skip constant features
//                }
//                final Node split = findBestSplit(samples, count, falseCount, impurity, varJ);
//                if (split.splitScore > node.splitScore) {
//                    node.splitFeature = split.splitFeature;
//                    node.quantitativeFeature = split.quantitativeFeature;
//                    node.splitValue = split.splitValue;
//                    node.splitScore = split.splitScore;
//                }
//            }
//
//            return node.splitFeature != -1;
//        }
//
//        @Nonnull
//        private int[] variableIndex() {
//            final Matrix X = X;
//            final IntReservoirSampler sampler = new IntReservoirSampler(nVariable, rnd.nextLong());
//            if (X.isSparse()) {
//                // sample columns from sampled examples
//                final RoaringBitmap cols = new RoaringBitmap();
//                final VectorOperation proc = new VectorOperation() {
//                    public void apply(final int col) {
//                        cols.add(col);
//                    }
//                };
//                final int[] sampleIndex = _sampleIndex;
//                for (int i = low, end = high; i < end; i++) {
//                    int row = sampleIndex[i];
//                    assert (Tree.this.samples[row] != 0) : row;
//                    X.eachColumnIndexInRow(row, proc);
//                }
//                cols.forEach(new IntConsumer() {
//                    public void accept(final int k) {
//                        sampler.add(k);
//                    }
//                });
//            } else {
//                final int ncols = X.numColumns();
//                for (int i = 0; i < ncols; i++) {
//                    sampler.add(i);
//                }
//            }
//            return sampler.getSample();
//        }
//
//        private boolean countSamples(@Nonnull final int[] count) {
//            final int[] sampleIndex = _sampleIndex;
//            final int[] samples = Tree.this.samples;
//            final int[] y = Tree.this.y;
//
//            boolean pure = true;
//
//            for (int i = low, end = high, label = -1; i < end; i++) {
//                int index = sampleIndex[i];
//                int y_i = y[index];
//                count[y_i] += samples[index];
//
//                if (label == -1) {
//                    label = y_i;
//                } else if (y_i != label) {
//                    pure = false;
//                }
//            }
//
//            return pure;
//        }
//
//        /**
//         * Finds the best split cutoff for attribute j at the current node.
//         *
//         * @param n the number instances in this node.
//         * @param count the sample count in each class.
//         * @param falseCount an array to store sample count in each class for false child node.
//         * @param impurity the impurity of this node.
//         * @param j the attribute index to split on.
//         */
//        private Node findBestSplit(final int n, final int[] count, final int[] falseCount,
//                                   final double impurity, final int j) {
//            final int[] samples = Tree.this.samples;
//            final int[] sampleIndex = _sampleIndex;
//            final Matrix X = X;
//            final int[] y = Tree.this.y;
//            final int classes = k;
//
//            final Node splitNode = new Node();
//
//            if (nominalAttributes.contains(j)) {
//                final Int2ObjectMap<int[]> trueCount = new Int2ObjectOpenHashMap<int[]>();
//
//                int countNaN = 0;
//                for (int i = low, end = high; i < end; i++) {
//                    final int index = sampleIndex[i];
//                    final int numSamples = samples[index];
//                    if (numSamples == 0) {
//                        continue;
//                    }
//
//                    final double v = X.get(index, j, Double.NaN);
//                    if (Double.isNaN(v)) {
//                        countNaN++;
//                        continue;
//                    }
//                    int x_ij = (int) v;
//
//                    int[] tc_x = trueCount.get(x_ij);
//                    if (tc_x == null) {
//                        tc_x = new int[classes];
//                        trueCount.put(x_ij, tc_x);
//                    }
//                    int y_i = y[index];
//                    tc_x[y_i] += numSamples;
//                }
//                final int countDistinctX = trueCount.size() + (countNaN == 0 ? 0 : 1);
//                if (countDistinctX <= 1) { // mark as a constant feature
//                    this.constFeatures = ArrayUtils.sortedArraySet(constFeatures, j);
//                }
//
//                for (Int2ObjectMap.Entry<int[]> e : trueCount.int2ObjectEntrySet()) {
//                    final int l = e.getIntKey();
//                    final int[] trueCount_l = e.getValue();
//
//                    final int tc = Math.sum(trueCount_l);
//                    final int fc = n - tc;
//
//                    // skip splitting this feature.
//                    if (tc < minSamplesSplit || fc < minSamplesSplit) {
//                        continue;
//                    }
//
//                    for (int k = 0; k < classes; k++) {
//                        falseCount[k] = count[k] - trueCount_l[k];
//                    }
//
//                    final double gain =
//                            impurity - (double) tc / n * impurity(trueCount_l, tc, splitRule)
//                                    - (double) fc / n * impurity(falseCount, fc, splitRule);
//
//                    if (gain > splitNode.splitScore) {
//                        // new best split
//                        splitNode.splitFeature = j;
//                        splitNode.quantitativeFeature = false;
//                        splitNode.splitValue = l;
//                        splitNode.splitScore = gain;
//                    }
//                }
//            } else {
//                final int[] trueCount = new int[classes];
//                final MutableInt countNaN = new MutableInt(0);
//                final MutableInt replaceCount = new MutableInt(0);
//
//                order.eachNonNullInColumn(j, low, high, new Consumer() {
//                    double prevx = Double.NaN, lastx = Double.NaN;
//                    int prevy = -1;
//
//                    @Override
//                    public void accept(int pos, final int i) {
//                        final int numSamples = samples[i];
//                        if (numSamples == 0) {
//                            return;
//                        }
//
//                        final double x_ij = X.get(i, j, Double.NaN);
//                        if (Double.isNaN(x_ij)) {
//                            countNaN.incr();
//                            return;
//                        }
//                        if (lastx != x_ij) {
//                            lastx = x_ij;
//                            replaceCount.incr();
//                        }
//
//                        final int y_i = y[i];
//                        if (Double.isNaN(prevx) || x_ij == prevx || y_i == prevy) {
//                            prevx = x_ij;
//                            prevy = y_i;
//                            trueCount[y_i] += numSamples;
//                            return;
//                        }
//
//                        final int tc = Math.sum(trueCount);
//                        final int fc = n - tc;
//
//                        // skip splitting this feature.
//                        if (tc < minSamplesSplit || fc < minSamplesSplit) {
//                            prevx = x_ij;
//                            prevy = y_i;
//                            trueCount[y_i] += numSamples;
//                            return;
//                        }
//
//                        for (int l = 0; l < classes; l++) {
//                            falseCount[l] = count[l] - trueCount[l];
//                        }
//
//                        final double gain =
//                                impurity - (double) tc / n * impurity(trueCount, tc, splitRule)
//                                        - (double) fc / n * impurity(falseCount, fc, splitRule);
//
//                        if (gain > splitNode.splitScore) {
//                            // new best split
//                            splitNode.splitFeature = j;
//                            splitNode.quantitativeFeature = true;
//                            splitNode.splitValue = (x_ij + prevx) / 2.d;
//                            splitNode.splitScore = gain;
//                        }
//
//                        prevx = x_ij;
//                        prevy = y_i;
//                        trueCount[y_i] += numSamples;
//                    }//apply()
//                });
//
//                final int countDistinctX = replaceCount.get() + (countNaN.get() == 0 ? 0 : 1);
//                if (countDistinctX <= 1) { // mark as a constant feature
//                    this.constFeatures = ArrayUtils.sortedArraySet(constFeatures, j);
//                }
//            }
//
//            return splitNode;
//        }
//
//        /**
//         * Split the node into two children nodes. Returns true if split success.
//         *
//         * @return true if split occurred. false if the node is set to leaf.
//         */
//        public boolean split(@Nullable final PriorityQueue<TrainNode> nextSplits) {
//            if (node.splitFeature < 0) {
//                throw new IllegalStateException("Split a node with invalid feature.");
//            }
//
//            final IntPredicate goesLeft = getPredicate();
//
//            // split samples
//            final int tc, fc, pivot;
//            final double[] trueChildPosteriori = new double[k],
//                    falseChildPosteriori = new double[k];
//            {
//                MutableInt tc_ = new MutableInt(0);
//                MutableInt fc_ = new MutableInt(0);
//                pivot = splitSamples(tc_, fc_, trueChildPosteriori, falseChildPosteriori, goesLeft);
//                tc = tc_.get();
//                fc = fc_.get();
//            }
//
//            if (tc < minSamplesLeaf || fc < minSamplesLeaf) {
//                node.markAsLeaf();
//                return false;
//            }
//
//            for (int i = 0; i < k; i++) {
//                trueChildPosteriori[i] /= tc; // divide by zero never happens
//                falseChildPosteriori[i] /= fc;
//            }
//
//            partitionOrder(low, pivot, high, goesLeft);
//
//            int leaves = 0;
//
//            node.trueChild = new Node(trueChildPosteriori);
//            TrainNode trueChild =
//                    new TrainNode(node.trueChild, depth + 1, low, pivot, tc, constFeatures.clone());
//            node.falseChild = new Node(falseChildPosteriori);
//            TrainNode falseChild =
//                    new TrainNode(node.falseChild, depth + 1, pivot, high, fc, constFeatures);
//            this.constFeatures = null;
//
//            if (tc >= minSamplesSplit && trueChild.findBestSplit()) {
//                if (nextSplits != null) {
//                    nextSplits.add(trueChild);
//                } else {
//                    if (trueChild.split(null) == false) {
//                        leaves++;
//                    }
//                }
//            } else {
//                leaves++;
//            }
//
//            if (fc >= minSamplesSplit && falseChild.findBestSplit()) {
//                if (nextSplits != null) {
//                    nextSplits.add(falseChild);
//                } else {
//                    if (falseChild.split(null) == false) {
//                        leaves++;
//                    }
//                }
//            } else {
//                leaves++;
//            }
//
//            // Prune meaningless branches
//            if (leaves == 2) {// both left and right child are leaf node
//                if (node.trueChild.output == node.falseChild.output) {// found a meaningless branch
//                    node.markAsLeaf();
//                    return false;
//                }
//            }
//
//            importance.incr(node.splitFeature, node.splitScore);
//            if (nextSplits == null) {
//                // For depth-first splitting, a posteriori is not needed for non-leaf nodes
//                node.posteriori = null;
//            }
//
//            return true;
//        }
//
//        /**
//         * @return Pivot to split samples
//         */
//        private int splitSamples(@Nonnull final MutableInt tc, @Nonnull final MutableInt fc,
//                                 @Nonnull final double[] trueChildPosteriori,
//                                 @Nonnull final double[] falseChildPosteriori,
//                                 @Nonnull final IntPredicate goesLeft) {
//            final int[] sampleIndex = _sampleIndex;
//            final int[] samples = Tree.this.samples;
//            final int[] y = Tree.this.y;
//
//            int pivot = low;
//            for (int k = low, end = high; k < end; k++) {
//                final int i = sampleIndex[k];
//                final int numSamples = samples[i];
//                final int yi = y[i];
//                if (goesLeft.test(i)) {
//                    tc.addValue(numSamples);
//                    trueChildPosteriori[yi] += numSamples;
//                    pivot++;
//                } else {
//                    fc.addValue(numSamples);
//                    falseChildPosteriori[yi] += numSamples;
//                }
//            }
//            return pivot;
//        }
//
//        /**
//         * Modifies {@link #order} and {@link #_sampleIndex} by partitioning the range from low
//         * (inclusive) to high (exclusive) so that all elements i for which goesLeft(i) is true come
//         * before all elements for which it is false, but element ordering is otherwise preserved.
//         * The number of true values returned by goesLeft must equal split-low.
//         *
//         * @param low the low bound of the segment of the order arrays which will be partitioned.
//         * @param split where the partition's split point will end up.
//         * @param high the high bound of the segment of the order arrays which will be partitioned.
//         * @param goesLeft whether an element goes to the left side or the right side of the
//         *        partition.
//         * @param buffer scratch space large enough to hold all elements for which goesLeft is
//         *        false.
//         */
//        private void partitionOrder(final int low, final int pivot, final int high,
//                                    @Nonnull final IntPredicate goesLeft) {
//            final int[] buf = new int[high - pivot];
//            order.eachRow(new Consumer() {
//                @Override
//                public void accept(int col, @Nonnull final SparseIntArray row) {
//                    partitionArray(row, low, pivot, high, goesLeft, buf);
//                }
//            });
//            partitionArray(_sampleIndex, low, pivot, high, goesLeft, buf);
//        }
//
//        @Nonnull
//        private IntPredicate getPredicate() {
//            if (node.quantitativeFeature) {
//                return new IntPredicate() {
//                    @Override
//                    public boolean test(int i) {
//                        return X.get(i, node.splitFeature, Double.NaN) <= node.splitValue;
//                    }
//                };
//            } else {
//                return new IntPredicate() {
//                    @Override
//                    public boolean test(int i) {
//                        return X.get(i, node.splitFeature, Double.NaN) == node.splitValue;
//                    }
//                };
//            }
//        }
//
//    }
//
//    private static double impurity(@Nonnull final int[] count, final int n, @Nonnull final SplitRule rule) {
//        double impurity = 0.0;
//
//        switch (rule) {
//            case GINI: {
//                impurity = 1.0;
//                for (int count_i : count) {
//                    if (count_i > 0) {
//                        double p = (double) count_i / n;
//                        impurity -= p * p;
//                    }
//                }
//                break;
//            }
//            case ENTROPY: {
//                for (int count_i : count) {
//                    if (count_i > 0) {
//                        double p = (double) count_i / n;
//                        impurity -= p * MathUtils.log2(p);
//                    }
//                }
//                break;
//            }
//            case CLASSIFICATION_ERROR: {
//                impurity = 0.d;
//                for (int count_i : count) {
//                    if (count_i > 0) {
//                        impurity = Math.max(impurity, (double) count_i / n);
//                    }
//                }
//                impurity = Math.abs(1.d - impurity);
//                break;
//            }
//        }
//
//        return impurity;
//    }
//
//    private static void indent(final StringBuilder builder, final int depth) {
//        for (int i = 0; i < depth; i++) {
//            builder.append("  ");
//        }
//    }
//
//}
