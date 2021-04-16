package drillbit.classification.multiclass;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import drillbit.BaseLearner;
import drillbit.FeatureValue;
import drillbit.TrainWeights;
import drillbit.parameter.*;
import drillbit.optimizer.LossFunctions;
import drillbit.protobuf.ClassificationPb;
import drillbit.utils.parser.StringParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/*
    One to all multiclass classifier.
 */
public abstract class MulticlassClassificationBaseLearner extends BaseLearner {
    // Models
    protected ConcurrentHashMap<String, Weights> label2model;
    protected long count;

    // For model storage and allocate
    protected boolean dense;
    protected int dims;

    // For iteratively training
    private int iters;

    @Override
    @Nonnull
    public Options getOptions() {
        Options opts = super.getOptions();

        opts.addOption("dense", "dense_model", false, "Use dense model or not");
        opts.addOption("dims", "feature_dimensions", true, "The dimension of model [default: 16777216 (2^24)]");
        opts.addOption("iters", "iterations", true, "number of iterations");
        //TODO: Is conversion check suitable for multiclass classification?
        //TODO: Add conversion check here if so.

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

        if (cl.hasOption("iters")) {
            iters = StringParser.parseInt(cl.getOptionValue("iters"), 1);
        }
        else {
            iters = 1;
        }

        if (iters < 1) {
            throw new IllegalArgumentException(String.format("invalid iterations of %d", iters));
        }

        label2model = new ConcurrentHashMap<>();

        return cl;
    }

    @Override
    public void add(@Nonnull final String feature, @Nonnull final String target, @Nonnull final String commandLine) {
        if (!optionProcessed) {
            // parse commandline value here.
            CommandLine cl = parseOptions(commandLine);
            processOptions(cl);
            optionProcessed = true;
        }

        ArrayList<String> featureValues = StringParser.parseArray(feature);
        ArrayList<FeatureValue> features = new ArrayList<>();
        assert featureValues != null;
        for (String featureValue : featureValues) {
            features.add(StringParser.parseFeature(featureValue));
        }

        checkTargetValue(target);

        count++;
        train(features, target);
    }

    protected abstract void train(@Nonnull final ArrayList<FeatureValue> features, @Nonnull final String actual_label);

    protected final double calcVariance(@Nonnull final ArrayList<FeatureValue> features) {
        double variance = 0.d;
        for (FeatureValue f : features) {// a += w[i] * x[i]
            if (f == null) {
                continue;
            }
            double v = f.getValueAsDouble();
            variance += v * v;
        }
        return variance;
    }

    protected final PredictionResult calcScoreAndVariance(@Nonnull final Weights weights, @Nonnull final ArrayList<FeatureValue> features) {
        double score = 0.f;
        double variance = 0.f;

        for (FeatureValue f : features) {// a += w[i] * x[i]
            if (f == null) {
                continue;
            }
            final Object k = f.getFeature();
            final double v = f.getValueAsDouble();

            TrainWeights.ExtendedWeight old_w = weights.get(k);
            if (old_w == null) {
                variance += (1.f * v * v);
            } else {
                score += (old_w.get() * v);
                variance += (old_w.getCovar() * v * v);
            }
        }

        return new PredictionResult(score).variance(variance);
    }

    @Override
    public final byte[] toByteArray() throws IllegalArgumentException, UnsupportedOperationException {
        logger.info("Trained " + label2model.size() + " classification model using " + count + " training examples");

        ClassificationPb.MulticlassClassifier.Builder builder = ClassificationPb.MulticlassClassifier.newBuilder();
        ClassificationPb.MulticlassClassifier.LabelAndWeights.Builder labelAndWeightsBuilder = ClassificationPb.MulticlassClassifier.LabelAndWeights.newBuilder();

        builder.setDense(dense);
        builder.setDims(dims);
        for (Map.Entry<String, Weights> entry : label2model.entrySet()) {
            labelAndWeightsBuilder.clear();
            builder.addLabel2Weights(labelAndWeightsBuilder.setLabel(entry.getKey()).setWeights(ByteString.copyFrom(entry.getValue().toByteArray())).build());
        }

        return builder.build().toByteArray();
    }

    @Override
    public final BaseLearner fromByteArray(byte[] learnerBytes) throws InvalidProtocolBufferException {
        ClassificationPb.MulticlassClassifier multiclassClassifier = ClassificationPb.MulticlassClassifier.parseFrom(learnerBytes);

        dense = multiclassClassifier.getDense();
        dims = multiclassClassifier.getDims();
        label2model = new ConcurrentHashMap<>();
        for (ClassificationPb.MulticlassClassifier.LabelAndWeights labelAndWeights : multiclassClassifier.getLabel2WeightsList()) {
            if (dense) {
                label2model.put(labelAndWeights.getLabel(), (new DenseWeights(dims, TrainWeights.WeightType.Single)).fromByteArray(labelAndWeights.getWeights().toByteArray()));
            }
            else {
                label2model.put(labelAndWeights.getLabel(), (new SparseWeights(dims, TrainWeights.WeightType.Single)).fromByteArray(labelAndWeights.getWeights().toByteArray()));
            }
        }

        return this;
    }

    @Override
    protected void finalizeTraining() {
    }

    @Override
    protected void checkTargetValue(String target) throws IllegalArgumentException {

    }

    @Override
    public void checkLossFunction(LossFunctions.LossFunction lossFunction) throws IllegalArgumentException {

    }

    @Override
    public LossFunctions.LossType getDefaultLossType() {
        return null;
    }

    @NotNull
    @Override
    protected String getLossOptionDescription() {
        return null;
    }

}
