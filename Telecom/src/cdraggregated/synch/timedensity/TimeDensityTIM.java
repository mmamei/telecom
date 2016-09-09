package cdraggregated.synch.timedensity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import region.RegionI;
import region.RegionMap;
import utils.Config;
import utils.CopyAndSerializationUtils;
import utils.StatsUtils;
import utils.time.TimeConverter;
import visual.html.GoogleChartGraph;
import visual.kml.KMLColorMap;



public class TimeDensityTIM implements TimeDensity {
	
	public enum UseResidentType {ALL,RESIDENTS,NOT_RESIDENTS};
	public static UseResidentType res_type =null;
	
	static boolean ENABLE_CACHE = true;
	
	private String city;
	private RegionMap grid;
	private TimeConverter tc = null;
	private Map<String,double[]> map = new HashMap<String,double[]>();
	private Map<String,double[]> mapz = new HashMap<String,double[]>();

	public List<String> getKeys() {
		List<String> keys = new ArrayList<>();
		for(String k: map.keySet())
			keys.add(k);
		return keys;
	}
	
	
	public double[] get(String key) {
		return map.get(key);
	}
	
	public double[] getz(String key) {
		return mapz.get(key);
	}
	
	private void printKML(String file, Map<String,Integer> assignments) {
		
		Map<String,String> desc = new HashMap<>();
		for(String r: map.keySet()) {
			desc.put(r, GoogleChartGraph.getGraph(tc.getTimeLabels(), map.get(r), "demo", "date", "cdr"));
		}
		try {
			KMLColorMap.drawColorMap(file, assignments, grid, desc);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public static void main(String[] args) throws Exception {
		String city = "milano";
		res_type = UseResidentType.ALL;
		TimeDensityTIM td = new TimeDensityTIM(city,Config.getInstance().dataset_folder+"/TI-CHALLENGE-2015/DEMOGRAPHIC/"+city+"/callsLM_"+city.substring(0,2).toUpperCase()+"_COMUNI2012","2015-03-31:0:0:0","2015-04-30:23:59:59");
		System.out.println("Done");
		
		String cell = "3950_2_3_0_0_0_1";
		String[] time = td.tc.getTimeLabels();
		double[] cdr = td.map.get(cell);
		double[] z = td.mapz.get(cell);
		
		PrintWriter out = new PrintWriter(new FileWriter("C:/BASE/"+city+"-"+cell+".csv"));
		out.println("time,cdr,z");
		for(int i=0; i<time.length;i++)
			out.println(time[i]+","+cdr[i]+","+z[i]);
		out.close();
		
		
		System.out.println("Done");		
	}

	
	@Override
	public Map<String,String> getMapping(RegionMap rm) {
		Map<String,String> result = new HashMap<>();
		
		for(RegionI r: grid.getRegions()) {
			float[] f = rm.computeAreaIntersection(r);
			
			int max = -1;
			for(int i=0;i<f.length;i++)
				if(f[i] > 0 && (max == -1 || f[i] > f[max]))
					max = i;
		
			RegionI o = rm.getRegion(max);
			if(o != null) 
				result.put(r.getName(), o.getName());
			//else System.err.println("mapping error "+r.getName());
		}
		return result;
	}
	
	
	TimeDensityTIM(String city, String file,String startTime, String endTime) {
		this.city = city;
		grid = (RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/tic-"+city+"-grid.ser"));
		try {
			tc = TimeConverter.getInstance(startTime,endTime);
			
			File f = null;
			
			switch(res_type) {
				case ALL : f = new File(Config.getInstance().base_folder+"/TIC2015/cache/all/"+city+".ser"); break;
				case RESIDENTS: f = new File(Config.getInstance().base_folder+"/TIC2015/cache/residents/"+city+".ser"); break;
				case NOT_RESIDENTS: f = new File(Config.getInstance().base_folder+"/TIC2015/cache/not_residents/"+city+".ser"); break;
			}
			
				
			if(ENABLE_CACHE && f.exists()) {
				map = (Map<String,double[]>)CopyAndSerializationUtils.restore(f);
			}
			else {
				f.getParentFile().mkdirs();
				processFile(city,file);
				CopyAndSerializationUtils.save(f, map);
				printKML(f.getParentFile()+"/"+city+".kml",null);
				printKML(f.getParentFile()+"/"+city+"-comuni2012.kml",KMLColorMap.toIntAssignments(getMapping((RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/tic-comuni2012-"+city+".ser")))));	
			}	
		}catch(Exception e) {
			e.printStackTrace();
		}	
		
		for(String k: map.keySet())
			mapz.put(k, (StatsUtils.getZH(map.get(k),tc)));
			
	}
	
	
	

	private void processFile(String city, String file) throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(file));
		long tot_lines = 0;
		long skipped_lines = 0;
		
		String line = null;
		
		Set<String> residents = getComuni2012FromCity(city);
		
		while ((line = br.readLine()) != null) {
			String[] x = line.split("\t");

			if (x.length < 4) {
				System.err.println(line);
				continue;
			}

			tot_lines++;

			long time = Long.parseLong(x[0]) * 1000;
			if (time > 2527282800000l) // 2527282800000l = 1/1/2050
				time = Long.parseLong(x[0]);

			// extra time constraint
			if (time < tc.startTime || time > tc.endTime) {
				skipped_lines++;
				continue;
			}

			String cell = x[1];
			double value = Double.parseDouble(x[3]);
			String meta = x[2];

			boolean to_be_considered = true;
			
			switch(res_type) {
				case ALL : to_be_considered = true; break;
				case RESIDENTS: to_be_considered = residents.contains(meta); break;
				case NOT_RESIDENTS: to_be_considered = !residents.contains(meta); break;
			}
			if(to_be_considered) {
				double[] v = map.get(cell);
				if (v == null) {
					v = new double[tc.getTimeSize()];
					map.put(cell, v);
				}
				// System.out.println(time+" --> "+new Date(time));
				v[tc.time2index(time)] += value;
			}
		}
		if (skipped_lines > 0)
			System.out.println("Skipped "+ skipped_lines+ " out of "+ tot_lines+ " due to time restrictions - TimeDensityFromAggregatedData");
	
		br.close();
	}
	
	private static Set<String> getComuni2012FromCity(String city) {
		Set<String> comuni = new HashSet<String>();
		RegionMap rm = (RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/tic-comuni2012-"+city+".ser"));
		for(RegionI r: rm.getRegions()) {
			String name = r.getName();
			if(name.indexOf("_")>0) {
				//System.out.print(name+"  -->  ");
				name = name.substring(0,name.indexOf("_"));
				//System.out.println(name);
			}
			comuni.add(name);
		}
		return comuni;
	}

	@Override
	public TimeConverter getTimeConverter() {
		return tc;
	}
}
