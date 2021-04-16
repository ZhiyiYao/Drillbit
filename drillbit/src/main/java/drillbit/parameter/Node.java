package drillbit.parameter;

import com.google.protobuf.InvalidProtocolBufferException;
import drillbit.utils.math.DenseVector;
import drillbit.utils.math.MathUtils;
import drillbit.protobuf.ParameterPb;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public final class Node {
        // Predicted class label for this node.
        public int output = -1;

        // A posteriori probability based on sample ratios in this node.
        @Nullable
        public double[] posteriori = null;

        // The split feature for this node.
        public int feature = -1;

        // The type of split feature
        public boolean numerical = true;

        // The split value.
        public double value = Double.NaN;

        // Reduction in splitting criterion.
        public double score = 0.0;

        public Node trueChild = null;

        public Node falseChild = null;

        public Node() {}

        public Node(@Nonnull double[] posteriori) {
            this.output = MathUtils.whichMax(posteriori);
            this.posteriori = posteriori;
        }

        public Node(int output, @Nonnull double[] posteriori) {
            this.output = output;
            this.posteriori = posteriori;
        }

        public boolean isLeafNode() {
            return trueChild == null && falseChild == null;
        }

        public void markAsLeafNode() {
            this.feature = -1;
            this.value = Double.NaN;
            this.score = 0.0;
            this.trueChild = null;
            this.falseChild = null;
        }

        public int predict(@Nonnull final double[] x) {
            return predict(new DenseVector(x));
        }

        public int predict(@Nonnull final DenseVector x) {
            if (isLeafNode()) {
                return output;
            } else {
                if (numerical) {
                    if (x.get(feature, Double.NaN) <= value) {
                        return trueChild.predict(x);
                    } else {
                        return falseChild.predict(x);
                    }
                } else {
                    if (x.get(feature, Double.NaN) == value) {
                        return trueChild.predict(x);
                    } else {
                        return falseChild.predict(x);
                    }
                }
            }
        }

        public byte[] toByteArray() {
            return build().toByteArray();
        }

        public Node fromByteArray(@Nonnull final byte[] byteArray) throws InvalidProtocolBufferException {
            ParameterPb.Node node = null;
            try {
                node = ParameterPb.Node.parseFrom(byteArray);
            }
            catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
                throw e;
            }

            return parse(node);
        }

        private ParameterPb.Node build() {
            ParameterPb.Node.Builder builder = ParameterPb.Node.newBuilder();
            builder.setOutput(output);

            assert posteriori != null;
            for (double value : posteriori) {
                builder.addPosteriori(value);
            }

            builder.setFeature(feature);
            builder.setNumerical(numerical);
            builder.setValue(value);

            if (trueChild != null) {
                builder.setTrueChild(trueChild.build());
            }

            if (falseChild != null) {
                builder.setFalseChild(falseChild.build());
            }

            return builder.build();
        }

        private Node parse(@Nonnull final ParameterPb.Node node) {
            output = node.getOutput();

            List<Double> posterioriList = node.getPosterioriList();
            posteriori = new double[posterioriList.size()];
            for (int i = 0; i < posterioriList.size(); i++) {
                posteriori[i] = posterioriList.get(i);
            }

            feature = node.getFeature();
            numerical = node.getNumerical();
            value = node.getValue();

            if (node.hasTrueChild()) {
                trueChild = (new Node()).parse(node.getTrueChild());
            }
            else {
                trueChild = null;
            }

            if (node.hasFalseChild()) {
                falseChild = (new Node()).parse(node.getFalseChild());
            }
            else {
                falseChild = null;
            }

            return this;
        }
   }