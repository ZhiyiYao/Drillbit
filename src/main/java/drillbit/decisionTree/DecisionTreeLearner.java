package drillbit.decisionTree;

import com.google.protobuf.InvalidProtocolBufferException;
import drillbit.BaseLearner;
import drillbit.FeatureValue;
import drillbit.optimizer.LossFunctions;
import drillbit.utils.parser.StringParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.ArrayList;

public class DecisionTreeLearner extends BaseLearner {
    private DecisionTree tree;

    public DecisionTreeLearner() {
        super();
    }

    @Override
    public Options getOptions() {
        Options opts = super.getOptions();

        opts.addOption("criterion", true, "decision tree split algorithm");
        opts.addOption("discrete", false, "whether the tree is discrete");
        opts.addOption("height", true, "tree height limit");

        return opts;
    }

    @Override
    public CommandLine processOptions(@Nonnull final CommandLine cl) {
        super.processOptions(cl);

        int height = -1;
        if (cl.hasOption("height")) {
            height = StringParser.parseInt(cl.getOptionValue("height"), -1);
        }

        if (cl.hasOption("discrete")) {
            tree = new DecisionTree(cl.getOptionValue("criterion"), true, height);
        } else {
            tree = new DecisionTree(cl.getOptionValue("criterion"), false, height);
        }

        return cl;
    }

    @Override
    protected void train(@Nonnull ArrayList<FeatureValue> featureVector, @Nonnull double target) {
        tree.train(featureValueVectors, targets);
    }

    @Override
    public Object predict(@Nonnull String features, @Nonnull String options) {
        return predict(parseFeatureList(features));
    }

    public String predict(ArrayList<FeatureValue> featureVector) {
        return tree.predict(featureVector);
    }

    @Override
    protected void update(@Nonnull ArrayList<FeatureValue> features, double target, double predicted) {
        ;
    }

    @Override
    public void add(@Nonnull String feature, @Nonnull String target) {
        ArrayList<FeatureValue> featureValues = parseFeatureList(feature);
        checkTargetValue(target);
        writeSample(featureValues, target);
    }

    @Override
    protected void finalizeTraining() {
        train(new ArrayList<>(), -1);
    }

    @Override
    public byte[] toByteArray() {
        return tree.getBytes();
    }

    @Override
    public BaseLearner fromByteArray(byte[] learnerBytes) throws InvalidProtocolBufferException {
        try {
            tree = new DecisionTree(learnerBytes);
        } catch (IOException e) {
            throw new InvalidProtocolBufferException(e.toString());
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

    @Nonnull
    @Override
    protected String getLossOptionDescription() {
        return null;
    }
}
