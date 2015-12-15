package cdraggregated;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import region.RegionI;
import region.RegionMap;
import utils.Config;
import utils.CopyAndSerializationUtils;

public class TIC2015_DatasetDemographic_CAPS_TO_COMUNI {
	static RegionMap rm_comuni2012 = (RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/comuni2012.ser"));
	static Map<String,String> cap2comuni = null;
	
	public static void main(String[] args) throws Exception {
		
		cap2comuni = cap2comuni();
		
		String[] cities = new String[]{
				//"caltanissetta",
				//"siracusa",
				//"benevento",
				//"palermo",
				//"campobasso",
				//"napoli",
				//"asti",
				//"bari",
				//"ferrara",
				//"venezia",
				//"torino",
				"ravenna",
				//"modena",
				//"roma",
				//"siena",
				//"milano"
		};
		
		
		
		for(String city: cities)
			process( Config.getInstance().dataset_folder+"/TI-CHALLENGE-2015/DEMOGRAPHIC/"+city+"/callsLM_"+city.substring(0,2).toUpperCase()+"_CAP");
		
		
		System.out.println("Done!");
	}
	
	
	
	
	public static void process(String caps_file) throws Exception {
		

		System.out.println("Processing..."+caps_file);
		
		int zero = 0;
		int found = 0;
		int not_found  = 0;
		Set<String> caps_not_found = new HashSet<String>();
		
		
		PrintWriter pw = new PrintWriter(new FileWriter(caps_file.replaceAll("_CAP", "_COMUNI2012")));
		
		BufferedReader br = new BufferedReader(new FileReader(caps_file));
		String line;
		while((line=br.readLine())!=null) {
			String[] e = line.split("\t");
			String cap = e[2];
			String comune = "0";
			
			if(!cap.equals("0")) {
				comune = cap2comuni.get(cap);
				if(comune == null) comune = cap2comuni.get(cap.substring(0, cap.length()-2)+"00");
				
				
				if(comune == null) {		
					caps_not_found.add(cap);
					not_found++;
					comune = "0";
				}
				else {
					found++;
					comune = comune.replaceFirst("^0+(?!$)", ""); // remove leading zeros
				}
			}
			else zero++;
			
			
			
			
			
			pw.println(e[0]+"\t"+e[1]+"\t"+comune+"\t"+e[3]);
			
			
			if(!comune.equals("0")) {
				RegionI r = rm_comuni2012.getRegion(comune);
				if(r == null)
					System.err.println(comune);
			}
		}
		
		br.close();
		pw.close();
		
		
		
		//for(String cap: caps_not_found)
		//	System.err.println(cap);
		
		int tot = found+not_found+zero;
		System.out.println("FOUND = "+(int)(100.0*found/tot)+"%");
		System.out.println("NOT_FOUND = "+(int)(100.0*not_found/tot)+"%");
		System.out.println("ZERO = "+(int)(100.0*zero/tot)+"%");
	}
	
	
	public static Map<String,String> cap2comuni() throws Exception {
		Map<String,String> cap2comuni = new HashMap<String,String>();
		
		
		BufferedReader br = new BufferedReader(new FileReader(Config.getInstance().dataset_folder+"/TI-CHALLENGE-2015/listacomuni.txt"));
		String line;
		while((line=br.readLine())!=null) {
			String[] e = line.split(";");
			cap2comuni.put(e[5].replaceAll("xx", "00"), e[0]);
		}
		br.close();
		
		
		
		RegionMap rm_caps = (RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/caps.ser"));
		for(RegionI r: rm_caps.getRegions()) {
			if(cap2comuni.get(r.getName()) == null) {
				//
				
				RegionI c =  rm_comuni2012.get(r.getLatLon()[1], r.getLatLon()[0]);
				if(c==null) {
					
					if(r.getName().equals("30121")) cap2comuni.put(r.getName(), "27042");	
					else if(r.getName().equals("74100")) cap2comuni.put(r.getName(), "73011");	
					else System.err.println(r.getName());
				}
				else cap2comuni.put(r.getName(), c.getName());	
			}	
		}
		return cap2comuni;
	}
	
	
	
}
