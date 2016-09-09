package utils;

import java.io.BufferedReader;
import java.io.FileReader;

public class FileUtils {

	public enum HowToDealWithFileHeader {NO_HEADER,ONE_LINE,FIFTEEN_LINES,UNTIL_FIRST_EMPTY};

	public static int lines(String file, HowToDealWithFileHeader header) {
		
		try {
			int count = 0;
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;
			skipHeader(br,header);
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
	
	public static void skipHeader(BufferedReader br, HowToDealWithFileHeader h) {
		try {
			switch (h) {
			case NO_HEADER: 
				break;
			case ONE_LINE:
				br.readLine();
				break;
			case FIFTEEN_LINES:
				for(int i=0;i<15;i++)	
					br.readLine();
				break;
			case UNTIL_FIRST_EMPTY:
				while (br.readLine().trim().length() > 0);
				break;
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
}
