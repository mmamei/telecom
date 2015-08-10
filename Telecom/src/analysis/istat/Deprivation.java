package analysis.istat;

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


public class Deprivation {
	
	private Map<String,Double> depriv;
	
	private static Deprivation instance = null;
	
	public static Deprivation getInstance() {
		if(instance == null)
			instance = new Deprivation();
		return instance;
	}
	
	private Deprivation() {
		try {
			
			depriv = new HashMap<String,Double>();
			
			RegionMap rm = (RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/regioni.ser"));
			
			BufferedReader br = new BufferedReader(new FileReader(Config.getInstance().dataset_folder+"/TI-CHALLENGE-2015/ISTAT/deprivation.csv"));
			String line;
			br.readLine(); // skip header
			while((line=br.readLine())!=null) {
				String[] e = line.split(";");
				String name = e[0];
				String prov_name = null;
				for(RegionI r: rm.getRegions()) {
					if(r.getName().toLowerCase().startsWith(name.toLowerCase()))
						prov_name = r.getName();
				}
				
				if(prov_name == null)
					System.out.println(name);
				
				
				depriv.put(prov_name, Double.parseDouble(e[2]));
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
		Deprivation sc = Deprivation.getInstance();
		RegionMap rm = (RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/regioni.ser"));
		rm.setName("deprivation");
		Map<String,Double> depriv = sc.getDepriv();
		KMLHeatMap.drawHeatMap(Config.getInstance().base_folder+"/TIC2015/"+rm.getName()+".kml",depriv,rm,"",false);
	}
	
}
