package drillbit.parameter;

import com.google.protobuf.InvalidProtocolBufferException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class AbstractModel implements Model {
    Weights.WeightBuilder weightBuilder;

    ModelType modelType;

    Weights.WeightType weightType;

    int size;

    AbstractModel(ModelType modelType, Weights.WeightType weightType) {
        this.modelType = modelType;
        this.weightType = weightType;
        weightBuilder = Weights.getWeightBuilder(weightType);
        size = 0;
    }

    AbstractModel(ModelType modelType, Weights.WeightBuilder weightBuilder) {
        this.modelType = modelType;
        this.weightBuilder = weightBuilder;
        weightType = weightBuilder.getWeightType();
        size = 0;
    }

    @Override
    public Weights.WeightBuilder getWeightBuilder() {
        return weightBuilder;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean contains(@Nonnull Object feature) {
        return false;
    }

    @Override
    public void delete(@Nonnull Object feature) {
        throw new UnsupportedOperationException();
    }

    @Nullable
    @Override
    public byte[] toByteArray() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Model fromByteArray(byte[] byteArray) throws InvalidProtocolBufferException {
        throw new UnsupportedOperationException();
    }

    @Override
    public double getWeight(@Nonnull Object feature) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setWeight(@Nonnull Object feature, @Nonnull double value) {
        throw new UnsupportedOperationException();
    }

    @Nullable
    @Override
    public <T extends Weights.ExtendedWeight> T get(@Nonnull Object feature) {
        return null;
    }

    @Override
    public <T extends Weights.ExtendedWeight> void set(@Nonnull Object feature, @Nonnull final T value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <V extends Weights.ExtendedWeight> ConcurrentHashMap<Object, V> weightMap() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T extends Weights.ExtendedWeight> ArrayList<T> weightList() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Model toAnotherModelWithDifferentWeight(Weights.WeightBuilder builder) {
        weightBuilder = builder;
        weightType = builder.getWeightType();
        try {
            return fromByteArray(toByteArray());
        }
        catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
            return null;
        }
    }
}
