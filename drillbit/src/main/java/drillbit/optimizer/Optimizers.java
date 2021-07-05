package drillbit.optimizer;

import drillbit.TrainWeights;
import drillbit.utils.math.MathUtils;
import drillbit.utils.parser.StringParser;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import java.util.concurrent.ConcurrentHashMap;

import static java.lang.Math.*;
import static java.lang.Math.pow;

public abstract class Optimizers {
    public static abstract class OptimizerBase {
        @Nonnull
        protected final EtaEstimator _eta;
        @Nonnull
        protected final Regularizers _reg;
        @Nonnegative
        protected long _numStep = 0L;

        public OptimizerBase(@Nonnull ConcurrentHashMap<String, String> options) {
            this._eta = getEtaEstimator(options);
            this._reg = Regularizers.get(options);
        }

        public abstract TrainWeights.WeightType getWeightType();

        @Nonnull
        protected TrainWeights.ExtendedWeight newWeightValue(final double weight) {
            return TrainWeights.getWeightBuilder(getWeightType()).buildFromWeight(weight);
        }

        @Nonnull
        protected EtaEstimator getEtaEstimator(@Nonnull ConcurrentHashMap<String, String> options) {
            return EtaEstimator.get(options);
        }

        public void proceedStep() {
            _numStep++;
        }

        public double update(@Nonnull Object feature, double weight, double loss, double gradient) {
            return update(feature, weight, gradient);
        }

        /**
         * Update the weights of models
         */
        protected abstract double update(@Nonnull Object feature, double weight, double gradient);

        /**
         * Update the given weight by the given gradient.
         *
         * @return new weight to be set
         */
        protected double update(@Nonnull final TrainWeights.ExtendedWeight weight, double gradient) {
            double oldWeight = weight.get();
            double delta = computeDelta(weight, gradient);
            double eta = eta(_numStep);
            double reg = _reg.regularize(oldWeight, delta);
            double newWeight = oldWeight - eta * reg;
//            double newWeight = oldWeight + eta * reg;
            weight.set(newWeight);
            return newWeight;
        }

        @Nonnull
        public abstract String getOptimizerName();

        public ConcurrentHashMap<String, Object> getHyperParameters() {
            ConcurrentHashMap<String, Object> params = new ConcurrentHashMap<>();
            params.put("optimizer", getOptimizerName());
            _eta.getHyperParameters(params);
            _reg.getHyperParameters(params);
            return params;
        }

        protected double eta(final long t) {
            return _eta.eta(_numStep);
        }

        protected double computeDelta(@Nonnull final TrainWeights.ExtendedWeight weight, final double gradient) {
            return gradient;
        }
    }

    public static abstract class Adam extends OptimizerBase {

        protected double alpha;
        protected final double beta1, beta2;
        protected final double eps;
        protected final double decay;

        // amsgrad
        protected final boolean amsgrad;
        protected double max_vhat = Double.MIN_VALUE;

        public Adam(@Nonnull ConcurrentHashMap<String, String> options) {
            super(options);
            this.alpha = StringParser.parseDouble(options.get("alpha"), 1.0d);
            this.beta1 = StringParser.parseDouble(options.get("beta1"), 0.9d);
            this.beta2 = StringParser.parseDouble(options.get("beta2"), 0.999d);
            this.eps = StringParser.parseDouble(options.get("eps"), 1e-8d);
            this.decay = StringParser.parseDouble(options.get("decay"), 0.d);
            this.amsgrad = options.containsKey("amsgrad");
        }


        @Override
        public TrainWeights.WeightType getWeightType() {
            return TrainWeights.WeightType.WithMAndV;
        }

        @Override
        protected double eta(final long t) {
            double fix1 = 1.d - Math.pow(beta1, t);
            double fix2 = 1.d - Math.pow(beta2, t);
            double eta = _eta.eta(t);
            double fix = Math.sqrt(fix2) / fix1;
            return eta * fix;
        }

        protected double alpha() {
            double fix1 = 1.d - Math.pow(beta1, _numStep);
            double fix2 = 1.d - Math.pow(beta2, _numStep);
            double fix = Math.sqrt(fix2) / fix1;
            return alpha * fix;
        }

