/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package loadData;

import it.stilo.g.algo.ConnectedComponents;
import it.stilo.g.algo.CoreDecomposition;
import it.stilo.g.algo.SubGraph;
import it.stilo.g.algo.SubGraphByEdgesWeight;
import it.stilo.g.structures.Core;
import it.stilo.g.structures.WeightedUndirectedGraph;

import static org.apache.lucene.util.Version.LUCENE_41;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;


import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.it.ItalianAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.QueryParserBase;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.util.BytesRef;



public class CoOcc {
    
	// ogni vertice rappresenta una delle parole presenti in questo array
    private String[] words;
    private HashSet<Integer> indices; // un set di documenti da controllare per connettere i vertici
    //private HashSet<Integer> indices;
    // fazione
    private Fazione fazione;
    // actual co-occurrence graph
    private WeightedUndirectedGraph g;
    ArrayList<String[]> components_core_nodes ;
	private QueryParserBase parser;														
    private static final int worker = (int) (Runtime.getRuntime().availableProcessors());  // chiedi perchè static e final... edit: questo effettivamente é un 
    																						//parametro sempre uguale a se stesso.. in ogni istanza
    
    public CoOcc(String[] words, HashSet<Integer> indices, Fazione fazione, int threshold) throws ParseException, IOException, InterruptedException {
        /*
         * input:
         * words - and array containing the words that will constitute the vertexes of the graph
         * indices - an array containing the indices of the docs posted by bosses
         */
        this.words = words; 
        this.indices = indices;
        this.fazione = fazione;
        
        set_graph(threshold);
        components_core_nodes = innermost_cores_from_connected_components(g);
    }

    CoOcc(ArrayList<String> clusterWords, HashSet<Integer> indices) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public int intersectionSize(HashSet<Integer> set_a, HashSet<Integer> set_b) {
     
      set_a.retainAll(set_b); // intersection of sets 
      int n = set_a.size() ; // length of intersection  
      
      return n; 
              
    }
    
    public void set_graph(int threshold) throws ParseException, IOException {
        /*
        * sets this.g : the co-occurence graph for a given cluster 
        */ 
     
        // initialize graph 
    	g = new WeightedUndirectedGraph(words.length);
    	
    	HashMap<String, HashSet<Integer>> word_to_docs_map = new HashMap<>();
      
        Directory dir = new SimpleFSDirectory(new File(".\\data\\lucene_index_ita"));
        IndexReader reader = DirectoryReader.open(dir);
        IndexSearcher searcher = new IndexSearcher(reader);
        //Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_41);
        Analyzer analyzer = new ItalianAnalyzer(LUCENE_41);
       
 	   	String mywords = String.join(" ", words);
 	   	System.out.println("words in current cluster: " + mywords );
 	  
 	   	String mybosses = String.join(" ", fazione.getBosses());
 	    System.out.println("searching among documents by: " + mybosses);
 	   
 	   	String queryString = "(text:" + mywords + ") AND (UserSN:" + mybosses + ")";
 	   	QueryParser parser = new QueryParser(Version.LUCENE_41, "UserSN", analyzer);
 	   	Query q = parser.parse(queryString); 
 	   	
 	   	// find all documents written by the bosses that contains at least one of the words in the cluster
 	   	TopDocs docs = searcher.search(q, reader.numDocs());
 	   	ScoreDoc[] hits = docs.scoreDocs;
 	   	
 	   	System.out.println("Found " + hits.length + " documents written by politicians that contain words in current cluster");
 	   	
 	   	// per ogni parola
 	   	for (int idx = 0; idx < words.length; idx++) {
 	   	  //System.out.println("parola: "+ words[idx]);
 		  HashSet<Integer> tmp = new HashSet<>();			// tmp è il set di documenti in cui compare la parola
 		  // for each document, retrieved from the query
          for(Integer i=0;i<hits.length;i++) {
        	  //System.out.println("hit num " + i);
              Integer docId = hits[i].doc;
              TermsEnum iterator = reader.getTermVector(docId, "text").iterator(null);
              BytesRef term;
              HashSet<String> wordsInDoc = new HashSet<>();		// trova le parole nel documento
              while((term = iterator.next())!=null) {
            	  wordsInDoc.add(term.utf8ToString());
            	  //System.out.println("add term " + term.utf8ToString());
              }
              if(wordsInDoc.contains(words[idx])){ // se la parola è contenuta nel documento aggiungi il documento a tmp
            	  tmp.add(docId);
  			  }
          }
          word_to_docs_map.put(words[idx], tmp); // aggiorna la mappa
 	   }
 	   	 
	    // add edges: iterate over possible term pairs ...
	    for(int idx_1 = 0; idx_1 < words.length - 1; idx_1++) {
	        
	        for(int idx_2 = idx_1 + 1 ; idx_2 <  words.length ; idx_2 ++ ) {
	            
	            // extract document sets associated with the considered terms 
	            HashSet<Integer> set_a = word_to_docs_map.get(words[idx_1]); 
	            HashSet<Integer> set_b = word_to_docs_map.get(words[idx_2]); 
	            
	            // compute set intersection 
	            int n = intersectionSize(set_a, set_b);
	            
	            // if the terms appear in at least one common document
	            if(n>threshold) {
	               // add edge 
	               g.testAndAdd(idx_1, idx_2 , n); 
	            }
	        } 
	    }
	    
	    
	}
    
