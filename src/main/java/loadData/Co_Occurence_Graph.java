/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package loadData;

import it.stilo.g.structures.WeightedUndirectedGraph;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
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
    
    private final String[] words;
    private final HashSet<Integer> indices;
    WeightedUndirectedGraph g;
    
    
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
          
        Directory dir = new SimpleFSDirectory(new File(".\\data\\lucene_index_r"));
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
    
    public WeightedUndirectedGraph getGraph() {return this.g;}
    
}