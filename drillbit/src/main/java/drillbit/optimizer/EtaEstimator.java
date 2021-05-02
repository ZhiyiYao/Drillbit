package drillbit.optimizer;

import java.util.Map;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.cli.CommandLine;

public abstract class EtaEstimator {
    public static final double DEFAULT_ETA0 = 0.1d;
    public static final double DEFAULT_ETA = 0.3d;
    public static final double DEFAULT_POWER_T = 0.1d;

    protected final double eta0;

    public EtaEstimator(double eta0) {
        this.eta0 = eta0;
    }

    @Nonnull
    public abstract String typeName();

    public double eta0() {
        return eta0;
    }

    public abstract double eta(long t);

    public void update(@Nonnegative double multiplier) {}

    public void getHyperParameters(@Nonnull Map<String, Object> hyperParams) {
        hyperParams.put("eta", typeName());
        hyperParams.put("eta0", eta0());
    }

    public static final class FixedEtaEstimator extends EtaEstimator {

        public FixedEtaEstimator(double eta) {
            super(eta);
        }

        @Nonnull
        public String typeName() {
            return "Fixed";
        }

        @Override
        public double eta(long t) {
            return eta0;
        }

        @Override
        public String toString() {
            return "FixedEtaEstimator [ eta0 = " + eta0 + " ]";
        }

    }

    public static final class SimpleEtaEstimator extends EtaEstimator {

        private final double finalEta;
        private final double total_steps;

        public SimpleEtaEstimator(double eta0, long total_steps) {
            super(eta0);
            this.finalEta = (double) (eta0 / 2.d);
            this.total_steps = total_steps;
        }

        @Nonnull
        public String typeName() {
            return "Simple";
        }

        @Override
        public double eta(final long t) {
            if (t > total_steps) {
                return finalEta;
            }
            return eta0 / (1.d + (t / total_steps));
        }

        @Override
        public String toString() {
            return "SimpleEtaEstimator [ eta0 = " + eta0 + ", totalSteps = " + total_steps
                    + ", finalEta = " + finalEta + " ]";
        }

        public void getHyperParameters(@Nonnull Map<String, Object> hyperParams) {
            super.getHyperParameters(hyperParams);
            hyperParams.put("total_steps", total_steps);
        }

    }

    public static final class InvscalingEtaEstimator extends EtaEstimator {

        private final double power_t;

        public InvscalingEtaEstimator(double eta0, double power_t) {
            super(eta0);
            this.power_t = power_t;
        }

        @Nonnull
        public String typeName() {
            return "Invscaling";
        }

        @Override
        public double eta(final long t) {
            return eta0 / Math.pow(t, power_t);
        }

        @Override
        public String toString() {
            return "InvscalingEtaEstimator [ eta0 = " + eta0 + ", power_t = " + power_t + " ]";
        }

        public void getHyperParameters(@Nonnull Map<String, Object> hyperParams) {
            super.getHyperParameters(hyperParams);
            hyperParams.put("power_t", power_t);
        }
    }

    /**
     * bold driver: Gemulla et al., Large-scale matrix factorization with distributed stochastic
     * gradient descent, KDD 2011.
     */
    public static final class AdjustingEtaEstimator extends EtaEstimator {

        private double eta;

        public AdjustingEtaEstimator(double eta) {
            super(eta);
            this.eta = eta;
        }

        @Nonnull
        public String typeName() {
            return "boldDriver";
        }

        @Override
        public double eta(long t) {
            return eta;
        }

        @Override
        public void update(@Nonnegative double multiplier) {
            double newEta = eta * multiplier;
            if (!isFinite(newEta)) {
                // avoid NaN or INFINITY
                return;
            }
            this.eta = Math.min(eta0, newEta); // never be larger than eta0
        }

        @Override
        public String toString() {
            return "AdjustingEtaEstimator [ eta0 = " + eta0 + ", eta = " + eta + " ]";
        }

    }
    
    private static boolean isFinite(final double v) {
        return (v > Double.NEGATIVE_INFINITY) & (v < Double.POSITIVE_INFINITY);
    }

    @Nonnull
    public static EtaEstimator get(@Nullable CommandLine cl) throws IllegalArgumentException {
        return get(cl, DEFAULT_ETA0);
    }

    @Nonnull
    public static EtaEstimator get(@Nullable CommandLine cl, double defaultEta0)
            throws IllegalArgumentException {
        if (cl == null) {
            return new InvscalingEtaEstimator(defaultEta0, DEFAULT_POWER_T);
        }

        if (cl.hasOption("boldDriver")) {
            double eta = parsedouble(cl.getOptionValue("eta"), DEFAULT_ETA);
            return new AdjustingEtaEstimator(eta);
        }

        String etaValue = cl.getOptionValue("eta");
        if (etaValue != null) {
            double eta = Double.parseDouble(etaValue);
            return new FixedEtaEstimator(eta);
        }

        double eta0 = parsedouble(cl.getOptionValue("eta0"), defaultEta0);
        if (cl.hasOption("t")) {
            long t = Long.parseLong(cl.getOptionValue("t"));
            return new SimpleEtaEstimator(eta0, t);
        }

        double power_t = parseDouble(cl.getOptionValue("power_t"), DEFAULT_POWER_T);
        return new InvscalingEtaEstimator(eta0, power_t);
    }

