package strict.ca.usask.cs.srlab.strict.test;

import org.junit.Test;
import strict.ca.usask.cs.srlab.strict.config.StaticData;
import strict.query.SearchTermProvider;
import strict.utility.BugReportLoader;

public class SearchTermProviderTest {
	
	
	@Test
	public void testTPRQuery() {
		String repoName="eclipse.jdt.debug";
		int bugID=217994;
		String title ="[patch][launching] Run/Debug honors JRE VM args before Launcher VM args";
		String bugReport = BugReportLoader.loadBugReport(repoName, bugID);
		StaticData.SUGGESTED_KEYWORD_COUNT = 10;
		
		SearchTermProvider provider=new SearchTermProvider(repoName, bugID, title, bugReport);
		String query = provider.provideSearchQuery("TPR");
		System.out.println(query);
	}
}
