package strict.graph;

import java.util.ArrayList;
import java.util.HashMap;
import org.jgraph.graph.DefaultEdge;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import strict.query.QueryToken;

public class GraphUtility {

	public static HashMap<String, QueryToken> initializeTokensDB(DirectedGraph<String, DefaultEdge> myGraph) {
		HashMap<String, QueryToken> tokendb = new HashMap<>();
		for (String node : myGraph.vertexSet()) {
			QueryToken qtoken = new QueryToken();
			qtoken.token = node;
			tokendb.put(node, qtoken);
		}
		return tokendb;
	}

	public static HashMap<String, QueryToken> initializeTokensDB(
			SimpleDirectedWeightedGraph<String, DefaultWeightedEdge> myGraph) {
		HashMap<String, QueryToken> tokendb = new HashMap<>();
		for (String node : myGraph.vertexSet()) {
			QueryToken qtoken = new QueryToken();
			qtoken.token = node;
			tokendb.put(node, qtoken);
		}
		return tokendb;
	}

	public static DirectedGraph<String, DefaultEdge> getWordNetwork(ArrayList<String> sentences) {
		WordNetworkMaker wnMaker = new WordNetworkMaker(sentences);
		return wnMaker.createWordNetwork();
	}

	public static SimpleDirectedWeightedGraph<String, DefaultWeightedEdge> getWeightedWordNetwork(
			ArrayList<String> sentences) {
		WordNetworkMaker wnMaker = new WordNetworkMaker(sentences);
		return wnMaker.createWeightedWordNetwork();
	}

	public static DirectedGraph<String, DefaultEdge> getPOSNetwork(ArrayList<String> sentences) {
		POSNetworkMaker pnMaker = new POSNetworkMaker(sentences);
		return pnMaker.createPOSNetwork();
	}

	public static SimpleDirectedWeightedGraph<String, DefaultWeightedEdge> getWeightedPOSNetwork(
			ArrayList<String> sentences) {
		POSNetworkMaker wnMaker = new POSNetworkMaker(sentences);
		return wnMaker.createWeightedPOSNetwork();
	}
}
