package strict.graph;

import java.util.HashMap;
import java.util.Set;

import org.jgraph.graph.DefaultEdge;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import proposed.query.QueryToken;
import config.StaticData;
import utility.MyItemSorter;

public class TokenRankProvider {
	public SimpleDirectedWeightedGraph<String, DefaultWeightedEdge> wgraph;
	public DirectedGraph<String, DefaultEdge> graph;
	HashMap<String, QueryToken> tokendb;
	HashMap<String, Double> oldScoreMap;
	HashMap<String, Double> newScoreMap;
	boolean isPOS = false;
	final double EDGE_WEIGHT_TH = 0.25;
	double INITIAL_VERTEX_SCORE = StaticData.INITIAL_TERM_WEIGHT;
	double DAMPING_FACTOR = StaticData.DAMPING_FACTOR;
	final int MAX_ITERATION = 100;

	HashMap<String, Double> initializerMap;
	boolean customIniit = false;

	public TokenRankProvider(SimpleDirectedWeightedGraph<String, DefaultWeightedEdge> wgraph,
			HashMap<String, QueryToken> tokendb, boolean isPOS) {
		// initialization of different objects
		// weighted graph constructor
		this.wgraph = wgraph;
		this.tokendb = tokendb;
		this.oldScoreMap = new HashMap<>();
		this.newScoreMap = new HashMap<>();
		this.isPOS = isPOS;
	}

	public TokenRankProvider(DirectedGraph<String, DefaultEdge> graph, HashMap<String, QueryToken> tokendb,
			boolean isPOS) {
		// un-weighted graph constructor
		this.graph = graph;
		this.tokendb = tokendb;
		this.oldScoreMap = new HashMap<>();
		this.newScoreMap = new HashMap<>();
		this.isPOS = isPOS;
	}

	public TokenRankProvider(DirectedGraph<String, DefaultEdge> graph, HashMap<String, QueryToken> tokendb,
			boolean isPOS, HashMap<String, Double> initializerMap) {
		// un-weighted graph constructor
		this.graph = graph;
		this.tokendb = tokendb;
		this.oldScoreMap = new HashMap<>();
		this.newScoreMap = new HashMap<>();
		this.isPOS = isPOS;
		this.initializerMap = initializerMap;
		this.customIniit = true;
	}

	boolean checkSignificantDiff(double oldV, double newV) {
		double diff = 0;
		if (newV > oldV)
			diff = newV - oldV;
		else
			diff = oldV - newV;
		return diff > StaticData.SIGNIFICANCE_THRESHOLD ? true : false;
	}

	public HashMap<String, QueryToken> calculateTokenRankWeighted() {
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

	protected void initializeWithTFIDF() {
		for (String vertex : graph.vertexSet()) {
			if (this.initializerMap.containsKey(vertex)) {
				oldScoreMap.put(vertex, this.initializerMap.get(vertex));
				newScoreMap.put(vertex, this.initializerMap.get(vertex));
			} else if (this.initializerMap.containsKey(vertex.toLowerCase())) {
				oldScoreMap.put(vertex, this.initializerMap.get(vertex.toLowerCase()));
				newScoreMap.put(vertex, this.initializerMap.get(vertex.toLowerCase()));
			} else {
				oldScoreMap.put(vertex, this.INITIAL_VERTEX_SCORE);
				newScoreMap.put(vertex, this.INITIAL_VERTEX_SCORE);
			}
		}
	}

	public HashMap<String, QueryToken> calculateTokenRank() {
		// calculating token rank score
		double d = this.DAMPING_FACTOR;
		double N = graph.vertexSet().size();
		// initially putting 1 to all
		if (customIniit) {
			this.initializeWithTFIDF();
		} else {
			this.initializeGraphBasic();
		}

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

					// score and out degree should be affected by the edge weight
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
					newScoreMap.put(vertex, trank);
					insignificant++;
				}
			}

			itercount++;

			if (insignificant == graph.vertexSet().size())
				enoughIteration = true;
			if (itercount == this.MAX_ITERATION)
				enoughIteration = true;

			// coping values to new Hash Map
			for (String key : newScoreMap.keySet()) {
				oldScoreMap.put(key, newScoreMap.get(key));
			}

		}

		// saving token ranks into tokendb
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
			if (!isPOS)
				qtoken.tokenRankScore = score;
			else
				qtoken.posRankScore = score;
			tokendb.put(key, qtoken);
		}
	}

	protected void showTokenRanks() {
		// showing token ranks
		for (String key : this.tokendb.keySet()) {
			System.out.println(key + " " + tokendb.get(key).tokenRankScore);
		}
	}
}
