package drillbit.utils.parser;


import drillbit.FeatureValue;

import java.util.ArrayList;
import java.util.Collections;

public final class StringParser {

    private StringParser() {
    }

    public static short parseShort(final String s, final short defaultValue) {
        if (s == null) {
            return defaultValue;
        }
        return Short.parseShort(s);
    }

    public static int parseInt(final String s, final int defaultValue) {
        if (s == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(s);
        }
        catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static long parseLong(final String s, final long defaultValue) {
        if (s == null) {
            return defaultValue;
        }
        return Long.parseLong(s);
    }

    public static float parseFloat(final String s, final float defaultValue) {
        if (s == null) {
            return defaultValue;
        }
        try {
            return Float.parseFloat(s);
        }
        catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static double parseDouble(final String s, final double defaultValue) {
        if (s == null) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(s);
        }
        catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static boolean parseBoolean(final String s, final boolean defaultValue) {
        if (s == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(s);
    }

    public static ArrayList<String> parseArray(final String s) {
        if ((s.indexOf('[') == 0) && (s.indexOf(']') == s.length() - 1)) {
            String newStr = s.substring(1, s.length() - 1);
            String[] strings = newStr.split(",\\s*");
            ArrayList<String> result = new ArrayList<>();
            Collections.addAll(result, strings);
            return result;
        }
        else {
            return parseArray("[" + s + "]");
        }
    }

    public static String[] parseList(final String s) {
        ArrayList<String> stringArray = parseArray(s);
        String[] stringList = new String[stringArray.size()];
        stringArray.toArray(stringList);
        return stringList;
    }

    public static FeatureValue parseFeature(final String s) {
        return FeatureValue.parse(s);
    }
}
