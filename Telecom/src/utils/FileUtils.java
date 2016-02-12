package utils;

import java.io.BufferedReader;
import java.io.FileReader;

public class FileUtils {
	
	public static int lines(String file) {
		
		try {
			int count = 0;
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;
			while((line = br.readLine()) != null)
				if(!line.startsWith("//"))
					count++;
			br.close();
			return count;
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}
	
}
