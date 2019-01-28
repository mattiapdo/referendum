package loadData;

import java.util.ArrayList;

public class Cluster {
	
	private int id;
	private ArrayList<String> words;
	
	
	public Cluster(int id) {
		this.setId(id);
		this.setWords(new ArrayList<>());
	}


	public int getId() {
		return id;
	}


	public void setId(int id) {
		this.id = id;
	}


	public ArrayList<String> getWords() {
		return words;
	}


	public void setWords(ArrayList<String> words) {
		this.words = words;
	}

	public void addWord(String word) {
		this.words.add(word);
	}
}
