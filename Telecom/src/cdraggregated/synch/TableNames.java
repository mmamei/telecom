package cdraggregated.synch;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import region.RegionI;
import region.RegionMap;
import utils.Config;
import utils.CopyAndSerializationUtils;

public class TableNames {
	
	static HashMap<String,String> city2region = new HashMap<String,String>();
	static {
		city2region.put("torino", "PIEMONTE");
		city2region.put("milano", "LOMBARDIA");
		city2region.put("venezia", "VENETO");
		city2region.put("roma", "LAZIO");
		city2region.put("napoli", "CAMPANIA");
		city2region.put("bari", "PUGLIA");
		city2region.put("palermo", "SICILIA");
		
		city2region.put("campobasso", "MOLISE");
		city2region.put("siracusa", "SICILIA");
		city2region.put("benevento", "CAMPANIA");
		city2region.put("caltanissetta", "SICILIA");
		city2region.put("modena", "EMILIA-ROMAGNA");
		city2region.put("siena", "TOSCANA");
		city2region.put("asti", "PIEMONTE");
		city2region.put("ferrara", "EMILIA-ROMAGNA");
		city2region.put("ravenna", "EMILIA-ROMAGNA");
	}
	
	static HashMap<String,String> city2province = new HashMap<String,String>();
	static {
	for(String province: new String[]{"torino","milano","venezia","roma","napoli","bari","palermo","campobasso","siracusa","benevento","caltanissetta","modena","siena","asti","ferrara","ravenna"})
		city2province.put(province,province.toUpperCase());
	}
	
	public enum Country {Italy,IvoryCoast,Senegal,IvoryCoast1Month,Senegal1Month};
	
	
	public static List<String> getAvailableProvinces(Country country) {
		List<String> provinces = new ArrayList<>();
		if(country.equals(Country.Italy)) {
			provinces.add("napoli");
			provinces.add("bari");
			provinces.add("caltanissetta");
			provinces.add("siracusa");
			provinces.add("benevento");
			provinces.add("palermo");
			provinces.add("campobasso");
			provinces.add("roma");
			provinces.add("siena");
			provinces.add("ravenna");
			provinces.add("ferrara");
			provinces.add("modena");
			provinces.add("venezia");
			provinces.add("torino");
			provinces.add("asti");
			provinces.add("milano");
		}
		if(country.equals(Country.IvoryCoast) || country.equals(Country.IvoryCoast1Month) ) {
			RegionMap rm_lvl2 = (RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/ivoryCoastProvince.ser"));
			for(RegionI r: rm_lvl2.getRegions())
				provinces.add(r.getName());
		}
		if(country.equals(Country.Senegal) || country.equals(Country.Senegal1Month) ) {
			RegionMap rm_lvl2 = (RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/senegal-province.ser"));
			for(RegionI r: rm_lvl2.getRegions())
				provinces.add(r.getName());
		}
		return provinces;
	}
}
