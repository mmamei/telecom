package cdraggregated.densityANDflows;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

public class ObjectID2IstatCode implements ZoneConverter {
	
	private Map<String,String> map;
	
	public  ObjectID2IstatCode(String file) {
		
		map = new HashMap<>();
		// WKT	OBJECTID	COD_REG	COD_PRO	COD_ISTAT	PRO_COM	NOME
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;
			while((line=br.readLine())!=null) {
				String[] x = line.split("\t");
				map.put(x[1], x[5]);
			}
			br.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
		
	@Override
	public String convert(String zone) {
		return map.get(zone);
	}

}
