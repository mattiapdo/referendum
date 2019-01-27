/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package loadData;

import it.stilo.g.structures.WeightedUndirectedGraph;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriterConfig;
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
    private final int[] indices;
    
    
    public Co_Occurence_Graph(String[] words,  int[] indices) {
        
        this.words = words; 
        this.indices = indices; 
          
    }
    
    
    public static int set_intersection(HashSet set_a, HashSet set_b) {
     
      set_a.retainAll(set_b); // intersection of sets 
      
      int n = set_a.size() ; // length of intersection  
      
      return n; 
              
    }
    
    
    
    
    public static WeightedUndirectedGraph create_graph(String[] words, Integer[] indices) throws ParseException, IOException {
        /*
        * 
        * words : array of words  
        * Return co-occurence graph for a given cluster 
        */ 
     
        // initialize graph 
        WeightedUndirectedGraph g = new WeightedUndirectedGraph(most_important_terms.size());
          
        Directory dir; 
        dir = new SimpleFSDirectory(new File("D:/luc_index"));
        Analyzer analyzer = new StandardAnalyzer(LUCENE_41);
        IndexWriterConfig cfg= new IndexWriterConfig(LUCENE_41,analyzer);
        
        HashMap<String,HashSet<Integer>> word_to_docs_map = new HashMap<String,HashSet<Integer>>();
        int n_strings = words.length; 
        List<Integer> indices_array; 
        indices_array = Arrays.asList(indices);
        
        
       
        for (int idx = 0; idx < n_strings; idx++) {
            
            String query = words[idx]; 
            QueryParser parser = new QueryParser(Version.LUCENE_41, "text", analyzer); 
            Query q = parser.parse(query); 

            IndexReader reader = DirectoryReader.open(dir);
            IndexSearcher searcher = new IndexSearcher(reader);
            TopDocs docs = searcher.search(q, reader.numDocs());
            ScoreDoc[] hits = docs.scoreDocs;
            
            // update map from word to docs it appears in 
            HashSet<Integer> tmp = null;
            word_to_docs_map.put(query, tmp);
            
            for(Integer i=0;i<hits.length;i++) {
                Integer docId = hits[i].doc;
                tmp = word_to_docs_map.get(query); 
                
                // if the document is a document written by an user of interest 
                if(indices_array.contains(docId)) {
                   tmp.add(docId);
                }
                word_to_docs_map.put(query, tmp); 
               
            }


    }
        

        
    // add edges 
    
    // iterate over possible term pairs 
    for(int idx_1 = 0; idx_1 < n_strings - 1; idx_1++) {
        
        for(int idx_2 = idx_1 + 1 ; idx_2 < n_strings ; idx_2 ++ ) {
            
            // extract document sets associated with the considered terms 
            HashSet set_a = word_to_docs_map.get(words[idx_1]); 
            HashSet set_b = word_to_docs_map.get(words[idx_2]); 
            
            // compute set intersection 
            int n = set_intersection(set_a, set_b);
            
            // if the terms appear in at least one common document
            if(n>0) {
               // add edge 
               g.testAndAdd(idx_1, idx_2 , n); 
            }
 
        }
        
        
    }

        return g ;    
    
}
    
}