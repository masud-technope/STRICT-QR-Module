package strict.query.evaluation;

import java.util.ArrayList;
import java.util.HashMap;
import strict.lucenecheck.LuceneSearcher;
import strict.utility.ContentWriter;
import strict.utility.QueryLoader;
import strict.utility.SelectedBugs;

public class RepoRankMaker {

	String repoName;
	HashMap<Integer, String> suggestedQueryMap;
	ArrayList<Integer> selectedBugs;
	String rankFile;
	String queryFile;
	static int tooGood = 0;
	static boolean matchClasses = false;

	public RepoRankMaker(String repoName) {
		this.repoName = repoName;
		this.suggestedQueryMap = new HashMap<>();
		this.selectedBugs = SelectedBugs.loadSelectedBugs(repoName);
	}

	public void setRankFile(String rankFile) {
		this.rankFile = rankFile;
	}

	public void setQueryFile(String queryFile) {
		this.queryFile = queryFile;
	}

	public RepoRankMaker(String repoName, HashMap<Integer, String> queryMap) {
		this.repoName = repoName;
		this.suggestedQueryMap = queryMap;
		this.selectedBugs = SelectedBugs.loadSelectedBugs(repoName);
	}

	public ArrayList<String> collectQE() {
		ArrayList<String> ranks = new ArrayList<>();

		if (this.suggestedQueryMap.isEmpty()) {
			this.suggestedQueryMap = QueryLoader.loadQuery(this.queryFile);
		}

		for (int bugID : this.selectedBugs) {
			if (suggestedQueryMap.containsKey(bugID)) {
				String suggested = suggestedQueryMap.get(bugID);
				LuceneSearcher lsearch = new LuceneSearcher(bugID, repoName, suggested.toLowerCase());
				int qe = lsearch.getFirstGoldRank();
				ranks.add(bugID + "\t" + qe);
			}
		}
		return ranks;
	}

	public void saveQE(ArrayList<String> ranks) {
		ContentWriter.writeContent(this.rankFile, ranks);
		System.out.println("Repo:" + repoName);
	}

}
