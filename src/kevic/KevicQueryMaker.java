package kevic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.lang.StringUtils;
import strict.ca.usask.cs.srlab.strict.config.StaticData;
import strict.text.normalizer.TextNormalizer;
import strict.utility.BugReportLoader;
import strict.utility.ContentWriter;
import strict.utility.ItemSorter;
import strict.utility.MiscUtility;
import strict.utility.SelectedBugs;

public class KevicQueryMaker {

	public HashMap<String, Double> idf;
	String repoName;
	String kevicQueryFile;
	public static boolean INCLUDE_TITLE = false;
	HashMap<String, Integer> keywordClassMap;
	HashMap<String, Double> selectionProbMap;

	public KevicQueryMaker(String repoName) {
		this.repoName = repoName;
		this.idf = new HashMap<>();
		this.idf = this.loadIDF(this.repoName);
	}

	public void setKevicQueryFile(String filePath) {
		this.kevicQueryFile = filePath;
	}

	public KevicQueryMaker(String repoName, HashMap<String, Integer> keywordClassMap,
			HashMap<String, Double> selectProbMap) {
		this.repoName = repoName;
		this.idf = new HashMap<>();
		this.idf = this.loadIDF(this.repoName);
		this.keywordClassMap = keywordClassMap;
		this.selectionProbMap = selectProbMap;
	}

	protected HashMap<String, Double> loadIDF(String repoName) {
		// loading the TFIDF scores
		if (!idf.isEmpty())
			return null;
		TFIDFManager tfidfProvider = new TFIDFManager(repoName);
		return tfidfProvider.calculateIDF();
	}

	protected HashMap<String, Double> calculateTFIDF(String content) {
		// calculating TFIDF
		HashMap<String, Integer> tf = MiscUtility.wordcount(content);
		HashMap<String, Double> tfidf = new HashMap<>();
		for (String term : tf.keySet()) {
			double tfidfScore = 0;
			int tfScore = tf.get(term);
			if (idf.containsKey(term)) {
				tfidfScore = tfScore * idf.get(term);
			}
			tfidf.put(term, tfidfScore);
		}
		// now normalize the TFIDF scores
		double maxTFIDF = 0;
		for (String key : tfidf.keySet()) {
			double weight = tfidf.get(key);
			if (weight > maxTFIDF) {
				maxTFIDF = weight;
			}
		}
		for (String key : tfidf.keySet()) {
			double weight = tfidf.get(key);
			double normalized = weight / maxTFIDF;
			tfidf.put(key, normalized);
		}
		// calculating TF-IDF
		return tfidf;
	}

	protected double getRelevance(double totalweigth) {
		return 1.0 / (1 + Math.exp(-1 * totalweigth));
	}

	protected ArrayList<String> getKevicQueryLine(int bugID) {
		ArrayList<String> titleTerms = MiscUtility.str2List(BugReportLoader.loadBugReportTitle(repoName, bugID));
		String title = MiscUtility.list2Str(titleTerms);
		ArrayList<String> descTerms = MiscUtility.str2List(BugReportLoader.loadBugReportDesc(repoName, bugID));
		String desc = MiscUtility.list2Str(descTerms);
		String reportContent = BugReportLoader.loadBugReport(repoName, bugID);
		String refinedTexts = new TextNormalizer(reportContent).normalizeSimpleCodeDiscardSmall(reportContent);
		String[] refinedTerms = refinedTexts.split("\\s+");
		HashMap<String, Double> tfidf = this.calculateTFIDF(refinedTexts);

		ArrayList<String> rows = new ArrayList<String>();

		for (String word : refinedTerms) {
			String keyID = repoName + "-" + bugID + "-" + word;
			String row = new String();
			row = keyID;
			// checking different dimensions
			try {
				if (titleTerms.contains(word) && descTerms.contains(word)) {
					row += "\t" + 1;
				} else {
					row += "\t" + 0;
				}
				if ((!title.startsWith(word) && !title.endsWith(word))
						|| (!desc.startsWith(word) && !desc.endsWith(word))) {
					row += "\t" + 1;
				} else {
					row += "\t" + 0;
				}
				String[] segments = StringUtils.splitByCharacterTypeCamelCase(word);
				if (segments.length > 1) {
					row += "\t" + 1;
				} else {
					row += "\t" + 0;
				}
				if (word.length() < 3)
					continue;
				if (tfidf.containsKey(word)) {
					row += "\t" + tfidf.get(word);
				} else if (tfidf.containsKey(word.toLowerCase())) {
					row += "\t" + tfidf.get(word.toLowerCase());
				} else {
					row += "\t" + 0;
				}

				if (this.keywordClassMap.containsKey(keyID)) {
					row += "\t" + keywordClassMap.get(keyID);
				} else {
					continue;
				}

			} catch (Exception ex) {
				// handle the exception
			}
			rows.add(row);
		}
		return rows;
	}

