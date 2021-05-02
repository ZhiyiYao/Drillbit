package drillbit.cluster;

import com.google.protobuf.InvalidProtocolBufferException;

import drillbit.BaseLearner;
import drillbit.FeatureValue;
import drillbit.optimizer.LossFunctions;

import org.apache.commons.cli.Options;
import javax.annotation.Nonnull;

import java.util.ArrayList;

public class GaussianMixtureModelClassificationLearner extends BaseLearner {
    @Override
    public Options getOptions() {
        Options opts = super.getOptions();

//        opts.addOption("");
        return opts;
    }

    @Override
    protected void train(@Nonnull ArrayList<FeatureValue> featureVector, @Nonnull double target) {

    }

    @Override
    public Object predict(@Nonnull String features, @Nonnull String options) {
        return null;
    }

    @Override
    protected void update(@Nonnull ArrayList<FeatureValue> features, double target, double predicted) {

    }

    @Override
    public void add(@Nonnull String feature, @Nonnull String target) {

    }

    @Override
    public byte[] toByteArray() {
        return new byte[0];
    }

    @Override
    public BaseLearner fromByteArray(byte[] learnerBytes) throws InvalidProtocolBufferException {
        return null;
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
