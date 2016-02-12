package cdraggregated.densityANDflows.flows;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import region.RegionMap;
import utils.Config;
import utils.CopyAndSerializationUtils;
import utils.mod.CoordinateUtil;
import utils.mod.Util;
import utils.mod.Util.Pair;
import utils.mod.Write;
import utils.mygraphhopper.MyGraphHopper;
import utils.mygraphhopper.WEdge;
import visual.r.RRoadNetwork;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.routing.Path;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.index.LocationIndex;
import com.graphhopper.storage.index.QueryResult;
import com.graphhopper.util.DistanceCalc2D;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.PointList;
import com.graphhopper.util.shapes.GHPoint;
import com.vividsolutions.jts.geom.Geometry;

public class MAPfromMOD {
		
//		se true		usa dati con origini e destinazioni generati casualmente in un'area		
//		se false	usa dati con origini e destinazioni puntuali
		static boolean polyMode = true;
		
//		static Double[] ita={0.2,0.2,0.2,0.2,0.2};
		static Double[] ita = {1.0};
//		static Double[] ita = {0.8,0.2};

		
//		usato solo se polyMode = true:
//		è il flusso massimo che si può assegnare a un singolo routing
//		minore sarà "parameterForRandomAssigment" più punti random verranno
//		generati all'interno dell'area di origine (e dell'area di destinazione)
		static Integer parameterForRandomAssigment = 50;
		
//		il flusso degli spostamenti letti dal file origine/destinazione inferiori alla tolleranza
//		non vengono presi in considerazione	
		static Double tolleranza=(double)  1;
		
		static boolean forecast = false;
		static GHPoint daForecast = new GHPoint(44.798891, 7.630273);
		static GHPoint aForecast = new GHPoint(44.809944, 7.61198);
		
		
		static GHPoint revGeocoding = new GHPoint (45.027936,7.600243);
		
