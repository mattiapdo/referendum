/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package loadData;

import it.stilo.g.algo.ConnectedComponents;
import it.stilo.g.algo.CoreDecomposition;
import it.stilo.g.structures.Core;
import it.stilo.g.structures.WeightedUndirectedGraph;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
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
import org.apache.lucene.util.Version;
import static org.apache.lucene.util.Version.LUCENE_41;


public class Co_Occurence_Graph {
    
	// ogni vertice rappresenta una delle parole presenti in questo array
    private String[] words;
    // indices � un set di documenti da controllare per connettere i vertici
    private HashSet<Integer> indices;
    // actual co-occurrence graph
    private WeightedUndirectedGraph g;														
    private static final int worker = (int) (Runtime.getRuntime().availableProcessors());  // chiedi perch� static e final... edit: questo effettivamente � un 
    																						//parametro sempre uguale a se stesso.. in ogni istanza
    
    public Co_Occurence_Graph(String[] words,  HashSet<Integer> indices) throws ParseException, IOException {
        /*
         * input:
         * words - and array containing the words that will constitute the vertexes of the graph
         * indices - an array containing the indices of the docs posted by bosses
         */
        this.words = words; 
        this.indices = indices;
        set_graph();
          
    }
    
    public int intersectionSize(HashSet<Integer> set_a, HashSet<Integer> set_b) {
     
      set_a.retainAll(set_b); // intersection of sets 
      int n = set_a.size() ; // length of intersection  
      
      return n; 
              
    }
    
    public void set_graph() throws ParseException, IOException {
        /*
        * sets this.g : the co-occurence graph for a given cluster 
        */ 
     
        // initialize graph 
    	g = new WeightedUndirectedGraph(words.length);
        //g = new WeightedUndirectedGraph(most_important_terms.size());
          
        Directory dir = new SimpleFSDirectory(new File(".\\data\\lucene_index_r_r"));
        Analyzer analyzer = new StandardAnalyzer(LUCENE_41);
        
        // creat a map that stores, for each word (in the cluster), a set of all the documents that contain that word
        HashMap<String,HashSet<Integer>> word_to_docs_map = new HashMap<String,HashSet<Integer>>();
        int n_strings = words.length; 
        
        // for each word...
        for (int idx = 0; idx < n_strings; idx++) {
            // query the index with that given word and retrieve all the documents that contains that word
            String query = words[idx]; 
            QueryParser parser = new QueryParser(Version.LUCENE_41, "text", analyzer); 
            Query q = parser.parse(query); 

            IndexReader reader = DirectoryReader.open(dir);
            IndexSearcher searcher = new IndexSearcher(reader);
            TopDocs docs = searcher.search(q, reader.numDocs());
            ScoreDoc[] hits = docs.scoreDocs;
            
            // update map from word to docs it appears in 
            //HashSet<Integer> tmp = null;
            // tmp is the set of all the document ids in which the current word is contained
            HashSet<Integer> tmp = new HashSet<>();
            //word_to_docs_map.put(query, tmp);
            
            // for each document, retrieved from the query
            for(Integer i=0;i<hits.length;i++) {
                Integer docId = hits[i].doc;
                // tmp = word_to_docs_map.get(query); 
                // if the document is a document written by an user of interest 
                if(indices.contains(docId)) {
                   tmp.add(docId);
                }
                //word_to_docs_map.put(query, tmp);   
            }
            word_to_docs_map.put(query, tmp);
	    }
	        
	    // add edges: iterate over possible term pairs ...
	    for(int idx_1 = 0; idx_1 < n_strings - 1; idx_1++) {
	        
	        for(int idx_2 = idx_1 + 1 ; idx_2 < n_strings ; idx_2 ++ ) {
	            
	            // extract document sets associated with the considered terms 
	            HashSet<Integer> set_a = word_to_docs_map.get(words[idx_1]); 
	            HashSet<Integer> set_b = word_to_docs_map.get(words[idx_2]); 
	            
	            // compute set intersection 
	            int n = intersectionSize(set_a, set_b);
	            
	            // if the terms appear in at least one common document
	            if(n>0) {
	               // add edge 
	               g.testAndAdd(idx_1, idx_2 , n); 
	            }
	        } 
	    }
	}
    
    public WeightedUndirectedGraph getGraph() {return this.g;}			// prima di settare g come atributo static era {return this.g;}
    
 // this method is not set to static because it uses the array of words 
    public Set<Set<Integer>> connected_components() throws InterruptedException {
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
       Set<Set<Integer>> comps = ConnectedComponents.rootedConnectedComponents(g, all, worker);
       
       //returns all the connected component as a nested set 
       return comps;
   }
    
 // this method is set to static becuase it does not take as input the array words (it is the same for each generated object) 
    // ma gli innermost cores dipendono dal particolare grafo che 
    public int[] innermost_cores() throws InterruptedException {
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
}
