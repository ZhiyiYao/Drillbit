package drillbit.udfs;

import io.netty.buffer.DrillBuf;
import org.apache.drill.exec.expr.DrillSimpleFunc;
import org.apache.drill.exec.expr.annotations.FunctionTemplate;
import org.apache.drill.exec.expr.annotations.Output;
import org.apache.drill.exec.expr.annotations.Param;
import org.apache.drill.exec.expr.annotations.Workspace;
import org.apache.drill.exec.expr.holders.NullableVarCharHolder;

import javax.inject.Inject;

@FunctionTemplate(
        name = "load_iris_dataset",
        scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL
)
public class LoadIrisDatasetWithoutOptions implements DrillSimpleFunc {
    @Param
    NullableVarCharHolder unusedInput;

    @Output
    NullableVarCharHolder featureAndTarget;

    @Workspace
    drillbit.data.IrisDataset dataset;

    @Inject
    DrillBuf buffer;

    @Override
    public void setup() {
        dataset = new drillbit.data.IrisDataset();
    }

    @Override
    public void eval() {
        if (!dataset.optionProcessed) {
            dataset.processOptions("");
        }

        String oneSample = dataset.loadOneSample();;

        featureAndTarget.isSet = 1;
        featureAndTarget.buffer = buffer;
        featureAndTarget.start = 0;
        featureAndTarget.end = oneSample.getBytes().length;
        featureAndTarget.buffer.setBytes(0, oneSample.getBytes());
    }
}
