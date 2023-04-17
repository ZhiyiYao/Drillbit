package drillbit.test;

import drillbit.utils.math.MathUtils;

import java.util.ArrayList;

public class TestMathUtils {
    public static void main(String[] args) {
        testMinInterval();
    }

    public static void testMinInterval() {
        ArrayList<Double> values = new ArrayList<>();

        values.add(0.1);
        values.add(0.2);
        values.add(0.3);
        values.add(0.35);
        values.add(0.32);
        values.add(0.02);
        values.add(0.11);

        System.out.println(MathUtils.minInterval(values));
    }
}
