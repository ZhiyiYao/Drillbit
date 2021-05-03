package drillbit.udfs;

import io.netty.buffer.DrillBuf;
import org.apache.drill.exec.expr.DrillSimpleFunc;
import org.apache.drill.exec.expr.annotations.FunctionTemplate;
import org.apache.drill.exec.expr.annotations.Output;
import org.apache.drill.exec.expr.annotations.Param;
import org.apache.drill.exec.expr.holders.NullableVarCharHolder;

import javax.inject.Inject;

@FunctionTemplate(
        name = "add_index",
        scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL
)
public class AddIndex implements DrillSimpleFunc {
    @Param
    NullableVarCharHolder featureHolder;

    @Output
    NullableVarCharHolder featureWithIndexHolder;

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
        String featureWithIndex = drillbit.data.FeatureHelper.addIndex(feature);
        byte[] featureWithIndexBytes = featureWithIndex.getBytes();

        featureWithIndexHolder.isSet = 1;
        buffer = featureWithIndexHolder.buffer = buffer.reallocIfNeeded(featureWithIndexBytes.length);
        featureWithIndexHolder.start = 0;
        featureWithIndexHolder.end = featureWithIndexBytes.length;
        featureWithIndexHolder.buffer.setBytes(0, featureWithIndexBytes);
    }
}
