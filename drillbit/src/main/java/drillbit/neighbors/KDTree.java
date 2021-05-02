package drillbit.neighbors;

import java.util.ArrayList;

import drillbit.neighbors.distance.Distance;
import drillbit.neighbors.distance.DistanceFactory;
import drillbit.parameter.Coordinates;

public class KDTree {
    private ArrayList<double[]> dataSet;
    private ArrayList<double[]> nearest;
    private ArrayList<Integer> knum;
    private KDTreeNode root;
    private int dim;
    private Distance distanceMetric;
    private int K;

    private int getSplit(int begin, int end) {
        ArrayList<Double> vSum = new ArrayList<>();
        ArrayList<Double> Sum = new ArrayList<>();
        int split = 0;
        int dim = (dataSet.size() - 1) / 2;

        for (int i = begin; i < end; i++) {
            for (int j = 0; j < dim; j++) {
                vSum.set(i, vSum.get(i) + dataSet.get(i)[j + dim]);
                Sum.set(i, Sum.get(i) + dataSet.get(i)[j]);
            }

            vSum.set(i, vSum.get(i) - Sum.get(i) * Sum.get(i));
            split = split > vSum.get(i) ? split : i;
        }

        quickSort(begin, end - 1, split);

        return split;
    }

    public int findNearist(int K, String distance, double[] x) {
        nearest = new ArrayList<>(K);
        distanceMetric = DistanceFactory.getDistance(distance);
        this.K = K;
        int temp;
        int maxLabel = -1;
        int maxnum = 0;

        for (int i = 0; i < K; i++) {
            double[] d = new double[2];
            d[0] = -1;
            d[1] = -1;
            nearest.set(i, d);
        }

        recurve(root, x);

        for (int i = 0; i < K; i++) {
            temp = (int) nearest.get(i)[1];
            knum.set(temp, knum.get(temp) + 1);
            if (maxnum < knum.get(temp)) {
                maxnum = knum.get(temp);
                maxLabel = temp;
            }
        }

        return maxLabel;
    }

    private void recurve(KDTreeNode n, double[] x) {
        if (n != null) {
            int axis = n.getSplit();
            double daxis = x[axis] - n.getData()[axis];

            if (daxis < 0) {
                recurve(n.getLeft(), x);
            } else {
                recurve(n.getRight(), x);
            }

            double[] d = new double[2];

            double dist = distanceMetric.evaluate(x, n.data);
            d[0] = dist;
            d[1] = n.getLabel();
            for (int i = 0; i < K; i++) {
                if (nearest.get(i)[0] < 0 || dist < nearest.get(i)[0]) {
                    nearest.add(i, d);
                    nearest.remove(K);
                    break;
                }
            }

            if (nearest.get(0)[0] > Math.abs(daxis)) {
                if (daxis < 0)
                    recurve(n.right, x);
                else
                    recurve(n.left, x);
            }
        }
    }

    public void build_kdtree(ArrayList<Coordinates> data) {
        dataSet = new ArrayList<double[]>();
        int dims = (int) data.get(0).nDim();
        double[] dataV, temp;
        knum = new ArrayList<Integer>(data.size());
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
                dataSet.add(dataV);
            }
        }

        root = build(0, dataSet.size());
    }

    private KDTreeNode build(int begin, int end) {
        if (begin == end) {
            return null;
        }

        KDTreeNode now = new KDTreeNode();
        double[] d = new double[(dim - 1) / 2];
        int s = getSplit(begin, end);
        now.setLabel((int) dataSet.get((begin + end) / 2)[dim - 1]);
        for (int i = 0; i < (dim - 1) / 2; i++) {
            d[i] = dataSet.get((begin + end) / 2)[i];
        }

        now.setData(d);
        now.setLeft(build(0, (begin + end) / 2));
        now.setRight(build((begin + end) / 2 + 1, end));
        now.setSplit(s);

        return now;
    }

    private void quickSort(int low, int high, int dim) {
        int p_pos, i;
        double[] pivot, temp;
        if (low < high) {
            p_pos = low;
            pivot = dataSet.get(p_pos);
            for (i = low + 1; i <= high; i++)
                if (dataSet.get(i)[dim] > pivot[dim]) {
                    p_pos++;
                    temp = dataSet.get(p_pos);
                    dataSet.set(p_pos, dataSet.get(i));
                    dataSet.set(i, temp);
                }
            temp = dataSet.get(low);
            dataSet.set(low, dataSet.get(p_pos));
            dataSet.set(p_pos, temp);
            quickSort(low, p_pos - 1, dim);
            quickSort(p_pos + 1, high, dim);
        }
    }

    private static class KDTreeNode {
        double[] data;
        int label;
        int split;
        KDTreeNode left;
        KDTreeNode right;

        public void setLeft(KDTreeNode l) {
            left = l;
        }

        public void setRight(KDTreeNode r) {
            right = r;
        }

        public void setSplit(int s) {
            split = s;
        }

        public void setLabel(int s) {
            label = s;
        }

        public int getSplit() {
            return split;
        }

        public int getLabel() {
            return label;
        }

        public KDTreeNode getLeft() {
            return left;
        }

        public KDTreeNode getRight() {
            return right;
        }

        public void setData(double[] d) {
            data = d;
        }

        public double[] getData() {
            return data;
        }
    }
}
