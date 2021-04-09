package drillbit.optimizer;

import drillbit.parameter.Weights;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;
import java.util.concurrent.ConcurrentHashMap;


public final class SparseOptimizerFactory {
    private static final Log LOG = LogFactory.getLog(SparseOptimizerFactory.class);

    @Nonnull
    public static Optimizers.OptimizerBase create(@Nonnull final int ndims,
                                   @Nonnull final ConcurrentHashMap<String, String> options) {
        final String optimizerName = options.get("optimizer");
        if (optimizerName == null) {
            throw new IllegalArgumentException("`optimizer` not defined");
        }
        final String name = optimizerName.toLowerCase();

        if ("rda".equalsIgnoreCase(options.get("regularization"))
                && !"adagrad".equals(name)) {
            throw new IllegalArgumentException(
                    "`-regularization rda` is only supported for AdaGrad but `-optimizer "
                            + optimizerName + "`. Please specify `-regularization l1` and so on.");
        }

        final Optimizers.OptimizerBase optimizerImpl;
        if ("sgd".equals(name)) {
            optimizerImpl = new Optimizers.SGD(options);
        } else if ("momentum".equals(name)) {
            optimizerImpl = new Momentum(ndims, options);
        } else if ("nesterov".equals(name)) {
            options.put("nesterov", "");
            optimizerImpl = new Momentum(ndims, options);
        } else if ("adagrad".equals(name)) {
            // If a regularization type is "RDA", wrap the optimizer with `Optimizer#RDA`.
            if ("rda".equalsIgnoreCase(options.get("regularization"))) {
                AdaGrad adagrad = new AdaGrad(ndims, options);
                optimizerImpl = new AdagradRDA(ndims, adagrad, options);
            } else {
                optimizerImpl = new AdaGrad(ndims, options);
            }
        } else if ("rmsprop".equals(name)) {
            optimizerImpl = new RMSprop(ndims, options);
        } else if ("rmspropgraves".equals(name) || "rmsprop_graves".equals(name)) {
            optimizerImpl = new RMSpropGraves(ndims, options);
        } else if ("adadelta".equals(name)) {
            optimizerImpl = new AdaDelta(ndims, options);
        } else if ("adam".equals(name)) {
            optimizerImpl = new Adam(ndims, options);
        } else if ("nadam".equals(name)) {
            optimizerImpl = new Nadam(ndims, options);
        } else if ("eve".equals(name)) {
            optimizerImpl = new Eve(ndims, options);
        } else if ("adam_hd".equals(name) || "adamhd".equals(name)) {
            optimizerImpl = new AdamHD(ndims, options);
        } else {
            throw new IllegalArgumentException("Unsupported optimizer name: " + optimizerName);
        }

        if (LOG.isInfoEnabled()) {
            LOG.info(
                    "Configured " + optimizerImpl.getOptimizerName() + " as the optimizer: " + options);
            LOG.info("ETA estimator: " + optimizerImpl._eta);
        }

        return optimizerImpl;
    }

    @NotThreadSafe
    static final class Momentum extends Optimizers.Momentum {

        @Nonnull
        private final ConcurrentHashMap<Object, Weights.WeightWithDelta> auxWeights;

        public Momentum(@Nonnegative int size, @Nonnull ConcurrentHashMap<String, String> options) {
            super(options);
            this.auxWeights = new ConcurrentHashMap<>(size);
        }

        @Override
        protected double update(@Nonnull final Object feature, final double weight,
                               final double gradient) {
            Weights.WeightWithDelta auxWeight = auxWeights.get(feature);
            if (auxWeight == null) {
                auxWeight = (Weights.WeightWithDelta) newWeightValue(weight);
                auxWeights.put(feature, auxWeight);
            } else {
                auxWeight.set(weight);
            }
            return update(auxWeight, gradient);
        }

    }

    @NotThreadSafe
    static final class AdaGrad extends Optimizers.AdaGrad {

        @Nonnull
        private final ConcurrentHashMap<Object, Weights.WeightWithSumOfSquaredGradients> auxWeights;