    @Nonnull
    public static EtaEstimator get(@Nonnull final Map<String, String> options)
            throws IllegalArgumentException {
        final double eta0 = parsedouble(options.get("eta0"), DEFAULT_ETA0);
        final double power_t = parseDouble(options.get("power_t"), DEFAULT_POWER_T);

        final String etaScheme = options.get("eta");
        if (etaScheme == null) {
            return new InvscalingEtaEstimator(eta0, power_t);
        }

        if ("fixed".equalsIgnoreCase(etaScheme)) {
            return new FixedEtaEstimator(eta0);
        } else if ("simple".equalsIgnoreCase(etaScheme)) {
            final long t;
            if (options.containsKey("total_steps")) {
                t = Long.parseLong(options.get("total_steps"));
            } else {
                throw new IllegalArgumentException(
                    "-total_steps MUST be provided when `-eta simple` is specified");
            }
            return new SimpleEtaEstimator(eta0, t);
        } else if ("inv".equalsIgnoreCase(etaScheme) || "inverse".equalsIgnoreCase(etaScheme)
                || "invscaling".equalsIgnoreCase(etaScheme)) {
            return new InvscalingEtaEstimator(eta0, power_t);
        } else {
            if (isNumber(etaScheme)) {
                double eta = Double.parseDouble(etaScheme);
                return new FixedEtaEstimator(eta);
            }
            throw new IllegalArgumentException("Unsupported ETA name: " + etaScheme);
        }
    }
    
    private static double parsedouble(final String s, final double defaultValue) {
        if (s == null) {
            return defaultValue;
        }
        return Double.parseDouble(s);
    }

    private static double parseDouble(final String s, final double defaultValue) {
        if (s == null) {
            return defaultValue;
        }
        return Double.parseDouble(s);
    }
    
    public static boolean isNumber(@Nullable final String str) {
        if (str == null || str.length() == 0) {
            return false;
        }
        char[] chars = str.toCharArray();
        int sz = chars.length;
        boolean hasExp = false;
        boolean hasDecPoint = false;
        boolean allowSigns = false;
        boolean foundDigit = false;
        // deal with any possible sign up front
        int start = (chars[0] == '-') ? 1 : 0;
        if (sz > start + 1) {
            if (chars[start] == '0' && chars[start + 1] == 'x') {
                int i = start + 2;
                if (i == sz) {
                    return false; // str == "0x"
                }
                // checking hex (it can't be anything else)
                for (; i < chars.length; i++) {
                    if ((chars[i] < '0' || chars[i] > '9') && (chars[i] < 'a' || chars[i] > 'f')
                            && (chars[i] < 'A' || chars[i] > 'F')) {
                        return false;
                    }
                }
                return true;
            }
        }
        sz--; // don't want to loop to the last char, check it afterwords
        // for type qualifiers
        int i = start;
        // loop to the next to last char or to the last char if we need another digit to
        // make a valid number (e.g. chars[0..5] = "1234E")
        while (i < sz || (i < sz + 1 && allowSigns && !foundDigit)) {
            if (chars[i] >= '0' && chars[i] <= '9') {
                foundDigit = true;
                allowSigns = false;

            } else if (chars[i] == '.') {
                if (hasDecPoint || hasExp) {
                    // two decimal points or dec in exponent
                    return false;
                }
                hasDecPoint = true;
            } else if (chars[i] == 'e' || chars[i] == 'E') {
                // we've already taken care of hex.
                if (hasExp) {
                    // two E's
                    return false;
                }
                if (!foundDigit) {
                    return false;
                }
                hasExp = true;
                allowSigns = true;
            } else if (chars[i] == '+' || chars[i] == '-') {
                if (!allowSigns) {
                    return false;
                }
                allowSigns = false;
                foundDigit = false; // we need a digit after the E
            } else {
                return false;
            }
            i++;
        }
        if (i < chars.length) {
            if (chars[i] >= '0' && chars[i] <= '9') {
                // no type qualifier, OK
                return true;
            }
            if (chars[i] == 'e' || chars[i] == 'E') {
                // can't have an E at the last byte
                return false;
            }
            if (!allowSigns
                    && (chars[i] == 'd' || chars[i] == 'D' || chars[i] == 'f' || chars[i] == 'F')) {
                return foundDigit;
            }
            if (chars[i] == 'l' || chars[i] == 'L') {
                // not allowing L with an exponent
                return foundDigit && !hasExp;
            }
            // last character is illegal
            return false;
        }
        // allowSigns is true iff the val ends in 'E'
        // found digit it to make sure weird stuff like '.' and '1E-' doesn't pass
        return !allowSigns && foundDigit;
    }
}
