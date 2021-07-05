package drillbit.udfs;

import io.netty.buffer.DrillBuf;
import org.apache.drill.exec.expr.DrillSimpleFunc;
import org.apache.drill.exec.expr.annotations.FunctionTemplate;
import org.apache.drill.exec.expr.annotations.Output;
import org.apache.drill.exec.expr.annotations.Param;
import org.apache.drill.exec.expr.annotations.Workspace;
import org.apache.drill.exec.expr.holders.NullableVarCharHolder;

import javax.inject.Inject;

@FunctionTemplate(
        name = "extract_target",
        scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL
)
public class ExtractTargetFromFeatureAndTarget implements DrillSimpleFunc {
    @Param
    NullableVarCharHolder featureAndTarget;

    @Output
    NullableVarCharHolder feature;

    @Workspace
    drillbit.data.DatasetHelper.FeatureAndTargetHelper helper;

    @Inject
    DrillBuf buffer;

    @Override
    public void setup() {
        helper = new drillbit.data.DatasetHelper.FeatureAndTargetHelper();
    }

    @Override
    public void eval() {
        String featureAndTargetString = org.apache.drill.exec.expr.fn.impl.StringFunctionHelpers.toStringFromUTF8(featureAndTarget.start, featureAndTarget.end, featureAndTarget.buffer);
        String featureTarget = helper.extractTarget(featureAndTargetString);
        byte[] featureTargetBytes = featureTarget.getBytes();

        feature.isSet = 1;
        buffer = feature.buffer = buffer.reallocIfNeeded(featureTargetBytes.length);
        feature.start = 0;
        feature.end = featureTargetBytes.length;
        feature.buffer.setBytes(0, featureTargetBytes);
    }
}