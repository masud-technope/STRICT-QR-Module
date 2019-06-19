package strict.stopwords;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.StringTokenizer;

import strict.ca.usask.cs.srlab.strict.config.StaticData;
import strict.utility.ContentLoader;

public class StopWordManager {

	public ArrayList<String> stopList;
	String stopDir = StaticData.STOPWORD_DIR + "/stop-words-english-total.txt";
	String javaKeywordFile = StaticData.STOPWORD_DIR + "/java-keywords.txt";
	String CppKeywordFile = StaticData.STOPWORD_DIR + "/cpp-keywords.txt";

	public StopWordManager() {
		// initialize the Hash set
		this.stopList = new ArrayList<>();
		this.loadStopWords();
	}

	protected void loadStopWords() {
		// loading stop words
		try {
			Scanner scanner = new Scanner(new File(this.stopDir));
			while (scanner.hasNext()) {
				String word = scanner.nextLine().trim();
				this.stopList.add(word);
			}
			scanner.close();

			// now add the keywords
			// ArrayList<String> keywords =
			// ContentLoader.getAllLinesOptList(javaKeywordFile);
			// String[] keywords=ContentLoader.getAllLines(CppKeywordFile);
			// this.stopList.addAll(keywords);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected String removeSpecialChars(String sentence) {
		// removing special characters
		String regex = "\\p{Punct}+|\\d+|\\s+";
		String[] parts = sentence.split(regex);
		String refined = new String();
		for (String str : parts) {
			refined += str.trim() + " ";
		}
		// if(modifiedWord.isEmpty())modifiedWord=word;
		return refined;
	}

	public String getRefinedSentence(String sentence) {
		// get refined sentence
		String refined = new String();
		String temp = removeSpecialChars(sentence);
		StringTokenizer tokenizer = new StringTokenizer(temp);
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			if (!this.stopList.contains(token.toLowerCase())) {
				if (token.length() >= 2) {
					refined += token + " ";
				}
			}
		}
		return refined.trim();
	}

	public ArrayList<String> getRefinedList(String[] words) {
		ArrayList<String> refined = new ArrayList<>();
		for (String word : words) {
			if (!this.stopList.contains(word.toLowerCase())) {
				refined.add(word);
			}
		}
		return refined;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		StopWordManager manager = new StopWordManager();
		String str = "Java for Mac OS X is transitioning interface case to delivering the JDK as a bundle with the .jdk extension. The bundles cause the Contents/Home directory to be opaque and inaccessible to standard file chooser dialogs.";
		// String modified=manager.removeSpecialChars(sentence);
		System.out.println(manager.getRefinedSentence(str));

	}

}
