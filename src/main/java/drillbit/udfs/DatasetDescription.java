package drillbit.udfs;

import io.netty.buffer.DrillBuf;
import org.apache.drill.exec.expr.DrillSimpleFunc;
import org.apache.drill.exec.expr.annotations.FunctionTemplate;
import org.apache.drill.exec.expr.annotations.Output;
import org.apache.drill.exec.expr.annotations.Param;
import org.apache.drill.exec.expr.holders.VarCharHolder;

import javax.inject.Inject;

@FunctionTemplate(
        name = "dataset_description",
        scope = FunctionTemplate.FunctionScope.SIMPLE,
        nulls = FunctionTemplate.NullHandling.NULL_IF_NULL
)
public class DatasetDescription implements DrillSimpleFunc {
    @Param(constant = true)
    VarCharHolder datasetNameHolder;

    @Output
    VarCharHolder descriptionHolder;

    @Inject
    DrillBuf buffer;

    @Override
    public void setup() {
    }

    @Override
    public void eval() {
        drillbit.data.Dataset dataset = drillbit.data.DatasetFactory.create(org.apache.drill.exec.expr.fn.impl.StringFunctionHelpers.toStringFromUTF8(datasetNameHolder.start, datasetNameHolder.end, datasetNameHolder.buffer));

        byte[] descriptionBytes = dataset.getDatasetDescription().getBytes();

        buffer = descriptionHolder.buffer = buffer.reallocIfNeeded(descriptionBytes.length);
        descriptionHolder.start = 0;
        descriptionHolder.end = descriptionBytes.length;
        descriptionHolder.buffer.setBytes(0, descriptionBytes);
    }
}
