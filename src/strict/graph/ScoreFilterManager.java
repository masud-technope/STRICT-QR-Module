package strict.graph;

import java.util.HashMap;
import java.util.HashSet;
import strict.ca.usask.cs.srlab.strict.config.StaticData;
import strict.query.QueryToken;
import strict.stopwords.StopWordManager;
import strict.utility.MiscUtility;

public class ScoreFilterManager {

	HashMap<String, QueryToken> tokendb;
	String scoreKey;

	public ScoreFilterManager(HashMap<String, QueryToken> tokendb, String scoreKey) {
		this.tokendb = tokendb;
		this.scoreKey = scoreKey;
	}

	protected HashMap<String, QueryToken> filterStopWords(HashMap<String, QueryToken> tokenMap) {
		// filtering the stop words
		HashSet<String> tokens = new HashSet<>(tokenMap.keySet());
		StopWordManager stopManager = new StopWordManager();
		for (String key : tokens) {
			if (stopManager.stopList.contains(key)) {
				// QueryToken qtoken = tokenMap.get(key);
				tokenMap.remove(key);
			}
		}
		return tokenMap;
	}

	protected HashMap<String, QueryToken> filterLowScores(HashMap<String, QueryToken> tokenMap, String type) {
		HashSet<String> tokens = new HashSet<>(tokenMap.keySet());
		for (String key : tokens) {
			double score = 0;
			switch (type) {
			case "TR":
				score = tokenMap.get(key).textRankScore;
				break;
			case "PR":
				score = tokenMap.get(key).posRankScore;
				break;
			case "TRC":
				score = tokenMap.get(key).coreRankScore;
				break;
			case "PRC":
				score = tokenMap.get(key).coreRankScore;
				break;
			}
			if (score < StaticData.INITIAL_TERM_WEIGHT) {
				tokenMap.remove(key);
			}
		}
		return tokenMap;
	}

	public HashMap<String, QueryToken> applyFilters() {
		HashMap<String, QueryToken> scoreMap = filterStopWords(this.tokendb);
		scoreMap = filterLowScores(scoreMap, scoreKey);
		return MiscUtility.normalizeScore(scoreMap, scoreKey);
	}

}
