package drillbit.parameter;

import drillbit.protobuf.ParameterPb;

import com.google.protobuf.InvalidProtocolBufferException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class SparseModel extends AbstractModel {
    ConcurrentHashMap<Object, Weights.ExtendedWeight> weights;

    public SparseModel(int ndims, Weights.WeightType weightType) {
        super(ModelType.Sparse, weightType);
        weights = new ConcurrentHashMap<>(ndims);
    }

    public SparseModel(int ndims, Weights.WeightBuilder weightBuilder) {
        super(ModelType.Sparse, weightBuilder);
        weights = new ConcurrentHashMap<>(ndims);
    }

    @Override
    public ConcurrentHashMap<Object, Weights.ExtendedWeight> weightMap() {
        return weights;
    }

    @Override
    public byte[] toByteArray() {
        ParameterPb.SparseWeights.Builder builder = ParameterPb.SparseWeights.newBuilder();
        ParameterPb.SparseWeights.SparseWeight.Builder weightBuilder = ParameterPb.SparseWeights.SparseWeight.newBuilder();
        for (ConcurrentHashMap.Entry<Object, Weights.ExtendedWeight> entry : weightMap().entrySet()) {
            builder.addWeight(weightBuilder.setFeature(entry.getKey().toString()).setWeight(entry.getValue().get()).build());
        }
        return builder.build().toByteArray();
    }

    @Override
    public SparseModel fromByteArray(byte[] byteArray) throws InvalidProtocolBufferException {
        try {
            Weights.WeightBuilder extendedWeightBuilder = getWeightBuilder();
            List<ParameterPb.SparseWeights.SparseWeight> weights = ParameterPb.SparseWeights.parseFrom(byteArray).getWeightList();
            for (ParameterPb.SparseWeights.SparseWeight weight : weights) {
                set(weight.getFeature(), extendedWeightBuilder.buildFromWeight(weight.getWeight()));
            }
        } catch (InvalidProtocolBufferException e) {
            throw e;
        }
        return this;
    }

    @Nullable
    @Override
    public <T extends Weights.ExtendedWeight> T get(@Nonnull final Object feature) {
        if (weights.containsKey(feature)) {
            Weights.ExtendedWeight weight = weights.get(feature);
            return (T) weight;
        }
        else {
            return null;
        }
    }

    @Override
    public <T extends Weights.ExtendedWeight> void set(@Nonnull final Object feature, @Nonnull T value) {
        weights.put(feature, value);
        size = weights.size();
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
        Weights.ExtendedWeight value = get(feature);
        if (value != null) {
            return value.get();
        }
        return 0.d;
    }

    @Override
    public void setWeight(@Nonnull Object feature, @Nonnull double weight) {
        Weights.ExtendedWeight value = get(feature);
        if (value != null) {
            value.set(weight);
            set(feature, value);
        }
        else {
            Weights.ExtendedWeight newValue = weightBuilder.buildFromWeight(weight);
            set(feature, newValue);
        }
    }
}
