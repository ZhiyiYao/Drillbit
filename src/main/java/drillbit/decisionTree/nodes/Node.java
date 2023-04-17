package drillbit.decisionTree.nodes;

import drillbit.FeatureValue;

import java.util.ArrayList;

public interface Node {
    public Node getChild(int index);

    byte[] getBytes();

    void display();

    public void setLeaf(boolean isLeaf);

    public void setNodeMap(ArrayList<Double> feature, ArrayList<Node> child);

    public Node getChild(ArrayList<FeatureValue> record);

    public double getPrediction(ArrayList<FeatureValue> record);
}
