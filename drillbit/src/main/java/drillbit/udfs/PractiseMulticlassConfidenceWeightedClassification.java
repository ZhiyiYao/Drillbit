package drillbit.udfs;

import com.google.protobuf.InvalidProtocolBufferException;
import org.apache.drill.exec.expr.DrillSimpleFunc;
import org.apache.drill.exec.expr.annotations.FunctionTemplate;
import org.apache.drill.exec.expr.annotations.Output;
import org.apache.drill.exec.expr.annotations.Param;
import org.apache.drill.exec.expr.annotations.Workspace;
import org.apache.drill.exec.expr.holders.NullableVarCharHolder;
import org.apache.drill.exec.expr.holders.ObjectHolder;
import org.apache.drill.exec.expr.holders.UInt8Holder;
import org.apache.drill.exec.expr.holders.VarCharHolder;

@FunctionTemplate(
        name = "practise_multiclass_confidence_weighted_classification",
        scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL
)
public class PractiseMulticlassConfidenceWeightedClassification implements DrillSimpleFunc {
    @Param
    NullableVarCharHolder featureHolder;

    @Param(constant = true)
    VarCharHolder learnerByteHolder;

    @Output
    UInt8Holder resultHolder;

    @Workspace
    ObjectHolder learnerHolder;

    @Override
    public void setup() {
        learnerHolder = null;
    }

    @Override
    public void eval() {
        if (learnerHolder == null) {
            byte[] learnerBytes = new byte[learnerByteHolder.end - learnerByteHolder.start];
            learnerByteHolder.buffer.getBytes(learnerByteHolder.start, learnerBytes);

            learnerHolder = new ObjectHolder();
            try {
                learnerHolder.obj = (new drillbit.classification.multiclass.MulticlassConfidenceWeightedClassificationLearner()).fromByteArray(learnerBytes);
            }
            catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            }
        }

        String feature = org.apache.drill.exec.expr.fn.impl.StringFunctionHelpers.toStringFromUTF8(featureHolder.start, featureHolder.end, featureHolder.buffer);
        resultHolder.value = (int) ((drillbit.classification.multiclass.MulticlassConfidenceWeightedClassificationLearner) learnerHolder.obj).predict(feature, "");
    }
}
