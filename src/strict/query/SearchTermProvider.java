package strict.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jgraph.graph.DefaultEdge;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import qd.model.prediction.sampling.BestQueryPredictorSampled;
import strict.stopwords.StopWordManager;
import strict.text.normalizer.TextNormalizer;
import strict.utility.ItemSorter;
import strict.utility.MiscUtility;
import strict.utility.MyItemSorter;
import strict.ca.usask.cs.srlab.strict.config.StaticData;
import strict.graph.GraphUtility;
import strict.graph.KCoreScoreProvider;
import strict.graph.POSRankManager;
import strict.graph.TextRankManager;
import strict.stemmer.WordNormalizer;

public class SearchTermProvider {

	int bugID;
	String repository;
	String bugtitle;
	String bugReport;
	final int MAX_TOKEN_IN_QUERY = StaticData.SUGGESTED_KEYWORD_COUNT;
	final String TECHNIQUE_NAME = "strict";
	final int predModelCount = 50;

	/****** strong options *******/
	boolean addTitle = StaticData.ADD_TITLE;
	boolean applyDynamicSize = StaticData.USE_DYNAMIC_KEYWORD_THRESHOLD;
	/**************/

	DirectedGraph<String, DefaultEdge> textGraph;
	DirectedGraph<String, DefaultEdge> posGraph;
	SimpleDirectedWeightedGraph<String, DefaultWeightedEdge> wtextGraph;
	SimpleDirectedWeightedGraph<String, DefaultWeightedEdge> wposGraph;
	ArrayList<String> sentences;

	public SearchTermProvider(String repository, int bugID, String title, String bugReport) {
		// initialization
		this.bugID = bugID;
		this.repository = repository;
		this.bugtitle = getNormalizeTitle(title);
		this.bugReport = bugReport;
		this.sentences = getAllSentences();
		this.textGraph = GraphUtility.getWordNetwork(sentences);
		this.wtextGraph = GraphUtility.getWeightedWordNetwork(sentences);
		this.posGraph = GraphUtility.getPOSNetwork(sentences);
		this.wposGraph = GraphUtility.getWeightedPOSNetwork(sentences);
	}

	protected HashMap<String, QueryToken> getQueryCoreRankScoresTRC() {
		KCoreScoreProvider kcsProvider = new KCoreScoreProvider(wtextGraph, StaticData.KCORE_SIZE);
		HashMap<String, Double> kcsMap = kcsProvider.provideKCoreScores();
		return convert2WKScore(kcsMap);
	}

	protected HashMap<String, QueryToken> convert2WKScore(HashMap<String, Double> kcsMap) {
		HashMap<String, QueryToken> tokendb = new HashMap<>();
		for (String key : kcsMap.keySet()) {
			double kcsScore = kcsMap.get(key);
			QueryToken qtoken = new QueryToken();
			qtoken.token = key;
			qtoken.coreRankScore = kcsScore;
			tokendb.put(key, qtoken);
		}
		return tokendb;
	}

	protected HashMap<String, QueryToken> getQueryCoreRankScoresPRC() {
		KCoreScoreProvider kcsProvider = new KCoreScoreProvider(wposGraph, StaticData.KCORE_SIZE);
		HashMap<String, Double> kcsMap = kcsProvider.provideKCoreScores();
		return convert2WKScore(kcsMap);
	}

	protected ArrayList<String> getAllSentences() {
		QTextCollector textcollector = new QTextCollector(this.bugtitle, this.bugReport);
		return textcollector.collectQuerySentences();
	}

	protected String getNormalizeTitle(String title) {
		return new TextNormalizer().normalizeSimpleDiscardSmallwithOrder(title);
	}

	protected HashMap<String, QueryToken> getTextRank() {
		HashMap<String, QueryToken> tokendb = GraphUtility.initializeTokensDB(this.textGraph);
		TextRankManager manager = new TextRankManager(this.textGraph, tokendb);
		return manager.getTextRank();
	}

	protected HashMap<String, QueryToken> getPOSRank() {
		HashMap<String, QueryToken> tokendb = GraphUtility.initializeTokensDB(this.posGraph);
		POSRankManager manager = new POSRankManager(this.posGraph, tokendb);
		return manager.getPOSRank();
	}

	protected HashMap<String, QueryToken> getTRC() {
		return this.getQueryCoreRankScoresTRC();
	}

	protected HashMap<String, QueryToken> getPRC() {
		return this.getQueryCoreRankScoresPRC();
	}

