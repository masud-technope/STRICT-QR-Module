package strict.ca.usask.cs.srlab.strict.test;

import org.junit.Test;
import strict.ca.usask.cs.srlab.strict.config.StaticData;
import strict.weka.model.PredictionCleaner;
import strict.weka.model.WekaModelPredictionMaker;

public class WekaModelPredictionMakerTest {

	@Test
	public void testWekaModelPredictionMaker() {
		String repoName = "tomcat70";
		
		String arffFile = StaticData.HOME_DIR + "/Proposed-STRICT/Query-Difficulty-Model/qdiff-model-dec23-8pm/"
				+ repoName + ".arff";
		String predictionFile = StaticData.HOME_DIR + "/Proposed-STRICT/Query-Difficulty-Model/predictions-apr17-4pm/"
				+ repoName + ".txt";

		WekaModelPredictionMaker maker = new WekaModelPredictionMaker(repoName, arffFile);
		maker.setPredictionFile(predictionFile);

		String algoKey = "ST";
		String prediction = maker.determineBestClassifications(100, algoKey);
		prediction = new PredictionCleaner().cleanPredictions(prediction);
		maker.saveEvaluations(prediction);

		System.out.println(prediction);
	}
}