		public static void main(String[] args) throws Exception{
			
//			String fileMOD = "od.csv";
//			String fileMOD = "C:/BASE/ODMatrix/MatriceOD_-_Piem_orario_uscita_-_1.csv";
//			String fileMOD = "C:/BASE/ODMatrix/ODMatrixHW_file_pls_piem/od2.csv";
			//String fileMOD = "C:/BASE/ODMatrix/emilia-romagna/4406_mod_201509142300_201509150000_calabrese_emilia_regione+ascbologna.txt";
			String fileMOD = "C:/BASE/ODMatrix/ODMatrixHW_file_pls_piem_file_pls_piem_01-06-2015-01-07-2015_minH_0_maxH_25_ABOVE_400limit_1000_cellXHour_piem2011.ser/od.csv";
			
			
			
//			String fileCoord = "latlon.csv";
			//String fileCoord = "G:/DATASET/OD-ALBERTO-FRANCIA/Map/Coord/CoordinateDecimaliComuni.csv";
//			String fileCoord = "C:/BASE/ODMatrix/ODMatrixHW_file_pls_piem/latlon.csv";
			//String fileCoord = "G:/DATASET/GEO/EmiliaRomagna/ER.csv";
			String fileCoord = "C:/BASE/ODMatrix/ODMatrixHW_file_pls_piem_file_pls_piem_01-06-2015-01-07-2015_minH_0_maxH_25_ABOVE_400limit_1000_cellXHour_piem2011.ser/latlon.csv";
			RegionMap rm = (RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/piem2011.ser"));
			
			
			MyGraphHopper gh = new MyGraphHopper();
			gh.setPreciseIndexResolution(1000);
			gh.setOSMFile("C:/DATASET/osm/piem2/piem.osm");
			gh.setGraphHopperLocation("C:/DATASET/osm/piem2");
			gh.setEncodingManager(new EncodingManager(EncodingManager.CAR));
			gh.setCHEnable(false);
			gh.importOrLoad();
			EdgeIteratorState edgeDa;
			EdgeIteratorState edgeA;
			LocationIndex index = gh.getLocationIndex();
			GraphHopperStorage graph = gh.getGraphHopperStorage();
			
			HashMap<String, GHPoint> coord = new HashMap<String, GHPoint>();
//			Formato HashMap coord: <String: cod Istat del comune "cc"+"-"+cp, String corrdinate del comune:"lat"+":"+"lon">
			
			HashMap<Integer, WEdge> edges = new HashMap<Integer, WEdge>();
//			Formato HashMap edges: <Integer: edge ID, WEdge: tutte le informazioni sul relativo edge>
			
			HashMap<String, Double> streets = new HashMap<String, Double>();
//			Formato HashMap RouteMatrix: <String: "lat1"+","+"lon1"+":"lat2"+","+"lon2", Double: num tot persone sulla strada>
			
			HashMap<String, Double> streetsForecast = new HashMap<String, Double>();
//			Formato HashMap RouteMatrix: <String: "lat1"+","+"lon1"+":"lat2"+","+"lon2", Double: num tot persone sulla strada>
			
			Map<String,Geometry> polycoord = new HashMap<String,Geometry>();
//			Formato HashMap polycoord: <String: cod Istat del comune "cc"+"-"+cp, Geometry: area del comune>
			
			HashMap<String,ArrayList<Pair<GHPoint,GHPoint>>> randomRoutes = new HashMap<String,ArrayList<Pair<GHPoint,GHPoint>>> ();
//			Formato HashMap randomPoints: <String: cod Istat del comune di partenza "cc"+"-"+cp+":"+"cc"+"-"+"cp" cod Istat del comune d'arrivo, ArrayList<GHPoint[]: [0] GHPoint partenza, [1] GHPoint arrivo>>		
			
			ArrayList<Integer> fe = new ArrayList<Integer>();
			fe = MyGraphHopper.getForbiddenEdge();
			gh.determineForbiddenEdges(fe);
			EdgeIteratorState forecastEdge = null;			
			if(forecast){
				QueryResult aqr, bqr;
				aqr = index.findClosest(daForecast.lat, daForecast.lon,  EdgeFilter.ALL_EDGES);
				bqr = index.findClosest(aForecast.lat, aForecast.lon,  EdgeFilter.ALL_EDGES);
				DistanceCalc2D dc = new DistanceCalc2D();
				forecastEdge = graph.edge(aqr.getClosestEdge().getBaseNode(), bqr.getClosestEdge().getBaseNode(), dc.calcDist( daForecast.lat, daForecast.lon, aForecast.lat, aForecast.lon ), true);
				graph.flush();
				
			}
			
			if(polyMode){
				//polycoord = CoordinateUtil.setPolycoord("G:/DATASET/OD-ALBERTO-FRANCIA/Geometry/comuni2014.csv");
				polycoord = CoordinateUtil.setPolycoord(fileCoord);
				
				//for(String k: polycoord.keySet())
				//	System.out.println(k);
				
			}
			else{
//				coord=CoordinateUtil.setCoord(fileMOD,fileCoord);
				String fileMBOD ="MatriceBoolean.csv";
				fileMBOD = Write.BooleanMatrix(tolleranza, fileMOD);
				coord = CoordinateUtil.setCoord(fileMBOD, fileMOD, fileCoord);
			}
			
//			ordino i valori di ita
			ita = Util.bubbleSort(ita);
//			controllo che i valori dell'algoritmo "ita" siano corretti
			double itaControl = 0;
			for(int iter = 0; iter<ita.length; iter++){
				itaControl+=ita[iter];
			}
			if((1-itaControl)<0.00001)			
				
			for(int iter = 0; iter<ita.length; iter++){
				int count=0;
				gh.determineBusyEdges(edges);
				
				BufferedReader br = new BufferedReader(new FileReader(fileMOD));
				
				for(int i=0;i<14;i++)	br.readLine();	//skip header
				
				String line = br.readLine();
				
				//line = line.replaceFirst("\t", "");
				
				//System.err.println(line);
				
				String ci[]=line.split("\t");
				
				int r=0;
				int countGhostRoute=0;	// il  numero di tragitti del quale non si è riuscito a calcolcare il percorso

				while((line=br.readLine())!=null){
					r++;
//					System.out.println(ci[r]);
			
					GHPoint da = new GHPoint(0.0,0.0);
					GHPoint a = new GHPoint(1.0,1.0);
					
					if(line.startsWith("\t"))
						line = line.replaceFirst("\t", "");
					
					//System.out.println(line);
					String l[]=line.split("\t");
					for(int c=1; c<l.length; c++){
						//if(ci[r].equals("5,0") && ci[c].equals("6,0"))	System.out.println("==>"+l[c]);
						
						
						
						double flux=Double.parseDouble(l[c]);
						if(flux>=tolleranza){
							double itaFlux=(flux*ita[iter]);
//							gh.determinebusyEdges(edges);
							
						//System.out.println("Iterazione n° "+(iter+1)+" su "+(ita.length)+",   Tragitto numero: "+(count+1)+", Riga numero: "+r+" su "+ci.length+" "+c);
							
							ArrayList<Pair<GHPoint,GHPoint>> randomPoints= new ArrayList<Pair<GHPoint,GHPoint>>();
							if(!polyMode){
								da = coord.get(ci[r]);
								a = coord.get(ci[c]);
							}

							if(polyMode||((!polyMode)&&(!a.equals(da)))){  //per non tener conto di tragitti con origine == alla destinazione nel caso non si sia in polymode
								count++;
//								n è la somma di tutti i flussi nelle vari iterazion(del ciclo while seguente), all'ultima iterazione sara pari a itaFlux
								int n=0;
//								f sarà il flusso da assegnare al traffico per ogni a getPath, non può superare parameterForRandomAssigment
								double f=itaFlux;
								while(n<itaFlux){			
									
									if(itaFlux-n<parameterForRandomAssigment){
										f=itaFlux-n;
									}
									else{
										if(itaFlux>parameterForRandomAssigment){
											f=parameterForRandomAssigment;
										}
									}
									n+=parameterForRandomAssigment;
									
									
//									in randomRoutes vengono memorizzati i tragitti casuali generati. avendo ordinato ita in ordine
//									decrescente non dovrebbero esserci eccezioni out of bounds, garantendo durante le seconde iterazioni
//									dell'algoritmo che il traffico continui ad essere assegnato agli stessi tragitti anche generati casualmente
									if(polyMode){
										//System.out.println(ci.length+" "+r+" "+c);
										if(randomRoutes.containsKey(ci[r]+":"+ci[c])){
											da	= randomRoutes.get(ci[r]+":"+ci[c]).get((n/parameterForRandomAssigment)-1).first;
											a	= randomRoutes.get(ci[r]+":"+ci[c]).get((n/parameterForRandomAssigment)-1).second;
										}
										else{
											
											//System.out.println("====> "+ci[c]);
											
											if(polycoord.containsKey(ci[c]) && polycoord.containsKey(ci[r])){
												da = CoordinateUtil.GenerateRandomPoint(polycoord.get(ci[r]));
												a = CoordinateUtil.GenerateRandomPoint(polycoord.get(ci[c]));
												randomPoints.add(new Pair<GHPoint,GHPoint>(da,a));
											}
											else	{
												if(polycoord.containsKey(ci[c])) System.out.println("ERROR NOT CONTEINED INPOLYCOORD:"+ci[r]);
												else System.out.println("ERROR NOT CONTEINED INPOLYCOORD:"+ci[c]);
											}
											
										}				
									}
									
									
									
									List<Path> lp;
									GHRequest req = new GHRequest(Util.round4(da.lat),Util.round4(da.lon),Util.round4(a.lat),Util.round4(a.lon));
									req.setWeighting("TRAFFIC");
									GHResponse res = gh.route(req);    
									lp=MyGraphHopper.getPaths(gh,req, res);
									
									//	getPaths restituisce una lista di path (che ho sempre visto avere dimensione uno)
									//	Un path è un oggetto contenente edge (archi, in questo caso gli edge del percorso ricercato
									
									if(lp.size()>1)System.out.println("WWOOOOOOOOOOOOWWWWWWWWW");
									for(int i=0; i<	lp.size();i++){
										List<EdgeIteratorState> thisPath=lp.get(i).calcEdges();
										
										//	devo trattare il primo e l'ultimo tratto del percorso in modo diverso rispetto agli altri:
										//	il primo (ultimo) edge è un'edge che non esiste in graphhopper,
										//	dato che ha come punto di partenza (fine) un punto fornito dall'utente. gethPath() infatti
										//	restituirebbe come primo (ultimo) edge un nuovo edge generato appositamente per questo singolo
										//	routing, assegnandogli, per ogni volta che calcolo un tragitto, lo stesso nuovo ID. Ad ogni
										//	nuovo routing quindi si andrebbe ad aggiungere un peso non esistente al primo (ultimo) edge generato
										//	Il primo edge quindi viene trovato tramite 	il metodo getClosestEdge, a discapito di una meno
										//	precisa localizzazione del punto di partenza (arrivo).
										
										edgeDa = index.findClosest(da.lat, da.lon, EdgeFilter.ALL_EDGES).getClosestEdge();
										if(edges.containsKey(edgeDa.getEdge()))	{
											edges.get(edgeDa.getEdge()).addWeight(f);
										}
										else	edges.put(edgeDa.getEdge(), new WEdge(edgeDa, f));
										if(ci[r].equals("5,0")&&ci[c].equals("6,0"))
											System.out.println("ok "+thisPath.size());
										
										for (int j=1; j<thisPath.size()-1; j++){
											if(edges.containsKey(thisPath.get(j).getEdge())){
												edges.get(thisPath.get(j).getEdge()).addWeight(f);
												
//							    				System.out.println(thisPath.get(j).toString()+" f = "+flux+"  "+thisPath.get(j).getEdge());
//							 	   				if(thisPath.get(j).getEdge()==306074)System.out.println("ooooooooooooooo"+thisPath.get(j).fetchWayGeometry(3));
											}
											else {
												edges.put(thisPath.get(j).getEdge(), new WEdge(thisPath.get(j), f));
//												System.out.println(thisPath.get(j).toString());
//								    			if(thisPath.get(j).getEdge()==49345) System.out.println(thisPath.get(j).fetchWayGeometry(0)+" flusso "+flux)
												
											}			    				
										}
										edgeA = index.findClosest(a.lat, a.lon, EdgeFilter.ALL_EDGES).getClosestEdge();
										if(edges.containsKey(edgeA.getEdge()))	edges.get(edgeA.getEdge()).addWeight(f);
										else	edges.put(edgeA.getEdge(), new WEdge(edgeA, f));
									}
									if(lp.size()==0&&!(ci[r].equals(ci[r]))){
										countGhostRoute++;
									}
								}
							}
							if(!randomPoints.isEmpty())
								randomRoutes.put(ci[r]+":"+ci[c], randomPoints);
						}
					}
				}
				System.out.println("tragitti calcolati:"+count+"  tragitti di cui non si è potuto calcolare il percorso:"+countGhostRoute);
				System.out.println(edges.size());
				br.close();
			}
			else System.out.println("Errore: inserire valori per la variabile 'ita' la cui somma sia 1 ");
			
			Set<Integer> keySet = edges.keySet();
			PointList p = new PointList();
			for(Integer id:keySet){
				p = edges.get(id).getPoints();
//				System.out.println(edges.get(id).getVia());
				for(int j=0;j<p.size()-1;j++){
					String da = Util.round4(p.getLat(j))+","+Util.round4(p.getLon(j));
					String a = Util.round4(p.getLat(j+1))+","+Util.round4(p.getLon(j+1));
					
					if(!da.equals(a)){
						streets.put(da+":"+a, edges.get(id).getWeight());	
					}
				}
			}
			

			
			if(forecast){	
				p = forecastEdge.fetchWayGeometry(3);
				for(int j=0;j<p.size()-1;j++){
					String da = Util.round4(p.getLat(j))+","+Util.round4(p.getLon(j));
					String a = Util.round4(p.getLat(j+1))+","+Util.round4(p.getLon(j+1));
					
					if(!da.equals(a)){
						if(edges.containsKey(forecastEdge.getEdge())){
							streetsForecast.put(da+":"+a, edges.get(forecastEdge.getEdge()).getWeight());
						}
						else{
							streetsForecast.put(da+":"+a, 0.0);
						}
					}
				}
			}
			
			/*
			QueryResult cqr;
			cqr = index.findClosest(revGeocoding.lat, revGeocoding.lon,  EdgeFilter.ALL_EDGES);
			EdgeIteratorState revGeo = cqr.getClosestEdge(); 
			if(!edges.containsKey(revGeo.getEdge())){
				System.out.println("Traffico su "+revGeo.getName()+": "+0);
//				System.out.println("velocità media : "+revGeo.getDistance()/revGeo.getFlags());
			}
			else{
				System.out.println("Traffico su "+revGeo.getName()+": "+edges.get(revGeo.getEdge()).getWeight());
				System.out.println("velocità media : "+edges.get(revGeo.getEdge()).getDistance()/edges.get(revGeo.getEdge()).getWeight());
			}
			
			for(int id: edges.keySet()) {
				System.out.println("---- "+edges.get(id).getVia()+" => "+edges.get(id).getWeight());
			}
			*/
			
			
			
			
			String name = fileMOD.substring("C:/BASE/ODMatrix/".length(), fileMOD.lastIndexOf("/"));
			
			
			drawR(name,streets,rm,Config.getInstance().base_folder+"/Images/"+name+".png");
			
			
			
			//MyGraphHopper.addForbiddenEdge(forecastEdge);
			System.out.println(streets.size());
			Write.simpleHTML(streets, streetsForecast);
//			Write.advancedHTML(streets);

			//Util.save(new File("temp/edges.dat"), Util.serialize(edges));
			System.out.println("Fine");
		}
		
		
		public static void drawR(String title, HashMap<String, Double> streets, RegionMap rm, String file) {
			//Formato HashMap RouteMatrix: <String: "lat1"+","+"lon1"+":"lat2"+","+"lon2", Double: num tot persone sulla strada>
			
			List<double[][]> latlon_segments = new ArrayList<>();
			List<Double> weights = new ArrayList<>();
			List<String> colors = new ArrayList<>();
			boolean directed = false;
			
			double max = 0;
			for(double w: streets.values())
				max = Math.max(max, w);
				
			for(String k: streets.keySet()) {
				String[] e = k.split(",|:");
				double lat1 = Double.parseDouble(e[0]);
				double lon1 = Double.parseDouble(e[1]);
				double lat2 = Double.parseDouble(e[2]);
				double lon2 = Double.parseDouble(e[3]);
				double w = streets.get(k);
				latlon_segments.add(new double[][]{{lat1,lon1},{lat2,lon2}});
				weights.add(10*w/max);
				//colors.add(Colors.val01_to_color(w/max));
				colors.add("#ff0000");
			}
			

			RRoadNetwork.draw(title, latlon_segments, weights, colors, directed, rm, file, null);
			
		}
}
