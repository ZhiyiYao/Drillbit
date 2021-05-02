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
        name = "train_multiclass_confidence_weighted_classification",
        scope = FunctionTemplate.FunctionScope.POINT_AGGREGATE,
        nulls = FunctionTemplate.NullHandling.INTERNAL
)
public class TrainMulticlassConfidenceWeightedClassificationWithoutCommandLine implements DrillAggFunc {
    @Param
    NullableVarCharHolder featureHolder;

    @Param
    NullableVarCharHolder targetHolder;

    @Inject
    DrillBuf buffer;

    @Output
    NullableVarCharHolder modelHolder;

    @Workspace
    ObjectHolder learnerHolder;

    @Override
    public void setup() {
        learnerHolder = new ObjectHolder();
        learnerHolder.obj = new drillbit.classification.multiclass.MulticlassConfidenceWeightedClassificationLearner();
    }

    @Override
    public void add() {
        if (featureHolder.isSet == 0 || targetHolder.isSet == 0) {
            return;
        }
        String feature = org.apache.drill.exec.expr.fn.impl.StringFunctionHelpers.toStringFromUTF8(featureHolder.start, featureHolder.end, featureHolder.buffer);
        String target = org.apache.drill.exec.expr.fn.impl.StringFunctionHelpers.toStringFromUTF8(targetHolder.start, targetHolder.end, targetHolder.buffer);
        ((drillbit.classification.multiclass.MulticlassConfidenceWeightedClassificationLearner) learnerHolder.obj).add(feature, target);
    }

    @Override
    public void output() {
        byte[] modelBytes = ((drillbit.classification.multiclass.MulticlassConfidenceWeightedClassificationLearner) learnerHolder.obj).output("");

        buffer = modelHolder.buffer = buffer.reallocIfNeeded(modelBytes.length);
        modelHolder.start = 0;
        modelHolder.end = modelBytes.length;
        modelHolder.buffer.setBytes(0, modelBytes);
        modelHolder.isSet = 1;
    }

    @Override
    public void reset() {
        ((drillbit.classification.multiclass.MulticlassConfidenceWeightedClassificationLearner) learnerHolder.obj).reset();
    }
}
