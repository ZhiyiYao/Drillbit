package drillbit.neighbors.solver;

import drillbit.neighbors.distance.Distance;
import drillbit.neighbors.utils.Score;
import drillbit.neighbors.weight.Weight;
import drillbit.parameter.Coordinates;
import drillbit.parameter.DenseCoordinates;
import drillbit.utils.parser.StringParser;

import javax.annotation.Nonnull;
import java.util.ArrayList;
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
		tree.build(coordinatesList);
		this.labels = labels;
	}

	@Override
	public int solveIndex(int k, Distance metric, Weight weight, double[] vec) {
		return tree.findNearest(k, metric, vec);
	}

	@Override
	public String solveLabel(int k, Distance metric, Weight weight, double[] vec) {
		return labels.get(solveIndex(k, metric, weight, vec));
	}

	@Override
	public ArrayList<Score> solveNeighbors(int k, Distance metric, Weight weight, double[] vec) {
		return null;
	}

	public class KDTree {
		private Coordinates dataset;
		private ArrayList<Integer> score;
		private ArrayList<double[]> counter;
		private Node root;
		private int dim;
		private int K;

		private int getSplit(int begin, int end) {
			ArrayList<Double> vSum = new ArrayList<>();
			ArrayList<Double> Sum = new ArrayList<>();
			int split = 0;
			int dim = (dataset.size() - 1) / 2;

			for (int i = begin; i < end; i++) {
				for (int j = 0; j < dim; j++) {
					vSum.set(i, vSum.get(i) + dataset.get(i)[j + dim]);
					Sum.set(i, Sum.get(i) + dataset.get(i)[j]);
				}

				vSum.set(i, vSum.get(i) - Sum.get(i) * Sum.get(i));
				split = split > vSum.get(i) ? split : i;
			}

			qSort(begin, end - 1, split);

			return split;
		}

		public int findNearest(int n, Distance metric, double[] x) {
			int maxLabel = -1;
			int maxnum = 0;

			K = n;

			for (int i = 0; i < n; i++) {
				double[] d = new double[2];
				d[0] = -1;
				d[1] = -1;
				counter.set(i, d);
			}

			recurve(root, x, metric);

			for (int i = 0; i < n; i++) {
				int temp = (int) counter.get(i)[1];
				score.set(temp, score.get(temp) + 1);
				if (maxnum < score.get(temp)) {
					maxnum = score.get(temp);
					maxLabel = temp;
				}
			}

			return maxLabel;

		}

		private void recurve(Node node, double[] x, Distance distanceMetric) {
			if (node != null) {
				int axis = node.getSplit();
				double daxis = x[axis] - node.getCoordinate()[axis];

				if (daxis < 0) {
					recurve(node.getLeft(), x, distanceMetric);
				}
				else {
					recurve(node.getRight(), x, distanceMetric);
				}

				double[] d = new double[2];

				double dist = distanceMetric.evaluate(x, node.getCoordinate());
				d[0] = dist;
				d[1] = node.getLabel();
				for (int i = 0; i < K; i++) {
					if (counter.get(i)[0] < 0 || dist < counter.get(i)[0]) {
						counter.add(i, d);
						counter.remove(K);
						break;
					}
				}

				if (counter.get(0)[0] > Math.abs(daxis)) {
					if (daxis < 0)
						recurve(node.right, x, distanceMetric);
					else
						recurve(node.left, x, distanceMetric);
				}
			}
		}

		public void build(ArrayList<Coordinates> data) {
			dataset = new DenseCoordinates(data.size());
			int dims = (int) data.get(0).nDim();
			double[] dataV, temp;
			score = new ArrayList<>(data.size());
			dim = dims * 2 + 1;

			for (int i = 0; i < data.size(); i++) {
				for (int j = 0; j < data.get(i).size(); j++) {
					dataV = new double[dim];
					temp = data.get(i).get(j);
					for (int k = 0; k < data.get(i).get(j).length; k++) {
						dataV[k] = temp[k];
						dataV[k + temp.length] = temp[k] * temp[k];
					}
					dataV[dim - 1] = i;
					dataset.set(i + j, dataV);
				}
			}

			root = build(0, dataset.size());
		}

		private Node build(int begin, int end) {
			if (begin == end) {
				return null;
			}

			Node node = new Node();
			double[] d = new double[(dim - 1) / 2];
			int s = getSplit(begin, end);
			node.setLabel((int) dataset.get((begin + end) / 2)[dim - 1]);
			for (int i = 0; i < (dim - 1) / 2; i++) {
				d[i] = dataset.get((begin + end) / 2)[i];
			}

			node.setCoordinate(d);
			node.setLeft(build(0, (begin + end) / 2));
			node.setRight(build((begin + end) / 2 + 1, end));
			node.setSplit(s);

			return node;
		}

		private void qSort(int low, int high, int dim) {
			if (low < high) {
				int pos = low;
				double[] pivot = dataset.get(pos), temp;
				for (int i = low + 1; i <= high; i++)
					if (dataset.get(i)[dim] > pivot[dim]) {
						pos++;
						temp = dataset.get(pos);
						dataset.set(pos, dataset.get(i));
						dataset.set(i, temp);
					}
				temp = dataset.get(low);
				dataset.set(low, dataset.get(pos));
				dataset.set(pos, temp);
				qSort(low, pos - 1, dim);
				qSort(pos + 1, high, dim);
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
