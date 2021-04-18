package strict.query.evaluation;

import java.util.ArrayList;
import java.util.HashMap;
import scanniello.method.LucenePRSearcher;
import strict.ca.usask.cs.srlab.strict.config.StaticData;
import strict.lucenecheck.LuceneSearcher;
import strict.lucenecheck.MethodResultRankMgr;
import strict.utility.QueryLoader;
import strict.utility.SelectedBugs;

public class QueryPerformanceCalc {

	String repoName;
	ArrayList<Double> RRList;
	ArrayList<Double> APList;
	public static int TOPKQTERMS = 1024;

	HashMap<Integer, Integer> baseRankMap;
	public static double sumAP = 0;
	public static double sumRR = 0;
	public static double sumTopKAcc = 0;

	public static ArrayList<Double> masterHitkList = new ArrayList<Double>();
	public static ArrayList<Double> masterAPList = new ArrayList<Double>();
	public static ArrayList<Double> masterRRList = new ArrayList<Double>();

	String approachQueryFile;
	ArrayList<Integer> selectedBugs;
	HashMap<Integer, String> queryMap;

	double HitK = 0;
	double mapK = 0;
	double mrrK = 0;
	static int totalBugs = 0;

	public static boolean useHQB = false;
	public static boolean useLQB = false;
	public static boolean useBaseline = false;
	public static boolean useScanniello = false;

	public QueryPerformanceCalc(String repoName, String resultKey, String approachFolder) {
		this.repoName = repoName;
		this.approachQueryFile = StaticData.HOME_DIR + "/" + approachFolder + "/query/" + repoName + "/" + resultKey
				+ ".txt";
		if (useBaseline) {
			this.approachQueryFile = StaticData.HOME_DIR + "/Baseline/query/query-whole/" + repoName + ".txt";
		}

		if (useHQB) {
			this.selectedBugs = SelectedBugs.loadSelectedHQBBugs(repoName);
		} else if (useLQB) {
			this.selectedBugs = SelectedBugs.loadSelectedLQBBugs(repoName);
		} else {
			this.selectedBugs = SelectedBugs.loadSelectedBugs(repoName);
		}

		this.RRList = new ArrayList<>();
		this.APList = new ArrayList<>();
		this.queryMap = loadQueries(this.approachQueryFile);
	}

	protected HashMap<Integer, String> loadQueries(String queryFile) {
		HashMap<Integer, String> tempMap = QueryLoader.loadQuery(queryFile);
		HashMap<Integer, String> myQueryMap = new HashMap<Integer, String>();
		for (int bugID : this.selectedBugs) {
			if (tempMap.containsKey(bugID)) {
				myQueryMap.put(bugID, tempMap.get(bugID));
			}
		}
		return tempMap;
	}

	protected String extractQuery(String line) {
		String[] words = line.split("\\s+");
		String temp = new String();
		for (int i = 1; i < words.length; i++) {
			temp += words[i] + "\t";
			if (i == TOPKQTERMS)
				break;
		}
		return temp.trim();
	}

	protected double getRR(int firstGoldIndex) {
		if (firstGoldIndex <= 0)
			return 0;
		return 1.0 / firstGoldIndex;
	}

	protected double getRR(ArrayList<Integer> foundIndices) {
		if (foundIndices.isEmpty())
			return 0;
		double min = 10000;
		for (int index : foundIndices) {
			if (index > 0 && index < min) {
				min = index;
			}
		}
		return 1.0 / min;
	}

	protected double getAP(ArrayList<Integer> foundIndices) {
		int indexcount = 0;
		double sumPrecision = 0;
		if (foundIndices.isEmpty())
			return 0;
		for (int index : foundIndices) {
			indexcount++;
			double precision = (double) indexcount / index;
			sumPrecision += precision;
		}
		return sumPrecision / indexcount;
	}

	protected double getPrecision(ArrayList<Integer> foundIndices, ArrayList<String> resultList) {
		// calculating regular precision
		return (double) foundIndices.size() / resultList.size();
	}

	protected double getRecall(ArrayList<Integer> foundIndices, ArrayList<String> goldset) {
		// calculating recall
		return (double) foundIndices.size() / goldset.size();
	}

	public void getQueryPerformance(int TOPCUT) {

		double sumRR = 0;
		double sumAP = 0;
		double TopKAcc = 0;
		int found = 0;

		for (int bugID : selectedBugs) {
			String searchQuery = this.queryMap.get(bugID);

			ArrayList<Integer> indices = new ArrayList<Integer>();

			if (useScanniello) {
				LucenePRSearcher searcher = new LucenePRSearcher(bugID, repoName, searchQuery);
				LucenePRSearcher.TOPK_RESULTS = TOPCUT;
				indices = searcher.getGoldFileIndices();
			} else {
				LuceneSearcher searcher = new LuceneSearcher(bugID, repoName, searchQuery);
				LuceneSearcher.TOPK_RESULTS = TOPCUT;
				indices = searcher.getGoldFileIndices();
			}

			double rr = 0, ap = 0;

			if (!indices.isEmpty()) {
				rr = getRR(indices);
				if (rr > 0) {
					sumRR += rr;
				}

				ap = getAP(indices);
				if (ap > 0) {
					sumAP += ap;
					found++;
				}
			}
		}

		// calculating MRR and MAP
		double MRR = sumRR / selectedBugs.size();
		double MAP = sumAP / selectedBugs.size();
		TopKAcc = (double) found / selectedBugs.size();

		this.HitK = TopKAcc;
		this.mapK = MAP;
		this.mrrK = MRR;

		// System.out.println("Done: "+repoName);
		// System.out.println(TopKAcc+",\t" + MAP + ",\t" + MRR+"," );

		QueryPerformanceCalc.sumAP += MAP;
		QueryPerformanceCalc.sumRR += MRR;
		QueryPerformanceCalc.sumTopKAcc += TopKAcc;

		// storing in the list
		masterHitkList.add(TopKAcc);
		masterAPList.add(MAP);
		masterRRList.add(MRR);

		// clearing the keys
		MethodResultRankMgr.keyMap.clear();
		LucenePRSearcher.prScoreMap.clear();
	}

	public double getHitK() {
		return this.HitK;
	}

	public double getMAPK() {
		return this.mapK;
	}

	public double getMRRK() {
		return this.mrrK;
	}
}
