package otherdata.d4d.ophi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import region.CreatorRegionMapFromGIS;
import region.Region2Region;
import region.RegionI;
import region.RegionMap;
import utils.Config;
import utils.CopyAndSerializationUtils;

public class Ophi {
	
	private Map<String,Double> depriv = new HashMap<>();
	
	public Ophi(String file) {
		this(file,null);
	}
	
	public Ophi(String file, Map<String,Set<String>> region2region) {
		
		try {	
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8"));
			//BufferedReader br = new BufferedReader(new FileReader(file));
			String line;
			while((line=br.readLine())!=null) {
				if(line.startsWith("//") || !line.contains(",")) continue;
				String[] e = line.split(",");
				
				String region = e[0];
				Set<String> regions = new HashSet<>();
				regions.add(region);
				
				if(region2region != null) {
					regions = region2region.get(region);
					if(regions == null) {
						System.out.println("OPHI: Cannot convert "+e[3]);
						continue;
					}	
				}
				
				for(String rx: regions) 
					depriv.put(rx, Double.parseDouble(e[e.length-1]));
			}
			br.close();
			
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public Map<String,Double> getDepriv() {
		return depriv;
	}
	
	public static void main(String[] args) throws Exception {
		//Ophi ophi = new Ophi("G:/DATASET/CENSUS/ophi/ophi-senegal.csv",Region2Region.region2region("G:/DATASET/GEO/senegal/senegal_subpref.csv","NAME_1","NAME_2"));
		//RegionMap rm = (RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/senegal_province.ser"));
		
		//Ophi ophi = new Ophi("G:/DATASET/CENSUS/ophi/ophi-senegal.csv");
		//RegionMap rm = (RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/senegal_regioni.ser"));
		
		//Ophi ophi = new Ophi("G:/DATASET/CENSUS/ophi/ophi-ivorycoast.csv");
		//RegionMap rm = (RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/ivorycoast_province.ser"));
		
		
		Ophi ophi = new Ophi("G:/DATASET/CENSUS/ophi/ophi-ivorycoast.csv",Region2Region.region2region("G:/DATASET/GEO/ivorycoast/ivorycoast_subpref.csv","NAME_2","NAME_1"));
		RegionMap rm = (RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/ivorycoast_regioni.ser"));
		
		for(String k: ophi.getDepriv().keySet()) {
			boolean found = false;
			for(RegionI r: rm.getRegions())
				if(r.getName().equals(k))
					found = true;
			if(!found) System.out.println("OPHI "+k+" not found");
			//else System.out.println("+++ OPHI "+k+" found");
		}
		System.out.println("-------------------------");
		
		for(RegionI r: rm.getRegions()) {
			boolean found = ophi.getDepriv().get(r.getName()) != null;
			if(!found) System.out.println("RM "+r.getName()+" not found");
			//else System.out.println("+++ RM "+r.getName()+" found");
		}
		for(RegionI r: rm.getRegions()) 
			System.out.println(r.getName());
		
		
	}
	
}
