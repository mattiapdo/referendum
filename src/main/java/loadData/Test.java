package loadData;

import java.util.ArrayList;
import java.util.Arrays;

public class Test {
	
	public static void main(String[] args) {
		int[] partition = {0, 1, 0, 0, 0, 1, 2, 1, 2, 0, 2, 2, 1};
		//String[] allWords1 = {"casa", "vacanza", "bagno", "cucina", "letto", "gita", "computer", "riposo", "portatile", "porta", "schermo", "tastiera", "hotel"};
		String[] allWords = {"rt", "https", "t.co", "per", "il", "e", "di", "bastaunsi", "con", "che", "non", "i", "matteorenzi"};
		
		Clusters clusters = new Clusters(partition, allWords);
        System.out.println("found " + clusters.getNumOfClusters() + " clusters");
        
        // Co-occurrence graphs
        // for each cluster...
        for(int i=0; i<clusters.getNumOfClusters(); i++) {
	        Cluster cluster = clusters.getCluster(i);
	        ArrayList<String> clusterWords = cluster.getWords();
	        String[] words = clusterWords.toArray(new String[0]);
	        System.out.println("\t Cluster number "+ i + " contains:" + Arrays.toString(words));
	        
	        System.out.println("Creating co-occurrence graph on cluster " + i + "...");
	        Co_Occurence_Graph CoOcc = new Co_Occurence_Graph(words, Y.getDocs());
	        WeightedUndirectedGraph g = CoOcc.getGraph();
        }
        
        
        System.out.println("All done");
	}

}
