package drillbit.neighbors.distance;

import javax.annotation.Nonnull;
import java.util.concurrent.ConcurrentHashMap;

public final class DistanceFactory {
    public static Distance create(@Nonnull final ConcurrentHashMap<String, String> options) {
        String distanceName = options.get("distance");
        if ("euclidean".equalsIgnoreCase(distanceName)) {
            return new EuclideanDistance(options);
        } else if ("hamming".equalsIgnoreCase(distanceName)) {
            return new HammingDistance(options);
        } else if ("manhattan".equalsIgnoreCase(distanceName)) {
            return new ManhattanDistance(options);
        } else if ("chebyshev".equalsIgnoreCase(distanceName)) {
            return new ChebyshevDistance(options);
        } else if ("cosine".equalsIgnoreCase(distanceName)) {
            return new CosineDistance(options);
        } else if ("angular".equalsIgnoreCase(distanceName)) {
            return new AngularDistance(options);
        } else if ("pnorm".equalsIgnoreCase(distanceName)) {
            return new PNormDistance(options);
        } else {
            throw new IllegalArgumentException("Unsupported distance metric: " + distanceName);
        }
    }

}
