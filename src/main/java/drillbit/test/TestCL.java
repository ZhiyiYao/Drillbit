package drillbit.test;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class TestCL {
    static public void main(String[] args) {
        String[] args1 = "-z 1 -y 2".split("\\s+");
        Options opts = new Options();
        opts.addOption("z", true, "z");
        opts.addOption("y", true, "y");
        opts.addOption("x", true, "x");

        final CommandLine cl;
        try {
            DefaultParser parser = new DefaultParser();
            cl = parser.parse(opts, args1);
        } catch (IllegalArgumentException | ParseException e) {
            throw new IllegalArgumentException(e);
        }

        System.out.println(cl.getOptionValue("x"));
    }
}
