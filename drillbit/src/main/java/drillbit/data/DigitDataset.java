package drillbit.data;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import javax.annotation.Nonnull;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import drillbit.optimizer.OptimizerOptions;
import drillbit.utils.common.Conditions;
import drillbit.utils.parser.StringParser;

public class DigitDataset implements Dataset {
	private ArrayList<String> features;

	private ArrayList<String> targets;

	public boolean optionProcessed;

	public DigitDataset() {
		features = new ArrayList<>();
		targets = new ArrayList<>();
		loadAllSamples(features, targets);
		optionProcessed = false;
	}

	@Override
	public void loadAllSamples(ArrayList<String> featureArray, ArrayList<String> targetArray) {
		featureArray.clear();
		targetArray.clear();
		ArrayList<String> rows = new ArrayList<String>();
		try {
			BufferedReader rd = new BufferedReader(new FileReader("../../../resources/digits.txt"));
			while (true) {
				String line = rd.readLine();
				if (line == null)
					break;
				rows.add(line);
			}
			rd.close();
		} catch (IOException ex) {
			throw new RuntimeException(ex.toString());
		}
		int index;
		for (String row : rows) {
			index = row.lastIndexOf(",");
			featureArray.add("[" + row.substring(0, index) + "]");
			targetArray.add(row.substring(index + 1));
		}
	}

	@Override
	public String loadOneSample() {
		String featureAndTarget = "[";
		if (features.size() >= 1 && targets.size() >= 1) {
			featureAndTarget += features.get(0) + ", ";
			featureAndTarget += targets.get(0) + "]";
		}
		else {
			throw new UnsupportedOperationException("No enough samples in dataset.");
		}
		features.remove(0);
		targets.remove(0);

		return featureAndTarget;
	}

	@Override
	public void processOptions(String options) {
		boolean shuffle = true;
		int nSamples = 1797;

		CommandLine cl = parseOptions(options);

		if (cl.hasOption("not_shuffle")) {
			shuffle = false;
		}

		if (cl.hasOption("n_samples")) {
			int nSamplesAssigned = StringParser.parseInt(cl.getOptionValue("n_samples"), nSamples);
			Conditions.checkArgument(0 <= nSamplesAssigned && nSamplesAssigned <= nSamples,
					String.format("Invalid sample number of %d", nSamplesAssigned));
			nSamples = nSamplesAssigned;
		}

		if (shuffle) {
			Dataset.shuffle(features, targets);
		}

		ArrayList<String> newFeatures = new ArrayList<>();
		ArrayList<String> newTargets = new ArrayList<>();
		for (String feature : features.subList(0, nSamples)) {
			newFeatures.add(feature);
		}

		for (String target : targets.subList(0, nSamples)) {
			newTargets.add(target);
		}

		features = newFeatures;
		targets = newTargets;

		optionProcessed = true;
	}

	@Nonnull
	@Override
	public Options getOptions() {
		Options options = new Options();
		options.addOption("n_samples", "number_of_samples", true, "assign number of samples to be generated");
		options.addOption("not_shuffle", "not_shullfe_samples", false, "do not shuffle samples");

		return options;
	}

	@Override
	public CommandLine parseOptions(@Nonnull String optionValue) {
		String[] args = optionValue.split("\\s+");
		Options opts = getOptions();
		OptimizerOptions.setup(opts);
		opts.addOption("help", false, "Show function help");

		final CommandLine cl;
		try {
			DefaultParser parser = new DefaultParser();
			cl = parser.parse(opts, args);
		} catch (IllegalArgumentException | ParseException e) {
			throw new IllegalArgumentException(e);
		}

		if (cl.hasOption("help")) {
			this.showHelp();
		}

		return cl;
	}

	@Override
	public String getRawDataset() {
		return "";
	}

	@Override
	public String getDatasetDescription() {
		return null;
	}

	@Override
	public void showHelp() {
		System.out.println(getDatasetDescription());
	}

}
