package region;

import java.io.File;

import utils.Config;
import utils.CopyAndSerializationUtils;

/*
 * Given two region maps, this class creates a region map that is the intersection of the twos
 */

public class CreatorRegionByIntersection {
	
	public static void main(String[] args) throws Exception {
		String[] cities = new String[]{"venezia","milano","torino","napoli","roma","palermo","bari"};
		for(String city: cities)
			process(city,"caps");
		System.out.println("Done");
	}
	
	public static void process(String name1, String name2) throws Exception {

		RegionMap rm1 = (RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/tic-"+name1+"-gird.ser"));
		RegionMap rm2 = (RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/"+name2+".ser"));
		
		RegionMap rm = new RegionMap(name1+"-"+name2);
		
	
		for(RegionI r1: rm1.rm.values())
			for(RegionI r2: rm2.getOverlappingRegions(r1.getGeom())) 
				rm.add(r2);
		
		rm.printKML();
		
		CopyAndSerializationUtils.save(new File(Config.getInstance().base_folder+"/RegionMap/tic-"+rm.name+".ser"), rm);
		
	}
	
}
