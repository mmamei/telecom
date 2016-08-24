package otherdata.d4d.afrobarometer;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import otherdata.d4d.ophi.Ophi;
import utils.AddMap;

public class AfroBarometer {
	
	// Dakar --> Query --> Histogram
	private Map<String,Map<String,AddMap>> all;
	private String[] header;
	public AfroBarometer(String file) {
		try {
			
			all = new HashMap<>();
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8"));
			String line;
			header = br.readLine().split(";");
					
			while((line=br.readLine())!=null) {
				String[] e = line.split(";");
				String region = e[3];
				Map<String,AddMap> v = all.get(region);
				if(v == null) {
					v = new HashMap<>();
					all.put(region, v);
					
					for(int i=4;i<e.length;i++)
						v.put(header[i], new AddMap());
				}
				
				for(int i=4;i<e.length;i++)
					v.get(header[i]).add(e[i], 1.0);
			}
			br.close();		
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public List<String> getRegions() {
		List<String> regions = new ArrayList<>();
		for(String k: all.keySet())
			regions.add(k);
		return regions;
	}
	
	public List<String> getHeaders() {
		List<String> h = new ArrayList<>();
		for(int i=4;i<header.length;i++)
			h.add(header[i]);
		return h;
	}
	
	public Map<String,Double> get(String region, String h) {
		return all.get(region).get(h);
	}
	
	public double proportion(String region, String h, String[] okValues) {
		
		
		Set<String> okSet = new HashSet<String>();
		for(String o: okValues)
			okSet.add(o);
			
		double cont = 0;
		double tot = 0;
		Map<String,Double> m = all.get(region).get(h);
		for(String k: m.keySet()) {
			if(okSet.contains(k)) cont+=m.get(k);
			if(!k.equals("You were too young to vote")) tot+=m.get(k);
		}
		
		
		return cont/tot;
	}
	
	public Map<String,Double> proportion(String h, String[] okValues) {
		Map<String,Double> result = new HashMap<>();
		for(String r: this.getRegions())
			result.put(r, proportion(r,h,okValues));
		return result;
	}
	
	public static void main(String[] args) throws Exception {
		//AfroBarometer ab = new AfroBarometer("G:/DATASET/CENSUS/afrobarometer/afrobar-senegal.csv");
		//Ophi ophi = new Ophi("G:/DATASET/CENSUS/ophi/ophi-senegal.csv");
		AfroBarometer ab = new AfroBarometer("G:/DATASET/CENSUS/afrobarometer/afrobar-ivorycoast.csv");
		Ophi ophi = new Ophi("G:/DATASET/CENSUS/ophi/ophi-ivorycoast.csv");
		
		/*
		List<String> ab_regions = ab.getRegions();
		Set<String> ophi_regions = ophi.getDepriv().keySet();
		
		for(String r: ab_regions) System.out.println(r);
		System.out.println("---------------------");
		for(String r: ophi_regions) System.out.println(r);
		*/
		
		
		/*
		for(String h: ab.getHeaders())
			System.out.println(h);
		*/
		
		String[] queries = new String[]{"Q19B", "Q20A", "Q20B", "Q21", "Q21_SEN"};
		for(String q: queries) {
			Map<String,Double> mq = ab.get("Abidjan",q);
			if(mq==null) continue;
			System.out.println(q+" ---------------------------");
			for(String a: mq.keySet())
				System.out.println(a+" => "+mq.get(a));
			}
		
	}
		
}