        public AdaGrad(@Nonnegative int size, @Nonnull ConcurrentHashMap<String, String> options) {
            super(options);
            this.auxWeights = new ConcurrentHashMap<>(size);
        }

        @Override
        protected double update(@Nonnull final Object feature, final double weight,
                               final double gradient) {
            Weights.WeightWithSumOfSquaredGradients auxWeight = auxWeights.get(feature);
            if (auxWeight == null) {
                auxWeight = (Weights.WeightWithSumOfSquaredGradients) newWeightValue(weight);
                auxWeights.put(feature, auxWeight);
            } else {
                auxWeight.set(weight);
            }
            return update(auxWeight, gradient);
        }

    }

    @NotThreadSafe
    static final class RMSprop extends Optimizers.RMSprop {

        @Nonnull
        private final ConcurrentHashMap<Object, Weights.WeightWithSumOfSquaredGradients> auxWeights;

        public RMSprop(@Nonnegative int size, @Nonnull ConcurrentHashMap<String, String> options) {
            super(options);
            this.auxWeights = new ConcurrentHashMap<>(size);
        }

        @Override
        protected double update(@Nonnull final Object feature, final double weight,
                               final double gradient) {
            Weights.WeightWithSumOfSquaredGradients auxWeight = auxWeights.get(feature);
            if (auxWeight == null) {
                auxWeight = (Weights.WeightWithSumOfSquaredGradients) newWeightValue(weight);
                auxWeights.put(feature, auxWeight);
            } else {
                auxWeight.set(weight);
            }
            return update(auxWeight, gradient);
        }

    }

    @NotThreadSafe
    static final class RMSpropGraves extends Optimizers.RMSpropGraves {

        @Nonnull
        private final ConcurrentHashMap<Object, Weights.WeightWithSumOfGradientsAndSumOfSquaredGradientsAndDelta> auxWeights;

        public RMSpropGraves(@Nonnegative int size, @Nonnull ConcurrentHashMap<String, String> options) {
            super(options);
            this.auxWeights = new ConcurrentHashMap<>(size);
        }

        @Override
        protected double update(@Nonnull final Object feature, final double weight,
                               final double gradient) {
            Weights.WeightWithSumOfGradientsAndSumOfSquaredGradientsAndDelta auxWeight = auxWeights.get(feature);
            if (auxWeight == null) {
                auxWeight = (Weights.WeightWithSumOfGradientsAndSumOfSquaredGradientsAndDelta) newWeightValue(weight);
                auxWeights.put(feature, auxWeight);
            } else {
                auxWeight.set(weight);
            }
            return update(auxWeight, gradient);
        }

    }

    @NotThreadSafe
    static final class AdaDelta extends Optimizers.AdaDelta {

        @Nonnull
        private final ConcurrentHashMap<Object, Weights.WeightWithSumOfSquaredGradientsAndSumOfSquaredDeltaX> auxWeights;

        public AdaDelta(@Nonnegative int size, @Nonnull ConcurrentHashMap<String, String> options) {
            super(options);
            this.auxWeights = new ConcurrentHashMap<>(size);
        }

        @Override
        protected double update(@Nonnull final Object feature, final double weight,
                               final double gradient) {
            Weights.WeightWithSumOfSquaredGradientsAndSumOfSquaredDeltaX auxWeight = auxWeights.get(feature);
            if (auxWeight == null) {
                auxWeight = (Weights.WeightWithSumOfSquaredGradientsAndSumOfSquaredDeltaX) newWeightValue(weight);
                auxWeights.put(feature, auxWeight);
            } else {
                auxWeight.set(weight);
            }
            return update(auxWeight, gradient);
        }

    }

    @NotThreadSafe
    static final class Adam extends Optimizers.Adam {

        @Nonnull
        private final ConcurrentHashMap<Object, Weights.WeightWithMAndV> auxWeights;

        public Adam(@Nonnegative int size, @Nonnull ConcurrentHashMap<String, String> options) {
            super(options);
            this.auxWeights = new ConcurrentHashMap<>(size);
        }

