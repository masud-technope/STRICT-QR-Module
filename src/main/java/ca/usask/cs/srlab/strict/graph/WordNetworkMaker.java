package ca.usask.cs.srlab.strict.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JFrame;

import org.jgraph.graph.DefaultEdge;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import ca.usask.cs.srlab.strict.query.QueryToken;

public class WordNetworkMaker {

    ArrayList<String> sentences;
    public SimpleDirectedWeightedGraph<String, DefaultWeightedEdge> wgraph;
    public DirectedGraph<String, DefaultEdge> graph;
    HashMap<String, QueryToken> tokendb;
    final int WINDOW_SIZE = 2;
    HashMap<String, Integer> coocCountMap;

    public WordNetworkMaker(ArrayList<String> sentences) {
        // initializing both graphs
        this.sentences = sentences;
        this.wgraph = new SimpleDirectedWeightedGraph<>(
                DefaultWeightedEdge.class);
        this.graph = new DefaultDirectedGraph<>(DefaultEdge.class);
        this.tokendb = new HashMap<>();
        this.coocCountMap = new HashMap<>();
    }

    public WordNetworkMaker(ArrayList<String> sentences,
                            HashMap<String, ArrayList<String>> alltermMap) {
        // initializing both graphs
        this.sentences = sentences;
        this.wgraph = new SimpleDirectedWeightedGraph<>(
                DefaultWeightedEdge.class);
        this.graph = new DefaultDirectedGraph<>(DefaultEdge.class);
        this.tokendb = new HashMap<>();
        this.coocCountMap = new HashMap<>();
    }

    public DirectedGraph<String, DefaultEdge> createWordNetwork() {
        // developing the word network
        for (String sentence : this.sentences) {
            String[] tokens = sentence.split("\\s+");
            for (int index = 0; index < tokens.length; index++) {
                String previousToken = new String();
                String nextToken = new String();
                String currentToken = tokens[index];
                if (index > 0)
                    previousToken = tokens[index - 1];

                if (index < tokens.length - 1)
                    nextToken = tokens[index + 1];

                // now add the graph nodes
                if (!graph.containsVertex(currentToken)) {
                    graph.addVertex(currentToken);
                }
                if (!graph.containsVertex(previousToken)
                        && !previousToken.isEmpty()) {
                    graph.addVertex(previousToken);
                }
                if (!graph.containsVertex(nextToken) && !nextToken.isEmpty()) {
                    graph.addVertex(nextToken);
                }

                // adding edges to the graph
                if (!previousToken.isEmpty())
                    if (!graph.containsEdge(currentToken, previousToken)) {
                        graph.addEdge(currentToken, previousToken);
                    }

                if (!nextToken.isEmpty())
                    if (!graph.containsEdge(currentToken, nextToken)) {
                        graph.addEdge(currentToken, nextToken);
                    }
            }
        }
        // returning the created graph
        return graph;
    }

    protected void setEdgeWeight() {
        int maxFreq = 0;
        Set<DefaultWeightedEdge> edges = this.wgraph.edgeSet();
        for (DefaultWeightedEdge edge : edges) {
            String source = wgraph.getEdgeSource(edge);
            String dest = wgraph.getEdgeTarget(edge);
            String keypair = source + "-" + dest;
            if (coocCountMap.containsKey(keypair)) {
                this.wgraph.setEdgeWeight(edge,
                        (double) coocCountMap.get(keypair));
            }
        }
    }

    protected void updateCooccCount(String source, String dest) {
        // updating the co-occurrence count
        String keypair = source + "-" + dest;
        if (this.coocCountMap.containsKey(keypair)) {
            int updated = coocCountMap.get(keypair) + 1;
            this.coocCountMap.put(keypair, updated);
        } else {
            this.coocCountMap.put(keypair, 1);
        }
    }

