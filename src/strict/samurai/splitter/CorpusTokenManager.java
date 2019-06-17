package strict.samurai.splitter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import strict.ca.usask.cs.srlab.strict.config.StaticData;
import strict.utility.ContentLoader;
import strict.utility.ContentWriter;

public class CorpusTokenManager {

	String repoName;
	String repoFolder;
	HashMap<String, Integer> wordMap;

	public CorpusTokenManager(String repoName) {
		this.repoName = repoName;
		this.repoFolder = StaticData.HOME_DIR + "/changereqs/" + repoName
				+ "/reqs";
		this.wordMap = new HashMap<>();
	}

	public CorpusTokenManager() {
		// defaut constructor
	}

	protected String decomposeCamelCase(String ccToken) {
		String cam1Regex = "([a-z])([A-Z])+";
		String replacement1 = "$1\t$2";
		return ccToken.replaceAll(cam1Regex, replacement1);
	}

	protected void getWordCount(String fileURL) {
		// extracting the file content
		ArrayList<String> lines = ContentLoader.getAllLinesOptList(fileURL);
		for (String line : lines) {
			String[] words = line.split("\\p{Punct}+|\\d+|\\s+");
			for (String word : words) {
				String decomposed = decomposeCamelCase(word);
				String[] smallTokens = decomposed.split("\\s+");
				for (String smallToken : smallTokens) {
					if (wordMap.containsKey(smallToken)) {
						int count = wordMap.get(smallToken) + 1;
						wordMap.put(smallToken, count);

					} else {
						wordMap.put(smallToken, 1);
					}
				}
			}
		}

	}

	protected void getCorpusWordMap() {
		// collecting corpus word map
		File[] files = new File(this.repoFolder).listFiles();
		for (File f : files) {
			this.getWordCount(f.getAbsolutePath());
		}
		String outFile = StaticData.HOME_DIR + "/tokens-br/" + repoName
				+ ".txt";
		ArrayList<String> items = new ArrayList<>();
		for (String key : wordMap.keySet()) {
			items.add(key + ":" + wordMap.get(key));
		}
		ContentWriter.writeContent(outFile, items);
		System.out.println("DONE!" +repoName);
	}

	protected static void mergeTokens() {
		HashMap<String, Integer> tokenMap = new HashMap<>();
		String[] repos = {"ecf", "eclipse.jdt.debug", "eclipse.jdt.core",
				"eclipse.jdt.ui", "eclipse.pde.ui", "log4j", "sling",
				"tomcat70" };
		for (String repoName : repos) {
			String tokenFile = StaticData.HOME_DIR + "/tokens/" + repoName
					+ ".txt";
			String[] lines = ContentLoader.getAllLines(tokenFile);
			for (String line : lines) {
				String[] parts = line.trim().split(":");
				String token = parts[0].trim();
				int count = Integer.parseInt(parts[1].trim());
				if (tokenMap.containsKey(token)) {
					int newCount = tokenMap.get(token) + count;
					tokenMap.put(token, newCount);
				} else {
					tokenMap.put(token, count);
				}
			}
		}
		
		for (String repoName : repos) {
			String tokenFile = StaticData.HOME_DIR + "/tokens-br/" + repoName
					+ ".txt";
			String[] lines = ContentLoader.getAllLines(tokenFile);
			for (String line : lines) {
				String[] parts = line.trim().split(":");
				String token = parts[0].trim();
				int count = Integer.parseInt(parts[1].trim());
				if (tokenMap.containsKey(token)) {
					int newCount = tokenMap.get(token) + count;
					tokenMap.put(token, newCount);
				} else {
					tokenMap.put(token, count);
				}
			}
		}
		

		String allTokenFile = StaticData.HOME_DIR + "/tokens/tokendb.txt";
		ArrayList<String> items = new ArrayList<>();
		for (String key : tokenMap.keySet()) {
			items.add(key + ":" + tokenMap.get(key));
		}
		ContentWriter.writeContent(allTokenFile, items);
		System.out.println("Merged!");

	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		long start = System.currentTimeMillis();

		String[] repos = { "ecf", "eclipse.jdt.debug", "eclipse.jdt.core",
				"eclipse.jdt.ui", "eclipse.pde.ui", "log4j", "sling",
				"tomcat70" };
		
		/*for (String repoName : repos) {
			new CorpusTokenManager(repoName).getCorpusWordMap();
		}*/

		new CorpusTokenManager().mergeTokens();

		long end = System.currentTimeMillis();
		System.out.println("Time elapsed:" + (end - start) / 1000 + "s");
	}
}
