package cdrindividual.densityANDflows.flows;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import region.RegionI;
import region.RegionMap;
import utils.Config;
import utils.CopyAndSerializationUtils;
import cdrindividual.CDR;
import cdrindividual.dataset.impl.DataFactory;

public class ODMatrixTime {
	
	public static void main(String[] args) throws Exception {
		process("file_pls_piem_file_pls_piem_01-06-2015-01-07-2015_minH_0_maxH_25_ABOVE_8limit_20000_cellXHour","odpiemonte.ser","15-06-2015");
	}
	
	public static void process(String fileName, String regionMap, String day) throws Exception {
	
		String in_file = Config.getInstance().base_folder+"/UserCellXHour/"+fileName+".csv";
		
		RegionMap rm = (RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/"+regionMap));
		
		Map<Integer, Map<Move,Double>> hour2listod = new HashMap<>();
		for(int i=0; i<24;i++)
			hour2listod.put(i, new HashMap<Move,Double>());
		
		
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
		Calendar startCal = Calendar.getInstance();
		startCal.setTime(sdf.parse(day));
		
		Calendar endCal = Calendar.getInstance();
		endCal.setTimeInMillis(startCal.getTimeInMillis());
		endCal.add(Calendar.DAY_OF_MONTH, 1);
		
		System.out.println("START TIME = "+startCal.getTime());
		System.out.println("END TIME = "+endCal.getTime());
		
		
		
		RegionMap nm = DataFactory.getNetworkMapFactory().getNetworkMap(startCal.getTimeInMillis());
		
		
		BufferedReader br = new BufferedReader(new FileReader(in_file));
		String line;
		double max = 0;
		while((line=br.readLine())!=null) {
			if(line.startsWith("//")) continue;
			List<CDR> cdrs = CDR.getDataFormUserEventCounterCellacXHourLine(line);
			for(int i=1;i<cdrs.size();i++) {
				CDR a = cdrs.get(i-1);
				CDR b = cdrs.get(i);
				Calendar cala = a.getCalendar();
				if(cala.before(startCal)) continue;
				if(cala.after(endCal)) break;
				
				int h = cala.get(Calendar.HOUR_OF_DAY);
				double[] a_latlon = nm.getRegion(a.getCellac()).getLatLon();
				double[] b_latlon = nm.getRegion(b.getCellac()).getLatLon();
				RegionI r_a = rm.get(a_latlon[1], a_latlon[0]);
				RegionI r_b = rm.get(b_latlon[1], b_latlon[0]);
				if(r_a!=null && r_b!=null) {
					Move m = new Move(r_a,r_b);
					Map<Move,Double> mv = hour2listod.get(h);
					mv.put(m,mv.get(m) == null ? 1.0 : mv.get(m)+1);
					max = Math.max(mv.get(m), max);
				}		
			}	
		}
		br.close();
		System.out.println("max num of movements = "+max);
		// save result in od matrix files
		for(int h: hour2listod.keySet()) {
			ODMatrixPrinter.print("ODMatrixTime_"+fileName+"_"+rm.getName()+"_"+day,hour2listod.get(h),rm,"Time",h,h+1);
		}
		
		
	}
	
}