    public SimpleDirectedWeightedGraph<String, DefaultWeightedEdge> createWeightedWordNetwork() {
        // developing the word network
        for (String sentence : this.sentences) {
            String[] tokens = sentence.split("\\s+");
            for (int index = 0; index < tokens.length; index++) {
                String previousToken = new String();
                String nextToken = new String();
                String currentToken = tokens[index];
                if (index > 0)
                    previousToken = tokens[index - 1];

                if (index < tokens.length - 1)
                    nextToken = tokens[index + 1];

                // now add the graph nodes
                if (!wgraph.containsVertex(currentToken)) {
                    wgraph.addVertex(currentToken);
                }
                if (!wgraph.containsVertex(previousToken)
                        && !previousToken.isEmpty()) {
                    wgraph.addVertex(previousToken);
                }
                if (!wgraph.containsVertex(nextToken) && !nextToken.isEmpty()) {
                    wgraph.addVertex(nextToken);
                }

                // adding edges to the graph
                if (!previousToken.isEmpty()
                        && !currentToken.equals(previousToken)) {
                    if (!wgraph.containsEdge(currentToken, previousToken)) {
                        DefaultWeightedEdge e = wgraph.addEdge(currentToken,
                                previousToken);
                    }
                    updateCooccCount(currentToken, previousToken);
                }
                if (!nextToken.isEmpty() && !currentToken.equals(nextToken)) {
                    if (!wgraph.containsEdge(currentToken, nextToken)) {
                        DefaultWeightedEdge e = wgraph.addEdge(currentToken,
                                nextToken);
                    }
                    updateCooccCount(currentToken, nextToken);
                }
            }
        }

        // setting edge weight
        this.setEdgeWeight();

        // returning the created graph
        return wgraph;
    }

    public HashMap<String, QueryToken> getTokenDictionary(boolean weighted) {
        // populating token dictionary
        HashSet<String> nodes = new HashSet<>();
        if (weighted)
            nodes.addAll(wgraph.vertexSet());
        else
            nodes.addAll(graph.vertexSet());
        for (String vertex : nodes) {
            QueryToken qtoken = new QueryToken();
            qtoken.token = vertex;
            this.tokendb.put(vertex, qtoken);
        }
        return this.tokendb;
    }

    protected ArrayList<String> collectTopTokens(
            HashMap<String, QueryToken> sortedtokendb) {
        // collecting top tokens
        ArrayList<String> toptokens = new ArrayList<>();
        int count = 0;
        for (String key : sortedtokendb.keySet()) {
            toptokens.add(key);
            count++;
            if (count == 5)
                break;
        }
        return toptokens;
    }

    protected ArrayList<String> getImportantTokens(
            HashMap<String, QueryToken> sortedtokendb, String bugtitle) {
        ArrayList<String> toptokens = new ArrayList<>();
        int count = 0;
        int intitle = 0;
        for (String key : sortedtokendb.keySet()) {
            if (bugtitle.contains(key)) {
                toptokens.add(key);
                count++;
                intitle++;
            }
            if (count == 5)
                break;
        }
        int lateradded = 0;
        if (intitle < 5) {
            for (String token : sortedtokendb.keySet()) {
                if (!bugtitle.contains(token)) {
                    toptokens.add(token);
                    lateradded++;
                    if (lateradded + intitle == 5)
                        break;
                }
            }
        }
        return toptokens;
    }

    public void showEdges(HashMap<String, QueryToken> tokendb) {
        // showing the network edges
        if (graph != null) {
            Set<DefaultEdge> edges = graph.edgeSet();
            ArrayList<DefaultEdge> edgeList = new ArrayList<>(edges);
            for (DefaultEdge edge : edgeList) {
                System.out.println(graph.getEdgeSource(edge) + "---"
                        + graph.getEdgeTarget(edge));
            }
        }
    }

    public void visualizeWordGraph(HashMap<String, QueryToken> sortedtokendb,
                                   String bugtitle) {
        // visualize the word net
        JFrame frame = new JFrame();
        // ArrayList<String> toptokens = collectTopTokens(sortedtokendb);
        ArrayList<String> toptokens = getImportantTokens(sortedtokendb,
                bugtitle);
        // StackGraph stackgraph=new StackGraph(graph, 900, 700);
        StackGraph stackgraph = new StackGraph(graph, 900, 700, toptokens);
        frame.getContentPane().add(stackgraph);
        frame.setVisible(true);
        frame.setSize(900, 700);
        stackgraph.init();
        // stackgraph.start();
    }

    public static void main(String[] args) {
        // main method
    }
}
