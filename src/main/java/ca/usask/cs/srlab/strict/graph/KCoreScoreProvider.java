package ca.usask.cs.srlab.strict.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import ca.usask.cs.srlab.strict.query.QueryToken;
import ca.usask.cs.srlab.strict.text.normalizer.TextNormalizer;
import ca.usask.cs.srlab.strict.utility.BugReportLoader;
import ca.usask.cs.srlab.strict.utility.ContentLoader;

public class KCoreScoreProvider {

    SimpleDirectedWeightedGraph<String, DefaultWeightedEdge> wgraph;
    int K;
    boolean isPOS;

    public KCoreScoreProvider(SimpleDirectedWeightedGraph<String, DefaultWeightedEdge> wgraph, int K) {
        this.wgraph = wgraph;
        this.K = K;
    }

    protected SimpleDirectedWeightedGraph<String, DefaultWeightedEdge> removeNode(
            SimpleDirectedWeightedGraph<String, DefaultWeightedEdge> g, String node) {
        try {
            if (g.containsVertex(node)) {
                // System.out.println(node);
                boolean isRemoved = g.removeVertex(node);
                // System.out.println(isRemoved);
            }
        } catch (Exception exc) {
            // handle the exception
            exc.printStackTrace();
        }
        return g;
    }

    protected String countPeripheralNodes(SimpleDirectedWeightedGraph<String, DefaultWeightedEdge> g) {
        Set<String> nodes = g.vertexSet();
        for (String node : nodes) {
            Set<DefaultWeightedEdge> edges = g.edgesOf(node);
            double totalWeight = 0;
            for (DefaultWeightedEdge edge : edges) {
                totalWeight += g.getEdgeWeight(edge);
            }
            // System.out.println(node+" "+totalWeight);
            if (totalWeight < K) {
                return node;
            }
        }
        return new String();
    }

    protected SimpleDirectedWeightedGraph<String, DefaultWeightedEdge> trimGraph(
            SimpleDirectedWeightedGraph<String, DefaultWeightedEdge> g) {
        String target = new String();

        SimpleDirectedWeightedGraph<String, DefaultWeightedEdge> updatedG = g;
        while (!(target = countPeripheralNodes(g)).isEmpty()) {
            updatedG = removeNode(g, target);
            g = updatedG;
            // System.out.println(g.vertexSet().size());
        }
        return updatedG;
    }

    public SimpleDirectedWeightedGraph<String, DefaultWeightedEdge> provideKCoreGraph() {
        return trimGraph(this.wgraph);
    }

    public HashMap<String, Double> provideKCoreScores() {
        SimpleDirectedWeightedGraph<String, DefaultWeightedEdge> kcore = provideKCoreGraph();
        HashMap<String, Double> degreeMap = new HashMap<>();
        double maxDegree = 0;
        for (String node : kcore.vertexSet()) {
            int degree = kcore.inDegreeOf(node) + kcore.outDegreeOf(node);
            degreeMap.put(node, (double) degree);
            if (degree > maxDegree) {
                maxDegree = degree;
            }
        }
        // now do the normalization
        for (String node : degreeMap.keySet()) {
            double normDegree = degreeMap.get(node) / maxDegree;
            degreeMap.put(node, normDegree);
        }
        return degreeMap;
    }

    public HashMap<String, QueryToken> getTokenDictionary(boolean weighted,
                                                          SimpleDirectedWeightedGraph<String, DefaultWeightedEdge> kcore) {
        // populating token dictionary
        HashSet<String> nodes = new HashSet<>();
        HashMap<String, QueryToken> tokendb = new HashMap<>();
        if (weighted) {
            nodes.addAll(kcore.vertexSet());
        }
        for (String vertex : nodes) {
            QueryToken qtoken = new QueryToken();
            qtoken.token = vertex;
            tokendb.put(vertex, qtoken);
        }
        return tokendb;
    }

    public HashMap<String, Double> provideKCoreScoresV2(boolean isPOS) {
        SimpleDirectedWeightedGraph<String, DefaultWeightedEdge> kcore = provideKCoreGraph();
        HashMap<String, Double> scoreMap = new HashMap<>();
        HashMap<String, QueryToken> tokendb = getTokenDictionary(true, kcore);

        if (isPOS) {
            POSRankProvider prProvider = new POSRankProvider(kcore, tokendb);
            tokendb = prProvider.calculatePOSRankWeighted();
            for (String token : tokendb.keySet()) {
                QueryToken qtoken = tokendb.get(token);
                scoreMap.put(token, qtoken.posRankScore);
            }
        } else {
            TextRankProvider trProvider = new TextRankProvider(kcore, tokendb);
            tokendb = trProvider.calculateTextRankWeighted();
            for (String token : tokendb.keySet()) {
                QueryToken qtoken = tokendb.get(token);
                scoreMap.put(token, qtoken.textRankScore);
            }
        }

        return scoreMap;
    }

    protected static ArrayList<String> extractNormalizedSentencesV2(String content) {
        String separator = "(?<=[.?!:;])\\s+(?=[a-zA-Z0-9])";
        String[] sentences = content.split(separator);
        ArrayList<String> normSentences = new ArrayList<>();
        for (String sentence : sentences) {
            String normSentence = new TextNormalizer().normalizeSimpleCodeDiscardSmall(sentence);
            normSentences.add(normSentence);
        }
        return normSentences;
    }

    protected void showDegrees(SimpleDirectedWeightedGraph<String, DefaultWeightedEdge> graph) {
        for (String node : graph.vertexSet()) {
            System.out.println(node + "\t" + (graph.inDegreeOf(node) + graph.outDegreeOf(node)));
        }
    }

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        int bugID = 33888;
        String title = "33888 � Enhance abbreviation options for logger and class layout pattern converters";
        String reportContent = ContentLoader.loadFileContent("./sample/33888.txt");

        ArrayList<String> sentences = extractNormalizedSentencesV2(reportContent);
        WordNetworkMaker wnMaker = new WordNetworkMaker(sentences);
        SimpleDirectedWeightedGraph<String, DefaultWeightedEdge> graph = wnMaker.createWeightedWordNetwork();

        int K = 4;
        System.out.println("Original:" + graph.vertexSet().size());
        KCoreScoreProvider kcore = new KCoreScoreProvider(graph, K);
        SimpleDirectedWeightedGraph<String, DefaultWeightedEdge> trimmed = kcore.provideKCoreGraph();
        System.out.println("New graph:" + trimmed.vertexSet().size());
        kcore.showDegrees(trimmed);
    }
}
