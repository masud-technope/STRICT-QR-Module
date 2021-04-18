package strict.lucenecheck;

import java.util.ArrayList;

public class MethodEntityUtil {

	public static MethodEntity analyseMethodEntity(String resultEntity) {
		// String[] parts = resultEntity.split(":");

		if (resultEntity.trim().isEmpty()) {
			// send blank entity
			return new MethodEntity();
		}

		MethodEntity me = new MethodEntity();

		// sanitize
		resultEntity = sanitize(resultEntity);

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

		// dealing with constructor
		if (methodName.equals("<init>")) {
			me.methodName = extractConstructor(className);
		}

		if (!paramBlock.trim().isEmpty()) {
			String[] params = paramBlock.trim().split(",");
			ArrayList<String> temp = new ArrayList<String>();
			for (String param : params) {
				// adding the name only version; not the canonical
				String sanitized = sanitize(param);
				String nameOnly = getItemNameOnly(sanitized.trim());
				// temp.add(param.trim());
				temp.add(nameOnly);
			}
			me.parameters = temp;
		}

		return me;
	}

	public static boolean checkFileMatch(String goldItem, String resultItem) {
		MethodEntity meGold = analyseMethodEntity(goldItem);
		MethodEntity meResult = analyseMethodEntity(resultItem);
		if (meGold.className.equals(meResult.className) || meGold.className.endsWith(meResult.className)
				|| meResult.className.endsWith(meGold.className)) {

			if (meGold.methodName.equals(meResult.methodName)) {
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

	public static boolean checkFileMatch(MethodEntity meGold, MethodEntity meResult) {
		if (meGold.className.equals(meResult.className) || meGold.className.endsWith(meResult.className)
				|| meResult.className.endsWith(meGold.className)) {

			if (meGold.methodName.equals(meResult.methodName)) {
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

	protected static String sanitize(String canonicalName) {
		// normalizing the inner class
		canonicalName = canonicalName.replace('$', ':');
		return canonicalName;
	}

	protected static String extractConstructor(String className) {
		String constructor = className;
		if (className.contains(":")) {
			String[] cparts = className.split(":");
			constructor = cparts[cparts.length - 1];
		}
		return constructor;
	}

	protected static String getItemNameOnly(String canonicalName) {
		String nameOnly = canonicalName;
		if (canonicalName.contains(".")) {
			String[] items = canonicalName.split("\\.");
			nameOnly = items[items.length - 1];
		}
		return nameOnly;
	}

}
