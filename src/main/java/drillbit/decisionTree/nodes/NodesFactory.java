package drillbit.decisionTree.nodes;

import drillbit.FeatureValue;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class NodesFactory {
    private Random rand;

    private void setupRandom() {
        rand = new Random();
    }

    public Node getNode(boolean discrete, String feature, double target) {
        if (discrete) {
            return new discreteNode(feature, target);
        } else {
            return new continuousNode(feature, target);
        }
    }

    public Node BuildTreeFromBytes(DataInputStream bytes) throws IOException {
        setupRandom();
        return buildOneNode(bytes);
    }

    private Node buildOneNode(DataInputStream stream) throws IOException {
        Node cur;
        int isLeaf = stream.readInt();
        if (isLeaf == 1) {
            double target = stream.readDouble();
            int isDiscrete = stream.readInt();
            if (isDiscrete == 1) {
                cur = getNode(true, "-1", target);
            } else {
                cur = getNode(false, "-1", target);
            }
            cur.setLeaf(true);
        } else {
            int childrenSize = stream.readInt();
            int featureSize = stream.readInt();
            byte[] temp = new byte[featureSize];
            int readed = stream.read(temp);
            assert readed == featureSize;
            String feature = new String(temp);

            ArrayList<Double> keys = new ArrayList<>(childrenSize);
            int isDiscrete = stream.readInt();
            if (isDiscrete == 1) {
                for (int i = 0; i < childrenSize; i++) {
                    keys.add(0.);
                }
                for (int i = 0; i < childrenSize; i++) {
                    double key = stream.readDouble();
                    keys.set(stream.readInt(), key);
                }
            } else {
                keys.add(stream.readDouble());
            }

            ArrayList<Node> nodes = new ArrayList<>();
            for (int i = 0; i < childrenSize; i++) {
                nodes.add(buildOneNode(stream));
            }

            if (isDiscrete == 1) {
                cur = getNode(true, feature, -1);
            } else {
                cur = getNode(false, feature, -1);
            }
            cur.setLeaf(false);
            cur.setNodeMap(keys, nodes);
        }

        return cur;
    }

    private enum type {discrete, continuous}

    private abstract class abstractNode implements Node {
        String feature;
        boolean isLeaf;
        double target;
        ArrayList<Node> children;
        ByteArrayOutputStream bytes;

        public abstractNode(String feature, double target) {
            this.feature = feature;
            this.target = target;
        }

        protected DataOutputStream basicInfo() throws IOException {
            bytes = new ByteArrayOutputStream();
            DataOutputStream data = new DataOutputStream(bytes);
            if (isLeaf) {
                data.writeInt(1);
                data.writeDouble(target);
            } else {
                data.writeInt(0);
                data.writeInt(children.size());

                byte[] featureBytes = feature.getBytes(StandardCharsets.UTF_8);
                data.writeInt(featureBytes.length);
                data.write(featureBytes);
            }

            return data;
        }

        @Override
        public abstract byte[] getBytes();

        public void setLeaf(boolean isLeaf) {
            this.isLeaf = isLeaf;
        }

        public abstract void setNodeMap(ArrayList<Double> feature, ArrayList<Node> child);

        public Node getChild(int index) {
            return children.get(index);
        }

        public double getPrediction(ArrayList<FeatureValue> record) {
            if (isLeaf) {
                return target;
            } else {
                return getChild(record).getPrediction(record);
            }
        }

        @Override
        public void display() {
            if (isLeaf) {
                System.out.print("leaf:" + Double.toString(target) + "\n");
            } else {
                System.out.print(feature + ":{\n");
                for (Node n : children) {
                    n.display();
                }
                System.out.print("}\n");
            }
        }

        public abstract Node getChild(ArrayList<FeatureValue> record);
    }

    private class discreteNode extends abstractNode {
        ConcurrentHashMap<Double, Integer> childsMap;

        public discreteNode(String feature, double target) {
            super(feature, target);
            childsMap = new ConcurrentHashMap<>();
        }

        @Override
        public byte[] getBytes() {
            DataOutputStream stream = null;
            try {
                stream = basicInfo();
                stream.writeInt(1);

                if (!isLeaf) {
                    for (Double d : childsMap.keySet()) {
                        stream.writeDouble(d);
                        stream.writeInt(childsMap.get(d));
                    }
                    for (Node n : children) {
                        stream.write(n.getBytes());
                    }
                }
            } catch (IOException ignored) {
                ;
            }

            assert stream != null;
            return bytes.toByteArray();
        }

        @Override
        public void setNodeMap(ArrayList<Double> feature, ArrayList<Node> child) {
            children = child;

            for (int i = 0; i < feature.size(); i++) {
                childsMap.put(feature.get(i), i);
            }
        }

        @Override
        public Node getChild(ArrayList<FeatureValue> record) {
            double v = 0.0;

            for (FeatureValue fv : record) {
                if (Objects.equals(fv.getFeature(), this.feature)) {
                    v = fv.getValueAsDouble();
                    break;
                }
            }

            Integer p = childsMap.get(v);
            if (p != null) {
                return children.get(p);
            } else {
                return children.get(rand.nextInt(children.size()));
            }
        }
    }

    private class continuousNode extends abstractNode {
        double mid;

        public continuousNode(String feature, double target) {
            super(feature, target);
        }

        @Override
        public byte[] getBytes() {
            DataOutputStream stream = null;
            try {
                stream = basicInfo();
                stream.writeInt(0);

                if (!isLeaf) {
                    stream.writeDouble(mid);
//                    //test
//                    byte[] data=bytes.toByteArray();
//                    DataInputStream reader = new DataInputStream(new ByteArrayInputStream(data));
//                    System.out.println(reader.readInt());
//                    System.out.println(reader.readInt());
//                    int fea= reader.readInt();
//                    byte[] buffer = new byte[fea];
//                    reader.read(buffer);
//                    System.out.println(reader.readInt());
//                    System.out.println(new String(buffer)+"here"+reader.readDouble());
//                    //test
                    for (Node n : children) {
                        stream.write(n.getBytes());
                    }
                }
            } catch (IOException ignored) {

            }

            assert bytes != null;
            return bytes.toByteArray();
        }

        @Override
        public void setNodeMap(ArrayList<Double> feature, ArrayList<Node> child) {
            mid = feature.get(0);
            children = child;
        }

        @Override
        public Node getChild(ArrayList<FeatureValue> record) {
            double v = 0.0;

            for (FeatureValue fv : record) {
                if (Objects.equals(fv.getFeature(), this.feature)) {
                    v = fv.getValueAsDouble();
                    break;
                }
            }

            if (v < mid) {
                return children.get(0);
            } else {
                return children.get(1);
            }
        }
    }
}
