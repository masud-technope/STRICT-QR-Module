package strict.utility;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import strict.query.QueryToken;

public class MyItemSorter {

	public static List<Map.Entry<String, QueryToken>> sortQTokensByTotal(
			HashMap<String, QueryToken> qTokenMap) {
		// code for sorting the hash map
		List<Map.Entry<String, QueryToken>> list = new LinkedList<>(
				qTokenMap.entrySet());
		list.sort(new Comparator<Map.Entry<String, QueryToken>>() {

			@Override
			public int compare(Entry<String, QueryToken> e1,
					Entry<String, QueryToken> e2) {
				// TODO Auto-generated method stub
				QueryToken t1 = e1.getValue();
				Double v1 = new Double(t1.totalScore);
				QueryToken t2 = e2.getValue();
				Double v2 = new Double(t2.totalScore);
				return v2.compareTo(v1);
			}
		});
		return list;

	}

	public static List<Map.Entry<String, QueryToken>> sortQTokensByTR(
			HashMap<String, QueryToken> qTokenMap) {
		// code for sorting the hash map
		List<Map.Entry<String, QueryToken>> list = new LinkedList<>(
				qTokenMap.entrySet());
		list.sort(new Comparator<Map.Entry<String, QueryToken>>() {

			@Override
			public int compare(Entry<String, QueryToken> e1,
					Entry<String, QueryToken> e2) {
				// TODO Auto-generated method stub
				QueryToken t1 = e1.getValue();
				Double v1 = new Double(t1.textRankScore);
				QueryToken t2 = e2.getValue();
				Double v2 = new Double(t2.textRankScore);
				return v2.compareTo(v1);
			}
		});
		return list;
	}

	public static List<Map.Entry<String, QueryToken>> sortQTokensByPOSR(
			HashMap<String, QueryToken> qTokenMap) {
		// code for sorting the hash map
		List<Map.Entry<String, QueryToken>> list = new LinkedList<>(
				qTokenMap.entrySet());
		list.sort(new Comparator<Map.Entry<String, QueryToken>>() {
			@Override
			public int compare(Entry<String, QueryToken> e1,
					Entry<String, QueryToken> e2) {
				// TODO Auto-generated method stub
				QueryToken t1 = e1.getValue();
				Double v1 = new Double(t1.posRankScore);
				QueryToken t2 = e2.getValue();
				Double v2 = new Double(t2.posRankScore);
				return v2.compareTo(v1);
			}
		});
		return list;
	}

	public static List<Map.Entry<String, QueryToken>> sortQTokensByBorda(
			HashMap<String, QueryToken> qTokenMap) {
		// code for sorting the hash map
		List<Map.Entry<String, QueryToken>> list = new LinkedList<>(
				qTokenMap.entrySet());
		list.sort(new Comparator<Map.Entry<String, QueryToken>>() {
			@Override
			public int compare(Entry<String, QueryToken> e1,
					Entry<String, QueryToken> e2) {
				// TODO Auto-generated method stub
				QueryToken t1 = e1.getValue();
				Double v1 = new Double(t1.bordaScore);
				QueryToken t2 = e2.getValue();
				Double v2 = new Double(t2.bordaScore);
				return v2.compareTo(v1);
			}
		});
		return list;
	}

	public static List<Map.Entry<String, QueryToken>> sortQTokensByScoreKey(
			HashMap<String, QueryToken> qTokenMap, String scoreKey) {
		// code for sorting the hash map
		List<Map.Entry<String, QueryToken>> list = new LinkedList<>(
				qTokenMap.entrySet());
		list.sort(new Comparator<Map.Entry<String, QueryToken>>() {
			@Override
			public int compare(Entry<String, QueryToken> e1,
					Entry<String, QueryToken> e2) {
				// TODO Auto-generated method stub

				QueryToken t1 = e1.getValue();
				Double v1 = 0.0;
				QueryToken t2 = e2.getValue();
				Double v2 = 0.0;

				switch (scoreKey) {
				case "TR":
					v1 = t1.textRankScore;
					v2 = t2.textRankScore;
					break;
				case "PR":
					v1 = t1.posRankScore;
					v2 = t2.posRankScore;
					break;
				case "TRC":
					v1 = t1.coreRankScore;
					v2 = t2.coreRankScore;
					break;
				case "PRC":
					v1 = t1.coreRankScore;
					v2 = t2.coreRankScore;
					break;
				default:
					break;
				}
				return v2.compareTo(v1);
			}
		});
		return list;
	}
}
