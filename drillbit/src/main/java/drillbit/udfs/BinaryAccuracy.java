package drillbit.udfs;

import org.apache.drill.exec.expr.DrillAggFunc;
import org.apache.drill.exec.expr.annotations.Output;
import org.apache.drill.exec.expr.annotations.Param;
import org.apache.drill.exec.expr.annotations.Workspace;
import org.apache.drill.exec.expr.holders.NullableVarCharHolder;
import org.apache.drill.exec.expr.holders.Float8Holder;
import org.apache.drill.exec.expr.holders.UInt8Holder;
import org.apache.drill.exec.expr.annotations.FunctionTemplate;

@FunctionTemplate(
        name = "acc",
        scope = FunctionTemplate.FunctionScope.POINT_AGGREGATE,
        nulls = FunctionTemplate.NullHandling.INTERNAL
)
public class BinaryAccuracy implements DrillAggFunc {
    @Param
    NullableVarCharHolder y_pred;

    @Param
    NullableVarCharHolder y_true;

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
        String pred_s = org.apache.drill.exec.expr.fn.impl.StringFunctionHelpers.toStringFromUTF8(y_pred.start, y_pred.end, y_pred.buffer);
        String true_s = org.apache.drill.exec.expr.fn.impl.StringFunctionHelpers.toStringFromUTF8(y_true.start, y_true.end, y_true.buffer);
        double pred_d = drillbit.utils.parser.StringParser.parseDouble(pred_s, 0);
        double true_d = drillbit.utils.parser.StringParser.parseDouble(true_s, 0);
        if (pred_d == true_d) {
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
