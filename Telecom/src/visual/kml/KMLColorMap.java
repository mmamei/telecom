package visual.kml;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import cdrindividual.dataset.impl.DataFactory;
import region.RegionI;
import region.RegionMap;
import utils.Colors;
import utils.Config;




public class KMLColorMap {
	
	
	public static void drawColorMap(String file, Map<String,Integer> assignments, RegionMap rm , String desc) throws Exception {
		
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file)));
		KML kml = new KML();
		kml.printHeaderFolder(out, rm.getName());
		
		for(RegionI r: rm.getRegions()) {
			
			Integer cluster = assignments.get(r.getName());
			if(cluster != null) {
				r.setDescription(desc+" CLUSTER = "+cluster);
				out.println(r.toKml(Colors.RANDOM_COLORS[cluster],"44aaaaaa"));
			}
		}
	
		
		kml.printFooterFolder(out);
		out.close();
	}
	
	public static void drawColorMap(String file, Map<String,Integer> assignments, RegionMap rm , Map<String,String> desc) throws Exception {
		
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file)));
		KML kml = new KML();
		kml.printHeaderFolder(out, rm.getName());
		
		for(RegionI r: rm.getRegions()) {
			
			Integer cluster = assignments.get(r.getName());
			if(cluster != null) {
				r.setDescription(desc.get(r.getName()));
				out.println(r.toKml(Colors.RANDOM_COLORS[cluster],"44aaaaaa"));
			}
		}
	
		
		kml.printFooterFolder(out);
		out.close();
	}
	
	
}
