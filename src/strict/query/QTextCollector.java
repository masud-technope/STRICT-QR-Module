package strict.query;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import strict.ca.usask.cs.srlab.strict.config.StaticData;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.process.DocumentPreprocessor;
import strict.stemmer.WordNormalizer;
import strict.stopwords.StopWordManager;
import strict.utility.MiscUtilityJobs;

public class QTextCollector {

	int bugID;
	HashMap<Integer, Integer> qamap;
	String questitle;
	public String bugtitle;
	String repoName;
	String reportContent;

	@Deprecated
	public QTextCollector(int bugID, String repoName) {
		// initialization
		this.bugID = bugID;
		this.qamap = new HashMap<>();
		// this.loadQAPairs();
		questitle = new String();
		bugtitle = new String();
		this.repoName = repoName;
	}

	public QTextCollector(String bugtitle, String reportContent) {
		this.reportContent = reportContent;
		this.bugtitle = bugtitle;
	}

	protected void loadQAPairs() {
		// loading QA pair
		String pairFile = StaticData.HOME_DIR + "/posts/pointer.txt";
		try {
			Scanner scanner = new Scanner(new File(pairFile));
			while (scanner.hasNext()) {
				String line = scanner.nextLine();
				String[] parts = line.split("\\s+");
				int answerID = Integer.parseInt(parts[0].trim());
				int questionID = Integer.parseInt(parts[1].trim());
				this.qamap.put(answerID, questionID);
			}
			scanner.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Deprecated
	protected String getQuestion(int questionID) {
		// collecting question
		String content = new String();
		// String questionFile = StaticData.SOTraceQData + "/posts/questions/"+
		// questionID + ".txt";
		String questionFile = StaticData.HOME_DIR + "/bugs/unresolved/"
				+ questionID + ".txt";
		try {
			Scanner scanner = new Scanner(new File(questionFile));
			questitle = scanner.nextLine();
			scanner.nextLine();
			scanner.nextLine();
			String desc = new String();
			while (scanner.hasNext()) {
				desc += scanner.nextLine() + "\n";
			}
			scanner.close();
			Document doc = Jsoup.parse(desc);
			content = doc.text();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return content;
	}

	@Deprecated
	protected String getAnswer(int answerID) {
		String content = new String();
		String answerFile = StaticData.HOME_DIR + "/posts/answers/"
				+ answerID + ".txt";
		try {
			Scanner scanner = new Scanner(new File(answerFile));
			scanner.nextLine();
			String desc = new String();
			while (scanner.hasNext()) {
				desc += scanner.nextLine() + "\n";
			}
			scanner.close();
			Document doc = Jsoup.parse(desc);
			content = doc.text();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return content;
	}

	@Deprecated
	protected String getBugDesc(int bugID) {
		// collect bug description
		String content = new String();
		String bugFile = StaticData.HOME_DIR + "/bugs";
		if (repoName.startsWith("eclipse")) {
			bugFile += ".debug";
		}
		bugFile += "/resolved/" + bugID + ".txt";
		try {
			Scanner scanner = new Scanner(new File(bugFile));
			bugtitle = scanner.nextLine();
			// discard next four lines
			scanner.nextLine();
			scanner.nextLine();
			scanner.nextLine();
			// now collect from next lines
			String desc = new String();
			while (scanner.hasNext()) {
				String line = scanner.nextLine();
				desc += line + "\n";
			}
			scanner.close();
			content = desc;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return content;
	}

	public String getBugDescNew(int bugID) {
		String content = new String();
		String bugFile = StaticData.HOME_DIR + "/changereqs/"
				+ this.repoName + "/reqs/" + bugID + ".txt";
		try {
			Scanner scanner = new Scanner(new File(bugFile));
			bugtitle = scanner.nextLine();
			// now collect from next lines
			String desc = new String();
			while (scanner.hasNext()) {
				String line = scanner.nextLine();
				// skip the empty lines
				if (line.trim().isEmpty())
					continue;
				desc += line + "\n";
			}
			scanner.close();
			content = desc;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return content;
	}

	public ArrayList<String> getSentenceSet(String content) {
		// collecting individual sentences from a list
		ArrayList<String> sentlist = new ArrayList<>();
		content = content.replace('\n', ' ');
		String separator = "(?<=[.?!:;])\\s+(?=[a-zA-Z0-9])";
		// String separator="\\. |\\? |! ";
		String[] sentences = content.split(separator);
		for (String sentence : sentences) {
			// System.out.println(sentence);
			sentlist.add(sentence);
		}
		return sentlist;
	}
	

	protected String getButTitle() {
		// returning the task title
		return this.bugtitle;
	}

	protected ArrayList<String> collectQuerySentences() {
		// collecting query sentences

		String bugdesc = getBugDescNew(this.bugID); // new bug description
		bugdesc = bugtitle + ". " + bugdesc;

		ArrayList<String> sentenceList = getSentenceSet(bugdesc);
		ArrayList<String> normalizedSentences = new ArrayList<>();
		StopWordManager manager = new StopWordManager();

		for (String sentence : sentenceList) {
			String refined = manager.getRefinedSentence(sentence);
			WordNormalizer normalizer = new WordNormalizer(refined);
			// String normalized = normalizer.normalizeSentence();
			// System.out.println(normalized);
			String normalized = normalizer.normalizeSentence();
			// String normalized = normalizer.normalizeSentenceWithCCD();
			// System.out.println(normalized);
			normalizedSentences.add(normalized);
		}
		// returning the normalized sentences
		return normalizedSentences;
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

	protected ArrayList<String> collectQuerySentencesV2() {
		String bugdesc = getBugDescNew(this.bugID); // new bug description
		bugdesc = bugtitle + ". " + bugdesc;

		ArrayList<String> sentenceList = getSentenceSet(bugdesc);
		ArrayList<String> normalizedSentences = new ArrayList<>();
		StopWordManager manager = new StopWordManager();

		for (String sentence : sentenceList) {
			String refined = manager.getRefinedSentence(sentence);
			String expanded = expandCCWords(refined).trim();
			expanded = manager.getRefinedSentence(expanded);
			normalizedSentences.add(expanded.trim()); // make it lower case
		}
		// returning the normalized sentences
		return normalizedSentences;
	}

	public ArrayList<String> collectQuerySentencesV3() {
		ArrayList<String> sentenceList = getSentenceSet(this.reportContent);
		ArrayList<String> normalizedSentences = new ArrayList<>();
		StopWordManager manager = new StopWordManager();

		for (String sentence : sentenceList) {
			String refined = manager.getRefinedSentence(sentence);
			String expanded = expandCCWords(refined).trim();
			expanded = manager.getRefinedSentence(expanded);
			normalizedSentences.add(expanded.trim()); // make it lower case
		}
		// returning the normalized sentences
		return normalizedSentences;
	}

	protected String expandCCWords(String sentence) {
		// expanding the CC words
		String expanded = new String();
		String[] tokens = sentence.split("\\s+");
		for (String token : tokens) {
			ArrayList<String> decomposed = decomposeCamelCase(token);
			if (decomposed.size() > 1) {
				expanded += token + " " + MiscUtilityJobs.list2Str(decomposed)
						+ " ";
			} else {
				expanded += token + "\t";
			}
		}
		return expanded.trim();
	}

	public static void main(String[] args) {
		int bugID = 183492;
		String repoName = "ecf";
		QTextCollector collector = new QTextCollector(bugID, repoName);
		collector.collectQuerySentences();
	}
}
