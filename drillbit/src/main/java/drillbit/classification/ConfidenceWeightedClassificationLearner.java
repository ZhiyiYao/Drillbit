package drillbit.classification;

import drillbit.FeatureValue;
import drillbit.TrainWeights;
import drillbit.parameter.*;
import drillbit.utils.math.MathUtils;
import drillbit.utils.parser.StringParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import javax.annotation.Nonnull;
import java.util.ArrayList;

public final class ConfidenceWeightedClassificationLearner extends BinaryClassificationBaseLearner {
    // Confidence parameter
    protected double phi;

    @Override
    public Options getOptions() {
        Options opts = super.getOptions();

        opts.addOption("phi", "confidence", true, "Confidence parameter [default 1.0]");
        opts.addOption("eta", "hyper_c", true, "Confidence hyperparameter eta in range (0.5, 1] [default 0.85]");

        return opts;
    }

    @Override
    public CommandLine processOptions(@Nonnull final CommandLine cl) {
        super.processOptions(cl);

        phi = 1.d;
        if (cl.hasOption("phi")) {
            phi = StringParser.parseDouble(cl.getOptionValue("phi"), 1.d);
        }

        if (cl.hasOption("eta")) {
            double eta = StringParser.parseDouble(cl.getOptionValue("eta"), 1);
            if (eta <= 0.5 || eta > 1) {
                throw new IllegalArgumentException("Confidence hyperparameter eta must be in range (0.5, 1]: ");
            }
            phi = MathUtils.probit(eta, 5d);
        }

        return cl;
    }

    @Override
    public final void train(@Nonnull final ArrayList<FeatureValue> features, double label) {
        // Here we don't use optimizer to update weights.
        final int y = label > 0 ? 1 : -1;

        PredictionResult margin = calcScoreAndVariance(features);
        double gamma = getGamma(margin, y);

        if (gamma > 0.d) {// alpha = max(0, gamma)
            double coeff = gamma * y;
            update(features, coeff, gamma);
        }
    }

    @Override
    protected void update(@Nonnull final ArrayList<FeatureValue> features, final double coeff, final double alpha) {
        for (FeatureValue f : features) {
            if (f == null) {
                continue;
            }
            final Object k = f.getFeature();
            final double v = f.getValueAsDouble();

            TrainWeights.WeightWithCovar oldW = weights.get(k);
            TrainWeights.WeightWithCovar newW = getNewWeight(oldW, v, coeff, alpha, phi);
            weights.set(k, newW);
        }
    }

    private double getGamma(PredictionResult margin, int y) {
        double score = margin.getScore() * y;
        double var = margin.getVariance();

        double b = 1.d + 2.d * phi * score;
        double gamma_numer = -b + Math.sqrt(b * b - 8.d * phi * (score - phi * var));
        double gamma_denom = 4.d * phi * var;
        if (gamma_denom == 0.d) {// avoid divide-by-zero
            return 0.d;
        }
        return gamma_numer / gamma_denom;
    }

    private static TrainWeights.WeightWithCovar getNewWeight(final TrainWeights.WeightWithCovar old, final double x, final double coeff, final double alpha, final double phi) {
        final double old_w, old_cov;
        if (old == null) {
            old_w = 0.d;
            old_cov = 1.d;
        } else {
            old_w = old.get();
            old_cov = old.getCovar();
        }

        double new_w = old_w + (coeff * old_cov * x);
        double new_cov = 1.d / (1.d / old_cov + (2.d * alpha * phi * x * x));
        return (TrainWeights.WeightWithCovar) TrainWeights.WeightWithCovar.newBuilder().buildFromWeightAndParams(new_w, new_cov);
    }
}
