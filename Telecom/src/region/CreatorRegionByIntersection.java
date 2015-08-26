package region;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import utils.Config;
import utils.CopyAndSerializationUtils;

import com.vividsolutions.jts.geom.Geometry;

/*
 * Given two region maps, this class creates a region map that is the intersection of the twos
 */

public class CreatorRegionByIntersection {
	/*
	public static void main(String[] args) throws Exception {
		String[] cities = new String[]{"venezia","milano","torino","napoli","roma","palermo","bari"};
		for(String city: cities)
			process(city,"caps");
		System.out.println("Done");
	}
	
	public static void process(String name1, String name2) throws Exception {

		RegionMap rm1 = (RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/tic-"+name1+"-gird.ser"));
		RegionMap rm2 = (RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/"+name2+".ser"));
		
		RegionMap rm = new RegionMap("tic-"+name1+"-"+name2);
		
		for(RegionI r1: rm1.rm.values())
			for(RegionI r2: rm2.getOverlappingRegions(r1.getGeom())) 
				rm.add(r2);
		
		rm.printKML();
		
		CopyAndSerializationUtils.save(new File(Config.getInstance().base_folder+"/RegionMap/"+rm.name+".ser"), rm);
	}
	*/
	
	public static final double BUFFER = 0.05;
	
	public static void main(String[] args) throws Exception {
		String base_com = "comuni2014";
		RegionMap comuni = (RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/"+base_com+".ser"));
		RegionMap provincie = (RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/prov2011.ser"));
		
		RegionMap rm = new RegionMap("tic-"+base_com);
		
		
		Map<String,Geometry> infl_prov = new HashMap<String,Geometry>();
		for(RegionI prov: provincie.rm.values()) {
			//System.out.println(prov.getName());
			infl_prov.put(prov.getName().toLowerCase(),prov.getGeom().buffer(BUFFER));
		}
		
		Map<String,RegionMap> rm_comuniXprovincia = new HashMap<String,RegionMap>();
		for(String prov_name: infl_prov.keySet()) {
			rm_comuniXprovincia.put(prov_name, new RegionMap("tic-"+base_com+"-"+prov_name));
		}
		
		for(RegionI comune: comuni.rm.values())
		for(String prov_name: infl_prov.keySet()) {
			Geometry prov_geom = infl_prov.get(prov_name);
			if(prov_geom.covers(comune.getGeom())) {
				rm.add(comune);
				rm_comuniXprovincia.get(prov_name).add(comune);
			}
		}
		
		rm.printKML();
		CopyAndSerializationUtils.save(new File(Config.getInstance().base_folder+"/RegionMap/"+rm.name+".ser"), rm);
		
		
		for(RegionMap rm_cpp: rm_comuniXprovincia.values()) {
			rm_cpp.printKML();
			CopyAndSerializationUtils.save(new File(Config.getInstance().base_folder+"/RegionMap/"+rm_cpp.name+".ser"), rm_cpp);
			
		}
		
		System.out.println("Done");
	}
}
