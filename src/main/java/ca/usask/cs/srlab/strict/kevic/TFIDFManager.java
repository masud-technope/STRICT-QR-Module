package ca.usask.cs.srlab.strict.kevic;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import ca.usask.cs.srlab.strict.config.StaticData;

public class TFIDFManager {

    String indexFolder;
    String repoName;
    public static HashMap<String, Double> idfMap;
    public static HashMap<String, Double> dfRatioMap;
    public static final String FIELD_CONTENTS = "contents";
    String targetTerm;
    HashSet<String> keys;

    public TFIDFManager(String targetTerm, String repoName) {
        this.indexFolder = StaticData.HOME_DIR + "/Lucene/index-method/" + repoName;
        this.targetTerm = targetTerm;
    }

    public TFIDFManager(String repoName) {
        this.indexFolder = StaticData.HOME_DIR + "/Lucene/index-method/" + repoName;
        this.repoName = repoName;
        this.keys = new HashSet<>();
        this.idfMap = new HashMap<>();
        this.dfRatioMap = new HashMap<>();
    }

    protected double getIDF(int N, int DF) {
        // getting the IDF
        return Math.log(1 + (double) N / DF);
    }

    public HashMap<String, Double> calculateIDF() {
        // calculate IDF of a term
        IndexReader reader = null;
        try {
            reader = DirectoryReader.open(FSDirectory.open(new File(indexFolder).toPath()));
            // String targetTerm = "breakpoint";

            Fields fields = MultiFields.getFields(reader);
            for (String field : fields) {
                Terms terms = fields.terms(field);
                TermsEnum termsEnum = terms.iterator();
                BytesRef bytesRef;
                while ((bytesRef = termsEnum.next()) != null) {
                    if (termsEnum.seekExact(bytesRef)) {
                        String term = bytesRef.utf8ToString();
                        this.keys.add(term);
                    }
                }
            }

            // now get their DF
            int N = reader.numDocs();
            for (String term : this.keys) {
                Term t = new Term(FIELD_CONTENTS, term);
                int docFreq = reader.docFreq(t);
                double idf = getIDF(N, docFreq);

                // adding IDF values
                if (!this.idfMap.containsKey(term)) {
                    this.idfMap.put(term, idf);
                }
                // storing the DF ratios
                if (!this.dfRatioMap.containsKey(term)) {
                    double dfR = (double) docFreq / N;
                    this.dfRatioMap.put(term, dfR);
                }
            }

            System.out.println("IDF map loaded:" + repoName);

        } catch (Exception exc) {
            exc.printStackTrace();
        }
        return idfMap;
    }
}
