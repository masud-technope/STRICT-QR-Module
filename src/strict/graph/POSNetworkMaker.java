package strict.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.jgraph.graph.DefaultEdge;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import strict.ca.usask.cs.srlab.strict.config.StaticData;
import strict.query.QueryToken;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

public class POSNetworkMaker {

	ArrayList<String> sentences;
	public SimpleDirectedWeightedGraph<String, DefaultWeightedEdge> wgraph;
	public DirectedGraph<String, DefaultEdge> graph;
	HashMap<String, QueryToken> tokendb;
	final int WINDOW_SIZE = 2;
	static MaxentTagger tagger = null;
	HashMap<String, Integer> coocCountMap;

	public POSNetworkMaker(ArrayList<String> sentences) {
		// initialization of items
		this.sentences = sentences;
		this.wgraph = new SimpleDirectedWeightedGraph<>(
				DefaultWeightedEdge.class);
		this.graph = new DefaultDirectedGraph<>(DefaultEdge.class);
		this.tokendb = new HashMap<>();
		// this.adjacent = new AdjacencyScoreProvider(sentences);
		// this.adjacent.collectAdjacentTermsLocal();
		if (tagger == null) {
			tagger = new MaxentTagger(
					StaticData.MAX_ENT_MODELS_DIR+"/english-left3words-distsim.tagger");
		}
		this.coocCountMap = new HashMap<>();
	}

	public POSNetworkMaker(ArrayList<String> sentences,
			HashMap<String, ArrayList<String>> alltermMap) {
		// initialization of items
		this.sentences = sentences;
		this.wgraph = new SimpleDirectedWeightedGraph<>(
				DefaultWeightedEdge.class);
		this.graph = new DefaultDirectedGraph<>(DefaultEdge.class);
		this.tokendb = new HashMap<>();
		// this.adjacent = new AdjacencyScoreProvider(sentences, alltermMap);
		if (tagger == null)
			tagger = new MaxentTagger(
					"./models/english-left3words-distsim.tagger");
		this.coocCountMap = new HashMap<>();
	}