        @Override
        protected double computeDelta(@Nonnull final TrainWeights.ExtendedWeight weight, double gradient) {
            assert weight instanceof TrainWeights.WeightWithMAndV;
            if (decay != 0.f) {// L2 regularization for weight decay
                double oldWeight = weight.get();
                gradient += decay * oldWeight;
            }
            // update biased first moment estimate
            double m = beta1 * ((TrainWeights.WeightWithMAndV) weight).getM() + (1.d - beta1) * gradient;
            // update biased second raw moment estimate
            double v = beta2 * ((TrainWeights.WeightWithMAndV) weight).getV() + (double) ((1.d - beta2) * square(gradient));
            double v_hat = v;
            if (amsgrad) {
                if (v_hat > max_vhat) {
                    this.max_vhat = v_hat;
                } else {// v_hat <= max_vhat
                    v_hat = max_vhat;
                }
            }
            // bias correlation using v_hat and m_hat
            double deltaU = m / (Math.sqrt(v_hat) + eps);
            // compute delta update
            double alpha_t = alpha();
            double delta = (double) (alpha_t * deltaU);
            // weight decay
            if (decay != 0.d) {
                double oldWeight = weight.get();
                delta += decay * oldWeight;
            }
            weight.setM(m);
            weight.setV(v);
            return delta;
        }

        @Override
        public String getOptimizerName() {
            return amsgrad ? "adam-amsgrad" : "adam";
        }

        @Override
        public ConcurrentHashMap<String, Object> getHyperParameters() {
            ConcurrentHashMap<String, Object> params = super.getHyperParameters();
            params.put("alpha", alpha);
            params.put("beta1", beta1);
            params.put("beta2", beta2);
            params.put("eps", eps);
            params.put("decay", decay);
            return params;
        }

        public double square(final double d) {
            return d * d;
        }
    }

    public static final class SGD extends OptimizerBase {
        private final TrainWeights.WeightWithCovar weightValueReused;

        public SGD(@Nonnull ConcurrentHashMap<String, String> options) {
            super(options);
            weightValueReused = (TrainWeights.WeightWithCovar) newWeightValue(0.d);
        }

        @Override
        public TrainWeights.WeightType getWeightType() {
            return TrainWeights.WeightType.WithCovar;
        }

        @Override
        protected double update(final Object feature, final double weight, final double gradient) {
            weightValueReused.set(weight);
            update(weightValueReused, gradient);
            return weightValueReused.get();
        }

        @Override
        public String getOptimizerName() {
            return "sgd";
        }
    }

    public static abstract class Momentum extends OptimizerBase {

        @Nonnull
        private final TrainWeights.WeightWithDelta weightValueReused;

        private final boolean nesterov;
        private final double alpha;
        private final double momentum;

        public Momentum(@Nonnull ConcurrentHashMap<String, String> options) {
            super(options);
            this.weightValueReused = (TrainWeights.WeightWithDelta) newWeightValue(0.f);
            this.nesterov = options.containsKey("nesterov");
            this.alpha = StringParser.parseDouble(options.get("alpha"), 1.d);
            this.momentum = StringParser.parseDouble(options.get("momentum"), 0.9d);
        }
        @Override
        public TrainWeights.WeightType getWeightType() {
            return TrainWeights.WeightType.WithDelta;
        }

        @Override
        protected double computeDelta(@Nonnull final TrainWeights.ExtendedWeight weight, final double gradient) {
            final double oldDelta = weight.getDelta();
            final double v = momentum * oldDelta + alpha * gradient;
            ((TrainWeights.WeightWithDelta) weight).setDelta(v);
            if (nesterov) {
                //return momentum * momentum * oldDelta + (1.f + momentum) * alpha * gradient;
                return momentum * momentum * v + (1.f + momentum) * alpha * gradient;
            } else {
                return v; // normal momentum
            }
        }

        @Override
        public String getOptimizerName() {
            return nesterov ? "nesterov" : "momentum";
        }

        @Override
        public ConcurrentHashMap<String, Object> getHyperParameters() {
            ConcurrentHashMap<String, Object> params = super.getHyperParameters();
            params.put("nesterov", nesterov);
            params.put("alpha", alpha);
            params.put("momentum", momentum);
            return params;
        }

    }

    public static abstract class AdaGrad extends OptimizerBase {

        private final double eps;
        private final double scale;

        public AdaGrad(@Nonnull ConcurrentHashMap<String, String> options) {
            super(options);
            this.eps = StringParser.parseDouble(options.get("eps"), 1.0d);
            this.scale = StringParser.parseDouble(options.get("scale"), 100.0d);
        }

        @Override
        public TrainWeights.WeightType getWeightType() {
            return TrainWeights.WeightType.WithSumOfSquaredGradients;
        }

