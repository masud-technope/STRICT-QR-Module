package ca.usask.cs.srlab.strict.utility;

import java.util.ArrayList;

import ca.usask.cs.srlab.strict.config.StaticData;

public class BugReportLoader {
    public static String loadBugReport(String repoName, int bugID) {

        String brFile = StaticData.HOME_DIR + "/Changereqs/" + repoName + "/" + bugID + ".txt";
        return ContentLoader.loadFileContent(brFile);
    }

    public static String loadBugReportTitle(String repoName, int bugID) {

        String brFile = StaticData.HOME_DIR + "/Changereqs/" + repoName + "/" + bugID + ".txt";
        return ContentLoader.getAllLinesOptList(brFile).get(0).trim();
    }

    public static String loadBugReportDesc(String repoName, int bugID) {
        String brFile = StaticData.HOME_DIR + "/Changereqs/" + repoName + "/" + bugID + ".txt";
        String desc = new String();
        ArrayList<String> brLines = ContentLoader.getAllLinesOptList(brFile);
        // skip the title, and start from index=1
        for (int i = 1; i < brLines.size(); i++) {
            desc += brLines.get(i) + "\n";
        }
        return desc.trim();
    }
}
