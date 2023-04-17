package drillbit.SVM;

import com.google.protobuf.InvalidProtocolBufferException;
import drillbit.BaseLearner;
import drillbit.FeatureValue;
import drillbit.SVM.libsvm.*;
import drillbit.optimizer.LossFunctions;
import drillbit.utils.parser.StringParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.io.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SupportVectorMachineLearner extends BaseLearner {
    private final svm_parameter params;
    private svm_model model;
    private svm_scale scale;

    public SupportVectorMachineLearner() {
        super();
        params = new svm_parameter();

        params.svm_type = svm_parameter.C_SVC;
        params.kernel_type = svm_parameter.RBF;
        params.degree = 3;
        params.gamma = 0.5;    // 1/num_features
        params.coef0 = 0;
        params.nu = 0.5;
        params.cache_size = 100;
        params.C = 5;
        params.eps = 1e-3;
        params.p = 0.1;
        params.shrinking = 1;
        params.probability = 0;
        params.nr_weight = 0;
        params.weight_label = new int[0];
        params.weight = new double[0];
    }

    @Override
    public @NotNull Options getOptions() {
        Options opts = super.getOptions();

        opts.addOption("kernel_type", true, "Type of kernel");
        opts.addOption("degree", true, "Degree in kernel function (default 3)");
        opts.addOption("gamma", true, "Gamma in kernel function (default 1/num_features)");
        opts.addOption("coef0", true, "coef0 in kernel function");
        opts.addOption("C", true, "set the parameter C");

        return opts;
    }

    @Override
    public CommandLine processOptions(@Nonnull final CommandLine cl) {
        super.processOptions(cl);

        if (cl.hasOption("kernel_type")) {
            params.kernel_type = svm_parameter.Parse_kernel(cl.getOptionValue("kernel_type"));
        }

        if (cl.hasOption("degree")) {
            params.degree = StringParser.parseInt(cl.getOptionValue("degree"), 3);
        }

        if (cl.hasOption("gamma")) {
            params.gamma = StringParser.parseInt(cl.getOptionValue("gamma"), 1);
        }

        if (cl.hasOption("coef0")) {
            params.coef0 = StringParser.parseInt(cl.getOptionValue("coef0"), 0);
        }

        if (cl.hasOption("C")) {
            params.C = StringParser.parseInt(cl.getOptionValue("C"), 1);
        }

        return cl;
    }

    @Override
    protected void train(@NotNull ArrayList<FeatureValue> featureVector, @NotNull double target) {
//        scale = svm.svm_scale_build(featureValueVectors);
//        for (ArrayList<FeatureValue> record : featureValueVectors) {
//            scale.restore(record);
//        }

        int sampleNum = featureValueVectors.size();
        int dims = featureValueVectors.get(0).size();
        svm_node[][] dataMatrix = new svm_node[sampleNum][dims];
        double[] dataLabel = new double[sampleNum];

        Map<String, Double> labels = new ConcurrentHashMap<>();
        int count = 0;
        for (int i = 0; i < sampleNum; i++) {
            ArrayList<FeatureValue> oneSample = featureValueVectors.get(i);
            for (int j = 0; j < dims; j++) {
                FeatureValue fv = oneSample.get(j);

                svm_node e = new svm_node();
                e.index = j + 1;
                e.value = fv.getValueAsDouble();
                dataMatrix[i][j] = e;
            }

            //label the class
            String label = targets.get(i);
            if (!labels.containsKey(label)) {
                labels.put(label, (double) (count));
                count++;
            }
            dataLabel[i] = labels.get(label);
        }

        svm_problem svmP = new svm_problem();
        svmP.x = dataMatrix;
        svmP.y = dataLabel;
        svmP.l = sampleNum;

        if (params.gamma == 0 && dims > 0)
            params.gamma = 1.0 / dims;

        model = svm.svm_train(svmP, params);

        String[] labelsGroup = new String[labels.size()];
        for (Map.Entry<String, Double> en : labels.entrySet()) {
            labelsGroup[en.getValue().intValue()] = en.getKey();
        }
        model.labels = labelsGroup;
    }

    @Override
    public Object predict(@NotNull String features, @NotNull String options) {
        ArrayList<FeatureValue> featureVector = parseFeatureList(features);
        //scale.restore(featureVector);
        int dims = featureVector.size();

        svm_node[] x = new svm_node[dims];
        for (int i = 0; i < dims; i++) {
            svm_node node = new svm_node();
            node.index = i + 1;
            node.value = featureVector.get(i).getValueAsDouble();
            //System.out.println(node.value);
            x[i] = node;
        }

        return model.labels[(int) svm.svm_predict(model, x)];
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

    @Override
    public void finalizeTraining() {
        train(new ArrayList<>(), -1);
    }

    @Override
    public byte[] toByteArray() {
        byte[] results = new byte[1];
        try {
            ByteArrayOutputStream bytes = svm.svm_save_model_as_flow(model);
            System.out.println(bytes.toString());
            // results = scale.save(bytes);
            results = bytes.toByteArray();
            // System.out.println(Arrays.toString(scale.feature_max));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return results;
    }

    @Override
    public BaseLearner fromByteArray(byte[] learnerBytes) throws InvalidProtocolBufferException {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(learnerBytes)));
            model = svm.svm_load_model(reader);
            //scale = svm.readFromBytes(reader);
            //System.out.println(Arrays.toString(scale.feature_max));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return this;
    }

    @Override
    protected void checkTargetValue(String target) throws IllegalArgumentException {

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
