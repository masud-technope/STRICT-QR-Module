package ca.usask.cs.srlab.strict.utility;

import java.util.ArrayList;

import ca.usask.cs.srlab.strict.config.StaticData;

public class SelectedBugs {

    String repoName;

    public SelectedBugs(String repoName) {
        this.repoName = repoName;
    }

    public static ArrayList<Integer> loadSelectedBugs(String repoName) {
        String selectedBugFile = StaticData.HOME_DIR + "/Selectedbug/" + repoName + ".txt";
        ArrayList<String> lines = ContentLoader.getAllLinesOptList(selectedBugFile);
        ArrayList<Integer> temp = new ArrayList<>();
        for (String line : lines) {
            temp.add(Integer.parseInt(line.trim()));
        }
        return temp;
    }

    public static ArrayList<Integer> loadSelectedHQBBugs(String repoName) {
        String selectedBugFile = StaticData.HOME_DIR + "/Selectedbug-HQB/" + repoName + ".txt";
        ArrayList<String> lines = ContentLoader.getAllLinesOptList(selectedBugFile);
        ArrayList<Integer> temp = new ArrayList<>();
        for (String line : lines) {
            temp.add(Integer.parseInt(line.trim()));
        }
        return temp;
    }

    public static ArrayList<Integer> loadSelectedLQBBugs(String repoName) {
        String selectedBugFile = StaticData.HOME_DIR + "/Selectedbug-LQB/" + repoName + ".txt";
        ArrayList<String> lines = ContentLoader.getAllLinesOptList(selectedBugFile);
        ArrayList<Integer> temp = new ArrayList<>();
        for (String line : lines) {
            temp.add(Integer.parseInt(line.trim()));
        }
        return temp;
    }
}
