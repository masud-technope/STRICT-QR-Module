package ca.usask.cs.srlab.strict.kevic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import ca.usask.cs.srlab.strict.config.StaticData;
import ca.usask.cs.srlab.strict.lucenecheck.LuceneSearcher;
import ca.usask.cs.srlab.strict.lucenecheck.MethodResultRankMgr;
import ca.usask.cs.srlab.strict.utility.MiscUtility;
import ca.usask.cs.srlab.strict.utility.QueryLoader;
import ca.usask.cs.srlab.strict.utility.ContentWriter;

public class KevicDatasetMaker {

    String repoName;
    HashMap<Integer, String> queryMap;
    public static HashMap<String, Integer> masterKeywordMap = new HashMap<String, Integer>();
    int MAX_QE_FOR_HITK = 100;
    static String masterKeywordFile;

    public KevicDatasetMaker(String repoName) {
        this.repoName = repoName;
        String queryFile = getBaselineQueryFile();
        this.queryMap = QueryLoader.loadQuery(queryFile);
    }

    private String getBaselineQueryFile() {
        return StaticData.HOME_DIR + "/Baseline/query/query-whole/" + repoName + ".txt";
    }

    protected int getKeywordClassLabel(int bugID, String keyword) {
        LuceneSearcher searcher = new LuceneSearcher(bugID, this.repoName, keyword);
        int qe = searcher.getFirstGoldRank();
        if (qe > 0) {
            return 1;
        }
        return 0;
    }

    public void determineKeywordClass() {
        for (int bugID : this.queryMap.keySet()) {
            String myQuery = this.queryMap.get(bugID);
            HashSet<String> keywords = new HashSet<String>(MiscUtility.str2List(myQuery));
            for (String keyword : keywords) {
                if (keyword.trim().length() >= 2) {
                    int keywordClass = getKeywordClassLabel(bugID, keyword);
                    String keyID = repoName + "-" + bugID + "-" + keyword;
                    masterKeywordMap.put(keyID, keywordClass);
                }
            }
        }
        MethodResultRankMgr.keyMap.clear();
    }

    public void setMasterKeywordFile(String filePath) {
        masterKeywordFile = filePath;
    }

    public static void saveMasterKeywords() {
        // now save the keyword classes
        ArrayList<String> resultLines = new ArrayList<String>();
        for (String keyword : masterKeywordMap.keySet()) {
            int kclass = masterKeywordMap.get(keyword);
            String cline = keyword + "\t" + kclass;
            resultLines.add(cline);
        }
        ContentWriter.writeContent(masterKeywordFile, resultLines);
        System.out.println("Done!");
    }

}
