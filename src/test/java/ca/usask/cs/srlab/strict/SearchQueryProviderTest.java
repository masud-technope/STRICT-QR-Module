package ca.usask.cs.srlab.strict;

import java.util.ArrayList;

import org.junit.Test;
import ca.usask.cs.srlab.strict.config.StaticData;
import ca.usask.cs.srlab.strict.query.SearchQueryProvider;
import ca.usask.cs.srlab.strict.utility.MiscUtility;
import ca.usask.cs.srlab.strict.utility.SelectedBugs;

public class SearchQueryProviderTest {

    @Test
    public void testProvideSearchQueries() {
        String repoName = "eclipse.jdt.debug";
        ArrayList<Integer> selectedBugs = SelectedBugs.loadSelectedBugs(repoName);
        String scoreKey = "TPR";
        StaticData.ADD_CODE_ELEM = false;
        StaticData.ADD_TITLE = true;
        ArrayList<String> queries = new SearchQueryProvider(repoName, scoreKey, selectedBugs).provideSearchQueries();
        MiscUtility.showItems(queries);
    }
}
