package strict.ca.usask.cs.srlab.strict.test;

import java.util.ArrayList;
import java.util.HashMap;
import org.junit.BeforeClass;
import org.junit.Test;
import kevic.TFIDFManager;
import rocchio.CandidateTermCollector;
import rocchio.RocchioRQMaker;
import rocchio.RocchioRankProvider;
import strict.ca.usask.cs.srlab.strict.config.StaticData;
import strict.lucenecheck.LuceneSearcher;
import strict.text.normalizer.TextNormalizer;
import strict.utility.BugReportLoader;
import strict.utility.MiscUtility;

public class RocchioTest {

	@BeforeClass
	public static void setup() {
		/* the path should be changed to replication package dir */
		StaticData.HOME_DIR = "C:\\MyWorks\\MyResearch\\STRICT\\experiment\\TSE-Experiment-2018";
		StaticData.SUGGESTED_KEYWORD_COUNT = 10;
	}

	protected HashMap<String, Double> getDFRatio(String repoName) {
		TFIDFManager tfIDFManager = new TFIDFManager(repoName);
		tfIDFManager.calculateIDF();
		return tfIDFManager.dfRatioMap;
	}

	@Test
	public void testSinleQueryMaker() {
		// TODO Auto-generated method stub
		int bugID = 815;
		String repoName = "adempiere-3.1.0";
		String searchQuery = BugReportLoader.loadBugReportTitle(repoName, bugID);

		String initQuery = new TextNormalizer().normalizeSimpleCodeDiscardSmall(searchQuery);

		HashMap<String, Double> dfRatioMap = getDFRatio(repoName);

		LuceneSearcher searcher0 = new LuceneSearcher(bugID, repoName, initQuery);
		ArrayList<String> retResults = searcher0.performVSMSearchList(false);
		CandidateTermCollector ctCollector = new CandidateTermCollector(repoName, retResults, initQuery, dfRatioMap,
				TFIDFManager.idfMap);
		ctCollector.collectSourceTermStats();

		// now collect the reformulated queries
		RocchioRankProvider drankProvider = new RocchioRankProvider(bugID, repoName, initQuery, ctCollector);
		ArrayList<String> roccQueryTokens = drankProvider.provideRocchioRank();
		String roccQuery = MiscUtility.list2Str(roccQueryTokens);
		System.out.println(roccQuery);
	}
	

	@Test
	public void testRepoQueryMakerForTitle() {
		String repoName = "adempiere-3.1.0";
		RocchioRQMaker rocc = new RocchioRQMaker(repoName, true);
		ArrayList<String> queries = rocc.makeRocchioQueries();
		MiscUtility.showItems(queries);
	}

}
