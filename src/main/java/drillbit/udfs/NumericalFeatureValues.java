package drillbit.udfs;

import io.netty.buffer.DrillBuf;
import org.apache.drill.exec.expr.DrillSimpleFunc;
import org.apache.drill.exec.expr.annotations.FunctionTemplate;
import org.apache.drill.exec.expr.annotations.Output;
import org.apache.drill.exec.expr.annotations.Param;
import org.apache.drill.exec.expr.holders.NullableVarCharHolder;
import org.apache.drill.exec.expr.holders.VarCharHolder;

import javax.inject.Inject;

@FunctionTemplate(
        name = "numerical_feature_values",
        scope = FunctionTemplate.FunctionScope.SIMPLE,
        isVarArg = true
)
public class NumericalFeatureValues implements DrillSimpleFunc {
    @Param(constant = true)
    VarCharHolder featureNames;

    @Param
    NullableVarCharHolder[] features;

    @Output
    NullableVarCharHolder out;

    @Inject
    DrillBuf buffer;

    @Override
    public void setup() {
    }

    @Override
    public void eval() {
        String namesString = org.apache.drill.exec.expr.fn.impl.StringFunctionHelpers.toStringFromUTF8(featureNames.start, featureNames.end, featureNames.buffer);
        String[] names = drillbit.utils.primitive.StringParser.parseList(namesString);
        String[] categoricalFeatures = new String[names.length];
        for (int i = 0; i < names.length; i++) {
            categoricalFeatures[i] = names[i] + ":" + org.apache.drill.exec.expr.fn.impl.StringFunctionHelpers.toStringFromUTF8(features[i].start, features[i].end, features[i].buffer);
        }

        out.isSet = 1;
        out.buffer = buffer;
        out.start = 0;
        out.end = java.util.Arrays.toString(categoricalFeatures).getBytes().length;
        out.buffer.setBytes(0, java.util.Arrays.toString(categoricalFeatures).getBytes());
    }
}
