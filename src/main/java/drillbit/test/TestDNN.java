package drillbit.test;

import org.apache.commons.io.FilenameUtils;
import org.datavec.api.records.reader.RecordReader;
import org.datavec.api.records.reader.impl.csv.CSVRecordReader;
import org.datavec.api.split.FileSplit;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.common.resources.Downloader;
import org.nd4j.evaluation.classification.Evaluation;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.SplitTestAndTrain;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerStandardize;
import org.nd4j.linalg.learning.config.Sgd;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;

/**
 * @author Adam Gibson
 */
@SuppressWarnings("DuplicatedCode")
public class TestDNN {

    private static Logger log = LoggerFactory.getLogger(TestDNN.class);

    public static void main(String[] args) throws Exception {

        //First: get the dataset using the record reader. CSVRecordReader handles loading/parsing
        int numLinesToSkip = 0;
        char delimiter = ',';
        RecordReader recordReader = new CSVRecordReader(numLinesToSkip, delimiter);
        recordReader.initialize(new FileSplit(new File(DownloaderUtility.IRISDATA.Download(), "iris.txt")));

        //Second: the RecordReaderDataSetIterator handles conversion to DataSet objects, ready for use in neural network
        int labelIndex = 4;     //5 values in each row of the iris.txt CSV: 4 input features followed by an integer label (class) index. Labels are the 5th value (index 4) in each row
        int numClasses = 3;     //3 classes (types of iris flowers) in the iris data set. Classes have integer values 0, 1 or 2
        int batchSize = 150;    //Iris data set: 150 examples total. We are loading all of them into one DataSet (not recommended for large data sets)

        DataSetIterator iterator = new RecordReaderDataSetIterator(recordReader, batchSize, labelIndex, numClasses);
        DataSet allData = iterator.next();
//        System.out.println(allData);
        allData.shuffle();
        SplitTestAndTrain testAndTrain = allData.splitTestAndTrain(0.65);  //Use 65% of data for training

        DataSet trainingData = testAndTrain.getTrain();
        DataSet testData = testAndTrain.getTest();

        //We need to normalize our data. We'll use NormalizeStandardize (which gives us mean 0, unit variance):
        DataNormalization normalizer = new NormalizerStandardize();
        normalizer.fit(trainingData);           //Collect the statistics (mean/stdev) from the training data. This does not modify the input data
        normalizer.transform(trainingData);     //Apply normalization to the training data
        normalizer.transform(testData);         //Apply normalization to the test data. This is using statistics calculated from the *training* set


        final int numInputs = 4;
        int outputNum = 3;
        long seed = 6;


        log.info("Build model....");
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(seed)
                .activation(Activation.TANH)
                .weightInit(WeightInit.XAVIER)
                .updater(new Sgd(0.1))
                .l2(1e-4)
                .list()
                .layer(new DenseLayer.Builder().nIn(numInputs).nOut(3)
                        .build())
                .layer(new DenseLayer.Builder().nIn(3).nOut(3)
                        .build())
                .layer(new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                        .activation(Activation.SOFTMAX) //Override the global TANH activation with softmax for this layer
                        .nIn(3).nOut(outputNum).build())
                .build();

        //run the model
        MultiLayerNetwork model = new MultiLayerNetwork(conf);
        model.init();
        //record score once every 100 iterations
        model.setListeners(new ScoreIterationListener(100));

        for (int i = 0; i < 1000; i++) {
            model.fit(trainingData);
        }

        //evaluate the model on the test set
        Evaluation eval = new Evaluation(3);
        INDArray output = model.output(testData.getFeatures());
        System.out.println(output.getDouble(0, 1));
        System.out.println(output);
        eval.eval(testData.getLabels(), output);
        log.info(eval.stats());

    }


    /**
     * Given a base url and a zipped file name downloads contents to a specified directory under ~/dl4j-examples-data
     * Will check md5 sum of downloaded file
     * <p>
     * <p>
     * Sample Usage with an instantiation DATAEXAMPLE(baseurl,"DataExamples.zip","data-dir",md5,size):
     * <p>
     * DATAEXAMPLE.Download() & DATAEXAMPLE.Download(true)
     * Will download DataExamples.zip from baseurl/DataExamples.zip to a temp directory,
     * Unzip it to ~/dl4j-example-data/data-dir
     * Return the string "~/dl4j-example-data/data-dir/DataExamples"
     * <p>
     * DATAEXAMPLE.Download(false)
     * will perform the same download and unzip as above
     * But returns the string "~/dl4j-example-data/data-dir" instead
     *
     * @author susaneraly
     */
    public enum DownloaderUtility {

