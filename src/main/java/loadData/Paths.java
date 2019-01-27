package loadData;
import java.io.File;
import java.io.IOException;


public class Paths {

	public static void main(String[] args) throws IOException {
		System.out.println(new File(".").getCanonicalPath() + "\\data");
		
		File encyptFile=new File(".\\data\\users.csv");
		System.out.println(encyptFile.exists());
		
	}

}
