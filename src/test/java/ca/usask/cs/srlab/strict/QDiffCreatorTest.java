package ca.usask.cs.srlab.strict;

import org.junit.Test;
import ca.usask.cs.srlab.strict.config.StaticData;
import ca.usask.cs.srlab.strict.weka.model.MyQDiffCollector2020;

public class QDiffCreatorTest {

    @Test
    public void testQDiffCreator() {
        String repoName = "adempiere-3.1.0";
        String qDiffFolder = StaticData.HOME_DIR + "/Proposed-STRICT/Query-Difficulty-Model";
        String modelFolder = qDiffFolder + "/sample-model";
        String indexFolder = StaticData.HOME_DIR + "/Lucene/index-method/" + repoName;
        String corpusFolder = "C:\\MyWorks\\MyResearch\\STRICT\\experiment\\TSE-Experiment-2018\\Corpus\\norm-method\\"
                + repoName;
        MyQDiffCollector2020 diffCreator = new MyQDiffCollector2020(repoName, indexFolder, corpusFolder);
        diffCreator.setQueryDiffFolder(qDiffFolder);
        diffCreator.setModelFolder(modelFolder);
        diffCreator.calculateQueryDifficulties();
    }
}
