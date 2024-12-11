package ca.usask.cs.srlab.strict.lucenecheck;

import java.io.File;
import java.util.ArrayList;

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

public class LuceneSearcher {

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
    int ALL_RESULTS = 10000;
    ArrayList<String> results;
    public ArrayList<String> goldset;
    IndexReader reader = null;
    IndexSearcher searcher = null;
    Analyzer analyzer = null;

    public double precision = 0;
    public double recall = 0;
    public double precatk = 0;
    public double recrank = 0;

    public LuceneSearcher(int bugID, String repository, String searchQuery) {
        // initialization
        this.bugID = bugID;
        this.repository = repository;
        this.indexFolder = StaticData.HOME_DIR + "/Lucene/index-method/" + repository;
        this.searchQuery = searchQuery;
        this.results = new ArrayList<>();
        this.goldset = new ArrayList<>();
    }

    public ArrayList<String> performVSMSearchList(boolean all) {
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
                if (!all) {
                    int len = hits.length < TOPK_RESULTS ? hits.length : TOPK_RESULTS;
                    for (int i = 0; i < len; i++) {
                        ScoreDoc item = hits[i];
                        Document doc = searcher.doc(item.doc);
                        String fileURL = doc.get("path");
                        fileURL = fileURL.replace('\\', '/');
                        this.results.add(fileURL);
                    }
                } else {
                    for (int i = 0; i < hits.length; i++) {
                        ScoreDoc item = hits[i];
                        Document doc = searcher.doc(item.doc);
                        String fileURL = doc.get("path");
                        fileURL = fileURL.replace('\\', '/');
                        this.results.add(fileURL);
                    }
                }
            }
        } catch (Exception exc) {
            exc.printStackTrace();
        }
        return this.results;
    }

    public ArrayList<Integer> getGoldFileIndices() {
        ArrayList<Integer> foundIndices = new ArrayList<>();
        this.results = performVSMSearchList(false);
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
        this.results = performVSMSearchList(true);
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
