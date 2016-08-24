package otherdata.d4d.ophi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

import region.RegionI;
import region.RegionMap;
import utils.Config;
import utils.CopyAndSerializationUtils;

public class Ophi {
	
	private Map<String,Double> depriv = new HashMap<>();
	
	public Ophi(String file) {
		
		try {	
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;
			while((line=br.readLine())!=null) {
				String[] e = line.split(",");
				depriv.put(e[0], Double.parseDouble(e[e.length-1]));
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
		//Ophi ophi = new Ophi("G:/DATASET/CENSUS/ophi/ophi-senegal.csv");
		//RegionMap rm = (RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/senegal-regioni.ser"));
		
		Ophi ophi = new Ophi("G:/DATASET/CENSUS/ophi/ophi-ivorycoast.csv");
		RegionMap rm = (RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/ivoryCoastProvince.ser"));
		
		
		
		for(String k: ophi.getDepriv().keySet()) {
			boolean found = false;
			for(RegionI r: rm.getRegions())
				if(r.getName().equals(k))
					found = true;
			if(!found) System.out.println("OPHI "+k+" not found");
			else System.out.println("+++ OPHI "+k+" found");
		}
		System.out.println("-------------------------");
		
		for(RegionI r: rm.getRegions()) {
			boolean found = ophi.getDepriv().get(r.getName()) != null;
			if(!found) System.out.println("RM "+r.getName()+" not found");
			else System.out.println("+++ RM "+r.getName()+" found");
		}
		
		
		
	}
	
}
