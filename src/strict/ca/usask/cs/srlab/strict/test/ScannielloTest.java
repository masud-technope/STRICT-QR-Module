package strict.ca.usask.cs.srlab.strict.test;

import java.util.ArrayList;
import org.junit.Test;
import scanniello.method.LucenePRSearcher;
import scanniello.method.RepoPRRankMaker;
import strict.utility.MiscUtility;

public class ScannielloTest {
	
	

	@Test
	public void getScannielloResults() {
		String repoName = "adempiere-3.1.0";
		String searchQuery = "LandedCost Cost Distribution type Cost work landed cost form select Costs cost distribution type working generating Landed cost allocation records Landed Cost";
		int bugID = 815;
		LucenePRSearcher searcher = new LucenePRSearcher(bugID, repoName, searchQuery);
		ArrayList<String> results= searcher.performVSMPRSearchList(false, true);
		System.out.println(results);
	}
	
	
	@Test
	public void getScannielloRanks() {
		String repoName="mahout-0.4";
		boolean useBaseline=true;
		RepoPRRankMaker maker=new RepoPRRankMaker(repoName, useBaseline);
		ArrayList<String> ranks=maker.collectQE();
		MiscUtility.showItems(ranks);
	}
	
}
