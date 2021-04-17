package strict.ca.usask.cs.srlab.strict.test;

import org.junit.Test;
import strict.ca.usask.cs.srlab.strict.config.StaticData;
import strict.query.SearchTermProvider;
import strict.utility.BugReportLoader;

public class SearchTermProviderTest {
	
	
	@Test
	public void testTPRQuery() {
		String repoName="bookkeeper-4.1.0";
		int bugID=355;
		String title ="Ledger recovery will mark ledger as closed with -1, in case of slow bookie is added to ensemble during  recovery add";
		String bugReport = BugReportLoader.loadBugReport(repoName, bugID);
		StaticData.SUGGESTED_KEYWORD_COUNT = 10;
		
		SearchTermProvider provider=new SearchTermProvider(repoName, bugID, title, bugReport);
		String query = provider.provideSearchQuery("PR");
		System.out.println(query);
	}
}
