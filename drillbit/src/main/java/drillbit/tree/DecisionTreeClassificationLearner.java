package drillbit.tree;

import com.google.protobuf.InvalidProtocolBufferException;
import drillbit.BaseLearner;
import drillbit.FeatureValue;
import drillbit.optimizer.LossFunctions;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public final class DecisionTreeClassificationLearner extends BaseLearner {

    @Override
    protected void train(@NotNull ArrayList<FeatureValue> featureVector, @NotNull double target) {

    }

    @Override
    public Object predict(@NotNull String features, @NotNull String options) {
        return null;
    }

    @Override
    protected void update(@NotNull ArrayList<FeatureValue> features, double target, double predicted) {

    }

    @Override
    public void add(@NotNull String feature, @NotNull String target, @NotNull String commandLine) {

    }

    @Override
    public void add(@NotNull String feature, @NotNull String target) {

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

    @NotNull
    @Override
    protected String getLossOptionDescription() {
        return null;
    }
}
