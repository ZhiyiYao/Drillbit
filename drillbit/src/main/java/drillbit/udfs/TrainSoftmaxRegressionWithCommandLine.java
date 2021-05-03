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
        name = "train_softmax_regression",
        scope = FunctionTemplate.FunctionScope.POINT_AGGREGATE,
        nulls = FunctionTemplate.NullHandling.INTERNAL
)
public class TrainSoftmaxRegressionWithCommandLine implements DrillAggFunc {
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

    }

    @Override
    public void add() {

    }

    @Override
    public void output() {

    }

    @Override
    public void reset() {

    }
}
