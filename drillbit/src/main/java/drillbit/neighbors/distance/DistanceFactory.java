package drillbit.neighbors.distance;

public class DistanceFactory {
    public static Distance getDistance(String distanceName) {
        if ("euclidean".equalsIgnoreCase(distanceName)) {
            return new EuclideanDistance();
        }
        else if ("hamming".equalsIgnoreCase(distanceName)) {
            return new HammingDistance();
        }
        else if ("manhattan".equalsIgnoreCase(distanceName)) {
            return new ManhattanDistance();
        }
        else if ("chebyshev".equalsIgnoreCase(distanceName)) {
            return new ChebyshevDistance();
        }
        else if ("cosine".equalsIgnoreCase(distanceName)) {
            return new CosineDistance();
        }
        else if ("angular".equalsIgnoreCase(distanceName)) {
        	return new AngularDistance();
        }

        throw new IllegalArgumentException("Unsupported distance metric: " + distanceName);
    }

}
