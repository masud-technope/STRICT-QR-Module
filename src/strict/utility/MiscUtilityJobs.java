package strict.utility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import strict.ca.usask.cs.srlab.strict.config.StaticData;
import strict.query.QueryToken;

public class MiscUtilityJobs {

	public static String list2Str(ArrayList<String> list) {
		String temp = new String();
		for (String item : list) {
			temp += item + " ";
		}
		return temp.trim();
	}

	public static String list2StrVertical(ArrayList<String> list) {
		String temp = new String();
		for (String item : list) {
			temp += item + " ";
		}
		return temp.trim();
	}

	public static ArrayList<String> str2List(String str) {
		return new ArrayList<>(Arrays.asList(str.split("\\s+")));
	}

	public static double[] list2Array(ArrayList<Integer> list) {
		double[] array = new double[list.size()];
		for (int index = 0; index < list.size(); index++) {
			array[index] = list.get(index);
		}
		return array;
	}

	public static HashMap<String, Integer> wordcount(String content) {
		// performing simple word count
		String[] words = content.split("\\s+");
		HashMap<String, Integer> countmap = new HashMap<>();
		for (String word : words) {
			if (countmap.containsKey(word)) {
				int count = countmap.get(word) + 1;
				countmap.put(word, count);
			} else {
				countmap.put(word, 1);
			}
		}
		return countmap;
	}

	public static HashMap<String, QueryToken> normalizeScore(
			HashMap<String, QueryToken> tokenScoreMap, String type) {
		double maxScore = 0;
		for (String key : tokenScoreMap.keySet()) {
			QueryToken qtoken = tokenScoreMap.get(key);
			if (type.equals("TR")) {
				if (qtoken.tokenRankScore > maxScore)
					maxScore = qtoken.tokenRankScore;
			} else if (type.equals("PR")) {
				if (qtoken.posRankScore > maxScore)
					maxScore = qtoken.posRankScore;
			} else if (type.equals("TS")) {
				if (qtoken.totalScore > maxScore)
					maxScore = qtoken.totalScore;
			}
		}
		// now do the normalization
		for (String key : tokenScoreMap.keySet()) {
			QueryToken qtoken = tokenScoreMap.get(key);
			if (type.equals("TR")) {
				qtoken.tokenRankScore = qtoken.tokenRankScore / maxScore;
			} else if (type.equals("PR")) {
				qtoken.posRankScore = qtoken.posRankScore / maxScore;
			} else if (type.equals("TS")) {
				qtoken.totalScore = qtoken.totalScore / maxScore;
			}
			tokenScoreMap.put(key, qtoken);
		}
		return tokenScoreMap;
	}

	public static String expandCCWords(String sentence) {
		// expanding the CC words
		String expanded = new String();
		String[] tokens = sentence.split("\\s+");
		for (String token : tokens) {
			ArrayList<String> decomposed = decomposeCamelCase(token);
			if (decomposed.size() > 1) {
				expanded += token + "\t" + MiscUtilityJobs.list2Str(decomposed)
						+ "\t";
			} else {
				expanded += token + "\t";
			}
		}
		return expanded.trim();
	}

	protected static ArrayList<String> decomposeCamelCase(String token) {
		// decomposing camel case tokens using regex
		ArrayList<String> refined = new ArrayList<>();
		String camRegex = "([a-z])([A-Z]+)";
		String replacement = "$1\t$2";
		String filtered = token.replaceAll(camRegex, replacement);
		String[] ftokens = filtered.split("\\s+");
		refined.addAll(Arrays.asList(ftokens));
		return refined;
	}

	public static double getDOI(int index, int N) {
		return (1 - (double) index / N);
	}

	public static String loadBug(String repoName, int bugID) {
		String reqFile = StaticData.HOME_DIR + "/changereqs/" + repoName
				+ "/reqs/" + bugID + ".txt";
		return ContentLoader.loadFileContent(reqFile);
	}
}
