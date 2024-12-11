package ca.usask.cs.srlab.strict;

import ca.usask.cs.srlab.strict.lucenecheck.LuceneSearcher; 
import ca.usask.cs.srlab.strict.lucenecheck.MethodEntityUtil;
import org.junit.Test;

public class LuceneSearcherTest {

    @Test
    public void testLuceneSearch() {
        int bugID = 303705;
        String repository = "eclipse.jdt.ui";
        String searchQuery = "IJava Content Tree element IResource java Element Provider";
        LuceneSearcher searcher = new LuceneSearcher(bugID, repository, searchQuery);
        System.out.println("First found index:" + searcher.getFirstGoldRank());
    }

    @Test
    public void testMethodEntryAnalysis() {
        String entry = "org.apache.commons.math3.geometry.euclidean.twod.Line$LineTransform:<init>(java.awt.geom.AffineTransform)";
        MethodEntityUtil.analyseMethodEntity(entry);
    }

}
