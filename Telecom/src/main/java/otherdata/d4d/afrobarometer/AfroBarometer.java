package otherdata.d4d.afrobarometer;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import otherdata.d4d.ophi.Ophi;
import region.Region2Region;
import utils.AddMap;

public class AfroBarometer {
	
	// Dakar --> Query --> Histogram
	private Map<String,Map<String,AddMap>> all;
	private String[] header;
	
	public AfroBarometer(String file) {
		this(file, null);
	}
	
	public AfroBarometer(String file, Map<String,Set<String>> region2region) {
		try {
			
			all = new HashMap<>();
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8"));
			String line;
			header = br.readLine().split(";");
					
			while((line=br.readLine())!=null) {
				String[] e = line.split(";");
				String region = e[3];
				
				// funny accent name converison
				if(region.equals("Gboklè")) region = "Gbôkle";
				if(region.equals("San Pedro")) region = "San-Pédro";
				if(region.equals("Agneby-Tiassa")) region = "Agnéby-Tiassa";
				if(region.equals("Haut Sassandra")) region = "Haut-Sassandra";
				if(region.equals("Indenié-Djuablin")) region = "Indénié-Djuablin";
				if(region.equals("Gbékè")) region = "Gbeke";
				if(region.equals("N'Zi")) region = "N'zi";
				
				
				Set<String> regions = new HashSet<>();
				regions.add(region);
				
				
				if(region2region != null) {
					regions = region2region.get(region);
					if(regions == null) {
						System.out.println("AfroBarometer: Cannot convert "+e[3]);
						continue;
					}	
				}
				
				for(String rx: regions) {
				
					Map<String,AddMap> v = all.get(rx);
					if(v == null) {
						v = new HashMap<>();
						all.put(rx, v);
						
						for(int i=4;i<e.length;i++)
							v.put(header[i], new AddMap());
					}
					
					for(int i=4;i<e.length;i++)
						v.get(header[i]).add(e[i], 1.0);
				}
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
		//AfroBarometer ab = new AfroBarometer("G:/DATASET/CENSUS/afrobarometer/afrobar-senegal.csv",Region2Region.region2region("G:/DATASET/GEO/senegal/senegal_subpref.csv","NAME_1","NAME_2"));
		//Ophi ophi = new Ophi("G:/DATASET/CENSUS/ophi/ophi-senegal.csv",Region2Region.region2region("G:/DATASET/GEO/senegal/senegal_subpref.csv","NAME_1","NAME_2"));
		
		//AfroBarometer ab = new AfroBarometer("G:/DATASET/CENSUS/afrobarometer/afrobar-senegal.csv");
		//Ophi ophi = new Ophi("G:/DATASET/CENSUS/ophi/ophi-senegal.csv",Region2Region.region2region("G:/DATASET/GEO/senegal/senegal_subpref.csv","NAME_2","NAME_1"));
		
		
		AfroBarometer ab = new AfroBarometer("G:/DATASET/CENSUS/afrobarometer/afrobar-ivorycoast.csv",Region2Region.region2region("G:/DATASET/GEO/ivorycoast/ivorycoast_subpref.csv","NAME_2","NAME_1"));
		Ophi ophi = new Ophi("G:/DATASET/CENSUS/ophi/ophi-ivorycoast.csv",Region2Region.region2region("G:/DATASET/GEO/ivorycoast/ivorycoast_subpref.csv","NAME_2","NAME_1"));
		
		//AfroBarometer ab = new AfroBarometer("G:/DATASET/CENSUS/afrobarometer/afrobar-ivorycoast.csv");
		//Ophi ophi = new Ophi("G:/DATASET/CENSUS/ophi/ophi-ivorycoast.csv");
		
		
		List<String> ab_regions = ab.getRegions();
		Set<String> ophi_regions = ophi.getDepriv().keySet();
		
		
		for(String r: ab_regions)
			if(!ophi_regions.contains(r)) System.out.println(r);
		for(String r: ophi_regions)
			if(!ab_regions.contains(r)) System.out.println(r);
		
		//Map<String,Double> x = ab.proportion("Q19B", new String[]{"Official Leader","Active Member"});
		//System.out.println(x);
		/*
		for(String h: ab.getHeaders())
			System.out.println(h);
		*/
		/*
		String[] queries = new String[]{"Q19B", "Q20A", "Q20B", "Q21", "Q21_SEN"};
		for(String q: queries) {
			Map<String,Double> mq = ab.get("Abidjan",q);
			if(mq==null) continue;
			System.out.println(q+" ---------------------------");
			for(String a: mq.keySet())
				System.out.println(a+" => "+mq.get(a));
			}
		*/
	}
		
}
