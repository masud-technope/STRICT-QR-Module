package ca.usask.cs.srlab.strict.scanniello.method;

import java.util.ArrayList;
import java.util.HashMap;

import ca.usask.cs.srlab.strict.config.StaticData;
import ca.usask.cs.srlab.strict.utility.ContentWriter;
import ca.usask.cs.srlab.strict.utility.QueryLoader;
import ca.usask.cs.srlab.strict.utility.SelectedBugs;

public class RepoPRRankMaker {

    String repoName;
    HashMap<Integer, String> suggestedQueryMap;
    ArrayList<Integer> selectedBugs;
    String rankFile;
    String baseVersion;

    static boolean matchclasses = false;

    public RepoPRRankMaker(String repoName, boolean useBaseline) {
        this.repoName = repoName;
        String queryFile = getQueryFile(useBaseline);
        this.suggestedQueryMap = QueryLoader.loadQuery(queryFile);
        this.selectedBugs = SelectedBugs.loadSelectedBugs(repoName);
    }

    protected String getQueryFile(boolean useBaseline) {
        if (useBaseline) {
            return getBaselineQuery();
        } else {
            return getSTRICTQuery();
        }
    }

    protected String getBaselineQuery() {
        return StaticData.HOME_DIR + "/Baseline/query/query-whole/" + repoName + ".txt";
    }

    protected String getSTRICTQuery() {
        return StaticData.HOME_DIR + "/Proposed-STRICT/query/" + repoName + "/STRICT-best-query-dec23-8pm.txt";
    }

    public void setRankFile(String rankFile) {
        this.rankFile = rankFile;
    }

    public RepoPRRankMaker(String repoName, HashMap<Integer, String> queryMap) {
        this.repoName = repoName;
        this.suggestedQueryMap = queryMap;
        this.selectedBugs = SelectedBugs.loadSelectedBugs(repoName);
    }

    public ArrayList<String> collectQE() {
        ArrayList<String> ranks = new ArrayList<>();
        for (int bugID : this.selectedBugs) {
            if (suggestedQueryMap.containsKey(bugID)) {
                String suggested = suggestedQueryMap.get(bugID);
                LucenePRSearcher lsearch = new LucenePRSearcher(bugID, repoName, suggested.toLowerCase());
                int qe = lsearch.getFirstGoldRank();
                ranks.add(bugID + "\t" + qe);
            }
        }
        return ranks;
    }

    public void saveQE(ArrayList<String> ranks) {
        ContentWriter.writeContent(rankFile, ranks);
        System.out.println("Repo:" + repoName);
    }

}
