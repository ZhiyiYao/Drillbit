package drillbit.udfs;

import org.apache.drill.exec.expr.DrillAggFunc;
import org.apache.drill.exec.expr.annotations.FunctionTemplate;

@FunctionTemplate(
        name = "cm",
        scope = FunctionTemplate.FunctionScope.POINT_AGGREGATE,
        nulls = FunctionTemplate.NullHandling.INTERNAL
)
public class ConfusionMatrixWithoutOptions implements DrillAggFunc {
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
