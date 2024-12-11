package ca.usask.cs.srlab.strict.graph;

import java.util.HashMap;

import org.jgraph.graph.DefaultEdge;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import ca.usask.cs.srlab.strict.query.QueryToken;

public class POSRankManager {

    public SimpleDirectedWeightedGraph<String, DefaultWeightedEdge> wgraph;
    public DirectedGraph<String, DefaultEdge> graph;
    HashMap<String, QueryToken> tokendb;

    public POSRankManager(DirectedGraph<String, DefaultEdge> posGraph, HashMap<String, QueryToken> tokendb) {
        this.graph = posGraph;
        this.tokendb = tokendb;
    }

    protected HashMap<String, QueryToken> calculatePOSRankScores() {
        POSRankProvider prProvider = new POSRankProvider(this.graph, tokendb);
        return prProvider.calculatePOSRank();
    }

    public HashMap<String, QueryToken> getPOSRank() {
        HashMap<String, QueryToken> posRankMap = calculatePOSRankScores();
        ScoreFilterManager manager = new ScoreFilterManager(posRankMap, "PR");
        return manager.applyFilters();
    }
}
