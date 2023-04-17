package drillbit.parameter;

import com.google.protobuf.InvalidProtocolBufferException;
import drillbit.TrainWeights;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public abstract class Weights {
    StorageType storageType;
    int size;

    TrainWeights.WeightBuilder weightBuilder;
    TrainWeights.WeightType weightType;

    Weights(StorageType storageType, TrainWeights.WeightType weightType) {
        this.storageType = storageType;
        this.weightType = weightType;
        weightBuilder = TrainWeights.getWeightBuilder(weightType);
        size = 0;
    }

    Weights(StorageType storageType, TrainWeights.WeightBuilder weightBuilder) {
        this.storageType = storageType;
        this.weightBuilder = weightBuilder;
        weightType = weightBuilder.getWeightType();
        size = 0;
    }

    public TrainWeights.WeightBuilder getWeightBuilder() {
        return weightBuilder;
    }

    public abstract int size();

    public abstract boolean contains(@Nonnull Object feature);

    public abstract void delete(@Nonnull Object feature);

    @Nullable
    public abstract byte[] toByteArray();

    public abstract Weights fromByteArray(byte[] byteArray) throws InvalidProtocolBufferException;

    public abstract double getWeight(@Nonnull Object feature);

    public abstract void setWeight(@Nonnull Object feature, @Nonnull double value);

    @Nullable
    public abstract <T extends TrainWeights.ExtendedWeight> T get(@Nonnull Object feature);

    public abstract <T extends TrainWeights.ExtendedWeight> void set(@Nonnull Object feature, @Nonnull final T value);

    public abstract <V extends TrainWeights.ExtendedWeight> ConcurrentHashMap<Object, V> weightMap();

    public abstract <T extends TrainWeights.ExtendedWeight> ArrayList<T> weightList();

    public Weights toAnotherModelWithDifferentWeight(TrainWeights.WeightBuilder builder) {
        weightBuilder = builder;
        weightType = builder.getWeightType();
        try {
            return fromByteArray(toByteArray());
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
            return null;
        }
    }

    enum StorageType {
        Dense, Sparse
    }
}
