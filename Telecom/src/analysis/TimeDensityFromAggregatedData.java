package analysis;

import java.io.BufferedReader;
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

import utils.Config;
import utils.StatsUtils;
import utils.time.TimeConverter;
import visual.html.GoogleChartGraph;
import visual.r.RPlotter;

public class TimeDensityFromAggregatedData {
	
	private String city;
	private String type;
	
	public TimeConverter tc = null;
	public Map<String,double[]> map = new HashMap<String,double[]>();
	
	
	public TimeDensityFromAggregatedData(String city, String type, String file, int[] readIndexes, SynchConstraints constraint) {
		this.city = city;
		this.type = type+"-"+constraint.title;
		try {
			tc = TimeConverter.getInstance();
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
			
		}catch(Exception e) {
			e.printStackTrace();
		}	
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
			       //double calltime = Double.parseDouble(x[4]);
				       double[] v = map.get(cell);
				       if(v == null) {
				       	   v = new double[tc.getTimeSize()];
				       	   map.put(cell, v);
				       }
				       //System.out.println(time+" --> "+new Date(time));
				       v[tc.time2index(time)]+= value;
			       }
			}
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
		TimeDensityFromAggregatedData td = new TimeDensityFromAggregatedData(city,type,"G:/DATASET/TI-CHALLENGE-2015/TELECOM/"+city+"/"+type+".tar.gz",new int[]{0,1,2,3},null);
		//td.plot("3693_3_1_3_1");
		
		double fdtw = StatsUtils.fdtw(td.map.get("3693_3_1_3_1"), td.map.get("3693_3_1_2_1"));
		System.out.println("fdtw = "+fdtw);
		
		double dtw = StatsUtils.dtw(td.map.get("3693_3_1_3_1"), td.map.get("3693_3_1_2_1"));
		System.out.println("dtw = "+dtw);
		
		double ed = StatsUtils.ed(td.map.get("3693_3_1_3_1"), td.map.get("3693_3_1_2_1"));
		System.out.println("ed = "+ed);
		
		System.out.println("Done!");
	}
		
		
		
}
