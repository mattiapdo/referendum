/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package loadData;

import java.awt.TextField;
import java.io.IOException;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import static org.apache.lucene.util.Version.LUCENE_41;

/**
 *
 * @author marti
 */
public class index_creator {
    
 
    
    public static void addDoc(IndexWriter writer , String userSN , Long userid, String text, Long time) throws IOException {
        
        // document instatiation 
        Document doc = new Document(); 
        
        //list of fields 
        doc.add(new StringField("UserSN", userSN, Field.Store.YES)); 
        doc.add(new LongField("userid", userid, Field.Store.YES)); 
        doc.add(new StringField("text", text, Field.Store.NO )); 
        doc.add(new LongField("time", time, Field.Store.YES )); 
        
        writer.addDocument((Iterable<? extends IndexableField>) doc); 
    }
    
    
    
    
    
}
