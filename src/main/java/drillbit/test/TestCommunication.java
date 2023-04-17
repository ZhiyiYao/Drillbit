package drillbit.test;

import drillbit.distributed.Fragment;
import drillbit.distributed.FragmentDescriptor;

public class TestCommunication {
    public static void main(String[] args) {
        testDescriptor();
    }

    public static void testDescriptor() {
        FragmentDescriptor fragmentDescriptor = new FragmentDescriptor();
        fragmentDescriptor.count = 100;
        fragmentDescriptor.file = "123.txt";
        fragmentDescriptor.id = 1;

        for (int i = 0; i < 100; i++) {
            Fragment fragment = new Fragment(fragmentDescriptor);
            fragment.register();
        }
    }
}