	public String provideSearchQuery(String scoreKey) {
		HashMap<String, QueryToken> textRankMap = new HashMap<>();
		HashMap<String, QueryToken> posRankMap = new HashMap<>();
		HashMap<String, QueryToken> coreRankMapTR = new HashMap<>();
		HashMap<String, QueryToken> coreRankMapPR = new HashMap<>();

		HashMap<String, Double> combineddb = new HashMap<>();

		switch (scoreKey) {
		case "TR":
			textRankMap = getTextRank();
			combineddb = transferScores(textRankMap, "TR");
			break;
		case "PR":
			posRankMap = getPOSRank();
			combineddb = transferScores(posRankMap, "PR");
			break;
		case "TPR":
			textRankMap = getTextRank();
			posRankMap = getPOSRank();
			combineddb = getCombinedBordaScores(textRankMap, posRankMap);
			break;
		case "TRC":
			coreRankMapTR = getQueryCoreRankScoresTRC();
			combineddb = transferScores(coreRankMapTR, "TRC");
			break;
		case "PRC":
			coreRankMapPR = getQueryCoreRankScoresPRC();
			combineddb = transferScores(coreRankMapPR, "PRC");
			break;

		case "TPRC":
			coreRankMapTR = getQueryCoreRankScoresTRC();
			coreRankMapPR = getQueryCoreRankScoresPRC();
			combineddb = addCoreRankScores(combineddb, coreRankMapPR);
			combineddb = addCoreRankScores(combineddb, coreRankMapTR);
			break;

		default:
			break;
		}

		return getQueryFinalizedBorda(combineddb);
	}

	protected double getDOI(int index, int N) {
		return (1 - (double) index / N);
	}
	
	
	protected HashMap<String, Double> getCombinedBordaScores(HashMap<String, QueryToken> tokenRankMap,
			HashMap<String, QueryToken> posRankMap) {
		// extracting final query terms
		List<Map.Entry<String, QueryToken>> trSorted = MyItemSorter.sortQTokensByTR(tokenRankMap);
		List<Map.Entry<String, QueryToken>> prSorted = MyItemSorter.sortQTokensByPOSR(posRankMap);

		HashMap<String, Double> combineddb = new HashMap<>();
		for (int i = 0; i < trSorted.size(); i++) {
			double doi = getDOI(i, trSorted.size());
			String key = trSorted.get(i).getKey();
			if (!combineddb.containsKey(key)) {
				combineddb.put(key, doi * StaticData.alpha);
			} else {
				double score = combineddb.get(key) + doi * StaticData.alpha;
				combineddb.put(key, score);
			}
		}

		for (int i = 0; i < prSorted.size(); i++) {
			double doi = getDOI(i, prSorted.size());
			String key = prSorted.get(i).getKey();
			// System.out.println(key);
			if (!combineddb.containsKey(key)) {
				combineddb.put(key, doi * StaticData.beta);
			} else {
				double score = combineddb.get(key) + doi * StaticData.beta;
				combineddb.put(key, score);
			}
		}

		return combineddb;
	}

	// added recently
	protected HashMap<String, Double> addCoreRankScores(HashMap<String, Double> combineddb,
			HashMap<String, QueryToken> kcoreMap) {
		// works for both TRC and PRC
		List<Map.Entry<String, QueryToken>> sorted = MyItemSorter.sortQTokensByScoreKey(kcoreMap, "TRC");
		HashMap<String, Double> kcoreScoreMap = new HashMap<>();
		for (int i = 0; i < sorted.size(); i++) {
			double doi = getDOI(i, sorted.size());
			kcoreScoreMap.put(sorted.get(i).getKey(), doi);
		}

		for (String key : kcoreScoreMap.keySet()) {
			if (combineddb.containsKey(key)) {
				double updated = combineddb.get(key) + kcoreScoreMap.get(key);
				combineddb.put(key, updated);
			} else {
				combineddb.put(key, kcoreScoreMap.get(key));
			}
		}
		return combineddb;
	}


	protected HashMap<String, Double> transferScores(HashMap<String, QueryToken> scoreMap, String scoreKey) {
		HashMap<String, Double> tempMap = new HashMap<>();
		for (String key : scoreMap.keySet()) {
			switch (scoreKey) {
			case "TR":
				tempMap.put(key, scoreMap.get(key).textRankScore);
				break;
			case "PR":
				tempMap.put(key, scoreMap.get(key).posRankScore);
				break;
			case "TRC":
			case "PRC":
				tempMap.put(key, scoreMap.get(key).coreRankScore);
				break;
			case "ALL":
				tempMap.put(key, scoreMap.get(key).totalScore);
				break;
			default:
				break;
			}
		}
		return tempMap;
	}

	protected HashMap<String, QueryToken> getOnlyTopK(HashMap<String, QueryToken> tokenMap, String type) {
		List<Map.Entry<String, QueryToken>> sorted = null;
		if (type.equals("TR")) {
			sorted = MyItemSorter.sortQTokensByTR(tokenMap);
		} else if (type.equals("PR")) {
			sorted = MyItemSorter.sortQTokensByTR(tokenMap);
		}
		HashMap<String, QueryToken> tempMap = new HashMap<>();
		int index = 0;
		for (Map.Entry<String, QueryToken> entry : sorted) {
			tempMap.put(entry.getKey(), entry.getValue());
			index++;
			if (index == MAX_TOKEN_IN_QUERY)
				break;
		}
		return tempMap;
	}

