package drillbit.parameter;

import com.google.protobuf.InvalidProtocolBufferException;
import drillbit.TrainWeights;
import drillbit.protobuf.ParameterPb;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class SparseWeights extends Weights {
    private final ConcurrentHashMap<String, TrainWeights.ExtendedWeight> weights;

    public SparseWeights(int ndims, TrainWeights.WeightType weightType) {
        super(StorageType.Sparse, weightType);
        weights = new ConcurrentHashMap<>(ndims);
    }

    public SparseWeights(int ndims, TrainWeights.WeightBuilder weightBuilder) {
        super(StorageType.Sparse, weightBuilder);
        weights = new ConcurrentHashMap<>(ndims);
    }

    @Override
    public ConcurrentHashMap<Object, TrainWeights.ExtendedWeight> weightMap() {
        ConcurrentHashMap<Object, TrainWeights.ExtendedWeight> result = new ConcurrentHashMap<>();
        for (Map.Entry<String, TrainWeights.ExtendedWeight> entry : weights.entrySet()) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }

    @Override
    public byte[] toByteArray() {
        ParameterPb.SparseWeights.Builder builder = ParameterPb.SparseWeights.newBuilder();
        ParameterPb.SparseWeights.Item.Builder itemBuilder = ParameterPb.SparseWeights.Item.newBuilder();
        for (ConcurrentHashMap.Entry<Object, TrainWeights.ExtendedWeight> entry : weightMap().entrySet()) {
            builder.addWeight(itemBuilder.setFeature(entry.getKey().toString()).setWeight(entry.getValue().get()).build());
        }
        return builder.build().toByteArray();
    }

    @Override
    public SparseWeights fromByteArray(byte[] byteArray) throws InvalidProtocolBufferException {
        try {
            TrainWeights.WeightBuilder extendedWeightBuilder = getWeightBuilder();
            List<ParameterPb.SparseWeights.Item> weights = ParameterPb.SparseWeights.parseFrom(byteArray).getWeightList();
            for (ParameterPb.SparseWeights.Item weight : weights) {
                set(weight.getFeature(), extendedWeightBuilder.buildFromWeight(weight.getWeight()));
            }
        } catch (InvalidProtocolBufferException e) {
            throw e;
        }
        return this;
    }

    @Nullable
    @Override
    public <T extends TrainWeights.ExtendedWeight> T get(@Nonnull final Object feature) {
        if (weights.containsKey((String) feature)) {
            TrainWeights.ExtendedWeight weight = weights.get((String) feature);
            return (T) weight;
        } else {
            return null;
        }
    }

    @Override
    public <T extends TrainWeights.ExtendedWeight> void set(@Nonnull final Object feature, @Nonnull T value) {
        weights.put((String) feature, value);
        size = weights.size();
    }

    @Override
    public <T extends TrainWeights.ExtendedWeight> ArrayList<T> weightList() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int size() {
        return weights.size();
    }

    @Override
    public boolean contains(@Nonnull final Object feature) {
        return weights.containsKey(feature);
    }

    @Override
    public void delete(@Nonnull final Object feature) {
        weights.remove(feature);
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
        } else {
            TrainWeights.ExtendedWeight newValue = weightBuilder.buildFromWeight(weight);
            set(feature, newValue);
        }
    }
}
