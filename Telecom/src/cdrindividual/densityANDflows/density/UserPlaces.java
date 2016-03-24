package cdrindividual.densityANDflows.density;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserPlaces {
	public String username;
	public Map<String,List<double[]>> lonlat_places;
	public Map<String,List<double[]>> clustersize_minTime_maxTime;
	
	UserPlaces(String username) {
		this.username = username;
		lonlat_places = new HashMap<String,List<double[]>>();
		clustersize_minTime_maxTime = new HashMap<String,List<double[]>>();
	}
	
	void addPlaceInfo(String kop, double lon, double lat) {
		List<double[]> p = lonlat_places.get(kop);
		if(p==null) {
			p = new ArrayList<double[]>();
			lonlat_places.put(kop, p);
		}
		p.add(new double[]{lon,lat});
	}
	
	
	void addTimeInfo(String kop, double clustersize, double minTime, double maxTime) {
		List<double[]> p = clustersize_minTime_maxTime.get(kop);
		if(p==null) {
			p = new ArrayList<double[]>();
			clustersize_minTime_maxTime.put(kop, p);
		}
		p.add(new double[]{clustersize,minTime,maxTime});
	}
	
	public static Map<String,UserPlaces> readUserPlaces(String place_file,String time_file) throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(place_file));
		BufferedReader br_time = null;
		if(time_file!=null) br_time = new BufferedReader(new FileReader(time_file));
		
		String line;
		String[] elements;
		Map<String,UserPlaces> up = new HashMap<String,UserPlaces>();
		while((line = br.readLine())!=null) {
			//System.out.println(line);
			elements = line.split(",");
			String username = elements[0];
			UserPlaces places = up.get(username);
			if(places==null) {
				places = new UserPlaces(username);
				up.put(username, places);
			}
			String kind = elements[1];
			for(int i=2;i<elements.length;i++){
				String c = elements[i];
				double lon = Double.parseDouble(c.substring(0,c.indexOf(" ")));
				double lat = Double.parseDouble(c.substring(c.indexOf(" ")+1));
				places.addPlaceInfo(kind, lon, lat);
			} 
			
			
			
			
			
			
			
			
			
			
			if(br_time!=null) {
				line = br_time.readLine();
				
				if(line.contains("?")) continue;
				
				String[] te = line.split(",");
				if(!te[0].equals(username) || !te[1].equals(kind)) System.err.println("ERROR PLACES AND TIME FILE ARE NOT ALIGNED!");
				for(int i=2;i<te.length;i++){
					String[] c = te[i].split(";");
					places.addTimeInfo(te[1],Double.parseDouble(c[0]), Double.parseDouble(c[1]), Double.parseDouble(c[2]));
				} 
			}
			
		}
		br.close();
		return up;
	}
}
