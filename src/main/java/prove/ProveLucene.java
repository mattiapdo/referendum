package prove;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.DocsEnum;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;

public class ProveLucene {

	public static void main(String[] args) throws ParseException, IOException {
		Directory dir = new SimpleFSDirectory(new File(".\\data\\lucene_index"));
        //QueryParser parser = new QueryParser(Version.LUCENE_41, "text", new StandardAnalyzer(LUCENE_41)); 
        IndexReader reader = DirectoryReader.open(dir);
        IndexSearcher searcher = new IndexSearcher(reader);
        Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_41);
       
 	   	String mywords = "referendum italia iovotosi e si la";
 	   	System.out.println("mywords: " + mywords );
 	  
 	   	String mybosses = String.join(" ", "matteorenzi orfini senatoripd");
 	   	System.out.println("mybosses: " + mybosses);
 	   	
 	   	String queryString = "(text:" + mywords + ") AND (UserSN:" + mybosses + ")";
 	   	QueryParser parser = new QueryParser(Version.LUCENE_41, "UserSN", analyzer);
 	   	Query q = parser.parse(queryString); 
 	   	
 	   	// find all documents written by the bosses that contains at least one of the words in the cluster
 	   	TopDocs docs = searcher.search(q, reader.numDocs());
 	   	ScoreDoc[] hits = docs.scoreDocs;
 	   int docID;
 	   	
 	   System.out.println("found " + hits.length + " documents");
 	   
 	  
 	   // get terms vectors for one document and one field
 	  for(ScoreDoc sc:hits) {
		   	
		  docID = sc.doc;
		  Terms terms = reader.getTermVector(docID, "text"); 

	 	  if (terms != null && terms.size() > 0) {
	 	      // access the terms for this field
	 	      TermsEnum termsEnum = terms.iterator(null); 
	 	      BytesRef term = null;
	
	 	      // explore the terms for this field
	 	      while ((term = termsEnum.next()) != null) {
	 	          // enumerate through documents, in this case only one
	 	          DocsEnum docsEnum = termsEnum.docs(null, null); 
	 	          int docIdEnum;
	 	          while ((docIdEnum = docsEnum.nextDoc()) != DocIdSetIterator.NO_MORE_DOCS) {
	 	              // get the term frequency in the document 
	 	              System.out.println(term.utf8ToString()+ " " + docIdEnum + " " + docsEnum.freq()); 
	 	          }
	 	      }
	 	  }
 	  }
	}

}
