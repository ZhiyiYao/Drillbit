package drillbit.metrics.utils;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class PrettyPrinter {
    public static String printIntegerMatrix(@Nonnull ArrayList<String> rows, ArrayList<String> cols, ConcurrentHashMap<String, ConcurrentHashMap<String, Integer>> table) {
        int maxRowLabelLength = 0;
        for (String row : rows) {
            if (row.length() > maxRowLabelLength) {
                maxRowLabelLength = row.length();
            }
        }
        maxRowLabelLength += 1;

        int maxColLabelLength = 0;
        for (String col : cols) {
            if (col.length() > maxColLabelLength) {
                maxColLabelLength = col.length();
            }
        }
        maxColLabelLength += 1;

        String colFormat = "|%-" + maxColLabelLength + "s";
        String eleFormat = "|%-" + maxColLabelLength + "d";
        String rowFormat = "|%-" + maxRowLabelLength + "s";
        String seperateLine = printSeperateLine(maxColLabelLength * cols.size() + 1);

        StringBuilder builder = new StringBuilder();
        builder.append(seperateLine);
        for (String col : cols) {
            builder.append(String.format(colFormat, col));
        }
        builder.append("\n");
        builder.append(seperateLine);
        for (String row : rows) {
            ConcurrentHashMap<String, Integer> rowMap = table.get(row);
            for (String col : cols) {
                builder.append(String.format(eleFormat, rowMap.get(col)));
            }
            builder.append("\n");
            builder.append(seperateLine);
        }

        builder.deleteCharAt(builder.length());

        return builder.toString();
    }

    public static String printSeperateLine(int length) {
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < length; i++) {
            line.append("-");
        }
        line.append("\n");

        return line.toString();
    }
}
