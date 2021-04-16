package strict.query;

import java.util.ArrayList;
import strict.text.normalizer.TextNormalizer;

public class QTextCollector {

	public String bugtitle;
	String reportContent;

	public QTextCollector(String bugtitle, String reportContent) {
		this.reportContent = reportContent;
		this.bugtitle = bugtitle;
	}
	
	public ArrayList<String> getSentenceSet(String content) {
		ArrayList<String> sentlist = new ArrayList<>();
		content = content.replace("\n", ". ");
		String separator = "(?<=[.?!:;])\\s+(?=[a-zA-Z0-9])";
		String[] sentences = content.split(separator);
		for (String sentence : sentences) {
			sentlist.add(sentence);
		}
		return sentlist;
	}

	public ArrayList<String> collectQuerySentences() {
		ArrayList<String> sentenceList = getSentenceSet(this.reportContent);
		ArrayList<String> normalizedSentences = new ArrayList<>();
		for (String sentence : sentenceList) {
			String normalized = new TextNormalizer().normalizeSimpleCodeDiscardSmall(sentence);
			normalizedSentences.add(normalized);
		}
		return normalizedSentences;
	}
}
