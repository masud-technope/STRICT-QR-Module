package strict.ca.usask.cs.srlab.strict.test;

import org.junit.Test;
import ca.usask.cs.srlab.strict.config.StaticData;
import ca.usask.cs.srlab.strict.weka.model.BestWorstQueryDetector;

public class BestWorstQueryDetectorTest {

    @Test
    public void testBestWorst() {
        String[] scoreKeys = {"TR", "PR", "TPR", "TPRE", "TRC", "PRC", "TPRC"};
        String repoName = "atunes-1.10.0";
        BestWorstQueryDetector detector = new BestWorstQueryDetector(repoName, scoreKeys);
        String bestFolder = StaticData.HOME_DIR + "/Proposed-STRICT/Query-Difficulty-Model/bestq1";
        detector.setBestQueryFolder(bestFolder);
        String worstFolder = StaticData.HOME_DIR + "/Proposed-STRICT/Query-Difficulty-Model/worstq1";
        detector.setWorstQueryFolder(worstFolder);

        detector.collectBestQueries();
        detector.collectWorstQueries();

    }
}
