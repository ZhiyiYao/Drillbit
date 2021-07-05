package drillbit.test;

import drillbit.protobuf.CommunicationPb;

import java.util.Arrays;

public class TestComm {
    public static void main(String[] args) {
        CommunicationPb.Jobs.Builder builder = CommunicationPb.Jobs.newBuilder();
        CommunicationPb.Jobs.Job.Builder jobBuilder = CommunicationPb.Jobs.Job.newBuilder();
        jobBuilder.setBatchNum(1);
        jobBuilder.setJobID(2);
        jobBuilder.setFile("123");
        jobBuilder.setHost("123");
        for (int i = 0; i < 200; i++) {
            builder.addJob(jobBuilder.build());
        }
        System.out.println(Arrays.toString(builder.build().toByteArray()));
    }
}