	protected String getKevicQuerySmart(int bugID) {
		String reportContent = BugReportLoader.loadBugReport(repoName, bugID);
		String refinedTexts = new TextNormalizer(reportContent).normalizeSimpleCodeDiscardSmall(reportContent);
		String[] refinedTerms = refinedTexts.split("\\s+");
		ArrayList<String> skeywords = new ArrayList<String>();
		HashMap<String, Double> scoreMap = new HashMap<String, Double>();
		for (String keyword : refinedTerms) {
			String keyID = repoName + "-" + bugID + "-" + keyword;
			if (this.keywordClassMap.containsKey(keyID)) {
				int kclass = keywordClassMap.get(keyID);
				if (kclass > 0) {
					if (selectionProbMap.containsKey(keyID)) {
						double selectProb = selectionProbMap.get(keyID);
						scoreMap.put(keyword, selectProb);
					}
				}
			}
		}
		List<Map.Entry<String, Double>> sorted = ItemSorter.sortHashMapDouble(scoreMap);
		for (Map.Entry<String, Double> entry : sorted) {
			skeywords.add(entry.getKey());
			if (skeywords.size() == StaticData.SUGGESTED_KEYWORD_COUNT) 
				break;
		}

		return MiscUtility.list2Str(skeywords);
	}

	protected String getKevicQuery(int bugID) {

		ArrayList<String> titleTerms = MiscUtility.str2List(BugReportLoader.loadBugReportTitle(repoName, bugID));
		String title = MiscUtility.list2Str(titleTerms);
		ArrayList<String> descTerms = MiscUtility.str2List(BugReportLoader.loadBugReportDesc(repoName, bugID));
		String desc = MiscUtility.list2Str(descTerms);
		String reportContent = BugReportLoader.loadBugReport(repoName, bugID);

		String refinedTexts = new TextNormalizer(reportContent).normalizeSimpleCodeDiscardSmall(reportContent);
		String[] refinedTerms = refinedTexts.split("\\s+");

		// now calculate the TFIDF
		HashMap<String, Double> tfidf = this.calculateTFIDF(refinedTexts);
		HashMap<String, Double> termWeight = new HashMap<>();

		for (String word : refinedTerms) {
			double termScore = -2.100;
			// checking different dimensions
			try {
				if (titleTerms.contains(word) && descTerms.contains(word)) {
					termScore += 1.217;
				}
				if ((!title.startsWith(word) && !title.endsWith(word))
						|| (!desc.startsWith(word) && !desc.endsWith(word))) {
					termScore += -.568;
				}
				String[] segments = StringUtils.splitByCharacterTypeCamelCase(word);
				if (segments.length > 1) {
					termScore += 0.907;
				}
				if (word.length() < 3)
					continue;
				if (tfidf.containsKey(word)) {
					termScore += 3.332 * tfidf.get(word);
				} else if (tfidf.containsKey(word.toLowerCase())) {
					termScore += 3.332 * tfidf.get(word.toLowerCase());
				}
			} catch (Exception ex) {
				// handle the exception
			}

			// now calculate the relevance
			double relevance = getRelevance(termScore);
			termWeight.put(word, relevance);
		}
		if (INCLUDE_TITLE) {
			String normTitle = new TextNormalizer().normalizeSimpleCodeDiscardSmall(title);
			return normTitle + "\t" + collectTopTerms(bugID, termWeight);
		}
		return collectTopTerms(bugID, termWeight);
	}

	protected String collectTopTerms(int bugID, HashMap<String, Double> termWeight) {
		// sorting the tokens
		List<Map.Entry<String, Double>> list = new LinkedList<>(termWeight.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {
			@Override
			public int compare(Entry<String, Double> o1, Entry<String, Double> o2) {
				// TODO Auto-generated method stub
				Double v2 = o2.getValue();
				Double v1 = o1.getValue();
				return v2.compareTo(v1);
			}
		});

		int MAX_SUGGESTED_TERMS = StaticData.SUGGESTED_KEYWORD_COUNT;
		// get the top 33% keywords
		if (StaticData.USE_DYNAMIC_KEYWORD_THRESHOLD) {
			MAX_SUGGESTED_TERMS = (int) (termWeight.size() * StaticData.KEYWORD_RATIO);
		}

		// now collect top three terms
		String query = new String();
		int count = 0;
		for (Map.Entry<String, Double> entry : list) {
			query += entry.getKey() + " ";
			count++;
			if (count == MAX_SUGGESTED_TERMS)
				break;
		}
		return query;
	}

	public ArrayList<String> makeKevicQuery() {
		ArrayList<Integer> selectedBugs = SelectedBugs.loadSelectedBugs(repoName);
		ArrayList<String> queries = new ArrayList<>();
		for (int bugID : selectedBugs) {
			String kevicQuery = getKevicQuery(bugID);
			queries.add(bugID + "\t" + kevicQuery);
		}
		// clearing the TF-IDF
		TFIDFManager.idfMap.clear();
		return queries;
	}

	public void saveKevicQuery(ArrayList<String> queries) {
		ContentWriter.writeContent(this.kevicQueryFile, queries);
		System.out.println("Done:" + repoName);
	}

	public ArrayList<String> makeKevicQuerySmart() {
		ArrayList<Integer> selectedBugs = SelectedBugs.loadSelectedBugs(repoName);
		ArrayList<String> queries = new ArrayList<>();
		for (int bugID : selectedBugs) {
			String kevicQuery = getKevicQuerySmart(bugID);
			queries.add(bugID + "\t" + kevicQuery);
		}
		// clearing the TF-IDF
		TFIDFManager.idfMap.clear();
		return queries;
	}

	protected ArrayList<String> makeKevicQueryRowsForML() {
		ArrayList<Integer> selectedBugs = SelectedBugs.loadSelectedBugs(repoName);
		ArrayList<String> queries = new ArrayList<>();
		for (int bugID : selectedBugs) {
			ArrayList<String> temp = getKevicQueryLine(bugID);
			queries.addAll(temp);
		}
		// clearing the TF-IDF
		TFIDFManager.idfMap.clear();
		return queries;
	}
}
