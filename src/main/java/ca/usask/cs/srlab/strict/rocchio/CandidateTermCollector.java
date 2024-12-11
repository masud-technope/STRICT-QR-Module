package ca.usask.cs.srlab.strict.rocchio;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import ca.usask.cs.srlab.strict.config.StaticData;
import ca.usask.cs.srlab.strict.stopwords.StopWordManager;
import ca.usask.cs.srlab.strict.utility.ContentLoader;
import ca.usask.cs.srlab.strict.utility.ItemSorter;

public class CandidateTermCollector {

    String repoName;
    ArrayList<String> results;
    String repoFolder;
    HashMap<String, Integer> termMap;
    ArrayList<String> qterms;
    HashMap<String, HashMap<String, Integer>> fileTermMap;
    public HashSet<String> candidates;
    StopWordManager stopManager;
    HashMap<String, Double> dfRatios = null;
    HashMap<String, Double> idfMap;
    final double COVERAGE_THRESHOLD = 0.25;

    int MAXSRCTERM = 5;

    public CandidateTermCollector(String repoName, ArrayList<String> results) {
        this.repoName = repoName;
        this.repoFolder = getSourceFolder(this.repoName);
        this.results = results;
        this.termMap = new HashMap<>();
        this.fileTermMap = new HashMap<>();
        this.candidates = new HashSet<>();
        this.stopManager = new StopWordManager();
    }

    protected String getSourceFolder(String repoName) {
        return StaticData.HOME_DIR + "/Corpus/norm-method/" + repoName;
    }


    public CandidateTermCollector(String repoName, ArrayList<String> results, String query) {
        this.repoName = repoName;
        this.repoFolder = getSourceFolder(this.repoName);
        this.results = results;
        this.qterms = getQueryTerms(query);
        this.termMap = new HashMap<>();
        this.fileTermMap = new HashMap<>();
        this.candidates = new HashSet<>();
        this.stopManager = new StopWordManager();
    }

    public CandidateTermCollector(String repoName, ArrayList<String> results, String query,
                                  HashMap<String, Double> dfRatios, HashMap<String, Double> idfMap) {
        this.repoName = repoName;
        this.repoFolder = getSourceFolder(this.repoName);
        this.results = results;
        this.qterms = getQueryTerms(query);
        this.termMap = new HashMap<>();
        this.fileTermMap = new HashMap<>();
        this.candidates = new HashSet<>();
        this.stopManager = new StopWordManager();
        this.dfRatios = dfRatios;
        this.idfMap = idfMap;
    }

    public void collectSourceTermStats() {
        // collecting source terms
        candidates = new HashSet<>();
        for (String fileURL : this.results) {

            // avoid other files than Java
            // discarding this restriction
            // if (!fileURL.endsWith(".java"))
            // continue;

            String fileName = new File(fileURL).getName();
            String activeURL = this.repoFolder + "/" + fileName;

            ArrayList<String> lines = ContentLoader.getAllLinesList(activeURL);
            HashMap<String, Integer> temp = new HashMap<>();
            for (String line : lines) {
                String regex = "\\p{Punct}+|\\d+|\\s+";
                String[] words = line.trim().split(regex);

                // decomposing the camel case
                words = normalizeTokens(words);

                ArrayList<String> rwords = stopManager.getRefinedList(words);

                for (String word : rwords) {
                    if (qterms.contains(word) || qterms.contains(word.toLowerCase()))
                        continue; // avoid the query terms
                    if (!isCommon(word) && !isCommon(word.toLowerCase())) {
                        if (temp.containsKey(word)) {
                            int count = temp.get(word) + 1;
                            temp.put(word, count);
                        } else if (temp.containsKey(word.toLowerCase())) {
                            int count = temp.get(word.toLowerCase()) + 1;
                            temp.put(word.toLowerCase(), count);
                        } else {
                            temp.put(word, 1);
                        }
                    }
                }
            }

            ArrayList<String> sortedTemp = new ArrayList<>();
            List<Map.Entry<String, Integer>> sorted = ItemSorter.sortHashMapInt(temp);
            int count = 0;
            for (Map.Entry<String, Integer> entry : sorted) {
                sortedTemp.add(entry.getKey());
                count++;
                if (count == MAXSRCTERM)
                    break;
            }

            candidates.addAll(sortedTemp);
            // add to file map
            this.fileTermMap.put(activeURL, temp);
        }
    }

    protected boolean isCommon(String candidateWord) {
        // check if the word is too common
        boolean common = false;
        if (this.dfRatios.containsKey(candidateWord)) {
            double ratio = this.dfRatios.get(candidateWord);
            if (ratio < COVERAGE_THRESHOLD) {
                common = false;
            } else
                common = true;
        } else
            common = true;
        return common;
    }

    protected ArrayList<String> getQueryTerms(String query) {
        String[] tokens = query.split("\\s+");
        return new ArrayList<>(Arrays.asList(tokens));
    }

    protected String[] normalizeTokens(String tokens[]) {
        ArrayList<String> temp = new ArrayList<>();
        for (String token : tokens) {
            temp.addAll(decomposeCamelCase(token));
        }
        return temp.toArray(new String[temp.size()]);
    }

    protected ArrayList<String> decomposeCamelCase(String token) {
        // decomposing camel case tokens using regex
        ArrayList<String> refined = new ArrayList<>();
        String camRegex = "([a-z])([A-Z]+)";
        String replacement = "$1\t$2";
        String filtered = token.replaceAll(camRegex, replacement);
        String[] ftokens = filtered.split("\\s+");
        refined.addAll(Arrays.asList(ftokens));
        return refined;
    }

    public HashSet<String> getDistinctTerms() {
        return this.candidates;
    }

    protected HashMap<String, HashMap<String, Integer>> getFileTermMap() {
        return this.fileTermMap;
    }

}
