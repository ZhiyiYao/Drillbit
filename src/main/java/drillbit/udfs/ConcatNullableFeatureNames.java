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
        name = "concat_feature_names",
        scope = FunctionTemplate.FunctionScope.SIMPLE,
        isVarArg = true
)
public class ConcatNullableFeatureNames implements DrillSimpleFunc {
    @Param
    NullableVarCharHolder[] names;

    @Output
    VarCharHolder out;

    @Inject
    DrillBuf buffer;

    @Override
    public void setup() {
    }

    @Override
    public void eval() {
        String[] nameList = new String[names.length];
        for (int i = 0; i < names.length; i++) {
            nameList[i] = org.apache.drill.exec.expr.fn.impl.StringFunctionHelpers.toStringFromUTF8(names[i].start, names[i].end, names[i].buffer);
        }
        out.buffer = buffer;
        out.start = 0;
        out.end = java.util.Arrays.toString(nameList).getBytes().length;
        out.buffer.setBytes(0, java.util.Arrays.toString(nameList).getBytes());
    }
}
