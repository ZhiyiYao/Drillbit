package drillbit.optimizer;

import drillbit.TrainWeights;
import drillbit.utils.math.MathUtils;
import drillbit.utils.parser.ObjectParser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

public final class DenseOptimizerFactory {
    private static final Log logger = LogFactory.getLog(DenseOptimizerFactory.class);

    @Nonnull
    public static Optimizers.OptimizerBase create(@Nonnegative final int ndims,
                                                  @Nonnull final ConcurrentHashMap<String, String> options) {
        final String optimizerName = options.get("optimizer");
        if (optimizerName == null) {
            throw new IllegalArgumentException("`optimizer` not defined");
        }
        final String name = optimizerName.toLowerCase();

        if ("rda".equalsIgnoreCase(options.get("regularization"))
                && "adagrad".equals(name) == false) {
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

        if (logger.isInfoEnabled()) {
            logger.info(
                    "Configured " + optimizerImpl.getOptimizerName() + " as the optimizer: " + options);
            logger.info("ETA estimator: " + optimizerImpl._eta);
        }

        return optimizerImpl;
    }

    @NotThreadSafe
    static final class Momentum extends Optimizers.Momentum {

        @Nonnull
        private final TrainWeights.WeightWithDelta weightValueReused;
        @Nonnull
        private double[] delta;

        public Momentum(int ndims, ConcurrentHashMap<String, String> options) {
            super(options);
            this.weightValueReused = (TrainWeights.WeightWithDelta) newWeightValue(0.d);
            this.delta = new double[ndims];
        }

        @Override
        protected double update(@Nonnull final Object feature, final double weight,
                               final double gradient) {
            int i = ObjectParser.parseInt(feature);
            ensureCapacity(i);
            weightValueReused.set(weight);
            weightValueReused.setDelta(delta[i]);
            update(weightValueReused, gradient);
            delta[i] = weightValueReused.getDelta();
            return weightValueReused.get();
        }

        private void ensureCapacity(final int index) {
            if (index >= delta.length) {
                int bits = MathUtils.bitsRequired(index);
                int newSize = (1 << bits) + 1;
                this.delta = Arrays.copyOf(delta, newSize);
            }
        }

    }

    @NotThreadSafe
    static final class AdaGrad extends Optimizers.AdaGrad {

        @Nonnull
        private final TrainWeights.WeightWithSumOfSquaredGradients weightValueReused;
        @Nonnull
        private double[] sum_of_squared_gradients;

        public AdaGrad(int ndims, ConcurrentHashMap<String, String> options) {
            super(options);
            this.weightValueReused = (TrainWeights.WeightWithSumOfSquaredGradients) newWeightValue(0.d);
            this.sum_of_squared_gradients = new double[ndims];
        }

        @Override
        protected double update(@Nonnull final Object feature, final double weight,
                               final double gradient) {
            int i = ObjectParser.parseInt(feature);
            ensureCapacity(i);
            weightValueReused.set(weight);
            weightValueReused.setSumOfSquaredGradients(sum_of_squared_gradients[i]);
            update(weightValueReused, gradient);
            sum_of_squared_gradients[i] = weightValueReused.getSumOfSquaredGradients();
            return weightValueReused.get();
        }

        private void ensureCapacity(final int index) {
            if (index >= sum_of_squared_gradients.length) {
                int bits = MathUtils.bitsRequired(index);
                int newSize = (1 << bits) + 1;
                this.sum_of_squared_gradients = Arrays.copyOf(sum_of_squared_gradients, newSize);
            }
        }

    }

    @NotThreadSafe
    static final class RMSprop extends Optimizers.RMSprop {

        @Nonnull
        private final TrainWeights.WeightWithSumOfSquaredGradients weightValueReused;
        @Nonnull
        private double[] sum_of_squared_gradients;

        public RMSprop(int ndims, ConcurrentHashMap<String, String> options) {
            super(options);
            this.weightValueReused = (TrainWeights.WeightWithSumOfSquaredGradients) newWeightValue(0.d);
            this.sum_of_squared_gradients = new double[ndims];
        }

        @Override
        protected double update(@Nonnull final Object feature, final double weight,
                               final double gradient) {
            int i = ObjectParser.parseInt(feature);
            ensureCapacity(i);
            weightValueReused.set(weight);
            weightValueReused.setSumOfSquaredGradients(sum_of_squared_gradients[i]);
            update(weightValueReused, gradient);
            sum_of_squared_gradients[i] = weightValueReused.getSumOfSquaredGradients();
            return weightValueReused.get();
        }

        private void ensureCapacity(final int index) {
            if (index >= sum_of_squared_gradients.length) {
                int bits = MathUtils.bitsRequired(index);
                int newSize = (1 << bits) + 1;
                this.sum_of_squared_gradients = Arrays.copyOf(sum_of_squared_gradients, newSize);
            }
        }

    }

    @NotThreadSafe
    static final class RMSpropGraves extends Optimizers.RMSpropGraves {

        @Nonnull
        private final TrainWeights.WeightWithSumOfGradientsAndSumOfSquaredGradientsAndDelta weightValueReused;
        @Nonnull
        private double[] sum_of_gradients;
        @Nonnull
        private double[] sum_of_squared_gradients;
        @Nonnull
        private double[] delta;

        public RMSpropGraves(int ndims, ConcurrentHashMap<String, String> options) {
            super(options);
            this.weightValueReused = (TrainWeights.WeightWithSumOfGradientsAndSumOfSquaredGradientsAndDelta) newWeightValue(0.d);
            this.sum_of_gradients = new double[ndims];
            this.sum_of_squared_gradients = new double[ndims];
            this.delta = new double[ndims];
        }

        @Override
        protected double update(@Nonnull final Object feature, final double weight,
                               final double gradient) {
            int i = ObjectParser.parseInt(feature);
            ensureCapacity(i);
            weightValueReused.set(weight);
            weightValueReused.setSumOfGradients(sum_of_gradients[i]);
            weightValueReused.setSumOfSquaredGradients(sum_of_squared_gradients[i]);
            weightValueReused.setDelta(delta[i]);
            update(weightValueReused, gradient);
            sum_of_gradients[i] = weightValueReused.getSumOfGradients();
            sum_of_squared_gradients[i] = weightValueReused.getSumOfSquaredGradients();
            delta[i] = weightValueReused.getDelta();
            return weightValueReused.get();
        }

        private void ensureCapacity(final int index) {
            if (index >= sum_of_gradients.length) {
                int bits = MathUtils.bitsRequired(index);
                int newSize = (1 << bits) + 1;
                this.sum_of_gradients = Arrays.copyOf(sum_of_gradients, newSize);
                this.sum_of_squared_gradients = Arrays.copyOf(sum_of_squared_gradients, newSize);
                this.delta = Arrays.copyOf(delta, newSize);
            }
        }

    }

    @NotThreadSafe
    static final class AdaDelta extends Optimizers.AdaDelta {

        @Nonnull
        private final TrainWeights.WeightWithSumOfSquaredGradientsAndSumOfSquaredDeltaX weightValueReused;

        @Nonnull
        private double[] sum_of_squared_gradients;
        @Nonnull
        private double[] sum_of_squared_delta_x;

        public AdaDelta(int ndims, ConcurrentHashMap<String, String> options) {
            super(options);
            this.weightValueReused = (TrainWeights.WeightWithSumOfSquaredGradientsAndSumOfSquaredDeltaX) newWeightValue(0.d);
            this.sum_of_squared_gradients = new double[ndims];
            this.sum_of_squared_delta_x = new double[ndims];
        }

        @Override
        protected double update(@Nonnull final Object feature, final double weight,
                               final double gradient) {
            int i = ObjectParser.parseInt(feature);
            ensureCapacity(i);
            weightValueReused.set(weight);
            weightValueReused.setSumOfSquaredGradients(sum_of_squared_gradients[i]);
            weightValueReused.setSumOfSquaredDeltaX(sum_of_squared_delta_x[i]);
            update(weightValueReused, gradient);
            sum_of_squared_gradients[i] = weightValueReused.getSumOfSquaredGradients();
            sum_of_squared_delta_x[i] = weightValueReused.getSumOfSquaredDeltaX();
            return weightValueReused.get();
        }

        private void ensureCapacity(final int index) {
            if (index >= sum_of_squared_gradients.length) {
                int bits = MathUtils.bitsRequired(index);
                int newSize = (1 << bits) + 1;
                this.sum_of_squared_gradients = Arrays.copyOf(sum_of_squared_gradients, newSize);
                this.sum_of_squared_delta_x = Arrays.copyOf(sum_of_squared_delta_x, newSize);
            }
        }

    }

    @NotThreadSafe
    static final class Adam extends Optimizers.Adam {

        @Nonnull
        private final TrainWeights.WeightWithMAndV weightValueReused;

        @Nonnull
        private double[] val_m;
        @Nonnull
        private double[] val_v;

        public Adam(int ndims, ConcurrentHashMap<String, String> options) {
            super(options);
            this.weightValueReused = (TrainWeights.WeightWithMAndV) newWeightValue(0.d);
            this.val_m = new double[ndims];
            this.val_v = new double[ndims];
        }

        @Override
        protected double update(@Nonnull final Object feature, final double weight,
                               final double gradient) {
            int i = ObjectParser.parseInt(feature);
            ensureCapacity(i);
            weightValueReused.set(weight);
            weightValueReused.setM(val_m[i]);
            weightValueReused.setV(val_v[i]);
            update(weightValueReused, gradient);
            val_m[i] = weightValueReused.getM();
            val_v[i] = weightValueReused.getV();
            return weightValueReused.get();
        }

        private void ensureCapacity(final int index) {
            if (index >= val_m.length) {
                int bits = MathUtils.bitsRequired(index);
                int newSize = (1 << bits) + 1;
                this.val_m = Arrays.copyOf(val_m, newSize);
                this.val_v = Arrays.copyOf(val_v, newSize);
            }
        }

    }

    @NotThreadSafe
    static final class Nadam extends Optimizers.Nadam {

        @Nonnull
        private final TrainWeights.WeightWithMAndV weightValueReused;

        @Nonnull
        private double[] val_m;
        @Nonnull
        private double[] val_v;

        public Nadam(int ndims, ConcurrentHashMap<String, String> options) {
            super(options);
            this.weightValueReused = (TrainWeights.WeightWithMAndV) newWeightValue(0.f);
            this.val_m = new double[ndims];
            this.val_v = new double[ndims];
        }

        @Override
        protected double update(@Nonnull final Object feature, final double weight,
                               final double gradient) {
            int i = ObjectParser.parseInt(feature);
            ensureCapacity(i);
            weightValueReused.set(weight);
            weightValueReused.setM(val_m[i]);
            weightValueReused.setV(val_v[i]);
            update(weightValueReused, gradient);
            val_m[i] = weightValueReused.getM();
            val_v[i] = weightValueReused.getV();
            return weightValueReused.get();
        }

        private void ensureCapacity(final int index) {
            if (index >= val_m.length) {
                int bits = MathUtils.bitsRequired(index);
                int newSize = (1 << bits) + 1;
                this.val_m = Arrays.copyOf(val_m, newSize);
                this.val_v = Arrays.copyOf(val_v, newSize);
            }
        }

    }

    @NotThreadSafe
    static final class Eve extends Optimizers.Eve {

        @Nonnull
        private final TrainWeights.WeightWithMAndV weightValueReused;

        @Nonnull
        private double[] val_m;
        @Nonnull
        private double[] val_v;

        public Eve(int ndims, ConcurrentHashMap<String, String> options) {
            super(options);
            this.weightValueReused = (TrainWeights.WeightWithMAndV) newWeightValue(0.f);
            this.val_m = new double[ndims];
            this.val_v = new double[ndims];
        }

        @Override
        protected double update(@Nonnull final Object feature, final double weight,
                               final double gradient) {
            int i = ObjectParser.parseInt(feature);
            ensureCapacity(i);
            weightValueReused.set(weight);
            weightValueReused.setM(val_m[i]);
            weightValueReused.setV(val_v[i]);
            update(weightValueReused, gradient);
            val_m[i] = weightValueReused.getM();
            val_v[i] = weightValueReused.getV();
            return weightValueReused.get();
        }

        private void ensureCapacity(final int index) {
            if (index >= val_m.length) {
                int bits = MathUtils.bitsRequired(index);
                int newSize = (1 << bits) + 1;
                this.val_m = Arrays.copyOf(val_m, newSize);
                this.val_v = Arrays.copyOf(val_v, newSize);
            }
        }

    }


    @NotThreadSafe
    static final class AdamHD extends Optimizers.AdamHD {

        @Nonnull
        private final TrainWeights.WeightWithMAndV weightValueReused;

        @Nonnull
        private double[] val_m;
        @Nonnull
        private double[] val_v;

        public AdamHD(int ndims, ConcurrentHashMap<String, String> options) {
            super(options);
            this.weightValueReused = (TrainWeights.WeightWithMAndV) newWeightValue(0.d);
            this.val_m = new double[ndims];
            this.val_v = new double[ndims];
        }

        @Override
        protected double update(@Nonnull final Object feature, final double weight,
                               final double gradient) {
            int i = ObjectParser.parseInt(feature);
            ensureCapacity(i);
            weightValueReused.set(weight);
            weightValueReused.setM(val_m[i]);
            weightValueReused.setV(val_v[i]);
            update(weightValueReused, gradient);
            val_m[i] = weightValueReused.getM();
            val_v[i] = weightValueReused.getV();
            return weightValueReused.get();
        }

        private void ensureCapacity(final int index) {
            if (index >= val_m.length) {
                int bits = MathUtils.bitsRequired(index);
                int newSize = (1 << bits) + 1;
                this.val_m = Arrays.copyOf(val_m, newSize);
                this.val_v = Arrays.copyOf(val_v, newSize);
            }
        }
    }

    @NotThreadSafe
    static final class AdagradRDA extends Optimizers.AdagradRDA {

        @Nonnull
        private final TrainWeights.WeightWithSumOfSquaredGradientsAndSumOfGradients weightValueReused;

        @Nonnull
        private double[] sum_of_gradients;

        public AdagradRDA(int ndims, @Nonnull Optimizers.AdaGrad optimizerImpl,
                          @Nonnull ConcurrentHashMap<String, String> options) {
            super(optimizerImpl, options);
            this.weightValueReused = (TrainWeights.WeightWithSumOfSquaredGradientsAndSumOfGradients) newWeightValue(0.f);
            this.sum_of_gradients = new double[ndims];
        }

        @Override
        protected double update(@Nonnull final Object feature, final double weight,
                               final double gradient) {
            int i = ObjectParser.parseInt(feature);
            ensureCapacity(i);
            weightValueReused.set(weight);
            weightValueReused.setSumOfGradients(sum_of_gradients[i]);
            update(weightValueReused, gradient);
            sum_of_gradients[i] = weightValueReused.getSumOfGradients();
            return weightValueReused.get();
        }

        private void ensureCapacity(final int index) {
            if (index >= sum_of_gradients.length) {
                int bits = MathUtils.bitsRequired(index);
                int newSize = (1 << bits) + 1;
                this.sum_of_gradients = Arrays.copyOf(sum_of_gradients, newSize);
            }
        }

    }

}
