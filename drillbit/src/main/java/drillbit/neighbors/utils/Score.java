package drillbit.neighbors.utils;

public class Score {
    private String label;
    private double distance;
    double[] coordinate;

    public Score(String label, double distance, double[] coordinate) {
        this.label = label;
        this.distance = distance;
        this.coordinate = coordinate;
    }

    public Score setLabel(String label) {
        this.label = label;
        return this;
    }

    public Score setDistance(double distance) {
        this.distance = distance;
        return this;
    }

    public Score setCoordinate(double[] coordinate) {
        this.coordinate = coordinate;
        return this;
    }

    public String getLabel() {
        return label;
    }

    public double getDistance() {
        return distance;
    }

    public double[] getCoordinate() {
        return coordinate;
    }
}

