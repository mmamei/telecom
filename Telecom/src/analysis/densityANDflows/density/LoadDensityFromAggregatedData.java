package analysis.densityANDflows.density;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

import region.RegionMap;
import utils.AddMap;
import utils.Config;
import utils.CopyAndSerializationUtils;
import utils.StatsUtils;
import utils.time.TimeConverter;
import visual.html.GoogleChartGraph;
import visual.kml.KMLHeatMap;
import visual.r.RPlotter;
import analysis.TimeDensityFromAggregatedData;

public class LoadDensityFromAggregatedData {
	
	/*
	 * This class load density from aggregated data file, like those used in the Telecom Data Challenge
	 */
	
	
	public static void main(String[] args) throws Exception {
		String[] city = new String[]{"torino","milano","venezia","roma","napoli","bari","palermo"};
		for(String c: city)
			process(c,new String[]{"01-32","grande",""});
		System.out.println("Done");
	}	
	
	public static void process(String city, String[] constraints) throws Exception {
		
		System.out.println("Processing "+city);
		
		//Calendar start_time = new GregorianCalendar(2015,Calendar.MARCH,28,0,0,0);
		//Calendar end_time = new GregorianCalendar(2015,Calendar.MARCH,28,23,59,59);
		
		//AddMap density = loadFromPresenceFile(city,start_time,end_time);
		//AddMap density = loadFromTelecomDataFile(city,"CallIn",start_time,end_time);
		
		LinkedHashMap<String, Double> company_map = LoadDensityFromCompanyData.getInstance(city,constraints);
		Map<String,Double> density = new HashMap<String,Double>();
		int c = 0;
		for(String k: company_map.keySet()) {
			if(company_map.get(k)==null) continue;
			density.put(k, company_map.get(k));
			c++;
			if(c > 10) break;
		}
		
		RegionMap rm = (RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/tic-"+city+"-gird.ser"));
		
		//KMLHeatMap.drawHeatMap(Config.getInstance().base_folder+"/TIC2015/"+city+".kml",density,rm,"presence",false);
		//RPlotter.drawHeatMap(Config.getInstance().base_folder+"/TIC2015/"+city+".pdf", density, rm, false, "presence");
		
		String[] types = new String[]{"CallIn","CallOut","SmsIn","SmsOut"};
		TimeDensityFromAggregatedData td = new TimeDensityFromAggregatedData(city,types[0],"G:/DATASET/TI-CHALLENGE-2015/TELECOM/"+city+"/"+types[0]+".tar.gz",new int[]{0,1,2,3},null);
		for(int i=1; i<types.length;i++)
			td.add(new TimeDensityFromAggregatedData(td.getCity(),types[i],"G:/DATASET/TI-CHALLENGE-2015/TELECOM/"+city+"/"+types[i]+".tar.gz",new int[]{0,1,2,3},null));
		
		// compute z-score
		for(String k: td.map.keySet())
			td.map.put(k, StatsUtils.getZ(td.map.get(k)));
		
		
		TimeConverter tc = TimeConverter.getInstance();
		String[] x = tc.getTimeLabels();
		
		Map<String,String> desc = new HashMap<String,String>();
		for(String r: density.keySet()) {
				
			List<double[]> y = new ArrayList<double[]>();
			y.add(td.map.get(r));
			
			
			List<String> names = new ArrayList<String>();
			names.add(td.getType());
	
			desc.put(r, td.map.get(r)==null ? "not available" : GoogleChartGraph.getGraph(x, y, names, "data", td.getType()));
		}
		String suffix = (constraints == null) ? "" :  "-"+constraints[0]+"-"+constraints[1]+"-"+constraints[2];
		KMLHeatMap.drawHeatMap(Config.getInstance().base_folder+"/TIC2015/"+city+suffix+"-"+td.getType()+".kml",density,rm,desc,false);
		
	}
	
	
	
	private static AddMap loadFromPresenceFile(String city, Calendar start_time, Calendar end_time) {
		
		AddMap density = new AddMap();
		
		try {
		
			long start = start_time.getTimeInMillis();
			long end = end_time.getTimeInMillis();
			
			
			BufferedReader br = new BufferedReader(new FileReader("G:/DATASET/TI-CHALLENGE-2015/PRESENCE/presence-"+city+".csv"));
			String line;
			while((line=br.readLine())!=null) {
				String[] e = line.split(",");
				String cell_id = e[0];
				double count = Double.parseDouble(e[1]);
				long timestamp = Long.parseLong(e[2]);
				if(timestamp >= start && timestamp <= end) {
					//System.out.println(new Date(timestamp));
					density.add(cell_id, count);
				}
			}
			br.close();
		
		} catch(Exception e) {
			e.printStackTrace();
			
		}
		return density;
	}
	
	
	private static AddMap loadFromTelecomDataFile(String city, String type, Calendar start_time, Calendar end_time) {
		AddMap density = new AddMap();
		String line = null;
		try {
			
			long start = start_time.getTimeInMillis();
			long end = end_time.getTimeInMillis();
			
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
			       if(start <= time && time <= end)
			    	   density.add(cell, ncall);
			    }
			    currentEntry = tarInput.getNextTarEntry(); 
			}
			tarInput.close();
		}catch(Exception e) {
			System.err.println(line);
			e.printStackTrace();
		}
		return density;
	}

}
