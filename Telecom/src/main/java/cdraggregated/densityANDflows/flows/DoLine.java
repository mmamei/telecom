package cdraggregated.densityANDflows.flows;

import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import utils.mod.CoordinateUtil;
import utils.mod.Route;
import utils.mod.Util;
import utils.mod.Util.Pair;
import utils.multithread.WorkerCallbackI;
import utils.mygraphhopper.MyGraphHopper;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.util.shapes.GHPoint;
import com.vividsolutions.jts.geom.Geometry;

public class DoLine implements WorkerCallbackI<String>{
	
	private BufferedWriter bw;
	private String ci[];
	private double tolleranza;
	private Integer parameterForRandomAssigment;
	private int orario;
	private Double ita;

	private boolean polyMode;
	private Double analizeTollerance;


	private Map<String, GHPoint> coord = new HashMap<String, GHPoint>();
	private Map<String,ArrayList<Pair<GHPoint,GHPoint>>> randomRoutes = new HashMap<String,ArrayList<Pair<GHPoint,GHPoint>>>();
	private	Map<String,Geometry> polycoord = new HashMap<String,Geometry>();
	private MyGraphHopper gh;
	private EncodingManager eM;
	private String weight;
//	private int conta=0;
//	private int cont=0;
//	private int contb=0;
	private HashMap<String, Route> routes = new HashMap<String, Route>();
	
	public DoLine( BufferedWriter bw, String ci[], double tolleranza, Integer parameterForRandomAssigment, int orario, Double ita, boolean polyMode,Double analizeTollerance, Map<String, GHPoint> coord, Map<String,ArrayList<Pair<GHPoint,GHPoint>>> randomRoutes, Map<String,Geometry> polycoord, MyGraphHopper gh, EncodingManager eM, String weight){
		this.bw=bw;
		this.ci=ci;
		this.tolleranza=tolleranza;
		this.parameterForRandomAssigment=parameterForRandomAssigment;
		this.orario=orario;
		this.ita=ita;
		this.polyMode=polyMode;
		this.analizeTollerance=analizeTollerance;
		
		this.coord=coord;
		this.randomRoutes=randomRoutes;
		this.polycoord=polycoord;
		this.gh=gh;
		this.eM=eM;
		this.weight=weight;		
	}
	
