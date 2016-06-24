package cdraggregated.synch;

import java.util.HashMap;

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
}
