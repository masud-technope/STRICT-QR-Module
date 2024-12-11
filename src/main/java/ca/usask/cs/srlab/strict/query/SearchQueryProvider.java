package ca.usask.cs.srlab.strict.query;

import java.util.ArrayList;
import java.util.Arrays;

import ca.usask.cs.srlab.strict.config.StaticData;
import ca.usask.cs.srlab.strict.text.normalizer.TextNormalizer;
import ca.usask.cs.srlab.strict.utility.BugReportLoader;
import ca.usask.cs.srlab.strict.utility.ContentWriter;
import ca.usask.cs.srlab.strict.utility.MiscUtility;

public class SearchQueryProvider {

    String repoName;
    String scoreKey;
    ArrayList<Integer> selectedBugs;
    String queryOutputFile;

    public SearchQueryProvider(String repoName, String scoreKey, ArrayList<Integer> selectedBugs) {
        this.repoName = repoName;
        this.scoreKey = scoreKey;
        this.selectedBugs = selectedBugs;
    }

    protected void setQueryFile(String fileName) {
        this.queryOutputFile = fileName;
    }

    protected void saveQueries(ArrayList<String> queries) {
        ContentWriter.writeContent(this.queryOutputFile, queries);
    }

    protected String getCamelCaseQuery(String bugReport) {
        ArrayList<String> sentences = getCCExpandedTokens(bugReport);
        SearchTermProvider provider = new SearchTermProvider(sentences);
        return provider.provideSearchQuery("TR");
    }

    protected ArrayList<String> getCCExpandedTokens(String bugReport) {
        String[] words = bugReport.split("\\p{Punct}+|\\d+|\\s+");
        ArrayList<String> wordList = new ArrayList<>(Arrays.asList(words));
        TextNormalizer textNormalizer = new TextNormalizer();
        ArrayList<String> codeLikeElems = textNormalizer.extractCodeItem(wordList);
        ArrayList<String> sentences = new ArrayList<>();
        for (String celem : codeLikeElems) {
            String sentence = MiscUtility.list2Str(textNormalizer.decomposeCamelCase(celem));
            sentences.add(sentence);
        }
        return sentences;
    }

    protected String getNormalizedTitle(String title) {
        return new TextNormalizer().normalizeSimpleCodeDiscardSmall(title);
    }

    public ArrayList<String> provideSearchQueries() {
        ArrayList<String> queries = new ArrayList<>();
        for (int bugID : this.selectedBugs) {
            String bugReport = BugReportLoader.loadBugReport(repoName, bugID);
            String title = BugReportLoader.loadBugReportTitle(repoName, bugID);

            SearchTermProvider provider = new SearchTermProvider(repoName, bugID, title, bugReport);
            String suggestedKeywords = provider.provideSearchQuery(scoreKey);
            String suggestedQuery = new String(suggestedKeywords);

            if (StaticData.ADD_CODE_ELEM) {
                String codetokens = getCamelCaseQuery(bugReport);
                suggestedQuery += "\t" + codetokens;
            }
            if (StaticData.ADD_TITLE) {
                String titletokens = getNormalizedTitle(title);
                suggestedQuery += "\t" + titletokens;
            }

            String queryLine = bugID + "\t" + suggestedQuery;
            queries.add(queryLine);
        }
        return queries;
    }
}
