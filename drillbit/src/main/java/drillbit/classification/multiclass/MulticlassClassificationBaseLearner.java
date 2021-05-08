package drillbit.classification.multiclass;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import drillbit.BaseLearner;
import drillbit.FeatureValue;
import drillbit.TrainWeights;
import drillbit.parameter.*;
import drillbit.optimizer.LossFunctions;
import drillbit.protobuf.ClassificationPb;
import drillbit.protobuf.SamplePb;
import drillbit.utils.math.MathUtils;
import drillbit.utils.parser.StringParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

/*
    One-to-all multiclass classifier.
 */
public abstract class MulticlassClassificationBaseLearner extends BaseLearner {
    // Models
    protected ArrayList<Weights> models;
    protected ArrayList<String> labels;
    protected int nClasses;

    // For model storage and allocate
    protected boolean dense;
    protected int dims;

    // For iteratively training
    protected int iters;
    protected final int DEFAULT_ITERS = 20;

    // Options for predict
    protected boolean returnProba;

    public MulticlassClassificationBaseLearner() {
        super();
        returnProba = false;
    }

    @Override
    @Nonnull
    public Options getOptions() {
        Options opts = super.getOptions();

        opts.addOption("dense", false, "Use dense model or not");
        opts.addOption("dims", true, "The dimension of model [default: 16777216 (2^24)]");
        opts.addOption("iters", true, "number of iterations");
        opts.addOption("labels", true, "list of labels");

        return opts;
    }

    @Override
    @Nonnull
    public Options getPredictOptions() {
        Options opts = super.getPredictOptions();

        opts.addOption("return_proba", false, "Return probability for each class");

        return opts;
    }

    @Override
    public CommandLine processOptions(@Nonnull final CommandLine cl) {
        super.processOptions(cl);

        dense = cl.hasOption("dense");

        dims = StringParser.parseInt(cl.getOptionValue("dims"), -1);
        if (dims < 0) {
            dims = dense ? DEFAULT_DENSE_DIMS : DEFAULT_SPARSE_DIMS;
        }

        iters = StringParser.parseInt(cl.getOptionValue("iters"), DEFAULT_ITERS);
        if (iters < 1) {
            throw new IllegalArgumentException(String.format("invalid iterations of %d", iters));
        }

        labels = new ArrayList<>();
        models = new ArrayList<>();
        if (cl.hasOption("labels")) {
            String[] labelList = StringParser.parseList(cl.getOptionValue("labels"));
            for (String label : labelList) {
                labels.add(label);
                models.add(createWeights(dense, dims));
            }
        }

        nClasses = labels.size();

        return cl;
    }

    @Override
    public CommandLine processPredictOptions(@Nonnull final CommandLine cl) {
        super.processPredictOptions(cl);

        returnProba = cl.hasOption("return_proba");

        return cl;
    }

    @Override
    public void add(@Nonnull final String feature, @Nonnull final String target) {
        ArrayList<FeatureValue> featureVector = parseFeatureList(feature);
        checkTargetValue(target);
        writeSampleToTempFile(featureVector, target);
    }

    protected abstract void train(@Nonnull final ArrayList<FeatureValue> features, @Nonnull final String actual_label);

    @Override
    protected void train(@Nonnull ArrayList<FeatureValue> featureVector, @Nonnull double target) {
        // Double target value is unsupported for multiclass tasks.
        throw new UnsupportedOperationException();
    }

    @Override
    protected void update(@Nonnull ArrayList<FeatureValue> features, double target, double predicted) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object predict(@Nonnull String features, @Nonnull String options) {
        if (!optionForPredictProcessed) {
            CommandLine cl = parsePredictOptions(options);
            processPredictOptions(cl);
            optionForPredictProcessed = true;
        }

        ArrayList<FeatureValue> featureVector = parseFeatureList(features);

        if (returnProba) {
            PredictionResult result = classify(featureVector);
            return result.getLabel();
        }
        else {
            return scoresToString(calcScoreForAllClasses(featureVector));
        }
    }

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

    @Nonnull
    protected String scoresToString(@Nonnull final ArrayList<Double> scores) {
        if (scores.size() != nClasses) {
            throw new IllegalArgumentException("Length of input scores does not match number of classes.");
        }

        StringBuilder builder = new StringBuilder();

        builder.append("[");
        for (int i = 0; i < nClasses; i++) {
            builder.append(labels.get(i));
            builder.append(": ");
            builder.append(scores.get(i));
            builder.append(", ");
        }
        builder.append("]");

        return builder.toString();
    }

