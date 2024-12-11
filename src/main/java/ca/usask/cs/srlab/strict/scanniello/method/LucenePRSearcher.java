package ca.usask.cs.srlab.strict.scanniello.method;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import ca.usask.cs.srlab.strict.config.StaticData;
import ca.usask.cs.srlab.strict.lucenecheck.MethodResultRankMgr;
import ca.usask.cs.srlab.strict.utility.ContentLoader;
import ca.usask.cs.srlab.strict.utility.ItemSorter;

public class LucenePRSearcher {

    int bugID;
    String repository;
    String indexFolder;
    String field = "contents";
    String queries = null;
    int repeat = 0;
    boolean raw = true;
    String queryString = null;
    int hitsPerPage = 10;
    String searchQuery;
    public static int TOPK_RESULTS = 10;
    public static int ALL_RESULTS = 1000;
    ArrayList<String> results;
    public ArrayList<String> goldset;
    IndexReader reader = null;
    IndexSearcher searcher = null;
    Analyzer analyzer = null;

    public double precision = 0;
    public double recall = 0;
    public double precatk = 0;
    public double recrank = 0;

    public static HashMap<Integer, Double> prScoreMap = new HashMap<Integer, Double>();

    public LucenePRSearcher(int bugID, String repository, String searchQuery) {
        // initialization
        this.bugID = bugID;
        this.repository = repository;
        this.indexFolder = getIndexFolder();
        this.searchQuery = searchQuery;
        this.results = new ArrayList<>();
        this.goldset = new ArrayList<>();
        if (prScoreMap.isEmpty()) {
            prScoreMap = loadPRScores();
        }
    }

    protected String getIndexFolder() {
        return StaticData.HOME_DIR + "/Lucene/index-method/" + repository;
    }

    protected String getPRScoreFile() {
        return StaticData.HOME_DIR + "/Scanniello/Method-PageRank/" + repository + ".txt";
    }

    protected HashMap<Integer, Double> loadPRScores() {
        String scoreFile = getPRScoreFile();
        ArrayList<String> scoreLines = ContentLoader.getAllLinesOptList(scoreFile);
        HashMap<Integer, Double> tempMap = new HashMap<Integer, Double>();
        for (String scoreLine : scoreLines) {
            String[] parts = scoreLine.split("\\s+");
            int methodID = Integer.parseInt(parts[0].trim());
            double score = Double.parseDouble(parts[1].trim());
            tempMap.put(methodID, score);
        }
        return tempMap;
    }

    public ArrayList<String> performVSMPRSearchList(boolean all, boolean addPR) {
        try {
            if (reader == null)
                reader = DirectoryReader.open(FSDirectory.open(new File(indexFolder).toPath()));
            if (searcher == null)
                searcher = new IndexSearcher(reader);
            if (analyzer == null)
                analyzer = new StandardAnalyzer();
            QueryParser parser = new QueryParser(field, analyzer);

            if (!searchQuery.isEmpty()) {
                Query myquery = parser.parse(searchQuery);

                TopDocs results = searcher.search(myquery, ALL_RESULTS);
                ScoreDoc[] hits = results.scoreDocs;
                HashMap<String, Double> vsmScoreMap = new HashMap<String, Double>();

                for (int i = 0; i < hits.length; i++) {
                    ScoreDoc item = hits[i];
                    Document doc = searcher.doc(item.doc);
                    String fileURL = doc.get("path");
                    fileURL = fileURL.replace('\\', '/');
                    double vsmScore = item.score;
                    vsmScoreMap.put(fileURL, vsmScore);
                }

                int len = hits.length;
                if (!all) {
                    len = hits.length < TOPK_RESULTS ? hits.length : TOPK_RESULTS;
                }

                // normalize the VSM scores
                vsmScoreMap = normalizeScores(vsmScoreMap);

                ArrayList<String> rankedFiles = rankByPageRank(vsmScoreMap);
                for (int i = 0; i < len; i++) {
                    this.results.add(rankedFiles.get(i));
                }
            }
        } catch (Exception exc) {
            exc.printStackTrace();
        }
        return this.results;
    }

    protected HashMap<String, Double> normalizeScores(HashMap<String, Double> scoreMap) {
        double maxScore = 0;
        for (String key : scoreMap.keySet()) {
            double score = scoreMap.get(key);
            if (score > maxScore) {
                maxScore = score;
            }
        }

        for (String key : scoreMap.keySet()) {
            double score = scoreMap.get(key);
            scoreMap.put(key, score / maxScore);
        }
        return scoreMap;
    }

    protected ArrayList<String> rankByPageRank(HashMap<String, Double> vsmScoreMap) {
        HashMap<String, Double> combinedScoreMap = new HashMap<String, Double>();
        for (String fileURL : vsmScoreMap.keySet()) {
            double vsmScore = vsmScoreMap.get(fileURL);
            String fileName = new File(fileURL).getName();
            int fileID = Integer.parseInt(fileName.split("\\.")[0].trim());
            double prScore = StaticData.INITIAL_TERM_WEIGHT;
            if (prScoreMap.containsKey(fileID)) {
                prScore = prScoreMap.get(fileID);
            }
            // combined the score
            double combined = vsmScore * prScore;
            combinedScoreMap.put(fileURL, combined);
        }
        List<Map.Entry<String, Double>> sorted = ItemSorter.sortHashMapDouble(combinedScoreMap);
        ArrayList<String> sortedFiles = new ArrayList<String>();
        for (Map.Entry<String, Double> entry : sorted) {
            sortedFiles.add(entry.getKey());
        }
        return sortedFiles;
    }

    public ArrayList<Integer> getGoldFileIndices() {
        ArrayList<Integer> foundIndices = new ArrayList<>();
        this.results = performVSMPRSearchList(false, true);
        // System.out.println("TopK:" + this.results.size());
        try {
            MethodResultRankMgr rankFinder = new MethodResultRankMgr(repository, bugID, results);
            ArrayList<Integer> indices = rankFinder.getCorrectRanks();
            if (!indices.isEmpty()) {
                foundIndices.addAll(indices);
            }
        } catch (Exception exc) {
            // handle the exception
            exc.printStackTrace();
            System.out.println("Problem:" + repository);
        }
        return foundIndices;
    }

    public int getFirstGoldRank() {
        this.results = performVSMPRSearchList(true, true);
        int foundIndex = -1;
        try {
            MethodResultRankMgr rankFinder = new MethodResultRankMgr(repository, bugID, results);
            foundIndex = rankFinder.getFirstGoldRank();
        } catch (Exception exc) {
            // handle the exception
        }
        return foundIndex;
    }
}
