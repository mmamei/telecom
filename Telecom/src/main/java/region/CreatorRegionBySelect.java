package region;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import utils.Config;
import utils.CopyAndSerializationUtils;

/*
 * This creator takes a RegionMap, select only some regions. and create a RegionMap with the selected regions
 * 
 * 
 */

public class CreatorRegionBySelect {
	
	
	
	
	public static void main(String[] args) throws Exception {
		
		Set<String> select = new HashSet<String>();
		
		select.add("TORINO");
		select.add("MILANO");
		select.add("VENEZIA");
		select.add("ROMA");
		select.add("BARI");
		select.add("NAPOLI");
		select.add("PALERMO");
		
		select.add("BENEVENTO");
		select.add("CALTANISSETTA");
		select.add("MODENA");
		select.add("RAVENNA");
		select.add("SIENA");
		select.add("SIRACUSA");
		select.add("ASTI");
		select.add("CAMPOBASSO");
		select.add("FERRARA");
		create(Config.getInstance().base_folder+"/RegionMap/prov2011.ser", select, Config.getInstance().base_folder+"/RegionMap/tic-prov2011.ser");
	
		System.out.println("Done");
	
	}
	
	
	public static void create(String input_region_map, Set<String> select, String output_region_map) throws Exception {
	
	
		RegionMap input = (RegionMap)CopyAndSerializationUtils.restore(new File(input_region_map));
		
		File out = new File(output_region_map);
		
		RegionMap rm = new RegionMap(out.getName().substring(0,out.getName().indexOf(".")));
		
		for(RegionI r: input.getRegions())
			if(select.contains(r.name))
				rm.add(r);
	
		rm.printKML();
		CopyAndSerializationUtils.save(out, rm);
	}
}
