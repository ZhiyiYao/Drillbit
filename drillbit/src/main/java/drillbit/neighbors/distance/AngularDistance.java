package drillbit.neighbors.distance;

public class AngularDistance implements Distance {

	@Override
	public double evaluate(double[] vec1, double[] vec2) {
		return Math.acos(new CosineDistance().evaluate(vec1, vec2));
	}
}
