package loadData;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.opencsv.CSVWriter;

import net.seninp.jmotif.sax.SAXException;

public class Termini {
	/*
	 * Attributes
	 */
	
	private Map<String, Termine> terms;
	//private Map<String, Termine> ordered_terms;
	private Map<String, Termine> reduced_terms;
	private char[][] saxMostImp;
	private String[] paroleMostImp;
	
	public boolean ASC = true;
    public boolean DESC = false;
	
	/*
	 * Methods
	 */
	
	Termini(){this.terms = new LinkedHashMap<String, Termine>();}
	
	public Map<String, Termine> getTerms() { return terms; }
	
	public void addTerm(String term, Termine Termine) {terms.put(term, Termine);}
	
	public Termine getTerm(String key) {return terms.get(key);}
	
	public  Set<String> getImportantTermsKeySet() {return reduced_terms.keySet();} //!!
	
	public  Map<String, Termine> getImportantTerms() {return reduced_terms;}
	
	public void sortTermsByFreqComparatorAndTakeTopN(int n){   
		/*
		 * Si ordina la mappa terms con una lista e si salvano le prime n entries nella mappa reduced_terms
		 */

        List<Entry<String, Termine>> list = new LinkedList<Entry<String, Termine>>(terms.entrySet());

        // Sorting the list based on values
        Collections.sort(list, new Comparator< Entry<String, Termine>>() {
        public int compare(Entry<String, Termine> o1, Entry<String, Termine> o2){
            if (DESC){
                return Long.compare(o1.getValue().getFreq(), o2.getValue().getFreq());
            }
            else{
                return Long.compare(o2.getValue().getFreq(), o1.getValue().getFreq());
            }
        }
		});
        
        // Maintaining insertion order with the help of LinkedList
        //HashMap<String, Termine> sortedMap = new LinkedHashMap<String, Termine>();
        /*
        for (Entry<String, Termine> entry : list)
        {
        	terms.put(entry.getKey(), entry.getValue());
        }
		*/
        reduced_terms = new LinkedHashMap<>();
        int cnt = 0;
        
        for (Entry<String, Termine> entry : list) {
        	if (cnt<n) {
        		String key =  entry.getKey();
                Termine value= entry.getValue();
                reduced_terms.put(key, value);
                cnt++;
        	}
        }
        /*
        Iterator<Entry<String, Termine>> itr=  terms.entrySet().iterator();
        while (cnt<n && itr.hasNext()) {
        	Entry<String, Termine> entry = itr.next();
            String key =  entry.getKey();
            Termine value= entry.getValue();
            reduced_terms.put(key, value);
            cnt++;
        }
        */
    }
	
	public void setTopTermsTimeSeries(long grain) {
		/*
		 * Si itera lungo la mappa reduced_terms e si setta l'attributo timeSeries di ogni valore
		 */
		Iterator<Entry<String, Termine>> itr= reduced_terms.entrySet().iterator();
        while (itr.hasNext()) {
        	Entry<String, Termine> entry = itr.next();
            entry.getValue().setTimeSeries(grain);
        }
	}
	
	public void setTopTermsSAXStrings(int alphabetSize, double nThreshold) throws SAXException {
		/*
		 * Si itera lungo la mappa reduced_terms e si setta l'attributo saxString di ogni valore
		 */
		Iterator<Entry<String, Termine>> itr= reduced_terms.entrySet().iterator();
        while (itr.hasNext()) {
        	Entry<String, Termine> entry = itr.next();
            entry.getValue().setSAXString(alphabetSize, nThreshold);
        }
	}
	
	public void getSAXStringsIntoFile(String filePath) {
		/*
		 * Funzione utile se si vuole stampare le sax in un file in filePath
		 */
		File file = new File(filePath); 
	    try { 
	        // create FileWriter object with file as parameter 
	        FileWriter outputfile = new FileWriter(file); 
	  
	        // create CSVWriter object filewriter object as parameter 
	        CSVWriter writer = new CSVWriter(outputfile); 
	  
	        // add data to csv 
	        while (reduced_terms.entrySet().iterator().hasNext()) {
				writer.writeNext( new String[] {Arrays.toString(reduced_terms.entrySet().iterator().next().getValue().getSAXString())}); 
			} 	  
	        // closing writer connection 
	        writer.close(); 
	    } 
	    catch (IOException e) { 
	        e.printStackTrace(); 
	    } 
	}

	

	
	public void setSaxMostImp() {
		/*
		 * questa funzione scorre la LinkedHashMap reduced_terms e setta una matrice - saxMostImp - con tutte le parole
		 */
		
		int n = this.reduced_terms.size();
		int m = this.reduced_terms.entrySet().iterator().next().getValue().getSaxLength(); 
		this.saxMostImp = new char[n][m];    // assumendo che le time series abbiano tutte la stessa lunghezza
		int count = 0;
		
		//while(this.reduced_terms.entrySet().iterator().hasNext() ) {
		while(count<n) {
			this.saxMostImp[count] = this.reduced_terms.entrySet().iterator().next().getValue().getSAXString();
			count++;
		}
	}
	
	public void setParolaMostImp() {
		/*
		 * questa funzione scorre la LinkedHashMap reduced_terms e setta un vettore paroleMostImp con tutte le parole
		 */

		int n = this.reduced_terms.size();
		this.paroleMostImp = new String[n];
		int count = 0;
		
        Iterator<Entry<String, Termine>> itr= reduced_terms.entrySet().iterator();
        while (itr.hasNext()) {
        	Entry<String, Termine> entry = itr.next();
            paroleMostImp[count] = entry.getValue().getParola();
            count ++;
        }
	}

	public char[][] getSaxMostImp(){return(this.saxMostImp);}
	
	public String[] getParolaMostImp() {return(this.paroleMostImp);}
}
