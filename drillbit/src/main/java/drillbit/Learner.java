package drillbit;

import javax.annotation.Nonnull;

import com.google.protobuf.InvalidProtocolBufferException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

/*
    Learner interface designed for Apache Drill UDAF's calling.
 */
public interface Learner {
    @Nonnull
    Options getOptions();

    Options getPredictOptions();

    CommandLine parseOptions(@Nonnull String commandLineValue) throws IllegalArgumentException;

    CommandLine parsePredictOptions(@Nonnull String commandLineValue) throws IllegalArgumentException;

    CommandLine processOptions(@Nonnull CommandLine cl) throws IllegalArgumentException;

    CommandLine processPredictOptions(@Nonnull CommandLine cl) throws IllegalArgumentException;

    void showHelp(@Nonnull Options opts);

    void add(@Nonnull final String feature, @Nonnull final String target, @Nonnull final String commandLine);

    void add(@Nonnull final String feature, @Nonnull final String target);

    byte[] output();

    void reset();

    byte[] toByteArray();

    Learner fromByteArray(byte[] learnerBytes) throws InvalidProtocolBufferException;
}
