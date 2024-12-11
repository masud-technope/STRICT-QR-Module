package ca.usask.cs.srlab.strict;

import java.util.ArrayList;

import org.junit.Test;
import ca.usask.cs.srlab.strict.scanniello.method.LucenePRSearcher;
import ca.usask.cs.srlab.strict.lucenecheck.MethodResultRankMgr;
import ca.usask.cs.srlab.strict.query.evaluation.QueryPerformanceCalc;
import ca.usask.cs.srlab.strict.utility.ContentLoader;

public class QueryPerformanceCalcTest {

    @Test
    public void testQueryPerformanceAllRepos() {
        ArrayList<String> repos = ContentLoader.getAllLinesOptList("./repos/repos.txt");

        MethodResultRankMgr.matchClass = false;
        QueryPerformanceCalc.useHQB = false;
        QueryPerformanceCalc.useLQB = false;

        String approachFolder = "proposed-STRICT";
        // String approachFolder = "Rocchio";

        /** use the baseline ***/
        QueryPerformanceCalc.useBaseline = false;

        /* use of PR scanniello */
        QueryPerformanceCalc.useScanniello = false;
        LucenePRSearcher.ALL_RESULTS = 100;

        int[] hits = {10, 50, 100};

        for (int hit : hits) {

            for (String repoName : repos) {
                String resultKey = "STRICT-TRC-10-title";
                //String resultKey = "STRICT-best-query-dec23-8pm";

                new QueryPerformanceCalc(repoName, resultKey, approachFolder).getQueryPerformance(hit);
            }

            System.out.println(QueryPerformanceCalc.sumTopKAcc / repos.size() + ", "
                    + QueryPerformanceCalc.sumAP / repos.size() + ", " + QueryPerformanceCalc.sumRR / repos.size());

            QueryPerformanceCalc.sumTopKAcc = 0;
            QueryPerformanceCalc.sumAP = 0;
            QueryPerformanceCalc.sumRR = 0;

            QueryPerformanceCalc.masterHitkList.clear();
            QueryPerformanceCalc.masterAPList.clear();
            QueryPerformanceCalc.masterRRList.clear();
        }
    }
}
