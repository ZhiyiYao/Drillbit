package drillbit;

import drillbit.utils.parser.ObjectParser;
import drillbit.utils.parser.StringParser;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/*
    Numerical and categorical feature value.
    Numerical value contains float or double value.
    Categorical value contains string or int value.
 */
public final class FeatureValue {
    private String feature;
    private Object value;

    private FeatureType featureType;

    public enum FeatureType {
        NUMERICAL, CATEGORICAL
    }

    public String getFeature() {
        return feature;
    }

    public void setFeature(String feature) {
        this.feature = feature;
    }

    public FeatureValue(String f, Object v, FeatureType type) {
        feature = f;
        value = v;
        featureType = type;
    }

    public FeatureType getFeatureType() {
        return featureType;
    }

    public int getValueAsInt() {
        if (featureType == FeatureType.NUMERICAL) {
            return ObjectParser.parseInt(value);
        }
        else {
            return 1;
        }
    }

    public double getValueAsDouble() {
        if (featureType == FeatureType.NUMERICAL) {
            return ObjectParser.parseDouble(value);
        }
        else {
            return 1.d;
        }
    }

    public float getValueAsFloat() {
        if (featureType == FeatureType.NUMERICAL) {
            return ObjectParser.parseFloat(value);
        }
        else {
            return 1.f;
        }
    }

    public void setValue(float value) {
        if (this.featureType == FeatureType.NUMERICAL) {
            this.value = (double) value;
            return;
        }
        throw new UnsupportedOperationException(String.format("Cannot assign %f to categorical value.", value));
    }

    public void setValue(double value) {
        if (this.featureType == FeatureType.NUMERICAL) {
            this.value = value;
            return;
        }
        throw new UnsupportedOperationException(String.format("Cannot assign %f to categorical value.", value));
    }

    public void setValue(String value) {
        if (this.featureType == FeatureType.CATEGORICAL) {
            this.value = value;
            return;
        }
        throw new UnsupportedOperationException(String.format("Cannot assign %s to numerical value.", value));
    }

    public void setValue(int value) {
        if (this.featureType == FeatureType.CATEGORICAL) {
            this.value = value;
            return;
        }
        throw new UnsupportedOperationException(String.format("Cannot assing $d to numerical value", value));
    }

    public void setValue(@Nonnull Object value) {
        if (featureType == FeatureType.NUMERICAL) {
            setValue(ObjectParser.parseDouble(value));
        }
        else {
            setValue(ObjectParser.parseString(value));
        }
    }

    @Nullable
    public static FeatureValue parse(final Object o) throws IllegalArgumentException {
        if (o == null) {
            return null;
        }
        String s = ObjectParser.parseString(o);
        return parse(s);
    }

    @Nonnull
    public static FeatureValue parse(@Nonnull String s) throws IllegalArgumentException {
        int pos;
        String feature;
        Object value;
        FeatureType featureType;

        // remove quotes.
        while ((s.indexOf('"') == 0) || (s.indexOf('\'') == 0)) {
            s = s.substring(1);
        }
        while ((s.indexOf('"') == s.length() - 1) || (s.indexOf('\'') == s.length() - 1)) {
            s = s.substring(0, s.length() - 1);
        }

        if (s.indexOf(':') != -1) {
            // Numerical feature value.
            pos = s.indexOf(':');
            if (pos == 0) {
                throw new IllegalArgumentException("Invalid numerical feature value representation: " + s);
            }
            feature = s.substring(0, pos);
            value = StringParser.parseDouble(s.substring(pos + 1), 1.d);
            featureType = FeatureType.NUMERICAL;
        }
        else if (s.indexOf('#') != -1){
            // Categorical feature value.
            pos = s.indexOf('#');
            if (pos == 0) {
                throw new IllegalArgumentException("Invalid categorical feature value representation: " + s);
            }
            feature = s.substring(0, pos);
            value = s.substring(pos + 1);
            featureType = FeatureType.CATEGORICAL;
        }
        else {
            // No delimiter, recognized as numerical feature value.
            try {
                // For scalar values that have no feature.
                double v = StringParser.parseDouble(s, Double.NaN);
                feature = "";
                value = v;
            }
            catch (NumberFormatException e) {
                // For string features that have no value.
                feature = s;
                // Set weight to 1.
                value = 1.d;
            }
            finally {
                featureType = FeatureType.NUMERICAL;
            }
        }

        return new FeatureValue(feature, value, featureType);
    }

    public static void parse(@Nonnull final String s, @Nonnull final FeatureValue probe) throws IllegalArgumentException {
        FeatureValue featureValue = parse(s);
        probe.feature = featureValue.feature;
        probe.value = featureValue.value;
        probe.featureType = featureValue.featureType;
    }

    @Override
    public String toString() {
        if (featureType == FeatureType.NUMERICAL) {
            return feature + ":" + value;
        }
        else {
            return feature + "#" + value;
        }
    }

}
