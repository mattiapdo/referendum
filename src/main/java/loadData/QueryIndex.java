/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package loadData;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.store.SimpleFSDirectory;

/**
 *
 * @author marti
 */
public class QueryIndex {
    
    
		public static void main(String [] args) throws IOException {
			
			
			// directory.open(indexPath)
			Directory dir; 
                        dir = new SimpleFSDirectory(new File("D:/lucene_index"));
			
			// open a directory
			IndexReader ir = DirectoryReader.open(dir);
                        
                        System.out.println("Number of documents: " + ir.numDocs());
			
			// create index searcher
			IndexSearcher searcher = new IndexSearcher(ir);	
                    
                     
                        String[] times = new String[ir.numDocs()];
                        
                        
                        Set<String> ids = new HashSet<String>(); 

                        
                        for (int i=0; i<ir.maxDoc(); i++) {
                             Document doc = ir.document(i);
                             String userid = doc.get("userid");
                             ids.add(userid); 
                             String time = doc.get("time"); 
                             times[i] = time; 
                        }
                        

                        System.out.println("Number of users: " + ids.size());     

                        System.out.println("Distribution over time: "); 

                        List asList = Arrays.asList(times);
                        
                        Set<String> times_set = new HashSet<String>(asList);

                        for(String s: times_set){

                         System.out.println(s + " " + Collections.frequency(asList,s));

                        }

                    
			// close index reader
			ir.close();
       
		}
    
}
