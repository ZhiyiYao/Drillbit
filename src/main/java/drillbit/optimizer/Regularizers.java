package drillbit.optimizer;

import javax.annotation.Nonnull;
import java.util.Map;

public abstract class Regularizers {
    private static final double DEFAULT_LAMBDA = 0.0001d;

    protected final double lambda;

    public Regularizers(@Nonnull Map<String, String> options) {
        this.lambda = parsedouble(options.get("lambda"), DEFAULT_LAMBDA);
    }

    @Nonnull
    public static Regularizers get(@Nonnull final Map<String, String> options)
            throws IllegalArgumentException {
        final String regName = options.get("regularization");
        if (regName == null) {
            return new PassThrough(options);
        }

        if ("no".equalsIgnoreCase(regName)) {
            return new PassThrough(options);
        } else if ("l1".equalsIgnoreCase(regName)) {
            return new L1(options);
        } else if ("l2".equalsIgnoreCase(regName)) {
            return new L2(options);
        } else if ("elasticnet".equalsIgnoreCase(regName)) {
            return new ElasticNet(options);
        } else if ("rda".equalsIgnoreCase(regName)) {
            // Return `PassThrough` because we need special handling for RDA.
            // See an implementation of `Optimizer#RDA`.
            return new PassThrough(options);
        } else {
            throw new IllegalArgumentException("Unsupported regularization name: " + regName);
        }
    }

    private static double parsedouble(final String s, final double defaultValue) {
        if (s == null) {
            return defaultValue;
        }
        return Double.parseDouble(s);
    }

    public void getHyperParameters(@Nonnull Map<String, Object> hyperParams) {
        hyperParams.put("lambda", lambda);
    }

    public double regularize(final double weight, final double gradient) {
        return gradient + lambda * getRegularizer(weight);
    }

    abstract double getRegularizer(double weight);

    public static final class PassThrough extends Regularizers {

        public PassThrough(final Map<String, String> options) {
            super(options);
        }

        @Override
        public double getRegularizer(double weight) {
            return 0.f;
        }

        @Override
        public double regularize(final double weight, final double gradient) {
            return gradient;
        }

        @Override
        public void getHyperParameters(@Nonnull Map<String, Object> hyperParams) {
            super.getHyperParameters(hyperParams);
            hyperParams.put("regularization", "no");
        }
    }

    public static final class L1 extends Regularizers {

        public L1(Map<String, String> options) {
            super(options);
        }

        @Override
        public double getRegularizer(final double weight) {
            return weight > 0.f ? 1.f : -1.f;
        }

        @Override
        public void getHyperParameters(@Nonnull Map<String, Object> hyperParams) {
            super.getHyperParameters(hyperParams);
            hyperParams.put("regularization", "L1");
        }
    }

    public static final class L2 extends Regularizers {

        public L2(final Map<String, String> options) {
            super(options);
        }

        @Override
        public double getRegularizer(double weight) {
            return weight;
        }

        @Override
        public void getHyperParameters(@Nonnull Map<String, Object> hyperParams) {
            super.getHyperParameters(hyperParams);
            hyperParams.put("regularization", "L2");
        }
    }

    public static final class ElasticNet extends Regularizers {
        private static final double DEFAULT_L1_RATIO = 0.5f;

        @Nonnull
        private final L1 l1;
        @Nonnull
        private final L2 l2;

        private final double l1Ratio;

        public ElasticNet(@Nonnull Map<String, String> options) {
            super(options);

            this.l1 = new L1(options);
            this.l2 = new L2(options);

            this.l1Ratio = parsedouble(options.get("l1_ratio"), DEFAULT_L1_RATIO);
            if (l1Ratio < 0.f || l1Ratio > 1.f) {
                throw new IllegalArgumentException(
                        "L1 ratio should be in [0.0, 1.0], but got " + l1Ratio);
            }
        }

        @Override
        public double getRegularizer(final double weight) {
            return l1Ratio * l1.getRegularizer(weight)
                    + (1.f - l1Ratio) * l2.getRegularizer(weight);
        }

        @Override
        public void getHyperParameters(@Nonnull Map<String, Object> hyperParams) {
            super.getHyperParameters(hyperParams);
            hyperParams.put("regularization", "ElasticNet");
            hyperParams.put("l1_ratio", l1Ratio);
        }
    }

}
