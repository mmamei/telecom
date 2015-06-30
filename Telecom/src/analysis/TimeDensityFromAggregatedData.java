package analysis;

import java.io.BufferedReader;
import java.io.FileInputStream;
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
import utils.fastdtw.dtw.DTW;
import utils.fastdtw.dtw.FastDTW;
import utils.fastdtw.timeseries.TimeSeries;
import utils.fastdtw.timeseries.TimeSeriesPoint;
import utils.fastdtw.util.DistanceFunctionFactory;
import utils.time.TimeConverter;
import visual.html.GoogleChartGraph;
import visual.r.RPlotter;

public class TimeDensityFromAggregatedData {
	
	private String city;
	private String type;
	
	private TimeConverter tc = null;
	public Map<String,double[]> map = new HashMap<String,double[]>();
	
	
	public TimeDensityFromAggregatedData(String city, String type) {
		
		
		
		this.city = city;
		this.type = type;
		String line = null;
		try {
			tc = TimeConverter.getInstance();
			String file = "G:/DATASET/TI-CHALLENGE-2015/TELECOM/"+city+"/"+type+".tar.gz";
			TarArchiveInputStream tarInput = new TarArchiveInputStream(new GzipCompressorInputStream(new FileInputStream(file)));
			TarArchiveEntry currentEntry = tarInput.getNextTarEntry();
			BufferedReader br = null;
			while (currentEntry != null) {
			    br = new BufferedReader(new InputStreamReader(tarInput)); // Read directly from tarInput
			    //System.out.println("Reading File = " + currentEntry.getName());
			   
			    while ((line = br.readLine()) != null) {
			       String[] x = line.split("\t");
			       long time = Long.parseLong(x[0]) * 1000;
			       String cell = x[1];
			       double ncall = Double.parseDouble(x[2]);
			       String mcc = x[3];
			       //double calltime = Double.parseDouble(x[4]);
			       double[] v = map.get(cell);
			       if(v == null) {
			       	   v = new double[tc.getTimeSize()];
			       	   map.put(cell, v);
			       }
			       //System.out.println(time+" --> "+new Date(time));
			       v[tc.time2index(time)]+= ncall;
			    }
			    currentEntry = tarInput.getNextTarEntry(); 
			}
			tarInput.close();
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
	
	
	// euclidean distance
	private double ed(double[] x, double[] y) {
		double d = 0;
		for(int i=0; i<x.length;i++)
			d+= Math.pow(x[i]-y[i], 2);
		return Math.sqrt(d);
	}
	
	private double dtw(double[] x, double[] y) {
		TimeSeries tsI = new TimeSeries(1);
		for(int i=0; i<x.length;i++)
			tsI.addLast(i, new TimeSeriesPoint(new double[] {x[i]}));
		
		TimeSeries tsJ = new TimeSeries(1);
		for(int i=0; i<y.length;i++)
			tsJ.addLast(i, new TimeSeriesPoint(new double[] {y[i]}));
		
		return DTW.getWarpDistBetween(tsI, tsJ, DistanceFunctionFactory.EUCLIDEAN_DIST_FN);
	}
	
	private double fdtw(double[] x, double[] y) {
		TimeSeries tsI = new TimeSeries(1);
		for(int i=0; i<x.length;i++)
			tsI.addLast(i, new TimeSeriesPoint(new double[] {x[i]}));
		
		TimeSeries tsJ = new TimeSeries(1);
		for(int i=0; i<y.length;i++)
			tsJ.addLast(i, new TimeSeriesPoint(new double[] {y[i]}));
		
		return FastDTW.getWarpDistBetween(tsI, tsJ, DistanceFunctionFactory.EUCLIDEAN_DIST_FN);
	}


	
	public String getCity() {
		return city;
	}
	
	public String getType() {
		return type;
	}
	
	public void plot(String cell) {
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
		TimeDensityFromAggregatedData td = new TimeDensityFromAggregatedData("venezia","CallOut");
		//td.plot("3693_3_1_3_1");
		
		double fdtw = td.fdtw(td.map.get("3693_3_1_3_1"), td.map.get("3693_3_1_2_1"));
		System.out.println("fdtw = "+fdtw);
		
		double dtw = td.dtw(td.map.get("3693_3_1_3_1"), td.map.get("3693_3_1_2_1"));
		System.out.println("dtw = "+dtw);
		
		double ed = td.ed(td.map.get("3693_3_1_3_1"), td.map.get("3693_3_1_2_1"));
		System.out.println("ed = "+ed);
		
		System.out.println("Done!");
	}
		
		
		
}
