package drillbit;

import com.google.protobuf.InvalidProtocolBufferException;
import drillbit.neighbors.distance.DistanceOptions;
import drillbit.neighbors.solver.SolverOptions;
import drillbit.neighbors.weight.WeightOptions;
import drillbit.optimizer.*;
import drillbit.parameter.DenseWeights;
import drillbit.parameter.SparseWeights;
import drillbit.parameter.Weights;
import drillbit.protobuf.SamplePb;
import drillbit.utils.lang.SizeOf;
import drillbit.utils.parser.StringParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/*
    Base learner for general regression learner and general classification learner.
    Does not contain a model or any options.
    Derived class should decide their form of model.
 */
public abstract class BaseLearner implements Learner {
    protected static final Log logger = LogFactory.getLog(BaseLearner.class);

    // Default and extreme values
    protected static final int DEFAULT_DENSE_DIMS = 32767;
    protected static final int DEFAULT_SPARSE_DIMS = 16384; // half of 32768
    protected static final double MAX_DLOSS = 1e+12d;
    protected static final double MIN_DLOSS = -1e+12d;

    // Process options at first call of output method of training Aggregate UDF
    protected boolean optionProcessed;

    // Process predict options at first call of add method of predicting Simple UDF
    protected boolean optionForPredictProcessed;

    // For read and write of samples
    protected boolean onDisk;

    // For on disk setup
    protected Path path;
    protected FileSystem fs;
    protected FSDataOutputStream outputStream;
    protected FSDataInputStream inputStream;
    // For in memory setup
    protected ArrayList<ArrayList<FeatureValue>> featureValueVectors;
    protected ArrayList<String> targets;
    SamplePb.Sample.Builder builder;
    int index = 0;

    public BaseLearner() {
        optionProcessed = false;
        optionForPredictProcessed = false;
        path = null;

        featureValueVectors = new ArrayList<>();
        targets = new ArrayList<>();
    }

    @Override
    public Options getOptions() {
        Options opts = new Options();
        opts.addOption("on_disk", false, "Store data on disk or not");

        return opts;
    }

    @Override
    public Options getPredictOptions() {
        return new Options();
    }

