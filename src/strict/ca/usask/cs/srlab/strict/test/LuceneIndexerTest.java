package strict.ca.usask.cs.srlab.strict.test;

import org.junit.Test;
import strict.ca.usask.cs.srlab.strict.config.StaticData;
import strict.lucenecheck.IndexLucene;

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
