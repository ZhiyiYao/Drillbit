package drillbit.regression;

import com.google.protobuf.InvalidProtocolBufferException;
import drillbit.BaseLearner;
import drillbit.parameter.FeatureValue;
import drillbit.optimizer.LossFunctions;

import javax.annotation.Nonnull;
import java.util.ArrayList;

public class RegressionBaseLearner extends BaseLearner {
    @Override
    protected void train(@Nonnull ArrayList<FeatureValue> featureVector, double target) {

    }

    @Override
    public double predict(@Nonnull ArrayList<FeatureValue> features) {
        return 0;
    }

    @Override
    protected void update(@Nonnull ArrayList<FeatureValue> features, double target, double predicted) {

    }

    @Override
    public void add(@Nonnull String feature, @Nonnull String target, @Nonnull String commandLine) {

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
        return "";
    }
}
