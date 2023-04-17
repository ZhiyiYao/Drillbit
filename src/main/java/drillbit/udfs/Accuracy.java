package drillbit.udfs;

import org.apache.drill.exec.expr.DrillAggFunc;
import org.apache.drill.exec.expr.annotations.FunctionTemplate;
import org.apache.drill.exec.expr.annotations.Output;
import org.apache.drill.exec.expr.annotations.Param;
import org.apache.drill.exec.expr.annotations.Workspace;
import org.apache.drill.exec.expr.holders.Float8Holder;
import org.apache.drill.exec.expr.holders.NullableVarCharHolder;
import org.apache.drill.exec.expr.holders.UInt8Holder;

@FunctionTemplate(
        name = "acc",
        scope = FunctionTemplate.FunctionScope.POINT_AGGREGATE,
        nulls = FunctionTemplate.NullHandling.INTERNAL
)
public class Accuracy implements DrillAggFunc {
    @Param
    NullableVarCharHolder yPred;

    @Param
    NullableVarCharHolder yTrue;

    @Workspace
    UInt8Holder count;

    @Workspace
    UInt8Holder trueCount;

    @Output
    Float8Holder result;

    @Override
    public void setup() {
        count = new UInt8Holder();
        count.value = 0;
        trueCount = new UInt8Holder();
        trueCount.value = 0;
    }

    @Override
    public void add() {
        String predS = org.apache.drill.exec.expr.fn.impl.StringFunctionHelpers.toStringFromUTF8(yPred.start, yPred.end, yPred.buffer).trim();
        String trueS = org.apache.drill.exec.expr.fn.impl.StringFunctionHelpers.toStringFromUTF8(yTrue.start, yTrue.end, yTrue.buffer).trim();
        if (trueS.equals(predS)) {
            trueCount.value++;
        }
        count.value++;
    }

    @Override
    public void output() {
        result.value = ((double) trueCount.value) / count.value;
    }

    @Override
    public void reset() {
        count.value = 0;
        trueCount.value = 0;
    }
}
