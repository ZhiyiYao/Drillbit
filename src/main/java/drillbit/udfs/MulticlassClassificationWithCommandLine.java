package drillbit.udfs;

import org.apache.drill.exec.expr.DrillAggFunc;
import org.apache.drill.exec.expr.annotations.FunctionTemplate;

@FunctionTemplate(
        name = "train_multilclass_classification",
        scope = FunctionTemplate.FunctionScope.POINT_AGGREGATE,
        nulls = FunctionTemplate.NullHandling.INTERNAL
)
public class MulticlassClassificationWithCommandLine implements DrillAggFunc {


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
