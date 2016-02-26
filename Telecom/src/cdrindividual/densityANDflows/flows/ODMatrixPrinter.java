package cdrindividual.densityANDflows.flows;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import region.RegionI;
import region.RegionMap;
import utils.Config;

public class ODMatrixPrinter {
	
	public static void print(String basedir, Map<Move,Double> list_od, RegionMap rm, String method, int minH, int maxH) {
		try {
			File dir = new File(Config.getInstance().base_folder+"/ODMatrix/"+basedir);
			dir.mkdirs();
			
			//System.out.println(basedir);
			
			PrintWriter out = new PrintWriter(new FileWriter(dir+"/latlon.csv"));
			out.println("WTK\tID");
			for(RegionI r: rm.getRegions()) 
				out.println("\""+r.getGeom()+"\"\t"+r.getName());
			out.close();
			
			
			double tot = 0;
			for(double v: list_od.values())
				tot+=v;
			
			
			out = new PrintWriter(new FileWriter(dir+"/od-"+minH+"-"+maxH+"-"+(int)tot+".csv"));
			
			Map<String,String> tm = parseBaseDir(basedir);
			tm.put("minh", String.valueOf(minH));
			tm.put("maxh", String.valueOf(maxH));
			
			
			printHeader(out,method,tm.get("startdate")+" "+tm.get("minh"),tm.get("enddate")+" "+tm.get("maxh"),tm.get("above"),tm.get("limit"),rm.getName());
			
			// print first line
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
	
	private static Map<String,String> REGION2REGION = new HashMap<>();
	static {
		REGION2REGION.put("piem", "Piemonte");
		REGION2REGION.put("lomb", "Lombardia");
	}
	
	//ODMatrixHW_file_pls_piem_file_pls_piem_01-06-2015-01-07-2015_minH_0_maxH_25_ABOVE_400limit_1000_cellXHour_odpiemonte.ser
	public static Map<String,String> parseBaseDir(String file) {
			Map<String,String> tm = new HashMap<>();
			// parse title
			String[] e = file.split("-|_");
			tm.put("region",REGION2REGION.get(e[3]));
			tm.put("startdate", e[7]+"-"+e[8]+"-"+e[9]);
			tm.put("enddate", e[10]+"-"+e[11]+"-"+e[12]);
			tm.put("minh", e[14]);
			tm.put("maxh", e[16]);
			tm.put("above", e[18].replaceAll("limit", ""));
			tm.put("limit", e[19]);
			if(e[21].startsWith("od")) 	tm.put("zone", e[21].substring(2));
			else tm.put("zone", e[21]);
			return tm;
	}
	
	public static void printHeader(PrintWriter out, String method, String startdate, String enddate, String above, String limit, String region) {
		out.println("--------------------------------------------------------------");
		out.println("Matrice Origine Destinazione");
		out.println("--------------------------------------------------------------");
		out.println("- Metodo: "+method);
		out.println("- Zona interessata: "+region);
		out.println("- Istante di inizio: "+startdate);
		out.println("- Soglia per italiani: "+above);
		out.println("- Applicato filtro privacy: 1");
		out.println("- # di utenti su utenti del campione: "+limit);
		out.println("- Tipologia utenti: Tutti");
		out.println(" - Istante di fine: "+enddate);
		out.println("- Soglia per stranieri: "+above);
		out.println("--------------------------------------------------------------");
		out.println(); // la quattordicesima riga del file è vuota
	}
	
	
}
