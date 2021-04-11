/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package drillbit.optimizer;

import drillbit.utils.math.MathUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @link https://github.com/JohnLangford/vowpal_wabbit/wiki/Loss-functions
 */
public final class LossFunctions {

    public enum LossType {
        SquaredLoss, QuantileLoss, EpsilonInsensitiveLoss, SquaredEpsilonInsensitiveLoss, HuberLoss,
        HingeLoss, LogLoss, SquaredHingeLoss, ModifiedHuberLoss
    }

    @Nonnull
    public static LossFunction getLossFunction(@Nullable final String type) {
        if ("SquaredLoss".equalsIgnoreCase(type) || "squared".equalsIgnoreCase(type)) {
            return new SquaredLoss();
        } else if ("QuantileLoss".equalsIgnoreCase(type) || "quantile".equalsIgnoreCase(type)) {
            return new QuantileLoss();
        } else if ("EpsilonInsensitiveLoss".equalsIgnoreCase(type)
                || "epsilon_insensitive".equalsIgnoreCase(type)) {
            return new EpsilonInsensitiveLoss();
        } else if ("SquaredEpsilonInsensitiveLoss".equalsIgnoreCase(type)
                || "squared_epsilon_insensitive".equalsIgnoreCase(type)) {
            return new SquaredEpsilonInsensitiveLoss();
        } else if ("HuberLoss".equalsIgnoreCase(type) || "huber".equalsIgnoreCase(type)) {
            return new HuberLoss();
        } else if ("HingeLoss".equalsIgnoreCase(type) || "hinge".equalsIgnoreCase(type)) {
            return new HingeLoss();
        } else if ("LogLoss".equalsIgnoreCase(type) || "log".equalsIgnoreCase(type)
                || "LogisticLoss".equalsIgnoreCase(type) || "logistic".equalsIgnoreCase(type)) {
            return new LogLoss();
        } else if ("SquaredHingeLoss".equalsIgnoreCase(type)
                || "squared_hinge".equalsIgnoreCase(type)) {
            return new SquaredHingeLoss();
        } else if ("ModifiedHuberLoss".equalsIgnoreCase(type)
                || "modified_huber".equalsIgnoreCase(type)) {
            return new ModifiedHuberLoss();
        }
        throw new IllegalArgumentException("Unsupported loss function name: " + type);
    }

    @Nonnull
    public static LossFunction getLossFunction(@Nonnull final LossType type) {
        switch (type) {
            case SquaredLoss:
                return new SquaredLoss();
            case QuantileLoss:
                return new QuantileLoss();
            case EpsilonInsensitiveLoss:
                return new EpsilonInsensitiveLoss();
            case SquaredEpsilonInsensitiveLoss:
                return new SquaredEpsilonInsensitiveLoss();
            case HuberLoss:
                return new HuberLoss();
            case HingeLoss:
                return new HingeLoss();
            case LogLoss:
                return new LogLoss();
            case SquaredHingeLoss:
                return new SquaredHingeLoss();
            case ModifiedHuberLoss:
                return new ModifiedHuberLoss();
            default:
                throw new IllegalArgumentException("Unsupported loss function name: " + type);
        }
    }

    public interface LossFunction {

        /**
         * Evaluate the loss function.
         *
         * @param p The prediction, p = w^T x
         * @param y The true value (aka target)
         * @return The loss evaluated at `p` and `y`.
         */
        public double loss(double p, double y);

        /**
         * Evaluate the derivative of the loss function with respect to the prediction `p`.
         *
         * @param p The prediction, p = w^T x
         * @param y The true value (aka target)
         * @return The derivative of the loss function w.r.t. `p`.
         */
        public double dloss(double p, double y);

        public boolean forBinaryClassification();

        public boolean forRegression();

        @Nonnull
        public LossType getType();

    }

    public static abstract class RegressionLoss implements LossFunction {

        @Override
        public boolean forBinaryClassification() {
            return false;
        }

        @Override
        public boolean forRegression() {
            return true;
        }
    }

    public static abstract class BinaryLoss implements LossFunction {

        protected static void checkTarget(final double y) {
            if (!(y == 1.d || y == -1.d)) {
                throw new IllegalArgumentException("target must be [+1,-1]: " + y);
            }
        }

        @Override
        public boolean forBinaryClassification() {
            return true;
        }

        @Override
        public boolean forRegression() {
            return false;
        }
    }

    /**
     * Squared loss for regression problems.
     *
     * If you're trying to minimize the mean error, use squared-loss.
     */
    public static final class SquaredLoss extends RegressionLoss {

        @Override
        public double loss(final double p, final double y) {
            final double z = p - y;
            return z * z * 0.5d;
        }

