package drillbit.knn;

import drillbit.BaseLearner;
import drillbit.FeatureValue;
import drillbit.knn.distance.Distance;
import drillbit.knn.distance.DistanceFactory;
import drillbit.optimizer.LossFunctions;
import drillbit.parameter.Coordinates;
import drillbit.parameter.DenseCoordinates;
import drillbit.protobuf.KnnPb;
import drillbit.utils.math.DenseVector;
import drillbit.utils.math.SparseVector;
import drillbit.utils.parser.StringParser;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Objects;

import javax.annotation.Nonnull;

public class KNearestNeighborClassificationLearner extends BaseLearner {
    private int k;
    private int kForPredict;

    private int dims;

    private ArrayList<String> labels;
    private ArrayList<Coordinates> coordinatesList;
    private int nClasses;

    private boolean dense;

    private String metric;
    private String metricForPredict;
    private Distance distanceMetric;

    private boolean returnIndex;

    private static final int DEFAULT_K = 5;

    private static final String DEFAULT_METRIC = "euclidean";

    private int count;

    @Override
    public Options getOptions() {
        Options opts = super.getOptions();

        opts.addOption("k", "k_nearest", true, "number of nearest points");
        opts.addOption("metric", "distance_metric", true, "function of distance metric");
        opts.addOption("dims", "feature_dimensions", true, "The dimension of model");
        opts.addOption("dense", "use_dense_model", false, "Use dense model or not");

        return opts;
    }

    public Options getPredictOptions() {
        Options opts = super.getPredictOptions();

        opts.addOption("k", "k_nearest", true, "number of nearest points");
        opts.addOption("metric", "distance_metric", true, "function of distance metric");
        opts.addOption("index", "return_index", false, "return index of label");

        return opts;
    }

    @Override
    public CommandLine processOptions(@Nonnull final CommandLine cl) {
        super.processOptions(cl);

        if (cl.hasOption("k")) {
            k = StringParser.parseInt(cl.getOptionValue("k"), DEFAULT_K);
        }
        k = k > 0 ? k : DEFAULT_K;

        if (cl.hasOption("metric")) {
            metric = cl.getOptionValue("metric");
        }
        else {
            metric = DEFAULT_METRIC;
        }

        if (cl.hasOption("dims")) {
            dims = StringParser.parseInt(cl.getOptionValue("dims"), -1);
        }
        else {
            dims = -1;
        }

        if (dims <= 0) {
            throw new IllegalArgumentException("Dimension of feature not specified");
        }

        labels = new ArrayList<>();
        coordinatesList = new ArrayList<>();

        dense = cl.hasOption("dense");

        return cl;
    }

    public CommandLine processPredictOptions(@Nonnull final CommandLine cl) {
        super.processPredictOptions(cl);

        if (cl.hasOption("k")) {
            kForPredict = StringParser.parseInt(cl.getOptionValue("k"), k);
        }
        else {
            kForPredict = k;
        }

        if (cl.hasOption("metric")) {
            metricForPredict = cl.getOptionValue("metric");
        }
        else {
            metricForPredict = metric;
        }

        try {
            distanceMetric = DistanceFactory.getDistance(metricForPredict);
        } catch (Exception e) {
            logger.error(e);
            throw new IllegalArgumentException(e.getMessage());
        }


        returnIndex = cl.hasOption("index");

        return cl;
    }

    @Override
    public void add(String feature, String target, String commandLine) {
        if (!optionProcessed) {
            count = 0;
            // parse commandline value here.
            CommandLine cl = parseOptions(commandLine);
            processOptions(cl);
            optionProcessed = true;
        }

        SparseVector vector = new SparseVector();
        for (String featureValue : Objects.requireNonNull(StringParser.parseArray(feature))) {
            if (StringParser.parseFeature(featureValue).getFeatureType() != FeatureValue.FeatureType.NUMERICAL) {
                throw new IllegalArgumentException();
            }

            FeatureValue temp = StringParser.parseFeature(featureValue);
            vector.set(StringParser.parseInt(temp.getFeature(), -1), temp.getValueAsDouble());
        }

        int index = getLabelIndex(target);
        if (index == -1) {
            DenseCoordinates coordinates = new DenseCoordinates(dims);
            coordinates.add(vector.toArray());

            labels.add(target);
            coordinatesList.add(coordinates);
            nClasses++;
        }
        else {
            DenseCoordinates coordinates = (DenseCoordinates) coordinatesList.get(index);
            coordinates.add(vector.toArray());
            coordinatesList.set(index, coordinates);
        }

        count++;
    }

    @Override
    public void add(@Nonnull String feature, @Nonnull String target) {
        add(feature, target, "");
    }

    @Override
    public byte[] output() {
        logger.info("Trained a knn model using " + count + " training examples");
        return toByteArray();
    }

