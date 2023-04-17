package drillbit.udfs;

import io.netty.buffer.DrillBuf;
import org.apache.drill.exec.expr.DrillAggFunc;
import org.apache.drill.exec.expr.annotations.FunctionTemplate;
import org.apache.drill.exec.expr.annotations.Output;
import org.apache.drill.exec.expr.annotations.Param;
import org.apache.drill.exec.expr.annotations.Workspace;
import org.apache.drill.exec.expr.holders.NullableVarCharHolder;
import org.apache.drill.exec.expr.holders.ObjectHolder;

import javax.inject.Inject;

@FunctionTemplate(
        name = "cm",
        scope = FunctionTemplate.FunctionScope.POINT_AGGREGATE,
        nulls = FunctionTemplate.NullHandling.INTERNAL
)
public class ConfusionMatrixWithoutOptions implements DrillAggFunc {
    @Param
    NullableVarCharHolder actual;

    @Param
    NullableVarCharHolder predicted;

    @Output
    NullableVarCharHolder output;

    @Workspace
    ObjectHolder confusionMatrixHolder;

    @Inject
    DrillBuf buffer;

    @Override
    public void setup() {
        confusionMatrixHolder = new ObjectHolder();
        confusionMatrixHolder.obj = new drillbit.metrics.ConfusionMatrix();
    }

    @Override
    public void add() {
        if (actual.isSet != 1 || predicted.isSet != 1) {
            return;
        }

        String actualString = org.apache.drill.exec.expr.fn.impl.StringFunctionHelpers.toStringFromUTF8(actual.start, actual.end, actual.buffer);
        String predictedString = org.apache.drill.exec.expr.fn.impl.StringFunctionHelpers.toStringFromUTF8(predicted.start, predicted.end, predicted.buffer);
        ((drillbit.metrics.ConfusionMatrix) confusionMatrixHolder.obj).add(actualString, predictedString, "");
    }

    @Override
    public void output() {
        String confusionMatrix = (String) ((drillbit.metrics.ConfusionMatrix) confusionMatrixHolder.obj).output();
        byte[] confusionMatrixBytes = confusionMatrix.getBytes();

        output.isSet = 1;
        buffer = output.buffer = buffer.reallocIfNeeded(confusionMatrixBytes.length);
        output.start = 0;
        output.end = confusionMatrixBytes.length;
        output.buffer.setBytes(0, confusionMatrixBytes);
    }

    @Override
    public void reset() {
        ((drillbit.metrics.ConfusionMatrix) confusionMatrixHolder.obj).reset();
    }
}
