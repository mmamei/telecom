package otherdata.TIbigdatachallenge2015;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

import region.RegionI;
import region.RegionMap;
import utils.Config;
import utils.CopyAndSerializationUtils;
import visual.kml.KMLHeatMap;


public class SocialCapital {
	
	private Map<String,Double> assoc;
	private Map<String,Double> referendum;
	private Map<String,Double> blood;
	
	private static SocialCapital instance = null;
	
	public static SocialCapital getInstance() {
		if(instance == null)
			instance = new SocialCapital();
		return instance;
	}
	
	private SocialCapital() {
		try {
			
			assoc = new HashMap<String,Double>();
			referendum = new HashMap<String,Double>();
			blood = new HashMap<String,Double>();
			
			RegionMap rm = (RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/prov2011.ser"));
			
			BufferedReader br = new BufferedReader(new FileReader(Config.getInstance().dataset_folder+"/TI-CHALLENGE-2015/ISTAT/dati_social_capital.csv"));
			String line;
			br.readLine(); // skip header
			while((line=br.readLine())!=null) {
				String[] e = line.split(";");
				String name = e[2];
				String prov_name = null;
				for(RegionI r: rm.getRegions()) {
					if(r.getName().toLowerCase().endsWith(name.toLowerCase()))
						prov_name = r.getName();
				}
				
				//if(prov_name == null)
				//	System.out.println("------ SocialCapital:52 --- "+name);
				
				
				assoc.put(prov_name, Double.parseDouble(e[3]));
				referendum.put(prov_name, Double.parseDouble(e[4]));
				blood.put(prov_name,Double.parseDouble(e[5]));
			}
			br.close();
			
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public Map<String,Double> getAssoc() {
		return assoc;
	}
	
	public Map<String,Double> getReferendum() {
		return referendum;
	}
	
	public Map<String,Double> getBlood() {
		return blood;
	}
	
	public static void main(String[] args) throws Exception {
		SocialCapital sc = SocialCapital.getInstance();
		RegionMap rm = (RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/prov2011.ser"));
		rm.setName("social-capital-blood");
		Map<String,Double> blood = sc.getBlood();
		KMLHeatMap.drawHeatMap(Config.getInstance().base_folder+"/TIC2015/"+rm.getName()+".kml",blood,rm,"",false);
	}
	
}
