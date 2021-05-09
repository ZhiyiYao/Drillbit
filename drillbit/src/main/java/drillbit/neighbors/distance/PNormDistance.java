package drillbit.neighbors.distance;

import drillbit.utils.parser.StringParser;

import java.util.concurrent.ConcurrentHashMap;

public class PNormDistance extends Distance {
	int p;

	public PNormDistance(ConcurrentHashMap<String, String> options) {
		super(options);
		p = StringParser.parseInt(options.get("p"), 1);
	}

	@Override
	public double evaluate(double[] vec1, double[] vec2) {
		int len = vec1.length;
		double squaredDistance = 0;

		for (int i = 0; i < len; i++) {
			squaredDistance += Math.pow(Math.abs(vec1[i] - vec2[i]), p);
		}

		return Math.pow(squaredDistance, 1 / (double) p);
	}
}
