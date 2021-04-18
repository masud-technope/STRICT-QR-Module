package strict.lucenecheck;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import strict.ca.usask.cs.srlab.strict.config.StaticData;
import strict.utility.ContentLoader;

public class MethodResultRankMgr {

	String repoName;
	ArrayList<String> results;
	ArrayList<String> goldset;
	public static HashMap<Integer, String> keyMap = new HashMap<>();
	String keyfile;

	public static boolean matchParam = true;
	public static boolean matchMethod = false;
	public static boolean matchClass = false;

	public MethodResultRankMgr(String repoName, int bugID, ArrayList<String> results) {
		this.repoName = repoName;
		this.results = results;
		this.goldset = GoldsetLoader.loadGoldset(repoName, bugID);
		this.keyfile = StaticData.HOME_DIR + "/Corpus/" + this.repoName + ".keys";
		if (keyMap.isEmpty()) {
			this.loadKeys();
		}
	}

	protected void loadKeys() {
		// loading file name keys
		ArrayList<String> lines = ContentLoader.getAllLinesOptList(this.keyfile);
		for (String line : lines) {
			String[] parts = line.split("=");
			try {
				int fileID = Integer.parseInt(parts[0].trim());
				String restLine = parts[1].trim();
				String ccMethodName = restLine;
				if (restLine.indexOf('/') > 0) {
					int leftHashIndex = restLine.indexOf('/');
					ccMethodName = restLine.substring(0, leftHashIndex).trim();
				}
				// String key = parts[0] + ".java";
				keyMap.put(fileID, ccMethodName);
			} catch (Exception exc) {
				System.err.println(line);
			}
		}
	}

	protected ArrayList<String> translateResults(ArrayList<String> results) {
		// translating the results
		ArrayList<String> translated = new ArrayList<>();
		for (String fileURL : results) {
			String keyFileName = new File(fileURL).getName();
			int key = Integer.parseInt(keyFileName.split("\\.")[0]);
			if (keyMap.containsKey(key)) {
				String methodCCName = keyMap.get(key);
				translated.add(methodCCName);
			}
		}
		return translated;
	}

	protected boolean checkFileMatch(String goldItem, String resultItem) {
		MethodEntity meGold = analyseMethodEntity(goldItem);
		MethodEntity meResult = analyseMethodEntity(resultItem);
		if (meGold.className.equals(meResult.className)) {

			if (matchClass)
				return true;

			if (meGold.methodName.equals(meResult.methodName)) {

				if (matchMethod)
					return true;

				if (meGold.parameters.size() == meResult.parameters.size()) {
					int pmatched = 0;
					for (int i = 0; i < meGold.parameters.size(); i++) {
						if (meGold.parameters.get(i).equals(meResult.parameters.get(i))) {
							pmatched++;
						}
					}
					if (pmatched == meGold.parameters.size()) {
						return true;
					}
				}
			}
		}
		return false;
	}

	protected MethodEntity analyseMethodEntity(String resultEntity) {
		// String[] parts = resultEntity.split(":");

		if (resultEntity.trim().isEmpty()) {
			// send blank entity
			return new MethodEntity();
		}

		MethodEntity me = new MethodEntity();

		int lastDashIndex = resultEntity.lastIndexOf(':');
		String ccName = resultEntity.substring(0, lastDashIndex);
		String theRest = resultEntity.substring(lastDashIndex + 1);

		String className = ccName;
		if (ccName.indexOf(".") > 0) {
			String[] cparts = ccName.split("\\.");
			className = cparts[cparts.length - 1];
		}

		int leftBraceIndex = 0, rightBraceIndex = 0;
		if (theRest.indexOf('(') > 0) {
			leftBraceIndex = theRest.indexOf('(');
		}
		if (theRest.indexOf(')') > 0) {
			rightBraceIndex = theRest.lastIndexOf(')');
		}
		String methodName = theRest;
		String paramBlock = "()";
		try {
			methodName = theRest.substring(0, leftBraceIndex);
			paramBlock = theRest.substring(leftBraceIndex + 1, rightBraceIndex);
		} catch (Exception exc) {
			// handle the exception
		}

		// now add the items to a class
		me.canonicalClassName = ccName;
		me.className = className;
		me.methodName = methodName;
		if (!paramBlock.trim().isEmpty()) {
			String[] params = paramBlock.trim().split(",");
			ArrayList<String> temp = new ArrayList<String>();
			for (String param : params) {
				temp.add(param.trim());
			}
			me.parameters = temp;
		}

		return me;
	}

	public int getFirstGoldRank() {
		int foundIndex = -1;
		int index = 0;
		ArrayList<String> results = translateResults(this.results);
		outer: for (String elem : results) {
			index++;
			for (String item : goldset) {
				if (checkFileMatch(item, elem)) {
					// found = true;
					foundIndex = index;
					break outer;
				}
			}
		}
		return foundIndex;
	}

	public ArrayList<Integer> getCorrectRanks() {
		ArrayList<Integer> foundIndices = new ArrayList<>();
		ArrayList<String> results = translateResults(this.results);
		int index = 0;
		for (String elem : results) {
			index++;
			for (String item : goldset) {
				if (checkFileMatch(item, elem)) {
					// found = true;
					if (!foundIndices.contains(index)) {
						foundIndices.add(index);
					}
					// break;
				}
			}
		}
		return foundIndices;
	}
}
