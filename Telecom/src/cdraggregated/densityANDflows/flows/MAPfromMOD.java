package cdraggregated.densityANDflows.flows;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import utils.mod.CoordinateUtil;
import utils.mod.Route;
import utils.mod.Util;
import utils.mod.Util.Pair;
import utils.mod.Write;
import utils.multithread2.MultiWorker;
import utils.mygraphhopper.MyGHResponse;
import utils.mygraphhopper.MyGraphHopper;
import utils.mygraphhopper.WEdge;

import com.graphhopper.GHRequest;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.index.LocationIndex;
import com.graphhopper.storage.index.QueryResult;
import com.graphhopper.util.DistanceCalc3D;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.PointList;
import com.graphhopper.util.shapes.GHPoint;
import com.vividsolutions.jts.geom.Geometry;

public class MAPfromMOD{
//	nome e percorso file della MOD
	static String fileMOD = "temp/MatriceOD_per_-_Lombardia__orario_uscita_-_1.csv";
//	static String fileMOD = "temp/od.csv";
	
//	Le province visualizzate nel file html che verrà generato
	static String[] provToVisualize = {"015"};
	
//	I comuni visualizzati nel file html che verrà generato
	static String[] comToVisualize = {};
	
	static String percorsoInput = "C:/BASE/Francia";
	static String percorsoOutput = "C:/BASE/Francia/html";
	
//	se true		usa dati con origini e destinazioni generati casualmente in un'area		
//	se false	usa dati con origini e destinazioni puntuali
	static boolean polyMode = true;
	static String weight = "traffic";
	//	quanto viene preso in considerazione il "traffico statico"	 
	//  funziona solo per lombardia
	//  guarda il tasso di motorizzazione e alloca t = 0.1 (10%) del traffico sulle strade
	static double ta = 0.1;
//	static Double[] ita = {1.0};
	static Double[] ita = {0.6,0.4};
//	static Double[] ita = {0.6,0.3,0.1};
//	static Double[] ita={0.2,0.4,0.1,0.3};

//	usato solo se polyMode = true:
//	è il flusso massimo che si può assegnare a un singolo routing
//	minore sarà "parameterForRandomAssigment" più punti random verranno
//	generati all'interno dell'area di origine (e dell'area di destinazione)
	static Integer parameterForRandomAssigment = 100;
	
//	il flusso degli spostamenti letti dal file origine/destinazione inferiori alla tolleranza
//	non vengono presi in considerazione	
	static Double tolleranza = (double)  100;
	
	
	// serve per l'analisi dei tempi.
	// se ci sono meno di analizeTollerance persone, questo tragitto non viene considerato nel confronte dei tempi
	
	static Double analizeTollerance = 20.0;
	
//	se si vuole aggiungere una nuova strada
	static boolean forecast = false;
	static GHPoint daForecast = new GHPoint(45.418591, 9.205387);
	static GHPoint aForecast = new GHPoint(45.431696, 9.223983);
	
//	se true i comuni sono identificati da un codice formato istat
// serve per trattere i codici dei comuni. daIstat = true sono i codici istat.
	static boolean daIstat = true;
			
//	per ottenere informazioni sul traffico in questo punto (OLD)
	static GHPoint revGeocoding = new GHPoint (45.027936,7.600243);
	
//	stringa contenente le informazioni sui tragitti "delay", ossia quelli che non rientrano più in questa fascia oraria
//	formato str: "da.lat"-"da.lon"-"da.lat"-"da.lon"-"flusso"-"nuovo orario"|
	
	static int countGhostRoute = 0; // non dovrebbe servire
// il  numero di tragitti del quale non si è riuscito a calcolcare il percorso
	
