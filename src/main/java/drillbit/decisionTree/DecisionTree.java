package drillbit.decisionTree;

import drillbit.FeatureValue;
import drillbit.decisionTree.algorithm.decisionAL;
import drillbit.decisionTree.algorithm.decisionAlgorithmFactory;
import drillbit.decisionTree.nodes.Node;
import drillbit.decisionTree.nodes.NodesFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class DecisionTree {
    Node root;
    NodesFactory nodeMaker;
    boolean isDiscrete;
    decisionAL decisionMaker;
    int heightLimit;
    ArrayList<String> targetMap;

    public DecisionTree(String al, boolean discrete, int heightLimit) {
        isDiscrete = discrete;
        nodeMaker = new NodesFactory();
        decisionMaker = decisionAlgorithmFactory.make(al, discrete);
        this.heightLimit = heightLimit;
    }

    public DecisionTree(byte[] tree) throws IOException {
        targetMap = new ArrayList<>();
        DataInputStream reader = new DataInputStream(new ByteArrayInputStream(tree));
        int targetLen = reader.readInt();
        for (int i = 0; i < targetLen; i++) {
            int stringLen = reader.readInt();
            byte[] buffer = new byte[stringLen];
            reader.read(buffer);
            targetMap.add(new String(buffer));
        }

        nodeMaker = new NodesFactory();
        root = nodeMaker.BuildTreeFromBytes(reader);
        root.display();
    }

    public String predict(ArrayList<FeatureValue> features) {
        double dIndex = root.getPrediction(features);
        //root.display();
        return targetMap.get((int) dIndex);
    }

    public byte[] getBytes() {
        ByteArrayOutputStream data = new ByteArrayOutputStream();
        DataOutputStream writer = new DataOutputStream(data);
        try {
            writer.writeInt(targetMap.size());
            for (String t : targetMap) {
                byte[] b = t.getBytes(StandardCharsets.UTF_8);
                writer.writeInt(b.length);
                writer.write(b);
            }

            writer.write(root.getBytes());
        } catch (IOException ignored) {

        }
        return data.toByteArray();
    }

    private ArrayList<Integer> makeAvailFeatures(ArrayList<ArrayList<FeatureValue>> featureValueVectors) {
        ArrayList<Integer> features = new ArrayList<>();
        int size = featureValueVectors.get(0).size();
        for (int i = 0; i < size; i++) {
            features.add(i);
        }
        return features;
    }

    public void train(ArrayList<ArrayList<FeatureValue>> featureValueVectors,
                      ArrayList<String> targets) {
        this.makeAvailFeatures(featureValueVectors);

        root = buildNode(featureValueVectors, getTargetMap(targets));
        root.display();
    }

    private ArrayList<Double> getTargetMap(ArrayList<String> targets) {
        ArrayList<Double> targetsIndex = new ArrayList<>();
        ConcurrentHashMap<String, Double> targetMapIndex = new ConcurrentHashMap<>();
        double currentIndex = 0;
        for (String t : targets) {
            if (!targetMapIndex.containsKey(t)) {
                targetMapIndex.put(t, currentIndex);
                currentIndex += 1;
            }
            Double i = targetMapIndex.get(t);
            targetsIndex.add(i);
        }

        targetMap = new ArrayList<>();
        for (int i = 0; i < currentIndex; i++) {
            targetMap.add("");
        }
        for (String target : targetMapIndex.keySet()) {
            targetMap.set(targetMapIndex.get(target).intValue(), target);
        }

        return targetsIndex;
    }

    private Node buildNode(ArrayList<ArrayList<FeatureValue>> featureValueVectors,
                           ArrayList<Double> targets) {
        ConcurrentHashMap<Integer, ArrayList<Double>>
                flag = decisionMaker.getFeature(
                makeAvailFeatures(featureValueVectors), featureValueVectors, targets);
        //System.out.println(flag);
        if (flag == null) {
            return buildLeafNode(targets);
        }

        ArrayList<Integer> keys = new ArrayList<>(flag.keySet());
        int index = keys.get(0);

        ArrayList<Node> children;
        if (isDiscrete) {
            children = discreteDatasetDivision(index, flag.get(index), featureValueVectors, targets);
        } else {
            children = continuousDatasetDivision(index, flag.get(index), featureValueVectors, targets);
            //System.out.println(children.size());
        }

        Node cur = nodeMaker.getNode(isDiscrete, featureValueVectors.get(0).get(index).getFeature(), -1);
        cur.setLeaf(false);
        cur.setNodeMap(flag.get(index), children);

        return cur;
    }

    private Node buildLeafNode(ArrayList<Double> targets) {
        ArrayList<Integer> counts = new ArrayList<>();
        for (int i = 0; i < targets.size(); i++) {
            int value = targets.get(i).intValue();

            while (value >= counts.size()) {
                counts.add(0);
            }

            counts.set(value, counts.get(value) + 1);
        }

        int maxIndex = -1;
        int maxValue = 0;
        for (int i = 0; i < counts.size(); i++) {
            if (counts.get(i) > maxValue) {
                maxValue = counts.get(i);
                maxIndex = i;
            }
        }

        Node cur = nodeMaker.getNode(isDiscrete, "-1", maxIndex);
        cur.setLeaf(true);

        return cur;
    }

    private ArrayList<Node> discreteDatasetDivision(int featureIndex, ArrayList<Double> valueSet,
                                                    ArrayList<ArrayList<FeatureValue>> featureValueVectors,
                                                    ArrayList<Double> targets) {
        int sampleNum = featureValueVectors.size();
        ArrayList<Node> children;
        HashMap<Double, ArrayList<ArrayList<FeatureValue>>> featuresDivision = new HashMap<>();
        HashMap<Double, ArrayList<Double>> targetsDivision = new HashMap<>();
        for (Double v : valueSet) {
            featuresDivision.put(v, new ArrayList<>());
            targetsDivision.put(v, new ArrayList<>());
        }

        for (int i = 0; i < sampleNum; i++) {
            ArrayList<FeatureValue> features = featureValueVectors.get(i);
            double featureValue = features.get(featureIndex).getValueAsDouble();

            featuresDivision.get(featureValue).add(features);
            targetsDivision.get(featureValue).add(targets.get(i));
        }

        children = new ArrayList<>();
        for (Double v : valueSet) {
            children.add(buildNode(featuresDivision.get(v), targetsDivision.get(v)));
        }

        return children;
    }

    private ArrayList<Node> continuousDatasetDivision(int featureIndex, ArrayList<Double> valueSet,
                                                      ArrayList<ArrayList<FeatureValue>> featureValueVectors,
                                                      ArrayList<Double> targets) {
        int sampleNum = featureValueVectors.size();
        double flag = valueSet.get(0);
        ArrayList<Node> children;
        ArrayList<ArrayList<ArrayList<FeatureValue>>> featuresDivision = new ArrayList<>();
        ArrayList<ArrayList<Double>> targetsDivision = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            featuresDivision.add(new ArrayList<>());
            targetsDivision.add(new ArrayList<>());
        }

        for (int i = 0; i < sampleNum; i++) {
            ArrayList<FeatureValue> features = featureValueVectors.get(i);
            double featureValue = features.get(featureIndex).getValueAsDouble();

            if (featureValue < flag) {
                featuresDivision.get(0).add(features);
                targetsDivision.get(0).add(targets.get(i));
            } else {
                featuresDivision.get(1).add(features);
                targetsDivision.get(1).add(targets.get(i));
            }
        }

        children = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            children.add(buildNode(featuresDivision.get(i), targetsDivision.get(i)));
        }

        return children;
    }


}
