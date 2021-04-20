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
		name = "train_knn_classification",
		nulls = FunctionTemplate.NullHandling.INTERNAL,
		scope = FunctionTemplate.FunctionScope.POINT_AGGREGATE
)
public class KNearestNeighborClassificationWithCommandLine implements DrillAggFunc {

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

	@Override
	public void setup() {
		learnerHolder = new ObjectHolder();
		learnerHolder.obj = new drillbit.neighbors.KNeighborClassificationLearner();
	}

	@Override
	public void add() {
		if (featureHolder.isSet == 0 || targetHolder.isSet == 0) {
			return;
		}
		String feature = org.apache.drill.exec.expr.fn.impl.StringFunctionHelpers.toStringFromUTF8(featureHolder.start, featureHolder.end, featureHolder.buffer);
		String target = org.apache.drill.exec.expr.fn.impl.StringFunctionHelpers.toStringFromUTF8(targetHolder.start, targetHolder.end, targetHolder.buffer);
		String commandLine = org.apache.drill.exec.expr.fn.impl.StringFunctionHelpers.toStringFromUTF8(commandLineHolder.start, commandLineHolder.end, commandLineHolder.buffer);
		((drillbit.neighbors.KNeighborClassificationLearner) learnerHolder.obj).add(feature, target, commandLine);
	}

	@Override
	public void output() {
		byte[] modelBytes = ((drillbit.neighbors.KNeighborClassificationLearner) learnerHolder.obj).output();

		modelHolder.isSet = 1;
		buffer = modelHolder.buffer = buffer.reallocIfNeeded(modelBytes.length);
		modelHolder.start = 0;
		modelHolder.end = modelBytes.length;
		modelHolder.buffer.setBytes(0, modelBytes);
	}

	@Override
	public void reset() {
		((drillbit.neighbors.KNeighborClassificationLearner) learnerHolder.obj).reset();
	}

}
