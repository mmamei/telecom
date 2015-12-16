package cdraggregated;

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

import otherdata.TIbigdatachallenge2015.MEF_IRPEF_BLOG;
import JavaMI.Entropy;
import region.RegionI;
import region.RegionMap;
import utils.Config;
import utils.CopyAndSerializationUtils;
import utils.Sort;
import utils.StatsUtils;
import utils.time.TimeConverter;
import visual.html.GoogleChartGraph;
import visual.kml.KMLHeatMap;
import visual.r.RPlotter;


/*
 * This class analyzes telecom big data challenge demographic dataset.
 * For each region (e.g., comune) of the associated region map (mr) it group the calls from that region by the regions of a second region map (meta_rm).
 * In this way it is possible to see that the calls from a given comune are generate primarily by redident of the comunui x,y,z.
 * The class produces a KML showing this.
 * In addition it computes a mesure for each region associated to the entorty of regions comprising the calls
 * 
 */



public class TimeDensityFromAggregatedDataMulti {
	
	private String city;
	private String type;
	
	public TimeConverter tc = null;
	private Map<String,Map<String,double[]>> map = new HashMap<String,Map<String,double[]>>();
	
	private RegionMap gridMap;
	private RegionMap rm;
	private RegionMap meta_rm;
	
	
		
