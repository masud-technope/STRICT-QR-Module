package ca.usask.cs.srlab.strict.utility;

import java.util.ArrayList;
import java.util.HashMap;

import ca.usask.cs.srlab.strict.config.StaticData;

public class QueryLoader {

    protected static String extractQuery(String line) {
        String temp = new String();
        String[] parts = line.split("\\s+");
        for (int i = 1; i < parts.length; i++) {
            temp += parts[i] + " ";
            if (i == StaticData.MAX_QUERY_LEN)
                break;
        }
        return temp.trim();
    }

    public static HashMap<Integer, String> loadQuery(String queryFile) {
        ArrayList<String> qlines = ContentLoader.getAllLinesOptList(queryFile);
        HashMap<Integer, String> queryMap = new HashMap<>();
        for (String line : qlines) {
            int bugID = Integer.parseInt(line.split("\\s+")[0]);
            String query = extractQuery(line);
            queryMap.put(bugID, query);
        }
        return queryMap;
    }

    public static HashMap<Integer, Integer> loadQueryQE(String queryEffFile) {
        ArrayList<String> qlines = ContentLoader.getAllLinesOptList(queryEffFile);
        HashMap<Integer, Integer> queryMap = new HashMap<>();
        for (String line : qlines) {
            int bugID = Integer.parseInt(line.split("\\s+")[0]);
            int qe = Integer.parseInt(line.trim().split("\\s+")[1]);
            queryMap.put(bugID, qe);
        }
        return queryMap;
    }

}
