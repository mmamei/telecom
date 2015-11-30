package cdrindividual.densityANDflows.flows;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Map;

import region.RegionI;
import region.RegionMap;
import utils.Config;

public class ODMatrixPrinter {
	
	public static void print(String basedir, Map<Move,Double> list_od, RegionMap rm, String method) {
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
			
			printHeader(out,method,rm.getName());
			
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
	
	
	public static void printHeader(PrintWriter out, String method, String region) {
		out.println("--------------------------------------------------------------");
		out.println("Matrice Origine Destinazione");
		out.println("--------------------------------------------------------------");
		out.println("- Metodo: "+method);
		out.println("- Zona interessata: "+region);
		out.println("- Istante di inizio: Mon, 14 Sep 2015 23:00");
		out.println("- Soglia per italiani: 8");
		out.println("- Applicato filtro privacy: 1");
		out.println("- # di utenti su utenti del campione: 32300 su 631560");
		out.println("- Tipologia utenti: Tutti");
		out.println(" - Istante di fine: Tue, 15 Sep 2015 00:00");
		out.println("- Soglia per stranieri: 8");
		out.println("--------------------------------------------------------------");
		out.println(); // la quattordicesima riga del file è vuota
	}
	
	
}
