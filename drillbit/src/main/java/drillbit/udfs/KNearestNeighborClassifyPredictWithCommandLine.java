package drillbit.udfs;

import javax.inject.Inject;

import org.apache.drill.exec.expr.DrillSimpleFunc;
import org.apache.drill.exec.expr.annotations.FunctionTemplate;
import org.apache.drill.exec.expr.annotations.Output;
import org.apache.drill.exec.expr.annotations.Param;
import org.apache.drill.exec.expr.annotations.Workspace;
import org.apache.drill.exec.expr.holders.NullableVarCharHolder;
import org.apache.drill.exec.expr.holders.ObjectHolder;
import org.apache.drill.exec.expr.holders.VarCharHolder;

import com.google.protobuf.InvalidProtocolBufferException;

import io.netty.buffer.DrillBuf;

@FunctionTemplate(
		name = "knn_classification_predict",
		nulls = FunctionTemplate.NullHandling.INTERNAL,
		scope = FunctionTemplate.FunctionScope.POINT_AGGREGATE
)
public class KNearestNeighborClassifyPredictWithCommandLine implements DrillSimpleFunc {

	@Param
	NullableVarCharHolder featureHolder;

	@Param
	VarCharHolder modelHolder;

	@Param(constant = true)
	VarCharHolder commandLineHolder;

	@Inject
	DrillBuf buffer;

	@Output
	VarCharHolder targetHolder;

	@Workspace
	ObjectHolder learnerHolder;

	@Override
	public void setup() {
		learnerHolder = new ObjectHolder();
		learnerHolder.obj = null;
	}

	@Override
	public void eval() {
		if (learnerHolder.obj != null) {
			String model = org.apache.drill.exec.expr.fn.impl.StringFunctionHelpers.toStringFromUTF8(modelHolder.start, modelHolder.end, modelHolder.buffer);
			learnerHolder.obj = new drillbit.neighbors.KNeighborClassificationLearner();
			try {
				((drillbit.neighbors.KNeighborClassificationLearner) learnerHolder.obj).fromByteArray(model.getBytes());
			} catch (InvalidProtocolBufferException e) {
				e.printStackTrace();
			}
		}

		String feature = org.apache.drill.exec.expr.fn.impl.StringFunctionHelpers.toStringFromUTF8(featureHolder.start, featureHolder.end, featureHolder.buffer);
		String commandLine = org.apache.drill.exec.expr.fn.impl.StringFunctionHelpers.toStringFromUTF8(commandLineHolder.start, commandLineHolder.end, commandLineHolder.buffer);
		Object output = ((drillbit.neighbors.KNeighborClassificationLearner) learnerHolder.obj).predict(feature, commandLine);
		byte[] outputBytes = output.toString().getBytes();

		buffer = targetHolder.buffer = buffer.reallocIfNeeded(outputBytes.length);
		targetHolder.start = 0;
		targetHolder.end = outputBytes.length;
		targetHolder.buffer.setBytes(0, outputBytes);
	}

}
