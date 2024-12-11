package ca.usask.cs.srlab.strict.weka.model;

import java.util.ArrayList;
import java.util.HashMap;

import qd.core.EntropyCalc;
import qd.core.QDMetricsCollector;
import ca.usask.cs.srlab.strict.config.StaticData;
import ca.usask.cs.srlab.strict.utility.ContentLoader;
import ca.usask.cs.srlab.strict.utility.ContentWriter;
import ca.usask.cs.srlab.strict.utility.SelectedBugs;

public class MyQDiffCollector2020 {

    String repoName;
    String indexFolder;
    String corpusFolder;
    ArrayList<Integer> selectedBugs;
    EntropyCalc entCalc;
    static ArrayList<String> masterModelLines = new ArrayList<>();
    ArrayList<String> modelLines = new ArrayList<>();
    String queryDiffFolder;
    String modelFolder;

    public MyQDiffCollector2020(String repoName, String indexFolder, String corpusFolder) {
        this.repoName = repoName;
        this.indexFolder = indexFolder;
        this.corpusFolder = corpusFolder;
        // load selected bugs
        this.selectedBugs = SelectedBugs.loadSelectedBugs(repoName);
        this.entCalc = new EntropyCalc(repoName, this.indexFolder, this.corpusFolder);
    }

    protected String getRowKey(int bugID, String qKey) {
        return repoName + "-" + bugID + "-" + qKey;
    }

    public void setQueryDiffFolder(String folderName) {
        this.queryDiffFolder = folderName;
    }

    public void setModelFolder(String folderName) {
        this.modelFolder = folderName;
    }

    public void calculateQueryDifficulties() {
        String[] queryKeys = {"bestq", "worstq"};
        for (String key : queryKeys) {
            String queryFilePath = this.queryDiffFolder + "/" + key + "/" + repoName + ".txt";
            /** post retrieval did not increase performance, so turned off! */
            QDMetricsCollector qdcoll = new QDMetricsCollector(repoName, queryFilePath, false, this.indexFolder,
                    this.corpusFolder, this.entCalc);
            HashMap<Integer, String> tempQDMap = qdcoll.collectQDMetrics();
            for (int bugID : tempQDMap.keySet()) {
                String modelLine = getRowKey(bugID, key) + "\t" + tempQDMap.get(bugID) + "\t" + key;
                modelLines.add(modelLine);
            }
        }
        this.addDifficultyModel(modelLines);
    }

    protected void addDifficultyModel(ArrayList<String> modelLines) {
        String queryFilePath = this.modelFolder + "/" + repoName + ".txt";
        ContentWriter.writeContent(queryFilePath, modelLines);
        // adding the model header
        ArrayList<String> headerLines = ContentLoader.getAllLinesOptList(StaticData.ARFF_HEADER_FILE);
        ArrayList<String> arffLines = new ArrayList<>();
        arffLines.addAll(headerLines);
        arffLines.add("\n");
        arffLines.addAll(modelLines);
        String arffFilePath = this.modelFolder + "/" + repoName + ".arff";
        ContentWriter.writeContent(arffFilePath, arffLines);
    }

}
