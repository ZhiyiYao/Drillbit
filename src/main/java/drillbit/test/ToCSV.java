package drillbit.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ToCSV {
    public static void main(String[] args) {
        ToCSV toCSV = new ToCSV();
        toCSV.toCsv();
    }

    public void toCsv() {
        InputStream in = getClass().getResourceAsStream("/dataset/boston.data");
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuilder builder = new StringBuilder();
        String line = null;
        while (true) {
            try {
                if (line == null) {
                    line = reader.readLine();
                }
                String[] features = line.split("\\s+");
                for (int i = 1; i < features.length; i++) {
                    builder.append(features[i]);
                    if (i < features.length - 1) {
                        builder.append(",");
                    }
                }
                line = reader.readLine();
                if (line != null) {
                    builder.append("\n");
                } else {
                    break;
                }
            } catch (IOException e) {
                break;
            }
        }


        System.out.println(builder.toString());
    }
}
