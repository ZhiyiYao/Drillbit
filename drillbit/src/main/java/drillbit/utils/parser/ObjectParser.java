package drillbit.utils.parser;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.velocity.util.ArrayListWrapper;

import javax.annotation.Nonnull;
import java.io.StringWriter;
import java.util.ArrayList;

public final class ObjectParser {

    public static int parseInt(@Nonnull final Object o) {
        if (o instanceof Integer) {
            return ((Integer) o).intValue();
        }
        if (o instanceof IntWritable) {
            return ((IntWritable) o).get();
        }
        if (o instanceof LongWritable) {
            long l = ((LongWritable) o).get();
            if (l > Integer.MAX_VALUE) {
                throw new IllegalArgumentException(
                        "int value must be less than " + Integer.MAX_VALUE + ", but was " + l);
            }
            return (int) l;
        }
        String s = o.toString();
        return Integer.parseInt(s);
    }

    public static float parseFloat(@Nonnull final Object o) {
        if (o instanceof Float) {
            return ((Float) o).floatValue();
        }
        if (o instanceof FloatWritable) {
            return ((FloatWritable) o).get();
        }
        if (o instanceof DoubleWritable) {
            double d = ((DoubleWritable) o).get();
            if (d > Float.MAX_VALUE) {
                throw new IllegalArgumentException(
                        "float value must be less than " + Float.MAX_VALUE + ", but was " + d);
            }
            return (float) d;
        }
        String s = o.toString();
        return Float.parseFloat(s);
    }

    public static double parseDouble(@Nonnull final Object o) {
        if (o instanceof Double) {
            return ((Double) o).floatValue();
        }
        if (o instanceof DoubleWritable) {
            double d = ((DoubleWritable) o).get();
            if (d > Float.MAX_VALUE) {
                throw new IllegalArgumentException(
                        "float value must be less than " + Float.MAX_VALUE + ", but was " + d);
            }
            return d;
        }
        String s = o.toString();
        return Double.parseDouble(s);
    }

    public static ArrayList<?> parseList(@Nonnull final Object o) {
        if (o instanceof ArrayList) {
            return (ArrayList<?>) o;
        }
        if (o instanceof ArrayListWrapper) {
            return (ArrayList<?>) ((ArrayListWrapper) o).subList(0, ((ArrayListWrapper) o).size());
        }
        if (o instanceof String) {
            return StringParser.parseArray((String) o);
        }
        return null;
    }

    public static String parseString(@Nonnull final Object o) {
        if (o instanceof String) {
            return (String) o;
        }
        else if (o instanceof StringWriter) {
            return ((StringWriter) o).toString();
        }
        else {
            try {
                return o.toString();
            }
            catch (Exception e) {
                throw new IllegalArgumentException(e);
            }
        }
    }
}