        @Override
        protected double update(@Nonnull final Object feature, final double weight,
                               final double gradient) {
            Weights.WeightWithMAndV auxWeight = auxWeights.get(feature);
            if (auxWeight == null) {
                auxWeight = (Weights.WeightWithMAndV) newWeightValue(weight);
                auxWeights.put(feature, auxWeight);
            } else {
                auxWeight.set(weight);
            }
            return update(auxWeight, gradient);
        }

    }

    @NotThreadSafe
    static final class Nadam extends Optimizers.Nadam {

        @Nonnull
        private final ConcurrentHashMap<Object, Weights.WeightWithMAndV> auxWeights;

        public Nadam(@Nonnegative int size, @Nonnull ConcurrentHashMap<String, String> options) {
            super(options);
            this.auxWeights = new ConcurrentHashMap<>(size);
        }

        @Override
        protected double update(@Nonnull final Object feature, final double weight,
                               final double gradient) {
            Weights.WeightWithMAndV auxWeight = auxWeights.get(feature);
            if (auxWeight == null) {
                auxWeight = (Weights.WeightWithMAndV) newWeightValue(weight);
                auxWeights.put(feature, auxWeight);
            } else {
                auxWeight.set(weight);
            }
            return update(auxWeight, gradient);
        }

    }

    @NotThreadSafe
    static final class Eve extends Optimizers.Eve {

        @Nonnull
        private final ConcurrentHashMap<Object, Weights.SingleWeight> auxWeights;

        public Eve(@Nonnegative int size, @Nonnull ConcurrentHashMap<String, String> options) {
            super(options);
            this.auxWeights = new ConcurrentHashMap<>(size);
        }

        @Override
        protected double update(@Nonnull final Object feature, final double weight,
                               final double gradient) {
            Weights.SingleWeight auxWeight = auxWeights.get(feature);
            if (auxWeight == null) {
                auxWeight = ( Weights.SingleWeight) newWeightValue(weight);
                auxWeights.put(feature, auxWeight);
            } else {
                auxWeight.set(weight);
            }
            return update(auxWeight, gradient);
        }

    }

    @NotThreadSafe
    static final class AdamHD extends Optimizers.AdamHD {

        @Nonnull
        private final ConcurrentHashMap<Object, Weights.WeightWithMAndV> auxWeights;

        public AdamHD(@Nonnegative int size, @Nonnull ConcurrentHashMap<String, String> options) {
            super(options);
            this.auxWeights = new ConcurrentHashMap<>(size);
        }

        @Override
        protected double update(@Nonnull final Object feature, final double weight,
                               final double gradient) {
            Weights.WeightWithMAndV auxWeight = auxWeights.get(feature);
            if (auxWeight == null) {
                auxWeight = (Weights.WeightWithMAndV) newWeightValue(weight);
                auxWeights.put(feature, auxWeight);
            } else {
                auxWeight.set(weight);
            }
            return update(auxWeight, gradient);
        }

    }

    @NotThreadSafe
    static final class AdagradRDA extends Optimizers.AdagradRDA {

        @Nonnull
        private final ConcurrentHashMap<Object, Weights.WeightWithSumOfSquaredGradientsAndSumOfGradients> auxWeights;

        public AdagradRDA(@Nonnegative int size, @Nonnull Optimizers.AdaGrad optimizerImpl,
                          @Nonnull ConcurrentHashMap<String, String> options) {
            super(optimizerImpl, options);
            this.auxWeights = new ConcurrentHashMap<>(size);
        }

        @Override
        protected double update(@Nonnull final Object feature, final double weight,
                               final double gradient) {
            Weights.WeightWithSumOfSquaredGradientsAndSumOfGradients auxWeight = auxWeights.get(feature);
            if (auxWeight == null) {
                auxWeight = (Weights.WeightWithSumOfSquaredGradientsAndSumOfGradients) newWeightValue(weight);
                auxWeights.put(feature, auxWeight);
            } else {
                auxWeight.set(weight);
            }
            final double newWeight = update(auxWeight, gradient);
            if (newWeight == 0.f) {
                auxWeights.remove(feature);
            }
            return newWeight;
        }

    }

}
