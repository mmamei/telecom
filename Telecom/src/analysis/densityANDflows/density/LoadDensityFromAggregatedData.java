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
		
		String city = "venezia";
		Calendar start_time = new GregorianCalendar(2015,Calendar.MARCH,28,0,0,0);
		Calendar end_time = new GregorianCalendar(2015,Calendar.MARCH,28,23,59,59);
		
		//AddMap density = loadFromPresenceFile(city,start_time,end_time);
		AddMap density = loadFromTelecomDataFile(city,"SmsIn",start_time,end_time);
		
		RegionMap rm = (RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/tic-"+city+"-gird.ser"));
		
		//KMLHeatMap.drawHeatMap(Config.getInstance().base_folder+"/TIC2015/"+city+".kml",density,rm,"presence",false);
		//RPlotter.drawHeatMap(Config.getInstance().base_folder+"/TIC2015/"+city+".pdf", density, rm, false, "presence");
		
		
		TimeDensityFromAggregatedData td = new TimeDensityFromAggregatedData("venezia","CallIn");
		td.add(new TimeDensityFromAggregatedData(td.getCity(),"CallOut"));
		td.add(new TimeDensityFromAggregatedData(td.getCity(),"SmsIn"));
		td.add(new TimeDensityFromAggregatedData(td.getCity(),"SmsOut"));
		
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
		
		KMLHeatMap.drawHeatMap(Config.getInstance().base_folder+"/TIC2015/"+city+"-"+td.getType()+".kml",density,rm,desc,false);
		
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