    public WeightedUndirectedGraph getGraph() {return this.g;}			// prima di settare g come atributo static era {return this.g;}
    
    public WeightedUndirectedGraph getSubGraph(double threshold) {
        
        WeightedUndirectedGraph s = SubGraphByEdgesWeight.extract(g, threshold, worker);
        
        return s; 
    }
      
    // this method is not set to static because it uses the array of words 
    public Set<Set<Integer>> connected_components(WeightedUndirectedGraph s) throws InterruptedException {
    /* Find all the connected components in the input graph 
     * Input : graph g
     * Output : Nested set in which each inner set stores the node in a connected component 
     *
     */
        
       // initialize array of roots (taking all the nodes) 
       int[] all = new int[words.length];
       for (int i = 0; i < words.length; i++) {
           all[i] = i;
       }
       
       // extract connected components 
       Set<Set<Integer>> comps = ConnectedComponents.rootedConnectedComponents(s, all, worker);
       
       //returns all the connected component as a nested set 
       return comps;
   }
    
    // this method is set to static becuase it does not take as input the array words (it is the same for each generated object) 
    // ma gli innermost cores dipendono dal particolare grafo che 
    public int[] innermost_cores(WeightedUndirectedGraph g ) throws InterruptedException {
    /* Find the innermost core of the input graph 
     * Input : graph g
     * Output : array showing the nodes belonging to the innermost core 
     */
   
       // extract innermost cores
       Core c = CoreDecomposition.getInnerMostCore(g, worker);
       
       //number of nodes in the innermost cores 
       int n_nodes = c.seq.length; 
       
       // initialize array to store nodes in the core 
       int[] core_nodes = new int[n_nodes]; 
       
       // fill the array @core_nodes
       System.arraycopy(c.seq, 0, core_nodes, 0, n_nodes);
       
       //returns all the nodes in the innermost core 
       return core_nodes;
   }
    
    public int[] toInt(Set<Integer> set) {
        
             int[] a = new int[set.size()];
             int i = 0;
             for (Integer val : set) a[i++] = val;
             return a;
    
    }
    
    public ArrayList<String[]> innermost_cores_from_connected_components(WeightedUndirectedGraph s) throws InterruptedException {
        
        Set<Set<Integer>> components = connected_components(s);
               
        // intialize array to store all the nodes belonging to an innermost core in each connected component 
        ArrayList<int[]> all_core_nodes = new ArrayList<int[]>();
        ArrayList<String[]> all_core_words = new ArrayList<String[]>();
        
       // for each connected component 
        for(Set<Integer> component:components) {
            
             // create subgraph 
             
             // convert set to primitive array 
             int[] component_array = toInt(component); 
             
             // extract corresponding subgraph 
             WeightedUndirectedGraph s_connected = SubGraph.extract(s, component_array, worker);
            
             // get innermost core 
             int[] core_nodes = innermost_cores(s_connected) ; 
             String[] words_core = new String[core_nodes.length];
             for(int i = 0; i < core_nodes.length; i++ ) {
                 
                 words_core[i] = words[core_nodes[i]]; 
                 
             }
             
             // append to @all_core_nodes  
             all_core_nodes.add(core_nodes); 
             all_core_words.add(words_core); 
            
        }
        
        

     return all_core_words;    
    }


    
}




