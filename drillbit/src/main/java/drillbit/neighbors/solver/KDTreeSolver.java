package drillbit.neighbors.solver;

import drillbit.neighbors.distance.Distance;
import drillbit.neighbors.utils.Counter;
import drillbit.neighbors.utils.Score;
import drillbit.neighbors.weight.Weight;
import drillbit.parameter.Coordinates;
import drillbit.parameter.DenseCoordinates;
import drillbit.utils.parser.StringParser;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

public class KDTreeSolver extends Solver {
	int leafSize;
	KDTree tree;
	ArrayList<String> labels;

	public KDTreeSolver(ConcurrentHashMap<String, String> options) {
		super(options);
		leafSize = StringParser.parseInt(options.get("leaf_size"), 10);
	}

	@Override
	public ConcurrentHashMap<String, String> getOptions() {
		ConcurrentHashMap<String, String> opts = super.getOptions();

		opts.put("leaf_size", "10");

		return opts;
	}

	@Override
	public void build(@Nonnull ArrayList<String> labels, @Nonnull ArrayList<Coordinates> coordinatesList) {
		tree = new KDTree();
		tree.build(coordinatesList, labels);
		this.labels = labels;
	}

	@Override
	public int solveIndex(int k, Distance metric, Weight weight, double[] vec) {
		tree.reset();
		return tree.findNearest(k, metric, vec, weight);
	}

	@Override
	public String solveLabel(int k, Distance metric, Weight weight, double[] vec) {
		tree.reset();
		return labels.get(tree.findNearest(k, metric, vec, weight));
	}

	@Override
	public ArrayList<Score> solveNeighbors(int k, Distance metric, Weight weight, double[] vec) {
		tree.reset();
		return tree.getScore(k, metric, vec);
	}

	public class KDTree {
		private Coordinates coordinates;
		private ArrayList<Counter> counters;
		private ArrayList<Score> scores;
		private Node root;
		private int dim;

		public int findNearest(int k, Distance distance, double[] x, Weight weight) {
			int maxLabel = -1;
			double maxnum = 0;

			scores = new ArrayList<>();
			reset();
			recurve(root, k, distance, x);

			for (int i = 0; i < k; i++) {
				int label = StringParser.parseInt(scores.get(i).getLabel(), 0);
				counters.get(label).incr(weight.evaluate(scores.get(i).getDistance()));
				if (maxnum < counters.get(label).getCount()) {
					maxnum = counters.get(label).getCount();
					maxLabel = label;
				}
			}

			return maxLabel;

		}

		private void recurve(Node node, int k, Distance distance, double[] x) {
			if (node != null) {
				int split = node.getSplit();
				double dSplit = x[split] - node.getCoordinate()[split];

				if (dSplit < 0) {
					recurve(node.getLeft(), k, distance, x);
				}
				else {
					recurve(node.getRight(), k, distance, x);
				}

				double dist = distance.evaluate(x, node.getCoordinate());
				Score score = new Score(Integer.toString(node.getLabel()), dist, node.getCoordinate());

				for (int i = 0; i < k; i++) {
					if (scores.size() < i + 1) {
						scores.add(score);
						break;
					}
					if (dist < scores.get(i).getDistance()) {
						scores.add(i, score);
						if (scores.size() > k) {
							scores.remove(k - 1);
						}
						break;
					}
				}

				if (scores.get(scores.size() - 1).getDistance() > Math.abs(dSplit)) {
					if (dSplit < 0)
						recurve(node.getRight(), k, distance, x);
					else
						recurve(node.getLeft(), k, distance, x);
				}
			}
		}

		public void build(ArrayList<Coordinates> coordinatesList, ArrayList<String> labels) {
			assert coordinatesList != null && labels != null && coordinatesList.size() == labels.size();

			dim = coordinatesList.get(0).nDim();
			counters = new ArrayList<>();
			coordinates = new DenseCoordinates(2 * dim + 1);

			for (String label : labels) {
				Counter counter = new Counter(label);
				counters.add(counter);
			}

			for (int i = 0; i < coordinatesList.size(); i++) {
				for (int j = 0; j < dim; j++) {
					double[] extendedCoordinate = new double[dim*2+1];
					double[] coordinate = coordinatesList.get(i).get(j);
					for (int k = 0; k < dim; k++) {
						extendedCoordinate[k] = coordinate[k];
						extendedCoordinate[k + dim] = coordinate[k] * coordinate[k];
					}
					extendedCoordinate[2 * dim] = i;
					coordinates.add(extendedCoordinate);
				}
			}

			root = build(coordinates, 0, coordinates.size());
		}

		private Node build(Coordinates coordinates, int begin, int end) {
			if (begin == end) {
				return null;
			}

			int split = getSplit(coordinates, begin, end);
			double[] coordinate = Arrays.copyOfRange(coordinates.get((begin + end) / 2), 0, dim);
			int label = (int) coordinates.get((begin + end) / 2)[2 * dim];

			Node node = new Node();

			node.setCoordinate(coordinate);
			node.setLabel(label);
			node.setSplit(split);
			node.setLeft(build(coordinates, 0, (begin + end) / 2));
			node.setRight(build(coordinates, (begin + end) / 2 + 1, end));

			return node;
		}

		public void reset() {
			if (scores != null) {
				scores.clear();
			}
			else {
				scores = new ArrayList<>();
			}

			if (counters != null) {
				for (int i = 0; i < counters.size(); i++) {
					Counter counter = new Counter(counters.get(i).getLabel());
					counters.set(i, counter);
				}
			}
			else {
				counters = new ArrayList<>();
			}
		}

		private int getSplit(Coordinates coordinates, int begin, int end) {
			int split = 0;

			for (int j = 0; j < dim; j++) {
				double v = 0;
				double s = 0;
				double[] coordinate ;
				for (int i = begin; i < end; i++) {
					coordinate= coordinates.get(i);
					v += coordinate[j + dim];
					s += coordinate[j];
				}
				v -= s * s;
				split = split > v ? split : j;
			}

			quickSort(coordinates, begin, end - 1, split);

			return split;
		}

		public ArrayList<Score> getScore(int k, Distance distance, double[] x) {
			recurve(root, k, distance, x);

			return scores;
		}

		private void quickSort(Coordinates coordinates, int low, int high, int dim) {
			if (low < high) {
				int pos = low;
				double[] pivot = coordinates.get(pos), temp;
				for (int i = low + 1; i <= high; i++) {
					if (coordinates.get(i)[dim] > pivot[dim]) {
						pos++;
						temp = coordinates.get(pos);
						coordinates.set(pos, coordinates.get(i));
						coordinates.set(i, temp);
					}
				}

				temp = coordinates.get(low);
				coordinates.set(low, coordinates.get(pos));
				coordinates.set(pos, temp);
				quickSort(coordinates, low, pos - 1, dim);
				quickSort(coordinates,pos + 1, high, dim);
			}
		}

		private class Node {
			double[] coordinate;
			int label;
			int split;
			Node left;
			Node right;

			public void setLeft(Node left) {
				this.left = left;
			}

			public void setRight(Node right) {
				this.right = right;
			}

			public void setSplit(int split) {
				this.split = split;
			}

			public void setLabel(int label) {
				this.label = label;
			}

			public int getSplit() {
				return split;
			}

			public int getLabel() {
				return label;
			}

			public Node getLeft() {
				return left;
			}

			public Node getRight() {
				return right;
			}

			public void setCoordinate(double[] coordinate) {
				this.coordinate = coordinate;
			}

			public double[] getCoordinate() {
				return coordinate;
			}
		}
	}
}