    @Override
    public CommandLine parseOptions(String optionValue) {
        String[] args = optionValue.split("\\s+");
        Options opts = getOptions();
        OptimizerOptions.setup(opts);
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
    public CommandLine parsePredictOptions(String optionValue) {
        String[] args = optionValue.split("\\s+");
        Options opts = getPredictOptions();
        SolverOptions.setup(opts);
        DistanceOptions.setup(opts);
        WeightOptions.setup(opts);
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
    public void showHelp(@Nonnull Options opts) {
        //TODO: implement show help method
    }

    /*
     * process the options. Reused by subclasses
     */
    @Override
    public CommandLine processOptions(@Nonnull final CommandLine cl) throws IllegalArgumentException {
        onDisk = cl.hasOption("on_disk");

        return cl;
    }

    /*
     * process prediction options.
     */
    @Override
    public CommandLine processPredictOptions(@Nonnull final CommandLine cl) throws IllegalArgumentException {
        return cl;
    }

    @Override
    public byte[] output(@Nonnull final String commandLine) {
        if (!optionProcessed) {
            CommandLine cl = parseOptions(commandLine);
            processOptions(cl);
            optionProcessed = true;
        }
        finalizeTraining();
        return toByteArray();
    }

    @Override
    public void reset() {
        this.optionProcessed = false;
    }

    protected abstract void train(@Nonnull final ArrayList<FeatureValue> featureVector, @Nonnull final double target);

    protected ArrayList<FeatureValue> parseFeatureList(String feature) {
        ArrayList<String> featureValues = StringParser.parseArray(feature);
        ArrayList<FeatureValue> featureVector = new ArrayList<>();
        assert featureValues != null;
        for (String featureValue : featureValues) {
            featureVector.add(StringParser.parseFeature(featureValue));
        }

        return featureVector;
    }

    @Nonnull
    protected final Optimizers.OptimizerBase createOptimizer(@CheckForNull ConcurrentHashMap<String, String> options, boolean dense, int dims) {
        assert options != null;
        if (dense) {
            return DenseOptimizerFactory.create(dims, options);
        } else {
            return SparseOptimizerFactory.create(dims, options);
        }
    }

    @Nonnull
    protected Weights createWeights(boolean dense, int dims) {
        Weights weights;
        if (dense) {
            logger.info(String.format("Build a dense model with initial with %d initial dimensions", dims));
            weights = new DenseWeights(dims, TrainWeights.WeightType.WithCovar);
        } else {
            logger.info(String.format("Build a sparse model with initial with %d initial dimensions", dims));
            weights = new SparseWeights(dims, TrainWeights.WeightType.WithCovar);
        }
        return weights;
    }

    private void writeSampleToTempFile(@Nonnull final ArrayList<FeatureValue> featureVector, @Nonnull final String target) {
        if (fs == null) {
            Configuration conf = new Configuration();
            try {
                fs = FileSystem.get(conf);
                path = new Path(".temp/drillbit_temp_file.pb");
                outputStream = fs.create(path);
            } catch (IOException e) {
                throw new IllegalArgumentException(e.getMessage());
            }
        }

        SamplePb.Sample.Builder builder = SamplePb.Sample.newBuilder();
        for (FeatureValue f : featureVector) {
            if (f == null) {
                continue;
            }
            builder.addFeature(f.toString());
        }

        builder.setTarget(target);
        SamplePb.Sample sample = builder.build();
        byte[] sampleByte = ByteBuffer.allocate(SizeOf.INT).putInt(sample.getSerializedSize()).array();
        // TODO: complete the synchronize mechanism of writing to temp file in both local filesystem and distributed filesystem
        synchronized (outputStream) {
            try {
                outputStream.write(sampleByte, 0, sampleByte.length);
                outputStream.write(sample.toByteArray(), 0, sample.toByteArray().length);
            } catch (IOException e) {
                logger.error("temp file write failed.");
                logger.error(e.getMessage(), e);
            }
        }
        builder.clear();
    }

    private void writeSampleToMemory(@Nonnull final ArrayList<FeatureValue> featureVector, @Nonnull final String target) {
        featureValueVectors.add(featureVector);
        targets.add(target);
    }

    protected void writeSample(@Nonnull final ArrayList<FeatureValue> featureVector, @Nonnull final String target) {
        if (onDisk) {
            writeSampleToTempFile(featureVector, target);
        } else {
            writeSampleToMemory(featureVector, target);
        }
    }

    private OneSample readSampleFromTempFile() {
        if (inputStream == null) {
            try {
                inputStream = fs.open(path);
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }

        byte[] intByte = new byte[SizeOf.INT];
        try {
            int nBytesRead = inputStream.read(intByte);
            if (nBytesRead == -1) {
                // InputStream meets the end of file.
                return null;
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

        int OneSampleBytes = ByteBuffer.wrap(intByte).getInt();
        byte[] OneSampleByte = new byte[OneSampleBytes];
        try {
            int nBytesRead = inputStream.read(OneSampleByte);
            if (nBytesRead == -1) {
                // InputStream meets the end of file.
                return null;
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

        SamplePb.Sample oneSample = null;
        if (builder == null) {
            builder = SamplePb.Sample.newBuilder();
        }
        builder.clear();

        try {
            oneSample = builder.mergeFrom(OneSampleByte).build();
        } catch (InvalidProtocolBufferException e) {
            logger.error(e.getMessage(), e);
        }

        assert oneSample != null;

        OneSample result = new OneSample();
        int featureVectorSize = oneSample.getFeatureList().size();
        result.target = oneSample.getTarget();
        result.featureValueVector = new ArrayList<>(featureVectorSize);
        for (int i = 0; i < featureVectorSize; i++) {
            result.featureValueVector.add(StringParser.parseFeature(oneSample.getFeature(i)));
        }

        return result;
    }

    private OneSample readSampleFromMemory() {
        if (index >= featureValueVectors.size()) {
            return null;
        }
        OneSample oneSample = new OneSample();
        oneSample.target = targets.get(index);
        oneSample.featureValueVector = featureValueVectors.get(index);
        index++;

        return oneSample;
    }

    private boolean memorySampleHasNext() {
        return index < featureValueVectors.size();
    }

    private boolean tempFIleSampleHasNext() {
        if (inputStream == null) {
            try {
                inputStream = fs.open(path);
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }

        try {
            int nBytesRead = inputStream.available();
            if (nBytesRead == 0) {
                // InputStream meets the end of file.
                return false;
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

        return true;
    }

    protected boolean sampleHasNext() {
        if (onDisk) {
            return tempFIleSampleHasNext();
        } else {
            return memorySampleHasNext();
        }
    }

    protected OneSample readSample() {
        if (onDisk) {
            return readSampleFromTempFile();
        } else {
            return readSampleFromMemory();
        }
    }

    protected void epochReadCleanUp() {
        if (onDisk) {
            try {
                inputStream.close();
            } catch (IOException e) {
                logger.error("temp file close failed.");
                logger.error(e.getMessage(), e);
            } finally {
                inputStream = null;
            }
        } else {
            index = 0;
        }
    }

    protected void writeCleanUp() {
        if (onDisk) {
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    protected void readCleanUp() {
        if (onDisk) {
            // delete the temporary file and release resources
            File file = new File(String.valueOf(path));
            file.delete();
        } else {
            featureValueVectors.clear();
            targets.clear();
        }
    }

    protected @Nonnull
    ArrayList<OneSample> readBatch(int batch) {
        ArrayList<OneSample> result = new ArrayList<>();
        for (int i = 0; i < batch; i++) {
            result.add(readSample());
        }

        return result;
    }

//    protected void runIterativeTraining(@Nonnull Model model, boolean isMiniBatch, @Nonnegative final int iterations, @Nonnegative final long count, @Nonnull final ConversionState cvState) {
//        OneSamplePb.OneSample oneSample = null;
//        ArrayList<FeatureValue> featureValueVector;
//        double target;
//
//        try {
//            outputStream.close();
//        }
//        catch (IOException e) {
//            logger.error(e.getMessage(), e);
//        }
//
//        try {
//            for (int iter = 2; iter <= iterations; iter++) {
//                for (int i = 0; i < count; i++) {
//                    cvState.next();
//
//                    oneSample = readOneSampleFromTempFile();
//                    int featureVectorSize = oneSample.getFeatureList().size();
//                    target = oneSample.getTarget();
//                    featureValueVector = new ArrayList<>(featureVectorSize);
//                    for (int j = 0; j < featureVectorSize; j++) {
//                        featureValueVector.add(StringParser.parseFeature(oneSample.getFeature(j)));
//                    }
//                    train(featureValueVector, target);
//                }
//
//                if (isMiniBatch) {
//                    batchUpdate();
//                }
//
//                try {
//                    inputStream.close();
//                }
//                catch (IOException e) {
//                    logger.error("temp file close failed.");
//                    logger.error(e.getMessage(), e);
//                }
//                finally {
//                    inputStream = null;
//                }
//            }
//        } catch (Throwable e) {
//            throw new IllegalArgumentException("Exception caused in the iterative training", e);
//        } finally {
//            // delete the temporary file and release resources
//            File file = new File("drillbit_input_samples.data_pb");
//            file.delete();
//        }
//    }

    public abstract Object predict(@Nonnull final String features, @Nonnull final String options);

    protected abstract void update(@Nonnull final ArrayList<FeatureValue> features, final double target, final double predicted);

//    protected void onlineUpdate(@Nonnull final ArrayList<FeatureValue> features, final double loss, final double dloss) {
//        for (FeatureValue f : features) {
//            Object feature = f.getFeature();
//            double xi = f.getValueAsDouble();
//            double weight = model.getWeight(feature);
//            double gradient = dloss * xi;
//            final double new_weight = optimizer.update(feature, weight, loss, gradient);
//            if (new_weight == 0.f) {
//                model.delete(feature);
//                continue;
//            }
//            model.setWeight(feature, new_weight);
//        }
//    }

//    protected void accumulateUpdate(@Nonnull final ArrayList<FeatureValue> features, final double loss, final double dloss) {
//        for (FeatureValue f : features) {
//            Object feature = f.getFeature();
//            double xi = f.getValueAsDouble();
//            double weight = model.getWeight(feature);
//
//            double gradient = dloss * xi;
//            double new_weight = optimizer.update(feature, weight, loss, gradient);
//
//            assert accumulated != null;
//            DoubleAccumulator acc = accumulated.get(feature);
//            if (acc == null) {
//                acc = new DoubleAccumulator(new_weight);
//                accumulated.put(feature, acc);
//            } else {
//                acc.add(new_weight);
//            }
//        }
//        sampled++;
//    }

//    protected void batchUpdate() {
//        assert accumulated != null;
//        if (accumulated.isEmpty()) {
//            sampled = 0;
//            return;
//        }
//
//        for (ConcurrentHashMap.Entry<Object, DoubleAccumulator> e : accumulated.entrySet()) {
//            Object feature = e.getKey();
//            DoubleAccumulator v = e.getValue();
//            final double new_weight = v.get();
//            if (new_weight == 0.d) {
//                model.delete(feature);
//                continue;
//            }
//            model.setWeight(feature, new_weight);
//        }
//
//        accumulated.clear();
//        this.sampled = 0;
//    }

    public abstract byte[] toByteArray();

    public abstract BaseLearner fromByteArray(byte[] learnerBytes) throws InvalidProtocolBufferException;

    protected void finalizeTraining() {
    }

//    protected void finalizeTraining() {
//        if (count == 0L) {
//            return;
//        }
//        if (isMiniBatch) { // Update model with accumulated delta
//            batchUpdate();
//        }
//        if (iters > 1) {
//            runIterativeTraining(iters);
//        }
//    }

    protected abstract void checkTargetValue(String target) throws IllegalArgumentException;

    /*
    Check whether the loss function is for regression or classification.
     */
    public abstract void checkLossFunction(LossFunctions.LossFunction lossFunction) throws IllegalArgumentException;

    public abstract LossFunctions.LossType getDefaultLossType();

    @Nonnull
    protected abstract String getLossOptionDescription();

    protected class OneSample {

        public ArrayList<FeatureValue> featureValueVector;

        public String target;
    }
}
