package utils;

import java.util.HashMap;

public class AddMap extends HashMap<String,Double> {
	
	public void add(String key, double value) {
		Double v = this.get(key);
		if(v == null) 
			this.put(key, value);
		else 
			this.put(key, v+value);
	}
	
	public static void main(String[] args) {
		
		AddMap m = new AddMap();
		m.add("ciao",3);
		m.add("ciao",3);
		
		for(String k: m.keySet())
			System.out.println(k+" -> "+m.get(k));
		
	}
		
}
