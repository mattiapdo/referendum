/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package loadData;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import loadData.Fazione;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.it.ItalianAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;

import it.stilo.g.algo.ConnectedComponents;
import it.stilo.g.algo.HubnessAuthority;
import it.stilo.g.algo.SubGraph;
import it.stilo.g.structures.DoubleValues;
import it.stilo.g.structures.WeightedDirectedGraph;
import it.stilo.g.util.NodesMapper;

import static org.apache.lucene.util.Version.LUCENE_41;

/**
 * We obtain user names and tweet content of all the individuals that mentions politicians 
 * or via one of the words that we have previously identified as innermost cores of the connected 
 * components 
 * @author marti
 */
public class supporters {
	
	
    
    // get politicians supporting Yes 

    @SuppressWarnings("unlikely-arg-type")
	public static void main(String[] args) throws FileNotFoundException, IOException, ParseException, InterruptedException {
    
	    long startTime = System.currentTimeMillis();
	    Fazione Y = new Fazione("Y", ".//data//users.csv");
	    
	    HashSet<String> users_Y = Y.getBosses(); 
	    
	    // get politicans supporting No 
	    
	    Fazione N = new Fazione("N", ".//data//users.csv"); 
	    
	    HashSet<String> users_N = N.getBosses(); 
	  
	    
	    // read important words for Yes
	    HashSet<String> words_YES = new HashSet<String>(); //Array to save results
	    String filepath_words_Yes = "output\\words_Y.txt";
	    
	    BufferedReader br_Yes = new BufferedReader(new FileReader(filepath_words_Yes)); 
	    String line_Yes; 
	    while ((line_Yes = br_Yes.readLine()) != null) {
	        words_YES.add(line_Yes); 
	    } 
	    br_Yes.close();
	    
	    // print out number of Yes-related words 
	    System.out.println("Number of important words related to Yes retireved: " + words_YES.size());
	    
	    // read important words for NO 
	    HashSet<String> words_NO = new HashSet<String>(); //Array to save results
	    String filepath_words_NO = "output\\words_N.txt";
	    
	    BufferedReader br_NO = new BufferedReader(new FileReader(filepath_words_NO)); 
	    String line_NO; 
	    while ((line_NO = br_NO.readLine()) != null) {
	        words_NO.add(line_NO); 
	    }
	   
	    br_NO.close(); 
	    
	    System.out.println("Number of important words related to NO retireved: " + words_NO.size());
	    
	    // create a String containing all the words 
	    
	    HashSet<String> allTerms = new HashSet<String>();
	    
	    allTerms.addAll(users_Y);
	    allTerms.addAll(words_YES);
	    allTerms.addAll(users_N);
	    allTerms.addAll(words_NO);
	    
	    String allTerms_string = String.join(" ",  allTerms).toLowerCase();
	    
	    // query index 
	    
	    //Analyzer analyzer = new StandardAnalyzer(LUCENE_41); 
	    Analyzer analyzer = new ItalianAnalyzer(LUCENE_41);
	    
	    Directory dir = new SimpleFSDirectory(new File(".\\data\\lucene_index"));
	    //String querystr = args.length > 0 ? args[0] : allTerms_string;
	    String querystr = "text:" + allTerms_string;
	    QueryParser parser = new QueryParser(LUCENE_41, "text", analyzer);
	    Query q = parser.parse(querystr); 
	    
	    
	    IndexReader reader = DirectoryReader.open(dir);
	    IndexSearcher searcher = new IndexSearcher(reader);
	    System.out.println("Searching for terms..." +  allTerms_string);
	    TopDocs docs = searcher.search(q, reader.numDocs());
	    System.out.println("Writing users to text file...");
	    ScoreDoc[] hits = docs.scoreDocs;
	    
	    // find users and write to text file 
	    
//	    BufferedWriter writer_userId = new BufferedWriter(new FileWriter("output//userIds.txt"));
//	    BufferedWriter writer_UserNames = new BufferedWriter(new FileWriter("output//usersNames.txt"));
//	    BufferedWriter writer_docId = new BufferedWriter(new FileWriter("output//docIds.txt"));
	    
	    HashSet<String> retrieved_users = new HashSet<String>(); // these set may not be needed 
	    HashSet<String> retrieved_user_ids = new HashSet<String>();
	    HashSet<Integer> retrieved_doc_ids = new HashSet<Integer>();
	    
	    System.out.println("The total number of matching tweets is : " + docs.totalHits);
	    
	    // for each results 
	    for(int i = 0; i<hits.length; i++) {
	        
	        // get document 
	        Document doc = reader.document(i);
		
	        // user names 
	        String user = doc.get("UserSN");
	        //writer_UserNames.write(user);
	        //writer_UserNames.newLine();
	        retrieved_users.add(user); 
	        
	        // user ids 
	        String user_id = doc.get("userid"); 
	        //writer_userId.write(user_id);
	        //writer_userId.newLine(); 
	        retrieved_user_ids.add(user_id);
	        
	        // doc ids 
	        Integer docId = hits[i].doc;
	        //writer_docId.write(docId.toString());
	        //writer_docId.newLine();
	        retrieved_doc_ids.add(docId);        
	    }
	    
//	    writer_userId.close();
//	    writer_docId.close();
//	    writer_UserNames.close(); 
	    
	   
	    // print out number of corresponding users 
	    
	    System.out.println("The associated number of users is  : " + retrieved_user_ids.size());
	    System.out.println("Elapsed time: " + (long) (System.currentTimeMillis() - startTime) + " msec");
	    
	    startTime = System.currentTimeMillis();
    	
        // read file of users id and put themi into users HashSet
    	
		System.out.println("Collected user ids... they are " + retrieved_user_ids.size());
		System.out.println("Elapsed time: " + (long) (System.currentTimeMillis() - startTime) + " msec"); 
		long now = System.currentTimeMillis();
		
        NodesMapper<Long> mapper = new NodesMapper<>();
        
 		// read file containing the graph and create the graph	
		System.out.println("Reading file containing the graph and create the graph");
		WeightedDirectedGraph g = new WeightedDirectedGraph(retrieved_user_ids.size()+1);
        String filePath = "data\\test-dataset-sbn2017\\Official_SBN-ITA-2016-Net";
        BufferedReader br = null;
		FileReader fr = null;

		fr = new FileReader(filePath);
		br = new BufferedReader(fr);
		String sCurrentLine;

		while ((sCurrentLine = br.readLine()) != null) {
			String[] splittedLine = sCurrentLine.split("\t");
			Long source = Long.parseLong(splittedLine[0]);
			Long destination = Long.parseLong(splittedLine[1]);
			Integer weight = Integer.parseInt(splittedLine[2]);
			// if both nodes are in our users, add source and destination
			if(retrieved_user_ids.containsAll(Arrays.asList(source, destination))) {
				g.testAndAdd(mapper.getId(source), mapper.getId(destination), weight);
			}
		}
		br.close();
		fr.close();
		
		System.out.println("Built graph... size = " + g.size);
		System.out.println("Elapsed time: " + (long) (System.currentTimeMillis() - now)/60 + " sec");
		now = System.currentTimeMillis();
		 
        System.out.println("Creating roots array to find connected components ...");
        // collect ids of the users as roots to be used to find largest connected component
 		int i = 0;
 		int[] roots = new int[retrieved_user_ids.size()];
 		for(String userId: retrieved_user_ids) {
 			roots[i] = mapper.getId(Long.parseLong(userId));
 			i++;
 		}
 		System.out.println("Elapsed time: " + (long) (System.currentTimeMillis() - now)/60 + " sec");
		now = System.currentTimeMillis();
 		
 		System.out.println("Finding largest connected components ...");
 		int numThreads = 4;
 		Set<Set<Integer>> connComponents = ConnectedComponents.rootedConnectedComponents(g, roots, numThreads);
 		System.out.println("Found " + connComponents.size() +" connected components");
 		
 		int maxCCsize = -1;
 		Set<Integer> LCC = new HashSet<>();
		for(Set<Integer> component: connComponents) {
			if(component.size()>maxCCsize) {
				System.out.println("\tcurrent component has " + component.size() + " nodes");
				LCC = component;
			}
		}
		
		System.out.println("LCC  component has " + LCC.size() + " nodes");
		
		int[] LCC_arr = new int[LCC.size()]; 
		int j = 0;
		for(Integer node: LCC) {
			LCC_arr[j] = node;
			j++;
		}
		System.out.println("Elapsed time: " + (long) (System.currentTimeMillis() - now)/60 + " sec");
		now = System.currentTimeMillis();
		
		System.out.println("Finding largest connected components");
		WeightedDirectedGraph g_LCC = SubGraph.extract(g, LCC_arr, numThreads);
		System.out.println("\tLCC graph has " + LCC.size() + " nodes");
		System.out.println("Elapsed time: " + (long) (System.currentTimeMillis() - now)/60 + " sec");
		now = System.currentTimeMillis();
		
		System.out.println("Running HITS ...");
		ArrayList<ArrayList<DoubleValues>> authorities = HubnessAuthority.compute(g_LCC, 0.00001, numThreads); //graph, precision and number of cores
        ArrayList<DoubleValues> authorityScore =  authorities.get(0); //
        ArrayList<DoubleValues> hubnessScore =  authorities.get(1); 
        System.out.println("Elapsed time: " + (long) (System.currentTimeMillis() - now)/60 + " sec");
		
        System.out.println("Total elapsed time: " + (long) (System.currentTimeMillis() - startTime)/60 + " sec");

	    
	   }

}