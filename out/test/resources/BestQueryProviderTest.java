package ca.usask.cs.srlab.strict.test;

import java.util.HashMap;

import org.junit.Test;
import ca.usask.cs.srlab.strict.config.StaticData;
import ca.usask.cs.srlab.strict.weka.model.BestQueryProvider;

public class BestQueryProviderTest {


    @Test
    public void testProvideBestQueries() {
        String repoName = "atunes-1.10.0";
        String predictionFile = StaticData.HOME_DIR + "\\Proposed-STRICT\\Query-Difficulty-Model\\predictions-apr17-4pm\\"
                + repoName + ".txt";
        HashMap<Integer, String> queryMap = new BestQueryProvider(repoName, predictionFile).extractPredictedQueries();
        for (int bugID : queryMap.keySet()) {
            System.out.println(bugID + "\t" + queryMap.get(bugID));
        }
    }
}
