package ca.usask.cs.srlab.strict.lucenecheck;

import java.util.ArrayList;

import ca.usask.cs.srlab.strict.config.StaticData;
import ca.usask.cs.srlab.strict.utility.ContentLoader;

public class GoldsetLoader {

    public static ArrayList<String> loadGoldset(String repoName, int bugID) {
        String gsFile = StaticData.HOME_DIR + "/Goldset/" + repoName + "/" + bugID + ".txt";
        return ContentLoader.getAllLinesOptList(gsFile);
    }

    public static ArrayList<String> loadGoldset(String repoName, String bugID, String subFolder) {
        String gsFile = StaticData.HOME_DIR + "/Goldset/" + subFolder + "/" + repoName + "/" + bugID + ".txt";
        return ContentLoader.getAllLinesOptList(gsFile);
    }
}