	protected void setEdgeWeight() {
		int maxFreq = 0;
		for (String keypair : this.coocCountMap.keySet()) {
			int cooc = this.coocCountMap.get(keypair);
			if (cooc > maxFreq) {
				maxFreq = cooc;
			}
		}
		Set<DefaultWeightedEdge> edges = this.wgraph.edgeSet();
		for (DefaultWeightedEdge edge : edges) {
			String source = wgraph.getEdgeSource(edge);
			String dest = wgraph.getEdgeTarget(edge);
			String keypair = source + "-" + dest;
			if (coocCountMap.containsKey(keypair)) {
				double normWeight = (double) coocCountMap.get(keypair)
						/ maxFreq;
				this.wgraph.setEdgeWeight(edge, normWeight);
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

	protected void addSelfLinks(DirectedGraph<String, DefaultEdge> graph,
			ArrayList<String> nodes) {
		// create the self-linking using the nodes
		for (int index = 0; index < nodes.size(); index++) {
			String previousToken = new String();
			String nextToken = new String();
			String currentToken = nodes.get(index);
			if (index > 0)
				previousToken = nodes.get(index - 1);
			if (index < nodes.size() - 1)
				nextToken = nodes.get(index + 1);
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

	protected void addSelfLinks(
			SimpleDirectedWeightedGraph<String, DefaultWeightedEdge> wgraph,
			ArrayList<String> nodes) {
		// create the self-linking using the nodes
		for (int index = 0; index < nodes.size(); index++) {
			String previousToken = new String();
			String nextToken = new String();
			String currentToken = nodes.get(index);
			if (index > 0)
				previousToken = nodes.get(index - 1);
			if (index < nodes.size() - 1)
				nextToken = nodes.get(index + 1);
			// adding edges to the graph
			if (!previousToken.isEmpty() && !currentToken.equals(previousToken))
				if (!wgraph.containsEdge(currentToken, previousToken)) {
					wgraph.addEdge(currentToken, previousToken);
					updateCooccCount(currentToken, previousToken);
				}
			if (!nextToken.isEmpty() && !currentToken.equals(nextToken))
				if (!wgraph.containsEdge(currentToken, nextToken)) {
					wgraph.addEdge(currentToken, nextToken);
					updateCooccCount(currentToken, nextToken);
				}
		}
	}

	protected void addCrossLinks(DirectedGraph<String, DefaultEdge> graph,
			ArrayList<String> srcNodes, ArrayList<String> destNodes) {
		// adding cross-links
		for (String srcNode : srcNodes) {
			for (String destNode : destNodes) {
				if (!graph.containsEdge(srcNode, destNode)) {
					graph.addEdge(srcNode, destNode);
				}
			}
		}
	}

	protected void addCrossLinks(
			SimpleDirectedWeightedGraph<String, DefaultWeightedEdge> wgraph,
			ArrayList<String> srcNodes, ArrayList<String> destNodes) {
		// adding cross-links
		for (String srcNode : srcNodes) {
			for (String destNode : destNodes) {
				if (!wgraph.containsEdge(srcNode, destNode)
						&& !srcNode.equals(destNode)) {
					wgraph.addEdge(srcNode, destNode);
					updateCooccCount(srcNode, destNode);
				}
			}
		}
	}

	public DirectedGraph<String, DefaultEdge> createPOSNetwork() {
		// developing the word network
		for (String sentence : this.sentences) {
			HashMap<String, HashSet<String>> posmap = tagSingleQuery(sentence);
			// System.out.println(posmap);
			// adding the vertices
			for (String pos : posmap.keySet()) {
				HashSet<String> vertices = posmap.get(pos);
				for (String vertice : vertices) {
					graph.addVertex(vertice);
				}
			}
			// now adding the edges
			ArrayList<String> nouns = new ArrayList<>();
			ArrayList<String> verbs = new ArrayList<>();
			ArrayList<String> adjectives = new ArrayList<>();
			ArrayList<String> adverbs = new ArrayList<>();

			for (String pos : posmap.keySet()) {
				if (pos.contains("NN")) {
					nouns.addAll(posmap.get(pos));
				} else if (pos.contains("VB")) {
					verbs.addAll(posmap.get(pos));
				} else if (pos.contains("JJ")) {
					adjectives.addAll(posmap.get(pos));
				} else if (pos.contains("RB ")) {
					adverbs.addAll(posmap.get(pos));
				}
			}

			// NN: primary, VB/JJ: secondary and RB: tertiary
			addSelfLinks(graph, nouns);
			addSelfLinks(graph, verbs);
			// addSelfLinks(graph, adjectives);
			// addSelfLinks(graph, adverbs);

			// now add the cross links
			addCrossLinks(graph, verbs, nouns);
			addCrossLinks(graph, verbs, adjectives);
			addCrossLinks(graph, adverbs, verbs);

		}
		// System.out.println("Nodes:" + graph.vertexSet().size());
		// System.out.println("Edges:" + graph.edgeSet().size());
		// returning the created graph
		return graph;
	}

	public SimpleDirectedWeightedGraph<String, DefaultWeightedEdge> createWeightedPOSNetwork() {
		// developing the word network
		for (String sentence : this.sentences) {
			HashMap<String, HashSet<String>> posmap = tagSingleQuery(sentence);
			// System.out.println(posmap);
			// adding the vertices
			for (String pos : posmap.keySet()) {
				HashSet<String> vertices = posmap.get(pos);
				for (String vertice : vertices) {
					wgraph.addVertex(vertice);
				}
			}
			// now adding the edges
			ArrayList<String> nouns = new ArrayList<>();
			ArrayList<String> verbs = new ArrayList<>();
			ArrayList<String> adjectives = new ArrayList<>();
			ArrayList<String> adverbs = new ArrayList<>();

			for (String pos : posmap.keySet()) {
				if (pos.contains("NN")) {
					nouns.addAll(posmap.get(pos));
				} else if (pos.contains("VB")) {
					verbs.addAll(posmap.get(pos));
				} else if (pos.contains("JJ")) {
					adjectives.addAll(posmap.get(pos));
				} else if (pos.contains("RB ")) {
					adverbs.addAll(posmap.get(pos));
				}
			}

			// NN: primary, VB/JJ: secondary and RB: tertiary
			addSelfLinks(wgraph, nouns);
			addSelfLinks(wgraph, verbs);
			// addSelfLinks(graph, adjectives);
			// addSelfLinks(graph, adverbs);

			// now add the cross links
			addCrossLinks(wgraph, verbs, nouns);
			addCrossLinks(wgraph, verbs, adjectives);
			addCrossLinks(wgraph, adverbs, verbs);
		}

		// adding the weight
		this.setEdgeWeight();

		// returning the created graph
		return wgraph;
	}

	protected HashMap<String, HashSet<String>> tagSingleQuery(String sentence) {
		// tag a single query
		String tagged = tagger.tagString(sentence.trim());
		String[] words = tagged.split("\\s+");
		HashMap<String, HashSet<String>> posmap = new HashMap<>();
		for (String word : words) {
			String[] parts = word.split("_");
			if (parts.length < 2)
				continue; // avoid untagged parts
			String pos = parts[1].trim();
			if (posmap.containsKey(pos)) {
				HashSet<String> tokens = posmap.get(pos);
				tokens.add(parts[0].trim());
				posmap.put(pos, tokens);
			} else {
				HashSet<String> tokens = new HashSet<>();
				tokens.add(parts[0].trim());
				posmap.put(pos, tokens);
			}
		}
		return posmap;
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

	protected void showEdges() {
		// show the graph edges
		for (String key : graph.vertexSet()) {
			Set<DefaultEdge> edges = graph.edgesOf(key);
			System.out.println(key + "\t" + edges.size());
		}
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

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ArrayList<String> sentences = new ArrayList<>();
		sentences
				.add("Closing the ECF Buddy List view cause chat room disconnect");
		sentences
				.add("If you close the ECF Buddy List view, and then try and type in a chat room you were connected to the following errors are thrown:");
		POSNetworkMaker maker = new POSNetworkMaker(sentences);
		DirectedGraph<String, DefaultEdge> graph = maker.createPOSNetwork();
		// System.out.println(graph);
		maker.showEdges();

	}
}
