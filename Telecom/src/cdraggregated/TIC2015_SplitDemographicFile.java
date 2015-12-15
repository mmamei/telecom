package cdraggregated;

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
		
		
		String[] grid_files = new String[]{
			Config.getInstance().dataset_folder+"/TI-CHALLENGE-2015/GRID2/Benevento.csv",
			Config.getInstance().dataset_folder+"/TI-CHALLENGE-2015/GRID2/Caltanissetta.csv",
			Config.getInstance().dataset_folder+"/TI-CHALLENGE-2015/GRID2/Modena.csv",
			Config.getInstance().dataset_folder+"/TI-CHALLENGE-2015/GRID2/Siena.csv",
			Config.getInstance().dataset_folder+"/TI-CHALLENGE-2015/GRID2/Siracusa.csv",
			Config.getInstance().dataset_folder+"/TI-CHALLENGE-2015/GRID2/Asti.csv",
			Config.getInstance().dataset_folder+"/TI-CHALLENGE-2015/GRID2/Campobasso.csv",
			Config.getInstance().dataset_folder+"/TI-CHALLENGE-2015/GRID2/Ferrara.csv",
			Config.getInstance().dataset_folder+"/TI-CHALLENGE-2015/GRID2/Ravenna.csv",
		};
		
		
		
		// create an hashmap associating the province (file) name to all the grid cell
		
		
		Map<String,String> gridcell2province = new HashMap<String,String>();
		for(String gf: grid_files) {
			File f = new File(gf);
			if(!f.exists()) {
				System.err.println(gf+" not found!");
				continue;
			}
			String province = f.getName().substring(0,f.getName().indexOf(".")).toLowerCase();
			
			BufferedReader br = new BufferedReader(new FileReader(f));
			String line;
			br.readLine(); // skip header
			while((line=br.readLine())!=null) {
				String gridcell = line.split("\t")[1];
				gridcell2province.put(gridcell, province);
			}
			br.close();
		}
		
		
		// create directories a nd files
		
		Map<String,PrintWriter> province2printwriter = new HashMap<String,PrintWriter>();
		for(String gf: grid_files) {
			File f = new File(gf);
			String province = f.getName().substring(0,f.getName().indexOf(".")).toLowerCase();
			File dir = new File(Config.getInstance().dataset_folder+"/TI-CHALLENGE-2015/DEMOGRAPHIC/"+province);
			dir.mkdirs();
			province2printwriter.put(province, new PrintWriter(new FileWriter(dir+"/callsLM_"+province.substring(0,2).toUpperCase()+"_CAP")));
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
