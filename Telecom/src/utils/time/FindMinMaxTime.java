package utils.time;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Date;

import utils.Config;

public class FindMinMaxTime {

	public static void main(String[] args) throws Exception {
		
		String input_file = Config.getInstance().dataset_folder+"/TI-CHALLENGE-2015/NEWCAP/Social_capital_CAP.csv";
		//String input_file = Config.getInstance().dataset_folder+"/TI-CHALLENGE-2015/DEMOGRAPHIC/palermo/callsLM_PA_CAP";
		
		
		BufferedReader br = new BufferedReader(new FileReader(input_file));
		String line;
		
		
		long minTime = -1;
		long maxTime = -1;
		
		while((line=br.readLine())!=null) {
			long time = Long.parseLong(line.split("\t")[0]);
			if(minTime == -1 || time < minTime) minTime = time;
			if(maxTime == -1 || time > maxTime) maxTime = time;
		}
		br.close();
		
		
	    System.out.println(new Date(minTime));
	    System.out.println(new Date(maxTime));
		
	}
	
}
