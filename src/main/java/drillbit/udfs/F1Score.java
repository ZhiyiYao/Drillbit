package drillbit.udfs;

import drillbit.metrics.BinaryConfusionMatrix;
import org.apache.drill.exec.expr.DrillAggFunc;
import org.apache.drill.exec.expr.annotations.FunctionTemplate;
import org.apache.drill.exec.expr.annotations.Output;
import org.apache.drill.exec.expr.annotations.Param;
import org.apache.drill.exec.expr.annotations.Workspace;
import org.apache.drill.exec.expr.holders.*;

@FunctionTemplate(
        name = "f1",
        scope = FunctionTemplate.FunctionScope.POINT_AGGREGATE,
        nulls = FunctionTemplate.NullHandling.INTERNAL
)

public class F1Score implements DrillAggFunc {
    @Param
    NullableVarCharHolder yPred;

    @Param
    NullableVarCharHolder yTrue;

    @Param
    IntHolder rate;

    @Workspace
    ObjectHolder confusionMatrixHolder;

    @Output
    Float8Holder result;

    @Override
    public void setup() {
        confusionMatrixHolder = new ObjectHolder();
        confusionMatrixHolder.obj = new BinaryConfusionMatrix();
    }

    @Override
    public void add() {
//        if (yPred.isSet == 1 && yTrue.isSet == 1) {
//            String predString = org.apache.drill.exec.expr.fn.impl.StringFunctionHelpers.toStringFromUTF8(yPred.start, yPred.end, yPred.buffer);
//            String trueString = org.apache.drill.exec.expr.fn.impl.StringFunctionHelpers.toStringFromUTF8(yTrue.start, yTrue.end, yTrue.buffer);
//            double predValue = drillbit.utils.primitive.StringParser.parseDouble(predString, 0);
//            double trueValue = drillbit.utils.primitive.StringParser.parseDouble(trueString, 0);
//
//            ((BinaryConfusionMatrix) confusionMatrixHolder.obj).add(predValue, trueValue);
//            ((BinaryConfusionMatrix) confusionMatrixHolder.obj).setRate(rate.value);
//        }
    }

    @Override
    public void output() {
//        ((BinaryConfusionMatrix) confusionMatrixHolder.obj).setR();
//
//        double tp = ((BinaryConfusionMatrix) confusionMatrixHolder.obj).tp;
//        double tn = ((BinaryConfusionMatrix) confusionMatrixHolder.obj).tn;
//        double fp = ((BinaryConfusionMatrix) confusionMatrixHolder.obj).fp;
//        double fn = ((BinaryConfusionMatrix) confusionMatrixHolder.obj).fn;
//        double p = tp / (tp + fp);
//        double r = tp / (tp + fn);
//
//        result.value = 2 * p * r / (p + r);
    }

    @Override
    public void reset() {
//        ((BinaryConfusionMatrix) confusionMatrixHolder.obj).clear();
    }

}
