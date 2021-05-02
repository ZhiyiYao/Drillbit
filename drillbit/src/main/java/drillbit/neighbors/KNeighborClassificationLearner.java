package drillbit.neighbors;

import drillbit.BaseLearner;
import drillbit.FeatureValue;
import drillbit.neighbors.distance.Distance;
import drillbit.neighbors.distance.DistanceFactory;
import drillbit.neighbors.weight.Weight;
import drillbit.optimizer.LossFunctions;
import drillbit.parameter.Coordinates;
import drillbit.parameter.DenseCoordinates;
import drillbit.protobuf.NeighborsPb;
import drillbit.protobuf.SamplePb;
import drillbit.utils.math.SparseVector;
import drillbit.utils.parser.StringParser;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.annotation.Nonnull;

public final class KNeighborClassificationLearner extends BaseLearner {
    private int n;
    private int nForPredict;

    private String metric;
    private String metricForPredict;
    private Distance distanceMetric;

    private String weight;
    private String weightForPredict;
    private Weight weightFunction;

    private String algorithm;

    private int leafSize;

    private int p;

    private boolean dense;
    private int dims;

    private ArrayList<String> labels;
    private ArrayList<Coordinates> coordinatesList;
    private int nClasses;

    private boolean returnIndex;

    private static final int DEFAULT_N = 5;
    private static final String DEFAULT_METRIC = "euclidean";
    private static final String DEFAULT_WEIGHT = "uniform";
    private static final String DEFAULT_ALGORITHM = "brute";
    private static final int DEFAULT_LEAF_SIZE = 2;
    private static final int DEFAULT_P = 1;

    private int count;

    @Override
    public Options getOptions() {
        Options opts = super.getOptions();

        // Options for KNN algorithm
        opts.addOption("n_neighbors", true, "number of nearest neighbors");
        opts.addOption("metric", true, "function of distance metric");
        opts.addOption("weight", true, "weight function used in prediction");
        opts.addOption("algorithm", true, "algorithm used to compute the nearest neighbors");
        opts.addOption("leaf_size", true, "leaf size passed to BallTree or KDTree");
        opts.addOption("p", true, "power parameter for the Minkowski metric");

        // Options for model storage
        opts.addOption("dims", "feature_dimensions", true, "The dimension of model");
        opts.addOption("dense", "use_dense_model", false, "Use dense model or not");

        return opts;
    }

    public Options getPredictOptions() {
        Options opts = super.getPredictOptions();

        // KNN algorithm
        opts.addOption("n_neighbors", true, "number of nearest neighbors");
        opts.addOption("metric", true, "function of distance metric");
        opts.addOption("weight", true, "weight function used in prediction");

        // Options for output format
        opts.addOption("index", "return_index", false, "return index of label");

        return opts;
    }

    @Override
    public CommandLine processOptions(@Nonnull final CommandLine cl) {
        super.processOptions(cl);

        dense = cl.hasOption("dense");

        if (cl.hasOption("dims")) {
            dims = StringParser.parseInt(cl.getOptionValue("dims"), -1);
        }
        else {
            dims = -1;
        }
        if (dims <= 0) {
            throw new IllegalArgumentException("Dimension of feature not specified");
        }

        if (cl.hasOption("k")) {
            n = StringParser.parseInt(cl.getOptionValue("k"), DEFAULT_N);
        }
        n = n > 0 ? n : DEFAULT_N;

        if (cl.hasOption("metric")) {
            metric = cl.getOptionValue("metric");
        }
        else {
            metric = DEFAULT_METRIC;
        }

        if (cl.hasOption("weight")) {
            weight = cl.getOptionValue("weight");
        }
        else {
            weight = DEFAULT_WEIGHT;
        }

        if (cl.hasOption("algorithm")) {
            algorithm = cl.getOptionValue("algorithm");
        }
        else {
            algorithm = DEFAULT_ALGORITHM;
        }

        if (cl.hasOption("leaf_size")) {
            leafSize = StringParser.parseInt(cl.getOptionValue("leaf_size"), DEFAULT_LEAF_SIZE);
        }
        else {
            leafSize = DEFAULT_LEAF_SIZE;
        }

        if (cl.hasOption("p")) {
            p = StringParser.parseInt(cl.getOptionValue("p"), DEFAULT_P);
        }
        else {
            p = DEFAULT_P;
        }

        labels = new ArrayList<>();
        coordinatesList = new ArrayList<>();

        return cl;
    }

