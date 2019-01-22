/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package loadData;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.seninp.jmotif.sax.SAXException;
import net.seninp.jmotif.sax.SAXProcessor;
import net.seninp.jmotif.sax.alphabet.NormalAlphabet;
import net.seninp.jmotif.sax.datastructure.SAXRecords;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.codecs.TermStats;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.DocsEnum;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.CollectionStatistics;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.TermStatistics;
import org.apache.lucene.search.similarities.BasicStats;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.BytesRef;

/**
 *
 * @author marti
 */
public class BuldSax {
    
    public static void main(String [] args) throws IOException, SAXException  {
    // TODO Auto-generated method stub
    String iNDEX_DIR2 = "D:/lucene_index"; 
    System.out.println("INDEX_DIR:" + iNDEX_DIR2);
    SimpleFSDirectory dir = new SimpleFSDirectory(new File("D:/lucene_index"));
    IndexReader reader  = DirectoryReader.open(dir);
                       
    int num_doc = reader.numDocs();
    System.out.println("number of docs: " + String.valueOf(num_doc));
    
    
    Fields fields = MultiFields.getFields(reader); 
    Terms terms = fields.terms("text"); 
    TermsEnum termsIterator = terms.iterator(null);
      
    BytesRef BytesRef = null;  
    // For every term in the "Text" Field:
    while ((BytesRef = termsIterator.next()) != null) {
    String t = new String(BytesRef.bytes, BytesRef.offset,
    BytesRef.length);
     if ((t.length() > 2) && (t.length() < 15) && StringUtils.isAlpha(t) && (termsIterator.docFreq() > 15)) { 
        System.out.println(t); 
     }
    }
    

        /*
        Map<String, Integer> frequencies = new HashMap<>();
        Set<String> terms_set = new HashSet<>();
        BytesRef text = null;
        while ((text = termsIterator.next()) != null) {
        String term = text.utf8ToString();
        int freq = (int) termsIterator.totalTermFreq();
        System.out.println(freq); 
        frequencies.put(term, freq);
        terms_set.add(term);
        }
         */ 
    
    
      
    int alphabetSize = 2;
    double nThreshold = 0.01; 
    
    // instantiate classes 
    
    NormalAlphabet na = new NormalAlphabet();
    SAXProcessor sp = new SAXProcessor();
    
    double[] ts = {10, 20, 20, 50, 80, 10, 50, 80, 10, 5};             

    
    // perform the discretization
    SAXRecords res = sp.ts2saxByChunking(ts, ts.length,
    na.getCuts(alphabetSize), nThreshold); 
    
    String sax = res.getSAXString("");
    System.out.println(sax); 
    System.out.println(sax.matches("a+b+a*b*a*")); 
              
    }
    
    
    


}

