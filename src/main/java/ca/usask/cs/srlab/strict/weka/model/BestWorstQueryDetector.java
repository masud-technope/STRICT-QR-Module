package ca.usask.cs.srlab.strict.weka.model;

import java.util.ArrayList;
import java.util.HashMap;

import ca.usask.cs.srlab.strict.config.StaticData;
import ca.usask.cs.srlab.strict.utility.ContentWriter;
import ca.usask.cs.srlab.strict.utility.QueryLoader;
import ca.usask.cs.srlab.strict.utility.SelectedBugs;

public class BestWorstQueryDetector {

    String repoName;
    ArrayList<Integer> selectedBugs;
    String[] scoreKeys;
    HashMap<String, HashMap<Integer, String>> masterQueryMap;
    HashMap<String, HashMap<Integer, Integer>> masterQEMap;
    String bestQueryFolder;
    String worstQueryFolder;

    public BestWorstQueryDetector(String repoName, String[] scoreKeys) {
        this.repoName = repoName;
        this.selectedBugs = SelectedBugs.loadSelectedBugs(repoName);
        this.scoreKeys = scoreKeys;
        // populating the masters
        this.masterQueryMap = collectMasterQueries(this.scoreKeys);
        this.masterQEMap = collectMasterQE(this.scoreKeys);
    }

    protected HashMap<Integer, String> loadBaseQueries() {
        String baseQueryFile = StaticData.HOME_DIR + "/Baseline/query/query-whole/" + repoName + ".txt";
        return QueryLoader.loadQuery(baseQueryFile);
    }

    protected HashMap<Integer, Integer> loadBaseQE() {
        String baseQEFile = StaticData.HOME_DIR + "/Baseline/rank/rank-whole/" + repoName + ".txt";
        return QueryLoader.loadQueryQE(baseQEFile);
    }

    protected HashMap<String, HashMap<Integer, String>> collectMasterQueries(String[] scoreKeys) {
        HashMap<String, HashMap<Integer, String>> masterQueryMap = new HashMap<>();
        for (String scoreKey : scoreKeys) {
            String queryFile = getQueryFileName(scoreKey);
            HashMap<Integer, String> tempQueryMap = QueryLoader.loadQuery(queryFile);
            if (tempQueryMap.isEmpty())
                continue;
            masterQueryMap.put(scoreKey, tempQueryMap);
        }
        return masterQueryMap;
    }

    protected HashMap<String, HashMap<Integer, Integer>> collectMasterQE(String[] scoreKeys) {
        HashMap<String, HashMap<Integer, Integer>> masterQEMap = new HashMap<>();
        for (String scoreKey : scoreKeys) {
            String qeFile = getQEFileName(scoreKey);
            HashMap<Integer, Integer> tempQEMap = QueryLoader.loadQueryQE(qeFile);
            if (tempQEMap.isEmpty())
                continue;
            masterQEMap.put(scoreKey, tempQEMap);
        }
        return masterQEMap;
    }

    protected String getQueryFileName(String scoreKey) {
        return StaticData.HOME_DIR + "/proposed-STRICT/query/" + repoName + "/STRICT-" + scoreKey + "-10-title.txt";
    }

    protected String getQEFileName(String scoreKey) {
        return StaticData.HOME_DIR + "/proposed-STRICT/rank/" + repoName + "/STRICT-" + scoreKey + "-10-title.txt";
    }

    public void setBestQueryFolder(String folderName) {
        this.bestQueryFolder = folderName;
    }

    public void setWorstQueryFolder(String folderName) {
        this.worstQueryFolder = folderName;
    }

    protected boolean checkBetterQE(int base, int target) {
        if (target > 0) {
            if (base > 0) {
                if (target < base) {
                    return true;
                } else if (target == base) {
                    return true;
                } else if (target > base) {
                    return false;
                }
            } else {
                if (target > 0) {
                    return true;
                }
            }
        } else {
            if (base == target) {
                return false;
            } else {
                return false;
            }
        }
        return false;
    }

    protected boolean checkWorseQE(int base, int target) {
        if (target > 0) {
            if (base > 0) {
                if (target < base) {
                    return false;
                } else if (target == base) {
                    return false;
                } else if (target > base) {
                    return true;
                }
            } else {
                if (target > 0) {
                    return false;
                }
            }
        } else {
            if (base == target) {
                return true;
            } else {
                return true;
            }
        }
        return true;
    }

    protected ArrayList<String> determineBestQuery() {
        ArrayList<String> bestQueries = new ArrayList<>();
        HashMap<Integer, Integer> baseQEMap = loadBaseQE();
        HashMap<Integer, String> baseQueryMap = loadBaseQueries();
        for (int bugID : selectedBugs) {
            int bestQE = baseQEMap.get(bugID);
            String bestQuery = baseQueryMap.get(bugID);
            for (String key : masterQEMap.keySet()) {
                HashMap<Integer, Integer> tempQEMap = masterQEMap.get(key);
                if (tempQEMap.containsKey(bugID)) {
                    int tempQE = tempQEMap.get(bugID);
                    if (checkBetterQE(bestQE, tempQE)) {
                        bestQE = tempQE;
                        HashMap<Integer, String> tempQueryMap = masterQueryMap.get(key);
                        if (tempQueryMap.containsKey(bugID)) {
                            bestQuery = tempQueryMap.get(bugID);
                        }
                    }
                }
            }

            String queryLine = bugID + "\t" + bestQuery;
            // System.out.println(bugID + "\t" + bestQE);
            bestQueries.add(queryLine);
        }
        return bestQueries;
    }

    protected ArrayList<String> determineWorstQuery() {
        HashMap<Integer, String> baseQueryMap = loadBaseQueries();
        HashMap<Integer, Integer> baseQEMap = loadBaseQE();
        ArrayList<String> worstQueries = new ArrayList<>();

        for (int bugID : selectedBugs) {
            int worstQE = baseQEMap.get(bugID);
            String worstQuery = baseQueryMap.get(bugID);
            for (String key : masterQEMap.keySet()) {
                HashMap<Integer, Integer> tempQEMap = masterQEMap.get(key);
                if (tempQEMap.containsKey(bugID)) {
                    int tempQE = tempQEMap.get(bugID);
                    if (checkWorseQE(worstQE, tempQE)) {
                        worstQE = tempQE;
                        HashMap<Integer, String> tempQueryMap = masterQueryMap.get(key);
                        if (tempQueryMap.containsKey(bugID)) {
                            worstQuery = tempQueryMap.get(bugID);
                        }
                    }
                }
            }

            String queryLine = bugID + "\t" + worstQuery;
            System.out.println(bugID + "\t" + worstQE);
            worstQueries.add(queryLine);
        }
        return worstQueries;
    }

    protected String getBestQueryFile() {
        return this.bestQueryFolder + "/" + repoName + ".txt";
    }

    protected String getWorstQueryFile() {
        return this.worstQueryFolder + "/" + repoName + ".txt";
    }

    public void collectBestQueries() {
        ArrayList<String> bestQueries = determineBestQuery();
        ContentWriter.writeContent(getBestQueryFile(), bestQueries);
    }

    public void collectWorstQueries() {
        ArrayList<String> worstQueries = determineWorstQuery();
        ContentWriter.writeContent(getWorstQueryFile(), worstQueries);
    }

}
