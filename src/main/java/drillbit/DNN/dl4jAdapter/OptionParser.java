package drillbit.DNN.dl4jAdapter;

import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.learning.config.AdaGrad;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.learning.config.IUpdater;
import org.nd4j.linalg.learning.config.Sgd;
import org.nd4j.linalg.lossfunctions.LossFunctions;

public class OptionParser {
    static public IUpdater ParseOptimizer(String opt, double lr) {
        if ("ADAGRAD".equals(opt)) {
            return new AdaGrad(lr);
        } else if ("ADAM".equals(opt)) {
            return new Adam(lr);
        } else {
            return new Sgd(lr);
        }
    }

    static public Activation ParseActivation(String act) {
        if ("GELU".equals(act)) {
            return Activation.GELU;
        } else if ("TANH".equals(act)) {
            return Activation.TANH;
        } else if ("SIGMOID".equals(act)) {
            return Activation.SIGMOID;
        } else {
            return Activation.RELU;
        }
    }

    static public LossFunctions.LossFunction ParseLossFunction(String loss) {
        if ("LOGITS".equals(loss)) {
            return LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD;
        } else if ("HINGE".equals(loss)) {
            return LossFunctions.LossFunction.HINGE;
        } else if ("MSE".equals(loss)) {
            return LossFunctions.LossFunction.MSE;
        } else if ("MAE".equals(loss)) {
            return LossFunctions.LossFunction.MEAN_ABSOLUTE_ERROR;
        } else {
            return LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD;
        }
    }
}
