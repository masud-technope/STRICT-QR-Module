package strict.ca.usask.cs.srlab.strict.test;

import java.util.ArrayList;
import org.junit.Test;
import strict.ca.usask.cs.srlab.strict.config.StaticData;
import strict.query.SearchQueryProvider;
import strict.utility.MiscUtility;
import strict.utility.SelectedBugs;

public class SearchQueryProviderTest {

	@Test
	public void testProvideSearchQueries() {
		String repoName = "bookkeeper-4.1.0";
		ArrayList<Integer> selectedBugs = SelectedBugs.loadSelectedBugs(repoName);
		String scoreKey = "TPR";
		StaticData.ADD_CODE_ELEM=true;
		StaticData.ADD_TITLE=false;
		
		ArrayList<String> queries = new SearchQueryProvider(repoName, scoreKey, selectedBugs).provideSearchQueries();
		MiscUtility.showItems(queries);
	}
}
