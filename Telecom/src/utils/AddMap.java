package utils;

import java.util.HashMap;
import java.util.Map;

public class AddMap extends HashMap<String,Double> {
	
	private HashMap<String,Double> den; // denominator allows to compute mean
	
	public AddMap() {
		super();
		den = new HashMap<String,Double>();
	}
	
	public void add(String key, double value) {
		Double v = this.get(key);
		if(v == null) 
			this.put(key, value);
		else 
			this.put(key, v+value);
		
		
		Double cont = den.get(key);
		den.put(key, cont == null ? 1 : cont+1);
		
	}
	
	public void addAll(Map<String,Double> map) {
		for(String k: map.keySet())
			this.add(k, map.get(k));
	}
	
	public void mean() {
		for(String k: this.keySet()) {
			this.put(k, this.get(k) / den.get(k));
			
		}
	}

	
	
	public static void main(String[] args) {
		
		AddMap m = new AddMap();
		m.add("ciao",3);
		m.add("ciao",3);
		
		for(String k: m.keySet())
			System.out.println(k+" -> "+m.get(k));
		
	}
		
}
