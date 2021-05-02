package drillbit.udfs;

import io.netty.buffer.DrillBuf;
import org.apache.drill.exec.expr.DrillAggFunc;
import org.apache.drill.exec.expr.annotations.FunctionTemplate;
import org.apache.drill.exec.expr.annotations.Output;
import org.apache.drill.exec.expr.annotations.Param;
import org.apache.drill.exec.expr.annotations.Workspace;
import org.apache.drill.exec.expr.holders.NullableVarCharHolder;
import org.apache.drill.exec.expr.holders.ObjectHolder;
import org.apache.drill.exec.expr.holders.VarCharHolder;

import javax.inject.Inject;

@FunctionTemplate(
        name = "train_classification",
        nulls = FunctionTemplate.NullHandling.INTERNAL,
        scope = FunctionTemplate.FunctionScope.POINT_AGGREGATE
)
public class TrainGeneralClassificationWithCommandLine implements DrillAggFunc {
    @Param
    NullableVarCharHolder featureHolder;

    @Param
    NullableVarCharHolder targetHolder;

    @Param(constant = true)
    VarCharHolder commandLineHolder;

    @Inject
    DrillBuf buffer;

    @Output
    NullableVarCharHolder modelHolder;

    @Workspace
    ObjectHolder learnerHolder;

    @Workspace
    ObjectHolder optionsHolder;

    @Override
    public void setup() {
        learnerHolder = new ObjectHolder();
        learnerHolder.obj = new drillbit.classification.GeneralClassificationLearner();
        optionsHolder = new ObjectHolder();
        optionsHolder.obj = "";
    }

    @Override
    public void add() {
        if (featureHolder.isSet == 0 || targetHolder.isSet == 0) {
            return;
        }
        String feature = org.apache.drill.exec.expr.fn.impl.StringFunctionHelpers.toStringFromUTF8(featureHolder.start, featureHolder.end, featureHolder.buffer);
        String target = org.apache.drill.exec.expr.fn.impl.StringFunctionHelpers.toStringFromUTF8(targetHolder.start, targetHolder.end, targetHolder.buffer);
        if (optionsHolder.obj == "") {
            optionsHolder.obj = org.apache.drill.exec.expr.fn.impl.StringFunctionHelpers.toStringFromUTF8(commandLineHolder.start, commandLineHolder.end, commandLineHolder.buffer);
        }
        ((drillbit.classification.GeneralClassificationLearner) learnerHolder.obj).add(feature, target);
    }

    @Override
    public void output() {
        byte[] modelBytes = ((drillbit.classification.GeneralClassificationLearner) learnerHolder.obj).output((String) optionsHolder.obj);

        buffer = modelHolder.buffer = buffer.reallocIfNeeded(modelBytes.length);
        modelHolder.start = 0;
        modelHolder.end = modelBytes.length;
        modelHolder.buffer.setBytes(0, modelBytes);
        modelHolder.isSet = 1;
    }

    @Override
    public void reset() {
        ((drillbit.classification.GeneralClassificationLearner) learnerHolder.obj).reset();
    }
}