        @Override
        public double dloss(final double p, final double y) {
            return p - y; // 2 (p - y) / 2
        }

        @Override
        public LossType getType() {
            return LossType.SquaredLoss;
        }
    }

    /**
     * Quantile loss is useful to predict rank/order and you do not mind the mean error to increase
     * as long as you get the relative order correct.
     *
     * @link http://en.wikipedia.org/wiki/Quantile_regression
     */
    public static final class QuantileLoss extends RegressionLoss {

        private double tau;

        public QuantileLoss() {
            this.tau = 0.5f;
        }

        public QuantileLoss(double tau) {
            setTau(tau);
        }

        public void setTau(double tau) {
            if (tau <= 0 || tau >= 1.0) {
                throw new IllegalArgumentException("tau must be in range (0, 1): " + tau);
            }
            this.tau = tau;
        }

        @Override
        public double loss(final double p, final double y) {
            double e = y - p;
            if (e > 0.d) {
                return tau * e;
            } else {
                return -(1.d - tau) * e;
            }
        }

        @Override
        public double dloss(final double p, final double y) {
            double e = y - p;
            if (e == 0.f) {
                return 0.f;
            }
            return (e > 0.f) ? -tau : (1.f - tau);
        }

        @Override
        public LossType getType() {
            return LossType.QuantileLoss;
        }
    }

    /**
     * Epsilon-Insensitive loss used by Support Vector Regression (SVR).
     * <code>loss = max(0, |y - p| - epsilon)</code>
     */
    public static final class EpsilonInsensitiveLoss extends RegressionLoss {

        private double epsilon;

        public EpsilonInsensitiveLoss() {
            this(0.1f);
        }

        public EpsilonInsensitiveLoss(double epsilon) {
            this.epsilon = epsilon;
        }

        public void setEpsilon(double epsilon) {
            this.epsilon = epsilon;
        }

        @Override
        public double loss(final double p, final double y) {
            double loss = Math.abs(y - p) - epsilon;
            return (loss > 0.d) ? loss : 0.d;
        }

        @Override
        public double dloss(final double p, final double y) {
            if ((y - p) > epsilon) {// real value > predicted value - epsilon
                return -1.f;
            } else if ((p - y) > epsilon) {// real value < predicted value - epsilon
                return 1.f;
            } else {
                return 0.f;
            }
        }

        @Override
        public LossType getType() {
            return LossType.EpsilonInsensitiveLoss;
        }
    }

    /**
     * Squared Epsilon-Insensitive loss. <code>loss = max(0, |y - p| - epsilon)^2</code>
     */
    public static final class SquaredEpsilonInsensitiveLoss extends RegressionLoss {

        private double epsilon;

        public SquaredEpsilonInsensitiveLoss() {
            this(0.1f);
        }

        public SquaredEpsilonInsensitiveLoss(double epsilon) {
            this.epsilon = epsilon;
        }

        public void setEpsilon(double epsilon) {
            this.epsilon = epsilon;
        }

        @Override
        public double loss(final double p, final double y) {
            double d = Math.abs(y - p) - epsilon;
            return (d > 0.d) ? (d * d) : 0.d;
        }

        @Override
        public double dloss(final double p, final double y) {
            final double z = y - p;
            if (z > epsilon) {
                return -2 * (z - epsilon);
            } else if (-z > epsilon) {
                return 2 * (-z - epsilon);
            } else {
                return 0.f;
            }
        }

        @Override
        public LossType getType() {
            return LossType.SquaredEpsilonInsensitiveLoss;
        }
    }

    /**
     * Huber regression loss.
     *
     * Variant of the SquaredLoss which is robust to outliers.
     *
     * @link https://en.wikipedia.org/wiki/Huber_Loss_Function
     */
    public static final class HuberLoss extends RegressionLoss {

        private double c;

        public HuberLoss() {
            this(1.f); // i.e., beyond 1 standard deviation, the loss becomes linear
        }

        public HuberLoss(double c) {
            this.c = c;
        }

        public void setC(double c) {
            this.c = c;
        }

        @Override
        public double loss(final double p, final double y) {
            final double r = p - y;
            final double rAbs = Math.abs(r);
            if (rAbs <= c) {
                return 0.5d * r * r;
            }
            return c * rAbs - (0.5d * c * c);
        }

        @Override
        public double dloss(final double p, final double y) {
            final double r = p - y;
            final double rAbs = Math.abs(r);
            if (rAbs <= c) {
                return r;
            } else if (r > 0.f) {
                return c;
            }
            return -c;
        }

        @Override
        public LossType getType() {
            return LossType.HuberLoss;
        }
    }

