package cdrindividual.densityANDflows.flows;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Map;

import region.RegionI;
import region.RegionMap;
import utils.Config;

public class ODMatrixPrinter {
	
	public static void print(String basedir, Map<Move,Double> list_od, RegionMap rm) {
		try {
			File dir = new File(Config.getInstance().base_folder+"/ODMatrix/"+basedir);
			dir.mkdirs();
			
			PrintWriter out = new PrintWriter(new FileWriter(dir+"/latlon.csv"));
			for(RegionI r: rm.getRegions()) {
				double[] latlon = r.getLatLon();
				out.println(r.getName()+"\t"+latlon[0]+","+latlon[1]);
			}
			out.close();
			
			
			out = new PrintWriter(new FileWriter(dir+"/od.csv"));
			
			// print header
			out.print("O\\D");
			for(RegionI d: rm.getRegions())
				out.print("\t"+d.getName());
			out.println();
			
			
			for(RegionI o: rm.getRegions()) {
				out.print(o.getName());
				for(RegionI d: rm.getRegions()) {
					Double v = list_od.get(new Move(o,d));
					if(v == null) v = 0.0;
					out.print("\t"+v);
				}
				out.println();
			}
			out.close();
			
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	
	
}
