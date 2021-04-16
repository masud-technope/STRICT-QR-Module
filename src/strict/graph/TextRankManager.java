package strict.graph;

import java.util.HashMap;
import org.jgraph.graph.DefaultEdge;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import strict.query.QueryToken;
import strict.utility.MiscUtility;

public class TextRankManager {

	DirectedGraph<String, DefaultEdge> textGraph;
	SimpleDirectedWeightedGraph<String, DefaultWeightedEdge> wtextGraph;
	HashMap<String, QueryToken> tokendb;

	public TextRankManager(DirectedGraph<String, DefaultEdge> textGraph, HashMap<String, QueryToken> tokendb) {
		this.textGraph = textGraph;
		this.tokendb = tokendb;
	}

	public TextRankManager(SimpleDirectedWeightedGraph<String, DefaultWeightedEdge> wtextGraph,
			HashMap<String, QueryToken> tokendb) {
		this.wtextGraph = wtextGraph;
		this.tokendb = tokendb;
	}

	protected HashMap<String, QueryToken> calculateTextRankScores() {
		// collect query token scores
		TextRankProvider trProvider = new TextRankProvider(this.textGraph, tokendb);
		return trProvider.calculateTextRank();
	}

	public HashMap<String, QueryToken> getTextRank() {
		HashMap<String, QueryToken> textRankMap = new HashMap<>();
		textRankMap = calculateTextRankScores();
		ScoreFilterManager filter = new ScoreFilterManager(textRankMap, "TR");
		return filter.applyFilters();
	}

}
