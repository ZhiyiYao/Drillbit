package drillbit.classification.multiclass;

import drillbit.FeatureValue;
import drillbit.TrainWeights;
import drillbit.optimizer.EtaEstimator;
import drillbit.optimizer.LossFunctions;
import drillbit.parameter.Weights;
import drillbit.protobuf.SamplePb;
import drillbit.utils.common.DoubleAccumulator;
import drillbit.utils.math.MathUtils;
import drillbit.utils.parser.StringParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class SoftmaxRegressionLearner extends MulticlassClassificationBaseLearner {
    private EtaEstimator etaEstimator;

    // For mini batch update
    protected boolean isMiniBatch;
    protected int miniBatch;
    protected ConcurrentHashMap<String, ConcurrentHashMap<Object, DoubleAccumulator>> accumulated;
    protected int sampled;

    private int iter = 0;

    public SoftmaxRegressionLearner() {
        super();
    }

    @Override
    public final Options getOptions() {
        Options opts = super.getOptions();

        opts.addOption("mini_batch", true, "mini batch size");
        opts.addOption("power_t", true, "The exponent for inverse scaling learning rate [default: " + EtaEstimator.DEFAULT_POWER_T + "]");
        opts.addOption("eta0", true, "The initial learning rate [default: " + EtaEstimator.DEFAULT_ETA0 + "]");
        opts.addOption("mini_batch", true, "mini batch size");

        return opts;
    }

    @Override
    public CommandLine processOptions(@Nonnull final CommandLine cl) {
        super.processOptions(cl);

        etaEstimator = EtaEstimator.get(cl);

        miniBatch = StringParser.parseInt(cl.getOptionValue("mini_batch"), 1);
        if (miniBatch < 1) {
            throw new IllegalArgumentException(String.format("invalid mini batch size of %d", miniBatch));
        }

        isMiniBatch = miniBatch > 1;

        accumulated = new ConcurrentHashMap<>();

        return cl;
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
            ArrayList<Double> scores = calcScoreForAllClasses(featureVector);
            return scoresToString(MathUtils.softmax(scores));
        }
        else {
            return classify(featureVector).getLabel();
        }
    }

    @Override
    public final void train(@Nonnull ArrayList<FeatureValue> features, @Nonnull String actualLabel) {
        ArrayList<Double> scores = calcScoreForAllClasses(features);
        update(features, scores, actualLabel);
    }

    @Override
    public final void runIterativeTraining() {
        try {
            outputStream.close();
        }
        catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

        try {
            for (iter = 1; iter <= iters; iter++) {
                while (true) {
                    SamplePb.Sample sample = readSampleFromTempFile();
                    if (sample == null) {
                        // One iteration completes
                        break;
                    }
                    int featureVectorSize = sample.getFeatureList().size();
                    String target = sample.getTarget().trim();
                    ArrayList<FeatureValue> featureValueVector = new ArrayList<>();
                    for (int j = 0; j < featureVectorSize; j++) {
                        featureValueVector.add(StringParser.parseFeature(sample.getFeature(j)));
                    }

                    int index = getLabelIndex(target);
                    if (index == -1) {
                        labels.add(target);
                        models.add(createWeights(dense, dims));
                        nClasses++;
                    }
                    train(featureValueVector, target);
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
            File file = new File(String.valueOf(path));
            file.delete();
        }
    }

    public void update(@Nonnull final ArrayList<FeatureValue> features, @Nonnull final ArrayList<Double> scores, @Nonnull final String actualLabel) {
        if (scores.size() != nClasses) {
            throw new IllegalArgumentException("Length of scores does not match number of classes.");
        }

        ArrayList<Double> softmaxScores = MathUtils.softmax(scores);
        if (isMiniBatch) {
            // Do batch update
            for (int i = 0; i < nClasses; i++) {
                String label = labels.get(i);
                ConcurrentHashMap<Object, DoubleAccumulator> accumulator = accumulated.get(label);
                if (accumulator == null) {
                    accumulator = new ConcurrentHashMap<>();
                }
                Weights model = models.get(i);

                double score = softmaxScores.get(i);
                double loss = computeLoss(label.equals(actualLabel) ? 1 : 0, score);

                accumulateUpdate(accumulator, model, features, loss);
                accumulated.put(label, accumulator);
            }
            sampled++;
            if (sampled >= miniBatch) {
                batchUpdate();
            }
        }
        else {
            for (int i = 0; i < nClasses; i++) {
                String label = labels.get(i);
                Weights model = models.get(i);
                double score = softmaxScores.get(i);
                double loss = computeLoss(label.equals(actualLabel) ? 1 : 0, score);

                onlineUpdate(model, features, loss);
                models.set(i, model);
            }
        }
    }

    final void batchUpdate() {
        // Batch update for weights for different classes.
        assert accumulated != null;
        if (accumulated.isEmpty()) {
            sampled = 0;
            return;
        }

        for (ConcurrentHashMap.Entry<String, ConcurrentHashMap<Object, DoubleAccumulator>> e : accumulated.entrySet()) {
            String label = e.getKey();
            ConcurrentHashMap<Object, DoubleAccumulator> accumulator = e.getValue();
            int index = getLabelIndex(label);
            Weights model = models.get(index);
            for (Map.Entry<Object, DoubleAccumulator> e1 : accumulator.entrySet()) {
                Object feature = e.getKey();
                DoubleAccumulator v = e1.getValue();
                final double newWeight = v.get();
                if (newWeight == 0.d) {
                    model.delete(feature);
                    continue;
                }
                model.setWeight(feature, newWeight);
            }
            models.set(index, model);
        }

        accumulated.clear();
        this.sampled = 0;
    }

    final void accumulateUpdate(@Nonnull ConcurrentHashMap<Object, DoubleAccumulator> accumulator, @Nonnull final Weights model, @Nonnull final ArrayList<FeatureValue> features, final double loss) {
        for (FeatureValue f : features) {
            Object feature = f.getFeature();
            double xi = f.getValueAsDouble();
            double oldWeight = model.getWeight(feature);

            // eta controls the learning rate
            double newWeight = oldWeight + etaEstimator.eta(iter) * loss * xi;
            DoubleAccumulator acc = accumulator.get(feature);
            if (acc == null) {
                acc = new DoubleAccumulator(newWeight);
                accumulator.put(feature, acc);
            }
            else {
                acc.add(newWeight);
            }
        }
    }

    protected void onlineUpdate(@Nonnull Weights weights, @Nonnull final ArrayList<FeatureValue> features, double loss) {
        for (FeatureValue f : features) {// w[i] += y * x[i]
            if (f == null) {
                continue;
            }

            final Object x = f.getFeature();
            final double xi = f.getValueAsDouble();

            double oldW = weights.getWeight(x);
            double newW = oldW + etaEstimator.eta(iter) * loss * xi;
            weights.set(x, new TrainWeights.SingleWeight(newW));
        }
    }

    protected double computeLoss(double target, double predicted) {
        return LossFunctions.logisticLoss(target, predicted);
    }
}
