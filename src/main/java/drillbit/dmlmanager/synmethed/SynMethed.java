package drillbit.dmlmanager.synmethed;

import drillbit.dmlmanager.SynData;
import drillbit.optimizer.Optimizers;
import drillbit.parameter.Weights;

import java.util.ArrayList;

public interface SynMethed {
    ArrayList<Weights> update(SynData accumulated, ArrayList<String> labels, ArrayList<Weights> weightsArray, Optimizers.OptimizerBase op);
}
