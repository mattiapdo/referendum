package loadData;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.SimpleFSDirectory;

public class Read {
	
	public static void main(String [] args) throws IOException {
		
		// directory.open(indexPath)
		Directory dir = new SimpleFSDirectory(new File("./data/lucene_index"));
		// open a directory
		IndexReader ir = DirectoryReader.open(dir);
		// create index searcher
		IndexSearcher searcher = new IndexSearcher(ir);	
		
		// perform queries
		
		//Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_41);
		
		// define the query
		Query q = new TermQuery(new Term("UserSN","matteorenzi"));
		
		// optional: query parsing
		//QueryParser parser = new QueryParser(Version.LUCENE_41, "Name", analyzer);
		
		// this method in the IndexSearcher object searches for the to 10 matches for the query q
		System.out.println("Searching for query:    " + q.toString() + "\n\n");
		TopDocs top = searcher.search(q, 10); // Finds the top n hits for query.
		
		// the top instance of the TopDocs object has an attribute .scoreDocs ..  it is an array of type ScoreDoc
		ScoreDoc[] hits = top.scoreDocs;
		
		Document doc = null;
		
		// for each element in the top n list, show the result
		for(ScoreDoc sc : hits) {
			
			doc = searcher.doc(sc.doc);
			
			System.out.println("UserSN :=  "+ doc.get("UserSN"));
			System.out.println("userid :=  "+ doc.get("userid"));
			System.out.println("text :=  "+ doc.get("text"));
			System.out.println("time :=  "+ doc.get("time"));
			//System.out.println("time :=  "+ String.valueOf(doc.get("time")));


		}
		
		// close index reader
		ir.close();
	}

}
