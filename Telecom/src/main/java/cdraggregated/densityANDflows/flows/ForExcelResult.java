package cdraggregated.densityANDflows.flows;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;

import utils.CopyAndSerializationUtils;
import utils.mod.Route;
import utils.mod.Util;

public class ForExcelResult {

	static String percorsoInput = "D:/CODE/Project";
	static String MODAvIstat="D:/CODE/Project/temp/MatriceOD_per_-_Lombardia_average_from_Istat__orario_uscita_-_1.csv";
	public static void main(String[] args) throws Exception{
		
		FileWriter fw = new FileWriter(new File(percorsoInput+"/Excel/infoOk.csv"));
		
		@SuppressWarnings("unchecked")
		HashMap<String, Double> traffic = (HashMap<String, Double>) CopyAndSerializationUtils.restore(new File(percorsoInput+"/temp/HashMaps/HMtimesTraffic.dat"));
		
		@SuppressWarnings("unchecked")
		HashMap<String, Double> oldTraffic = (HashMap<String, Double>) CopyAndSerializationUtils.restore(new File(percorsoInput+"/temp/HashMaps/HMtimesOldTraffic.dat"));

//		@SuppressWarnings("unchecked")
//		HashMap<String, Double> fastest2 = (HashMap<String, Double>) Util.restore(new File(percorsoInput+"/temp/HashMaps/HMtimesFastest2.dat"));
		@SuppressWarnings("unchecked")
		HashMap<String, Double> fastest = (HashMap<String, Double>) CopyAndSerializationUtils.restore(new File(percorsoInput+"/temp/HashMaps/HMtimesFastest.dat"));
				
		System.out.println(oldTraffic.size());
		for(String k:traffic.keySet()){
			System.out.println(traffic.get(k)+" "+oldTraffic.get(k)+"  "+fastest.get(k));
		}
		
		if(traffic.size()!=fastest.size()) System.out.println("errore nella creazione delle hashMap");
		if(traffic.size()!=oldTraffic.size()) System.out.println("errore nella creazione delle hashMap");

		HashMap<String, Route> routesIstat = new HashMap<String, Route>();
		
		BufferedReader bri = new BufferedReader(new FileReader(new File(MODAvIstat)));
		for(int i=0; i<14; i++)	bri.readLine();	//skip header
		String line = bri.readLine();
		String ci[] = line.split("\t");
		
//		leggo le medie istat
		for(int i=1; i<ci.length; i++){
//			System.out.println(i+" su "+ci.length);
			line = bri.readLine();
			String v[]= line.split("\t");
			for(int j=1; j<v.length; j++){		
				if(!v[j].equals("0.0")&&traffic.containsKey(ci[i]+":"+ci[j])){
					Route r = new Route(ci[i]+":"+ci[j]);
					String mv[]=v[j].split(":");
					r.setVarianzaIstat(Double.parseDouble(mv[1]));
					r.setMediaIstat(Double.parseDouble(mv[0]));
					routesIstat.put(ci[i]+":"+ci[j], r);
				}
			}
		}
		bri.close();
		
//		le inserisco in un array di Routes e inserisco i dati per traffic e fastest
		Route[] routes = new Route[routesIstat.size()];
		int c=0;
		for(String k:routesIstat.keySet()){
			routes[c]=routesIstat.get(k).setMediaProject(traffic.get(k)).setMediaFastest(fastest.get(k)).setMediaOldT(oldTraffic.get(k));
			c++;
			System.out.println(c+" su "+routesIstat.size());
		}
		traffic.clear();
		fastest.clear();
		routesIstat.clear();
		

//		ora devo ordinare routes in ordine cresciente di media Istat
		for(int i=0; i<routes.length; i++){
			boolean b = false;
			for(int j=0; j<routes.length-1; j++){
				if(routes[j].getMediaIstat()>routes[j+1].getMediaIstat()){
					Route r = routes[j];
					routes[j] = routes[j+1];
					routes[j+1] = r;
					b=true;
				}
				else{
					if(routes[j].getMediaIstat()==routes[j+1].getMediaIstat()){
						if(routes[j].getMediaProject()>routes[j+1].getMediaProject()){
							Route r = routes[j];
							routes[j] = routes[j+1];
							routes[j+1] = r;
							b=true;
						}
					}
				}
			}
			if(!b) i=routes.length; 
		}
		//trascrivo
		BufferedWriter bw = new BufferedWriter(fw);
		for(int i=0; i<routes.length; i++){
			if(routes[i].getMediaProject()!=0.0){
				bw.write(routes[i].getId()+"\t"+(routes[i].getMediaIstat()+"\t"
					+ "\t"+routes[i].getMediaFastest()+"\t"+routes[i].getMediaProject()).replace(".", ",")+"\n");
//				System.out.println(routes[i].getId()+"\t"+(routes[i].getMediaIstat()+"\t"
//						+ "\t"+routes[i].getMediaFastest()+"\t"+routes[i].getMediaProject()).replace(".", ",")+"\n");
//		
			}
		}
		System.out.println("FINE");
		bw.close();
		
	}
		
}
