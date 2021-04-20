package drillbit.classification;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import drillbit.BaseLearner;
import drillbit.FeatureValue;
import drillbit.TrainWeights;
import drillbit.parameter.*;
import drillbit.optimizer.ConversionState;
import drillbit.optimizer.LossFunctions;
import drillbit.protobuf.ClassificationPb;
import drillbit.protobuf.SamplePb;
import drillbit.utils.parser.StringParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import javax.annotation.Nonnull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;


public abstract class BinaryClassificationBaseLearner extends BaseLearner {
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
    public void add(@Nonnull final String feature, @Nonnull final String target, @Nonnull final String commandLine) {
        if (!optionProcessed) {
            // parse commandline value here.
            CommandLine cl = parseOptions(commandLine);
            processOptions(cl);
            optionProcessed = true;
        }

        ArrayList<String> featureValues = StringParser.parseArray(feature);
        ArrayList<FeatureValue> featureVector = new ArrayList<>();
        assert featureValues != null;
        for (String featureValue : featureValues) {
            featureVector.add(StringParser.parseFeature(featureValue));
        }

        checkTargetValue(target);

        train(featureVector, StringParser.parseDouble(target, -1));
        count++;
    }

    @Override
    public void add(@Nonnull final String feature, @Nonnull final String target) {
        add(feature, target, "");
    }

    // Here we don't use optimizer to update weights
    @Override
    protected void train(@Nonnull ArrayList<FeatureValue> features, @Nonnull double target) {
        final double y = target > 0 ? 1d : -1d;
        final double p = predict(features);
        final double z = p * y;
        if (z <= 0.f) { // miss labeled
            update(features, y, p);
        }
    }

    public double predict(@Nonnull final ArrayList<FeatureValue> features) {
        double score = 0.f;
        for (FeatureValue f : features) {// a += w[i] * x[i]
            if (f == null) {
                continue;
            }
            Object k = f.getFeature();
            double old_w = weights.getWeight(k);
            if (old_w != 0.f) {
                score += (old_w * f.getValueAsFloat());
            }
        }
        return score;
    }

    @Override
    public Object predict(@Nonnull String features, @Nonnull String options) {
        ArrayList<String> featureValues = StringParser.parseArray(features);
        ArrayList<FeatureValue> featureVector = new ArrayList<>();
        assert featureValues != null;
        for (String featureValue : featureValues) {
            featureVector.add(StringParser.parseFeature(featureValue));
        }

        return predict(featureVector) > 0 ? 1 : -1;
    }

    @Override
    protected void update(@Nonnull final ArrayList<FeatureValue> features, double y, double p) {
        throw new IllegalStateException("Update should not be called");
    }

    protected void update(@Nonnull final FeatureValue[] features, final double coeff) {
        for (FeatureValue f : features) {// w[f] += y * x[f]
            if (f == null) {
                continue;
            }
            final Object k = f.getFeature();
            final double v = f.getValueAsDouble();

            double old_w = weights.getWeight(k);
            double new_w = old_w + (coeff * v);
            weights.setWeight(k, new_w);
        }
    }

    @Override
    public void checkLossFunction(LossFunctions.LossFunction lossFunction) throws IllegalArgumentException {
        if (!lossFunction.forBinaryClassification()) {
            throw new IllegalArgumentException(String.format("Loss function %s is not for binary classification.", lossFunction.getType()));
        }
    }

    @Override
    public LossFunctions.LossType getDefaultLossType() {
        //TODO: decide to keep abstract (let derived class implement this method) or return a default value
        return null;
    }

    @Nonnull
    @Override
    protected String getLossOptionDescription() {
        //TODO: complete loss option description
        return "";
    }

    @Nonnull
    protected PredictionResult calcScore(@Nonnull final ArrayList<FeatureValue> features) {
        double score = predict(features);

        return new PredictionResult(score > 0 ? "1" : "-1", score);
    }

    @Nonnull
    protected PredictionResult calcScoreAndVariance(@Nonnull final ArrayList<FeatureValue> features) {
        float score = 0.f;
        float variance = 0.f;

        for (FeatureValue f : features) {// a += w[i] * x[i]
            if (f == null) {
                continue;
            }
            final Object k = f.getFeature();
            final float v = f.getValueAsFloat();

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
    protected void finalizeTraining() {
        runIterativeTraining();
    }

    @Override
    public byte[] toByteArray() {
        logger.info("Trained a binary classification model using " + count + " training examples");

        ClassificationPb.BinaryClassifier.Builder builder = ClassificationPb.BinaryClassifier.newBuilder();

        builder.setDense(dense);
        builder.setDims(dims);
        builder.setWeights(ByteString.copyFrom(weights.toByteArray()));

        return builder.build().toByteArray();
    }

    @Override
    public BaseLearner fromByteArray(byte[] learnerBytes) throws InvalidProtocolBufferException {
        ClassificationPb.BinaryClassifier binaryClassifier = ClassificationPb.BinaryClassifier.parseFrom(learnerBytes);

        dense = binaryClassifier.getDense();
        dims = binaryClassifier.getDims();
        byte[] modelBytes = binaryClassifier.getWeights().toByteArray();

        if (dense) {
            weights = (new DenseWeights(dims, TrainWeights.WeightType.Single)).fromByteArray(modelBytes);
        }
        else {
            weights = (new SparseWeights(dims, TrainWeights.WeightType.Single)).fromByteArray(modelBytes);
        }

        return this;
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

    @Override
    protected void checkTargetValue(final String target) throws IllegalArgumentException {
        // Binary classification targets must be 0 or 1
        int targetValue = StringParser.parseInt(target, -2);
        if (targetValue != -1 && targetValue != 0 && targetValue != 1) {
            throw new IllegalArgumentException("Target should be either -1 or 1.");
        }
    }
}
