package drillbit.udfs;

import org.apache.drill.exec.expr.DrillAggFunc;
import org.apache.drill.exec.expr.annotations.FunctionTemplate;
import org.apache.drill.exec.expr.annotations.Output;
import org.apache.drill.exec.expr.annotations.Param;
import org.apache.drill.exec.expr.annotations.Workspace;
import org.apache.drill.exec.expr.holders.Float8Holder;
import org.apache.drill.exec.expr.holders.NullableVarCharHolder;
import org.apache.drill.exec.expr.holders.ObjectHolder;


@FunctionTemplate(
        name = "roc_auc",
        scope = FunctionTemplate.FunctionScope.POINT_AGGREGATE,
        nulls = FunctionTemplate.NullHandling.INTERNAL
)
public class ReceiverOperatingCharacteristicAreaUnderCurve implements DrillAggFunc {
    @Param
    NullableVarCharHolder label;

    @Param
    NullableVarCharHolder score;

    @Workspace
    ObjectHolder aucHolder;

    @Output
    Float8Holder result;

    @Override
    public void setup() {
        aucHolder = new ObjectHolder();
        aucHolder.obj = new drillbit.metrics.AreaUnderCurve();
    }

    @Override
    public void add() {
        if (label.isSet == 1 && score.isSet == 1) {
            String labelString = org.apache.drill.exec.expr.fn.impl.StringFunctionHelpers.toStringFromUTF8(label.start, label.end, label.buffer);
            String scoreString = org.apache.drill.exec.expr.fn.impl.StringFunctionHelpers.toStringFromUTF8(score.start, score.end, score.buffer);

            double labelValue = drillbit.utils.primitive.StringParser.parseDouble(labelString, 0);
            double scoreValue = drillbit.utils.primitive.StringParser.parseDouble(scoreString, 0.d);

            ((drillbit.metrics.AreaUnderCurve) aucHolder.obj).add(labelValue, scoreValue, "-type roc");
        }
    }

    @Override
    public void output() {
        result.value = (double) ((drillbit.metrics.AreaUnderCurve) aucHolder.obj).output();
    }

    @Override
    public void reset() {
        ((drillbit.metrics.AreaUnderCurve) aucHolder.obj).reset();
    }
}
