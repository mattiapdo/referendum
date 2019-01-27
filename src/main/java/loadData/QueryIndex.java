/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package loadData;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import static java.util.stream.Collectors.toMap;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.BytesRef;

import it.stilo.g.structures.WeightedDirectedGraph;
import net.seninp.jmotif.sax.SAXException;

/**
 *
 * @author marti
 */
public class QueryIndex {
    
    
		public HashMap<String,ArrayList<Long>> takeTimeStamps(HashSet<String> target_names) throws IOException {
			/* This procedure accesses the index and for the users of interest , it creates a map associating 
           	the most frequent 1000 terms to the array of timestamps in which a Tweet containing such term is shared. 
            Input: HashSet storing target names 
            Output: HashMap mapping selected words to array of times 
            */	
			
			// directory.open(indexPath)
			Directory dir; 
			dir = new SimpleFSDirectory(new File(".\\data\\lucene_index_r"));
			
			// open a directory
			IndexReader ir = DirectoryReader.open(dir);            
            System.out.println("Number of documents: " + ir.numDocs());
			
            // initialize objects to store id frequency and time maps 
            Set<String> ids = new HashSet<String>(); 
            Long l = new Long(30);
            HashMap<String, Long> freq_map =  new HashMap<>();
            HashMap<String,ArrayList<Long>> times_map = new HashMap<String,ArrayList<Long>>();    
           
            ArrayList<Integer> boss_doc = new ArrayList<Integer>();
            
            // iterate all documents 
            for (int i=0; i<ir.maxDoc(); i++) {
                
                 Terms terms = ir.getTermVector(i, "text");  
                 TermsEnum iterator = terms.iterator(null); 
                 BytesRef term; 
                 String termText;
                 Long termFreq; 
                 Document doc = ir.document(i);
                 String name = doc.get("UserSN"); 
                 
                 // if the user corresponding to the document is of interest 
                 if(target_names.contains(name)) {

                    // iterate all the terms in the document 
                    while((term = iterator.next())!=null) {

                        termText = term.utf8ToString();
                        termFreq = iterator.totalTermFreq();

                        if(!freq_map.containsKey(termText)) {
                        	
                        	boss_doc.add(i); // aggiungi il doc a quelli dei boss

                            //update frequency 
                            freq_map.put( termText, termFreq);
                            //update times
                            List<Long> list = new ArrayList<>();
                            Long n = l.valueOf(doc.get("time")); 
                            list.add(n); 
                            times_map.put(termText, (ArrayList<Long>) list); 

                        } else {

                            //update frequency
                            freq_map.put( termText, freq_map.get(termText) + termFreq ); 
                            // store times here as a dictionary to list 

                            //update times  
                            ArrayList<Long> new_list; 
                            new_list = times_map.get(termText);
                            Long n = l.valueOf(doc.get("time")); 
                            new_list.add(n); 
                            times_map.put(termText, new_list);
                            
                        }
                    }

                    String userid = doc.get("userid");                             
                    ids.add(userid); 

                }
                             
            }
                             
            //}
                        
             // get 1000 highest frequency words         
             
             //sort the array in decreasing order of frequency 
             LinkedHashMap<String, Long> sorted = freq_map.entrySet().stream().sorted(Collections.reverseOrder(Map.Entry.comparingByValue())).collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
                            LinkedHashMap::new));
                
             
             //create map from the 1000 most frequent words to timestamp lists 
            HashMap<String,ArrayList<Long>> reduced_map = new HashMap<String,ArrayList<Long>>();    
 
            // intitialize counter 
            int cnt = 0; 
            Iterator it = sorted.entrySet().iterator();
            // stop when 1000 words are included in @reduced_map 
            while(cnt<1000 && it.hasNext()) {

              Map.Entry pair = (Map.Entry)it.next();  

              //update reduced_map adding a key value pair 
              String k = (String) pair.getKey(); 
              reduced_map.put(k, times_map.get(k) ); 

              //update counter 
              cnt++; 

            }

                   //return  
            return reduced_map;
            
   }
		
		public static void main(String [ ] args) throws FileNotFoundException, IOException, SAXException {
			
			IndexReader ir = DirectoryReader.open(new SimpleFSDirectory(new File(".\\data\\lucene_index_r")));
			
			HashSet<String> most_important_terms = new HashSet<String>();
			most_important_terms.add("e");			// parole a buffo che uso come "parole importanti (basically perchè appaiono nei primi documenti.. per vedere che funziona)"
			most_important_terms.add("dei");
			most_important_terms.add("amici16");
			
			Map<String, Integer> vert_ind = new HashMap<String, Integer>();
			
			// associate an ID to each important term (integer number ranging from 0 to 1000 - size of the list)
			Integer ind =  new Integer(0);
			for(String imp_term: most_important_terms) {
				vert_ind.put(imp_term, ind);
				ind ++;
			}
			
			// create the graph with the top 1000 terms as vertexes
			WeightedDirectedGraph g = new WeightedDirectedGraph(most_important_terms.size());
						
			// per ogni documento... (da migliorare: per ogni documento scritto dai boss -  quindi prendi una lista di indici)
			for (int i=0; i<10; i++) {
	            Terms terms = ir.getTermVector(i, "text");
	            TermsEnum iterator = terms.iterator(null);
	            BytesRef term;  String termText;
	            
	            // aggiungi in un set tutti i termini che trovi nel doumento; poi (fuori dal ciclo) tieni solo le parole importanti
	            HashSet<String> result = new HashSet<String>();
	            while ((term = iterator.next())!= null) { 
	               termText = term.utf8ToString();
			       //System.out.println(termText);
	               result.add(termText);
	            }
	            result.retainAll(most_important_terms);  // result contiene ogni volta i termini importanti nel singolo documento
	            
	            
			    //
	            //HashMap<String, HashSet<String>> links = new HashMap<String, HashSet<String>>();
	            
			    WeightedDirectedGraph small_g = new WeightedDirectedGraph(result.size()); // piccolo grafo per tenere traccia delle coppie di parole già viste..
			    System.out.println(Arrays.toString(small_g.V));
			    //System.out.println(Arrays.deepToString(small_g.out));
			    System.out.println("Size of the graph " + small_g.size)	;
			    // per ogni coppia di termini (importanti)
			    for (String term_a : result) {
			    	int a = vert_ind.get(term_a);
			        System.out.println(term_a + "\t"+ a);
			        for (String term_b : result) {
			        	int b = vert_ind.get(term_b);
			        	System.out.println(term_b + "\t"+ b);
			        	
			        	/*if (!links.get(term_a).contains(term_b) && !links.get(term_b).contains(term_a) && ! term_a.equals(term_b)) {	// se a non è già collegato con b
			        					// .. allora aggiungi b ai link di a
			        	}*/
			        	
			        	if ( !(small_g.get( a, b ) > -1) && a != b){ // se i due termini non sono collegati localmente e sono diversi
			        		small_g.add(a, b, 1);
			        		if(g.get(a, b) == -1) g.testAndAdd(a, b, 1);
			        		else g.update(a, b, g.get(a, b)+1);
			        		
			        	}
			        }
			    }
			    
			    
			}
            
		
			
		}
		
                
}
                  
		
    