    @Override
    public final byte[] toByteArray() throws IllegalArgumentException, UnsupportedOperationException {
        ClassificationPb.MulticlassClassifier.Builder builder = ClassificationPb.MulticlassClassifier.newBuilder();
        ClassificationPb.MulticlassClassifier.LabelAndWeights.Builder labelAndWeightsBuilder = ClassificationPb.MulticlassClassifier.LabelAndWeights.newBuilder();

        builder.setDense(dense);
        builder.setDims(dims);
        for (int i = 0; i < nClasses; i++) {
            labelAndWeightsBuilder.clear();
            builder.addLabel2Weights(labelAndWeightsBuilder.setLabel(labels.get(i)).setWeights(ByteString.copyFrom(Objects.requireNonNull(models.get(i).toByteArray()))).build());
        }

        return builder.build().toByteArray();
    }

    @Override
    public final BaseLearner fromByteArray(byte[] learnerBytes) throws InvalidProtocolBufferException {
        ClassificationPb.MulticlassClassifier multiclassClassifier = ClassificationPb.MulticlassClassifier.parseFrom(learnerBytes);

        dense = multiclassClassifier.getDense();
        dims = multiclassClassifier.getDims();
        labels = new ArrayList<>();
        models = new ArrayList<>();
        for (ClassificationPb.MulticlassClassifier.LabelAndWeights labelAndWeights : multiclassClassifier.getLabel2WeightsList()) {
            labels.add(labelAndWeights.getLabel());
            if (dense) {
                models.add((new DenseWeights(dims, TrainWeights.WeightType.Single)).fromByteArray(labelAndWeights.getWeights().toByteArray()));
            }
            else {
                models.add((new SparseWeights(dims, TrainWeights.WeightType.Single)).fromByteArray(labelAndWeights.getWeights().toByteArray()));
            }
        }

        nClasses = labels.size();

        return this;
    }

    @Override
    protected void finalizeTraining() {
        runIterativeTraining();
    }

    void runIterativeTraining() {
        try {
            outputStream.close();
        }
        catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

        try {
            for (int iter = 1; iter <= iters; iter++) {
                // For learner with variant learning rate, the iteration may have to count from 1. e.g. the softmax learner.
                while (true) {
                    SamplePb.Sample sample = readSampleFromTempFile();
                    if (sample == null) {
                        break;
                    }
                    int featureVectorSize = sample.getFeatureList().size();
                    String target = sample.getTarget().trim();
                    ArrayList<FeatureValue> featureValueVector = new ArrayList<>();
                    for (int j = 0; j < featureVectorSize; j++) {
                        featureValueVector.add(StringParser.parseFeature(sample.getFeature(j)));
                    }

                    if (getLabelIndex(target) == -1) {
                        throw new IllegalArgumentException(String.format("Invalid class label of %s", target));
                    }

                    train(featureValueVector, target);
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
            File file = new File(String.valueOf(path));
            file.delete();
        }
    }

    protected final PredictionResult classify(@Nonnull final ArrayList<FeatureValue> features) {
        ArrayList<Double> scores = calcScoreForAllClasses(features);
        int index = MathUtils.whichMax(scores);

        String maxScoredLabel = labels.get(index);
        double maxScore = scores.get(index);

        return new PredictionResult(maxScoredLabel, maxScore);
    }

    protected ArrayList<Double> calcScoreForAllClasses(@Nonnull final ArrayList<FeatureValue> features) {
        ArrayList<Double> scores = new ArrayList<>();
        for (Weights weights : models) {
            // Compute score for each class.
            scores.add(calcScore(weights, features));
        }

        return scores;
    }

    protected double calcScore(@Nonnull final Weights weights, @Nonnull final ArrayList<FeatureValue> features) {
        double score = 0.d;
        for (FeatureValue f : features) {// a += w[i] * x[i]
            if (f == null) {
                continue;
            }
            final String k = f.getFeature();
            final double v = f.getValueAsDouble();

            double oldW = weights.getWeight(k);
            if (oldW != 0.d) {
                score += oldW * v;
            }
        }
        return score;
    }

    @Override
    public final void checkTargetValue(String target) throws IllegalArgumentException {
        // Do nothing for multiclass classification task.
    }

    protected int getLabelIndex(String label) {
        for (int i = 0; i < labels.size(); i++) {
            if (label.equals(labels.get(i))) {
                return i;
            }
        }

        return -1;
    }

    @Override
    public final void checkLossFunction(LossFunctions.LossFunction lossFunction) throws IllegalArgumentException {
        throw new UnsupportedOperationException();
    }

    @Override
    public final LossFunctions.LossType getDefaultLossType() {
        throw new UnsupportedOperationException();
    }

    @Nonnull
    @Override
    public final String getLossOptionDescription() {
        throw new UnsupportedOperationException();
    }
}
