package ca.usask.cs.srlab.strict.rocchio;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import ca.usask.cs.srlab.strict.config.StaticData;
import ca.usask.cs.srlab.strict.utility.ItemSorter;

public class RocchioRankProvider {

    int bugID;
    String repository;
    String searchQuery;
    HashSet<String> uniqueTerms;
    String indexFolder;
    static HashMap<String, Double> idf;
    HashMap<String, HashMap<String, Integer>> fileTermMap;
    HashMap<String, Double> rocchioScoreMap;
    CandidateTermCollector ctCollector;

    public RocchioRankProvider(int bugID, String repoName, String searchQuery) {
        this.bugID = bugID;
        this.repository = repoName;
        this.searchQuery = searchQuery;
        this.uniqueTerms = new HashSet<>();
        this.indexFolder = getIndexFolder();
        this.idf = new HashMap<>();
        this.fileTermMap = new HashMap<>();
        this.rocchioScoreMap = new HashMap<>();
    }

    private String getIndexFolder() {
        return StaticData.HOME_DIR + "/Lucene/index-method/" + repository;
    }

    public RocchioRankProvider(int bugID, String repoName, String searchQuery, CandidateTermCollector ctCollector) {
        this.bugID = bugID;
        this.repository = repoName;
        this.searchQuery = searchQuery;
        this.uniqueTerms = new HashSet<>();
        this.indexFolder = getIndexFolder();
        this.idf = new HashMap<>();
        this.fileTermMap = new HashMap<>();
        this.rocchioScoreMap = new HashMap<>();
        this.ctCollector = ctCollector;
    }

    protected void getCandidateTermStats() {
        this.uniqueTerms = ctCollector.getDistinctTerms();
        this.fileTermMap = ctCollector.getFileTermMap();
        this.idf = this.ctCollector.idfMap;
    }

    protected void calculateTFIDFScores() {
        // calculating the TF-IDF scores
        for (String url : fileTermMap.keySet()) {
            HashMap<String, Integer> wordcount = this.fileTermMap.get(url);
            for (String word : wordcount.keySet()) {
                int tf = wordcount.get(word);
                double idfScore = 0;
                if (this.idf.containsKey(word)) {
                    idfScore = this.idf.get(word);
                }
                double tfidf = tf * idfScore;
                if (this.rocchioScoreMap.containsKey(word)) {
                    double score = this.rocchioScoreMap.get(word) + tfidf;
                    this.rocchioScoreMap.put(word, score);
                } else {
                    this.rocchioScoreMap.put(word, tfidf);
                }
            }
        }
    }

    public ArrayList<String> provideRocchioRank() {
        // provides term frequency
        this.getCandidateTermStats();
        // provides TF-IDF
        this.calculateTFIDFScores();
        List<Map.Entry<String, Double>> sorted = ItemSorter.sortHashMapDouble(this.rocchioScoreMap);
        ArrayList<String> ranked = new ArrayList<>();
        int MAX_SUGGESTED_TERMS = StaticData.SUGGESTED_KEYWORD_COUNT;

        for (Map.Entry<String, Double> entry : sorted) {
            // System.out.println(entry.getKey() + "\t" + entry.getValue());
            ranked.add(entry.getKey());
            if (ranked.size() == MAX_SUGGESTED_TERMS)
                break;
        }
        return ranked;
    }

}
