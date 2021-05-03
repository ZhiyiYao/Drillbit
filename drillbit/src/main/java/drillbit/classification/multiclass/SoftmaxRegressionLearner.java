package drillbit.classification.multiclass;

import drillbit.FeatureValue;
import drillbit.TrainWeights;
import drillbit.optimizer.EtaEstimator;
import drillbit.optimizer.LossFunctions;
import drillbit.parameter.Weights;
import drillbit.utils.math.MathUtils;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import javax.annotation.Nonnull;
import java.util.ArrayList;

public class SoftmaxRegressionLearner extends MulticlassClassificationBaseLearner {
    private EtaEstimator etaEstimator;

    public SoftmaxRegressionLearner() {
        super();
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
            return classify(featureVector);
        }
    }

    @Override
    public final void train(@Nonnull ArrayList<FeatureValue> features, @Nonnull String actualLabel) {
        ArrayList<Double> scores = calcScoreForAllClasses(features);
        update(features, scores, actualLabel);
    }

    public void update(@Nonnull final ArrayList<FeatureValue> features, @Nonnull final ArrayList<Double> scores, @Nonnull final String actualLabel) {
        assert (actualLabel != null);
        if (scores.size() != nClasses) {
            throw new IllegalArgumentException("Length of scores does not match number of classes.");
        }

        if (getLabelIndex(actualLabel) == -1) {
            labels.add(actualLabel);
            models.add(createWeights(dense, dims));
            nClasses++;
        }

        for (int i = 0; i < nClasses; i++) {
            String label = labels.get(i);
            Weights model = models.get(i);
            double score = scores.get(i);
            double grad = computeGradient(label.equals(actualLabel) ? 1 : 0, score);
            // Here we only support online update.
            onlineUpdate(model, features, grad);
            models.set(i, model);
        }
    }

    protected void onlineUpdate(@Nonnull Weights weights, @Nonnull final ArrayList<FeatureValue> features, double coeff) {
        for (FeatureValue f : features) {// w[i] += y * x[i]
            if (f == null) {
                continue;
            }

            final Object x = f.getFeature();
            final double xi = f.getValueAsDouble();

            double oldW = weights.getWeight(x);
            double newW = oldW + (coeff * xi);
            weights.set(x, new TrainWeights.SingleWeight(newW));
        }
    }

    protected double computeGradient(double target, double predicted) {
        double eta = etaEstimator.eta(0);
        double gradient = LossFunctions.logisticLoss(target, predicted);
        return eta * gradient;
    }
}
