package drillbit.regression;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import drillbit.BaseLearner;
import drillbit.FeatureValue;
import drillbit.TrainWeights;
import drillbit.optimizer.*;
import drillbit.parameter.DenseWeights;
import drillbit.parameter.SparseWeights;
import drillbit.parameter.Weights;
import drillbit.protobuf.RegressionPb;
import drillbit.utils.common.DoubleAccumulator;
import drillbit.utils.parser.StringParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public final class GeneralRegressionLearner extends BaseLearner {
    // Model
    protected Weights weights;

    // Model storage
    protected boolean dense;
    protected int dims;
    // For mini batch update
    protected boolean isMiniBatch;
    protected int miniBatch;
    protected ConcurrentHashMap<Object, DoubleAccumulator> accumulated;
    protected int sampled;
    // For optimization
    protected ConcurrentHashMap<String, String> optimizerOptions;
    protected Optimizers.OptimizerBase optimizer;
    protected LossFunctions.LossFunction lossFunction;
    protected ConversionState cvState;
    // For iteratively training
    private int iters;
    // Conversion check
    private boolean chkCv;
    private double cvRate;

    public GeneralRegressionLearner() {
        super();
    }

    @Nonnull
    @Override
    public Options getOptions() {
        Options opts = super.getOptions();

        opts.addOption("dense", false, "Use dense model or not");
        opts.addOption("dims", true, "The dimension of model [default: 16777216 (2^24)]");
        opts.addOption("iters", true, "number of iterations");
        opts.addOption("mini_batch", true, "mini batch size");
        opts.addOption("loss", true, "loss function name");
        opts.addOption("chk_cv", false, "whether to check conversion");
        opts.addOption("opt", true, "optimizer name");
        opts.addOption("cv_rate", true, "conversion rate used in checking");

        return opts;
    }

    @Override
    final public CommandLine processOptions(@Nonnull final CommandLine cl) {
        super.processOptions(cl);

        dense = cl.hasOption("dense");

        dims = StringParser.parseInt(cl.getOptionValue("dims"), -1);
        if (dims < 0) {
            dims = dense ? DEFAULT_DENSE_DIMS : DEFAULT_SPARSE_DIMS;
        }

        optimizerOptions = OptimizerOptions.create();
        OptimizerOptions.processOptions(cl, optimizerOptions);
        optimizer = createOptimizer();

        iters = StringParser.parseInt(cl.getOptionValue("iters"), 1);
        if (iters < 1) {
            throw new IllegalArgumentException(String.format("invalid iterations of %d", iters));
        }

        miniBatch = StringParser.parseInt(cl.getOptionValue("mini_batch"), 1);
        if (miniBatch < 1) {
            throw new IllegalArgumentException(String.format("invalid mini batch size of %d", miniBatch));
        }

        isMiniBatch = miniBatch > 1;
        if (isMiniBatch) {
            accumulated = new ConcurrentHashMap<>();
        }

        lossFunction = LossFunctions.getLossFunction(getDefaultLossType());
        try {
            if (cl.hasOption("loss")) {
                lossFunction = LossFunctions.getLossFunction(cl.getOptionValue("loss"));
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(e.getMessage());
        }
        checkLossFunction(lossFunction);

        chkCv = cl.hasOption("chk_cv") || cl.hasOption("cv_rate");
        cvRate = StringParser.parseDouble(cl.getOptionValue("cv_rate"), cvRate);
        cvState = new ConversionState(chkCv, cvRate);

        weights = createWeights();
        sampled = 0;

        return cl;
    }

    @Nonnull
    final Optimizers.OptimizerBase createOptimizer() {
        if (dense) {
            return DenseOptimizerFactory.create(dims, optimizerOptions);
        } else {
            return SparseOptimizerFactory.create(dims, optimizerOptions);
        }
    }

    @Nonnull
    final Weights createWeights() {
        Weights weights;
        if (dense) {
            logger.info(String.format("Build a dense model with initial with %d initial dimensions", dims));
            weights = new DenseWeights(dims, optimizer.getWeightType());
        } else {
            logger.info(String.format("Build a dense model with initial with %d initial dimensions", dims));
            weights = new SparseWeights(dims, optimizer.getWeightType());
        }
        return weights;
    }

    @Override
    final public void add(@Nonnull String feature, @Nonnull String target) {
        ArrayList<FeatureValue> featureValues = parseFeatureList(feature);
        checkTargetValue(target);
        writeSample(featureValues, target);
    }

    @Override
    final public void train(@Nonnull final ArrayList<FeatureValue> features, final double target) {
        double p = predict(features);
        update(features, target, p);
    }

    @Override
    final public Object predict(@Nonnull String features, @Nonnull String options) {
        // Options unused.
        return Double.toString(predict(parseFeatureList(features)));
    }

    final public double predict(@Nonnull ArrayList<FeatureValue> features) {
        double score = 0.d;
        for (FeatureValue f : features) {// a += w[i] * x[i]
            if (f == null) {
                continue;
            }
            final Object k = f.getFeature();
            final double v = f.getValueAsDouble();

            double w = weights.getWeight(k);
            score += w * v;
        }

        return score;
    }

    @Override
    public void update(@Nonnull ArrayList<FeatureValue> features, double target, double predicted) {
        optimizer.proceedStep();
        double loss = lossFunction.loss(predicted, target);
        cvState.incrLoss(loss); // retain cumulative loss to check convergence
        double dloss = lossFunction.dloss(predicted, target);
        if (dloss == 0.d) {
            return;
        }
        if (dloss < MIN_DLOSS) {
            dloss = MIN_DLOSS;
        } else if (dloss > MAX_DLOSS) {
            dloss = MAX_DLOSS;
        }

        if (isMiniBatch) {
            accumulateUpdate(features, loss, dloss);
            if (sampled >= miniBatch) {
                //printDebugWeight(features.get(1));
                batchUpdate();
                accumulated.clear();
                this.sampled = 0;
                //printDebugWeight(features.get(1));
            }
        } else {
//            System.out.println(loss + " " + dloss);
            onlineUpdate(features, loss, dloss);
        }
    }

    private void printDebugWeight(FeatureValue fv) {
        System.out.println(weights.getWeight(fv.getFeature()));
    }

    final void runIterativeTraining() {
        writeCleanUp();
        try {
            for (int iter = 0; iter < iters; iter++) {
                while (true) {
                    cvState.next();
                    OneSample sample = readSample();
                    if (sample == null) {
                        break;
                    }
                    train(sample.featureValueVector, StringParser.parseDouble(sample.target, 0.d));
                }
                if (isMiniBatch) {
                    batchUpdate();
                }

                epochReadCleanUp();
            }
        } catch (Throwable e) {
            throw new IllegalArgumentException("Exception caused in the iterative training", e);
        } finally {
            readCleanUp();
        }
    }

    final void accumulateUpdate(@Nonnull final ArrayList<FeatureValue> features, final double loss, final double dloss) {
        for (FeatureValue f : features) {
            Object feature = f.getFeature();
            double xi = f.getValueAsDouble();
            double weight = weights.getWeight(feature);
            double gradient = dloss * xi;
            double newWeight = optimizer.update(feature, weight, loss, gradient);

            assert accumulated != null;
            //System.out.println("acc: "+newWeight);
            DoubleAccumulator acc = accumulated.get(feature);
            if (acc == null) {
                acc = new DoubleAccumulator(newWeight);
                accumulated.put(feature, acc);
            } else {
                acc.add(newWeight);
            }
        }
        sampled++;
    }

    final void onlineUpdate(@Nonnull final ArrayList<FeatureValue> features, final double loss, final double dloss) {
        for (FeatureValue f : features) {
            Object feature = f.getFeature();
            double xi = f.getValueAsDouble();
            double weight = weights.getWeight(feature);
            double gradient = dloss * xi;
            final double newWeight = optimizer.update(feature, weight, loss, gradient);
//            System.out.println(weight + " " + newWeight);
            weights.setWeight(feature, newWeight);
        }
    }

    final void batchUpdate() {
        assert accumulated != null;
        if (accumulated.isEmpty()) {
            sampled = 0;
            return;
        }

        for (ConcurrentHashMap.Entry<Object, DoubleAccumulator> e : accumulated.entrySet()) {
            Object feature = e.getKey();
            DoubleAccumulator v = e.getValue();
            final double newWeight = v.get();
//            if (newWeight == 0.d) {
//                weights.delete(feature);
//                continue;
//            }
            weights.setWeight(feature, newWeight);
        }

        accumulated.clear();
        this.sampled = 0;
    }

    @Override
    public final byte[] toByteArray() {
        RegressionPb.GeneralRegressor.Builder builder = RegressionPb.GeneralRegressor.newBuilder();

        builder.setDense(dense);
        builder.setDims(dims);
        builder.setWeights(ByteString.copyFrom(Objects.requireNonNull(weights.toByteArray())));

        return builder.build().toByteArray();
    }

    @Override
    public final BaseLearner fromByteArray(byte[] learnerBytes) throws InvalidProtocolBufferException {
        RegressionPb.GeneralRegressor generalRegressor;
        try {
            generalRegressor = RegressionPb.GeneralRegressor.parseFrom(learnerBytes);
        } catch (InvalidProtocolBufferException e) {
            logger.error(e);
            throw e;
        }

        dense = generalRegressor.getDense();
        dims = generalRegressor.getDims();
        byte[] modelBytes = generalRegressor.getWeights().toByteArray();

        if (dense) {
            weights = (new DenseWeights(dims, TrainWeights.WeightType.Single)).fromByteArray(modelBytes);
        } else {
            weights = (new SparseWeights(dims, TrainWeights.WeightType.Single)).fromByteArray(modelBytes);
        }

        return this;
    }

    @Override
    public void finalizeTraining() {
        runIterativeTraining();

        if (isMiniBatch) {
            batchUpdate();
        }
    }

    @Override
    public void checkTargetValue(String target) throws IllegalArgumentException {
        if (Double.isNaN(StringParser.parseDouble(target, Double.NaN))) {
            throw new IllegalArgumentException(String.format("Invalid target of %s", target));
        }
    }

    @Override
    public void checkLossFunction(LossFunctions.LossFunction lossFunction) throws IllegalArgumentException {
        if (!lossFunction.forRegression()) {
            throw new IllegalArgumentException(String.format("Loss function %s not for regression", lossFunction.getType()));
        }
    }

    @Override
    public LossFunctions.LossType getDefaultLossType() {
        return LossFunctions.LossType.SquaredLoss;
    }

    @Nonnull
    @Override
    protected String getLossOptionDescription() {
        return "Loss function [SquaredLoss (default), QuantileLoss, EpsilonInsensitiveLoss, "
                + "SquaredEpsilonInsensitiveLoss, HuberLoss]";
    }
}
