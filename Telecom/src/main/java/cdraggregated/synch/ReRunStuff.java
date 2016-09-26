package cdraggregated.synch;

import java.util.ArrayList;
import java.util.List;

import region.CreatorRegionMapFromGIS;

public class ReRunStuff {
	
	
	/*
	 * this reruns stuff useful for this package, RegionMaps. etc.
	 */
	
	public static void main(String[] args) throws Exception {
		
		
		List<String> cities = new ArrayList<>();
		cities.add("napoli");
		cities.add("bari");
		cities.add("caltanissetta");
		cities.add("siracusa");
		cities.add("benevento");
		cities.add("palermo");
		cities.add("campobasso");
		cities.add("roma");
		cities.add("siena");
		cities.add("ravenna");
		cities.add("ferrara");
		cities.add("modena");
		cities.add("venezia");
		cities.add("torino");
		cities.add("asti");
		cities.add("milano");
		
		for(String city:cities) {
			String name = "tic-"+city+"-grid";
			String input_file = "G:/DATASET/TI-CHALLENGE-2015/GRID/tic-"+city+"-grid.csv";
			String output_obj_file = "C:/BASE/RegionMap/tic-"+city+"-grid.ser";
			CreatorRegionMapFromGIS.processWTK(name, input_file, output_obj_file, new int[]{1});
		}
	}
	
}
