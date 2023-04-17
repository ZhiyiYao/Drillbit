package drillbit.data;

import drillbit.FeatureValue;
import drillbit.utils.parser.StringParser;

import javax.annotation.Nonnull;
import java.util.ArrayList;

public class FeatureHelper {
    public static String addBias(@Nonnull final String features) {
        ArrayList<String> featureStrings = StringParser.parseArray(features);
        final int iMax = featureStrings.size() - 1;
        if (iMax == -1) {
            return "[]";
        }

        StringBuilder builder = new StringBuilder();
        builder.append("[");
        builder.append("0:1.0, ");
        for (int i = 0; ; i++) {
            builder.append(featureStrings.get(i));
            if (i == iMax) {
                return builder.append("]").toString();
            }
            builder.append(", ");
        }
    }

    public static String addIndex(@Nonnull final String features) {
        ArrayList<String> featureStrings = StringParser.parseArray(features);
        final int iMax = featureStrings.size() - 1;
        if (iMax == -1) {
            return "[]";
        }

        ArrayList<FeatureValue> featureValues = new ArrayList<>();
        IntegerIndexGenerator generator = new IntegerIndexGenerator();
        for (int i = 0; i <= iMax; i++) {
            String featureString = featureStrings.get(i);
            double value = StringParser.parseDouble(featureString, Double.NaN);
            if (Double.isNaN(value)) {
                if (featureString.indexOf(':') != -1 || featureString.indexOf('#') != -1) {
                    // For feature that has a delimiter.
                    FeatureValue featureValue = StringParser.parseFeature(featureString);
                    generator.reportUsedIndex(featureValue.getFeature());
                    featureValues.add(featureValue);
                } else {
                    // For feature that does not have a delimiter.
                    // Can be a scalar value or a string feature.
                    featureValues.add(new FeatureValue(generator.nextIndex(), featureString, FeatureValue.FeatureType.NUMERICAL));
                }
            } else {
                featureValues.add(new FeatureValue(generator.nextIndex(), value, FeatureValue.FeatureType.NUMERICAL));
            }
        }

        StringBuilder builder = new StringBuilder();
        builder.append("[");
        for (int i = 0; ; i++) {
            builder.append(featureValues.get(i).toString());
            if (i == iMax) {
                return builder.append("]").toString();
            }
            builder.append(", ");
        }
    }

    public static class IntegerIndexGenerator {
        int index;
        ArrayList<Integer> used;

        public IntegerIndexGenerator() {
            index = 1;
            used = new ArrayList<>();
        }

        public String nextIndex() {
            while (isUsed()) {
                index++;
            }
            index++;
            return Integer.toString(index - 1);
        }

        public void reportUsedIndex(String usedIndex) {
            int usedInteger = StringParser.parseInt(usedIndex, Integer.MAX_VALUE);
            if (usedInteger != Integer.MAX_VALUE) {
                // Only add used integer features to the used list.
                used.add(usedInteger);
            }
            if (0 < usedInteger && usedInteger < index) {
                throw new IllegalArgumentException(String.format("Index %s has been used, should be greater than %d.", usedIndex, index - 1));
            }
        }

        private boolean isUsed() {
            for (int usedInteger : used) {
                if (index == usedInteger) {
                    return true;
                }
            }
            return false;
        }
    }
}
