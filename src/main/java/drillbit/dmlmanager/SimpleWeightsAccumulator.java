package drillbit.dmlmanager;

import drillbit.utils.common.DoubleAccumulator;
import drillbit.utils.parser.ObjectParser;
import drillbit.utils.parser.Serializer;

import java.util.concurrent.ConcurrentHashMap;

public class SimpleWeightsAccumulator implements SynData {
    private ConcurrentHashMap<Object, DoubleAccumulator> accumulated;

    public SimpleWeightsAccumulator(ConcurrentHashMap<Object, DoubleAccumulator> input) {
        accumulated = input;
    }

    public SimpleWeightsAccumulator() {
        accumulated = new ConcurrentHashMap<>();
    }

    @Override
    public void addData(SynData data) {
        ConcurrentHashMap<Object, DoubleAccumulator> received = data.getAccumulation(null);
        for (ConcurrentHashMap.Entry<Object, DoubleAccumulator> e : received.entrySet()) {
            Object feature = e.getKey();
            DoubleAccumulator v = e.getValue();

            DoubleAccumulator acc = accumulated.get(ObjectParser.parseInt(feature));
            //System.out.println("recieve "+ObjectParser.parseInt(feature)+" acc: "+v.get());
            if (acc == null) {
                acc = new DoubleAccumulator(v.get());
                accumulated.put(feature, acc);
            } else {
                acc.add(v.get());
            }
        }
    }

    @Override
    public ConcurrentHashMap<Object, DoubleAccumulator> getAccumulation(String label) {
        return accumulated;
    }

    @Override
    public byte[] getBytes() {
        return Serializer.accumulatedToByteArray(accumulated);
    }

    @Override
    public void fromBytes(byte[] data) {
        accumulated = Serializer.accumulatedFromByteArray(data);
    }

    @Override
    public void clear() {
        accumulated.clear();
    }
}
