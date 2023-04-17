package drillbit.classification;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import drillbit.BaseLearner;
import drillbit.FeatureValue;
import drillbit.TrainWeights;
import drillbit.dmlmanager.ParameterServerManager;
import drillbit.optimizer.*;
import drillbit.parameter.DenseWeights;
import drillbit.parameter.SparseWeights;
import drillbit.parameter.Weights;
import drillbit.protobuf.ClassificationPb;
import drillbit.utils.common.DoubleAccumulator;
import drillbit.utils.parser.StringParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public final class FedGeneralClassificationLearner extends BaseLearner {
    protected ConversionState cvState;
    protected boolean returnProba;
    // Model
    private Weights weights;
    private ArrayList<Weights> syncWeights;
    // For model storage and allocate
    private boolean dense;
    private int dims;
    // For iteratively training
    private int iters;
    // For mini batch update
    private boolean isMiniBatch;
    private int miniBatch;
    private ConcurrentHashMap<Object, DoubleAccumulator> accumulated;
    private int sampled;
    // For optimization
    private ConcurrentHashMap<String, String> optimizerOptions;
    private Optimizers.OptimizerBase optimizer;
    private LossFunctions.LossFunction lossFunction;
    // Conversion check
    private boolean chkCv;
    private double cvRate;
    //federated learning
    private ParameterServerManager manager;

    public FedGeneralClassificationLearner() {
        super();
        accumulated = new ConcurrentHashMap<>();
        returnProba = false;
    }

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
    public Options getPredictOptions() {
        Options opts = super.getPredictOptions();

        // Predict option only includes return the probability of input sample or not.
        opts.addOption("return_proba", false, "return probability of input sample");

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

        optimizerOptions = OptimizerOptions.create();
        OptimizerOptions.processOptions(cl, optimizerOptions);
        optimizer = createOptimizer();

        try {
            manager = new ParameterServerManager(optimizer);
        } catch (InterruptedException ex) {
            ;
        }

        iters = StringParser.parseInt(cl.getOptionValue("iters"), 1);
        if (iters < 1) {
            throw new IllegalArgumentException(String.format("invalid iterations of %d", iters));
        }

        miniBatch = StringParser.parseInt(cl.getOptionValue("mini_batch"), 1);
        if (miniBatch < 1) {
            throw new IllegalArgumentException(String.format("invalid mini batch size of %d", miniBatch));
        }

        isMiniBatch = miniBatch > 1;

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

    @Override
    public final CommandLine processPredictOptions(@Nonnull final CommandLine cl) {
        super.processPredictOptions(cl);

        returnProba = cl.hasOption("return_proba");

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

        syncWeights = new ArrayList<>();
        syncWeights.add(weights);
        return weights;
    }

    @Override
    public void add(@Nonnull String feature, @Nonnull String target) {
        ArrayList<FeatureValue> featureValues = parseFeatureList(feature);
        checkTargetValue(target);
        writeSample(featureValues, target);
    }

    @Override
    public final void train(@Nonnull ArrayList<FeatureValue> features, double target) {
        double p = predict(features);
        double y = target > 0.d ? 1.d : -1.d;

        update(features, y, p);
    }

    @Override
    public final Object predict(@Nonnull String features, @Nonnull String options) {
        if (!optionForPredictProcessed) {
            CommandLine cl = parsePredictOptions(options);
            processPredictOptions(cl);
            optionForPredictProcessed = true;
        }

        ArrayList<FeatureValue> featureVector = parseFeatureList(features);

        if (returnProba) {
            return predict(featureVector);
        } else {
            return predict(featureVector) > 0 ? 1 : -1;
        }
    }

    public final double predict(@Nonnull ArrayList<FeatureValue> features) {
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
    public final void update(@Nonnull ArrayList<FeatureValue> features, double target, double predicted) {
        optimizer.proceedStep();

        double loss = lossFunction.loss(predicted, target);
        cvState.incrLoss(loss); // retain cumulative loss to check convergence

        double dloss = lossFunction.dloss(predicted, target);
        if (dloss == 0.f) {
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
                manager.batchUpdate(syncWeights, accumulated);
                accumulated.clear();
                this.sampled = 0;
            }
        } else {
//            System.out.println(loss + " " + dloss);
            // weights = manager.onlineUpdate(features, loss, dloss, weights);
        }
    }

    @Override
    public final byte[] toByteArray() {
        ClassificationPb.GeneralClassifier.Builder builder = ClassificationPb.GeneralClassifier.newBuilder();

        builder.setDense(dense);
        builder.setDims(dims);
        builder.setWeights(ByteString.copyFrom(Objects.requireNonNull(weights.toByteArray())));

        return builder.build().toByteArray();
    }

    @Override
    public final BaseLearner fromByteArray(byte[] learnerBytes) throws InvalidProtocolBufferException {
        ClassificationPb.GeneralClassifier generalClassifier = ClassificationPb.GeneralClassifier.parseFrom(learnerBytes);

        dense = generalClassifier.getDense();
        dims = generalClassifier.getDims();
        byte[] modelBytes = generalClassifier.getWeights().toByteArray();

        if (dense) {
            weights = new DenseWeights(dims, TrainWeights.WeightType.Single).fromByteArray(modelBytes);
        } else {
            weights = new SparseWeights(dims, TrainWeights.WeightType.Single).fromByteArray(modelBytes);
        }

        return this;
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
                    manager.stopIteration();
                    manager.batchUpdate(syncWeights, accumulated);
                    accumulated.clear();
                    this.sampled = 0;
                }

                epochReadCleanUp();
            }
        } catch (Throwable e) {
            throw new IllegalArgumentException("Exception caused in the iterative training", e);
        } finally {
            readCleanUp();
        }
    }

    final void onlineUpdate(@Nonnull final ArrayList<FeatureValue> features, final double loss, final double dloss) {
        for (FeatureValue f : features) {
            Object feature = f.getFeature();
            double xi = f.getValueAsDouble();
            double weight = weights.getWeight(feature);
            double gradient = dloss * xi;
            final double new_weight = optimizer.update(feature, weight, loss, gradient);
            if (new_weight == 0.d) {
                weights.delete(feature);
                continue;
            }
            weights.setWeight(feature, new_weight);
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
    final public void finalizeTraining() {
        runIterativeTraining();

        if (isMiniBatch) {
            batchUpdate();
        }
    }

    @Override
    protected void checkTargetValue(String target) throws IllegalArgumentException {
        int targetValue = StringParser.parseInt(target, Integer.MAX_VALUE);
        if (targetValue != 1 && targetValue != -1 && targetValue != 0) {
            throw new IllegalArgumentException("Target should be either -1 or 1");
        }
    }

    @Override
    public void checkLossFunction(LossFunctions.LossFunction lossFunction) throws IllegalArgumentException {
        if (!lossFunction.forBinaryClassification()) {
            throw new IllegalArgumentException(String.format("Loss function %s not for classification", lossFunction.getType().toString()));
        }
    }

    @Override
    public LossFunctions.LossType getDefaultLossType() {
        return LossFunctions.LossType.HingeLoss;
    }

    @Nonnull
    @Override
    protected String getLossOptionDescription() {
        return "Loss function [HingeLoss (default), LogLoss, SquaredHingeLoss, ModifiedHuberLoss, or\n"
                + "a regression loss: SquaredLoss, QuantileLoss, EpsilonInsensitiveLoss, "
                + "SquaredEpsilonInsensitiveLoss, HuberLoss]";
    }
}
