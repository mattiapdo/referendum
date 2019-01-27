package loadData;

import static org.apache.lucene.util.Version.LUCENE_41;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.BytesRef;

import com.opencsv.CSVReader;

import net.seninp.jmotif.sax.SAXException;
import net.seninp.jmotif.sax.SAXProcessor;
import net.seninp.jmotif.sax.alphabet.NormalAlphabet;
import net.seninp.jmotif.sax.datastructure.SAXRecords;

public class Members {
	
	String idea;
	HashSet<String> users = new HashSet<String>();
	
	Members(String x){
		idea = x;
	}
	
	

	public void readDataLineByLine(String file) { 
	  
	    try { 
	    	
	        FileReader filereader = new FileReader(file); 
	        CSVReader csvReader = new CSVReader(filereader); 
	        String[] nextRecord; 

	        while ((nextRecord = csvReader.readNext()) != null) { 
	            //String Name = nextRecord[0];
	            String username = nextRecord[1];
	            String choice = nextRecord[2];
	            
	            if (choice.equals(idea)) {
	            	users.add(username);
	            }
	        } 
	    } 
	    catch (Exception e) { 
	        e.printStackTrace(); 
	    }
	}
	
	/*
	public HashMap<String, long[]> getTimeSeries (HashMap<String, Long> termsTimestamps, long grain) {
		//HashMap<String,ArrayList<String>> times_map=new HashMap<String,ArrayList<String>>();
		
		HashMap<String, long[]> timeSeries = new HashMap<String, long[]>();
		
		grain = 43200; // 12 ore
		long max = 1483185600; //  Saturday 31 December 2016 12:00:00
		long min = 1462104000; //  Sunday 1 May 2016 12:00:00
		int i = (int) ((max-min)/grain);
		
		Iterator<Entry<String, Long>> it = termsTimestamps.entrySet().iterator();
		// per ogni parola
	    while (it.hasNext()) {
	    	// inizializza la timeserie con tutti zeri
	    	long[] timeSerie = new long[i];
	        Arrays.fill(timeSerie, 0);
	        
	        Map.Entry pair = (Map.Entry)it.next();		// prendi la successiva coppia chiave valore di termsTimestamps
	        String term = (String) pair.getKey();		// questa è la parola
	        long[] timestamps =  (long[]) pair.getValue();		// questi sono i suoi timestaps
	        // per ogni timestamp
	        for (int j=0; j<timestamps.length; i++) {
	        	int index = (int) Math.floor(timestamps[j] - min);
	        	timeSerie[index]++;
	        }
	        it.remove(); // avoids a ConcurrentModificationException
	        timeSeries.put(term, timeSerie);
	    }
		
		return(timeSeries);
	}
	*/
	
	public static HashMap<String, double[]> getTimeSeries (HashMap<String,ArrayList<Long>> termsTimestamps, long grain) {
		//HashMap<String,ArrayList<String>> times_map=new HashMap<String,ArrayList<String>>();
		/*
		 * input
		 *  - termsTimestamps: HashMap che fa corrispondere a ogni termine una lista di timestamps
		 *  - grain : grain della time series
		 *  
		 *  output:
		 *  timeSeries: HashMap che fa corrispondere a ogni termine un array di double, ovvero la time series
		 */ 
		
		HashMap<String, double[]> timeSeries = new HashMap<String, double[]>();
		
		grain = 43200; // 12 ore
		long max = 1481035996842L; //  Saturday 31 December 2016 12:00:00
		long min = 1480170785012L; //  Sunday 1 May 2016 12:00:00
		int i = (int) ((max-min)/grain);
		
		Iterator<Entry<String, ArrayList<Long>>> it = termsTimestamps.entrySet().iterator();
		// per ogni parola
	    while (it.hasNext()) {
	    	// inizializza la timeserie con tutti zeri
	    	double[] timeSerie = new double[i+1];
	        Arrays.fill(timeSerie, 0);
	        
	        Map.Entry pair = (Map.Entry)it.next();		// prendi la successiva coppia chiave valore di termsTimestamps
	        String term = (String) pair.getKey();		// questa è la parola
	        ArrayList<Long> timestamps =  (ArrayList<Long>) pair.getValue();		// questi sono i suoi timestaps
	        // per ogni timestamp
	        System.out.println("max index = :" );
	        for (int j=0; j<timestamps.size(); j++) {
	        	int index = (int) Math.floor((timestamps.get(j) - min)/grain);
	        	timeSerie[index]++;
	        }
	        it.remove(); // avoids a ConcurrentModificationException
	        timeSeries.put(term, timeSerie);
	    }
		
		return(timeSeries);
	}
	
	public static String TimeSeriesToSAX(double[] timeserie, int alphabetSize, double nThreshold) throws SAXException { // 2, 0.01
		
		/*
		 * input:
		 *  - timeserie: un array di double
		 *  - alphabetSize, nThreshold: parametri per costruire la SAX
		 *  
		 *  output: 
		 *  - sax : stringa SAX
		 */
    	
        // instantiate classes 
        NormalAlphabet na = new NormalAlphabet();
        SAXProcessor sp = new SAXProcessor();     

        // perform the discretization with sp.ts2saxByChunking(timeseries , paaSize, cuts, nThreshold)
        SAXRecords res = sp.ts2saxByChunking(timeserie, timeserie.length, na.getCuts(alphabetSize), nThreshold);
        // get sax string
        String sax = res.getSAXString("");
   
        return (sax); 
    }
	
	public static HashMap<String, String> getSAX_Strings (HashMap<String, double[]> timeseries) throws SAXException {
		
		/*
		 * input: 
		 * timeseries : HashMap che fa corrispondere a ogni termine (String) 
		 */
			
		HashMap<String, String> SAX_Strings = new HashMap<String, String>();
		
		Iterator<Entry<String, double[]>> it = timeseries.entrySet().iterator();
		// per ogni parola
	    while (it.hasNext()) {
	        Map.Entry pair = (Map.Entry)it.next();		
	        String term = (String) pair.getKey() ;					// questa è la parola
	        double[] timeserie =  (double[])pair.getValue();		// questa è la sua time series
	        String SAX = TimeSeriesToSAX(timeserie, 2, 0.01);
	        SAX_Strings.put(term, SAX);
	        it.remove(); 											// avoids a ConcurrentModificationException
	    }
		
		return(SAX_Strings);
	}
	
	public static void main(String [ ] args) throws FileNotFoundException, IOException, SAXException {
		
		Members Y = new Members("Y");
		Y.readDataLineByLine(".//data//users.csv");
		System.out.println("number of yes supporters: " + Y.users.size());
		
		Members N = new Members("N");
		N.readDataLineByLine(".//data//users.csv");
		System.out.println("number of no supporters: " + N.users.size());	
		
		QueryIndex qi = new QueryIndex();
		
		
		// time series del SI
		System.out.println("Getting  time stamps...");
		HashMap<String, ArrayList<Long>> timestamps = qi.takeTimeStamps(Y.users);
		System.out.println("Getting time series...");
		HashMap<String, double[]> timeseries = getTimeSeries(timestamps, (long) 43200);
		System.out.println("Getting SAX strings...");
		HashMap<String, String> sax_strings = getSAX_Strings(timeseries);
		
		for (String term : sax_strings.keySet()){
			
            String key = term.toString();
            String value = sax_strings.get(key).toString();  
            System.out.println(key + " " + value);  


		} 
		
		
	}	
}


