package loadData;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.lucene.analysis.util.CharArrayMap.EntrySet;

import com.opencsv.CSVWriter;

import net.seninp.jmotif.sax.SAXException;

public class Termini {
	/*
	 * Attributes
	 */
	
	private Map<String, Termine> terms;
	private Map<String, Termine> reduced_terms;
	private char[][] saxMostImp;
	//private ??? groups;
	//private ??? coGraph;
	
	/*
	 * Methods
	 */
	
	Termini(){
		this.terms = new HashMap<String, Termine>();
	}
	
	public Map<String, Termine> getTerms() { return this.terms; }
	
	public void addTerm(String term, Termine Termine) {this.terms.put(term, Termine);}
	
	public Termine getTerm(String key) {return this.terms.get(key);}
	
	public  Set<String> getImportantTermsKeySet() {return this.reduced_terms.keySet();} //!!
	
	public  Map<String, Termine> getImportantTerms() {return this.reduced_terms;}
	
	//public void kMeans();
	//public void getCoGraph();
	
	public void sortByFreq() {
		   Map<String, Termine> termini = this.terms;
		   Map<String, Termine> sortedMap = new HashMap<>();
		  		   
		   // not yet sorted
	       List<Termine> termsByFreq = new ArrayList<>(termini.values());
	       
	       
	       Collections.sort(termsByFreq, Comparator.comparing(Termine::getFreq));
	       Collections.reverse(termsByFreq);
	       
	       /* Collections.sort(termsByFreq, Collections.reverseOrder(Comparator.comparing(Termine::getFreq))); */
	       
	       for (Entry<String, Termine> termine : this.terms.entrySet()) {
				sortedMap.put(termine.getKey(), termine.getValue());
			}
	       this.terms = sortedMap;
    }
	
	public void setTop(int n) {
		this.reduced_terms = new HashMap<>();
		int cnt = 0; 
        
        Iterator<String> itr= this.terms.keySet().iterator();
        while (cnt<n && itr.hasNext()) {
            String key =  itr.next().toString();
            Termine value= this.terms.get(key);
            this.reduced_terms.put(key, value);
            cnt++;
        }
	}
	
	public void setTopTermsTimeSeries(long grain) {
		
		for (Entry<String, Termine> termine : this.reduced_terms.entrySet()) {
			termine.getValue().setTimeSeries(grain);
		}
	}
	
	public void setTopTermsSAXStrings(int alphabetSize, double nThreshold) throws SAXException {
		
		for (Entry<String, Termine> termine : this.reduced_terms.entrySet()) {
			termine.getValue().setSAXString(alphabetSize, nThreshold);;
		}
	}
	
	public void getSAXStringsIntoFile(String filePath) {
		File file = new File(filePath); 
	    try { 
	        // create FileWriter object with file as parameter 
	        FileWriter outputfile = new FileWriter(file); 
	  
	        // create CSVWriter object filewriter object as parameter 
	        CSVWriter writer = new CSVWriter(outputfile); 
	  
	        // add data to csv 
	        for (Entry<String, Termine> termine : this.reduced_terms.entrySet()) {
				writer.writeNext( new String[] {termine.getValue().getSAXString().toString()}); 
			} 	  
	        // closing writer connection 
	        writer.close(); 
	    } 
	    catch (IOException e) { 
	        // TODO Auto-generated catch block 
	        e.printStackTrace(); 
	    } 
	}

	public void setSaxMostImp() {
		int n = this.reduced_terms.size();
		// qui ci andrebbe un try except perh� non � detto che l'iterator che invochi abbia un next()
		int m = this.reduced_terms.entrySet().iterator().next().getValue().getTimeSeries().length; 
		this.saxMostImp = new char[n][m];
		// assumendo che le time series abbiano tutte la stessa lunghezza:
		int count = 0;
		while(this.reduced_terms.entrySet().iterator().hasNext()) {
			this.saxMostImp[count] = this.reduced_terms.entrySet().iterator().next().getValue().getSAXString();
			count++;
		}
	}

	public char[][] getSaxMostImp(){return(this.saxMostImp);}
}