        @Override
        protected double computeDelta(@Nonnull final TrainWeights.ExtendedWeight weight, final double gradient) {
            double old_scaled_gg = weight.getSumOfSquaredGradients();
            double new_scaled_gg = old_scaled_gg + gradient * (gradient / scale);
            weight.setSumOfSquaredGradients(new_scaled_gg);
            return (gradient / sqrt(eps + old_scaled_gg * scale));
        }

        @Override
        public String getOptimizerName() {
            return "adagrad";
        }

        @Override
        public ConcurrentHashMap<String, Object> getHyperParameters() {
            ConcurrentHashMap<String, Object> params = super.getHyperParameters();
            params.put("eps", eps);
            params.put("scale", scale);
            return params;
        }
    }

    public static abstract class RMSprop extends OptimizerBase {

        /** decay rate */
        private final double decay;
        /** constant for numerical stability */
        private final double eps;

        private final double scale; // to hold g*g in double range

        public RMSprop(@Nonnull ConcurrentHashMap<String, String> options) {
            super(options);
            this.decay = StringParser.parseDouble(options.get("decay"), 0.95d);
            this.eps = StringParser.parseDouble(options.get("eps"), 1.0d);
            this.scale = StringParser.parseDouble(options.get("scale"), 100.0d);
        }

        @Override
        public TrainWeights.WeightType getWeightType() {
            return TrainWeights.WeightType.WithSumOfSquaredGradients;
        }

        @Override
        protected double computeDelta(@Nonnull final TrainWeights.ExtendedWeight weight, final double gradient) {
            double old_scaled_gg = weight.getSumOfSquaredGradients();
            double new_scaled_gg = decay * old_scaled_gg + (1.d - decay) * gradient * (gradient / scale);
            weight.setSumOfSquaredGradients(new_scaled_gg);
            return (gradient / sqrt(eps + old_scaled_gg * scale));
        }

        @Override
        public String getOptimizerName() {
            return "rmsprop";
        }

        @Override
        public ConcurrentHashMap<String, Object> getHyperParameters() {
            ConcurrentHashMap<String, Object> params = super.getHyperParameters();
            params.put("decay", decay);
            params.put("eps", eps);
            params.put("scale", scale);
            return params;
        }
    }

    public static abstract class RMSpropGraves extends OptimizerBase {

        /** decay rate */
        private final double decay;
        private final double alpha;
        private final double momentum;
        /** constant for numerical stability */
        private final double eps;

        private final double scale; // to hold g*g in double range

        public RMSpropGraves(@Nonnull ConcurrentHashMap<String, String> options) {
            super(options);
            this.decay = StringParser.parseDouble(options.get("decay"), 0.95d);
            this.alpha = StringParser.parseDouble(options.get("alpha"), 1.d);
            this.momentum = StringParser.parseDouble(options.get("momentum"), 0.9d);
            this.eps = StringParser.parseDouble(options.get("eps"), 1.0d);
            this.scale = StringParser.parseDouble(options.get("scale"), 100.0d);
        }

        @Override
        public TrainWeights.WeightType getWeightType() {
            return TrainWeights.WeightType.WithSumOfGradientsAndSumOfSquaredGradientsAndDelta;
        }

        @Override
        protected double computeDelta(@Nonnull final TrainWeights.ExtendedWeight weight, final double gradient) {
            double old_scaled_n = weight.getSumOfSquaredGradients();
            double new_scaled_n = decay * old_scaled_n + (1.d - decay) * gradient * (gradient / scale);
            weight.setSumOfSquaredGradients(new_scaled_n);
            double old_scaled_g = weight.getSumOfGradients();
            double new_scaled_g = decay * old_scaled_g + (1.d - decay) * gradient / scale;
            weight.setSumOfGradients(new_scaled_g);
            double n = old_scaled_n * scale;
            double g = new_scaled_g * scale;
            double oldDelta = weight.getDelta();
            double delta = momentum * oldDelta + alpha * (double) (gradient / sqrt(n - g * g + eps));
            weight.setDelta(delta);
            return delta;
        }

        @Override
        public String getOptimizerName() {
            return "rmsprop_graves";
        }

        @Override
        public ConcurrentHashMap<String, Object> getHyperParameters() {
            ConcurrentHashMap<String, Object> params = super.getHyperParameters();
            params.put("decay", decay);
            params.put("alpha", alpha);
            params.put("momentum", momentum);
            params.put("eps", eps);
            return params;
        }

    }

