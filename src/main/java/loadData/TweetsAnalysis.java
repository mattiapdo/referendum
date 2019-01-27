package loadData;

import java.io.File;
import java.io.IOException;


import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.BytesRef;

import net.seninp.jmotif.sax.SAXException;

public class TweetsAnalysis {
	
	public static void scanDocuments(IndexReader ir, Fazione Y) throws NumberFormatException, IOException {
		 
		System.out.println("Number of documents: " + ir.numDocs());
	     // iterate all documents 
	        for (int i=0; i<ir.maxDoc(); i++) {
	            
	             Terms terms = ir.getTermVector(i, "text");  
	             TermsEnum iterator = terms.iterator(null); 
	             BytesRef term; String termText; Long termFreq; 
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

	public static void main(String[] args) throws IOException, SAXException {
		        
        Fazione Y = new Fazione("Y", ".//data//users.csv");
		System.out.println("number of yes supporters: " + Y.getUsers().size());
		
		// open a directory
		Directory dir = new SimpleFSDirectory(new File(".\\data\\lucene_index_r"));
		IndexReader ir = DirectoryReader.open(dir);            
        
		System.out.println("Scanning documents...");
        scanDocuments(ir, Y);
        
        System.out.println("Sorting terms by frequency");
        Y.getTermini().sortByFreq();
        System.out.println("Setting hashmap with top 1000 results");
        Y.getTermini().setTop(1000);
        System.out.println("Top 1000 terms by frequency:\n" + Y.getTermini().getImportantTerms());
        System.out.println("Setting top terms time series");
        Y.getTermini().setTopTermsTimeSeries(43200L);
        System.out.println("Setting top terms SAX strings");
        Y.getTermini().setTopTermsSAXStrings(2, 0.01);
        System.out.println("Saving SAX string into file");
        Y.getTermini().getSAXStringIntoFile("./data/SAXStrings.csv");
        
        System.out.println("All done");

	}
}
