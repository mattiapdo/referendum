package loadData;

import java.io.FileReader;
import java.util.HashSet;

import com.opencsv.CSVReader;

public class Fazione {
	
	/*
	 * Attributi
	 */
	@SuppressWarnings("unused")
	private String idea;
	private HashSet<String> users;
	private HashSet<String> bosses;
	private HashSet<Integer> docs;
	private Termini terms = new Termini();
	//private Clusters clusters;
	
	/*
	 * Metodi
	 */
	// Costruttore
	Fazione(String idea, String file){
		this.idea = idea;
		this.docs = new HashSet<Integer>();
		this.terms = new Termini();
		this.users = new HashSet<String>();
		this.bosses = new HashSet<String>();
		try { 
	    	
	        FileReader filereader = new FileReader(file); 
	        CSVReader csvReader = new CSVReader(filereader); 
	        String[] nextRecord; 

	        while ((nextRecord = csvReader.readNext()) != null) { 
	            String username = nextRecord[1];
	            String choice = nextRecord[2];
	            
	            if (choice.equals(idea)) {
	            	this.bosses.add(username);
	            }
	        }
	        csvReader.close();
	    } 
	    catch (Exception e) { 
	        e.printStackTrace(); 
	    }
	}
	
	public HashSet<String> getUsers() {	return this.users;}
	
	public HashSet<String> getBosses() {	return this.bosses;}
	
	public Termini getTermini() {return this.terms; }
	
	public void addUser(String newUser) { this.users.add(newUser); }

	public void addDoc(Integer newDoc) { this.docs.add(newDoc); }
	
	public HashSet<Integer> getDocs() {return this.docs;}
	
}
