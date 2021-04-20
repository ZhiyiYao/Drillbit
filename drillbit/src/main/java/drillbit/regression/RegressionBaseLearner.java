package drillbit.regression;

import com.google.protobuf.InvalidProtocolBufferException;
import drillbit.BaseLearner;
import drillbit.FeatureValue;
import drillbit.TrainWeights;
import drillbit.optimizer.ConversionState;
import drillbit.optimizer.LossFunctions;
import drillbit.parameter.DenseWeights;
import drillbit.parameter.SparseWeights;
import drillbit.parameter.Weights;
import drillbit.utils.parser.StringParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import javax.annotation.Nonnull;

import java.util.ArrayList;

public class RegressionBaseLearner extends BaseLearner {
    // Model
    protected Weights weights;
    protected int count;

    // For model storage and allocate
    protected boolean dense;
    protected int dims;

    // For iteratively training
    protected int iters;

    // Conversion check
    private boolean chkCv;
    private double cvRate;
    protected ConversionState cvState;

    @Override
    @Nonnull
    public Options getOptions() {
        Options opts = super.getOptions();

        opts.addOption("dense", false, "Use dense model or not");
        opts.addOption("dims", true, "The dimension of model [default: 16777216 (2^24)]");
        opts.addOption("iters", true, "The number of training iterations");
        opts.addOption("mini_batch", true, "mini batch size");
        opts.addOption("loss", true, "loss function name");
        opts.addOption("chk_cv", false, "whether to check conversion");
        opts.addOption("cv_rate", true, "conversion rate used in checking");

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

        chkCv = cl.hasOption("chk_cv");
        if (cl.hasOption("cv_rate")) {
            cvRate = StringParser.parseDouble(cl.getOptionValue("cv_rate"), cvRate);
            chkCv = true;
        }
        cvState = new ConversionState(chkCv, cvRate);

        weights = createModel(dense, dims);
        count = 0;

        return cl;
    }

    @Override
    protected void train(@Nonnull ArrayList<FeatureValue> featureVector, double target) {
        double score = predict(featureVector);
        update(featureVector, target, score);
    }

    @Override
    public Object predict(@Nonnull String features, @Nonnull String options) {
        // Ignore options
        return predict(parseFeatureList(features));
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
            if (old_w != 0.d) {
                score += (old_w * v);
            }
        }
        return score;
    }

    @Override
    protected void update(@Nonnull ArrayList<FeatureValue> features, double target, double predicted) {
//        final double grad = computeGradient(target, predicted);

//        if () {
//            accumulateUpdate(features, grad);
//            if (sampled >= mini_batch_size) {
//                batchUpdate();
//            }
//        } else {
//            onlineUpdate(features, grad);
//        }
    }

    /**
     * Compute a gradient by using a loss function in derived classes
     */
    protected float computeGradient(float target, float predicted) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void add(@Nonnull String feature, @Nonnull String target, @Nonnull String commandLine) {
        if (!optionProcessed) {
            CommandLine cl = parseOptions(commandLine);
            processOptions(cl);
            optionProcessed = true;
        }

        ArrayList<FeatureValue> featureValues = parseFeatureList(feature);
        checkTargetValue(target);
        double targetValue = StringParser.parseDouble(target, 0.d);

        count++;
        train(featureValues, targetValue);
    }

    @Override
    public void add(@Nonnull String feature, @Nonnull String target) {
        add(feature, target, "");
    }

    @Override
    public byte[] toByteArray() {
        return weights.toByteArray();
    }

    @Override
    public BaseLearner fromByteArray(byte[] learnerBytes) throws InvalidProtocolBufferException {
        weights.fromByteArray(learnerBytes);

        return this;
    }

    @Override
    protected void checkTargetValue(String target) throws IllegalArgumentException {
        // Do nothing.
    }

    @Override
    public void checkLossFunction(LossFunctions.LossFunction lossFunction) throws IllegalArgumentException {
        if (!lossFunction.forRegression()) {
            throw new IllegalArgumentException(String.format("Loss function %s not for regression.", lossFunction));
        }
    }

    @Override
    public LossFunctions.LossType getDefaultLossType() {
        return null;
    }

    @Nonnull
    @Override
    protected String getLossOptionDescription() {
        return "";
    }
}
