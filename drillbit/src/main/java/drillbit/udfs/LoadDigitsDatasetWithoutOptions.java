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
        name = "load_digits_dataset",
        scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL
)
public class LoadDigitsDatasetWithoutOptions implements DrillSimpleFunc {
    @Param
    NullableVarCharHolder unusedInput;

    @Output
    NullableVarCharHolder featureAndTarget;

    @Workspace
    drillbit.data.DigitsDataset dataset;

    @Inject
    DrillBuf buffer;

    @Override
    public void setup() {
        dataset = new drillbit.data.DigitsDataset();
    }

    @Override
    public void eval() {
        if (!dataset.optionProcessed) {
            dataset.processOptions("");
        }

        byte[] sampleBytes = dataset.loadOneSample().getBytes();

        featureAndTarget.isSet = 1;
        buffer = featureAndTarget.buffer = buffer.reallocIfNeeded(sampleBytes.length);
        featureAndTarget.start = 0;
        featureAndTarget.end = sampleBytes.length;
        featureAndTarget.buffer.setBytes(0, sampleBytes);
    }
}
