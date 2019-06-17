package strict.utility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import strict.query.QueryToken;

public class MiscUtility {

	public static String list2Str(ArrayList<String> list) {
		String temp = new String();
		for (String item : list) {
			temp += item + " ";
		}
		return temp.trim();
	}
	
	public static String list2Str(HashSet<String> list) {
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
				if (qtoken.textRankScore > maxScore)
					maxScore = qtoken.textRankScore;
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
				qtoken.textRankScore = qtoken.textRankScore / maxScore;
			} else if (type.equals("PR")) {
				qtoken.posRankScore = qtoken.posRankScore / maxScore;
			} else if (type.equals("TS")) {
				qtoken.totalScore = qtoken.totalScore / maxScore;
			}
			tokenScoreMap.put(key, qtoken);
		}
		return tokenScoreMap;
	}

	public static ArrayList<String> filterSmallTokens(ArrayList<String> tokens) {
		ArrayList<String> temp = new ArrayList<>();
		for (String token : tokens) {
			if (token.length() > 2) { // token length threshold
				temp.add(token);
			}
		}
		return temp;
	}

	public static void showItems(ArrayList<String> items) {
		for (String item : items) {
			System.out.println(item);
		}
	}

	public static void showItems(HashSet<String> items) {
		for (String item : items) {
			System.out.println(item);
		}
	}

	public static void showItems(String[] items) {
		for (String item : items) {
			System.out.println(item);
		}
	}

}
