package prove;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class Aula {
	
	private int num_aula;
	private LinkedHashMap<String, Alunno> alunni = new LinkedHashMap<>();
	
	public void addAlunno(String name, int age) {
		alunni.put(name, new Alunno(name, age));
	}
	
	public Map<String, Alunno> getAlunni(){
		return alunni;
	}
	
	public int getNum_aula() {
		return num_aula;
	}
	public void setNum_aula(int num_aula) {
		this.num_aula = num_aula;
	}
	
	

}
