package analysis.densityANDflows.density;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import region.RegionMap;
import utils.AddMap;
import utils.Config;
import utils.CopyAndSerializationUtils;
import visual.kml.KMLHeatMap;
import visual.r.RPlotter;

public class LoadDensityFromAggregatedData {
	
	/*
	 * This class load density from aggregated data file, like those used in the Telecom Data Challenge
	 */
	
	public static void main(String[] args) throws Exception {
		
		String city = "venezia";
		
		Config.getInstance().pls_start_time = new GregorianCalendar(2015,Calendar.MARCH,28,0,0,0);
		Config.getInstance().pls_end_time = new GregorianCalendar(2015,Calendar.MARCH,28,23,59,59);
		
		AddMap density = new AddMap();
		
		long start = Config.getInstance().pls_start_time.getTimeInMillis();
		long end = Config.getInstance().pls_end_time.getTimeInMillis();
		
		
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
		
		
		RegionMap rm = (RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/tic-"+city+"-gird.ser"));
		
		KMLHeatMap.drawHeatMap(Config.getInstance().base_folder+"/TIC2015/"+city+".kml",density,rm,"presence",false);
		RPlotter.drawHeatMap(Config.getInstance().base_folder+"/TIC2015/"+city+".pdf", density, rm, false, "presence");
		
		
	}

}
