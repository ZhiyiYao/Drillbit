package drillbit.neighbors.solver;

import drillbit.neighbors.distance.Distance;
import drillbit.neighbors.utils.Score;
import drillbit.parameter.Coordinates;
import drillbit.utils.parser.StringParser;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class KDTreeSolver extends Solver {
    int leafSize;
    double p;

    public KDTreeSolver(ConcurrentHashMap<String, String> options) {
        super(options);
        leafSize = StringParser.parseInt(options.get("leaf_size"), 10);
        p = StringParser.parseDouble(options.get("p"), 1.d);
    }

    @Override
    public ConcurrentHashMap<String, String> getOptions() {
        ConcurrentHashMap<String, String> opts = super.getOptions();

        // TODO: complete the options that can be specified to KDTree, refer to sklearn for more information.
        opts.put("leaf_size", "10");
        opts.put("p", "1");

        return opts;
    }

    @Override
    public void build(@Nonnull ArrayList<String> labels, @Nonnull ArrayList<Coordinates> coordinatesList) {
        // TODO: complete the build of KDTree.
    }

    @Override
    public int solveIndex(int k, Distance metric, double[] vec) {
        return 0;
    }

    @Override
    public String solveLabel(int k, Distance metric, double[] vec) {
        return null;
    }

    @Override
    public ArrayList<Score> solveNeighbors(int k, Distance metric, double[] vec) {
        return null;
    }

    public static class KDTree {
        private ArrayList<double[]> dataset;
        private ArrayList<Integer> knum;
        private Node root;
        private int dim;

        private int getSplit(int begin, int end) {
//            ArrayList<Double> vSum = new ArrayList<>();
//            ArrayList<Double> Sum = new ArrayList<>();
//            int split = 0;
//            int dim = (dataset.size() - 1) / 2;
//
//            for (int i = begin; i < end; i++) {
//                for (int j = 0; j < dim; j++) {
//                    vSum.set(i, vSum.get(i) + dataset.get(i)[j + dim]);
//                    Sum.set(i, Sum.get(i) + dataset.get(i)[j]);
//                }
//
//                vSum.set(i, vSum.get(i) - Sum.get(i) * Sum.get(i));
//                split = split > vSum.get(i) ? split : i;
//            }
//
//            qSort(begin, end - 1, split);
//
//            return split;
            return 0;
        }

        public int findNearest(int n, Distance metric, double[] x) {
//            int temp;
//            int maxLabel = -1;
//            int maxnum = 0;
//
//            for (int i = 0; i < K; i++) {
//                double[] d = new double[2];
//                d[0] = -1;
//                d[1] = -1;
//                nearest.set(i, d);
//            }
//
//            recurve(root, x);
//
//            for (int i = 0; i < K; i++) {
//                temp = (int) nearest.get(i)[1];
//                knum.set(temp, knum.get(temp) + 1);
//                if (maxnum < knum.get(temp)) {
//                    maxnum = knum.get(temp);
//                    maxLabel = temp;
//                }
//            }
//
//            return maxLabel;
            return 0;
        }

        private void recurve(Node n, double[] x) {
//            if (n != null) {
//                int axis = n.getSplit();
//                double daxis = x[axis] - n.getCoordinate()[axis];
//
//                if (daxis < 0) {
//                    recurve(n.getLeft(), x);
//                } else {
//                    recurve(n.getRight(), x);
//                }
//
//                double[] d = new double[2];
//
//                double dist = distanceMetric.evaluate(x, n.coordinate);
//                d[0] = dist;
//                d[1] = n.getLabel();
//                for (int i = 0; i < K; i++) {
//                    if (nearest.get(i)[0] < 0 || dist < nearest.get(i)[0]) {
//                        nearest.add(i, d);
//                        nearest.remove(K);
//                        break;
//                    }
//                }
//
//                if (nearest.get(0)[0] > Math.abs(daxis)) {
//                    if (daxis < 0)
//                        recurve(n.right, x);
//                    else
//                        recurve(n.left, x);
//                }
//            }
        }

        public void build(ArrayList<Coordinates> data) {
//            dataset = new ArrayList<>();
//            int dims = (int) data.get(0).nDim();
//            double[] dataV, temp;
//            knum = new ArrayList<>(data.size());
//            dim = dims * 2 + 1;
//
//            for (int i = 0; i < data.size(); i++) {
//
//                for (int j = 0; j < data.get(i).size(); j++) {
//                    dataV = new double[dim];
//                    temp = data.get(i).get(j);
//                    for (int k = 0; k < data.get(i).get(j).length; k++) {
//                        dataV[k] = temp[k];
//                        dataV[k + temp.length] = temp[k] * temp[k];
//                    }
//                    dataV[dim - 1] = i;
//                    dataset.add(dataV);
//                }
//            }
//
//            root = build(0, dataset.size());
        }

        private Node build(int begin, int end) {
//            if (begin == end) {
//                return null;
//            }
//
//            Node root = new Node();
//            double[] d = new double[(dim - 1) / 2];
//            int s = getSplit(begin, end);
//            root.setLabel((int) dataset.get((begin + end) / 2)[dim - 1]);
//            for (int i = 0; i < (dim - 1) / 2; i++) {
//                d[i] = dataset.get((begin + end) / 2)[i];
//            }
//
//            root.setCoordinate(d);
//            root.setLeft(build(0, (begin + end) / 2));
//            root.setRight(build((begin + end) / 2 + 1, end));
//            root.setSplit(s);
//
//            return root;
//            return null;
            return null;
        }

        private void qSort(int low, int high, int dim) {
//            if (low < high) {
//                int pos = low;
//                double[] pivot = dataset.get(pos), temp;
//                for (int i = low + 1; i <= high; i++)
//                    if (dataset.get(i)[dim] > pivot[dim]) {
//                        pos++;
//                        temp = dataset.get(pos);
//                        dataset.set(pos, dataset.get(i));
//                        dataset.set(i, temp);
//                    }
//                temp = dataset.get(low);
//                dataset.set(low, dataset.get(pos));
//                dataset.set(pos, temp);
//                qSort(low, pos - 1, dim);
//                qSort(pos + 1, high, dim);
//            }
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
