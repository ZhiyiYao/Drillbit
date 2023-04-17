package drillbit.DNN.dl4jAdapter;

import org.deeplearning4j.nn.api.Model;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.gradient.Gradient;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.workspace.LayerWorkspaceMgr;
import org.deeplearning4j.optimize.api.StepFunction;
import org.deeplearning4j.optimize.api.TrainingListener;
import org.deeplearning4j.optimize.solvers.BaseOptimizer;
import org.deeplearning4j.optimize.solvers.StochasticGradientDescent;
import org.nd4j.common.primitives.Pair;
import org.nd4j.linalg.api.memory.MemoryWorkspace;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.util.Collection;
import java.util.Iterator;

public class DistributedStochasticGradientDescent extends StochasticGradientDescent {

    public DistributedStochasticGradientDescent(NeuralNetConfiguration conf, StepFunction stepFunction, Collection<TrainingListener> trainingListeners, Model model) {
        super(conf, stepFunction, trainingListeners, model);
    }

    @Override
    public boolean optimize(LayerWorkspaceMgr workspaceMgr){
        if (this.accumulator != null && this.accumulator.hasAnything()) {
            this.accumulator.applyUpdate(this.stepFunction, this.model.params(), Nd4j.createUninitialized(this.model.params().shape(), this.model.params().ordering()), false);
        }

        Pair<Gradient, Double> pair = this.gradientAndScore(workspaceMgr);
        Gradient gradient = (Gradient)pair.getFirst();
        INDArray params = this.model.params();
        INDArray fullGrad = gradient.gradient();
        fullGrad = fullGrad.reshape(new long[]{fullGrad.length()});
        int iterationCount;
        int epochCount;
        if (this.accumulator != null) {
            iterationCount = 0;
            epochCount = 0;
            if (this.model instanceof MultiLayerNetwork) {
                epochCount = ((MultiLayerNetwork)this.model).getIterationCount();
                iterationCount = ((MultiLayerNetwork)this.model).getEpochCount();
            } else if (this.model instanceof ComputationGraph) {
                epochCount = ((ComputationGraph)this.model).getIterationCount();
                iterationCount = ((ComputationGraph)this.model).getEpochCount();
            }

            this.accumulator.storeUpdate(fullGrad, epochCount, iterationCount);
            this.accumulator.applyUpdate(this.stepFunction, params, fullGrad, true);
        } else {
            this.stepFunction.step(params, fullGrad);
        }

        this.model.setParams(params);
        iterationCount = BaseOptimizer.getIterationCount(this.model);
        epochCount = BaseOptimizer.getEpochCount(this.model);
        MemoryWorkspace workspace = Nd4j.getMemoryManager().scopeOutOfWorkspaces();

        try {
            Iterator var9 = this.trainingListeners.iterator();

            while(var9.hasNext()) {
                TrainingListener listener = (TrainingListener)var9.next();
                listener.iterationDone(this.model, iterationCount, epochCount);
            }
        } catch (Throwable var12) {
            if (workspace != null) {
                try {
                    workspace.close();
                } catch (Throwable var11) {
                    var12.addSuppressed(var11);
                }
            }

            throw var12;
        }

        if (workspace != null) {
            workspace.close();
        }

        BaseOptimizer.incrementIterationCount(this.model, 1);
        applyConstraints(this.model);
        return true;
    }
}
