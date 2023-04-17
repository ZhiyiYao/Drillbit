package drillbit.SVM.libsvm;

import drillbit.FeatureValue;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Formatter;

public class svm_scale {
    private static final double lower = -1;
    private static final double upper = 1;
    public double[] feature_max;
    public double[] feature_min;

    public svm_scale(double[] feature_max, double[] feature_min) {
        this.feature_max = feature_max;
        this.feature_min = feature_min;
    }

    public void restore(ArrayList<FeatureValue> features) {
        for (int i = 0; i < features.size(); i++) {
            FeatureValue fv = features.get(i);
            double value = fv.getValueAsDouble();
            if (value == feature_min[i])
                fv.setValue(lower);
            else if (value == feature_max[i])
                fv.setValue(upper);
            else
                fv.setValue(lower + (upper - lower) *
                        (value - feature_min[i]) /
                        (feature_max[i] - feature_min[i]));
        }
    }

    public byte[] save(ByteArrayOutputStream out) throws IOException {
        Formatter formatter = new Formatter(new StringBuilder());
        BufferedWriter fp_save = new BufferedWriter(new OutputStreamWriter(out));

        formatter.format("%d\n", feature_max.length);
        for (int i = 0; i < feature_max.length; i++) {
            formatter.format("%f %f\n", feature_min[i], feature_max[i]);
        }
        fp_save.write(formatter.toString());
        fp_save.close();

        return out.toByteArray();
    }
}
