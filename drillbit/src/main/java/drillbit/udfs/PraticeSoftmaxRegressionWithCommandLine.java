package drillbit.udfs;

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
        name = "practice_softmax_regression",
        scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL
)
public class PraticeSoftmaxRegressionWithCommandLine implements DrillSimpleFunc {
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
                learnerHolder.obj = (new drillbit.classification.multiclass.SoftmaxRegressionLearner()).fromByteArray(learnerBytes);
            }
            catch (com.google.protobuf.InvalidProtocolBufferException e) {
                e.printStackTrace();
            }
        }

        String feature = org.apache.drill.exec.expr.fn.impl.StringFunctionHelpers.toStringFromUTF8(featureHolder.start, featureHolder.end, featureHolder.buffer);
        String predicted = (String) ((drillbit.classification.multiclass.SoftmaxRegressionLearner) learnerHolder.obj).predict(feature, org.apache.drill.exec.expr.fn.impl.StringFunctionHelpers.toStringFromUTF8(commandLineHolder.start, commandLineHolder.end, commandLineHolder.buffer));

        resultHolder.isSet = 1;
        resultHolder.buffer = buffer;
        resultHolder.start = 0;
        resultHolder.end = predicted.getBytes().length;
        resultHolder.buffer.setBytes(0, predicted.getBytes());
    }
}
