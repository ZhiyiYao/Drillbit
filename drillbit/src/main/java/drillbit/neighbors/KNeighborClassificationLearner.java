package drillbit.neighbors;

import drillbit.BaseLearner;
import drillbit.FeatureValue;
import drillbit.neighbors.distance.Distance;
import drillbit.neighbors.distance.DistanceFactory;
import drillbit.neighbors.solver.Solver;
import drillbit.neighbors.solver.SolverFactory;
import drillbit.neighbors.solver.SolverOptions;
import drillbit.neighbors.weight.Weight;
import drillbit.optimizer.LossFunctions;
import drillbit.optimizer.OptimizerOptions;
import drillbit.optimizer.Optimizers;
import drillbit.parameter.Coordinates;
import drillbit.parameter.DenseCoordinates;
import drillbit.protobuf.NeighborsPb;
import drillbit.protobuf.SamplePb;
import drillbit.utils.math.DenseVector;
import drillbit.utils.math.SparseVector;
import drillbit.utils.math.Vector;
import drillbit.utils.parser.StringParser;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import org.apache.commons.cli.ParseException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nonnull;

public final class KNeighborClassificationLearner extends BaseLearner {
    private int n;

    private String metric;
    private Distance distanceMetric;

    private String weight;
    private Weight weightFunction;

    private ConcurrentHashMap<String, String> solverOptions;
    private Solver solver;

    private boolean dense;
    private int dims;

    private ArrayList<String> labels;
    private ArrayList<Coordinates> coordinatesList;
    private int nClasses;

    private boolean returnIndex;

    private static final int DEFAULT_N = 7;
    private static final String DEFAULT_METRIC = "euclidean";
    private static final String DEFAULT_WEIGHT = "uniform";

    @Override
    public CommandLine parseOptions(String optionValue) {
        String[] args = optionValue.split("\\s+");
        Options opts = getOptions();
        SolverOptions.setup(opts);
        opts.addOption("help", false, "Show function help");

        final CommandLine cl;
        try {
            DefaultParser parser = new DefaultParser();
            cl = parser.parse(opts, args);
        } catch (IllegalArgumentException | ParseException e) {
            throw new IllegalArgumentException(e);
        }

        if (cl.hasOption("help")) {
            this.showHelp(opts);
        }

        return cl;
    }

    @Override
    public Options getOptions() {
        Options opts = super.getOptions();

        // Options for KNN algorithm
        opts.addOption("n_neighbors", true, "number of nearest neighbors");
        opts.addOption("metric", true, "function of distance metric");
        opts.addOption("weight", true, "weight function used in prediction");
        opts.addOption("algorithm", true, "algorithm used to compute the nearest neighbors");

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
        opts.addOption("return_index", false, "return index of label");

        return opts;
    }

    @Override
    public CommandLine processOptions(@Nonnull final CommandLine cl) {
        super.processOptions(cl);

        dense = cl.hasOption("dense");

        dims = StringParser.parseInt(cl.getOptionValue("dims"), -1);
        if (dims <= 0) {
            throw new IllegalArgumentException("Dimension of feature not specified");
        }

        n = StringParser.parseInt(cl.getOptionValue("n"), DEFAULT_N);
        n = n > 0 ? n : DEFAULT_N;

        metric = cl.getOptionValue("metric", DEFAULT_METRIC);

        weight = cl.getOptionValue("weight", DEFAULT_WEIGHT);

        labels = new ArrayList<>();
        coordinatesList = new ArrayList<>();

        return cl;
    }

    public CommandLine processPredictOptions(@Nonnull final CommandLine cl) {
        super.processPredictOptions(cl);

        n = StringParser.parseInt(cl.getOptionValue("n_neighbors"), n);
        n = n > 0 ? n : DEFAULT_N;

        metric = cl.getOptionValue("metric", metric);
        try {
            distanceMetric = DistanceFactory.getDistance(metric);
        }
        catch (Exception e) {
            logger.error(e);
            throw new IllegalArgumentException(e.getMessage());
        }

        solverOptions = SolverOptions.create();
        SolverOptions.processOptions(cl, solverOptions);
        solver = SolverFactory.create(solverOptions);
        solver.build(labels, coordinatesList);

        returnIndex = cl.hasOption("return_index");

        return cl;
    }

    @Override
    public void add(@Nonnull String feature, @Nonnull String target) {
        ArrayList<FeatureValue> featureValues = parseFeatureList(feature);
        writeSampleToTempFile(featureValues, target);
    }

    @Override
    public byte[] output(@Nonnull String commandLine) {
        if (!optionProcessed) {
            CommandLine cl = parseOptions(commandLine);
            processOptions(cl);
            optionProcessed = true;
        }
        finalizeTraining();
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
            while (true) {
                SamplePb.Sample sample = readSampleFromTempFile();
                if (sample == null) {
                    break;
                }
                int featureVectorSize = sample.getFeatureList().size();
                String target = sample.getTarget();
                ArrayList<FeatureValue> featureVector = new ArrayList<>();
                for (int i = 0; i < featureVectorSize; i++) {
                    featureVector.add(StringParser.parseFeature(sample.getFeature(i)));
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

        labels = new ArrayList<>();
        coordinatesList = new ArrayList<>();
        for (NeighborsPb.KNeighborsClassifier.LabelAndCoordinates labelAndCoordinates : KnnClassifier.getLabel2CoordinatesList()) {
            labels.add(labelAndCoordinates.getLabel());
            coordinatesList.add((new DenseCoordinates(dims)).fromByteArray(labelAndCoordinates.getCoordinates().toByteArray()));
        }
        nClasses = labels.size();

        return this;
    }

    @Override
    public final void checkTargetValue(String target) throws IllegalArgumentException {
        // Do nothing.
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
        Vector vector;
        if (dense) {
            vector = new DenseVector(dims);
        }
        else {
            vector = new SparseVector(dims);
        }
        for (FeatureValue featureValue : featureValues) {
            if (featureValue.getFeatureType() != FeatureValue.FeatureType.NUMERICAL) {
                throw new IllegalArgumentException();
            }

            vector.set(StringParser.parseInt(featureValue.getFeature(), -1), featureValue.getValueAsDouble());
        }

        if (returnIndex) {
            return solver.solveIndex(n, distanceMetric, vector.toArray());
        }
        else {
            return solver.solveLabel(n, distanceMetric, vector.toArray());
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
}
