package ca.usask.cs.srlab.strict.weka.model;

import java.util.ArrayList;

import ca.usask.cs.srlab.strict.utility.ContentLoader;
import ca.usask.cs.srlab.strict.utility.ContentWriter;
import ca.usask.cs.srlab.strict.utility.MiscUtility;

public class PredictionCleaner {

    String predictionFile;
    String outputFile;

    public PredictionCleaner(String predictionFile) {
        this.predictionFile = predictionFile;
        this.outputFile = predictionFile;
    }

    public PredictionCleaner() {
        // default constructor
    }

    protected void cleanPredictionOutput() {
        ArrayList<String> lines = ContentLoader.getAllLinesOptList(this.predictionFile);
        // discard the first line
        lines.remove(0);
        ArrayList<String> cleanedLines = new ArrayList<>();
        for (String line : lines) {
            String cleaned = line.replaceAll("\\(", " ");
            cleaned = cleaned.replaceAll("\\)", " ");
            cleaned = cleaned.replaceAll("\\*", " ");
            cleaned = cleaned.replaceAll("\\+", " ");
            cleaned = cleaned.replaceAll(",", " ");
            cleanedLines.add(cleaned);
        }
        ContentWriter.writeContent(this.outputFile, cleanedLines);
        System.out.println("Done!");
    }

    public String cleanPredictions(String predictions) {
        ArrayList<String> lines = MiscUtility.str2ListForNewLine(predictions);
        lines.remove(0);
        ArrayList<String> cleanedLines = new ArrayList<>();
        for (String line : lines) {
            String cleaned = line.replaceAll("\\(", " ");
            cleaned = cleaned.replaceAll("\\)", " ");
            cleaned = cleaned.replaceAll("\\*", " ");
            cleaned = cleaned.replaceAll("\\+", " ");
            cleaned = cleaned.replaceAll(",", " ");
            cleanedLines.add(cleaned);
        }
        return MiscUtility.list2StrWithNewLine(cleanedLines);
    }

    protected ArrayList<String> getCleanedOutputLines() {
        ArrayList<String> lines = ContentLoader.getAllLinesOptList(this.predictionFile);
        // discard the first line
        lines.remove(0);
        ArrayList<String> cleanedLines = new ArrayList<>();
        for (String line : lines) {
            String cleaned = line.replaceAll("\\(", " ");
            cleaned = cleaned.replaceAll("\\)", " ");
            cleaned = cleaned.replaceAll("\\*", " ");
            cleaned = cleaned.replaceAll("\\+", " ");
            cleaned = cleaned.replaceAll(",", " ");
            cleanedLines.add(cleaned);
        }
        return cleanedLines;
    }
}