	public TimeDensityFromAggregatedDataMulti(String city, String type, String file, int[] readIndexes, SynchConstraints constraint, RegionMap rm, RegionMap meta_rm) {
		this.city = city;
		this.type = constraint==null ? type : type+"-"+constraint.title;
		this.rm = rm;
		this.meta_rm = meta_rm;
		if(rm!=null) {
			// load the grid map to be used for conversion
			gridMap = (RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/tic-"+city+"-grid.ser"));
		}
		
		try {
			tc = TimeConverter.getInstance();
			
			String constr_title = constraint == null ? "" : constraint.title;
			
			File dir = new File(Config.getInstance().base_folder+"/TIC2015/cache/multi");
			File f = new File(dir+"/multi-"+city+"-"+type+"-"+rm.getName()+"-"+meta_rm.getName()+"-"+constr_title+".ser");
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
				dir.mkdirs();
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
			 if(NO_ZERO && kind.equals("0")) continue;
			 if(NO_TERNI && (kind.equals("55032") || kind.equals("TR") || kind.equals("UMBRIA"))) continue;
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
			       
			       
			       // extra time constraint
			       if(time < TimeConverter.getInstance().startTime || time > TimeConverter.getInstance().endTime) {
			    	   continue;
			       }
			       
			       
			       String cell = x[readIndexes[1]];   
			       double value = Double.parseDouble(x[readIndexes[2]]);
			       String meta = x[readIndexes[3]];
			       meta = projectFromComuni2012_to_meta_rm(meta);
			       if(meta == null) {
			    	   //System.out.println( x[readIndexes[3]]);
			    	   continue;
			       }
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
	
	
	public String projectFromComuni2012_to_meta_rm(String comune) {
		if(meta_rm.getName().equals("comuni2012")) return comune;
		else if(meta_rm.getName().equals("prov2011")) return TIC2015_ComuniProvincieRegioniConverter.getInstance().comuni2provincie(comune);
		else if(meta_rm.getName().equals("regioni")) return TIC2015_ComuniProvincieRegioniConverter.getInstance().comuni2regioni(comune);
		else System.err.println("Unsupported meta aggregation");
		return null;
	}

	
	public String getCity() {
		return city;
	}
	
	public String getType() {
		return type;
	}
	
	
	private static final int MAX_META = 10;
	public static boolean NO_ZERO = true;
	public static boolean NO_TERNI = true;
	public static boolean LOG = false;
	public static boolean LOGH = true;
	public Map<String,Double> plot() {
		
		
		Map<String,Double> density = new HashMap<String,Double>();
		Map<String,String> desc = new HashMap<String,String>();
		
		for(RegionI r: rm.getRegions()) {
			if(map.get(r.getName()) == null) continue; 
			
			
			//System.out.println(r.getName());
			
			Map<String,Double> sum = getSum(r.getName());
			
			String[] names = new String[Math.min(MAX_META, sum.size())];
			double[] v = new double[names.length];
			int i=0;
			Map<String,String> id2name = MEF_IRPEF_BLOG.id2name();
			for(String m: sum.keySet()) {
				names[i] = meta_rm.getName().equals("comuni2012") && id2name.get(m)!=null ? m+":"+id2name.get(m).replaceAll("'", "") : m;
				v[i] = sum.get(m);
				i++;
				if(i >= names.length) break;
			}
			
			/*
			System.out.print("[");
			for(i=0;i<v.length;i++)
				System.out.print(v[i]+" ");
			System.out.print("] --> ");
			System.out.println((Entropy.calculateEntropy(v)));
			*/
			
			density.put(r.getName(), Entropy.calculateEntropy(v));
			
			List<double[]> y = new ArrayList<double[]>();
			
			
			double[] w = new double[v.length];
			for(i=0; i<w.length;i++)
				w[i] = LOG ? Math.log(v[i]) : v[i];
			
			y.add(w);
			List<String> l = new ArrayList<String>();
			l.add(meta_rm.getName() + (LOG?"(Log)":""));
			
			desc.put(r.getName(),GoogleChartGraph.getHist(names, y, l, meta_rm.getName(), "%"));
		}
		

		try {
			String orig_name = rm.getName();
			rm.setName(city+"-multi-"+meta_rm.getName());
			KMLHeatMap.drawHeatMap(Config.getInstance().base_folder+"/TIC2015/"+city+"-multi-"+meta_rm.getName()+"-.kml",density,rm,desc,LOGH);
			rm.setName(orig_name);
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return density;
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
			if(cont > MAX_META) break;
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
		RPlotter.drawBar(n, sumv, meta_rm.getName(), "tot. cont", Config.getInstance().base_folder+"/Images/tdc-sum-multi-"+city+"-"+type+".pdf", null);
		
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
	
	
	
	
	public static final int COMUNI = 0;
	public static final int PROVINCIE = 1;
	public static final int REGIONI = 2;
	public static final int USE_META_RM = COMUNI;
	
	public static void main(String[] args) {
		
		RegionMap meta_rm = null;
		if(USE_META_RM == COMUNI) meta_rm = (RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/comuni2012.ser"));
		if(USE_META_RM == PROVINCIE) meta_rm = (RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/prov2011.ser"));
		if(USE_META_RM == REGIONI) meta_rm = (RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/regioni.ser"));
		
		String type = "Demo";
		int[] readIndexes = new int[]{0,1,3,2};
		
		String[] cities = new String[]{
				"caltanissetta",
				"siracusa",
				"benevento",
				"palermo",
				"campobasso",
				"napoli",
				"asti",
				"bari",
				"ravenna",
				"ferrara",
				"venezia",
				"torino",
				"modena",
				"roma",
				"siena",
				"milano"
		};
		
		List<String> ln = new ArrayList<String>();
		List<double[]> lv = new ArrayList<double[]>();
		
		for(String city: cities) {
			System.out.println("Processing "+city);
			RegionMap rm = (RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/tic-comuni2012-"+city+".ser"));
			TimeDensityFromAggregatedDataMulti td = new TimeDensityFromAggregatedDataMulti(city,type,Config.getInstance().dataset_folder+"/TI-CHALLENGE-2015/DEMOGRAPHIC/"+city+"/callsLM_"+city.substring(0,2).toUpperCase()+"_COMUNI2012",readIndexes,null,rm,meta_rm);
			Map<String,Double> density = td.plot();
			
			ln.add(city);
			lv.add(SynchAnalysis.density2array(density));
		}
		
		RPlotter.drawBoxplot(lv,ln,"comuni2012","multi",Config.getInstance().base_folder+"/Images/boxplot-multi-"+meta_rm.getName()+".pdf",10,null);
	
		System.out.println("Done!");
	}
		
		
		
}

