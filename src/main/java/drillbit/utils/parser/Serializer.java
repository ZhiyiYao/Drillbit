package drillbit.utils.parser;

import com.google.protobuf.InvalidProtocolBufferException;
import drillbit.protobuf.ParameterPb;
import drillbit.utils.common.DoubleAccumulator;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public final class Serializer {
    public static ConcurrentHashMap<Object, DoubleAccumulator> accumulatedFromByteArray(byte[] bytes) {
        ConcurrentHashMap<Object, DoubleAccumulator> accumulated = new ConcurrentHashMap<>();
        try {
            List<ParameterPb.Accumulator.Item> items = ParameterPb.Accumulator.parseFrom(bytes).getAccumulatedList();
            for (ParameterPb.Accumulator.Item item : items) {
                DoubleAccumulator accumulator = new DoubleAccumulator(item.getValue());
                accumulator.setCount(item.getCount());
                //System.out.println("recieve: "+item.getCount()+" "+item.getValue());
                accumulated.put(item.getFeature(), accumulator);
            }
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }

        return accumulated;
    }

    public static byte[] accumulatedToByteArray(ConcurrentHashMap<Object, DoubleAccumulator> accumulated) {
        ParameterPb.Accumulator.Builder builder = ParameterPb.Accumulator.newBuilder();
        for (ConcurrentHashMap.Entry<Object, DoubleAccumulator> entry : accumulated.entrySet()) {
            ParameterPb.Accumulator.Item.Builder itemBuilder = ParameterPb.Accumulator.Item.newBuilder();
            itemBuilder.setFeature((String) entry.getKey());
            itemBuilder.setValue(entry.getValue().get());
            itemBuilder.setCount(entry.getValue().getCount());
            builder.addAccumulated(itemBuilder.build());
        }

        return builder.build().toByteArray();
    }
}
