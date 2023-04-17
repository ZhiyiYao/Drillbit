package drillbit.DNN;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import drillbit.BaseLearner;
import drillbit.DNN.dl4jAdapter.OptionParser;
import drillbit.FeatureValue;
import drillbit.optimizer.LossFunctions;
import drillbit.protobuf.NetworkPb;
import drillbit.utils.parser.StringParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.deeplearning4j.datasets.iterator.utilty.ListDataSetIterator;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.util.ModelSerializer;
import org.jetbrains.annotations.NotNull;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;

import javax.annotation.Nonnull;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class NeuralNetworkLearner extends BaseLearner {
    private final Map<String, Integer> labelIndex;
    //base
    private ArrayList<String> labels;
    private boolean regression;
    private MultiLayerNetwork model;
    private int dims;
    //training
    private NeuralNetConfiguration.Builder confBuilder;
    private String lossFunction;
    private ArrayList<String> layerParams;
    private int layerNum;
    private int batchSize;
    private int epoch;
    private int outputSize;

    //prediction

    public NeuralNetworkLearner() {
        super();
        labelIndex = new ConcurrentHashMap<>();
        labels = new ArrayList<>();
    }

    @Override
    public @NotNull Options getOptions() {
        Options opts = super.getOptions();

        opts.addOption("opt", true, "optimizer name (default SGD)");
        opts.addOption("type", true, "classification or regression");
        opts.addOption("activation", true, "activation type (default 3)");
        opts.addOption("seed", true, "specify random seed (default time)");
        opts.addOption("lr", true, "learning rate (default 0.1)");
        opts.addOption("norm", true, "normalization function (default null)");
        opts.addOption("dropout", true, "dropout (default 1)");
        opts.addOption("loss", true, "loss function");
        opts.addOption("batch_size", true, "training batch size");
        opts.addOption("epoch", true, "training epoch");
        opts.addOption("layers", true, "layers count");
        opts.addOption("layer_params", true, "specify each layer parameters (default 64)");
//        opts.addOption("output", true, "specify the output size");

        return opts;
    }

    @Override
    public CommandLine processOptions(@Nonnull final CommandLine cl) {
        super.processOptions(cl);

        String optimizer = "SGD";
        if (cl.hasOption("opt")) {
            optimizer = cl.getOptionValue("opt");
        }

        regression = false;
        if (cl.hasOption("type")) {
            if (Objects.equals(cl.getOptionValue("type"), "regression")) {
                regression = true;
            }
        }

        String act = "RELU";
        if (cl.hasOption("activation")) {
            act = cl.getOptionValue("activation");
        }

        String seed = "";
        if (cl.hasOption("seed")) {
            seed = cl.getOptionValue("seed");
        }

        double learningRate = 0.1;
        if (cl.hasOption("lr")) {
            learningRate = StringParser.parseDouble(cl.getOptionValue("lr"), 0.1);
        }

        if (cl.hasOption("layers")) {
            layerNum = StringParser.parseInt(cl.getOptionValue("layers"), 1);
        }

        String norm = "";
        if (cl.hasOption("norm")) {
            norm = cl.getOptionValue("norm");
        }

        double dropout = 1;
        if (cl.hasOption("dropout")) {
            dropout = StringParser.parseDouble(cl.getOptionValue("dropout"), 1);
        }

        batchSize = StringParser.parseInt(cl.getOptionValue("batch_size"), 32);
        epoch = StringParser.parseInt(cl.getOptionValue("epoch"), 100);

        if (regression) lossFunction = "MSE";
        else lossFunction = "LOGITS";
        if (cl.hasOption("loss")) {
            norm = cl.getOptionValue("loss");
        }

        if (cl.hasOption("layer_params")) {
            layerParams = StringParser.parseArray(cl.getOptionValue("layer_params"));
        }

        confBuilder = new NeuralNetConfiguration.Builder();
        if (!seed.equals("")) {
            confBuilder.seed(StringParser.parseInt(seed, 0));
        }
        confBuilder.updater(OptionParser.ParseOptimizer(optimizer, learningRate))
                .weightInit(WeightInit.XAVIER)
                .activation(OptionParser.ParseActivation(act));
        if (!norm.equals("")) {
            if (norm.equals("l1")) confBuilder.l1(0.0001);
            else if (norm.equals("l2")) confBuilder.l2(0.0001);
        }
        confBuilder.dropOut(dropout);

        return cl;
    }

    @Override
    protected void train(@NotNull ArrayList<FeatureValue> featureVector, @NotNull double target) {

    }

    @Override
    public Object predict(@NotNull String features, @NotNull String options) {
        double[][] xArray = new double[1][dims];
        ArrayList<FeatureValue> featureValues = parseFeatureList(features);
        for (int i = 0; i < dims; i++) {
            xArray[0][i] = featureValues.get(i).getValueAsDouble();
        }

        INDArray x = Nd4j.createFromArray(xArray);
        INDArray result = model.output(x);

        String textResult = "";
        if (regression) {
            textResult = Double.toString(result.getDouble(0, 0));
        } else {
            INDArray classIndex = Nd4j.argMax(result, 1);
            textResult = labels.get(classIndex.getInt(0, 0));
        }
        return textResult;
    }

    @Override
    protected void update(@NotNull ArrayList<FeatureValue> features, double target, double predicted) {

    }

    @Override
    public void add(@NotNull String feature, @NotNull String target) {
        ArrayList<FeatureValue> featureValues = parseFeatureList(feature);
        checkTargetValue(target);
        writeSample(featureValues, target);
    }

    private DataSet prepareDataset() {
        outputSize = labelIndex.size();
        int sampleNum = featureValueVectors.size();
        if (regression) {
            outputSize = 1;
        } else {
            labels = new ArrayList<>();
            for (int i = 0; i < outputSize; i++) labels.add("");

            for (Map.Entry<String, Integer> kv : labelIndex.entrySet()) {
                labels.set(kv.getValue(), kv.getKey());
            }
        }
        double[][] xArray = new double[sampleNum][dims];
        double[][] yArray = new double[sampleNum][outputSize];

        if (regression) {
            for (int i = 0; i < sampleNum; i++) {
                for (int j = 0; j < dims; j++) {
                    xArray[i][j] = featureValueVectors.get(i).get(j).getValueAsDouble();
                    yArray[i][0] = StringParser.parseDouble(targets.get(i), 0);
                }
            }
        } else {
            for (int i = 0; i < sampleNum; i++) {
                for (int j = 0; j < dims; j++) {
                    xArray[i][j] = featureValueVectors.get(i).get(j).getValueAsDouble();
                }
                yArray[i][labelIndex.get(targets.get(i))] = 1;
            }
        }

        return new DataSet(Nd4j.createFromArray(xArray), Nd4j.createFromArray(yArray));
    }

    @Override
    public void finalizeTraining() {
        dims = featureValueVectors.get(0).size();

        int bp = dims;
        int ap = 64;

        DataSet allData = prepareDataset();

        NeuralNetConfiguration.ListBuilder listBuilder = confBuilder.list();
        if (layerParams != null) {
            bp = StringParser.parseInt(layerParams.get(0), dims);

            for (int i = 1; i < layerParams.size(); i++) {
                ap = StringParser.parseInt(layerParams.get(i), 64);
                listBuilder.layer(new DenseLayer
                        .Builder()
                        .nIn(bp)
                        .nOut(ap)
                        .build());
                bp = ap;
            }
        } else {
            for (int i = 0; i < layerNum; i++) {
                listBuilder.layer(new DenseLayer
                        .Builder()
                        .nIn(bp)
                        .nOut(ap)
                        .build());
                bp = ap;
            }
        }

        if (regression) {
            listBuilder.layer(new OutputLayer.Builder(OptionParser.ParseLossFunction(lossFunction))
                    .activation(Activation.IDENTITY)
                    .nIn(ap).nOut(1).build());
        } else {
            listBuilder.layer(new OutputLayer.Builder(OptionParser.ParseLossFunction(lossFunction))
                    .activation(Activation.SOFTMAX)
                    .nIn(ap).nOut(outputSize).build());
        }
        model = new MultiLayerNetwork(listBuilder.build());
        model.init();

        DataSetIterator dataLoader = new ListDataSetIterator<>(allData.asList(), batchSize);
//        model.setListeners(new ScoreIterationListener(100));

        for (int i = 0; i < epoch; i++) {
            model.fit(dataLoader);
        }
    }

    @Override
    public byte[] toByteArray() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            ModelSerializer.writeModel(model, out, false);
        } catch (IOException e) {
            e.printStackTrace();
        }

        NetworkPb.NeuralNetwork.Builder builder = NetworkPb.NeuralNetwork.newBuilder();
        builder.setRegression(regression);
        builder.addAllLabels(labels);
        builder.setDims(dims);
        builder.setModel(ByteString.copyFrom(out.toByteArray()));

        return builder.build().toByteArray();
    }

    @Override
    public BaseLearner fromByteArray(byte[] learnerBytes) throws InvalidProtocolBufferException {
        NetworkPb.NeuralNetwork byteNetwork;
        try {
            byteNetwork = NetworkPb.NeuralNetwork.parseFrom(learnerBytes);
        } catch (InvalidProtocolBufferException e) {
            logger.error(e);
            throw e;
        }

        regression = byteNetwork.getRegression();
        dims = byteNetwork.getDims();
        labels = new ArrayList<>();
        labels.addAll(byteNetwork.getLabelsList());

        try {
            model = ModelSerializer.restoreMultiLayerNetwork(
                    new ByteArrayInputStream(byteNetwork.getModel().toByteArray()), false);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return this;
    }

    @Override
    protected void checkTargetValue(String target) throws IllegalArgumentException {
        if (!regression && !labelIndex.containsKey(target)) {
            labelIndex.put(target, labelIndex.size());
        }
    }

    @Override
    public void checkLossFunction(LossFunctions.LossFunction lossFunction) throws IllegalArgumentException {

    }

    @Override
    public LossFunctions.LossType getDefaultLossType() {
        return null;
    }

    @NotNull
    @Override
    protected String getLossOptionDescription() {
        return null;
    }
}
