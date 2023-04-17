package drillbit.dmlmanager;

import com.google.protobuf.ByteString;
import drillbit.TrainWeights;
import drillbit.parameter.DenseWeights;
import drillbit.parameter.SparseWeights;
import drillbit.protobuf.ClassificationPb;
import drillbit.protobuf.SyncWeightsPb;
import drillbit.utils.common.DoubleAccumulator;
import drillbit.utils.parser.ObjectParser;
import drillbit.utils.parser.Serializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class MultiWeightsAccumulator implements SynData {
    private ConcurrentHashMap<String, ConcurrentHashMap<Object, DoubleAccumulator>> accumulated;

    public MultiWeightsAccumulator(ConcurrentHashMap<String, ConcurrentHashMap<Object, DoubleAccumulator>> input) {
        accumulated = input;
    }

    public MultiWeightsAccumulator() {
        accumulated = new ConcurrentHashMap<>();
    }

    @Override
    public void addData(SynData data) {
        for (Map.Entry<String, ConcurrentHashMap<Object, DoubleAccumulator>> classAccumulated : accumulated.entrySet()) {
            ConcurrentHashMap<Object, DoubleAccumulator> classAccumulator = classAccumulated.getValue();
            ConcurrentHashMap<Object, DoubleAccumulator> received = data.getAccumulation(classAccumulated.getKey());

            for (ConcurrentHashMap.Entry<Object, DoubleAccumulator> e : received.entrySet()) {
                Object feature = e.getKey();
                DoubleAccumulator v = e.getValue();

                DoubleAccumulator acc = classAccumulator.get(ObjectParser.parseInt(feature));
                //System.out.println("recieve "+ObjectParser.parseInt(feature)+" acc: "+v.get());
                if (acc == null) {
                    acc = new DoubleAccumulator(v.get());
                    classAccumulator.put(feature, acc);
                } else {
                    acc.add(v.get());
                }
            }
        }

    }

    @Override
    public ConcurrentHashMap<Object, DoubleAccumulator> getAccumulation(String label) {
        return accumulated.get(label);
    }

    @Override
    public byte[] getBytes() {
        SyncWeightsPb.SyncAccumulation.Builder builder = SyncWeightsPb.SyncAccumulation.newBuilder();
        SyncWeightsPb.SyncAccumulation.LabelAndWeights.Builder labelAndWeightsBuilder = SyncWeightsPb.SyncAccumulation.LabelAndWeights.newBuilder();

        for (Map.Entry<String, ConcurrentHashMap<Object, DoubleAccumulator>> classAccumulated : accumulated.entrySet()) {
            labelAndWeightsBuilder.clear();
            builder.addLabel2Weights(labelAndWeightsBuilder.setLabel(classAccumulated.getKey())
                    .setWeights(ByteString.copyFrom(Objects.requireNonNull(Serializer.accumulatedToByteArray(classAccumulated.getValue())))).build());
        }

        return builder.build().toByteArray();
    }

    @Override
    public void fromBytes(byte[] data) {
        try {
            SyncWeightsPb.SyncAccumulation byteAccumulator = SyncWeightsPb.SyncAccumulation.parseFrom(data);
            for (SyncWeightsPb.SyncAccumulation.LabelAndWeights labelAndWeights : byteAccumulator.getLabel2WeightsList()) {
                accumulated.put(labelAndWeights.getLabel(),Serializer.accumulatedFromByteArray(labelAndWeights.getWeights().toByteArray()));
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public void clear() {
        accumulated.clear();
    }
}
