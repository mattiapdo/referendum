package loadData;

import java.util.HashMap;
import java.util.stream.Stream;


public class Clusters {
	
	private int[] partition;
	private String[] allWords;
	private HashMap<Integer, String> mapIndexToWord;  // here the key ranges from 0 to (total number of cluterized words - 1)
	private int numOfClusters;
	
	private HashMap<Integer, Cluster> mapIndexToCluster; // here the key ranges from 0 to k-1
	
	Clusters(int[] partition, String[] allWords) {
		this.partition = partition;
		this.setAllWords(allWords);
		this.numOfClusters = Stream.of(partition).distinct().toArray().length;
		
		// fill mapIndexToWord
		for (int i=0; i<this.partition.length; i++) {
			this.mapIndexToWord.put(partition[i], allWords[i]);
		}
		
		// fill mapIndexToCluster
		for (int i=0; i<this.numOfClusters; i++) {
			this.mapIndexToCluster.put(i, new Cluster(i));
		}
		
		// fill the cluster objects that are inside mapIndexToCluster
		for (int i=0; i<this.partition.length; i++) {
			int currentIndex = this.partition[i];
			String currentWord = this.allWords[i];
			mapIndexToCluster.get(currentIndex).addWord(currentWord);
		}
		
		
		
	}

	public String[] getAllWords() {
		return allWords;
	}

	public void setAllWords(String[] allWords) {
		this.allWords = allWords;
	}

	public Cluster getCluster(int id) {
		return (this.mapIndexToCluster.get(id));
	}

	public int getNumOfClusters() {return this.numOfClusters;}
}
