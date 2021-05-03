package drillbit.udfs;

import io.netty.buffer.DrillBuf;
import org.apache.drill.exec.expr.DrillSimpleFunc;
import org.apache.drill.exec.expr.annotations.FunctionTemplate;
import org.apache.drill.exec.expr.annotations.Output;
import org.apache.drill.exec.expr.annotations.Param;
import org.apache.drill.exec.expr.holders.NullableVarCharHolder;

import javax.inject.Inject;

@FunctionTemplate(
        name = "add_bias",
        scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL
)
public class AddBias implements DrillSimpleFunc {
    @Param
    NullableVarCharHolder featureHolder;

    @Output
    NullableVarCharHolder featureWithBiasHolder;

    @Inject
    DrillBuf buffer;

    @Override
    public void setup() {
    }

    @Override
    public void eval() {
        if (featureHolder.isSet != 1) {
            return;
        }
        String feature = org.apache.drill.exec.expr.fn.impl.StringFunctionHelpers.toStringFromUTF8(featureHolder.start, featureHolder.end, featureHolder.buffer);
        String featureWithBias = drillbit.data.FeatureHelper.addBias(feature);
        byte[] featureWithBiasBytes = featureWithBias.getBytes();

        featureWithBiasHolder.isSet = 1;
        buffer = featureWithBiasHolder.buffer = buffer.reallocIfNeeded(featureWithBiasBytes.length);
        featureWithBiasHolder.start = 0;
        featureWithBiasHolder.end = featureWithBiasBytes.length;
        featureWithBiasHolder.buffer.setBytes(0, featureWithBiasBytes);
    }
}
