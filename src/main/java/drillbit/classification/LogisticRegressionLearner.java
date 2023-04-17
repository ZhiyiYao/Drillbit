package drillbit.classification;

import drillbit.FeatureValue;
import drillbit.TrainWeights;
import drillbit.optimizer.EtaEstimator;
import drillbit.optimizer.LossFunctions;
import drillbit.utils.common.DoubleAccumulator;
import drillbit.utils.math.MathUtils;
import drillbit.utils.parser.StringParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class LogisticRegressionLearner extends BinaryClassificationBaseLearner {
    private EtaEstimator etaEstimator;

    // For mini batch update
    private boolean isMiniBatch;
    private int miniBatch;
    private ConcurrentHashMap<Object, DoubleAccumulator> accumulated;
    private int sampled;

    public LogisticRegressionLearner() {
        super();
        accumulated = new ConcurrentHashMap<>();
    }

    @Override
    public final Options getOptions() {
        Options opts = super.getOptions();

        opts.addOption("mini_batch", true, "mini batch size");
        opts.addOption("power_t", true, "The exponent for inverse scaling learning rate [default: " + EtaEstimator.DEFAULT_POWER_T + "]");
        opts.addOption("eta0", true, "The initial learning rate [default: " + EtaEstimator.DEFAULT_ETA0 + "]");

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

        return cl;
    }

    @Override
    final public void finalizeTraining() {
        runIterativeTraining();

        if (isMiniBatch) {
            batchUpdate();
        }
    }

    @Override
    public final void runIterativeTraining() {
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

    @Override
    protected void train(@Nonnull ArrayList<FeatureValue> features, @Nonnull double target) {
        // Here we don't use optimizer to update weights.
        // Logistic regression uses 0 and 1 as target value.
        final double y = target > 0 ? 1.d : 0.d;
        final double p = predict(features);
        update(features, y, p);
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
            return MathUtils.logistic(predict(featureVector));
        } else {
            return predict(featureVector) > 0 ? 1 : 0;
        }
    }

    public final void update(@Nonnull final ArrayList<FeatureValue> features, final double target, final double predicted) {
        final double grad = computeGradient(target, predicted);

        if (isMiniBatch) {
            accumulateUpdate(features, grad);
            if (sampled >= miniBatch) {
                batchUpdate();
            }
        } else {
            onlineUpdate(features, grad);
        }
    }

    final void accumulateUpdate(@Nonnull final ArrayList<FeatureValue> features, final double coeff) {
        for (FeatureValue f : features) {
            if (f == null) {
                continue;
            }
            final Object x = f.getFeature();
            final double xi = f.getValueAsDouble();
            double delta = xi * coeff;

            DoubleAccumulator acc = accumulated.get(x);
            if (acc == null) {
                acc = new DoubleAccumulator(delta);
                accumulated.put(x, acc);
            } else {
                acc.add(delta);
            }
        }
        sampled++;
    }

    protected void batchUpdate() {
        if (accumulated.isEmpty()) {
            this.sampled = 0;
            return;
        }

        for (Map.Entry<Object, DoubleAccumulator> e : accumulated.entrySet()) {
            Object x = e.getKey();
            DoubleAccumulator v = e.getValue();
            double delta = v.get();

            double old_w = weights.getWeight(x);
            double new_w = old_w + delta;
            weights.set(x, new TrainWeights.SingleWeight(new_w));
        }
        accumulated.clear();
        this.sampled = 0;
    }

    protected void onlineUpdate(@Nonnull final ArrayList<FeatureValue> features, double coeff) {
        for (FeatureValue f : features) {// w[i] += y * x[i]
            if (f == null) {
                continue;
            }

            final Object x = f.getFeature();
            final double xi = f.getValueAsDouble();

            double old_w = weights.getWeight(x);
            double new_w = old_w + (coeff * xi);
            weights.set(x, new TrainWeights.SingleWeight(new_w));
        }
    }

    protected double computeGradient(final double target, final double predicted) {
        double eta = etaEstimator.eta(0);
        double gradient = LossFunctions.logisticLoss(target, predicted);
        return eta * gradient;
    }

    @Override
    public final void checkTargetValue(final String target) throws IllegalArgumentException {
        double value = StringParser.parseDouble(target, Double.NaN);
        if (value < 0.d || value > 1.d) {
            throw new IllegalArgumentException("target must be in range 0 to 1: " + target);
        }
    }
}
