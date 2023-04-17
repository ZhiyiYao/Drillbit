package drillbit.neighbors;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import drillbit.BaseLearner;
import drillbit.FeatureValue;
import drillbit.neighbors.distance.Distance;
import drillbit.neighbors.distance.DistanceFactory;
import drillbit.neighbors.distance.DistanceOptions;
import drillbit.neighbors.solver.Solver;
import drillbit.neighbors.solver.SolverFactory;
import drillbit.neighbors.solver.SolverOptions;
import drillbit.neighbors.weight.Weight;
import drillbit.neighbors.weight.WeightFactory;
import drillbit.neighbors.weight.WeightOptions;
import drillbit.optimizer.LossFunctions;
import drillbit.parameter.Coordinates;
import drillbit.parameter.DenseCoordinates;
import drillbit.protobuf.NeighborsPb;
import drillbit.utils.math.SparseVector;
import drillbit.utils.parser.StringParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public final class KNeighborsClassificationLearner extends BaseLearner {
    private static final int DEFAULT_K = 3;
    private static final String DEFAULT_WEIGHT = "uniform";
    private int k;
    private ConcurrentHashMap<String, String> distanceOptions;
    private Distance distance;
    private ConcurrentHashMap<String, String> weightOptions;
    private Weight weight;
    private ConcurrentHashMap<String, String> solverOptions;
    private Solver solver;
    private boolean dense;
    private int dims;
    private ArrayList<String> labels;
    private ArrayList<Coordinates> coordinatesList;
    private int nClasses;
    private boolean returnIndex;

    @Override
    public CommandLine parseOptions(String optionValue) {
        String[] args = optionValue.split("\\s+");
        Options opts = getOptions();

        SolverOptions.setup(opts);
        DistanceOptions.setup(opts);
        WeightOptions.setup(opts);

        opts.addOption("help", false, "Show function help");

        CommandLine cl;
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
        opts.addOption("k", true, "number of nearest neighbors");

        // Options for model storage
        opts.addOption("dims", true, "The dimension of model");
        opts.addOption("dense", false, "Use dense model or not");

        return opts;
    }

    public Options getPredictOptions() {
        Options opts = super.getPredictOptions();

        // KNN algorithm
        opts.addOption("k", true, "number of nearest neighbors");

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

        k = StringParser.parseInt(cl.getOptionValue("k"), DEFAULT_K);
        k = k > 0 ? k : DEFAULT_K;

        distanceOptions = DistanceOptions.create();
        DistanceOptions.processOptions(cl, distanceOptions);
        distance = DistanceFactory.create(distanceOptions);

        weightOptions = WeightOptions.create();
        WeightOptions.processOptions(cl, weightOptions);
        weight = WeightFactory.create(weightOptions);

        solverOptions = SolverOptions.create();
        SolverOptions.processOptions(cl, solverOptions);

        labels = new ArrayList<>();
        coordinatesList = new ArrayList<>();

        return cl;
    }

    public CommandLine processPredictOptions(@Nonnull final CommandLine cl) {
        super.processPredictOptions(cl);

        k = StringParser.parseInt(cl.getOptionValue("n_neighbors"), k);
        k = k > 0 ? k : DEFAULT_K;

        DistanceOptions.processOptions(cl, distanceOptions);
        distance = DistanceFactory.create(distanceOptions);

        WeightOptions.processOptions(cl, weightOptions);
        weight = WeightFactory.create(weightOptions);

        SolverOptions.processOptions(cl, solverOptions);
        solver = SolverFactory.create(solverOptions);
        solver.build(labels, coordinatesList);

        returnIndex = cl.hasOption("return_index");

        return cl;
    }

    @Override
    public void add(@Nonnull String feature, @Nonnull String target) {
        ArrayList<FeatureValue> featureValues = parseFeatureList(feature);
        writeSample(featureValues, target);
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
        if (onDisk) {
            try {
                outputStream.close();
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }

        try {
            while (true) {
                OneSample sample = readSample();
                if (sample == null) {
                    break;
                }
                SparseVector vector = new SparseVector();
                for (FeatureValue featureValue : sample.featureValueVector) {
                    if (featureValue.getFeatureType() != FeatureValue.FeatureType.NUMERICAL) {
                        throw new IllegalArgumentException();
                    }

                    vector.set(StringParser.parseInt(featureValue.getFeature(), -1), featureValue.getValueAsDouble());
                }

                int index = getLabelIndex(sample.target);
                if (index == -1) {
                    DenseCoordinates coordinates = new DenseCoordinates(dims);
                    coordinates.add(vector.toArray());
                    labels.add(sample.target);
                    coordinatesList.add(coordinates);
                    nClasses++;
                } else {
                    DenseCoordinates coordinates = (DenseCoordinates) coordinatesList.get(index);
                    coordinates.add(vector.toArray());
                    coordinatesList.set(index, coordinates);
                }
            }

            epochReadCleanUp();
        } catch (Throwable e) {
            throw new IllegalArgumentException("Exception caused in the iterative training", e);
        } finally {
            readCleanUp();
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
        builder.setK(k);
        builder.setDistanceOptions(DistanceOptions.optionsToString(distanceOptions));
        builder.setSolverOptions(SolverOptions.optionsToString(solverOptions));

        for (int i = 0; i < nClasses; i++) {
            NeighborsPb.KNeighborsClassifier.LabelAndCoordinates.Builder labelAndCoordinatesBuilder = NeighborsPb.KNeighborsClassifier.LabelAndCoordinates.newBuilder();
            labelAndCoordinatesBuilder.setLabel(labels.get(i));
            labelAndCoordinatesBuilder.setCoordinates(ByteString.copyFrom(Objects.requireNonNull(coordinatesList.get(i).toByteArray())));
            builder.addLabel2Coordinates(labelAndCoordinatesBuilder.build());
        }

        return builder.build().toByteArray();
    }

    @Override
    public final BaseLearner fromByteArray(byte[] learnerBytes) throws InvalidProtocolBufferException {
        NeighborsPb.KNeighborsClassifier KnnClassifier = NeighborsPb.KNeighborsClassifier.parseFrom(learnerBytes);

        dense = KnnClassifier.getDense();
        dims = KnnClassifier.getDims();

        distanceOptions = DistanceOptions.parse(KnnClassifier.getDistanceOptions());
        distance = DistanceFactory.create(distanceOptions);

        weightOptions = WeightOptions.parse(KnnClassifier.getWeightOptions());
        weight = WeightFactory.create(weightOptions);

        solverOptions = SolverOptions.parse(KnnClassifier.getSolverOptions());
        solver = SolverFactory.create(solverOptions);

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
        SparseVector vector = new SparseVector(dims);
        for (FeatureValue featureValue : featureValues) {
            if (featureValue.getFeatureType() != FeatureValue.FeatureType.NUMERICAL) {
                throw new IllegalArgumentException();
            }

            vector.set(StringParser.parseInt(featureValue.getFeature(), -1), featureValue.getValueAsDouble());
        }

        if (returnIndex) {
            return Integer.toString(solver.solveIndex(k, distance, weight, vector.toArray()));
        } else {
            return solver.solveLabel(k, distance, weight, vector.toArray());
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
