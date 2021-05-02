package drillbit.classification.multiclass;

import drillbit.FeatureValue;
import drillbit.TrainWeights;
import drillbit.parameter.PredictionResult;
import drillbit.parameter.Weights;
import drillbit.utils.Margin;
import drillbit.utils.math.StatsUtils;
import drillbit.utils.parser.StringParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import javax.annotation.Nonnull;
import java.util.ArrayList;

public final class MulticlassConfidenceWeightedClassificationLearner extends MulticlassClassificationBaseLearner {
    private double phi;

    private static double DEFAULT_PHI = 1.d;

    @Override
    public final Options getOptions() {
        Options opts = super.getOptions();

        opts.addOption("phi", true, "Confidence parameter [default 1.0]");
        opts.addOption("eta", true, "Confidence hyperparameter eta in range (0.5, 1] [default 0.85]");

        return opts;
    }

    @Override
    public final CommandLine processOptions(@Nonnull final CommandLine cl) {
        super.processOptions(cl);

        phi = StringParser.parseDouble(cl.getOptionValue("phi"), DEFAULT_PHI);
        if (cl.hasOption("eta")) {
            double eta = StringParser.parseDouble(cl.getOptionValue("eta"), 0.85);
            if (eta <= 0.5 || eta > 1) {
                throw new IllegalArgumentException("Confidence hyperparameter eta must be in range (0.5, 1]");
            }

            phi = StatsUtils.probit(eta, 5d);
        }

        return cl;
    }

    @Override
    public void train(@Nonnull ArrayList<FeatureValue> features, @Nonnull String actualLabel) {
        Margin margin = getMarginAndVariance(features, actualLabel, true);
        double gamma = getGamma(margin);

        if (gamma > 0.d) {// alpha = max(0, gamma)
            String missedLabel = margin.getMaxIncorrectLabel();
            update(features, gamma, actualLabel, missedLabel);
        }
    }

    private void update(ArrayList<FeatureValue> features, double alpha, String actualLabel, String missedLabel) {
        assert (actualLabel != null);
        if (actualLabel.equals(missedLabel)) {
            throw new IllegalArgumentException("Actual label equals to missed label: " + actualLabel);
        }

        if (getLabelIndex(actualLabel) == -1) {
            Weights model2add = createWeights(dense, dims);
            models.add(model2add);
            labels.add(actualLabel);
            nClasses++;
        }
        Weights model2add = models.get(getLabelIndex(actualLabel));

        if (missedLabel != null) {
            if (getLabelIndex(missedLabel) == -1) {
                Weights model2sub = createWeights(dense, dims);
                models.add(model2sub);
                labels.add(missedLabel);
                nClasses++;
            }
        }
        Weights model2sub = models.get(getLabelIndex(missedLabel));

        for (FeatureValue f : features) {// w[f] += y * x[f]
            if (f == null) {
                continue;
            }
            final String k = f.getFeature();
            final double v = f.getValueAsDouble();

            TrainWeights.ExtendedWeight oldCorrectclassW = model2add.get(k);
            TrainWeights.ExtendedWeight newCorrectclassW = getNewWeight(oldCorrectclassW, v, alpha, phi, true);
            model2add.set(k, newCorrectclassW);

            if (model2sub != null) {
                TrainWeights.ExtendedWeight oldWrongclassW = model2sub.get(k);
                TrainWeights.ExtendedWeight newWrongclassW =
                        getNewWeight(oldWrongclassW, v, alpha, phi, false);
                model2sub.set(k, newWrongclassW);
            }
        }

        models.set(getLabelIndex(missedLabel), model2sub);
        models.set(getLabelIndex(actualLabel), model2add);
    }

    protected final double getGamma(Margin margin) {
        double m = margin.get();
        double var = margin.getVariance();
        assert (var != 0);

        double b = 1.d + 2.d * phi * m;
        double gamma_numer = -b + (double) Math.sqrt(b * b - 8.d * phi * (m - phi * var));
        double gamma_denom = 4.d * phi * var;
        if (gamma_denom == 0.d) {// avoid divide-by-zero
            return 0.d;
        }
        return gamma_numer / gamma_denom;
    }

    protected Margin getMarginAndVariance(@Nonnull final ArrayList<FeatureValue> features, final Object actual_label, boolean nonZeroVariance) {
        double correctScore = 0.d;
        double correctVariance = 0.d;
        String maxAnotherLabel = null;
        double maxAnotherScore = 0.d;
        double maxAnotherVariance = 0.d;

        if (nonZeroVariance && labels.isEmpty()) {// for initial call
            double var = 2.d * calcVariance(features);
            return new Margin(correctScore, maxAnotherLabel, maxAnotherScore).variance(var);
        }

        for (int i = 0; i < nClasses; i++) {// for each class
            String label = labels.get(i);
            Weights model = models.get(i);
            PredictionResult predicted = calcScoreAndVariance(model, features);
            double score = predicted.getScore();

            if (label.equals(actual_label)) {
                correctScore = score;
                correctVariance = predicted.getVariance();
            }
            else {
                if (maxAnotherLabel == null || score > maxAnotherScore) {
                    maxAnotherLabel = label;
                    maxAnotherScore = score;
                    maxAnotherVariance = predicted.getVariance();
                }
            }
        }

        return new Margin(correctScore, maxAnotherLabel, maxAnotherScore).variance(correctVariance + maxAnotherVariance);
    }

    private static TrainWeights.ExtendedWeight getNewWeight(final TrainWeights.ExtendedWeight old, final double x, final double alpha, final double phi, final boolean positive) {
        final double old_w, old_cov;
        if (old == null) {
            old_w = 0.d;
            old_cov = 1.d;
        } else {
            old_w = old.get();
            old_cov = old.getCovar();
        }

        double delta_w = alpha * old_cov * x;
        double new_w = positive ? old_w + delta_w : old_w - delta_w;
        double new_cov = 1.d / (1.d / old_cov + (2.d * alpha * phi * x * x));
        return new TrainWeights.WeightWithCovar(new_w, new_cov);
    }
}
