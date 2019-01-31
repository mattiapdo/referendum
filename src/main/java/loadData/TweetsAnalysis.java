package loadData;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map.Entry;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;

import com.opencsv.CSVReader;

import it.stilo.g.structures.WeightedUndirectedGraph;
import net.seninp.jmotif.sax.SAXException;

public class TweetsAnalysis {
	
	public static char[][] charMatrixFromFile(String path, int nRows, int nCols) throws IOException {
		char[][] matrix = new char[nRows][nCols];
		
		Reader reader = Files.newBufferedReader(Paths.get(path));
        CSVReader csvReader = new CSVReader(reader);
	
		// Reading Records One by One in a String array
	    String[] nextRecord;
	    int i = 0;
	    while ((nextRecord = csvReader.readNext()) != null) {
	    	for(int j = 0; j<nextRecord.length; i++) {
	    		for (int lett=0; lett<nextRecord[j].length(); lett++) {
	    			matrix[i++][j+lett] =  nextRecord[j].charAt(lett); //.toCharArray();
	    		}
	    		
	    	}
		    
	    }
	    return matrix;
		
	}

	public static void printMatrixofDouble(double[][] m){
	    try{
	        int rows = m.length;
	        int columns = m[0].length;
	        String str = "|\t";

	        for(int i=0;i<rows;i++){
	            for(int j=0;j<columns;j++){
	                str += m[i][j] + "\t";
	            }

	            System.out.println(str + "|");
	            str = "|\t";
	        }

	    }catch(Exception e){System.out.println("Matrix is empty!!");}
	}
	
	public static void printMatrixofStrings(String[][] m){
	    try{
	        int rows = m.length;
	        int columns = m[0].length;
	        String str = "|\t";

	        for(int i=0;i<rows;i++){
	            for(int j=0;j<columns;j++){
	                str += m[i][j] + "\t";
	            }

	            System.out.println(str + "|");
	            str = "|\t";
	        }

	    }catch(Exception e){System.out.println("Matrix is empty!!");}
	}
	
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
	
	public static void queryForDocuments(IndexReader ir, Fazione Y) throws NumberFormatException, IOException, ParseException {
		
	   long startTime = System.currentTimeMillis();
		 
       System.out.println("Number of documents: " + ir.numDocs());
	   Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_41);
		   
	   
	   IndexSearcher searcher = new IndexSearcher(ir);
	   QueryParser parser = new QueryParser(Version.LUCENE_41, "UserSN", analyzer);
	   BooleanQuery bq = new BooleanQuery();
	   
	   for(String query : Y.getBosses()) {
		   
		   Query q = parser.parse(query);
		   BooleanClause boolClause = new BooleanClause(q, Occur.SHOULD);
		   bq.add(boolClause);
		  
	   }
	   
	   TopDocs docs = searcher.search(bq, ir.numDocs());
	   ScoreDoc[] hits = docs.scoreDocs;
	   
	   Document doc = null;
	   int docID;
	   
