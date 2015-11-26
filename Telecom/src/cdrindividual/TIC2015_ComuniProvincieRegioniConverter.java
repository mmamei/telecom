package cdrindividual;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

public class TIC2015_ComuniProvincieRegioniConverter {
	
	
	private Map<String,String> comuni_to_provincie;
	private Map<String,String> comuni_to_regioni;
	
	
	private static TIC2015_ComuniProvincieRegioniConverter instance = null;
	
	public static TIC2015_ComuniProvincieRegioniConverter getInstance() {
		if(instance == null)
			instance = new TIC2015_ComuniProvincieRegioniConverter();
		return instance;
	}
	
	private TIC2015_ComuniProvincieRegioniConverter() {
		
		comuni_to_provincie = new HashMap<String,String>();
		comuni_to_regioni = new HashMap<String,String>();
		
		try {
		
		    Map<String,String> id2prov = new HashMap<String,String>();
		    BufferedReader br = new BufferedReader(new FileReader("C:/DATASET/GEO/prov2011.csv"));
		    String line;
		    while((line=br.readLine())!=null) {
		    	String[] e = line.split("\t");
		    	String id = e[2];
		    	String name = e[4];
		    	id2prov.put(id, name);
		    }
		    br.close();
		    
		    
		    Map<String,String> id2region = new HashMap<String,String>();
		    br = new BufferedReader(new FileReader("C:/DATASET/GEO/regioni.csv"));
		    while((line=br.readLine())!=null) {
		    	String[] e = line.split("\t");
		    	String id = e[1];
		    	String name = e[2];
		    	id2region.put(id, name);
		    }
		    br.close();
		    
		    
		    br = new BufferedReader(new FileReader("C:/DATASET/GEO/comuni2012.csv"));
		    while((line=br.readLine())!=null) {
		    	String[] e = line.split("\t");
		    	String id = e[5];
		    	String prov = e[3];
		    	String reg = e[2];
		    	comuni_to_provincie.put(id, id2prov.get(prov));
		    	comuni_to_regioni.put(id, id2region.get(reg));
		    }
		    br.close();
		    
		    
		    /*
		    br = new BufferedReader(new FileReader("C:/DATASET/GEO/comuni2014.csv"));
		    while((line=br.readLine())!=null) {
		    	String[] e = line.split("\t");
		    	String id = e[1];
		    	String prov = e[3];
		    	String reg = e[2];
		    	comuni_to_provincie.put(id, id2prov.get(prov));
		    	comuni_to_regioni.put(id, id2region.get(reg));
		    }
		    br.close();
		    */
		    
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public String comuni2provincie(String c) {
		if(c.equals("0")) return c;
		if(c.equals("108033")) return "MI";
		if(c.equals("108023")) return "MI";
		return comuni_to_provincie.get(c);
	}
	
	public String comuni2regioni(String c) {
		if(c.equals("0")) return c;
		return comuni_to_regioni.get(c);
	}
	
	
	
	public static void main(String[] args) {
		System.out.println(TIC2015_ComuniProvincieRegioniConverter.getInstance().comuni2provincie("1272")+" "+TIC2015_ComuniProvincieRegioniConverter.getInstance().comuni2regioni("1272"));
		System.out.println(TIC2015_ComuniProvincieRegioniConverter.getInstance().comuni2provincie("15146")+" "+TIC2015_ComuniProvincieRegioniConverter.getInstance().comuni2regioni("15146"));
		System.out.println(TIC2015_ComuniProvincieRegioniConverter.getInstance().comuni2provincie("108033")+" "+TIC2015_ComuniProvincieRegioniConverter.getInstance().comuni2regioni("108033"));
		
	}
	
	
}
