package utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddMapL extends HashMap<String,List<Double>> {
	
	public AddMapL() {
		super();
	}
	
	public void add(String key, List<Double> value) {
		List<Double> v = this.get(key);
		if(v == null) {
			v = new ArrayList<Double>();
			this.put(key, v);
		}
		v.addAll(value);		
	}
	
	
	public void addAll(Map<String, List<Double>> map) {
		for(String k: map.keySet())
			this.add(k, map.get(k));
	}	
	
	public static void main(String[] args) {
		
		List<Double> v1 = new ArrayList<Double>();
		v1.add(3.0); 
		v1.add(4.0);
		
		List<Double> v2 = new ArrayList<Double>();
		v2.add(1.0); 
		v2.add(2.0);
		
		AddMapL m = new AddMapL();
		m.add("ciao",v1);
		m.add("ciao",v2);
		
		for(String k: m.keySet())
			System.out.println(k+" -> "+m.get(k));
		
	}

	
		
}
