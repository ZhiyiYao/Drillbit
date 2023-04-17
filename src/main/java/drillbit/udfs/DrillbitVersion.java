package drillbit.udfs;

import io.netty.buffer.DrillBuf;
import org.apache.drill.exec.expr.DrillSimpleFunc;
import org.apache.drill.exec.expr.annotations.FunctionTemplate;
import org.apache.drill.exec.expr.annotations.Output;
import org.apache.drill.exec.expr.holders.VarCharHolder;

import javax.inject.Inject;

@FunctionTemplate(
        name = "drillbit_version",
        scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL)
public class DrillbitVersion implements DrillSimpleFunc {
    @Output
    VarCharHolder version;

    @Inject
    DrillBuf buffer;

    @Override
    public void setup() {
    }

    @Override
    public void eval() {
        version.buffer = buffer;
        version.start = 0;
        version.end = drillbit.DrillbitConstants.VERSION.getBytes().length;
        version.buffer.setBytes(0, drillbit.DrillbitConstants.VERSION.getBytes());
    }
}