	public String getBugTitle() {
		return this.bugtitle;
	}

	protected HashMap<String, Double> gatherScores(List<Map.Entry<String, QueryToken>> sortedList,
			HashMap<String, Double> scoreMap) {
		for (int i = 0; i < sortedList.size(); i++) {
			double doi = getDOI(i, sortedList.size());
			String key = sortedList.get(i).getKey();
			if (scoreMap.containsKey(key)) {
				double updatedDOI = scoreMap.get(key) + doi;
				scoreMap.put(key, updatedDOI);
			} else {
				scoreMap.put(key, doi);
			}
		}
		return scoreMap;
	}

	protected String getQueryFinalizedBorda(HashMap<String, QueryToken> trMap, HashMap<String, QueryToken> prMap,
			HashMap<String, QueryToken> trcMap, HashMap<String, QueryToken> prcMap) {
		List<Map.Entry<String, QueryToken>> trList = MyItemSorter.sortQTokensByScoreKey(trMap, "TR");
		List<Map.Entry<String, QueryToken>> prList = MyItemSorter.sortQTokensByScoreKey(trMap, "PR");
		List<Map.Entry<String, QueryToken>> trcList = MyItemSorter.sortQTokensByScoreKey(trMap, "TRC");
		List<Map.Entry<String, QueryToken>> prcList = MyItemSorter.sortQTokensByScoreKey(trMap, "PRC");
		HashMap<String, Double> scoreMap = new HashMap<>();
		scoreMap = gatherScores(trList, scoreMap);
		scoreMap = gatherScores(prList, scoreMap);
		scoreMap = gatherScores(trcList, scoreMap);
		scoreMap = gatherScores(prcList, scoreMap);

		return getQueryFinalizedBorda(scoreMap);

	}

	protected String getQueryFinalizedBorda(HashMap<String, Double> combineddb) {

		List<Map.Entry<String, Double>> sorted = ItemSorter.sortHashMapDouble(combineddb);

		ArrayList<String> suggested = new ArrayList<>();
		int MAX_QUERY_SIZE = StaticData.SUGGESTED_KEYWORD_COUNT;

		if (StaticData.USE_DYNAMIC_KEYWORD_THRESHOLD) {
			MAX_QUERY_SIZE = (int) (sorted.size() * StaticData.KEYWORD_RATIO);
		}

		for (int i = 0; i < sorted.size(); i++) {
			String token = sorted.get(i).getKey();

			suggested.add(token);
			if (suggested.size() == MAX_QUERY_SIZE) {
				break;
			}
		}

		String queryStr = MiscUtility.list2Str(suggested);
		String expanded = new WordNormalizer().expandCCWords(queryStr);
		return new StopWordManager().getRefinedSentence(expanded);
	}

	public String deliverBestQuery() {

		SearchTermProvider stProvider = new SearchTermProvider(repository, bugID, bugtitle, bugReport);
		String trQuery = stProvider.provideSearchQuery("TR");
		String prQuery = stProvider.provideSearchQuery("PR");
		String tprQuery = stProvider.provideSearchQuery("TPR");
		String trcQuery = stProvider.provideSearchQuery("TRC");
		String prcQuery = stProvider.provideSearchQuery("PRC");
		String tprcQuery = stProvider.provideSearchQuery("TPRC");
		// String allQuery = stProvider.provideSearchQuery("ALL");

		HashMap<String, String> candidateQueryMap = new HashMap<>();
		candidateQueryMap.put("TR", bugID + "\t" + trQuery);
		candidateQueryMap.put("PR", bugID + "\t" + prQuery);
		candidateQueryMap.put("TPR", bugID + "\t" + tprQuery);
		candidateQueryMap.put("TRC", bugID + "\t" + trcQuery);
		candidateQueryMap.put("PRC", bugID + "\t" + prcQuery);
		candidateQueryMap.put("TPRC", bugID + "\t" + tprcQuery);
		// candidateQueryMap.put("ALL", bugID + "\t" + allQuery);

		System.out.println("Candidates:" + candidateQueryMap);

		// BestQueryPredictor bestQueryPredictor = new BestQueryPredictor(repository,
		// bugID, candidateQueryMap, entCalc);
		BestQueryPredictorSampled bestQueryPredictor = new BestQueryPredictorSampled(repository, bugID,
				candidateQueryMap, TECHNIQUE_NAME, predModelCount);
		return bestQueryPredictor.deliverBestQuery();

	}

}
