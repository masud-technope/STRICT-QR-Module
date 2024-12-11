package ca.usask.cs.srlab.strict;

import org.junit.Test;
import ca.usask.cs.srlab.strict.config.StaticData;
import ca.usask.cs.srlab.strict.query.SearchTermProvider;
import ca.usask.cs.srlab.strict.utility.BugReportLoader;

public class SearchTermProviderTest {


    @Test
    public void testTPRQuery() {
        String repoName = "eclipse.jdt.debug";
        int bugID = 217994;
        String title = "[patch][launching] Run/Debug honors JRE VM args before Launcher VM args";
        String bugReport = BugReportLoader.loadBugReport(repoName, bugID);
        StaticData.SUGGESTED_KEYWORD_COUNT = 10;

        SearchTermProvider provider = new SearchTermProvider(repoName, bugID, title, bugReport);
        String query = provider.provideSearchQuery("TPR");
        System.out.println(query);
    }
}
