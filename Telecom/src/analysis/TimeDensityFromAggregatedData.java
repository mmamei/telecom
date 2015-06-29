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
import utils.time.TimeConverter;
import visual.html.GoogleChartGraph;
import visual.r.RPlotter;

public class TimeDensityFromAggregatedData {
	
	private TimeConverter tc = null;
	private Map<String,double[]> map = new HashMap<String,double[]>();
	
	
	public TimeDensityFromAggregatedData(String city, String type) {
		try {
			tc = TimeConverter.getInstance();
			String file = "G:/DATASET/TI-CHALLENGE-2015/TELECOM/"+city+"/"+type+".tar.gz";
			TarArchiveInputStream tarInput = new TarArchiveInputStream(new GzipCompressorInputStream(new FileInputStream(file)));
			TarArchiveEntry currentEntry = tarInput.getNextTarEntry();
			BufferedReader br = null;
			while (currentEntry != null) {
			    br = new BufferedReader(new InputStreamReader(tarInput)); // Read directly from tarInput
			    System.out.println("Reading File = " + currentEntry.getName());
			    String line;
			    while ((line = br.readLine()) != null) {
			       String[] x = line.split("\t");
			       long time = Long.parseLong(x[0]) * 1000;
			       String cell = x[1];
			       double ncall = Double.parseDouble(x[2]);
			       String mcc = x[3];
			       double calltime = Double.parseDouble(x[4]);
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
			e.printStackTrace();
		}	
	}
	
	public void plot(String cell) {
		String[] x = tc.getTimeLabels();

		List<double[]> v = new ArrayList<double[]>();
		v.add(map.get(cell));
		
		List<String> names = new ArrayList<String>();
		names.add(cell);
		
		
		//RPlotter.drawLine(x, v, names, "cell", "date", "CallIn", Config.getInstance().base_folder+"/Images/tdc.pdf", null);
		
		try {
			PrintWriter out = new PrintWriter(new FileWriter(Config.getInstance().base_folder+"/Images/tdc.html"));
			out.println(GoogleChartGraph.getGraph(x, v, names, "date", "CallIn"));
			out.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		TimeDensityFromAggregatedData td = new TimeDensityFromAggregatedData("venezia","CallIn");
		td.plot("3693_3_1_3_1");
				
		System.out.println("Done!");
	}
		
		
		
}
