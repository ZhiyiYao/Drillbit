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
        maxRowLabelLength = Math.max(maxRowLabelLength, "actual\\predicted".length());
        maxRowLabelLength += 2;

        int maxColLabelLength = 0;
        for (String col : cols) {
            if (col.length() > maxColLabelLength) {
                maxColLabelLength = col.length();
            }
        }
        maxColLabelLength += 2;

        String colFormat = "|%-" + maxColLabelLength + "s";
        String eleFormat = "|%-" + maxColLabelLength + "d";
        String rowFormat = "|%-" + maxRowLabelLength + "s";
        String seperateLine = printSeperateLine((maxColLabelLength + 1) * cols.size() + maxRowLabelLength + 2);

        StringBuilder builder = new StringBuilder();
        builder.append(seperateLine);
        builder.append(String.format(rowFormat, "actual\\predicted"));
        for (String col : cols) {
            builder.append(String.format(colFormat, col));
        }
        builder.append("|\n");
        builder.append(seperateLine);
        for (String row : rows) {
            ConcurrentHashMap<String, Integer> rowMap = table.get(row);
            builder.append(String.format(rowFormat, row));
            for (String col : cols) {
                int count = rowMap.getOrDefault(col, 0);
                builder.append(String.format(eleFormat, count));
            }
            builder.append("|\n");
            builder.append(seperateLine);
        }

        builder.deleteCharAt(builder.length() - 1);

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
