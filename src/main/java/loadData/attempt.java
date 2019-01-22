/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package loadData;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Iterator;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;


/**
 *
 * @author marti
 */
public class attempt {
   
    
public static void main(String [ ] args)
{
    
    
    File file = new File("C:\\Users\\marti\\Desktop\\Social\\stream");
    String[] directories = file.list(new FilenameFilter() {
    public boolean accept(File current, String name) {
        return new File(current, name).isDirectory();
  }
});
   
    
    for (String directory : directories) {

     Iterator it = FileUtils.iterateFiles(new File("C:\\Users\\marti\\Desktop\\Social\\stream\\" + directory), null, false);
       while(it.hasNext()){
           System.out.println("C:\\Users\\marti\\Desktop\\Social\\stream\\" + directory + "\\" +  ((File) it.next()).getName());
     }
    
 
}
    
}

}