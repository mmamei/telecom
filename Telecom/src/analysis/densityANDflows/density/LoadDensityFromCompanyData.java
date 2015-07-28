package analysis.densityANDflows.density;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import region.RegionI;
import region.RegionMap;
import utils.AddMap;
import utils.Config;
import utils.CopyAndSerializationUtils;
import utils.Sort;
import visual.kml.KMLHeatMap;

public class LoadDensityFromCompanyData {
	/*
	micro: if number of employees <10 and (revenue <= 2 MLN euro or active <= 2 million euro)
	piccola: if number of employees <50 and (revenue <= 10 MLN euro or active <= EUR 10 million)
	media: If number of employees <250 and (revenue <= 50 MLN euro or active <= EUR 43 million)
	grande: If number of employees >250 
	*/
	
	
	
	public static void main(String[] args) throws Exception {
		String[] city = new String[]{"torino","milano","venezia","roma","napoli","bari","palermo"};
		for(String c: city)
			process(c, new String[]{"01-32","grande",""},10);
		System.out.println("Done");
	}	
		
	public static void process(String city, String[] COMPANY_CONSTRAINTS, int LIMIT) throws Exception {
		System.out.println("Process "+city);
		LinkedHashMap<String, Double> map = getInstance(city,COMPANY_CONSTRAINTS);
		
		Map<String,Double> limited_map = new HashMap<String,Double>();
		int c = 0;
		for(String k: map.keySet()) {
			limited_map.put(k, map.get(k));
			c++;
			if(c > LIMIT) break;
		}
		
		RegionMap rm = (RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/tic-"+city+"-gird.ser"));
		String suffix = (COMPANY_CONSTRAINTS == null) ? "" :  "-"+COMPANY_CONSTRAINTS[0]+"-"+COMPANY_CONSTRAINTS[1]+"-"+COMPANY_CONSTRAINTS[2];
		KMLHeatMap.drawHeatMap(Config.getInstance().base_folder+"/TIC2015/"+city+suffix+"-company.kml",limited_map,rm,"company",false);
		
	}
		
	public static LinkedHashMap<String, Double> getInstance(String city, String[] constraints) throws Exception {
		
		Map<String,String[]> desc = null;
		if(constraints!=null)
			desc = readCompanyDesc(); 
		
		RegionMap rm = (RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/tic-"+city+"-gird.ser"));
		AddMap map = new AddMap();
		
		BufferedReader br = new BufferedReader(new FileReader("G:/DATASET/TI-CHALLENGE-2015/CERVED/headquarters-full.csv"));
		String line;
		
		//subject_id;sign;long;lat;kind;area
		//361869449;;7.66839336;45.03530199;U;Torino
		
		while((line=br.readLine())!=null) {
			String[] e = line.split(";");
			if(e[5].toLowerCase().equals(city)) {
				
				if(desc != null) {
					String[] d = desc.get(e[0]);
					
					if(d == null) continue;
					if(constraints[0].length()>1) {
						int min = Integer.parseInt(constraints[0].split("-")[0]);
						int max = Integer.parseInt(constraints[0].split("-")[1]);
						int ateco = d[0].equals("n.d.")? -1 : Integer.parseInt(d[0]);
						if(ateco < min || ateco > max) continue;
					}
					if(constraints[1].length()>1)
						if(!d[1].equals(constraints[1])) continue;
					if(constraints[2].length()>1)
						if(!d[2].equals(constraints[2])) continue;
				}
				double lon = Double.parseDouble(e[2]);
				double lat = Double.parseDouble(e[3]);
				
				for(RegionI r: rm.getRegions()) {
					double[][] lonLatBbox = r.getBboxLonLat();
					if(lonLatBbox[0][0] <= lon && lon <= lonLatBbox[1][0] &&
					   lonLatBbox[0][1] <= lat && lat <= lonLatBbox[1][1]) {
						map.add(r.getName(), 1);
					}
				}
			}
		}
		br.close();
		
		double max = 0;
		for(double d: map.values())
			max = Math.max(max, d);
			
		for(String k: map.keySet())
			map.put(k, map.get(k)/max);
		
		
		
		LinkedHashMap<String, Double> sorted = Sort.sortHashMapByValuesD(map,Collections.reverseOrder());
		
		
		return sorted;
	}	
	
	
	public static Map<String,String[]> readCompanyDesc() throws Exception {
		
		Map<String,String[]> desc = new HashMap<String,String[]>();
		
		BufferedReader br = new BufferedReader(new FileReader("G:/DATASET/TI-CHALLENGE-2015/CERVED/cerved-companies.csv"));
		String line;
		br.readLine(); // skip header
		while((line=br.readLine())!=null) {
			String[] e = line.split(",");
			desc.put(e[0], new String[]{e[e.length-3],e[e.length-2],e[e.length-1]});
		}
		br.close();
		
		return desc;
		
	}
}
