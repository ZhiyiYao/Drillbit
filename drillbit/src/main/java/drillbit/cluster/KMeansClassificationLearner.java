package drillbit.cluster;

import com.google.protobuf.InvalidProtocolBufferException;

import drillbit.BaseLearner;
import drillbit.FeatureValue;
import drillbit.neighbors.weight.Weight;
import drillbit.optimizer.ConversionState;
import drillbit.optimizer.LossFunctions;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import javax.annotation.Nonnull;

import java.util.ArrayList;

public final class KMeansClassificationLearner extends BaseLearner {
    // Model
    private Weight[] centers;
    private int nCluster;
    private int count;

    // Cluster initialize
    String init;

    // For iteratively training
    private int iters;

    // Model storage
    private boolean dense;
    private int dims;

    // Conversion check
    private boolean chkCv;
    private double cvRate;
    protected ConversionState cvState;

    @Override
    public Options getOptions() {
        Options opts = super.getOptions();

        // Options for KMeans algorithm
        opts.addOption("n_clusters", true, "number of clusters");
        opts.addOption("init", true, "initialize method");

        opts.addOption("iters", "iterations", true, "number of iterations");

        // Options for model storage
        opts.addOption("dims", "feature_dimensions", true, "dimension of model");
        opts.addOption("dense", "use_dense_model", false, "use dense model or not");

        return opts;
    }

    @Override
    public final Options getPredictOptions() {
        throw new UnsupportedOperationException("KMeans algorithm does not support predict options");
    }

    @Override
    public final CommandLine processOptions(@Nonnull final CommandLine cl) {

        return cl;
    }

    @Override
    public final CommandLine processPredictOptions(@Nonnull final CommandLine cl) {
        throw new UnsupportedOperationException("KMeans algorithm does not support predict options");
    }

    @Override
    protected void train(@Nonnull ArrayList<FeatureValue> featureVector, @Nonnull double target) {

    }

    @Override
    public Object predict(@Nonnull String features, @Nonnull String options) {
        return null;
    }

    @Override
    protected void update(@Nonnull ArrayList<FeatureValue> features, double target, double predicted) {

    }

    @Override
    public void add(@Nonnull String feature, @Nonnull String target) {

    }

    @Override
    public byte[] toByteArray() {
        return new byte[0];
    }

    @Override
    public BaseLearner fromByteArray(byte[] learnerBytes) throws InvalidProtocolBufferException {
        return null;
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

    @Nonnull
    @Override
    protected String getLossOptionDescription() {
        return null;
    }
}
