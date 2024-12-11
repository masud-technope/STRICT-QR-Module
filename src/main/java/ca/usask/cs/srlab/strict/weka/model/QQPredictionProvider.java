package ca.usask.cs.srlab.strict.weka.model;

import java.util.ArrayList;
import java.util.HashMap;

import ca.usask.cs.srlab.strict.config.StaticData;
import ca.usask.cs.srlab.strict.utility.ContentLoader;

public class QQPredictionProvider {

    HashMap<String, Double> probabilityMap;
    String masterDSFile;
    String resamplingFolder;
    boolean nosampling = false;

    public QQPredictionProvider() {
        this.probabilityMap = new HashMap<>();
        this.masterDSFile = StaticData.HOME_DIR
                + "/Proposed/qdiff-model/master-dataset.txt";
        this.resamplingFolder = StaticData.HOME_DIR
                + "/Proposed/resampling";
    }

    public QQPredictionProvider(boolean noSampling) {
        this.probabilityMap = new HashMap<>();
        this.nosampling = noSampling;
        this.masterDSFile = StaticData.HOME_DIR
                + "/Proposed/qdiff-model/master-dataset.txt";
        this.resamplingFolder = StaticData.HOME_DIR
                + "/Proposed/nosampling";
    }

    protected HashMap<Integer, String> loadInstanceIDMap() {
        ArrayList<String> lines = ContentLoader
                .getAllLinesOptList(this.masterDSFile);
        HashMap<Integer, String> instanceMap = new HashMap<>();
        int index = 0;
        for (String line : lines) {
            index++;
            String key = line.split("\\s+")[0].trim();
            instanceMap.put(index, key);
        }
        return instanceMap;
    }

    protected HashMap<String, PredictionEntry> provideProbabilityEntryNS() {
        HashMap<Integer, String> instanceMap = loadInstanceIDMap();
        HashMap<String, PredictionEntry> pentryMap = new HashMap<>();
        String sampleFile = StaticData.HOME_DIR
                + "/Proposed/nosampling/101.txt";
        PredictionCleaner predcleaner = new PredictionCleaner(sampleFile);
        // System.out.println(iter+"-"+sample);
        ArrayList<String> clines = predcleaner.getCleanedOutputLines();
        for (String cline : clines) {
            String[] parts = cline.split("\\s+");
            if (parts.length == 7) {
                int instanceID = Integer.parseInt(parts[6].trim());
                double highProb = Double.parseDouble(parts[3].trim());
                double medProb = Double.parseDouble(parts[4].trim());
                double lowProb = Double.parseDouble(parts[5].trim());

                PredictionEntry pe = new PredictionEntry();
                if (instanceMap.containsKey(instanceID)) {
                    pe.key = instanceMap.get(instanceID);
                }

                pe.highProbability = highProb;
                pe.mediumProbability = medProb;
                pe.lowProbability = lowProb;
                pe.occurrenceCount++;

                // storing prediction
                pentryMap.put(pe.key, pe);
            }
        }

        // now analyze the predictions
        for (String key : pentryMap.keySet()) {
            PredictionEntry pentry = pentryMap.get(key);
            pentry.highProbability = pentry.highProbability
                    / pentry.occurrenceCount;
            pentry.mediumProbability = pentry.mediumProbability
                    / pentry.occurrenceCount;
            pentry.lowProbability = pentry.lowProbability
                    / pentry.occurrenceCount;
            pentryMap.put(key, pentry);
        }
        return pentryMap;
    }

    public HashMap<String, PredictionEntry> provideProbabilityEntry() {

        HashMap<Integer, String> instanceMap = loadInstanceIDMap();
        HashMap<String, PredictionEntry> pentryMap = new HashMap<>();
        for (int iter = 1; iter <= 10; iter++) {
            for (int sample = 1; sample <= 10; sample++) {
                String sampleFile = StaticData.HOME_DIR
                        + "/Proposed/resampling/iter-" + iter + "-sample-"
                        + sample + ".txt";
                PredictionCleaner predcleaner = new PredictionCleaner(
                        sampleFile);
                // System.out.println(iter+"-"+sample);
                ArrayList<String> clines = predcleaner.getCleanedOutputLines();
                for (String cline : clines) {
                    String[] parts = cline.split("\\s+");
                    if (parts.length == 7) {
                        int instanceID = Integer.parseInt(parts[6].trim());
                        double highProb = Double.parseDouble(parts[3].trim());
                        double medProb = Double.parseDouble(parts[4].trim());
                        double lowProb = Double.parseDouble(parts[5].trim());

                        PredictionEntry pe = new PredictionEntry();
                        if (instanceMap.containsKey(instanceID)) {
                            pe.key = instanceMap.get(instanceID);
                        }

                        pe.highProbability = highProb;
                        pe.mediumProbability = medProb;
                        pe.lowProbability = lowProb;
                        pe.occurrenceCount++;

                        // storing prediction
                        pentryMap.put(pe.key, pe);
                    }
                }
            }
        }

        // now analyze the predictions
        for (String key : pentryMap.keySet()) {
            PredictionEntry pentry = pentryMap.get(key);
            pentry.highProbability = pentry.highProbability
                    / pentry.occurrenceCount;
            pentry.mediumProbability = pentry.mediumProbability
                    / pentry.occurrenceCount;
            pentry.lowProbability = pentry.lowProbability
                    / pentry.occurrenceCount;
            pentryMap.put(key, pentry);
        }
        return pentryMap;
    }

    protected String getBestQuery(ArrayList<PredictionEntry> entries) {
        PredictionEntry maxEntry = null;
        double maxHighProb = 0;
        for (PredictionEntry entry : entries) {
            if (entry.highProbability >= maxHighProb) {
                maxHighProb = entry.highProbability;
                maxEntry = entry;
            }
        }
        return maxEntry.key;
    }

    public HashMap<String, String> provideBestQuery() {
        HashMap<String, PredictionEntry> pentryMap = new HashMap();
        if (nosampling) {
            pentryMap = provideProbabilityEntryNS();
        } else {
            pentryMap = provideProbabilityEntry();
        }
        HashMap<String, String> bestQueryMap = new HashMap<>();
        HashMap<String, ArrayList<PredictionEntry>> tempMap = new HashMap<>();
        for (String key : pentryMap.keySet()) {
            String[] keyparts = key.split("-");
            String newKey = keyparts[0] + "-" + keyparts[1];

            // only considering the TR and PR
            // String scoreKey = keyparts[2].trim();
            // if (scoreKey.equals("TRC") || scoreKey.equals("PRC"))
            // continue;

            if (tempMap.containsKey(newKey)) {
                ArrayList<PredictionEntry> temp2 = tempMap.get(newKey);
                temp2.add(pentryMap.get(key));
                tempMap.put(newKey, temp2);
            } else {
                ArrayList<PredictionEntry> temp2 = new ArrayList<>();
                temp2.add(pentryMap.get(key));
                tempMap.put(newKey, temp2);
            }
        }
        System.out.println("Predicted:" + tempMap.size());
        // now determine the best query
        String exampleKey = "eclipse.jdt.ui-303705";
        for (String newKey : tempMap.keySet()) {
            ArrayList<PredictionEntry> pentries = tempMap.get(newKey);
            String bestKey = getBestQuery(pentries);
            String bestScoreKey = bestKey.split("-")[2];

            if (newKey.equals(exampleKey)) {
                System.out.println(newKey + "\t" + bestScoreKey);
            }

            bestQueryMap.put(newKey, bestScoreKey);

        }
        return bestQueryMap;
    }

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        System.out.println(new QQPredictionProvider().provideBestQuery());
    }
}
