package rocchio;

import java.util.ArrayList;
import java.util.HashMap;
import kevic.TFIDFManager;
import strict.ca.usask.cs.srlab.strict.config.StaticData;
import strict.lucenecheck.LuceneSearcher;
import strict.text.normalizer.TextNormalizer;
import strict.utility.BugReportLoader;
import strict.utility.ContentWriter;
import strict.utility.MiscUtility;
import strict.utility.SelectedBugs;

public class RocchioRQMaker {

	String repoName;
	String queryFile;
	String roccQueryFile;
	final int MAXQTERMS = 1023;
	boolean isWhole;

	public RocchioRQMaker(String repoName, boolean isWhole) {
		this.repoName = repoName;
		this.isWhole = isWhole;
	}

	public void setRocchioQueryFile(String filePath) {
		this.roccQueryFile = filePath;
	}

	protected HashMap<String, Double> getDFRatio() {
		TFIDFManager tfIDFManager = new TFIDFManager(repoName);
		tfIDFManager.calculateIDF();
		return tfIDFManager.dfRatioMap;
	}

	protected String extractLimitedQuery(String line) {
		String[] words = line.split("\\s+");
		String temp = new String();
		for (int i = 0; i < words.length; i++) {
			temp += words[i] + "\t";
			if (i == MAXQTERMS)
				break;
		}
		return temp.trim();
	}

	public ArrayList<String> makeRocchioQueries() {
		// preparing Dice queries
		ArrayList<String> queries = new ArrayList<>();
		HashMap<String, Double> dfRatioMap = getDFRatio();
		ArrayList<Integer> selectedBugs = SelectedBugs.loadSelectedBugs(repoName);
		for (int bugID : selectedBugs) {

			// Rocchio-I = title, Rocchio-II = whole texts

			String bugReport = new String();
			if (isWhole) {
				bugReport = BugReportLoader.loadBugReport(repoName, bugID);
			} else {
				bugReport = BugReportLoader.loadBugReportTitle(repoName, bugID);
			}

			String initQuery = new TextNormalizer().normalizeSimpleCodeDiscardSmall(bugReport);
			// curtail the query length
			initQuery = extractLimitedQuery(initQuery);

			LuceneSearcher searcher0 = new LuceneSearcher(bugID, repoName, initQuery);
			ArrayList<String> retResults = searcher0.performVSMSearchList(false);
			CandidateTermCollector ctCollector = new CandidateTermCollector(repoName, retResults, initQuery, dfRatioMap,
					TFIDFManager.idfMap);
			ctCollector.collectSourceTermStats();

			// now collect the reformulated queries
			RocchioRankProvider drankProvider = new RocchioRankProvider(bugID, repoName, initQuery, ctCollector);
			ArrayList<String> roccQueryTokens = drankProvider.provideRocchioRank();
			String roccQuery = MiscUtility.list2Str(roccQueryTokens);
			queries.add(bugID + "\t" + initQuery + "\t" + roccQuery);
		}

		// clean up!
		TFIDFManager.idfMap.clear();

		return queries;
	}

	public void saveRocchioQueries(ArrayList<String> queries) {
		ContentWriter.writeContent(this.roccQueryFile, queries);
	}

}
