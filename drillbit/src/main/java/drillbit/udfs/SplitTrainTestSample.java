package drillbit.udfs;

import drillbit.utils.udf.TrainTestSampleSplitter;
import io.netty.buffer.DrillBuf;
import org.apache.drill.exec.expr.DrillSimpleFunc;
import org.apache.drill.exec.expr.annotations.FunctionTemplate;
import org.apache.drill.exec.expr.annotations.Output;
import org.apache.drill.exec.expr.annotations.Param;
import org.apache.drill.exec.expr.holders.Float8Holder;
import org.apache.drill.exec.expr.holders.VarCharHolder;

import javax.inject.Inject;

@FunctionTemplate(
        name = "split_train_test_sample",
        scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL
)
public class SplitTrainTestSample implements DrillSimpleFunc {
    @Param
    VarCharHolder targetIn;

    @Param(constant = true)
    Float8Holder ratio;

    @Output
    VarCharHolder out;

    @Inject
    DrillBuf buffer;

    @Override
    public void setup() {
    }

    @Override
    public void eval() {
        String label = TrainTestSampleSplitter.judge(ratio.value) ? "train" : "test";
        out.buffer = buffer;
        out.start = 0;
        out.end = label.getBytes().length;
        out.buffer.setBytes(0, label.getBytes());
    }
}
