package drillbit.DNN.dl4jAdapter;

import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.api.ndarray.INDArray;

public class DistributedMultiLayerNetwork extends MultiLayerNetwork {
    public DistributedMultiLayerNetwork(MultiLayerConfiguration conf) {
        super(conf);
    }

    public DistributedMultiLayerNetwork(String conf, INDArray params) {
        super(conf, params);
    }

    public DistributedMultiLayerNetwork(MultiLayerConfiguration conf, INDArray params) {
        super(conf, params);
    }
}
