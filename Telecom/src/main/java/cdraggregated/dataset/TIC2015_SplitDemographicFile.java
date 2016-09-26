package cdraggregated.dataset;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import utils.Config;

/*
 * This class splits a single data file containing data about multiple provinces, into multiple fies one for each province.
 */

public class TIC2015_SplitDemographicFile {

	public static void main(String[] args) throws Exception {
		
		
		
		String[] cities = new String[]{
				"caltanissetta",
				"siracusa",
				"benevento",
				"campobasso",
				"asti",
				"ravenna",
				"ferrara",
				"modena",
				"siena",	
		};
		
		
		
		// create an hashmap associating the province (file) name to all the grid cell
		
		
		Map<String,String> gridcell2province = new HashMap<String,String>();
		for(String city: cities) {
			File f = new File(Config.getInstance().dataset_folder+"/TI-CHALLENGE-2015/GRID/tic-"+city+"-grid.csv");
			if(!f.exists()) {
				System.err.println(f+" not found!");
				continue;
			}
			
			
			BufferedReader br = new BufferedReader(new FileReader(f));
			String line;
			br.readLine(); // skip header
			while((line=br.readLine())!=null) {
				String gridcell = line.split("\t")[1];
				gridcell2province.put(gridcell, city);
			}
			br.close();
		}
		
		
		// create directories  and files
		
		Map<String,PrintWriter> province2printwriter = new HashMap<String,PrintWriter>();
		for(String city: cities) {
			File dir = new File(Config.getInstance().dataset_folder+"/TI-CHALLENGE-2015/DEMOGRAPHIC/"+city);
			dir.mkdirs();
			province2printwriter.put(city, new PrintWriter(new FileWriter(dir+"/callsLM_"+city.substring(0,2).toUpperCase()+"_CAP")));
		}
		
		
		// read file and split
		
		String input_file = Config.getInstance().dataset_folder+"/TI-CHALLENGE-2015/NEWCAP/Social_capital_CAP.csv";
		BufferedReader br = new BufferedReader(new FileReader(input_file));
		String line;
		
		long not_found = 0;
		long found = 0;
		
		while((line=br.readLine())!=null) {
			
			String[] elements = line.split("\t");
			
			long time = Long.parseLong(elements[0]) / 1000; // time conversion to make it compatible with other telecom challenge data
			
			String gridcell = elements[1];
			String province = gridcell2province.get(gridcell);
			
			if(province == null) {
				not_found++;
				continue;
			}
			found ++;
			PrintWriter out = province2printwriter.get(province);
			
			out.print(time);
			for(int i=1;i<elements.length;i++)
				out.print("\t"+elements[i]);
			out.println();
			
		}
		br.close();
		
		
		System.out.println("Found = "+found);
		System.out.println("Not Found = "+not_found);
		
		// close all the printwriter
		for(PrintWriter out: province2printwriter.values())
			out.close();
	}
	
}
