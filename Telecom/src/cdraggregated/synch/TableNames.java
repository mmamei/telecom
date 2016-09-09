package cdraggregated.synch;

import static cdraggregated.synch.TableNames.Country.Italy;
import static cdraggregated.synch.TableNames.Country.IvoryCoast;
import static cdraggregated.synch.TableNames.Country.Senegal;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import region.RegionI;
import region.RegionMap;
import utils.Config;
import utils.CopyAndSerializationUtils;

public class TableNames {
	
	
	
	
	
	public enum Country {Italy,IvoryCoast,Senegal};
	
	
	static HashMap<String,String> ITcity2region = new HashMap<String,String>();
	static {
		ITcity2region.put("torino", "PIEMONTE");
		ITcity2region.put("milano", "LOMBARDIA");
		ITcity2region.put("venezia", "VENETO");
		ITcity2region.put("roma", "LAZIO");
		ITcity2region.put("napoli", "CAMPANIA");
		ITcity2region.put("bari", "PUGLIA");
		ITcity2region.put("palermo", "SICILIA");
		
		ITcity2region.put("campobasso", "MOLISE");
		ITcity2region.put("siracusa", "SICILIA");
		ITcity2region.put("benevento", "CAMPANIA");
		ITcity2region.put("caltanissetta", "SICILIA");
		ITcity2region.put("modena", "EMILIA-ROMAGNA");
		ITcity2region.put("siena", "TOSCANA");
		ITcity2region.put("asti", "PIEMONTE");
		ITcity2region.put("ferrara", "EMILIA-ROMAGNA");
		ITcity2region.put("ravenna", "EMILIA-ROMAGNA");
	}
	
	public static String city2region(String city, Country country) {
		if(country.equals(Country.Italy)) 
			return ITcity2region.get(city);
		return city;
	}
	
	public static String city2province(String city, Country country) {
		if(country.equals(Country.Italy)) 
			return city.toUpperCase();
		return city;
	}
	
	
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
		if(country.equals(IvoryCoast)) {
			RegionMap rm_lvl2 = (RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/ivorycoast_regioni.ser"));
			for(RegionI r: rm_lvl2.getRegions())
				provinces.add(r.getName());
		}
		if(country.equals(Senegal)) {
			RegionMap rm_lvl2 = (RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/senegal_regioni.ser"));
			for(RegionI r: rm_lvl2.getRegions())
				provinces.add(r.getName());
		}
		return provinces;
	}
	
	
	public static RegionMap getRegionMap(String city, Country country) {
		RegionMap rm = null;
		if(country.equals(Italy))
			rm = (RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/tic-comuni2012-"+city+".ser"));
		if(country.equals(IvoryCoast))
			rm = (RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/ivorycoast_comuni.ser"));
		if(country.equals(Senegal))
			rm = (RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/senegal_comuni.ser"));
		return rm;
	}
	
}
