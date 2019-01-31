package loadData;

import java.util.ArrayList;
import java.util.Arrays;

import net.seninp.jmotif.sax.SAXException;
import net.seninp.jmotif.sax.SAXProcessor;
import net.seninp.jmotif.sax.alphabet.NormalAlphabet;
import net.seninp.jmotif.sax.datastructure.SAXRecords;

public class Termine {
	/*
	 * Attributes
	 */
	private String parola;
	private Long freq = 0L;
	private ArrayList<Long> timeStamps = new ArrayList<>();
	private double[] timeSeries;
	private char[] saxString;
	
	/*
	 * Methods
	 */

	Termine(String parola){
		this.parola = parola;
	}
	public void setParola(String parola) { this.parola = parola; }
	
	public String getParola() {return this.parola; }
	
	public void setFreq(Long freq) { this.freq = freq; }
	
	public void incrementFreq(Long increment) { this.freq = this.freq + increment; }
	
	public Long getFreq() { return this.freq; }
	
	public void addTimestamp(Long timestamp) {this.timeStamps.add(timestamp);}
	
 	public void setTimeSeries (long grain) {
		
		/*
		 * input
		 *  - termsTimestamps: HashMap che fa corrispondere a ogni termine una lista di timestamps
		 *  - grain : grain della time series
		 *  
		 *  output:
		 *  timeSeries: HashMap che fa corrispondere a ogni termine un array di double, ovvero la time series
		 */ 
 		
//		//to be used with full dataset
//		grain = 43200000; // 12 ore
//		long max = 1481035996842L; //  Saturday 31 December 2016 12:00:00
// 		long min = 1480170785012L; //  Sunday 1 May 2016 12:00:00
 		
 		//to be used with reduced dataset
 		grain = 4320000; 
 		long max = 1480257098290L;  
		long min = 1480170785012L; 
		int i = (int) ((max-min)/grain);
		
		// inizializza la timeserie con tutti zeri
    	double[] timeSeries = new double[i+1];
        Arrays.fill(timeSeries, 0);
        
        for (int j=0; j< this.timeStamps.size(); j++) {
        	int index = (int) Math.floor((timeStamps.get(j) - min)/grain);
        	timeSeries[index]++;
        }
        
		this.timeSeries = timeSeries;
	}

 	public double[] getTimeSeries() {return this.timeSeries;}
 	
 	public int getTimeSeriesLength() {return this.timeSeries.length;}
 	
 	public void setSAXString(int alphabetSize, double nThreshold) throws SAXException { 
		
		/*
		 * input:
		 *  - alphabetSize 
		 *  - nThreshold 
		 *  
		 *  output: 
		 *  - sax : SAX string
		 */
    	
        // instantiate classes 
        NormalAlphabet na = new NormalAlphabet();
        SAXProcessor sp = new SAXProcessor();     

        // perform the discretization with sp.ts2saxByChunking(timeseries , paaSize, cuts, nThreshold)
        SAXRecords res = sp.ts2saxByChunking(this.timeSeries, this.timeSeries.length, na.getCuts(alphabetSize), nThreshold);
        // get sax string
        this.saxString = res.getSAXString("").toCharArray();
    }

 	public char[] getSAXString() {return this.saxString;}
 	
 	public int getSaxLength() {return this.saxString.length;}
}
