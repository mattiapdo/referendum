/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package loadData;

import java.io.IOException;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexableField;


/**
 *
 * @author marti
 */
public class IndexCreator {
    
 
    
    public static void addDoc(IndexWriter writer , String userSN , Long userid, String text, Long time) throws IOException {
        
        // document instatiation 
        Document doc = new Document(); 
        
        // doc.add(new TextField("text", text, Field.Store.NO )); 
        FieldType type = new FieldType();
        type.setIndexed(true);
        type.setStored(false); //not to make index too heavy 
        type.setStoreTermVectors(true);
        Field field = new Field("text", text, type);
        
        //list of fields 
        doc.add(new StringField("UserSN", userSN, Field.Store.YES)); 
        doc.add(new LongField("userid", userid, Field.Store.YES)); 
        doc.add(field); 
        doc.add(new LongField("time", time, Field.Store.YES )); 
        
        writer.addDocument((Iterable<? extends IndexableField>) doc); 
    }
    
    
    
    
    
}