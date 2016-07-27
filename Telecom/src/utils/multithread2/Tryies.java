package utils.multithread2;

import java.util.ArrayList;

import com.graphhopper.GHRequest;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.index.LocationIndex;
import com.graphhopper.storage.index.QueryResult;
import com.graphhopper.util.DistanceCalc3D;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.shapes.GHPoint;

import utils.mygraphhopper.MyGHResponse;
import utils.mygraphhopper.MyGraphHopper;

public class Tryies {
//	nome e percorso file della MOD
	static String fileMOD = "temp/MatriceOD_per_-_Lombardia__orario_uscita_-_1.csv";
//	static String fileMOD = "temp/od.csv";
	
//	Le province visualizzate nel file html che verrà generato
	static String[] provToVisualize = {"015"};
	
//	I comuni visualizzati nel file html che verrà generato
	static String[] comToVisualize = {};
	
	static String percorsoInput = "D:/CODE/Project";
	static String percorsoOutput = "C:/Users/marco/Desktop/mapOutput";
	
//	se true		usa dati con origini e destinazioni generati casualmente in un'area		
//	se false	usa dati con origini e destinazioni puntuali
	static boolean polyMode = true;
	static String weight = "traffic";
//	quanto viene preso in considerazione il "traffico statico"	
	static double ta = 0.0;
	static Double[] ita = {1.0};
//	static Double[] ita = {0.8,0.2};
//	static Double[] ita={0.2,0.4,0.1,0.3};

//	usato solo se polyMode = true:
//	è il flusso massimo che si può assegnare a un singolo routing
//	minore sarà "parameterForRandomAssigment" più punti random verranno
//	generati all'interno dell'area di origine (e dell'area di destinazione)
	static Integer parameterForRandomAssigment = 5;
	
//	il flusso degli spostamenti letti dal file origine/destinazione inferiori alla tolleranza
//	non vengono presi in considerazione	
	static Double tolleranza = (double)  1;
	static Double analizeTollerance = 50.0;
//	se si vuole aggiungere una nuova strada
	static boolean forecast = false;
	static GHPoint daForecast = new GHPoint(45.420217, 9.203595);
	static GHPoint aForecast = new GHPoint(45.4316965, 9.223983);
	
//	se true i comuni sono identificati da un codice formato istat
	static boolean daIstat = true;
			
//	per ottenere informazioni sul traffico in questo punto
	static GHPoint revGeocoding = new GHPoint (45.027936,7.600243);
	
//	stringa contenente le informazioni sui tragitti "delay", ossia quelli che non rientrano più in questa fascia oraria
//	formato str: "da.lat"-"da.lon"-"da.lat"-"da.lon"-"flusso"-"nuovo orario"|
	
	static int countGhostRoute = 0;
// il  numero di tragitti del quale non si è riuscito a calcolcare il percorso
	
