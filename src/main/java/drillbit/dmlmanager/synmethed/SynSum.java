package drillbit.dmlmanager.synmethed;

import drillbit.dmlmanager.SynData;
import drillbit.optimizer.Optimizers;
import drillbit.parameter.Weights;
import drillbit.utils.common.DoubleAccumulator;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class SynSum implements SynMethed {

    @Override
    public ArrayList<Weights> update(SynData accumulated, ArrayList<String> labels, ArrayList<Weights> weightsArray, Optimizers.OptimizerBase op) {
        if (labels == null) {
            updateOneWeights(accumulated.getAccumulation(null),
                    weightsArray.get(0),
                    op);
        } else {
            for (int i = 0; i < labels.size(); i++) {
                updateOneWeights(accumulated.getAccumulation(labels.get(i)),
                        weightsArray.get(i),
                        op);
            }
        }

        return weightsArray;
    }

    private void updateOneWeights(ConcurrentHashMap<Object, DoubleAccumulator> accumulated,
                                  Weights weights,
                                  Optimizers.OptimizerBase op) {
        for (ConcurrentHashMap.Entry<Object, DoubleAccumulator> e : accumulated.entrySet()) {
            Object feature = e.getKey();
            DoubleAccumulator v = e.getValue();
            double value = v.getValue();
            //System.out.println("acc: "+value);
            double weight = weights.getWeight(feature);
            final double newWeight = op.update(feature, weight, 0.0, value);
            weights.setWeight(feature, newWeight);
        }
    }
}
