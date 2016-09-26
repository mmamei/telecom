package cdraggregated.densityANDflows.flows;

import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.FlagEncoder;
import utils.mod.Route;
import utils.mod.Util;
import utils.multithread.WorkerCallbackI;
import utils.mygraphhopper.WEdge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Analize implements WorkerCallbackI<Route>{
	
	private Map<String, WEdge> wEdges;
	private FlagEncoder fEnc;
	private Map<String, Double> resultTimeTraffic = new HashMap<String, Double>();
	private Map<String, Double> resultTimeFastest = new HashMap<String, Double>();
	private Map<String, Double> resultTimeFastest2 = new HashMap<String, Double>();
	private Map<String, Double> resultTimeOldTraffic = new HashMap<String, Double>();
	private int nIta;
	
	public Analize(Map<String, WEdge> wEdges, EncodingManager eM, int nIta) {
		this.wEdges=wEdges;
		this.fEnc=eM.getEncoder("Car");
		this.nIta=nIta;
	}
	
	public Map<String, Double> getResultTimeTraffic(){
		return this.resultTimeTraffic;
	}
	
	public Map<String, Double> getResultTimeOldTraffic(){
		return this.resultTimeOldTraffic;
	}
	
	public Map<String, Double> getResultTimeFastest(){
		return this.resultTimeFastest;
	}
	public Map<String, Double> getResultTimeFastest2(){
		return this.resultTimeFastest2;
	}


	public void  runMultiThread(Route route){
		ArrayList<Integer[][]> paths = route.getWEdgesPaths();
//		l'arrayList di Integer[][] ha per ogni elemento un diverso instradamento effettuato dal nostro algoritmo causato dal polyMode
//		per ogni instradamento  ho un array bidimensionale contenente nella prima colonna l'id del wEdge, e nella seconda l'id del suo BaseNode
		ArrayList<Double[]> flowTime = route.getFlowTime();
		
		if(flowTime.size()!=paths.size())	System.err.println("flowTime.size()!=paths.size()");
		
		 
		double[] fast2 = new double[paths.size()/nIta];
		double[] fastF2 = new double[paths.size()/nIta];
		double[] fast = new double[paths.size()];
		double[] w = new double[paths.size()];
		double[] oldT = new double[paths.size()];
		double[] trafTime = new double[paths.size()];
		boolean d=true;
		boolean b=false;
		for(int i=0; i<paths.size(); i++){
			if(!(paths.get(i)[0][0].equals(-1)&&paths.get(i)[0][1].equals(-1))){
				w[i] = flowTime.get(i)[0];
				fast[i] = flowTime.get(i)[1];
				oldT[i] = flowTime.get(i)[2];
				
				trafTime[i] = 0.0;
//				double h =0.0;
				for(int y=0; y<paths.get(i).length; y++){
//					System.out.println("\t"+y+"  \t"+paths.get(i)[y][0]+" \t"+paths.get(i)[y][1]);
					trafTime[i]+=(Double) wEdges.get(""+paths.get(i)[y][0]).getTime(paths.get(i)[y][1], fEnc);
//					h+= (wEdges.get(""+paths.get(i)[y][0]).getDistance()/fEnc.getSpeed(wEdges.get(""+paths.get(i)[y][0]).getEdgeIteratorState().getFlags()))*3.6;	
//					System.out.println(trafTime[i]+" = "+h);
				}
				
				if(i<paths.size()/nIta){
					fast2[i] = 0.0;
					fastF2[i] = (double) flowTime.get(i)[0];
					b=true;
//					System.out.println();
//					double f=0.0;
//					double t=0.0;
					for(int y=0; y<paths.get(i).length; y++){
//						f+=(wEdges.get(""+paths.get(i)[y][0]).getDistance()/fEnc.getSpeed(wEdges.get(""+paths.get(i)[y][0]).getEdgeIteratorState().getFlags()))*3.6;
//						t+=wEdges.get(""+paths.get(i)[y][0]).getTime(paths.get(i)[y][1], fEnc);
//						System.out.println("->"+t+" "+f);
						fast2[i]+= (wEdges.get(""+paths.get(i)[y][0]).getDistance()/fEnc.getSpeed(wEdges.get(""+paths.get(i)[y][0]).getEdgeIteratorState().getFlags()))*3.6;
					}
				}
			}
			else{
				System.out.println("IDIOTS");
				d=false;
			}
		}
		
		if(d){
			if(b)System.out.println("traf: "+Util.calcWeightedAverage(trafTime,	w)+" - fast1: "+Util.calcWeightedAverage(fast,	w)+" - fast2: "+Util.calcWeightedAverage(fast2,	fastF2)+" - oldT: "+Util.calcWeightedAverage(oldT,	w));
			resultTimeTraffic.put(route.getId(), Util.round(Util.calcWeightedAverage(trafTime,	w),0));
			resultTimeFastest.put(route.getId(), Util.round(Util.calcWeightedAverage(fast,	w),0));
			resultTimeFastest2.put(route.getId(), Util.round(Util.calcWeightedAverage(fast2,	fastF2),0));
			resultTimeOldTraffic.put(route.getId(), Util.round(Util.calcWeightedAverage(oldT,	w),0));
		}
	}
}