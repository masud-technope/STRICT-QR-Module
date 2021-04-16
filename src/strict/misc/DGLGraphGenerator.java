package strict.misc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.jgraph.graph.DefaultEdge;
import org.jgrapht.DirectedGraph;
import qd.utility.ContentLoader;
import strict.graph.WordNetworkMaker;
import strict.query.QTextCollector;

public class DGLGraphGenerator {

	String title;
	String crFile;
	String goldKeywordFile;

	public DGLGraphGenerator(String title, String crFile, String gkFile) {
		this.title = title;
		this.crFile = crFile;
		this.goldKeywordFile = gkFile;
	}

	protected ArrayList<String> extractSentences() {
		String content = ContentLoader.loadFileContent(this.crFile);
		QTextCollector qtcoll = new QTextCollector(title, content);
		return qtcoll.collectQuerySentencesV3();
	}

	protected HashMap<String, Integer> convert2Number(HashSet<String> words) {
		int number = 0;
		HashMap<String, Integer> wordIDMap = new HashMap<>();
		for (String word : words) {
			wordIDMap.put(word, number++);
		}
		return wordIDMap;
	}

	protected void showKeys(HashMap<String, Integer> wordIDMap) {
		for (String word : wordIDMap.keySet()) {
			System.out.println(word + "\t" + wordIDMap.get(word));
		}
	}

	protected void showAdjacenyMatrix(DirectedGraph<String, DefaultEdge> textGraph,
			HashMap<String, Integer> wordIDMap) {
		Set<DefaultEdge> edges = textGraph.edgeSet();
		ArrayList<Integer> srcIDs = new ArrayList<>();
		ArrayList<Integer> destIDs = new ArrayList<>();

		for (DefaultEdge edge : edges) {
			String source = textGraph.getEdgeSource(edge);
			String destination = textGraph.getEdgeTarget(edge);
			int sourceIndex = wordIDMap.get(source);
			srcIDs.add(sourceIndex);
			int destIndex = wordIDMap.get(destination);
			destIDs.add(destIndex);
		}
		System.out.println(srcIDs);
		System.out.println(destIDs);
	}

	protected void constructTokenLabels(HashMap<String, Integer> wordIDMap) {
		ArrayList<String> tokens = ContentLoader.getAllTokens(this.goldKeywordFile);
		ArrayList<Integer> labels=new ArrayList<>();
		for(String token: wordIDMap.keySet()) {
			int label=0;
			if(tokens.contains(token)) {
				label=1;
			}
			labels.add(label);
			//System.out.println(token+"\t"+label);
		}
		System.out.println(labels);
	}

	protected void extractTextGraph() {
		ArrayList<String> sentences = extractSentences();
		WordNetworkMaker wnMaker = new WordNetworkMaker(sentences);
		DirectedGraph<String, DefaultEdge> textGraph = wnMaker.createWordNetwork();
		HashSet<String> vertices = new HashSet<>(textGraph.vertexSet());
		HashMap<String, Integer> wordIDMap = convert2Number(vertices);
		showAdjacenyMatrix(textGraph, wordIDMap);
		constructTokenLabels(wordIDMap);
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String title = "";
		String crFile = "./sample/13834.txt";
		String gkFile = "./sample/13834-gold.txt";
		new DGLGraphGenerator(title, crFile, gkFile).extractTextGraph();
	}
}
