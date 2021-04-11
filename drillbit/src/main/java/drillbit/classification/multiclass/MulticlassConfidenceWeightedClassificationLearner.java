package drillbit.classification.multiclass;

public class MulticlassConfidenceWeightedClassificationLearner {
}

//public final class MulticlassConfidenceWeightedClassificationLearner extends MulticlassClassificationLearner {
//    @Override
//    public void train(@NotNull ArrayList<FeatureValue> features, @NotNull String actual_label) {
//        Margin margin = getMarginAndVariance(features, actual_label, true);
//        double gamma = getGamma(margin);
//
//        if (gamma > 0.f) {// alpha = max(0, gamma)
//            Object missed_label = margin.getMaxIncorrectLabel();
//            update(features, gamma, actual_label, missed_label);
//        }
//    }
//
//    private void update(ArrayList<FeatureValue> features, double gamma, String actual_label, Object missed_label) {
//    }
//
//    @Override
//    public void add(@NotNull String feature, @NotNull String target) {
//
//    }
//
//    protected final double getGamma(Margin margin) {
//        double m = margin.get();
//        double var = margin.getVariance();
//        assert (var != 0);
//
//        double b = 1.f + 2.f * phi * m;
//        double gamma_numer = -b + (double) Math.sqrt(b * b - 8.f * phi * (m - phi * var));
//        double gamma_denom = 4.f * phi * var;
//        if (gamma_denom == 0.f) {// avoid divide-by-zero
//            return 0.f;
//        }
//        return gamma_numer / gamma_denom;
//    }
//
//    protected Margin getMarginAndVariance(@Nonnull final ArrayList<FeatureValue> features,
//                                          final Object actual_label, boolean nonZeroVariance) {
//        double correctScore = 0.f;
//        double correctVariance = 0.f;
//        Object maxAnotherLabel = null;
//        double maxAnotherScore = 0.f;
//        double maxAnotherVariance = 0.f;
//
//        if (nonZeroVariance && label2model.isEmpty()) {// for initial call
//            double var = 2.d * calcVariance(features);
//            return new Margin(correctScore, maxAnotherLabel, maxAnotherScore).variance(var);
//        }
//
//        for (Map.Entry<String, Model> label2map : label2model.entrySet()) {// for each class
//            Object label = label2map.getKey();
//            Model model = label2map.getValue();
//            PredictionResult predicted = calcScoreAndVariance(model, features);
//            double score = predicted.getScore();
//
//            if (label.equals(actual_label)) {
//                correctScore = score;
//                correctVariance = predicted.getVariance();
//            } else {
//                if (maxAnotherLabel == null || score > maxAnotherScore) {
//                    maxAnotherLabel = label;
//                    maxAnotherScore = score;
//                    maxAnotherVariance = predicted.getVariance();
//                }
//            }
//        }
//
//        double var = correctVariance + maxAnotherVariance;
//        return new Margin(correctScore, maxAnotherLabel, maxAnotherScore).variance(var);
//    }
//
//}
