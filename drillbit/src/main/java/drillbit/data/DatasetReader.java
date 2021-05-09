package drillbit.data;

import java.io.*;

public class DatasetReader {
	private BufferedReader reader;
	
	public DatasetReader() {}

	public DatasetReader(String filename) {
		try {
			reader = new BufferedReader(new FileReader(filename));
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public String readLine() {
		try {
			return reader.readLine();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public DatasetReader load(String filename) {
		InputStream in = getClass().getResourceAsStream(filename);
		assert in != null;
		reader = new BufferedReader(new InputStreamReader(in));

		return this;
	}
	
	public void close() {
		try {
			reader.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}
