package strict.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.jgraph.graph.DefaultEdge;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import strict.stopwords.StopWordManager;
import strict.text.normalizer.TextNormalizer;
import strict.utility.BugReportLoader;
import strict.utility.ContentWriter;
import strict.utility.ItemSorter;
import strict.utility.MiscUtility;
import strict.utility.MyItemSorter;
import strict.ca.usask.cs.srlab.strict.config.StaticData;
import strict.graph.KCoreScoreProvider;
import strict.graph.POSNetworkMaker;
import strict.graph.POSRankProvider;
import strict.graph.TextRankProvider;
import strict.graph.WordNetworkMaker;

public class SearchTermProvider {

	int bugID;
	String repository;
	String bugtitle;
	String bugReport;
	final int MAX_TOKEN_IN_QUERY = 10;
	static MaxentTagger tagger = null;

	boolean includeTFIDF = false;
	boolean addTitleScores = false;
	boolean customInitialize = false;

	/****** strong options *******/
	boolean addTitle = true;
	boolean applyDynamicSize = false;
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
		this.bugtitle = title;
		this.bugReport = bugReport;
		if (tagger == null) {
			tagger = new MaxentTagger("./models/english-left3words-distsim.tagger");
		}
		this.sentences = getAllSentences();
		this.textGraph = new WordNetworkMaker(sentences).createWordNetwork();
		this.wtextGraph = new WordNetworkMaker(sentences).createWeightedWordNetwork();
		this.posGraph = new POSNetworkMaker(sentences).createPOSNetwork();
		this.wposGraph=new POSNetworkMaker(sentences).createWeightedPOSNetwork();
	}

	protected HashMap<String, QueryToken> getQueryCoreRankScoresTR() {
		KCoreScoreProvider kcsProvider = new KCoreScoreProvider(wtextGraph, StaticData.KCORE_SIZE);
		HashMap<String, Double> kcsMap = kcsProvider.provideKCoreScores();
		// HashMap<String, Double> kcsMap = kcsProvider
		// .provideKCoreScoresV2(false);
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

	protected HashMap<String, QueryToken> getQueryCoreRankScoresPR() {
		KCoreScoreProvider kcsProvider = new KCoreScoreProvider(wposGraph, StaticData.KCORE_SIZE);
		HashMap<String, Double> kcsMap = kcsProvider.provideKCoreScores();
		// HashMap<String, Double> kcsMap =
		// kcsProvider.provideKCoreScoresV2(true);
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

	protected ArrayList<String> extractNormalizedSentencesV2(String content) {
		content = content.replace("\n", ". ");
		String separator = "(?<=[.?!:;])\\s+(?=[a-zA-Z0-9])";
		String[] sentences = content.split(separator);
		ArrayList<String> normSentences = new ArrayList<>();
		for (String sentence : sentences) {
			// String normSentence = new TextNormalizer()
			// .normalizeSimpleCodeDiscardSmall(sentence);
			String normSentence = new TextNormalizer().normalizeSimpleDiscardSmallwithOrder(sentence);
			if (!normSentence.trim().isEmpty()) {
				normSentences.add(normSentence);
			}
		}
		return normSentences;
	}

	protected DirectedGraph<String, DefaultEdge> getWordNetwork(ArrayList<String> sentences) {
		WordNetworkMaker wnMaker = new WordNetworkMaker(sentences);
		return wnMaker.createWordNetwork();
	}

	protected SimpleDirectedWeightedGraph<String, DefaultWeightedEdge> getWeightedWordNetwork(
			ArrayList<String> sentences) {
		WordNetworkMaker wnMaker = new WordNetworkMaker(sentences);
		return wnMaker.createWeightedWordNetwork();
	}

	protected DirectedGraph<String, DefaultEdge> getPOSNetwork(ArrayList<String> sentences) {
		POSNetworkMaker pnMaker = new POSNetworkMaker(sentences);
		return pnMaker.createPOSNetwork();
	}

	protected SimpleDirectedWeightedGraph<String, DefaultWeightedEdge> getWeightedPOSNetwork(
			ArrayList<String> sentences) {
		POSNetworkMaker wnMaker = new POSNetworkMaker(sentences);
		return wnMaker.createWeightedPOSNetwork();
	}

	protected ArrayList<String> getAllSentences() {
		QTextCollector textcollector = new QTextCollector(this.bugtitle, this.bugReport);
		return textcollector.collectQuerySentencesV3();
	}

	protected HashMap<String, QueryToken> initializeTokensDB(DirectedGraph<String, DefaultEdge> myGraph) {
		HashMap<String, QueryToken> tokendb = new HashMap<>();
		for (String node : myGraph.vertexSet()) {
			QueryToken qtoken = new QueryToken();
			qtoken.token = node;
			tokendb.put(node, qtoken);
		}
		return tokendb;
	}

	protected HashMap<String, QueryToken> initializeTokensDB(
			SimpleDirectedWeightedGraph<String, DefaultWeightedEdge> myGraph) {
		HashMap<String, QueryToken> tokendb = new HashMap<>();
		for (String node : myGraph.vertexSet()) {
			QueryToken qtoken = new QueryToken();
			qtoken.token = node;
			tokendb.put(node, qtoken);
		}
		return tokendb;
	}

	protected HashMap<String, QueryToken> getQueryTextRankScores() {
		// collect query token scores
		HashMap<String, QueryToken> tokendb = initializeTokensDB(textGraph);
		TextRankProvider trProvider = new TextRankProvider(this.textGraph, tokendb);
		return trProvider.calculateTextRank();
	}

	protected HashMap<String, QueryToken> getQueryPOSRankScores() {
		// collect query token scores
		HashMap<String, QueryToken> tokendb = initializeTokensDB(this.posGraph);
		POSRankProvider prProvider = new POSRankProvider(this.posGraph, tokendb);
		return prProvider.calculatePOSRank();
	}

	protected String normalizeTitle(String title) {
		return new TextNormalizer().normalizeSimpleDiscardSmallwithOrder(title);
	}

	protected HashMap<String, QueryToken> getTextRank() {
		HashMap<String, QueryToken> textRankMap = new HashMap<>();
		textRankMap = getQueryTextRankScores();
		textRankMap = filterStopWords(textRankMap);
		textRankMap = filterLowScores(textRankMap, "TR");
		textRankMap = MiscUtility.normalizeScore(textRankMap, "TR");
		return textRankMap;
	}

	protected HashMap<String, QueryToken> getPOSRank() {
		HashMap<String, QueryToken> posRankMap = getQueryPOSRankScores();
		posRankMap = filterStopWords(posRankMap);
		posRankMap = filterLowScores(posRankMap, "PR");
		posRankMap = MiscUtility.normalizeScore(posRankMap, "PR");
		return posRankMap;
	}

	public String provideSearchQuery(String scoreKey) {
		// forming query without any training
		this.bugtitle = normalizeTitle(this.bugtitle);

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
			coreRankMapTR = getQueryCoreRankScoresTR();
			combineddb = transferScores(coreRankMapTR, "TRC");
			break;
		case "PRC":
			coreRankMapPR = getQueryCoreRankScoresPR();
			combineddb = transferScores(coreRankMapPR, "PRC");
			break;
		case "TPRC":
			coreRankMapTR = getQueryCoreRankScoresTR();
			coreRankMapPR = getQueryCoreRankScoresPR();
			combineddb = addCoreRankScores(combineddb, coreRankMapPR);
			combineddb = addCoreRankScores(combineddb, coreRankMapTR);
			break;
		case "ALL":
			textRankMap = getTextRank();
			posRankMap = getPOSRank();
			combineddb = getCombinedBordaScores(textRankMap, posRankMap);
			coreRankMapTR = getQueryCoreRankScoresTR();
			combineddb = addCoreRankScores(combineddb, coreRankMapPR);
			coreRankMapPR = getQueryCoreRankScoresPR();
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

		if (addTitleScores) {
			combineddb = this.addTitleTermScores(combineddb);
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

	protected HashMap<String, Double> addTitleTermScores(HashMap<String, Double> combineddb) {

		bugtitle = new TextNormalizer().normalizeSimpleCodeDiscardSmall(bugtitle);
		String[] titlewords = bugtitle.split("\\p{Punct}+|\\d+|\\s+");

		for (int i = 0; i < titlewords.length; i++) {

			double doi = getDOI(0, titlewords.length);
			String key = titlewords[i];
			if (!combineddb.containsKey(key)) {
				combineddb.put(key, doi * StaticData.gamma);
			} else {
				double score = combineddb.get(key) + doi * StaticData.gamma;
				combineddb.put(key, score);
			}
		}
		return combineddb;
	}

	protected ArrayList<String> addBugTitle(ArrayList<String> suggested) {
		bugtitle = new TextNormalizer().normalizeSimpleCodeDiscardSmall(bugtitle);
		ArrayList<String> terms = MiscUtility.str2List(bugtitle);
		// add title carefully by avoiding duplicates
		for (String term : terms) {
			if (!suggested.contains(term)) {
				suggested.add(term);
			}
		}
		return suggested;
	}

	protected void addCandidateScores(HashMap<String, Double> combineddb, HashMap<String, QueryToken> tokenRankMap,
			HashMap<String, QueryToken> posRankMap) {
		String candidateFile = StaticData.HOME_DIR + "/weight-training/" + repository + "/" + bugID + ".txt";

		ArrayList<String> candidateTerms = new ArrayList<>();
		for (String key : combineddb.keySet()) {
			if (key.trim().isEmpty())
				continue;
			String cline = key;
			if (tokenRankMap.containsKey(key)) {
				cline += "\t" + tokenRankMap.get(key).textRankScore;
			} else {
				cline += "\t" + 0.0;
			}
			if (posRankMap.containsKey(key)) {
				cline += "\t" + posRankMap.get(key).posRankScore;
			} else {
				cline += "\t" + 0.0;
			}
			candidateTerms.add(cline);
		}
		ContentWriter.writeContent(candidateFile, candidateTerms);
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

	protected HashMap<String, QueryToken> filterStopWords(HashMap<String, QueryToken> tokenMap) {
		// filtering the stop words
		HashSet<String> tokens = new HashSet<>(tokenMap.keySet());
		StopWordManager stopManager = new StopWordManager();
		for (String key : tokens) {
			if (stopManager.stopList.contains(key)) {
				QueryToken qtoken = tokenMap.get(key);
				tokenMap.remove(qtoken);
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

	protected String filterStopWords(String sentence) {
		// refine the sentence
		return new StopWordManager().getRefinedSentence(sentence);
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

	protected String expandCCWords(String sentence) {
		// expanding the CC words
		String expanded = new String();
		String[] tokens = sentence.split("\\s+");
		for (String token : tokens) {
			ArrayList<String> decomposed = decomposeCamelCase(token);
			if (decomposed.size() > 1) {
				decomposed = MiscUtility.filterSmallTokens(decomposed);
				expanded += token + "\t" + MiscUtility.list2Str(decomposed) + "\t";
			} else {
				expanded += token + "\t";
			}
		}
		return expanded.trim();
	}

	protected ArrayList<String> decomposeCamelCase(String token) {
		// decomposing camel case tokens using regex
		ArrayList<String> refined = new ArrayList<>();
		String camRegex = "([a-z])([A-Z]+)";
		String replacement = "$1\t$2";
		String filtered = token.replaceAll(camRegex, replacement);
		String[] ftokens = filtered.split("\\s+");
		refined.addAll(Arrays.asList(ftokens));
		return refined;
	}

	public String collectBugTitle() {
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

		int DYNAMIC_SIZE = (int) (sorted.size() * StaticData.KEYWORD_RATIO); // 33%

		for (int i = 0; i < sorted.size(); i++) {
			String token = sorted.get(i).getKey();
			suggested.add(token);
			if (applyDynamicSize) {
				if (suggested.size() == DYNAMIC_SIZE) {
					break;
				}
			} else {
				if (suggested.size() == MAX_TOKEN_IN_QUERY) {
					break;
				}
			}
		}
		// add the title as is
		if (addTitle) {
			suggested = addBugTitle(suggested);
		}

		String queryStr = MiscUtility.list2Str(suggested);
		String expanded = expandCCWords(queryStr);
		return new StopWordManager().getRefinedSentence(expanded);
	}

	public static void main(String[] args) {
		int bugID = 5653;
		String repoName = "eclipse.jdt.debug";
		String title = "Bug 5653 – DCR: Debugger should catch uncaught exception by default";
		String bugReport = BugReportLoader.loadBugReport(repoName, bugID);

		SearchTermProvider stProvider = new SearchTermProvider(repoName, bugID, title, bugReport);
		String trQuery = stProvider.provideSearchQuery("TR");
		String prQuery = stProvider.provideSearchQuery("PR");
		String tprQuery = stProvider.provideSearchQuery("TPR");
		String trcQuery = stProvider.provideSearchQuery("TRC");
		String prcQuery = stProvider.provideSearchQuery("PRC");
		String tprcQuery = stProvider.provideSearchQuery("TPRC");
		String allQuery = stProvider.provideSearchQuery("ALL");

		System.out.println("TR: " + trQuery);
		System.out.println("PR: " + prQuery);
		System.out.println("TPR: " + tprQuery);
		System.out.println("TRC: " + trcQuery);
		System.out.println("PRC: " + prcQuery);
		System.out.println("TPRC: " + tprcQuery);
		System.out.println("ALL: " + allQuery);
	}
}
