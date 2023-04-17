package drillbit.neighbors.utils;

public class Score {
    double[] coordinate;
    private String label;
    private double distance;

    public Score(String label, double distance, double[] coordinate) {
        this.label = label;
        this.distance = distance;
        this.coordinate = coordinate;
    }

    public String getLabel() {
        return label;
    }

    public Score setLabel(String label) {
        this.label = label;
        return this;
    }

    public double getDistance() {
        return distance;
    }

    public Score setDistance(double distance) {
        this.distance = distance;
        return this;
    }

    public double[] getCoordinate() {
        return coordinate;
    }

    public Score setCoordinate(double[] coordinate) {
        this.coordinate = coordinate;
        return this;
    }
}

