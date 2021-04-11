package drillbit.utils.udf;

public class TrainTestSampleSplitter {

    public static boolean judge(double rate) {
        return Math.random() < rate;
    }
}
