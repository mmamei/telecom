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
				out.println(r.toKml(Colors.KML_RANDOM_COLORS[cluster],"44aaaaaa"));
			}
		}
	
		
		kml.printFooterFolder(out);
		out.close();
	}
	
	public static void drawColorMap(String file, Map<String,Integer> assignments, RegionMap rm , Map<String,String> desc) throws Exception {
		
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file)));
		KML kml = new KML();
		kml.printHeaderFolder(out, rm.getName());
		
		int cont = 0;
		for(RegionI r: rm.getRegions()) {
			Integer cluster = cont;
			if(assignments != null)
				cluster = assignments.get(r.getName());
			if(cluster != null) {
				r.setDescription(desc.get(r.getName()));
				out.println(r.toKml(Colors.KML_RANDOM_COLORS[cluster%Colors.KML_RANDOM_COLORS.length],"44aaaaaa"));
			}
			cont++;
		}
	
		
		kml.printFooterFolder(out);
		out.close();
	}
	
	
	public static Map<String,Integer> toIntAssignments(Map<String,String> m) {
		int cont = 0;
		Map<String,Integer> value2int = new HashMap<>();
		for(String x: m.values())
			if(value2int.get(x) == null)
				value2int.put(x, cont++);
		
		Map<String,Integer> assignments = new HashMap<>();
		for(String k: m.keySet())
			assignments.put(k, value2int.get(m.get(k)));
		
		return assignments;
	}
	
	
}
