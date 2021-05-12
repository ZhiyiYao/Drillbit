package drillbit.udfs;

import org.apache.drill.exec.expr.DrillAggFunc;
import org.apache.drill.exec.expr.annotations.FunctionTemplate;
import org.apache.drill.exec.expr.annotations.Output;
import org.apache.drill.exec.expr.annotations.Param;
import org.apache.drill.exec.expr.annotations.Workspace;
import org.apache.drill.exec.expr.holders.Float8Holder;
import org.apache.drill.exec.expr.holders.UInt8Holder;

@FunctionTemplate(
		name = "mae",
		scope = FunctionTemplate.FunctionScope.SIMPLE,
		nulls = FunctionTemplate.NullHandling.NULL_IF_NULL
)
public class MeanAbsoluteError implements DrillAggFunc {
	@Param
	Float8Holder yPred;

	@Param
	Float8Holder yTrue;

	@Workspace
	UInt8Holder count;

	@Workspace
	UInt8Holder squared;

	@Output
	Float8Holder result;

	@Override
	public void setup() {
		count = new UInt8Holder();
		count.value = 0;
		squared = new UInt8Holder();
		squared.value = 0;
	}

	@Override
	public void add() {
		double pred_d = yPred.value;
		double true_d = yTrue.value;
		squared.value += Math.abs(pred_d - true_d);
		count.value++;
	}

	@Override
	public void output() {
		result.value = (squared.value) / count.value;
	}

	@Override
	public void reset() {
		count.value = 0;
		squared.value = 0;
	}
}