	public static void main(String[] args) throws Exception{
//		calcolo lo StaticTraffic
		Double stc=0.0;
		if(weight.equalsIgnoreCase("traffic")){
//			leggo il numero totale del parco veicolare preso in considerazione nelle matrici OD
			BufferedReader b = new BufferedReader(new FileReader(percorsoInput+"/"+fileMOD));
			for(int i=0;i<3;i++)	b.readLine();	//skip header
			String s = b.readLine();
			double tot= Double.parseDouble(s.split("=")[1]);
			stc=Util.getStaticTrafficCost(b.readLine().replaceAll("regioni in considerazione =", "").replaceAll(",", "").split("\t"), tot, ta);
			b.close();
		}
		System.out.println("--------------------->>> "+stc);
		
//		Write.genericWrite(percorsoInput+"/temp/delays.dat", str);
					
		if(!percorsoInput.endsWith("/")){
			percorsoInput += "/";
		}
		
		String fileCoord = percorsoInput+"temp/latlon.csv";		
		if(daIstat)	{
			fileCoord = percorsoInput+"Dati/Map/Coord/CoordinateDecimaliComuni.csv";
		}
		
		MyGraphHopper gh = new MyGraphHopper();
		gh.setPreciseIndexResolution(10000);
		gh.setOSMFile("C:/DATASET/osm/lomb/lomb.osm");
		gh.setGraphHopperLocation(percorsoInput+"Dati/Map");
		EncodingManager eM = new EncodingManager(EncodingManager.CAR);
		gh.forDesktop();
		gh.setEncodingManager(eM);
		gh.setCHEnable(false);
		gh.determineStc(stc);
		gh.importOrLoad();
		gh.setWayPointMaxDistance(100);
		LocationIndex index = gh.getLocationIndex();
		GraphHopperStorage graph = gh.getGraphHopperStorage();
		

		int op=0;
		int ow=0;
		
		
		HashMap<String, GHPoint> coord = new HashMap<String, GHPoint>();
//		Formato HashMap coord: <String: cod Istat del comune "cc"+"-"+cp, String corrdinate del comune:"lat"+":"+"lon">
		
		HashMap<String, WEdge> edges = new HashMap<String, WEdge>();
//		Formato HashMap edges: <Integer: edge ID, WEdge: tutte le informazioni sul relativo edge>
		
		HashMap<String, Double> streets = new HashMap<String, Double>();
//		Formato HashMap RouteMatrix: <String: "lat1"+","+"lon1"+":"lat2"+","+"lon2", Double: num tot persone sulla strada>
		
		HashMap<String, Double> streetsForecast = new HashMap<String, Double>();
//		Formato HashMap RouteMatrix: <String: "lat1"+","+"lon1"+":"lat2"+","+"lon2", Double: num tot persone sulla strada>
		
		HashMap<String,Geometry> polycoord = new HashMap<String,Geometry>();
//		Formato HashMap polycoord: <String: cod Istat del comune "cc"+"-"+cp, Geometry: area del comune>
		
		HashMap<String,ArrayList<Pair<GHPoint,GHPoint>>> randomRoutes = new HashMap<String,ArrayList<Pair<GHPoint,GHPoint>>> ();
//		Formato HashMap randomPoints: <String: cod Istat del comune di partenza "cc"+"-"+cp+":"+"cc"+"-"+"cp" cod Istat del comune d'arrivo, ArrayList<GHPoint[]: [0] GHPoint partenza, [1] GHPoint arrivo>>		
		
//		HashMap<String, Double> time = new HashMap<String, Double>();
		HashMap<String, double[][]> timeM = new HashMap<String, double[][]>();
//		Formato HashMap Time: <Pair<String: cod Istat del comune di partenza "cc"+"-"+cp+":",String "cc"+"-"+"cp" cod Istat del comune d'arrivo>,Double[i] # persone che compiono nella fascia oraria i-esima>
		
		HashMap<String, Route> hm = new HashMap<String, Route>();
		
		
		ArrayList<Integer> fe = new ArrayList<Integer>();
		fe = Util.getForbiddenEdge(percorsoInput);
		gh.determineForbiddenEdges(fe);
		
		BufferedWriter bw= new BufferedWriter(new FileWriter(new File(percorsoInput+"temp/delay.csv")));
//		prima di tutto vado a calcolare i tragitti derivanti dalle scorse iterazioni del programma nelle diverse fascie orarie
		BufferedReader dr = new BufferedReader(new FileReader(percorsoInput+"/temp/delays.csv"));
		String m[] = fileMOD.split("_-_");
		int orario = (Integer.parseInt(m[2].replace(".csv", "")));
		
		String ds ="";
		if(weight.equalsIgnoreCase("traffic")){
			while((ds=dr.readLine())!=null){
				String dst[] = ds.split("|");
				for(int i=0; i<dst.length; i++){
					String dstt[] = dst[i].split("-");
					if(Integer.parseInt(dstt[5])==orario){
						double daLat = Double.parseDouble(dstt[0]);
						double daLon = Double.parseDouble(dstt[1]);
						double aLat = Double.parseDouble(dstt[2]);
						double aLon = Double.parseDouble(dstt[3]);
						MyGHResponse res = new MyGHResponse();
						do{
							GHRequest req = new GHRequest(daLat,daLon, aLat,aLon);
							req.setWeighting(weight);
							res = gh.route(req, eM, orario, Util.round(Double.parseDouble(dstt[4]), 4), "");
	//						da++; a++;
							if(res.hasErrors()){
								daLat+=0.001;
								daLon+=0.001;
								aLat+=0.001;
								aLon+=0.001;
							}
						}while(res.hasErrors());
						if(!res.equals("null"))	bw.write(res.getDelayInfo()+"\n");
					}
				}
			}
		}
		dr.close();
		
		EdgeIteratorState forecastEdge = null;			
		if(forecast){
			QueryResult aqr, bqr;
			aqr = index.findClosest(daForecast.lat, daForecast.lon,  EdgeFilter.ALL_EDGES);
			bqr = index.findClosest(aForecast.lat, aForecast.lon,  EdgeFilter.ALL_EDGES);
			DistanceCalc3D dc = new DistanceCalc3D();
			forecastEdge = graph.edge(aqr.getClosestEdge().getBaseNode(), bqr.getClosestEdge().getBaseNode(), dc.calcDist( daForecast.lat, daForecast.lon, aForecast.lat, aForecast.lon ), true);
			graph.flush();
			
		}
		
		
		polycoord = CoordinateUtil.setPolycoord("G:/DATASET/OD-ALBERTO-FRANCIA/Geometry/comuniUpdated.csv", daIstat);
		
		
		
		
		
		if(!polyMode){
//			coord=CoordinateUtil.setCoord(fileMOD,fileCoord);
			String fileMBOD = percorsoInput+"temp/MatriceBoolean.csv";
			fileMBOD = Write.BooleanMatrix(tolleranza, fileMOD, percorsoInput);
			coord = CoordinateUtil.setCoord(fileMBOD, fileMOD, fileCoord);
		}
		
//		ordino i valori di ita
		ita = Util.bubbleSort(ita);
//		controllo che i valori dell'algoritmo "ita" siano corretti
		double itaControl = 0;
		for(int iter = 0; iter<ita.length; iter++){
			itaControl+=ita[iter];
		}
		
		if((1-itaControl)<0.00001)
		for(int iter = 0; iter<ita.length; iter++){
			BufferedReader br = new BufferedReader(new FileReader(percorsoInput+fileMOD));
			for(int i=0;i<14;i++)	br.readLine();	//skip header
			
			String line = br.readLine();
			String ci[] = line.split("\t");
							
			System.out.println("Iterazione n° "+(iter+1)+" su "+(ita.length));
			
			DoLine dl = new DoLine(bw, ci, tolleranza, parameterForRandomAssigment, orario, ita[iter], polyMode, analizeTollerance, coord, randomRoutes, polycoord, gh, eM, weight);
			MultiWorker.run(percorsoInput+fileMOD, dl);
			randomRoutes=dl.getRandomRoutes();
			hm=dl.getRoutes();
			System.out.println("hm size "+hm.size() );
			gh.updateBusyEdge();
			br.close();
		}
		else System.out.println("Errore: inserire valori per la variabile 'ita' la cui somma sia 1 ");
		
		edges=gh.getBusyEdges();
		
		Util.serialize(new File(percorsoInput+"temp/edges.csv"), edges);
		
		Analize an = new Analize(edges, eM, ita.length);
		ArrayList<Route> routes = new ArrayList<Route>();
	
		for(String k: hm.keySet())
			routes.add(hm.get(k));
		System.out.println("finish route hm converter: "+routes.size());
//		hm.clear();
//		coord.clear();
		
		MultiWorker.run(routes, an);
		
		
		Util.save(new File(percorsoInput+"/temp/HashMaps/HMtimesTraffic.dat"), an.getResultTimeTraffic());
		Util.save(new File(percorsoInput+"/temp/HashMaps/HMtimesFastest.dat"), an.getResultTimeFastest());
		Util.save(new File(percorsoInput+"/temp/HashMaps/HMtimesOldTraffic.dat"), an.getResultTimeOldTraffic());
		Util.save(new File(percorsoInput+"/temp/HashMaps/HMtimesFastest2.dat"), an.getResultTimeOldTraffic());
		
		
//		System.out.println("tragitti calcolati:"+count+"  tragitti di cui non si è potuto calcolare il percorso:"+countGhostRoute);
		System.out.println("time size "+timeM.size());
		System.out.println(edges.size());
		
		
		
		
		Set<String> keySet = edges.keySet();
		PointList p = new PointList();
		for(String id:keySet){
			p = edges.get(id).getPoints();
//			System.out.println(edges.get(id).getVia());
			for(int j=0;j<p.size()-1;j++){
				String da = Util.round(p.getLat(j), 4)+","+Util.round(p.getLon(j), 4);
				String a = Util.round(p.getLat(j+1), 4)+","+Util.round(p.getLon(j+1), 4);
				
				if(!da.equals(a)){
					streets.put(da+":"+a, (edges.get(id).getFlux(edges.get(id).getBaseNode())+edges.get(id).getFlux(edges.get(id).getAdjNode())));
				}
			}
		}
		
		
		if(forecast){
			p = forecastEdge.fetchWayGeometry(3);
			for(int j=0;j<p.size()-1;j++){
				String da = Util.round(p.getLat(j), 4)+","+Util.round(p.getLon(j), 4);
				String a = Util.round(p.getLat(j+1), 4)+","+Util.round(p.getLon(j+1), 4);
				
				if(!da.equals(a)){
					if(edges.containsKey(forecastEdge.getEdge()+"")){
						streetsForecast.put(da+":"+a, (edges.get(forecastEdge.getEdge()+"").getFlux(forecastEdge.getBaseNode())+edges.get(forecastEdge.getEdge()+"").getFlux(forecastEdge.getAdjNode())));
					}
					else{
						streetsForecast.put(da+":"+a, 0.0);
					}
				}
			}
		}
//	//	REV GEOCODING
//		QueryResult cqr;
//		cqr = index.findClosest(revGeocoding.lat, revGeocoding.lon,  EdgeFilter.ALL_EDGES);
//		EdgeIteratorState revGeo = cqr.getClosestEdge(); 
//		if(!edges.containsKey(revGeo.getEdge())){
//			System.out.println("Traffico su "+revGeo.getName()+": "+0);
//		}
//		else{
//			System.out.println("Traffico su "+revGeo.getName()+": "+edges.get(revGeo.getEdge()).getWeight());
//			System.out.println("velocità media : "+edges.get(revGeo.getEdge()).getDistance()/edges.get(revGeo.getEdge()).getWeight());
//		}
	
//		QUA
		if(forecast){
			MyGraphHopper.addForbiddenEdge(forecastEdge, percorsoInput);
		}
		System.out.println("streets size: "+streets.size());

//		Write.advancedHTML(streets, streetsForecast, polycoord, comToVisualize, provToVisualize, percorsoOutput);
		
//	=====>	if(daIstat) Write.averageTime(time, percorsoInput, orario, tolleranza, weight, ita, polyMode, parameterForRandomAssigment, ta);

//		if(daIstat)	Write.averageTime2(time, fileMOD, percorsoInput, orario);
		
		
		System.out.println("Fine av time");	
		System.out.println("busyEdges size:"+gh.getBusyEdges().size());
		Util.save(new File(percorsoInput+"/temp/HashMaps/matrixTimeProject.dat"), timeM);

		
		
		/*
		 * Questo pezzo di codice carica il file edges.csv 
		 * la chiave è l'id osm (graphopper) che ideintifica un tratto di srtata
		 * WEDge contiene informaioni sul numero di macchinae e il tempo di percorrenza (per conforntare dopo con istat)
		 */
		
		
		HashMap<String, WEdge> desEdges = Util.deserializeWedges(new File(percorsoInput+"temp/edges.csv"), gh);
		for(String k:desEdges.keySet()){
			if(edges.containsKey(k)){
				if(desEdges.get(k).getFlux(desEdges.get(k).getBaseNode())!=edges.get(k).getFlux(edges.get(k).getBaseNode())  ){
					if(desEdges.get(k).getFlux(desEdges.get(k).getAdjNode())==edges.get(k).getFlux(edges.get(k).getBaseNode())  ){
						System.out.println("ERRORE FLUSSI INVERTITI");
					}
					else{
						System.out.println("NO NO NO NO:"+edges.get(k).getFlux(edges.get(k).getBaseNode())+" != "+desEdges.get(k).getFlux(desEdges.get(k).getAdjNode()));
					}
				}
//				else System.out.println("ok");
			}
			else	System.out.println("ERRORE ID");
		}
		
		// inizializzo i pesi di graphopper sulla base del traffico salvato in edges.csv (e caricato in desEdges).
		// Instradamenti che faccio dopo, tengono conto del traffico assegnato.
		
		gh.determineBusyEdge(desEdges);
		gh.updateBusyEdge();
		
		Write.simpleHTML(streets, streetsForecast, polycoord, comToVisualize, provToVisualize, percorsoOutput);
		System.out.println("op= "+op+" ow= "+ow);
		System.out.println("Fine");
	}
}