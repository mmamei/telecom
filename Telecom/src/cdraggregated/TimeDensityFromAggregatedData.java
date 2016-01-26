package cdraggregated;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

import region.RegionI;
import region.RegionMap;
import utils.Config;
import utils.CopyAndSerializationUtils;
import utils.StatsUtils;
import utils.time.TimeConverter;
import visual.html.GoogleChartGraph;
import visual.r.RPlotter;

public class TimeDensityFromAggregatedData {
	
	private String city;
	private String type;
	
	public TimeConverter tc = null;
	private Map<String,double[]> map = new HashMap<String,double[]>();
	
	private RegionMap gridMap;
	public RegionMap rm;
	
	
	public TimeDensityFromAggregatedData(String city, String type, String file, int[] readIndexes, SynchConstraints constraint) {
		this(city,type,file,readIndexes,constraint,null);	
	}
	
	
	private TimeDensityFromAggregatedData() {
		
	}

	
	
	public TimeDensityFromAggregatedData reproject2Map(RegionMap rm_to) {
		TimeDensityFromAggregatedData res = new TimeDensityFromAggregatedData();
		res.city = city;
		res.type = type;
		res.tc = tc;
		res.rm = rm_to;
		res.map = new HashMap<String,double[]>();
		
		
		for(String f : map.keySet()) {
			double[] value = map.get(f);
			
			
			
			if(rm_to.getName().contains("regioni")) {
				String region = SynchAnalysis.city2region.get(city);
				if(region!=null) add(res.map,region, value); 
				else System.err.println("ERROR IN "+city);
			}
			
			
			if(rm_to.getName().contains("prov2011")) {
				String province = SynchAnalysis.city2province.get(city);
				if(province!=null) add(res.map,province, value); 
				else System.err.println("ERROR IN "+city);
			}
			
		}
		//System.out.println("---------------------> "+res.map.size());
		return res;
	}
	
