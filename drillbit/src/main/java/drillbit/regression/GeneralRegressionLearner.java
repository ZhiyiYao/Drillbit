package drillbit.regression;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import drillbit.BaseLearner;
import drillbit.FeatureValue;
import drillbit.TrainWeights;
import drillbit.parameter.*;
import drillbit.optimizer.*;
import drillbit.protobuf.SamplePb;
import drillbit.protobuf.RegressionPb;
import drillbit.utils.common.DoubleAccumulator;
import drillbit.utils.parser.StringParser;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public final class GeneralRegressionLearner extends BaseLearner {
    // Model
    protected Weights weights;
    protected long count;

    // For model storage and allocate
    protected boolean dense;
    protected int dims;

    // For iteratively training
    private int iters;

    // For mini batch update
    protected boolean isMiniBatch;
    protected int miniBatch;
    protected ConcurrentHashMap<Object, DoubleAccumulator> accumulated;
    protected int sampled;

    // For optimization
    protected ConcurrentHashMap<String, String> optimizerOptions;
    protected Optimizers.OptimizerBase optimizer;
    protected LossFunctions.LossFunction lossFunction;

    // Conversion check
    private boolean chkCv;
    private double cvRate;
    protected ConversionState cvState;

    public GeneralRegressionLearner() {
        super();
    }

    @Override
    public Options getOptions() {
        Options opts = super.getOptions();

        opts.addOption("dense", "dense_model", false, "Use dense model or not");
        opts.addOption("dims", "feature_dimensions", true, "The dimension of model [default: 16777216 (2^24)]");
        opts.addOption("iters", "iterations", true, "number of iterations");
        opts.addOption("mini_batch", "mini_batch_size", true, "mini batch size");
        opts.addOption("loss", "loss_function", true, "loss function name");
        opts.addOption("chk_cv", "check_conversion", false, "whether to check conversion");
        opts.addOption("opt", "optimizer", true, "optimizer name");
        opts.addOption("cv_rate", "conversion_rate", true, "conversion rate used in checking");

        return opts;
    }

    @Override
    public CommandLine processOptions(@Nonnull final CommandLine cl) {
        super.processOptions(cl);

        dense = cl.hasOption("dense");

        if (cl.hasOption("dims")) {
            dims = StringParser.parseInt(cl.getOptionValue("dims"), -1);
        }
        if (dims < 0) {
            dims = dense ? DEFAULT_DENSE_DIMS : DEFAULT_SPARSE_DIMS;
        }

        optimizerOptions = OptimizerOptions.create();
        OptimizerOptions.processOptions(cl, optimizerOptions);
        optimizer = createOptimizer();

        if (cl.hasOption("iters")) {
            iters = StringParser.parseInt(cl.getOptionValue("iters"), 1);
        }
        else {
            iters = 1;
        }

        if (iters < 1) {
            throw new IllegalArgumentException(String.format("invalid iterations of %d", iters));
        }

        if (cl.hasOption("mini_batch")) {
            miniBatch = StringParser.parseInt(cl.getOptionValue("mini_batch"), 1);
        }
        else {
            miniBatch = 1;
        }
        if (miniBatch < 1) {
            throw new IllegalArgumentException(String.format("invalid mini batch size of %d", miniBatch));
        }
        isMiniBatch = miniBatch > 1;

        lossFunction = LossFunctions.getLossFunction(getDefaultLossType());
        if (cl.hasOption("loss")) {
            try {
                lossFunction = LossFunctions.getLossFunction(cl.getOptionValue("loss"));
            }
            catch (Exception e) {
                throw new IllegalArgumentException(e.getMessage());
            }
        }
        checkLossFunction(lossFunction);

        OptimizerOptions.processOptions(cl, optimizerOptions);

        chkCv = cl.hasOption("chk_cv");
        if (cl.hasOption("cv_rate")) {
            cvRate = StringParser.parseDouble(cl.getOptionValue("cv_rate"), cvRate);
            chkCv = true;
        }
        cvState = new ConversionState(chkCv, cvRate);

        weights = createModel();
        count = 0;
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
    final Weights createModel() {
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
    public void add(@Nonnull final String feature, @Nonnull final String target, @Nonnull final String commandLine) {
        if (!optionProcessed) {
            // parse commandline value here.
            CommandLine cl = parseOptions(commandLine);
            processOptions(cl);
            optionProcessed = true;
        }

        if (isMiniBatch && accumulated == null) {
            this.accumulated = new ConcurrentHashMap<>(1024);
        }

        ArrayList<String> featureValues = StringParser.parseArray(feature);
        ArrayList<FeatureValue> featureVector = new ArrayList<>();
        assert featureValues != null;
        for (String featureValue : featureValues) {
            featureVector.add(StringParser.parseFeature(featureValue));
        }

        checkTargetValue(target);
        double targetValue = StringParser.parseDouble(target, 0.d);

        count++;
        train(featureVector, targetValue);

        writeSampleToTempFile(featureVector, target);
    }

    @Override
    public void add(@Nonnull final String feature, @Nonnull final String target) {
        add(feature, target, "");
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
            throw new IllegalArgumentException(String.format("Loss function %s not for regression", lossFunction.getType().toString()));
        }
    }

    @Override
    public void train(@Nonnull final ArrayList<FeatureValue> features, final double target) {
        double p = predict(features);
        update(features, target, p);
    }

    @Override
    public Object predict(@NotNull String features, @NotNull String options) {
        ArrayList<String> featureValues = StringParser.parseArray(features);
        ArrayList<FeatureValue> featureVector = new ArrayList<>();
        assert featureValues != null;
        for (String featureValue : featureValues) {
            featureVector.add(StringParser.parseFeature(featureValue));
        }

        return predict(featureVector);
    }

    public double predict(@Nonnull ArrayList<FeatureValue> features) {
        double score = 0.d;
        for (FeatureValue f : features) {// a += w[i] * x[i]
            if (f == null) {
                continue;
            }
            final Object k = f.getFeature();
            final double v = f.getValueAsDouble();

            double old_w = weights.getWeight(k);
            if (old_w != 0.f) {
                score += (old_w * v);
            }
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
                batchUpdate();
            }
        } else {
            onlineUpdate(features, loss, dloss);
        }
    }

    final void runIterativeTraining() {
        SamplePb.Sample sample;
        ArrayList<FeatureValue> featureValueVector;
        double score;

        try {
            outputStream.close();
        }
        catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

        try {
            for (int iter = 2; iter <= iters; iter++) {
                for (int i = 0; i < count; i++) {
                    cvState.next();

                    sample = readSampleFromTempFile();
                    int featureVectorSize = sample.getFeatureList().size();
                    score = StringParser.parseDouble(sample.getTarget(), 0.d);
                    featureValueVector = new ArrayList<>(featureVectorSize);
                    for (int j = 0; j < featureVectorSize; j++) {
                        featureValueVector.add(StringParser.parseFeature(sample.getFeature(j)));
                    }
                    train(featureValueVector, score);
                }

                if (isMiniBatch) {
                    batchUpdate();
                }

                try {
                    inputStream.close();
                }
                catch (IOException e) {
                    logger.error("temp file close failed.");
                    logger.error(e.getMessage(), e);
                }
                finally {
                    inputStream = null;
                }
            }
        } catch (Throwable e) {
            throw new IllegalArgumentException("Exception caused in the iterative training", e);
        } finally {
            // delete the temporary file and release resources
            File file = new File("drillbit_input_samples.data_pb");
            file.delete();
        }
    }

    final void accumulateUpdate(@Nonnull final ArrayList<FeatureValue> features, final double loss, final double dloss) {
        for (FeatureValue f : features) {
            Object feature = f.getFeature();
            double xi = f.getValueAsDouble();
            double weight = weights.getWeight(feature);

            double gradient = dloss * xi;
            double new_weight = optimizer.update(feature, weight, loss, gradient);

            assert accumulated != null;
            DoubleAccumulator acc = accumulated.get(feature);
            if (acc == null) {
                acc = new DoubleAccumulator(new_weight);
                accumulated.put(feature, acc);
            } else {
                acc.add(new_weight);
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
            final double new_weight = optimizer.update(feature, weight, loss, gradient);
            if (new_weight == 0.f) {
                weights.delete(feature);
                continue;
            }
            weights.setWeight(feature, new_weight);
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
            final double new_weight = v.get();
            if (new_weight == 0.d) {
                weights.delete(feature);
                continue;
            }
            weights.setWeight(feature, new_weight);
        }

        accumulated.clear();
        this.sampled = 0;
    }

    @Override
    public final byte[] toByteArray() {
        logger.info("Trained a regression model using " + count + " training examples");

        RegressionPb.GeneralRegressor.Builder builder = RegressionPb.GeneralRegressor.newBuilder();

        builder.setDense(dense);
        builder.setDims(dims);
        builder.setWeights(ByteString.copyFrom(weights.toByteArray()));

        return builder.build().toByteArray();
    }

    @Override
    public final BaseLearner fromByteArray(byte[] learnerBytes) throws InvalidProtocolBufferException {
        RegressionPb.GeneralRegressor generalRegressor;
        try {
             generalRegressor = RegressionPb.GeneralRegressor.parseFrom(learnerBytes);
        }
        catch (InvalidProtocolBufferException e) {
            logger.error(e);
            throw e;
        }

        dense = generalRegressor.getDense();
        dims = generalRegressor.getDims();
        byte[] modelBytes = generalRegressor.getWeights().toByteArray();

        if (dense) {
            weights = (new DenseWeights(dims, TrainWeights.WeightType.Single)).fromByteArray(modelBytes);
        }
        else {
            weights = (new SparseWeights(dims, TrainWeights.WeightType.Single)).fromByteArray(modelBytes);
        }

        return this;
    }

    @Override
    public void finalizeTraining() {
        if (count == 0) {
            return;
        }
        if (iters > 1) {
            runIterativeTraining();
        }
        if (isMiniBatch) {
            batchUpdate();
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
