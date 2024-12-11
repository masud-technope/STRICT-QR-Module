package ca.usask.cs.srlab.strict.stemmer;

import java.util.ArrayList;
import java.util.Arrays;

import ca.usask.cs.srlab.strict.utility.MiscUtility;
import ca.usask.cs.srlab.strict.utility.MiscUtilityJobs;

public class WordNormalizer {

    String sentence;
    String dottedPattern = "";
    String camelCasePattern = "([A-Z][a-z0-9]+)+";
    String classPattern = "([A-Z][a-z]+){2,}";// "([A-Z][a-z]+)+";
    String methodPattern = "([a-z]+[A-Z][a-z]+){2,}";// "([a-z]+[A-Z][a-z]+)+";
    String numberPattern = "[0-9]+";

    public WordNormalizer(String sentence) {
        // initialization
        this.sentence = sentence;
    }

    public WordNormalizer() {
        // default constructor
    }

    protected ArrayList<String> decomposeDotToken(String token) {
        // decomposing dotted tokens
        ArrayList<String> decomposed = new ArrayList<>();
        if (token.contains(".")) {
            String[] singles = token.split("\\.");
            for (String single : singles) {
                decomposed.add(single);
            }
        } else
            decomposed.add(token);
        return decomposed;
    }

    protected ArrayList<String> decomposeCamelCase(String token) {
        // decomposing camel case tokens using regex
        ArrayList<String> refined = new ArrayList<>();
        String camRegex = "([a-z])([A-Z]+)";
        String replacement = "$1\t$2";
        String filtered = token.replaceAll(camRegex, replacement);
        String[] ftokens = filtered.split("\\s+");
        refined.addAll(Arrays.asList(ftokens));
        return refined;
    }

    public String expandCCWords(String sentence) {
        // expanding the CC words
        String expanded = new String();
        String[] tokens = sentence.split("\\s+");
        for (String token : tokens) {
            ArrayList<String> decomposed = decomposeCamelCase(token);
            if (decomposed.size() > 1) {
                decomposed = MiscUtility.filterSmallTokens(decomposed);
                expanded += token + "\t" + MiscUtility.list2Str(decomposed) + "\t";
            } else {
                expanded += token + "\t";
            }
        }
        return expanded.trim();
    }

    protected ArrayList<String> removeSpecialChars(String sentence) {
        // removing special characters
        String regex = "\\p{Punct}+|\\d+|\\s+";
        String[] parts = sentence.split(regex);
        ArrayList<String> refined = new ArrayList<>();
        for (String token : parts) {
            if (token.trim().isEmpty())
                continue;
            else if (token.trim().length() < 3)
                continue;
            else
                refined.add(token.trim());
        }
        return refined;
    }

    protected String removeTags(String content) {
        return content.replaceAll("\\<[^>]*>", "");
    }

    public String normalizeSentenceWithCCD() {
        // normalize sentence with camel case decomposition
        String refinedSentence = new String();
        Stemmer stemmer = new Stemmer();
        ArrayList<String> cleanedTokens = removeSpecialChars(this.sentence);
        String prevToken = new String();
        for (String token : cleanedTokens) {
            ArrayList<String> decomposed = decomposeCamelCase(token);
            if (decomposed.size() > 1) { // really camel case
                ArrayList<String> temp = new ArrayList<>();
                for (String decom : decomposed) {
                    String stoken = stemmer.stripAffixes(decom);
                    if (stoken.length() > 2) { // significant token
                        if (!stoken.equals(prevToken))
                            temp.add(stoken);
                    }
                    prevToken = stoken;
                }
                String decomposedStr = MiscUtilityJobs.list2Str(temp);
                refinedSentence += decomposedStr + " ";
            } else {
                String stoken = stemmer.stripAffixes(token.toLowerCase());
                if (stoken.length() > 2) { // significant token
                    if (!stoken.equals(prevToken))
                        refinedSentence += stoken + " ";
                }
                prevToken = stoken;
            }
        }
        return refinedSentence;
    }

    public String refineForGiovanni() {
        // refine for Giovanni
        ArrayList<String> cleanedTokens = removeSpecialChars(this.sentence);
        ArrayList<String> tokencoll = new ArrayList<>();
        for (String token : cleanedTokens) {
            ArrayList<String> decomposed = decomposeCamelCase(token);
            tokencoll.addAll(decomposed);
        }
        return MiscUtilityJobs.list2Str(tokencoll);
    }

    public String normalizeSentence() {
        // normalize the sentences
        ArrayList<String> refinedTokens = new ArrayList<>();
        // Stemmer stemmer = new Stemmer();
        // removing tags
        // this.sentence=removeTags(this.sentence);
        ArrayList<String> cleanedTokens = removeSpecialChars(this.sentence);
        String prevToken = new String();
        for (String token : cleanedTokens) {
            if (token.matches(methodPattern)) {
                if (!token.equals(prevToken))
                    refinedTokens.add(token);
                prevToken = token;
            } else if (token.matches(classPattern)) {
                if (!token.equals(prevToken))
                    refinedTokens.add(token);
                prevToken = token;
            } else if (token.matches(camelCasePattern)) {
                if (!token.equals(prevToken))
                    refinedTokens.add(token);
                prevToken = token;
            } else if (token.contains("_")) {
                if (!token.equals(prevToken))
                    refinedTokens.add(token);
                prevToken = token;
            } else {
                // this is a must component, do not remove it.
                if (token.length() > 2) {
                    if (!token.equals(prevToken))
                        refinedTokens.add(token);
                    prevToken = token;
                }

                // stemming is not helping. avoid it.
                /*
                 * String stoken = stemmer.stripAffixes(token.toLowerCase()); if
                 * (stoken.length() > 2) { // significant token if (!stoken.equals(prevToken))
                 * refinedTokens.add(stoken); prevToken = stoken; }
                 */
            }
        }
        return MiscUtilityJobs.list2Str(refinedTokens);
    }

    public static void main(String[] args) {
        String sentence = "When doing debugging, our vm talks to eclipse vm through socket.";
        WordNormalizer norm = new WordNormalizer(sentence);
        System.out.println(norm.normalizeSentence());
    }
}
