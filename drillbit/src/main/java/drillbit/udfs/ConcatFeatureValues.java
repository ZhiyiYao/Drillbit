package drillbit.udfs;

import io.netty.buffer.DrillBuf;
import org.apache.drill.exec.expr.DrillSimpleFunc;
import org.apache.drill.exec.expr.annotations.FunctionTemplate;
import org.apache.drill.exec.expr.annotations.Output;
import org.apache.drill.exec.expr.annotations.Param;
import org.apache.drill.exec.expr.holders.NullableVarCharHolder;

import javax.inject.Inject;

@FunctionTemplate(
        name = "concat_feature_values",
        scope = FunctionTemplate.FunctionScope.SIMPLE
)
public class ConcatFeatureValues implements DrillSimpleFunc {
    @Param
    NullableVarCharHolder numericalFeatures;

    @Param
    NullableVarCharHolder categoricalFeatures;

    @Output
    NullableVarCharHolder out;

    @Inject
    DrillBuf buffer;

    @Override
    public void setup() {
    }

    @Override
    public void eval() {
        String[] numericalStrings = drillbit.utils.primitive.StringParser.parseList(org.apache.drill.exec.expr.fn.impl.StringFunctionHelpers.toStringFromUTF8(numericalFeatures.start, numericalFeatures.end, numericalFeatures.buffer));
        String[] categoricalStrings = drillbit.utils.primitive.StringParser.parseList(org.apache.drill.exec.expr.fn.impl.StringFunctionHelpers.toStringFromUTF8(categoricalFeatures.start, categoricalFeatures.end, categoricalFeatures.buffer));
        String[] features = new String[numericalStrings.length + categoricalStrings.length];
        java.lang.System.arraycopy(numericalStrings, 0, features, 0, numericalStrings.length);
        java.lang.System.arraycopy(categoricalStrings, 0, features, numericalStrings.length, categoricalStrings.length);

        out.isSet = 1;
        out.buffer = buffer;
        out.start = 0;
        out.end = java.util.Arrays.toString(features).getBytes().length;
        out.buffer.setBytes(0, java.util.Arrays.toString(features).getBytes());
    }
}
