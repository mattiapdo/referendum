package loadData;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.BytesRef;

import it.stilo.g.structures.WeightedUndirectedGraph;
import net.seninp.jmotif.sax.SAXException;

public class TweetsAnalysis {
	
	public static void scanDocuments(IndexReader ir, Fazione Y) throws NumberFormatException, IOException {
		 
		System.out.println("Number of documents: " + ir.numDocs());
	     // iterate all documents 
	        for (int i=0; i<ir.maxDoc(); i++) {
	            
	             Terms terms = ir.getTermVector(i, "text");  
	             TermsEnum iterator = terms.iterator(null); 
	             BytesRef term; String termText; 
	             @SuppressWarnings("unused")
	             Long termFreq; 
	             Document doc = ir.document(i);
	             String name = doc.get("UserSN"); 
	             
	             // if the user corresponding to the document is of interest 
	             if(Y.getBosses().contains(name)) {
	            	 
	            	 Y.addDoc(i);	// aggiungi il doc a quelli dei boss
	            	 Y.addUser(doc.get("userid"));

	                // iterate all the terms in the document 
	                while((term = iterator.next())!=null) {

	                    termText = term.utf8ToString();
	                    termFreq = iterator.totalTermFreq();
	                    
	                    // se il token corrente non è tra quelli dei si
	                    if(! Y.getTermini().getTerms().containsKey(termText)) {
	                    	//.. aggiungilo
	                    	Y.getTermini().addTerm(termText, new Termine(termText));
	                    }
	                    //update frequency 
	                	Y.getTermini().getTerm(termText).incrementFreq(1L);  // frequencies è definito come un long ?!
	                    
	                    //update timestamp
	                	Y.getTermini().getTerm(termText).addTimestamp(Long.valueOf(doc.get("time"))); 
	                }   
	             }           
	        }
		}

	
	public static void main(String[] args) throws IOException, SAXException, ParseException {
		
		long startTime = System.currentTimeMillis();
		        
        Fazione Y = new Fazione("Y", ".//data//users.csv");
		
		// open a directory
		Directory dir = new SimpleFSDirectory(new File(".\\data\\lucene_index_r"));
		IndexReader ir = DirectoryReader.open(dir);            
        
			System.out.println("Scanning documents...");
        scanDocuments(ir, Y);
        
        	System.out.println("Number of bosses found in dataset " + Y.getUsers().size());
        	System.out.println("Sorting terms by frequency");
        Y.getTermini().sortByFreq();
        	System.out.println("Setting hashmap with top 1000 results");
        Y.getTermini().setTop(1000);
        	System.out.println("Top 1000 terms by frequency:\n" + Y.getTermini().getImportantTermsKeySet());
        	System.out.println("Setting top terms time series");
        Y.getTermini().setTopTermsTimeSeries(43200L);
        	System.out.println("Setting top terms SAX strings");
        Y.getTermini().setTopTermsSAXStrings(5, 0.1);
        	System.out.println("Saving SAX string into file");
        //Y.getTermini().getSAXStringsIntoFile("./data/SAXStrings.csv"); se non serve non chiamarlo.. impiega parecchio tempo
        
        String [] allWords = Y.getTermini().getParolaMostImp();
        Y.getTermini().setSaxMostImp(); // edited
        char[][] saxStrings = Y.getTermini().getSaxMostImp();
        
        // K-means
        int k = 3;
        	System.out.println("\nRunning "+ k +"- means clustering on top 1000 words from " + Y.getIdea() + " bosses");
        int original_size = Y.getTermini().getImportantTerms().entrySet().iterator().next().getValue().getTimeSeriesLength(); // non troppo pulito qui perchè si assume che l'iteratore abbia un next()
        Kmeans kmeans = new Kmeans(saxStrings, k, 5, original_size, 1000, 0.01); // remove redundant int alphabetsize
        int[] yes_partition = kmeans.perform_clustering();
        
        Clusters clusters = new Clusters(yes_partition, allWords);
        	System.out.println("found ");clusters.getNumOfClusters();
        
        // to be repeated for all the clusters!   -> use clusters.getNumOfClusters()
        for(int i=0; i<1; i++) {
	        Cluster cluster = clusters.getCluster(0);
	        ArrayList<String> clusterWords = cluster.getWords();
	        String[] words = clusterWords.toArray(new String[0]);
	        	System.out.println("\t Cluster number "+ 0 + "contains:" + words.toString());
	        
	        	System.out.println("Creating co-occurrence graph on cluster " + 0 + "...");
	        Co_Occurence_Graph CoOcc = new Co_Occurence_Graph(words, Y.getDocs());
	        WeightedUndirectedGraph g = CoOcc.getGraph();
        }
        
        
        System.out.println("All done");
        
        
        System.out.println("Elapsed time: " + (long) (System.currentTimeMillis() - startTime));

	}
}