    public static abstract class AdaDelta extends OptimizerBase {

        private final double decay;
        private final double eps;
        private final double scale;

        public AdaDelta(@Nonnull ConcurrentHashMap<String, String> options) {
            super(options);
            this.decay = StringParser.parseDouble(options.get("decay"), 0.95d);
            this.eps = StringParser.parseDouble(options.get("eps"), 1e-6d);
            this.scale = StringParser.parseDouble(options.get("scale"), 100.0d);
        }

        @Override
        public TrainWeights.WeightType getWeightType() {
            return TrainWeights.WeightType.WithSumOfSquaredGradientsAndSumOfSquaredDeltaX;
        }

        @Override
        protected final EtaEstimator getEtaEstimator(@Nonnull ConcurrentHashMap<String, String> options) {
            // override default learning rate scheme
            if (!options.containsKey("eta")) {
                options.put("eta", "fixed");
            }
            if (!options.containsKey("eta0")) {
                options.put("eta0", "1.0");
            }
            return super.getEtaEstimator(options);
        }

        @Override
        protected double computeDelta(@Nonnull final TrainWeights.ExtendedWeight weight, final double gradient) {
            double old_scaled_sum_sqgrad = weight.getSumOfSquaredGradients();
            double old_sum_squared_delta_x = weight.getSumOfSquaredDeltaX();
            double new_scaled_sum_sqgrad = (decay * old_scaled_sum_sqgrad)
                    + ((1.f - decay) * gradient * (gradient / scale));
            double delta = sqrt(
                    (old_sum_squared_delta_x + eps) / ( new_scaled_sum_sqgrad * scale + eps))
                    * gradient;
            double new_sum_squared_delta_x =
                    (decay * old_sum_squared_delta_x) + ((1.f - decay) * delta * delta);
            weight.setSumOfSquaredGradients(new_scaled_sum_sqgrad);
            weight.setSumOfSquaredDeltaX(new_sum_squared_delta_x);
            return delta;
        }

        @Override
        public String getOptimizerName() {
            return "adadelta";
        }

        @Override
        public ConcurrentHashMap<String, Object> getHyperParameters() {
            ConcurrentHashMap<String, Object> params = super.getHyperParameters();
            params.put("decay", decay);
            params.put("eps", eps);
            params.put("scale", scale);
            return params;
        }

    }

    public static abstract class Nadam extends OptimizerBase {

        protected double alpha;
        protected final double beta1, beta2;
        protected final double eps;
        protected final double decay;
        protected final double scheduleDecay;

        protected double mu_t, mu_t_1;
        protected double mu_product = 1.d;
        protected double mu_product_next = 1.d;

        public Nadam(@Nonnull ConcurrentHashMap<String, String> options) {
            super(options);
            this.alpha = StringParser.parseDouble(options.get("alpha"), 1.0d);
            this.beta1 = StringParser.parseDouble(options.get("beta1"), 0.9d);
            this.beta2 = StringParser.parseDouble(options.get("beta2"), 0.999d);
            this.eps = StringParser.parseDouble(options.get("eps"), 1e-8d);
            this.decay = StringParser.parseDouble(options.get("decay"), 0.d);
            this.scheduleDecay = StringParser.parseDouble(options.get("scheduleDecay"), 0.004d); // 1/250=0.004
        }

        @Override
        public TrainWeights.WeightType getWeightType() {
            return TrainWeights.WeightType.WithMAndV;
        }

        @Override
        public void proceedStep() {
            long t = _numStep + 1;
            this._numStep = t;
            double mu_product_prev = this.mu_product;
            // 0.9 * (1 - 0.5 * 0.96^(floor(t/250)+1))
            double mu_t = beta1 * (1.d - 0.5d * pow(0.96d, floor(t * scheduleDecay) + 1.d));
            double mu_t_1 =
                    beta1 * (1.d - 0.5d * pow(0.96d, floor((t + 1.d) * scheduleDecay) + 1.d));
            this.mu_t = mu_t;
            this.mu_t_1 = mu_t_1;
            this.mu_product = mu_product_prev * mu_t;
            this.mu_product_next = mu_product_prev * mu_t * mu_t_1;
        }

        @Override
        protected double eta(final long t) {
            double fix1 = 1.d - pow(beta1, t);
            double fix2 = 1.d - pow(beta2, t);
            double eta = _eta.eta(t);
            double fix = sqrt(fix2) / fix1;
            return (eta * fix);
        }