	/*
	public void runMultiThread(String line) {
		try{
//		popola il grafo
		GHPoint da = new GHPoint(0.0,0.0);
		GHPoint a = new GHPoint(1.0,1.0);
		String l[]=line.split("\t");
//		ci[r]=l[0]
		for(int c=1; c<l.length; c++){
			double flux=Double.parseDouble(l[c]);

			if(flux>=tolleranza){
			
//				traffico x4 circa
//				double itaFlux=((flux*( (9704151*0.75)/1679779.7500000007))*ita);
				double itaFlux=((flux*(ita)));
				ArrayList<Pair<GHPoint,GHPoint>> randomPoints= new ArrayList<Pair<GHPoint,GHPoint>>();

				if(!polyMode){
					da = coord.get(l[0]);
					a = coord.get(ci[c]);
				}

				if(polyMode||((!polyMode)&&(!a.equals(da)))){  //per non tener conto di tragitti con origine == alla destinazione nel caso non si sia in polymode
					String id = l[0]+":"+ci[c];
//					if(flux>=analizeTollerance) System.out.println(id);
					Route r;
					if(!routes.containsKey(id))	r = new Route(id);
					else	r = routes.get(id);
					
					boolean de= false;
//					n � la somma di tutti i flussi nelle vari iterazion(del ciclo while seguente), all'ultima iterazione sara pari a itaFlux
					int n=0;
//					f sar� il flusso da assegnare al traffico per ogni a getPath(route), non pu� superare parameterForRandomAssigment
					double f=itaFlux;
					int cg=0;
					double itaPFRA = parameterForRandomAssigment*ita;
					while(n<itaFlux){
						
//						gh.determineBusyEdges(edges);
						if(itaFlux-n<itaPFRA)
							f=itaFlux-n;
						else
							if(itaFlux>itaPFRA)
								f=itaPFRA;
						n+=itaPFRA;
						
//						in randomRoutes vengono memorizzati i tragitti casuali generati. avendo ordinato ita in ordine
//						decrescente non dovrebbero esserci eccezioni out of bounds, garantendo durante le seconde iterazioni
//						dell'algoritmo che il traffico continui ad essere assegnato agli stessi tragitti anche generati casualmente
						if(polyMode){
							if(randomRoutes.containsKey(l[0]+":"+ci[c])){
								da	= randomRoutes.get(l[0]+":"+ci[c]).get(cg).first;
								a	= randomRoutes.get(l[0]+":"+ci[c]).get(cg).second;
							}
							else{
								if(polycoord.containsKey(ci[c])&&polycoord.containsKey(l[0])){
									da = CoordinateUtil.GenerateRandomPoint(polycoord.get(l[0]));
									a = CoordinateUtil.GenerateRandomPoint(polycoord.get(ci[c]));
									randomPoints.add(new Pair<GHPoint,GHPoint>(da,a));
								}
							}
						}
						cg++;
						GHResponse res = new GHResponse();
//						int contFail=0;
						do{
							GHRequest req = new GHRequest(da.lat,da.lon, a.lat,a.lon);
							req.setWeighting(weight);
							res = gh.route(req, eM, Util.round(f, 4));
							if(res.hasErrors()||(res.getTime()<120000)){
//								contFail++;
								da.lat+=0.001;
								da.lon+=0.001;
								a.lat+=0.001;
								a.lon+=0.001;
							}
						}while(res.hasErrors()||(res.getTime()<120000));
						
						
						else{
							GHRequest fstReq = new GHRequest(da.lat,da.lon,a.lat,a.lon);
							Double[] t = new Double[3];
							t[0]=Util.round(f, 2);
	//						non usare la riga sotto, ma crea il tempo fastest com fai con Ttimes
							t[1]=Util.round((gh.route(fstReq.setWeighting("fastest"), eM).getRouteWeight()), 1);
							t[2]=Util.round(res.getTimeTot(), 1);
							r.addIter(t, res.getPath());
							
							
	//						System.out.println(res.getTimeTot()+"  "+(gh.route(fstReq.setWeighting("fastest"), eM).getRouteWeight()));
//							GHResponse fr = gh.route(fstReq.setWeighting("fastest"), eM);
//							double d=0.0;
//							HashMap<String, WEdge> be=gh.getBusyEdgesTemp();
//							for(int i=0; i<res.getPath().length; i++){
//								if(!de&&res.getPath()[0][1]!=-1)	{
////								int affa = res.getPath()[i][0];
////								be.get(""+res.getPath()[i][0]);
//								d+=(be.get(""+res.getPath()[i][0]).getDistance());
//								}
//							}
	//						System.out.println(fr.getDistance()+" "+d);
	
							
	//						System.out.println(gh.myGetPaths(gh, fstReq, fr).get(0).calcEdges().size()-2+" "+res.getPath().length);
//							if((fr.getRouteWeight())-res.getTimeTot()>10&&!de){
//								System.err.println("Eccolo "+ (gh.route(fstReq.setWeighting("fastest"), eM).getRouteWeight())+" > "+res.getTimeTot()+" "+id);
	//							System.out.println(fr.getTime()+" . "+res.getTime());
	//							System.out.println(fr.getDistance()+" "+res.getDistance());
//								be=gh.getBusyEdgesTemp();
//								double tr=0.0;
//								double fsr=0.0;
//								
//								for(int i=0; i<res.getPath().length; i++){
//									tr+=be.get(res.getPath()[i][0]+"").getTime(res.getPath()[i][1], eM.getEncoder("car"));
//									fsr+=(be.get(""+res.getPath()[i][0]).getDistance()/eM.getEncoder("car").getSpeed(be.get(""+res.getPath()[i][0]).getEdgeIteratorState().getFlags()))*3.6;
//	//								System.out.println("\t"+fsr+" > "+tr);
//								}
//								System.out.println(".");
//							}
						}
						if(res.getPath().length==0) System.err.println("res.getPath().length==0 "+da.lat+", "+da.lon+" - "+a.lat+" - "+a.lon);
					}
					
					if(flux>=analizeTollerance)
						if(routes.containsKey(id)){
							if(de)
								routes.remove(id);
							else {
								routes.remove(id);
								routes.put(id, r);
								//routes.replace(id, r);
							}
						}
						else{
							if(de)
								routes.remove(id);
							else
								routes.put(id, r);
						}
				}
				if(!randomPoints.isEmpty())
					randomRoutes.put(l[0]+":"+ci[c], randomPoints);
			}
		}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
*/
	public Map<String,ArrayList<Pair<GHPoint,GHPoint>>> getRandomRoutes(){
		return this.randomRoutes;
	}
	public HashMap<String, Route> getRoutes(){
		return this.routes;
	}

	@Override
	public void runMultiThread(String x) {

	}
}