        IRISDATA("IrisData.zip", "datavec-examples", "bb49e38bb91089634d7ef37ad8e430b8", "1KB"),
        ANIMALS("animals.zip", "dl4j-examples", "1976a1f2b61191d2906e4f615246d63e", "820KB"),
        ANOMALYSEQUENCEDATA("anomalysequencedata.zip", "dl4j-examples", "51bb7c50e265edec3a241a2d7cce0e73", "3MB"),
        CAPTCHAIMAGE("captchaImage.zip", "dl4j-examples", "1d159c9587fdbb1cbfd66f0d62380e61", "42MB"),
        CLASSIFICATIONDATA("classification.zip", "dl4j-examples", "dba31e5838fe15993579edbf1c60c355", "77KB"),
        DATAEXAMPLES("DataExamples.zip", "dl4j-examples", "e4de9c6f19aaae21fed45bfe2a730cbb", "2MB"),
        LOTTERYDATA("lottery.zip", "dl4j-examples", "1e54ac1210e39c948aa55417efee193a", "2MB"),
        NEWSDATA("NewsData.zip", "dl4j-examples", "0d08e902faabe6b8bfe5ecdd78af9f64", "21MB"),
        NLPDATA("nlp.zip", "dl4j-examples", "1ac7cd7ca08f13402f0e3b83e20c0512", "91MB"),
        PREDICTGENDERDATA("PredictGender.zip", "dl4j-examples", "42a3fec42afa798217e0b8687667257e", "3MB"),
        STYLETRANSFER("styletransfer.zip", "dl4j-examples", "b2b90834d667679d7ee3dfb1f40abe94", "3MB"),
        VIDEOEXAMPLE("video.zip", "dl4j-examples", "56274eb6329a848dce3e20631abc6752", "8.5MB");

        private static final String AZURE_BLOB_URL = "https://dl4jdata.blob.core.windows.net/dl4j-examples";
        private final String BASE_URL;
        private final String DATA_FOLDER;
        private final String ZIP_FILE;
        private final String MD5;
        private final String DATA_SIZE;

        /**
         * For use with resources uploaded to Azure blob storage.
         *
         * @param zipFile    Name of zipfile. Should be a zip of a single directory with the same name
         * @param dataFolder The folder to extract to under ~/dl4j-examples-data
         * @param md5        of zipfile
         * @param dataSize   of zipfile
         */
        DownloaderUtility(String zipFile, String dataFolder, String md5, String dataSize) {
            this(AZURE_BLOB_URL + "/" + dataFolder, zipFile, dataFolder, md5, dataSize);
        }

        /**
         * Downloads a zip file from a base url to a specified directory under the user's home directory
         *
         * @param baseURL    URL of file
         * @param zipFile    Name of zipfile to download from baseURL i.e baseURL+"/"+zipFile gives full URL
         * @param dataFolder The folder to extract to under ~/dl4j-examples-data
         * @param md5        of zipfile
         * @param dataSize   of zipfile
         */
        DownloaderUtility(String baseURL, String zipFile, String dataFolder, String md5, String dataSize) {
            BASE_URL = baseURL;
            DATA_FOLDER = dataFolder;
            ZIP_FILE = zipFile;
            MD5 = md5;
            DATA_SIZE = dataSize;
        }

        public String Download() throws Exception {
            return Download(true);
        }

        public String Download(boolean returnSubFolder) throws Exception {
            String dataURL = BASE_URL + "/" + ZIP_FILE;
            String downloadPath = FilenameUtils.concat(System.getProperty("java.io.tmpdir"), ZIP_FILE);
            String extractDir = FilenameUtils.concat(System.getProperty("user.home"), "dl4j-examples-data/" + DATA_FOLDER);
            if (!new File(extractDir).exists())
                new File(extractDir).mkdirs();
            String dataPathLocal = extractDir;
            if (returnSubFolder) {
                String resourceName = ZIP_FILE.substring(0, ZIP_FILE.lastIndexOf(".zip"));
                dataPathLocal = FilenameUtils.concat(extractDir, resourceName);
            }
            int downloadRetries = 10;
            if (!new File(dataPathLocal).exists() || new File(dataPathLocal).list().length == 0) {
                System.out.println("_______________________________________________________________________");
                System.out.println("Downloading data (" + DATA_SIZE + ") and extracting to \n\t" + dataPathLocal);
                System.out.println("_______________________________________________________________________");
                Downloader.downloadAndExtract("files",
                        new URL(dataURL),
                        new File(downloadPath),
                        new File(extractDir),
                        MD5,
                        downloadRetries);
            } else {
                System.out.println("_______________________________________________________________________");
                System.out.println("Example data present in \n\t" + dataPathLocal);
                System.out.println("_______________________________________________________________________");
            }
            return dataPathLocal;
        }
    }

}