        protected double alpha() {
            double fix1 = 1.d - pow(beta1, _numStep);
            double fix2 = 1.d - pow(beta2, _numStep);
            double fix = sqrt(fix2) / fix1;
            return alpha * fix;
        }

        @Override
        protected double computeDelta(@Nonnull final TrainWeights.ExtendedWeight weight, double gradient) {
            if (decay != 0.f) {// L2 regularization for weight decay
                double oldWeight = weight.get();
                gradient += decay * oldWeight;
            }
            // update biased first moment estimate
            double m = beta1 * weight.getM() + (1.f - beta1) * gradient;
            double m_hat = m / (1.d - mu_product_next);
            // update biased second raw moment estimate
            double v = beta2 * weight.getV() + (1.d - beta2) * MathUtils.square(gradient);
            double v_hat = v / (1.d - pow(beta2, _numStep));
            // gradient update for the current timestamp
            double g_hat = gradient / (1.d - mu_product);
            double m_bar = (1.d - mu_t) * g_hat + mu_t_1 * m_hat;
            // bias correlation using v_hat and m_hat
            double deltaU = m_bar / (sqrt(v_hat) + eps);
            // compute delta update
            double alpha_t = alpha();
            double delta = alpha_t * deltaU;
            // weight decay
            if (decay != 0.d) {
                double oldWeight = weight.get();
                delta += decay * oldWeight;
            }
            weight.setM(m);
            weight.setV(v);
            return delta;
        }

        @Override
        public String getOptimizerName() {
            return "nadam";
        }

        @Override
        public ConcurrentHashMap<String, Object> getHyperParameters() {
            ConcurrentHashMap<String, Object> params = super.getHyperParameters();
            params.put("alpha", alpha);
            params.put("beta1", beta1);
            params.put("beta2", beta2);
            params.put("eps", eps);
            params.put("decay", decay);
            params.put("scheduleDecay", scheduleDecay);
            return params;
        }
    }

    public static abstract class Eve extends Adam {

        protected final double beta3;
        private double c = 10.d;
        private double inv_c = 0.1d;

        private double currLoss;
        private double prevLoss = 0.d;
        private double prevDt = 1.d;

        public Eve(@Nonnull ConcurrentHashMap<String, String> options) {
            super(options);
            this.beta3 = StringParser.parseDouble(options.get("beta3"), 0.999d);
            this.c = StringParser.parseDouble(options.get("c"), 10d);
            this.inv_c = 1f / c;
        }

        @Override
        public TrainWeights.WeightType getWeightType() {
            return TrainWeights.WeightType.Single;
        }

        @Override
        protected double alpha() {
            double fix1 = 1.d - pow(beta1, _numStep);
            double fix2 = 1.d - pow(beta2, _numStep);
            double fix = sqrt(fix2) / fix1;
            double alpha_t = alpha * fix;
            // feedback of Eve
            if (_numStep > 1 && currLoss != prevLoss) {
                double d = abs(currLoss - prevLoss) / min(currLoss, prevLoss);
                d = MathUtils.clip(d, inv_c, c); // [alpha/c, c*alpha]
                d = (beta3 * prevDt) + (1.d - beta3) * d;
                this.prevDt = d;
                alpha_t = alpha_t / d;
            }
            return alpha_t;
        }

        @Override
        public double update(Object feature, double weight, double loss, double gradient) {
            this.currLoss = loss;
            double delta = update(feature, weight, gradient);
            this.prevLoss = loss;
            return delta;
        }

        @Override
        public String getOptimizerName() {
            return "eve";
        }

        @Override
        public ConcurrentHashMap<String, Object> getHyperParameters() {
            ConcurrentHashMap<String, Object> params = super.getHyperParameters();
            params.put("beta3", beta3);
            params.put("c", c);
            return params;
        }
    }

    public static abstract class AdamHD extends Adam {

        private final double beta;
        protected double deltaU = 0.d;

        public AdamHD(@Nonnull ConcurrentHashMap<String, String> options) {
            super(options);
            this.alpha = StringParser.parseDouble(options.get("alpha"), 0.02f);
            this.beta = StringParser.parseDouble(options.get("beta"), 1e-6f);
        }

        @Override
        public TrainWeights.WeightType getWeightType() {
            return TrainWeights.WeightType.WithMAndV;
        }

