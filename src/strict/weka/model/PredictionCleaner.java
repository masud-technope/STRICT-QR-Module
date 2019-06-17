package strict.weka.model;

import java.util.ArrayList;
import strict.utility.ContentLoader;
import strict.utility.ContentWriter;

public class PredictionCleaner {

	String predictionFile;
	String outputFile;

	public PredictionCleaner(String predictionFile) {
		this.predictionFile = predictionFile;
		this.outputFile = predictionFile;
	}

	protected void cleanPredictionOutput() {
		ArrayList<String> lines = ContentLoader
				.getAllLinesOptList(this.predictionFile);
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

	protected ArrayList<String> getCleanedOutputLines() {
		ArrayList<String> lines = ContentLoader
				.getAllLinesOptList(this.predictionFile);
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

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		/*for (int iter = 100; iter <= 1000; iter += 100) {
			for (int sample = 1; sample <= 10; sample++) {
				String predictionFile = StaticData.SOTraceQData2
						+ "/proposed/sampling/iter-" + (iter / 100)
						+ "-sample-" + sample + ".txt";
				new PredictionCleaner(predictionFile).cleanPredictionOutput();
			}
		}*/
		/*String predictionFile = StaticData.SOTraceQData2
				+ "/proposed/nosampling/101.txt";
		new PredictionCleaner(predictionFile).cleanPredictionOutput();*/
	}
}