	public static void main(String[] args) throws Exception{
		

		MyGraphHopper gh = new MyGraphHopper();
		gh.setPreciseIndexResolution(10000);
		gh.setOSMFile(percorsoInput+"/Dati/Map/italy-latest.osm");
		gh.setGraphHopperLocation(percorsoInput+"/Dati/Map");
		EncodingManager eM = new EncodingManager(EncodingManager.CAR);
		gh.setEncodingManager(eM);
		gh.setCHEnable(false);
		gh.determineStc(0);
		boolean twoWays = true;
		gh.importOrLoad();
		LocationIndex index = gh.getLocationIndex();
		GraphHopperStorage graph = gh.getGraphHopperStorage();
		ArrayList<Integer> fe = new ArrayList<Integer>();
		fe.add(4901520);
		fe.add(4901521);
		fe.add(4901522);
		fe.add(4901523);
		fe.add(4901524);
		fe.add(4901525);
		fe.add(4901526);
		fe.add(4901527);
		fe.add(4901528);
		fe.add(4901529);
		gh.determineForbiddenEdges(fe);
		EdgeIteratorState forecastEdge = null;			
		QueryResult aqr, bqr;
		aqr = index.findClosest(daForecast.lat, daForecast.lon,  EdgeFilter.ALL_EDGES);
		bqr = index.findClosest(aForecast.lat, aForecast.lon,  EdgeFilter.ALL_EDGES);
		DistanceCalc3D dc = new DistanceCalc3D();
		forecastEdge = graph.edge(aqr.getClosestEdge().getBaseNode(), bqr.getClosestEdge().getBaseNode());
		forecastEdge.setDistance(dc.calcDist( daForecast.lat, daForecast.lon, aForecast.lat, aForecast.lon ));
		EncodingManager em = gh.getEncodingManager();
		FlagEncoder encoder = em.getEncoder("car");
		forecastEdge.setFlags(encoder.setProperties(130, true, true));
		forecastEdge.detach(twoWays);

		System.out.println(encoder.getSpeed(forecastEdge.getFlags()));
		System.out.println(forecastEdge.getEdge()+" "+forecastEdge.getDistance());
		
		
		gh.setGraphHopperStorage(graph);
		graph.flush();
		MyGHResponse res = gh.route(new GHRequest(daForecast, aForecast).setWeighting("TRAFFIC"), eM, 1, 1.0, "String id" );
		System.out.println();
		System.out.println(res.getTime()+" "+res.getDistance()/(130/3.6));
		for(int i=0; i<res.getPath().length; i++) {
			System.out.println(res.getPath()[i][0]);
		}
		for(int i=0; i<res.getPoints().size(); i++) {
			System.out.println(res.getPoints().getLat(i)+", "+res.getPoints().getLon(i));
		}
		
		System.out.println(res.getDistance()+" "+res.getPoints().getSize());
		
		
//		MyGraphHopper gh = new MyGraphHopper();
//		gh.setPreciseIndexResolution(10000);
//		gh.setOSMFile(percorsoInput+"/Dati/Map/italy-latest.osm");
//		gh.setGraphHopperLocation(percorsoInput+"/Dati/Map");
//		EncodingManager eM = new EncodingManager(EncodingManager.CAR);
//		gh.setEncodingManager(eM);
//		gh.setCHEnable(false);
//		gh.determineStc(0);
//		gh.importOrLoad();
//		LocationIndex index = gh.getLocationIndex();
//		GraphHopperStorage graph = gh.getGraphHopperStorage();
//		
//		HashMap<String, GHPoint> coord = new HashMap<String, GHPoint>();
////		Formato HashMap coord: <String: cod Istat del comune "cc"+"-"+cp, String corrdinate del comune:"lat"+":"+"lon">
//		
//		HashMap<String, WEdge> edges = new HashMap<String, WEdge>();
////		Formato HashMap edges: <Integer: edge ID, WEdge: tutte le informazioni sul relativo edge>
//		
//		HashMap<String, Double> streets = new HashMap<String, Double>();
////		Formato HashMap RouteMatrix: <String: "lat1"+","+"lon1"+":"lat2"+","+"lon2", Double: num tot persone sulla strada>
//		
//		HashMap<String, Double> streetsForecast = new HashMap<String, Double>();
////		Formato HashMap RouteMatrix: <String: "lat1"+","+"lon1"+":"lat2"+","+"lon2", Double: num tot persone sulla strada>
//		
//		HashMap<String,Geometry> polycoord = new HashMap<String,Geometry>();
////		Formato HashMap polycoord: <String: cod Istat del comune "cc"+"-"+cp, Geometry: area del comune>
//		
//		HashMap<String,ArrayList<Pair<GHPoint,GHPoint>>> randomRoutes = new HashMap<String,ArrayList<Pair<GHPoint,GHPoint>>> ();
////		Formato HashMap randomPoints: <String: cod Istat del comune di partenza "cc"+"-"+cp+":"+"cc"+"-"+"cp" cod Istat del comune d'arrivo, ArrayList<GHPoint[]: [0] GHPoint partenza, [1] GHPoint arrivo>>		
//		
////		HashMap<String, Double> time = new HashMap<String, Double>();
//		HashMap<String, double[][]> timeM = new HashMap<String, double[][]>();
////		Formato HashMap Time: <Pair<String: cod Istat del comune di partenza "cc"+"-"+cp+":",String "cc"+"-"+"cp" cod Istat del comune d'arrivo>,Double[i] # persone che compiono nella fascia oraria i-esima>
//		MyGHResponse res = new MyGHResponse();
//		GHPoint da;
//		GHPoint a;
//		
//		
//		do{
//			da =new GHPoint(44.761549, 10.869814);
//			a =new GHPoint(44.725601, 10.904096);
//			GHRequest req = new GHRequest(da.lat,da.lon, a.lat,a.lon);
//			req.setWeighting(weight);
//			
//			res = gh.route(req, eM, 1, Util.round(0, 4), "id");
//			if(res.hasErrors())	System.out.println("no");
//		}while(res.hasErrors());
//		
//		System.out.println("w "+res.getRouteWeight());
//		gh.updateBusyEdge();
//		HashMap<String, WEdge> tr= gh.getBusyEdges();
//		System.out.println(tr.size());
//		System.out.println(res.getDistance());
//		for(String key:tr.keySet()){
////			System.out.println(key);
//			if(key.equals("1065786")){
//				System.out.println(key+"  "+tr.get(key).getBaseNode());
//				tr.get(key).addFlux(tr.get(key).getBaseNode(), 99999999);
//			}
//		}
//		gh.determineBusyEdge(tr);
//		
////		System.out.println(res.getRouteWeight());
////		GHRequest fstReq = new GHRequest(da.lat,da.lon,a.lat,a.lon);
////		System.out.println(Util.round(gh.route(fstReq.setWeighting("fastest")).getTime()/1000,0));
//		do{
//			da =new GHPoint(44.761549, 10.869814);
//			a =new GHPoint(44.725601, 10.904096);
//			GHRequest req = new GHRequest(da.lat,da.lon, a.lat,a.lon);
//			req.setWeighting(weight);
//			res = gh.route(req, eM, 1, Util.round(1.0, 4), "id");
//			if(res.hasErrors())	System.out.println("no");
//		}while(res.hasErrors());
//		gh.updateBusyEdge();
//		System.out.println();
//		System.out.println("w "+res.getRouteWeight());
//		System.out.println(gh.getBusyEdges().size());
////		System.out.println(res.getRouteWeight());
////		fstReq = new GHRequest(da.lat,da.lon,a.lat,a.lon);
////		System.out.println(Util.round(gh.route(fstReq.setWeighting("fastest")).getTime()/1000,0));
////		edges=Util.deserializeWedges(new File(percorsoInput+"/temp/edges.csv"), gh);
////		Analize an = new Analize(edges, eM);
////		System.out.println(edges.size());
//		
//		
	}

}