    public CommandLine processPredictOptions(@Nonnull final CommandLine cl) {
        super.processPredictOptions(cl);

        if (cl.hasOption("n")) {
            nForPredict = StringParser.parseInt(cl.getOptionValue("n"), n);
        }
        else {
            nForPredict = n;
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
    public void add(String feature, String target) {
        ArrayList<FeatureValue> featureValues = parseFeatureList(feature);
        writeSampleToTempFile(featureValues, target);
        count++;
    }

    @Override
    public byte[] output(String commandLine) {
        finalizeTraining();
        logger.info("Trained a knn model using " + count + " training examples");
        return toByteArray();
    }

    @Override
    public final void finalizeTraining() {
        try {
            outputStream.close();
        }
        catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

        try {
            for (int i = 0; i < count; i++) {
                SamplePb.Sample sample = readSampleFromTempFile();
                int featureVectorSize = sample.getFeatureList().size();
                String target = sample.getTarget();
                ArrayList<FeatureValue> featureVector = new ArrayList<>();
                for (int j = 0; j < featureVectorSize; j++) {
                    featureVector.add(StringParser.parseFeature(sample.getFeature(j)));
                }

                SparseVector vector = new SparseVector();
                for (FeatureValue featureValue : featureVector) {
                    if (featureValue.getFeatureType() != FeatureValue.FeatureType.NUMERICAL) {
                        throw new IllegalArgumentException();
                    }

                    vector.set(StringParser.parseInt(featureValue.getFeature(), -1), featureValue.getValueAsDouble());
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
            }

            try {
                inputStream.close();
            }
            catch (IOException e) {
                logger.error("temp file close failed.");
                logger.error(e.getMessage(), e);
            }
            finally {
                inputStream = null;
            }
        } catch (Throwable e) {
            throw new IllegalArgumentException("Exception caused in the iterative training", e);
        } finally {
            // delete the temporary file and release resources
            File file = new File(String.valueOf(path));
            file.delete();
        }
    }

    @Override
    public final void train(@Nonnull ArrayList<FeatureValue> featureVector, @Nonnull double target) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final byte[] toByteArray() {
        NeighborsPb.KNeighborsClassifier.Builder builder = NeighborsPb.KNeighborsClassifier.newBuilder();

        builder.setDense(dense);
        builder.setDims(dims);
        builder.setN(n);
        builder.setMetric(metric);

        for (int i = 0; i < labels.size(); i++) {
            NeighborsPb.KNeighborsClassifier.LabelAndCoordinates.Builder labelAndCoordinatesBuilder = NeighborsPb.KNeighborsClassifier.LabelAndCoordinates.newBuilder();
            labelAndCoordinatesBuilder.setLabel(labels.get(i));
            labelAndCoordinatesBuilder.setCoordinates(ByteString.copyFrom(coordinatesList.get(i).toByteArray()));
            builder.addLabel2Coordinates(labelAndCoordinatesBuilder.build());
        }

        return builder.build().toByteArray();
    }

    @Override
    public final BaseLearner fromByteArray(byte[] learnerBytes) throws InvalidProtocolBufferException {
        NeighborsPb.KNeighborsClassifier KnnClassifier = NeighborsPb.KNeighborsClassifier.parseFrom(learnerBytes);

        dense = KnnClassifier.getDense();
        dims = KnnClassifier.getDims();
        metric = KnnClassifier.getMetric();

        labels.clear();
        coordinatesList.clear();
        for (NeighborsPb.KNeighborsClassifier.LabelAndCoordinates labelAndCoordinates : KnnClassifier.getLabel2CoordinatesList()) {
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

    @Nonnull
    @Override
    public final String getLossOptionDescription() {
        throw new UnsupportedOperationException();
    }

    @Override
    public final void update(@Nonnull ArrayList<FeatureValue> features, double target, double predicted) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final Object predict(@Nonnull final String feature, @Nonnull final String options) {
        if (!optionForPredictProcessed) {
            CommandLine cl = parsePredictOptions(options);
            processPredictOptions(cl);
            optionForPredictProcessed = true;
        }

        ArrayList<FeatureValue> featureValues = parseFeatureList(feature);
        SparseVector vector = new SparseVector();
        for (FeatureValue featureValue : featureValues) {
            if (featureValue.getFeatureType() != FeatureValue.FeatureType.NUMERICAL) {
                throw new IllegalArgumentException();
            }

            vector.set(StringParser.parseInt(featureValue.getFeature(), -1), featureValue.getValueAsDouble());
        }

        double[] coordinate = vector.toArray();
        ArrayList<Score> scores = new ArrayList<>();
        ArrayList<Counter> counters = new ArrayList<>();
        if (algorithm.equals("brute")) {
            for (int i = 0; i < nClasses; i++) {
                String label = labels.get(i);
                Coordinates coordinates = coordinatesList.get(i);
                for (int j = 0; j < coordinates.size(); j++) {
                    scores.add(new Score(label, distanceMetric.evaluate(coordinate, coordinates.get(j))));
                }
            }
            scores.sort((s1, s2) -> s1.getDistance() > s2.getDistance() ? 1 : -1);

            for (int i = 0; i < n; i++) {
                String label = scores.get(i).getLabel();
                int index = getCounterIndex(counters, label);

                if (index == -1) {
                    counters.add(new Counter(label));
                }
                else {
                    counters.set(index, counters.get(index).incr());
                }
            }
            counters.sort((c1, c2) -> c1.getCount() < c2.getCount() ? 1 : -1);
        }
        else {
            throw new IllegalArgumentException(String.format("Invalid algorithm of %s.", algorithm));
        }

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
