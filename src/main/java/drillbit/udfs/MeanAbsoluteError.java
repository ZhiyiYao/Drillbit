package drillbit.udfs;

//import com.drew.metadata.jpeg.JpegReader;

import io.netty.buffer.DrillBuf;
import org.apache.drill.exec.expr.DrillAggFunc;
import org.apache.drill.exec.expr.annotations.FunctionTemplate;
import org.apache.drill.exec.expr.annotations.Output;
import org.apache.drill.exec.expr.annotations.Param;
import org.apache.drill.exec.expr.annotations.Workspace;
import org.apache.drill.exec.expr.holders.NullableVarCharHolder;
import org.apache.drill.exec.expr.holders.ObjectHolder;

import javax.inject.Inject;

@FunctionTemplate(
        name = "mae",
        scope = FunctionTemplate.FunctionScope.POINT_AGGREGATE,
        nulls = FunctionTemplate.NullHandling.INTERNAL
)
public class MeanAbsoluteError implements DrillAggFunc {
    @Param
    NullableVarCharHolder actual;

    @Param
    NullableVarCharHolder predicted;

    @Workspace
    ObjectHolder maeHolder;

    @Output
    NullableVarCharHolder result;

    @Inject
    DrillBuf buffer;

    @Override
    public void setup() {
        maeHolder = new ObjectHolder();
        maeHolder.obj = new drillbit.metrics.MeanAbsoluteError();
    }

    @Override
    public void add() {
        if (actual.isSet != 1 || predicted.isSet != 1) {
            return;
        }
        String actualString = org.apache.drill.exec.expr.fn.impl.StringFunctionHelpers.toStringFromUTF8(actual.start, actual.end, actual.buffer);
        String predictedString = org.apache.drill.exec.expr.fn.impl.StringFunctionHelpers.toStringFromUTF8(predicted.start, predicted.end, predicted.buffer);
        ((drillbit.metrics.MeanAbsoluteError) maeHolder.obj).add(actualString, predictedString, "");
    }

    @Override
    public void output() {
        String mae = ((drillbit.metrics.MeanAbsoluteError) maeHolder.obj).output().toString();
        byte[] maeBytes = mae.getBytes();

        result.isSet = 1;
        buffer = result.buffer = buffer.reallocIfNeeded(maeBytes.length);
        result.start = 0;
        result.end = maeBytes.length;
        result.buffer.setBytes(0, maeBytes);
    }

    @Override
    public void reset() {
        ((drillbit.metrics.MeanAbsoluteError) maeHolder.obj).reset();
    }
}