    @Override
    public final void train(@NotNull ArrayList<FeatureValue> featureVector, @NotNull double target) {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte[] toByteArray() {
        KnnPb.KNNClassifier.Builder builder = KnnPb.KNNClassifier.newBuilder();

        builder.setDense(dense);
        builder.setDims(dims);
        builder.setMetric(metric);

        for (int i = 0; i < labels.size(); i++) {
            KnnPb.KNNClassifier.LabelAndCoordinates.Builder labelAndCoordinatesBuilder = KnnPb.KNNClassifier.LabelAndCoordinates.newBuilder();
            labelAndCoordinatesBuilder.setLabel(labels.get(i));
            labelAndCoordinatesBuilder.setCoordinates(ByteString.copyFrom(coordinatesList.get(i).toByteArray()));
            builder.addLabel2Coordinates(labelAndCoordinatesBuilder.build());
        }

        return builder.build().toByteArray();
    }

    @Override
    public BaseLearner fromByteArray(byte[] learnerBytes) throws InvalidProtocolBufferException {
        KnnPb.KNNClassifier KnnClassifier = KnnPb.KNNClassifier.parseFrom(learnerBytes);

        dense = KnnClassifier.getDense();
        dims = KnnClassifier.getDims();
        metric = KnnClassifier.getMetric();

        labels.clear();
        coordinatesList.clear();
        for (KnnPb.KNNClassifier.LabelAndCoordinates labelAndCoordinates : KnnClassifier.getLabel2CoordinatesList()) {
            labels.add(labelAndCoordinates.getLabel());
            coordinatesList.add((new DenseCoordinates(dims)).fromByteArray(labelAndCoordinates.getCoordinates().toByteArray()));
        }

        return this;
    }

    @Override
    public final void checkTargetValue(String target) throws IllegalArgumentException {
        throw new UnsupportedOperationException();
    }

    @Override
    public final void checkLossFunction(LossFunctions.LossFunction lossFunction) throws IllegalArgumentException {
        throw new UnsupportedOperationException();
    }

    @Override
    public final LossFunctions.LossType getDefaultLossType() {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public final String getLossOptionDescription() {
        throw new UnsupportedOperationException();
    }

    @Override
    public final void update(@NotNull ArrayList<FeatureValue> features, double target, double predicted) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object predict(@Nonnull final String feature, @Nonnull final String options) {
        if (!optionForPredictProcessed) {
            CommandLine cl = parsePredictOptions(options);
            processPredictOptions(cl);
            optionForPredictProcessed = true;
        }

        DenseVector vector = new DenseVector(dims);
        for (String featureValue : Objects.requireNonNull(StringParser.parseArray(feature))) {
            if (StringParser.parseFeature(featureValue).getFeatureType() != FeatureValue.FeatureType.NUMERICAL) {
                throw new IllegalArgumentException();
            }

            FeatureValue temp = StringParser.parseFeature(featureValue);
            vector.set(StringParser.parseInt(temp.getFeature(), -1), temp.getValueAsDouble());
        }

        double[] coordinate = vector.toArray();
        ArrayList<Score> scores = new ArrayList<>();
        for (int i = 0; i < nClasses; i++) {
            String label = labels.get(i);
            Coordinates coordinates = coordinatesList.get(i);
            for (int j = 0; j < coordinates.size(); j++) {
                scores.add(new Score(label, distanceMetric.evaluate(coordinate, coordinates.get(j))));
            }
        }
        scores.sort((s1, s2) -> s1.getDistance() < s2.getDistance() ? 1 : 0);

        ArrayList<Counter> counters = new ArrayList<>();
        for (int i = 0; i < k; i++) {
            String label = scores.get(i).getLabel();
            int index = getCounterIndex(counters, label);

            if (index == -1) {
                counters.add(new Counter(label));
            }
            else {
                counters.set(index, counters.get(index).incr());
            }
        }
        counters.sort((c1, c2) -> c1.getCount() > c2.getCount() ? 1 : 0);

        String label = counters.get(0).getLabel();
        if (returnIndex) {
            return getLabelIndex(label);
        }
        else {
            return label;
        }
    }

    private int getLabelIndex(String label) {
        for (int i = 0; i < labels.size(); i++) {
            if (label.equals(labels.get(i))) {
                return i;
            }
        }

        return -1;
    }

    private int getCounterIndex(ArrayList<Counter> counters, String label) {
        for (int i = 0; i < counters.size(); i++) {
            if (counters.get(i).getLabel().equals(label)) {
                return i;
            }
        }

        return -1;
    }

    private static class Score {
        private String label;
        private double distance;

        public Score(String label, double distance) {
            this.label = label;
            this.distance = distance;
        }

        public Score setLabel(String label) {
            this.label = label;
            return this;
        }

        public Score setDistance(double distance) {
            this.distance = distance;
            return this;
        }

        public String getLabel() {
            return label;
        }

        public double getDistance() {
            return distance;
        }
    }

    private static class Counter {
        private final String label;
        private int count;

        public Counter(String label) {
            this.label = label;
            this.count = 0;
        }

        public int getCount() {
            return count;
        }

        public String getLabel() {
            return label;
        }

        public Counter incr() {
            count++;
            return this;
        }
    }
}
