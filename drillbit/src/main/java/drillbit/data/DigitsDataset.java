package drillbit.data;

import java.util.ArrayList;

import javax.annotation.Nonnull;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import drillbit.utils.common.Conditions;
import drillbit.utils.parser.StringParser;

public class DigitsDataset implements Dataset {
	private ArrayList<String> features;
	private ArrayList<String> targets;

	public boolean optionProcessed;

	public DigitsDataset() {
		features = new ArrayList<>();
		targets = new ArrayList<>();
		loadAllSamples(features, targets);
		optionProcessed = false;
	}

	@Override
	public void loadAllSamples(ArrayList<String> featureArray, ArrayList<String> targetArray) {
		String datasetString = Dataset.getRawDataset("/dataset/digits.data");
		featureArray.clear();
		targetArray.clear();
		String[] rows = datasetString.split("\\n");
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
		int nSamples;
		boolean shuffle;

		CommandLine cl = parseOptions(options);

		shuffle = !cl.hasOption("not_shuffle");

		final int MAX_N_SAMPLES = 1798;
		nSamples = StringParser.parseInt(cl.getOptionValue("n_samples"), MAX_N_SAMPLES);
		Conditions.checkArgument(0 <= nSamples && nSamples <= MAX_N_SAMPLES, String.format("Invalid sample number of %d", nSamples));

		features = new ArrayList<>(features.subList(0, nSamples));
		targets = new ArrayList<>(targets.subList(0, nSamples));

		if (shuffle) {
			Dataset.shuffle(features, targets);
		}

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
	public String getDatasetDescription() {
		return "Digit dataset: {\n" +
				"	type: classification,\n" +
				"	n_classes: 10,\n" +
				"	max_n_samples: 1797,\n" +
				"	features: [\n" +
				"		0-63\n" +
				"	],\n" +
				"	labels: [\n" +
				"		0-9\n" +
				"	]\n" +
				"}";
	}
}
