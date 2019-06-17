package strict.graph;

import java.util.HashMap;
import java.util.Set;

import org.jgraph.graph.DefaultEdge;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import strict.ca.usask.cs.srlab.strict.config.StaticData;
import strict.query.QueryToken;

public class POSRankProvider {

	public SimpleDirectedWeightedGraph<String, DefaultWeightedEdge> wgraph;
	public DirectedGraph<String, DefaultEdge> graph;
	HashMap<String, QueryToken> tokendb;
	HashMap<String, Double> oldScoreMap;
	HashMap<String, Double> newScoreMap;
	final double EDGE_WEIGHT_TH = 0.25;
	final double INITIAL_VERTEX_SCORE = StaticData.INITIAL_TERM_WEIGHT;
	final double DAMPING_FACTOR = 0.85;
	final int MAX_ITERATION = 100;

	HashMap<String, Double> initializerMap;
	boolean customIniit = false;

	public POSRankProvider(SimpleDirectedWeightedGraph<String, DefaultWeightedEdge> wgraph,
			HashMap<String, QueryToken> tokendb) {
		// initialization of different objects
		// weighted graph constructor
		this.wgraph = wgraph;
		this.tokendb = tokendb;
		this.oldScoreMap = new HashMap<>();
		this.newScoreMap = new HashMap<>();
	}
	
	public POSRankProvider(DirectedGraph<String, DefaultEdge> graph, HashMap<String, QueryToken> tokendb) {
		this.graph = graph;
		this.tokendb = tokendb;
		this.oldScoreMap = new HashMap<>();
		this.newScoreMap = new HashMap<>();
	}
	
	boolean checkSignificantDiff(double oldV, double newV) {
		double diff = 0;
		if (newV > oldV)
			diff = newV - oldV;
		else
			diff = oldV - newV;
		return diff > StaticData.SIGNIFICANCE_THRESHOLD ? true : false;
	}

	public HashMap<String, QueryToken> calculatePOSRankWeighted() {
		// calculating token rank score
		double d = this.DAMPING_FACTOR;
		double N = wgraph.vertexSet().size();
		// initially putting 1 to all
		for (String vertex : wgraph.vertexSet()) {
			oldScoreMap.put(vertex, this.INITIAL_VERTEX_SCORE);
			newScoreMap.put(vertex, this.INITIAL_VERTEX_SCORE);
		}
		boolean enoughIteration = false;
		int itercount = 0;

		while (!enoughIteration) {
			int insignificant = 0;
			for (String vertex : wgraph.vertexSet()) {
				Set<DefaultWeightedEdge> incomings = wgraph.incomingEdgesOf(vertex);
				// now calculate the PR score
				double trank = (1 - d);
				double comingScore = 0;
				for (DefaultWeightedEdge edge : incomings) {
					String source1 = wgraph.getEdgeSource(edge);
					int outdegree = wgraph.outDegreeOf(source1);
					// score and out degree should be affected by the edge
					// weight
					double score = oldScoreMap.get(source1);
					// adding edge weight
					double edgeWeight = wgraph.getEdgeWeight(edge);
					edgeWeight = 1; // by default 1
					if (outdegree == 0) {
						comingScore += score;
					} else {
						comingScore += ((score / outdegree) * edgeWeight);
					}
				}
				comingScore = comingScore * d;
				trank += comingScore;
				boolean significant = checkSignificantDiff(oldScoreMap.get(vertex).doubleValue(), trank);
				if (significant) {
					newScoreMap.put(vertex, trank);
				} else {
					insignificant++;
				}
			}
			// coping values to new Hash Map
			for (String key : newScoreMap.keySet()) {
				oldScoreMap.put(key, newScoreMap.get(key));
			}
			itercount++;
			if (insignificant == wgraph.vertexSet().size())
				enoughIteration = true;
			if (itercount == MAX_ITERATION)
				enoughIteration = true;
		}
		// saving token ranks
		recordNormalizeScores();
		// sort the token rank scores
		// this.tokendb = MyItemSorter.sortItemMap(this.tokendb);
		// showing token rank scores
		// showTokenRanks();
		return this.tokendb;
	}

	protected void initializeGraphBasic() {
		// initially putting 1 to all
		for (String vertex : graph.vertexSet()) {
			oldScoreMap.put(vertex, this.INITIAL_VERTEX_SCORE);
			newScoreMap.put(vertex, this.INITIAL_VERTEX_SCORE);
		}
	}

	public HashMap<String, QueryToken> calculatePOSRank() {
		// calculating token rank score
		double d = this.DAMPING_FACTOR;
		double N = graph.vertexSet().size();
		// initially putting 1 to all
		
		this.initializeGraphBasic();

		boolean enoughIteration = false;
		int itercount = 0;

		while (!enoughIteration) {
			int insignificant = 0;
			for (String vertex : graph.vertexSet()) {
				Set<DefaultEdge> incomings = graph.incomingEdgesOf(vertex);
				// now calculate the PR score
				double trank = (1 - d);
				double comingScore = 0;
				for (DefaultEdge edge : incomings) {
					String source1 = graph.getEdgeSource(edge);
					int outdegree = graph.outDegreeOf(source1);

					// score and out degree should be affected by the edge
					// weight
					double score = oldScoreMap.get(source1);
					// score=score*this.EDGE_WEIGHT_TH;

					if (outdegree == 1)
						comingScore += score;
					else if (outdegree > 1)
						comingScore += (score / outdegree);
				}
				comingScore = comingScore * d;
				trank += comingScore;
				boolean significant = checkSignificantDiff(oldScoreMap.get(vertex).doubleValue(), trank);
				if (significant) {
					newScoreMap.put(vertex, trank);
				} else {
					insignificant++;
				}
			}
			// coping values to new Hash Map
			for (String key : newScoreMap.keySet()) {
				oldScoreMap.put(key, newScoreMap.get(key));
			}
			itercount++;
			if (insignificant == graph.vertexSet().size())
				enoughIteration = true;
			if (itercount == this.MAX_ITERATION)
				enoughIteration = true;
		}
		
		recordNormalizeScores();
		// sort the token rank scores
		// this.tokendb = MyItemSorter.sortItemMap(this.tokendb);
		// showing token rank scores
		// showTokenRanks();
		return this.tokendb;
	}

	protected void recordNormalizeScores() {
		// record normalized scores
		double maxRank = 0;
		for (String key : newScoreMap.keySet()) {
			double score = newScoreMap.get(key).doubleValue();
			if (score > maxRank) {
				maxRank = score;
			}
		}
		for (String key : newScoreMap.keySet()) {
			double score = newScoreMap.get(key).doubleValue();
			score = score / maxRank;
			// this.newScoreMap.put(key, score);
			QueryToken qtoken = tokendb.get(key);
			qtoken.posRankScore = score;
			tokendb.put(key, qtoken);
		}
	}

	protected void showTokenRanks() {
		// showing token ranks
		for (String key : this.tokendb.keySet()) {
			System.out.println(key + " " + tokendb.get(key).posRankScore);
		}
	}


	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
}
