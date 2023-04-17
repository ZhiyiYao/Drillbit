package drillbit.decisionTree.algorithm;

public class decisionAlgorithmFactory {
    public static decisionAL make(String a, boolean discrete) {
        if (discrete) {
            if (a.equals("ID3")) {
                return new discreteAL(new discreteID3()) {
                };
            } else if (a.equals("C45")) {
                return new discreteAL(new discreteC45()) {
                };
            } else if (a.equals("gini")) {
                return new discreteAL(new discreteGini()) {
                };
            } else {
                throw new RuntimeException("unsupported algorithm");
            }
        } else {
            if (a.equals("ID3")) {
                return new continuousAL(new discreteID3()) {
                };
            } else if (a.equals("C45")) {
                return new discreteAL(new discreteC45()) {
                };
            } else if (a.equals("gini")) {
                return new discreteAL(new discreteGini()) {
                };
            } else {
                throw new RuntimeException("unsupported algorithm");
            }
        }
    }

}
