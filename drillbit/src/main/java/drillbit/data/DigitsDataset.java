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

	private int nSamples;
	private int nClass;

	public boolean optionProcessed;

	public DigitsDataset() {
		nClass= 10;
		nSamples = 1797;
		features = new ArrayList<>();
		targets = new ArrayList<>();
		loadAllSamples(features, targets);
		optionProcessed = false;
	}

	@Override
	public void loadAllSamples(ArrayList<String> featureArray, ArrayList<String> targetArray) {
		String datasetString = Dataset.getRawDataset("/dataset/digits.txt");
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
		boolean shuffle = true;

		CommandLine cl = parseOptions(options);

		if (cl.hasOption("not_shuffle")) {
			shuffle = false;
		}

		final int MAX_N_SAMPLES = 1798;
		nSamples = StringParser.parseInt(cl.getOptionValue("n_samples"), MAX_N_SAMPLES);
		Conditions.checkArgument(0 <= nSamples && nSamples <= MAX_N_SAMPLES, String.format("Invalid sample number of %d", nSamples));

		if (cl.hasOption("n_class")) {
			int nClassAssigned = StringParser.parseInt(cl.getOptionValue("n_class"), nClass);
			Conditions.checkArgument(0 < nClassAssigned && nClassAssigned <= nClass, String.format("Invalid sample number of %d", nClassAssigned));
			nClass = nClassAssigned;
		}

		features = new ArrayList<>();
		targets = new ArrayList<>();

		loadAllSamples(features,targets);

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
		options.addOption("n_class","number_of_class",true,"assgin number of class in [0,n_class]");

		return options;
	}


	@Override
	public String getDatasetDescription() {
		return "Digit dataset: {\n" +
				"   n_classes: 10,\n" +
				"   max_n_samples: 1797,\n" +
				"   labels: [\n" +
				"		0,\n" +
				"       1,\n" +
				"       2,\n" +
				"		3,\n" +
				"		4,\n" +
				"		5,\n" +
				"		6,\n" +
				"		7,\n" +
				"		8,\n" +
				"		9,\n" +
				"   ]\n" +
				"}";
	}
}
