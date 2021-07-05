package drillbit.udfs;

import io.netty.buffer.DrillBuf;
import org.apache.drill.exec.expr.DrillSimpleFunc;
import org.apache.drill.exec.expr.annotations.FunctionTemplate;
import org.apache.drill.exec.expr.annotations.Output;
import org.apache.drill.exec.expr.annotations.Param;
import org.apache.drill.exec.expr.annotations.Workspace;
import org.apache.drill.exec.expr.holders.*;

import javax.inject.Inject;

@FunctionTemplate(
        name = "practice_general_regression",
        scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL
)
public class PracticeGeneralRegression implements DrillSimpleFunc {
    @Param
    NullableVarCharHolder featureHolder;

    @Param
    NullableVarCharHolder learnerByteHolder;

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
                learnerHolder.obj = (new drillbit.regression.GeneralRegressionLearner()).fromByteArray(learnerBytes);
            }
            catch (com.google.protobuf.InvalidProtocolBufferException e) {
                e.printStackTrace();
            }
        }

        String feature = org.apache.drill.exec.expr.fn.impl.StringFunctionHelpers.toStringFromUTF8(featureHolder.start, featureHolder.end, featureHolder.buffer);
        String result = (String) ((drillbit.regression.GeneralRegressionLearner) learnerHolder.obj).predict(feature, "");
        byte[] resultBytes = result.getBytes();

        resultHolder.isSet = 1;
        buffer = resultHolder.buffer = buffer.reallocIfNeeded(resultBytes.length);
        resultHolder.start = 0;
        resultHolder.end = resultBytes.length;
        resultHolder.buffer.setBytes(0, resultBytes);
    }
}
