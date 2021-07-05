package drillbit.udfs;

import drillbit.neighbors.KNeighborsClassificationLearner;
import io.netty.buffer.DrillBuf;
import org.apache.drill.exec.expr.DrillSimpleFunc;
import org.apache.drill.exec.expr.annotations.FunctionTemplate;
import org.apache.drill.exec.expr.annotations.Output;
import org.apache.drill.exec.expr.annotations.Param;
import org.apache.drill.exec.expr.annotations.Workspace;
import org.apache.drill.exec.expr.holders.NullableVarCharHolder;
import org.apache.drill.exec.expr.holders.ObjectHolder;
import org.apache.drill.exec.expr.holders.VarCharHolder;

import javax.inject.Inject;

@FunctionTemplate(
        name = "practice_k_neighbors_classification",
        scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL
)
public class PracticeKNeighborsClassificationWithCommandLine implements DrillSimpleFunc {
    @Param
    NullableVarCharHolder featureHolder;

    @Param
    NullableVarCharHolder learnerByteHolder;

    @Param(constant = true)
    VarCharHolder commandLineHolder;

    @Output
    NullableVarCharHolder resultHolder;

    @Workspace
    ObjectHolder learnerHolder;

    @Inject
    DrillBuf buffer;

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
                learnerHolder.obj = (new drillbit.neighbors.KNeighborsClassificationLearner()).fromByteArray(learnerBytes);
            }
            catch (com.google.protobuf.InvalidProtocolBufferException e) {
                e.printStackTrace();
            }
        }

        String feature = org.apache.drill.exec.expr.fn.impl.StringFunctionHelpers.toStringFromUTF8(featureHolder.start, featureHolder.end, featureHolder.buffer);
        String predicted = (String) ((drillbit.neighbors.KNeighborsClassificationLearner) learnerHolder.obj).predict(feature, org.apache.drill.exec.expr.fn.impl.StringFunctionHelpers.toStringFromUTF8(commandLineHolder.start, commandLineHolder.end, commandLineHolder.buffer));
        byte[] predictedBytes = predicted.getBytes();

        resultHolder.isSet = 1;
        buffer = resultHolder.buffer = buffer.reallocIfNeeded(predictedBytes.length);
        resultHolder.start = 0;
        resultHolder.end = predictedBytes.length;
        resultHolder.buffer.setBytes(0, predictedBytes);
    }
}
