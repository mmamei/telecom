package analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

import region.RegionI;
import region.RegionMap;
import utils.Config;
import utils.CopyAndSerializationUtils;
import utils.Sort;
import utils.StatsUtils;
import utils.time.TimeConverter;
import visual.html.GoogleChartGraph;
import visual.r.RPlotter;

public class TimeDensityFromAggregatedDataMulti {
	
	private String city;
	private String type;
	
	public TimeConverter tc = null;
	private Map<String,Map<String,double[]>> map = new HashMap<String,Map<String,double[]>>();
	
	private RegionMap gridMap;
	private RegionMap rm;
	
	
	public TimeDensityFromAggregatedDataMulti(String city, String type, String file, int[] readIndexes, SynchConstraints constraint) {
		this(city,type,file,readIndexes,constraint,null);	
	}
		
	public TimeDensityFromAggregatedDataMulti(String city, String type, String file, int[] readIndexes, SynchConstraints constraint, RegionMap rm) {
		this.city = city;
		this.type = constraint==null ? type : type+"-"+constraint.title;
		this.rm = rm;
		if(rm!=null) {
			// load the grid map to be used for conversion
			gridMap = (RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/tic-"+city+"-gird.ser"));
		}
		
		try {
			tc = TimeConverter.getInstance();
			
			String constr_title = constraint == null ? "" : constraint.title;
			
			File f = new File(Config.getInstance().base_folder+"/TIC2015/cache/multi-"+city+"-"+type+"-"+new File(file).getName()+"-"+constr_title+".ser");
			if(f.exists()) {
				map = (Map<String,Map<String,double[]>>)CopyAndSerializationUtils.restore(f);
			}
			else {
			
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
					processFile(new BufferedReader(new FileReader(file)),readIndexes,constraint);
				}
				
				CopyAndSerializationUtils.save(f, map);
				
			}
			
			
			
		}catch(Exception e) {
			e.printStackTrace();
		}	
	}
	
	
	
	
	public Map<String,double[]> get(String r) {
		Map<String,double[]> t = map.get(r);
		return t;
	}
	
	public double[] get(String r, String kind) {
		double[] t = map.get(r).get(kind);
		if(t == null)
			t = new double[tc.getTimeSize()];
		return t;
	}
	
	public Map<String,Double> getSum(String r) {
		Map<String,double[]> t = map.get(r);
		 Map<String,Double> m = new  HashMap<String,Double>();
		 for(String kind: t.keySet()) {
			 double sum = 0;
			 for(double v: t.get(kind))
				 sum+=v;
			 m.put(kind, sum);
		 }
		
		 
		 LinkedHashMap<String, Double> sorted = Sort.sortHashMapByValuesD(m,Collections.reverseOrder());
		 return sorted;
	}
	
	
	
	
	
	public int size() {
		return map.size();
	}
	
	private void processFile(BufferedReader br,int[] readIndexes, SynchConstraints constraint) {
		
		
		String line = null;
		try {
			while ((line = br.readLine()) != null) {
			       String[] x = line.split("\t");
			       long time = Long.parseLong(x[readIndexes[0]]) * 1000;
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
			    				   Map<String,double[]> mr = map.get(r.getName());
			    				   if(mr == null) {
			    					   mr = new HashMap<String,double[]>();//;
			    					   map.put(r.getName(), mr);
			    					   
			    				   }
			    				   double[] v = mr.get(meta);
			    				   if(v == null) {
			    					   v = new double[tc.getTimeSize()];
			    					   mr.put(meta, v);
			    				   }
			    				   //System.out.println(time+" --> "+new Date(time));
			    				   v[tc.time2index(time)]+= value * areas[i];
			    			   }
				       }
			    	   else {
			    		   
			    		   Map<String,double[]> mr = map.get(cell);
			    		   if(mr == null) {
			    			   mr = new HashMap<String,double[]>();//;
	    					   map.put(cell, mr);
			    		   }
			    		   double[] v = mr.get(meta);
					       if(v == null) {
					       	   v = new double[tc.getTimeSize()];
					       	   mr.put(cell, v);
					       }
					       //System.out.println(time+" --> "+new Date(time));
					       v[tc.time2index(time)]+= value;
			    	   }
			       }
			}
			//br.close();
		}catch(Exception e) {
			System.err.println(line);
			e.printStackTrace();
		}	
	}

	
	public String getCity() {
		return city;
	}
	
	public String getType() {
		return type;
	}
	
	public void plotR(String cell) {
		String[] x = tc.getTimeLabels();
		
		Map<String,Double> sum = this.getSum(cell);
		List<String> names = new ArrayList<String>();
		List<double[]> v = new ArrayList<double[]>();
		int cont = 0;
		for(String meta: sum.keySet()) {
			names.add(meta);
			v.add(map.get(cell).get(meta));
			cont++;
			if(cont > 5) break;
		}
		RPlotter.drawLine(x, v, names, "cell", "date", city+" "+type, Config.getInstance().base_folder+"/Images/tdc-multi-"+city+"-"+type+".pdf", null);
		
		
		String[] n = new String[names.size()];
		double[] sumv = new double[n.length];
		cont = 0;
		for(String meta: sum.keySet()) {
			n[cont] = meta;
			sumv[cont] = sum.get(meta);
			cont++;
			if(cont > 5) break;
		}
		RPlotter.drawBar(n, sumv, "meta", "tot. cont", Config.getInstance().base_folder+"/Images/tdc-sum-multi-"+city+"-"+type+".pdf", null);
		
		/*
		try {
			PrintWriter out = new PrintWriter(new FileWriter(Config.getInstance().base_folder+"/Images/tdc-multi-"+city+"-"+type+".html"));
			out.println(GoogleChartGraph.getGraph(x, v, names, "date", type));
			out.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
		*/
	}
	
	public static void main(String[] args) {
		String city = "venezia";
		String type = "Demo";
		int[] readIndexes = new int[]{0,1,3,2};
		RegionMap rm = (RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/tic-"+city+"-caps.ser"));
		//TimeDensityFromAggregatedDataMulti td = new TimeDensityFromAggregatedDataMulti(city,type,Config.getInstance().dataset_folder+"/TI-CHALLENGE-2015/TELECOM/"+city+"/"+type+".tar.gz",readIndexes,null,rm);
		TimeDensityFromAggregatedDataMulti td = new TimeDensityFromAggregatedDataMulti(city,type,Config.getInstance().dataset_folder+"/TI-CHALLENGE-2015/DEMOGRAPHIC/"+city+"/callsLM_"+city.substring(0,2).toUpperCase()+"_CAP",readIndexes,null,rm);
		
		 
		
		
		td.plotR("30121");
	
		System.out.println("Done!");
	}
		
		
		
}
