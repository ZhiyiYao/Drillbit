package drillbit.dmlmanager.synmethed;

public class SynFactory {
    static public SynMethed getMethed(String name) {
        SynMethed m;

        if (name.equals("sum")) {
            m = new SynSum();
        } else if (name.equals("avg")) {
            m = new SynAvg();
        } else {
            throw new IllegalArgumentException("Unsupported sysMethed name: " + name);
        }

        return m;
    }
}
