package drillbit.parameter;

import com.google.protobuf.InvalidProtocolBufferException;
import drillbit.TrainWeights;
import drillbit.protobuf.ParameterPb;
import drillbit.utils.parser.ObjectParser;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class DenseWeights extends Weights {
    private final ArrayList<TrainWeights.ExtendedWeight> weights;

    public DenseWeights(int ndims, TrainWeights.WeightType weightType) {
        super(StorageType.Dense, weightType);
        weights = new ArrayList<>(ndims);
    }

    public DenseWeights(int ndims, TrainWeights.WeightBuilder weightBuilder) {
        super(StorageType.Dense, weightBuilder);
        weights = new ArrayList<>(ndims);
    }

    @Override
    public int size() {
        return size;
    }

    @Nullable
    @Override
    public ArrayList<TrainWeights.ExtendedWeight> weightList() {
        return weights;
    }

    @Override
    public boolean contains(@Nonnull Object feature) {
        int i = ObjectParser.parseInt(feature);
        if (0 <= i && i < size) {
            TrainWeights.ExtendedWeight value = weights.get(i);
            return value.get() != 0.d;
        }
        return false;
    }

    @Override
    public void delete(@Nonnull Object feature) {
        int i = ObjectParser.parseInt(feature);
        if (0 <= i && i < size) {
            TrainWeights.ExtendedWeight value = weights.get(i);
            value.clear();
            weights.set(i, value);
        }
    }

    @Nullable
    @Override
    public byte[] toByteArray() {
        byte[] byteArray;
        List<TrainWeights.ExtendedWeight> weights = weightList();
        ParameterPb.DenseWeights.Builder builder = ParameterPb.DenseWeights.newBuilder();
        for (TrainWeights.ExtendedWeight weight : weights) {
            if (weight != null) {
                builder.addWeight(weight.get());
            }
        }
        byteArray = builder.build().toByteArray();
        return byteArray;
    }

    @Override
    public DenseWeights fromByteArray(@Nonnull final byte[] byteArray) throws InvalidProtocolBufferException {
        List<Double> weights;
        try {
            weights = ParameterPb.DenseWeights.parseFrom(byteArray).getWeightList();
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
            throw e;
        }

        TrainWeights.WeightBuilder builder = getWeightBuilder();
        for (int i = 0; i < weights.size(); i++) {
            set(i, builder.buildFromWeight(weights.get(i)));
        }

        return this;
    }

    @Nullable
    @Override
    public <T extends TrainWeights.ExtendedWeight> T get(@Nonnull final Object feature) {
        final int i = ObjectParser.parseInt(feature);
        if (i > size) {
            return null;
        }
        TrainWeights.ExtendedWeight weight = null;
        try {
            weight = weights.get(i);
        }
        catch (IndexOutOfBoundsException ignored) {
        }
        if (weight == null || weight.get() == 0.d) {
            return null;
        }
        return (T) weight;
    }

    @Override
    public <T extends TrainWeights.ExtendedWeight> void set(@Nonnull final Object feature, @Nonnull T value) {
        final int i = ObjectParser.parseInt(feature);
        if (weights.size() <= i) {
            for (int j = weights.size(); j <= 2 * i + 1; j++) {
                weights.add(null);
            }
        }
        weights.set(i, value);
        size = weights.size();
    }

    @Override
    public <V extends TrainWeights.ExtendedWeight> ConcurrentHashMap<Object, V> weightMap() {
        throw new UnsupportedOperationException();
    }

    @Override
    public double getWeight(@Nonnull final Object feature) {
        TrainWeights.ExtendedWeight value = get(feature);
        if (value != null) {
            return value.get();
        }
        return 0.d;
    }

    @Override
    public void setWeight(@Nonnull Object feature, @Nonnull double weight) {
        TrainWeights.ExtendedWeight value = get(feature);
        if (value != null) {
            value.set(weight);
            set(feature, value);
        }
        else {
            TrainWeights.ExtendedWeight newValue = weightBuilder.buildFromWeight(weight);
            set(feature, newValue);
        }
    }
}