        @Override
        protected final EtaEstimator getEtaEstimator(@Nonnull ConcurrentHashMap<String, String> options) {
            // override default learning rate scheme
            if (!options.containsKey("eta")) {
                options.put("eta", "fixed");
            }
            if (!options.containsKey("eta0")) {
                options.put("eta0", "1.0");
            }
            return super.getEtaEstimator(options);
        }

        private double alpha(final double gradient, final double deltaU) {
            // multiplicative hypergradient descent
            final double h = gradient * deltaU;
            if (h > 0) {// g_{t-1}u_{t-2} > 0
                this.alpha = alpha * (1.d - beta); // decrease alpha
            } else if (h < 0) {// g_{t-1}u_{t-2} < 0
                this.alpha = alpha * (1.d + beta); // increase alpha
            }
            return alpha;
        }

        @Override
        protected double computeDelta(@Nonnull final TrainWeights.ExtendedWeight weight, double gradient) {
            if (decay != 0.f) {// L2 regularization for weight decay
                double oldWeight = weight.get();
                gradient += decay * oldWeight;
            }
            // update biased first moment estimate
            double m = beta1 * ((TrainWeights.WeightWithMAndV) weight).getM() + (1.d - beta1) * gradient;
            // update biased second raw moment estimate
            double v = beta2 * ((TrainWeights.WeightWithMAndV) weight).getV() + (1.d - beta2) * square(gradient);
            // compute bias-corrected first moment estimate
            double m_hat = m / (1.d - pow(beta1, _numStep));
            // compute bias-corrected second raw moment estimate
            double v_hat = v / (1.d - pow(beta2, _numStep));
            // compute delta update
            double alpha_t = alpha(gradient, deltaU);
            double deltaU = m_hat / (sqrt(v_hat) + eps);
            double delta = (double) (alpha_t * deltaU);
            this.deltaU = deltaU;
            // weight decay
            if (decay != 0.f) {
                double oldWeight = weight.get();
                delta += decay * oldWeight;
            }
            weight.setM(m);
            weight.setV(v);
            return delta;
        }

        @Override
        public String getOptimizerName() {
            return "adam_hd";
        }

        @Override
        public ConcurrentHashMap<String, Object> getHyperParameters() {
            ConcurrentHashMap<String, Object> params = super.getHyperParameters();
            params.put("beta", beta);
            return params;
        }
    }

    public static abstract class AdagradRDA extends OptimizerBase {

        @Nonnull
        private final AdaGrad optimizerImpl;
        private final double lambda;

        public AdagradRDA(@Nonnull AdaGrad optimizerImpl, @Nonnull ConcurrentHashMap<String, String> options) {
            super(options);
            this.optimizerImpl = optimizerImpl;
            this.lambda = StringParser.parseDouble(options.get("lambda"), 1e-6d);
        }

        @Override
        public TrainWeights.WeightType getWeightType() {
            return TrainWeights.WeightType.WithSumOfSquaredGradientsAndSumOfGradients;
        }

        @Override
        protected double update(@Nonnull final TrainWeights.ExtendedWeight weight, final double gradient) {
            final double new_sum_grad = weight.getSumOfGradients() + gradient;
            // sign(u_{t,i})
            final double sign = (new_sum_grad > 0.d) ? 1.d : -1.d;
            // |u_{t,i}|/t - \lambda
            final double meansOfGradients = (sign * new_sum_grad / _numStep) - lambda;
            if (meansOfGradients < 0.f) {
                // x_{t,i} = 0
                weight.set(0.f);
                weight.setSumOfSquaredGradients(0.f);
                weight.setSumOfGradients(0.f);
                return 0.d;
            }
            else {
                // x_{t,i} = -sign(u_{t,i}) * \frac{\eta t}{\sqrt{G_{t,ii}}}(|u_{t,i}|/t - \lambda)
                double newWeight = -1.d * sign * _eta.eta(_numStep) * _numStep
                        * optimizerImpl.computeDelta(weight, meansOfGradients);
                weight.set(newWeight);
                weight.setSumOfGradients(new_sum_grad);
                return newWeight;
            }
        }

        @Override
        public String getOptimizerName() {
            return "adagrad_rda";
        }

        @Override
        public ConcurrentHashMap<String, Object> getHyperParameters() {
            ConcurrentHashMap<String, Object> params = optimizerImpl.getHyperParameters();
            params.put("optimizer", getOptimizerName()); // replace
            params.put("lambda", lambda);
            return params;
        }
    }
}
