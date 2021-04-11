package drillbit.utils.udf;

public class DatasetHelper {
    public static class FeatureAndTargetHelper {
        public FeatureAndTargetHelper() {}

        public String extractFeature(String featureAndTarget) {
            if (featureAndTarget.charAt(0) == '[') {
                featureAndTarget = featureAndTarget.substring(1);
            }

            if (featureAndTarget.charAt(featureAndTarget.length() - 1) == ']') {
                featureAndTarget = featureAndTarget.substring(0, featureAndTarget.length() - 1);
            }

            int index = featureAndTarget.lastIndexOf(',');

            return featureAndTarget.substring(0, index);
        }

        public String extractTarget(String featureAndTarget) {
            if (featureAndTarget.charAt(0) == '[') {
                featureAndTarget = featureAndTarget.substring(1);
            }

            if (featureAndTarget.charAt(featureAndTarget.length() - 1) == ']') {
                featureAndTarget = featureAndTarget.substring(0, featureAndTarget.length() - 1);
            }

            int index = featureAndTarget.lastIndexOf(',');

            return featureAndTarget.substring(index + 1);
        }
    }
}
