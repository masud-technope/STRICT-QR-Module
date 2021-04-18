package strict.weka.model;

import java.util.ArrayList;
import java.util.HashMap;
import strict.ca.usask.cs.srlab.strict.config.StaticData;
import strict.utility.ContentLoader;
import strict.utility.QueryLoader;
import strict.utility.SelectedBugs;

public class BestQueryProvider {

	String predictionFile;
	String repoName;
	ArrayList<Integer> selectedBugs;

	public BestQueryProvider(String repoName, String predictionFile) {
		this.predictionFile = predictionFile;
		this.repoName = repoName;
		this.selectedBugs = SelectedBugs.loadSelectedBugs(repoName);
	}

	public BestQueryProvider() {
		// default constructor
	}

	public HashMap<Integer, String> extractPredictedQueries() {

		HashMap<Integer, String> bestKeyMap = new HashMap<>();
		HashMap<Integer, Double> bestKeyScoreMap = new HashMap<>();
		ArrayList<String> predLines = ContentLoader.getAllLinesOptList(this.predictionFile);

		for (String predLine : predLines) {
			String[] parts = predLine.split("\\s+");
			// String actualClass = parts[1].trim().split(":")[1];
			String predictedClass = parts[2].trim().split(":")[1];
			double bestClassProbability = Double.parseDouble(parts[3].trim());
			// double worstClassProbability = Double.parseDouble(parts[4].trim());

			String[] dashed = parts[5].split("-");
			int bugID = Integer.parseInt(dashed[dashed.length - 2].trim());
			String queryKey = dashed[dashed.length - 1].trim();

			if (predictedClass.equals("bestq")) {
				if (bestKeyMap.containsKey(bugID)) {
					double tempBCP = bestKeyScoreMap.get(bugID);
					if (bestClassProbability > tempBCP) {
						bestKeyMap.put(bugID, queryKey);
						bestKeyScoreMap.put(bugID, bestClassProbability);
					}
				} else {
					bestKeyMap.put(bugID, queryKey);
					bestKeyScoreMap.put(bugID, bestClassProbability);
				}
			}
		}

		// adding the missing bugs with baseline
		for (int bugID : this.selectedBugs) {
			if (!bestKeyMap.containsKey(bugID)) {
				bestKeyMap.put(bugID, "base");
			}
		}

		return populateQueries(bestKeyMap);
	}

	protected HashMap<Integer, String> populateQueries(HashMap<Integer, String> bestKeyMap) {
		String bestqFile = StaticData.HOME_DIR + "/Proposed-STRICT/Query-Difficulty-Model/bestq/" + repoName + ".txt";
		HashMap<Integer, String> goldBestQMap = QueryLoader.loadQuery(bestqFile);
		String worstqFile = StaticData.HOME_DIR + "/Proposed-STRICT/Query-Difficulty-Model/worstq/" + repoName + ".txt";
		HashMap<Integer, String> goldWorstQMap = QueryLoader.loadQuery(worstqFile);
		String baseQFile = StaticData.HOME_DIR + "/Baseline/query/query-whole/" + repoName + ".txt";
		HashMap<Integer, String> baseQueryMap = QueryLoader.loadQuery(baseQFile);

		HashMap<Integer, String> bestQueryMap = new HashMap<>();
		for (int bugID : bestKeyMap.keySet()) {
			String myQueryKey = bestKeyMap.get(bugID);
			switch (myQueryKey) {
			case "bestq":
				bestQueryMap.put(bugID, goldBestQMap.get(bugID));
				break;
			case "worstq":
				bestQueryMap.put(bugID, goldWorstQMap.get(bugID));
				break;
			case "base":
				bestQueryMap.put(bugID, baseQueryMap.get(bugID));
				break;

			default:
				break;
			}
		}
		return bestQueryMap;
	}
}
