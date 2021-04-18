package kevic;

import java.util.ArrayList;
import java.util.HashMap;
import strict.ca.usask.cs.srlab.strict.config.StaticData;
import strict.utility.ContentLoader;
import strict.utility.ContentWriter;

public class RQKevicManager {

	String masterModelFile;

	public void runRQMachine(boolean isML) {

		StaticData.USE_DYNAMIC_KEYWORD_THRESHOLD = false;
		ArrayList<String> repos = getAllRepos();
		// loading the predicted keyword class temporarily
		HashMap<String, Integer> predictedClassMap = loadPredictedClass();
		HashMap<String, Double> selectProbMap = loadSelectedPrediction();
		
		for (String repo : repos) {
			if (isML) {
				KevicQueryMaker maker = new KevicQueryMaker(repo, predictedClassMap, selectProbMap);
				maker.makeKevicQuerySmart();
			} else {
				KevicQueryMaker maker = new KevicQueryMaker(repo);
				maker.makeKevicQuery();
			}
		}
	}

	protected ArrayList<String> getAllRepos() {
		return ContentLoader.getAllLinesOptList("./repos/repos.txt");
	}

	public ArrayList<String> constructTrainingDataset(String queryFileKey) {
		// running the query machine
		StaticData.USE_DYNAMIC_KEYWORD_THRESHOLD = false;
		ArrayList<String> repos = getAllRepos();
		HashMap<String, Integer> masterLabelMap = loadMasterKeywords();
		ArrayList<String> masterRows = new ArrayList<String>();
		for (String repo : repos) {
			KevicQueryMaker maker = new KevicQueryMaker(repo, masterLabelMap, null);
			ArrayList<String> rows = maker.makeKevicQueryRowsForML();
			masterRows.addAll(rows);
		}
		return masterRows;
	}

	public void setModelFile(String filePath) {
		this.masterModelFile = filePath;
	}

	public void saveTrainingData(ArrayList<String> modelRows) {
		ContentWriter.writeContent(this.masterModelFile, modelRows);
	}

	protected static String getMasterKeywordFile() {
		return StaticData.HOME_DIR + "/Kevic/model/masterKeywords.txt";
	}

	protected static HashMap<String, Integer> loadMasterKeywords() {
		String keywordlabelFile = getMasterKeywordFile();
		ArrayList<String> lines = ContentLoader.getAllLinesOptList(keywordlabelFile);
		HashMap<String, Integer> masterLabMap = new HashMap<String, Integer>();
		for (String line : lines) {
			String[] parts = line.split("\\s+");
			String keyword = parts[0];
			int kclass = Integer.parseInt(parts[1].trim());
			masterLabMap.put(keyword, kclass);
		}
		return masterLabMap;
	}

	protected String getModelPredictionFile() {
		return StaticData.HOME_DIR + "/Kevic/model/prediction-Logisitc-Regression-Sanitized.txt";
	}

	protected HashMap<String, Integer> loadPredictedClass() {
		HashMap<String, Integer> predictedMap = new HashMap<String, Integer>();
		if (predictedMap.isEmpty()) {
			String predictedFile = getModelPredictionFile();
			ArrayList<String> keywordLines = ContentLoader.getAllLinesOptList(predictedFile);
			for (String keywordLine : keywordLines) {
				String[] parts = keywordLine.split("\\s+");
				int predicted = Integer.parseInt(parts[2].split(":")[1].trim());
				String keyword = parts[5].trim();
				predictedMap.put(keyword, predicted);
			}
		}
		return predictedMap;
	}

	protected HashMap<String, Double> loadSelectedPrediction() {
		HashMap<String, Double> predictedMap = new HashMap<String, Double>();
		if (predictedMap.isEmpty()) {
			String predictedFile = getModelPredictionFile();
			ArrayList<String> keywordLines = ContentLoader.getAllLinesOptList(predictedFile);
			for (String keywordLine : keywordLines) {
				String[] parts = keywordLine.split("\\s+");
				double predicted = Double.parseDouble(parts[3].trim());
				String keyword = parts[5].trim();
				predictedMap.put(keyword, predicted);
			}
		}
		return predictedMap;
	}

}
