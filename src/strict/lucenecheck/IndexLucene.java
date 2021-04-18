package strict.lucenecheck;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import strict.ca.usask.cs.srlab.strict.config.StaticData;

public class IndexLucene {

	String repoName;
	String index;
	String docs;
	public int totalIndexed = 0;

	public IndexLucene(String repoName) {
		// initialization
		this.index = StaticData.HOME_DIR + "/Lucene/index-method/";
		this.docs = StaticData.HOME_DIR + "/Corpus/";
		this.makeIndexFolder(repoName);
		System.out.println("Index:" + this.index);
		System.out.println("Docs:" + this.docs);
	}

	public IndexLucene(String indexFolder, String docsFolder) {
		this.index = indexFolder;
		this.docs = docsFolder;
		this.makeIndexFolder(indexFolder);
	}

	protected void makeIndexFolder(String indexFolder) {
		File indexDir = new File(indexFolder);
		if (!indexDir.exists()) {
			indexDir.mkdir();
		}
	}

	public void indexCorpusFiles() {
		// index the files
		try {
			Directory dir = FSDirectory.open(new File(index).toPath());
			Analyzer analyzer = new StandardAnalyzer();
			// Analyzer analyzer=new EnglishAnalyzer(Version.LUCENE_44);
			IndexWriterConfig config = new IndexWriterConfig(analyzer);
			IndexWriter writer = new IndexWriter(dir, config);
			indexDocs(writer, new File(this.docs));
			writer.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected void clearIndexFiles() {
		// clearing index files
		File[] files = new File(this.index).listFiles();
		for (File f : files) {
			f.delete();
		}
		System.out.println("Index cleared successfully.");
	}

	protected void indexDocs(IndexWriter writer, File file) {
		// writing to the index file
		if (file.canRead()) {
			if (file.isDirectory()) {
				String[] files = file.list();
				// an IO error could occur
				if (files != null) {
					for (int i = 0; i < files.length; i++) {
						indexDocs(writer, new File(file, files[i]));
					}
				}
			} else {
				FileInputStream fis;
				try {
					fis = new FileInputStream(file);
				} catch (FileNotFoundException fnfe) {
					return;
				}
				try {
					// make a new, empty document
					Document doc = new Document();
					Field pathField = new StringField("path", file.getPath(), Field.Store.YES);
					doc.add(pathField);
					doc.add(new TextField("contents", new BufferedReader(new InputStreamReader(fis, "UTF-8"))));
					// System.out.println("adding " + file);
					totalIndexed++;
					writer.addDocument(doc);
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (CorruptIndexException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					try {
						fis.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}
}
