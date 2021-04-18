package strict.ca.usask.cs.srlab.strict.test;

import java.util.ArrayList;
import java.util.HashMap;
import org.junit.Test;
import kevic.KevicDatasetMaker;
import kevic.KevicQueryMaker;
import strict.ca.usask.cs.srlab.strict.config.StaticData;
import strict.utility.ContentLoader;
import strict.utility.MiscUtility;

public class KevicTest {

	@Test
	public void setup() {
		StaticData.SUGGESTED_KEYWORD_COUNT = 10;
	}

	@Test
	public void testKevicDatasetMaker() {
		String[] repos = ContentLoader.getAllLines("./repos.txt");
		String repoName = "adempiere-3.1.0";
		// for (String repoName : repos) {
		KevicDatasetMaker kdbMaker = new KevicDatasetMaker(repoName);
		kdbMaker.determineKeywordClass();
		System.out.println(kdbMaker.masterKeywordMap);
		System.out.println("Done:" + repoName);
		// }
		// KevicDatasetMaker.saveMasterKeywords();
	}

	@Test
	public void testKevicQueryMaker() {
		String repoName = "adempiere-3.1.0";
		KevicQueryMaker.INCLUDE_TITLE = false;
		KevicQueryMaker maker = new KevicQueryMaker(repoName);
		ArrayList<String> queries = maker.makeKevicQuery();
		MiscUtility.showItems(queries);
	}

	@Test
	public void testKevicQueryWithML() {
		String repoName = "adempiere-3.1.0";
		KevicQueryMaker.INCLUDE_TITLE = false;
		HashMap<String, Integer> predictedClassMap = loadPredictedClass();
		HashMap<String, Double> selectProbMap = loadSelectedPrediction();
		KevicQueryMaker maker = new KevicQueryMaker(repoName, predictedClassMap, selectProbMap);
		ArrayList<String> queries = maker.makeKevicQuerySmart();
		MiscUtility.showItems(queries);
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