    /**
     * Hinge loss for binary classification tasks with y in {-1,1}.
     */
    public static final class HingeLoss extends BinaryLoss {

        private double threshold;

        public HingeLoss() {
            this(1.f);
        }

        /**
         * @param threshold Margin threshold. When threshold=1.0, one gets the loss used by SVM.
         *        When threshold=0.0, one gets the loss used by the Perceptron.
         */
        public HingeLoss(double threshold) {
            this.threshold = threshold;
        }

        public void setThreshold(double threshold) {
            this.threshold = threshold;
        }

        @Override
        public double loss(final double p, final double y) {
            double loss = hingeLoss(p, y, threshold);
            return (loss > 0.d) ? loss : 0.d;
        }

        @Override
        public double dloss(final double p, final double y) {
            double loss = hingeLoss(p, y, threshold);
            return (loss > 0.f) ? -y : 0.f;
        }

        @Override
        public LossType getType() {
            return LossType.HingeLoss;
        }
    }

    /**
     * Logistic regression loss for binary classification with y in {-1, 1}.
     */
    public static final class LogLoss extends BinaryLoss {

        /**
         * <code>logloss(p,y) = log(1+exp(-p*y))</code>
         */
        @Override
        public double loss(final double p, final double y) {
            checkTarget(y);

            final double z = y * p;
            if (z > 18.d) {
                return Math.exp(-z);
            }
            if (z < -18.d) {
                return -z;
            }
            return Math.log(1.d + Math.exp(-z));
        }

        @Override
        public double dloss(final double p, final double y) {
            checkTarget(y);

            double z = y * p;
            if (z > 18.f) {
                return (double) Math.exp(-z) * -y;
            }
            if (z < -18.f) {
                return -y;
            }
            return -y / ((double) Math.exp(z) + 1.f);
        }

        @Override
        public LossType getType() {
            return LossType.LogLoss;
        }
    }

    /**
     * Squared Hinge loss for binary classification tasks with y in {-1,1}.
     */
    public static final class SquaredHingeLoss extends BinaryLoss {

        @Override
        public double loss(final double p, final double y) {
            return squaredHingeLoss(p, y);
        }

        @Override
        public double dloss(final double p, final double y) {
            checkTarget(y);

            double d = 1 - (y * p);
            return (d > 0.f) ? -2.f * d * y : 0.f;
        }

        @Override
        public LossType getType() {
            return LossType.SquaredHingeLoss;
        }
    }

    /**
     * Modified Huber loss for binary classification with y in {-1, 1}.
     *
     * Equivalent to quadratically smoothed SVM with gamma = 2.
     */
    public static final class ModifiedHuberLoss extends BinaryLoss {

        @Override
        public double loss(final double p, final double y) {
            final double z = p * y;
            if (z >= 1.d) {
                return 0.d;
            } else if (z >= -1.d) {
                return (1.d - z) * (1.d - z);
            }
            return -4.d * z;
        }

        @Override
        public double dloss(final double p, final double y) {
            final double z = p * y;
            if (z >= 1.f) {
                return 0.f;
            } else if (z >= -1.f) {
                return 2.f * (1.f - z) * -y;
            }
            return -4.f * y;
        }

        @Override
        public LossType getType() {
            return LossType.ModifiedHuberLoss;
        }
    }

    /**
     * logistic loss function where target is 0 (negative) or 1 (positive).
     */
    public static double logisticLoss(final double target, final double predicted) {
        if (predicted > -100.d) {
            return target - (double) MathUtils.sigmoid(predicted);
        } else {
            return target;
        }
    }

    public static double logLoss(final double p, final double y) {
        BinaryLoss.checkTarget(y);

        final double z = y * p;
        if (z > 18.d) {
            return Math.exp(-z);
        }
        if (z < -18.d) {
            return -z;
        }
        return Math.log(1.d + Math.exp(-z));
    }

    public static double squaredLoss(final double p, final double y) {
        final double z = p - y;
        return z * z * 0.5d;
    }

    public static double hingeLoss(final double p, final double y, final double threshold) {
        BinaryLoss.checkTarget(y);

        double z = y * p;
        return threshold - z;
    }

    public static double hingeLoss(final double p, final double y) {
        return hingeLoss(p, y, 1.d);
    }

    public static double squaredHingeLoss(final double p, final double y) {
        BinaryLoss.checkTarget(y);

        double z = y * p;
        double d = 1.d - z;
        return (d > 0.d) ? d * d : 0.d;
    }

    /**
     * Math.abs(target - predicted) - epsilon
     */
    public static double epsilonInsensitiveLoss(final double predicted, final double target,
                                               final double epsilon) {
        return Math.abs(target - predicted) - epsilon;
    }
}
