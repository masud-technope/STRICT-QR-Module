package ca.usask.cs.srlab.strict;

import org.junit.Test;
import ca.usask.cs.srlab.strict.config.StaticData;
import ca.usask.cs.srlab.strict.lucenecheck.IndexLucene;

public class LuceneIndexerTest {

    @Test
    public void testLuceneIndex() {
        String repoName = "tomcat70";
        // IndexLucene indexer=new IndexLucene(repoName);
        String docs = StaticData.HOME_DIR + "/Corpus/norm-method/" + repoName;
        String index = StaticData.HOME_DIR + "/Lucene/index-method/" + repoName;
        IndexLucene indexer = new IndexLucene(index, docs);
        indexer.indexCorpusFiles();
    }

}
