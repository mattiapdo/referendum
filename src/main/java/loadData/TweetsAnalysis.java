package loadData;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map.Entry;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.it.ItalianAnalyzer;
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

	public static void queryForDocuments(IndexReader ir, Fazione Y) throws NumberFormatException, IOException, ParseException {
		
	   long startTime = System.currentTimeMillis();
		 
       System.out.println("Number of documents: " + ir.numDocs());
	   Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_41);
	   //Analyzer analyzer = new ItalianAnalyzer(LUCENE_41);
	   
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
				if(!termText.equals("https") &&  !termText.equals("rt") && !termText.equals("t.co")){
					if(! Y.getTermini().getTerms().containsKey(termText)){
	                	//.. aggiungilo
	                	Y.getTermini().addTerm(termText, new Termine(termText));
	                }
	                //update frequency 
	            	Y.getTermini().getTerm(termText).incrementFreq(1L);  
	                
	                //update timestamp
	            	Y.getTermini().getTerm(termText).addTimestamp(Long.valueOf(doc.get("time")));
				}
				
				
			}
		}
	   System.out.println("Elapsed time for data acquisition: " + (long) (System.currentTimeMillis() - startTime) + " msec");
	}

	public static void main(String[] args) throws IOException, SAXException, ParseException, InterruptedException {
		
		long startTime = System.currentTimeMillis();
		
        Fazione Y = new Fazione("Y", ".//data//users.csv");
		// open the index directory
		Directory dir = new SimpleFSDirectory(new File(".\\data\\lucene_index_ita"));
		IndexReader ir = DirectoryReader.open(dir);            
        
		System.out.println("Querying for documents in index...");
        queryForDocuments(ir,Y);
        
        System.out.println("Number of bosses found in dataset " + Y.getUsers().size());
        
        int numOfTopWords = 50;
        Y.getTermini().sortTermsByFreqComparatorAndTakeTopN(numOfTopWords);
        System.out.println("Top terms by frequency:\n" + Y.getTermini().getImportantTermsKeySet());
        	
      
        Y.getTermini().setTopTermsTimeSeries(43200000L);	//Setting top terms time series  
        Y.getTermini().setTopTermsSAXStrings(6, 0.01);	    //Setting top terms SAX strings

        // prepare input for k-means
        int k = 2;
        Y.getTermini().setSaxMostImp();
        char[][] saxStrings = Y.getTermini().getSaxMostImp();
        
        Y.getTermini().setParolaMostImp();
        String [] allWords = Y.getTermini().getParolaMostImp();
        System.out.println("Most important words: " +Arrays.toString(allWords));
        int original_size = Y.getTermini().getImportantTerms().entrySet().iterator().next().getValue().getTimeSeriesLength(); // non troppo pulito qui perchè si assume che l'iteratore abbia un next() 
        
        // K-means
        System.out.println("\n\nRunning "+ k +"- means clustering on top 1000 words from " + Y.getIdea() + " bosses");
        Kmeans kmeans = new Kmeans(saxStrings, k, 6, original_size, 1000, 0.); // remove redundant int alphabetsize

        int[] yes_partition = kmeans.perform_clustering();        
        System.out.println("Final partition: " + Arrays.toString(yes_partition));
        
        Clusters clusters = new Clusters(yes_partition, allWords);
        System.out.println("found " + clusters.getNumOfClusters() + " clusters\n");
        
        // initilize file in writing mode 
        BufferedWriter writer = new BufferedWriter(new FileWriter("output//words_"+ Y.getIdea()+".txt"));
        
        // for each cluster...
        for(int i=0; i<clusters.getNumOfClusters(); i++) {
	        Cluster cluster = clusters.getCluster(i);
	        ArrayList<String> clusterWords = cluster.getWords();
	        String[] words = clusterWords.toArray(new String[0]);
	        System.out.println("\nCluster number "+ i );
	        
	        int threshold = 3;
	        System.out.println("Creating co-occurrence graph on cluster " + i + " with threshold " + threshold + " on edge weights");
	        CoOcc CoOcc = new CoOcc(words, Y.getDocs(), Y, threshold);
	        System.out.println("Extract innermost cores from connected components ");
            ArrayList<String[]> components_core_nodes = CoOcc.components_core_nodes; 
           
            int c = 0; 
            // for each word in @components_core_nodes , retrieve time series 
            for( String[] words_arr : components_core_nodes  ) {
                for( String word : words_arr) {
                    // get time series with grain 3 hours 
                    Y.getTermini().getTerms().get(word).setTimeSeries(10800);
                    double[] ts = Y.getTermini().getTerms().get(word).getTimeSeries();
                    // connected component id + cluster id + time series 
                    System.out.println(("cluster id: " + String.valueOf(i) +  " connected comp id: " +  String.valueOf(c) + " timeseries:" + Arrays.toString(ts) ));
                    writer.write(word);
                    writer.newLine();
                }
                // increment connected component counter 
                c++; 
            }
        }
        
        writer.close();
        System.out.println("\n\n***********************\nAll done");
        
        
        System.out.println("Total elapsed time: " + (long) (System.currentTimeMillis() - startTime) + " msec");

	}
}
