package utils.mygraphhopper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.routing.Path;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.Weighting;
import com.graphhopper.routing.util.WeightingMap;
import com.graphhopper.util.DouglasPeucker;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.PathMerger;
import com.graphhopper.util.TranslationMap;

public class MyGraphHopper extends GraphHopper {

	private TranslationMap trMap = new TranslationMap().doImport();
	private Map<String, WEdge> busyEdges=new HashMap<>();
	private Map<String, WEdge> busyEdgesTemp=new HashMap<>();
	private double stc=0.0;
	
	public void updateBusyEdge(){
		for(String k:busyEdgesTemp.keySet()){
			if(busyEdges.containsKey(k)){
				WEdge weTemp =  busyEdgesTemp.get(k);
				WEdge we =	busyEdges.get(k);
				we.addFlux(weTemp.getBaseNode(), weTemp.getFlux(weTemp.getBaseNode()));
				we.addFlux(weTemp.getAdjNode(), weTemp.getFlux(weTemp.getAdjNode()));
			}
			else busyEdges.put(k, busyEdgesTemp.get(k));
		}
		busyEdgesTemp.clear();
	}
	
	public void setBusyEdges(HashMap<String, WEdge> busyEdges){
		this.busyEdges=busyEdges;
	}
	

	public void setStc(double stc) {
		this.stc = stc;
	}

	@Override
	public Weighting createWeighting( WeightingMap wMap, FlagEncoder encoder )	{
		if ("TRAFFIC".equalsIgnoreCase(wMap.getWeighting()))
			return new TrafficWeighting(encoder, busyEdges, stc);
		else
		    return super.createWeighting(wMap, encoder);
    }

	public List<Path> getPaths(GHRequest request, GHResponse rsp )	{
		return super.getPaths(request,rsp);
 	}
	
	public Map<String, WEdge> getBusyEdges (){
		return busyEdges;
	}
	
	public GHResponse route(GHRequest req, EncodingManager eM){
		if(req.getWeighting().equalsIgnoreCase("FASTEST")){
			FlagEncoder fEnc=eM.getEncoder("car");
			GHResponse res = super.route(req);
			List<EdgeIteratorState> thisPath = getPaths(req, res).get(0).calcEdges();
			double time=0.0;
			if(thisPath.size()>2)  {
				double fd=thisPath.get(0).getDistance();
				double ld=thisPath.get(thisPath.size()-1).getDistance();
				for(int i=1; i<thisPath.size()-1; i++){
					time+=(thisPath.get(i).getDistance()/fEnc.getSpeed(thisPath.get(i).getFlags()))*3.6;
				}
				return res.setRouteWeight(time).setDistance(res.getDistance()-(fd+ld));
			}
			return super.route(req).setRouteWeight(0);
		}
		else return super.route(req);
	}
	
	
	public GHResponse route( GHRequest request, EncodingManager eM, double f){
		GHResponse response = new GHResponse();
		List<Path> paths = getPaths(request, response);
		
		if(paths.size() > 1) System.err.println("Warning MyGraphHopper: paths.size() > 1");
		if (response.hasErrors()) return null;

		List<EdgeIteratorState> thisPath = paths.get(0).calcEdges();
		if (thisPath.size() > 2) {
			Integer[][] p = new Integer[thisPath.size() - 2][2];

			for (int j = 1; j < thisPath.size() - 1; j++) {
				p[j - 1][0] = thisPath.get(j).getEdge();
				p[j - 1][1] = thisPath.get(j).getBaseNode();
				String e = String.valueOf(thisPath.get(j).getEdge());
				if (busyEdgesTemp.containsKey(e))
					busyEdgesTemp.get(e).addFlux(thisPath.get(j).getBaseNode(), f);
				else
					busyEdgesTemp.put(e,new WEdge(thisPath.get(j), f, stc, false));
			}
		}
		
		boolean tmpEnableInstructions = request.getHints().getBool("instructions", false);
		boolean tmpCalcPoints = request.getHints().getBool("calcPoints", true);	//false per calcolare solo le distanze
		double wayPointMaxDistance = request.getHints().getDouble("wayPointMaxDistance", 1d);
		Locale locale = request.getLocale();
		DouglasPeucker peucker = new DouglasPeucker().setMaxDistance(wayPointMaxDistance);
		
		new PathMerger().
			setCalcPoints(tmpCalcPoints).
			setDouglasPeucker(peucker).
			setEnableInstructions(tmpEnableInstructions).
			setSimplifyResponse(true && wayPointMaxDistance > 0).//simplifyResponse
			doWork(response, paths, trMap.getWithFallBack(locale));
		return response;
	}
}