	   for(ScoreDoc sc:hits) {
		   	
		   	docID = sc.doc;
		   	doc = searcher.doc(docID);
		   	Y.addDoc(docID);	// aggiungi il doc a quelli dei boss
        	Y.addUser(doc.get("userid"));
        	
			//System.out.println("Doc examined has ID " + docID + " with time " + searcher.doc(docID).get("time") +" from " + doc.get("UserSN") + " ..\n ******   Terms  *******");
			Terms terms = ir.getTermVector(docID, "text");  
		    TermsEnum iterator = terms.iterator(null); 
		    BytesRef term; String termText; 
		    
		    
        	
			while((term = iterator.next())!=null) {
				termText = term.utf8ToString();
				//System.out.print(termText); System.out.print(" ");
				
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
	   System.out.println("Elapsed time for data acquisition: " + (long) (System.currentTimeMillis() - startTime) + " msec");
	}

	public static void main(String[] args) throws IOException, SAXException, ParseException {
		
		long startTime = System.currentTimeMillis();
		
        Fazione Y = new Fazione("Y", ".//data//users.csv");
		// open the index directory
		Directory dir = new SimpleFSDirectory(new File(".\\data\\lucene_index"));
		IndexReader ir = DirectoryReader.open(dir);            
        
		/*System.out.println("Scanning documents..."); // this is the old function
        scanDocuments(ir, Y);
        */
        
		System.out.println("Querying for documents in index...");
        queryForDocuments(ir,Y);
        
        System.out.println("Number of bosses found in dataset " + Y.getUsers().size());
        System.out.println("Sorting terms by frequency");
        int numOfTopWords = 50;
        Y.getTermini().sortTermsByFreqComparatorAndTakeTopN(numOfTopWords);
        System.out.println("Setting hashmap with top "+numOfTopWords+" results");
        System.out.println("Top terms by frequency:\n" + Y.getTermini().getImportantTermsKeySet());
        	
        System.out.println("Setting top terms time series");
        Y.getTermini().setTopTermsTimeSeries(43200000L);
        System.out.println("Setting top terms SAX strings");
        Y.getTermini().setTopTermsSAXStrings(6, 0.01);
        System.out.println("Saving SAX string into file");
        
        
        System.out.println("Important Terms:");
        for (Entry<String, Termine> current_entry : Y.getTermini().getImportantTerms().entrySet())
        	{
        		String key = current_entry.getKey();
        		Termine value = current_entry.getValue();
        		String parola = value.getParola();
    			long fre = value.getFreq();
    			double[] ts = value.getTimeSeries();
    			char[] sax = value.getSAXString();
    			System.out.println("key= " + key + "\n\tparola= " + parola + "\n\tfrequenza= " + fre + 
    				"\n\ttimeseries= " + Arrays.toString(ts) + "\n\tsax= " + Arrays.toString(sax));
        	    
        	}
        
        //Y.getTermini().getSAXStringsIntoFile("./data/SAXStrings.csv"); //se non serve non chiamarlo.. impiega parecchio tempo
        
        // prepare input for k-means
        int k = 4;
        Y.getTermini().setSaxMostImp();
        char[][] saxStrings = Y.getTermini().getSaxMostImp();
        
        System.out.println("Sax strings");
        for(int i=0; i<saxStrings.length; i++) {
        	System.out.println(Arrays.toString(saxStrings[i]));
        }
        Y.getTermini().setParolaMostImp();
        String [] allWords = Y.getTermini().getParolaMostImp();
        System.out.println("Most important words: " +Arrays.toString(allWords));
        int original_size = Y.getTermini().getImportantTerms().entrySet().iterator().next().getValue().getTimeSeriesLength(); // non troppo pulito qui perchè si assume che l'iteratore abbia un next() 
        
        // K-means
        System.out.println("\nRunning "+ k +"- means clustering on top 1000 words from " + Y.getIdea() + " bosses");
        Kmeans kmeans = new Kmeans(saxStrings, k, 6, original_size, 1000, 0.); // remove redundant int alphabetsize

        int[] yes_partition = kmeans.perform_clustering();        
        System.out.println("partizione finale: " + Arrays.toString(yes_partition));
        
        Clusters clusters = new Clusters(yes_partition, allWords);
        System.out.println("found " + clusters.getNumOfClusters() + " clusters");
        
        // Co-occurrence graphs
        // for each cluster...
        for(int i=0; i<clusters.getNumOfClusters(); i++) {
	        Cluster cluster = clusters.getCluster(i);
	        ArrayList<String> clusterWords = cluster.getWords();
	        String[] words = clusterWords.toArray(new String[0]);
	        System.out.println("\t Cluster number "+ i + " contains:" + Arrays.toString(words));
	        
	        System.out.println("\tCreating co-occurrence graph on cluster " + i + "...");
	        Co_Occurence_Graph CoOcc = new Co_Occurence_Graph(words, Y.getDocs());
	        WeightedUndirectedGraph g = CoOcc.getGraph();
	        ;
        }
        
        
        System.out.println("All done");
        
        
        System.out.println("Total elapsed time: " + (long) (System.currentTimeMillis() - startTime) + " msec");

	}
}
