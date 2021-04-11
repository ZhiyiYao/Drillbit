package drillbit.parameter;

public final class Margin {

    private final double correctScore;
    private final Object maxIncorrectLabel;
    private final double maxIncorrectScore;

    private double variance;

    public Margin(double correctScore, Object maxIncorrectLabel, double maxIncorrectScore) {
        this.correctScore = correctScore;
        this.maxIncorrectLabel = maxIncorrectLabel;
        this.maxIncorrectScore = maxIncorrectScore;
    }

    public double get() {
        return correctScore - maxIncorrectScore;
    }

    public Margin variance(double var) {
        this.variance = var;
        return this;
    }

    public double getCorrectScore() {
        return correctScore;
    }

    public Object getMaxIncorrectLabel() {
        return maxIncorrectLabel;
    }

    public double getMaxIncorrectScore() {
        return maxIncorrectScore;
    }

    public double getVariance() {
        return variance;
    }

}