	public static void add(Map<String,double[]> map, String key, double[] value) {
		double[] v = map.get(key);
		if(v == null) v = new double[value.length];
		for(int i=0; i<v.length;i++)
			v[i]+=value[i];
		map.put(key, v);
	}
	
	
	public TimeDensityFromAggregatedData(String city, String type, String file, int[] readIndexes, SynchConstraints constraint, RegionMap rm) {
		this.city = city;
		this.type = constraint==null ? type : type+"-"+constraint.title;
		this.rm = rm;
		if(rm!=null) {
			// load the grid map to be used for conversion
			gridMap = (RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/tic-"+city+"-grid.ser"));
		}
		
		try {
			tc = TimeConverter.getInstance();
			
			
			File f = new File(Config.getInstance().base_folder+"/TIC2015/cache/single/"+city+"-"+type+"-"+new File(file).getName()+"-"+constraint.title+".ser");
			if(f.exists()) {
				map = (Map<String,double[]>)CopyAndSerializationUtils.restore(f);
			}
			else {
				
				new File(Config.getInstance().base_folder+"/TIC2015/cache/single/").mkdirs();
				
				if(file.endsWith(".tar.gz")) {
					TarArchiveInputStream tarInput = new TarArchiveInputStream(new GzipCompressorInputStream(new FileInputStream(file)));
					TarArchiveEntry currentEntry = tarInput.getNextTarEntry();
					
					while (currentEntry != null) {
						processFile(new BufferedReader(new InputStreamReader(tarInput)),readIndexes,constraint); // Read directly from tarInput
					    //System.out.println("Reading File = " + currentEntry.getName()); 
					    currentEntry = tarInput.getNextTarEntry(); 
					}
					tarInput.close();
				}
				else {
					//System.out.println(file);
					processFile(new BufferedReader(new FileReader(file)),readIndexes,constraint);
				}
				
				CopyAndSerializationUtils.save(f, map);
				saveCSV(f.getAbsolutePath().replaceAll(".ser", ".csv"),map);
				saveARFF(f.getAbsolutePath().replaceAll(".ser", ".arff"),map);
			}
			
			
			
		}catch(Exception e) {
			e.printStackTrace();
		}	
	}
	
	static boolean Z = true;
	
	public static void saveCSV(String file, Map<String,double[]> map) {
		try {
			PrintWriter out = new PrintWriter(new FileWriter(new File(file)));
			TimeConverter tc = TimeConverter.getInstance();
			for(String k: map.keySet()) {
				out.print(k);
				double[] series = map.get(k);
				double[] fseries = Z ? (StatsUtils.getZH(series,tc)) : series;
				for(double v: fseries)
					out.print(";"+v);
				out.println();
			}			
			out.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public static void saveARFF(String fn, Map<String,double[]> map) {
		try {
			File file = new File(fn);
			PrintWriter out = new PrintWriter(new FileWriter(file));
			
			out.println("@RELATION "+file.getName().replaceAll(".arff", (Z?"_Z":"")));
			
			out.println("@ATTRIBUTE region STRING");
			
			int size = map.values().iterator().next().length-1;
			for(int i=0; i<size;i++)
			out.println("@ATTRIBUTE v"+i+" NUMERIC");
			out.println("@DATA");
			
			
			TimeConverter tc = TimeConverter.getInstance();
			
			
			for(String k: map.keySet()) {
				out.print("'"+k+"'");
				double[] series = map.get(k);
				double[] fseries = Z ? (StatsUtils.getZH(series,tc)) : series;
				for(double v: fseries)
					out.print(","+v);
				out.println();
			}			
			out.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public double[] get(String r) {
		return map.get(r);
	}
	
	public int size() {
		return map.size();
	}
	
	private void processFile(BufferedReader br,int[] readIndexes, SynchConstraints constraint) {
		
		long tot_lines = 0;
		long skipped_lines = 0;
		
		String line = null;
		try {
			while ((line = br.readLine()) != null) {
			       String[] x = line.split("\t");
			 
			       if(x.length < 4) {
			    	   System.err.println(line);
			    	   continue;
			       }
			       
			       
			       tot_lines ++;
			       
			       long time = Long.parseLong(x[readIndexes[0]]) * 1000;
			       //long time = Long.parseLong(x[readIndexes[0]]);
			       
			       // extra time constraint
			       if(time < TimeConverter.getInstance().startTime || time > TimeConverter.getInstance().endTime) {
			    	   skipped_lines ++;
			    	   continue;
			       }
			       
			       
			       String cell = x[readIndexes[1]];   
			       double value = Double.parseDouble(x[readIndexes[2]]);
			       String meta = x[readIndexes[3]];

			       if(constraint == null || constraint.ok(meta)) {
			       
			    	   if(rm != null) {
				    	   // convert from grid representation (gridMap) to another region map (rm)
				    	   			    		   
			    		   float[] areas = rm.computeAreaIntersection(gridMap.getRegion(cell));
			    		   for(int i=0; i<areas.length;i++)
			    			   if(areas[i] > 0) {
			    				   RegionI r = rm.getRegion(i);
			    				   double[] v = map.get(r.getName());
			    				   if(v == null) {
			    					   v = new double[tc.getTimeSize()];
			    					   map.put(r.getName(), v);
			    				   }
			    				   //System.out.println(time+" --> "+new Date(time)+" .... "+value * areas[i]);
			    				   v[tc.time2index(time)]+= value * areas[i];
			    			   }
				       }
			    	   else {
			    		   double[] v = map.get(cell);
					       if(v == null) {
					       	   v = new double[tc.getTimeSize()];
					       	   map.put(cell, v);
					       }
					       //System.out.println(time+" --> "+new Date(time));
					       v[tc.time2index(time)]+= value;
			    	   }
			       }
			}
			if(skipped_lines > 0) System.out.println("Skipped "+skipped_lines+" out of "+tot_lines+" due to time restrictions - TimeDensityFromAggregatedData");
			//br.close();
		}catch(Exception e) {
			System.err.println(line);
			e.printStackTrace();
		}	
	}
	
	
	public void add(TimeDensityFromAggregatedData td) {
		try {
			if(!city.equals(td.city)) throw new Exception("Cannot add TimeDensityFromAggregatedData of different cities");
			type = type+"-"+td.type;
			for(String r: map.keySet()) {
				double[] v = map.get(r);
				double[] w = td.map.get(r);
				if(w != null)
					for(int i=0; i<v.length;i++)
						v[i] += w[i];
			}
			for(String r: td.map.keySet()) {
				if(map.get(r) == null)
					map.put(r, td.map.get(r));
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public double[] getFirst() {
		return this.map.values().iterator().next();
	}
	
	
	
	
	


	
	public String getCity() {
		return city;
	}
	
	public String getType() {
		return type;
	}
	
	public void plotR(String cell) {
		String[] x = tc.getTimeLabels();

		List<double[]> v = new ArrayList<double[]>();
		v.add(map.get(cell));
		
		List<String> names = new ArrayList<String>();
		names.add(cell);
		
		
		RPlotter.drawLine(x, v, names, "cell", "date", city+" "+type, Config.getInstance().base_folder+"/Images/tdc-"+city+"-"+type+".pdf", null);
		
		try {
			PrintWriter out = new PrintWriter(new FileWriter(Config.getInstance().base_folder+"/Images/tdc-"+city+"-"+type+".html"));
			out.println(GoogleChartGraph.getGraph(x, v, names, "date", type));
			out.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		String city = "venezia";
		String type = "CallOut";
		RegionMap rm = (RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/tic-"+city+"-caps.ser"));
		TimeDensityFromAggregatedData td = new TimeDensityFromAggregatedData(city,type,Config.getInstance().dataset_folder+"/TI-CHALLENGE-2015/TELECOM/"+city+"/"+type+".tar.gz",new int[]{0,1,2,3},null,rm);
		//td.plot("3693_3_1_3_1");
		
		/*
		double fdtw = StatsUtils.fdtw(td.map.get("3693_3_1_3_1"), td.map.get("3693_3_1_2_1"));
		System.out.println("fdtw = "+fdtw);
		
		double dtw = StatsUtils.dtw(td.map.get("3693_3_1_3_1"), td.map.get("3693_3_1_2_1"));
		System.out.println("dtw = "+dtw);
		
		double ed = StatsUtils.ed(td.map.get("3693_3_1_3_1"), td.map.get("3693_3_1_2_1"));
		System.out.println("ed = "+ed);
		*/
		System.out.println("Done!");
	}
		
		
		
}
