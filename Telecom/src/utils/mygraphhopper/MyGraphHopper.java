package utils.mygraphhopper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.routing.AlgorithmOptions;
import com.graphhopper.routing.Path;
import com.graphhopper.routing.QueryGraph;
import com.graphhopper.routing.RoutingAlgorithm;
import com.graphhopper.routing.RoutingAlgorithmFactory;
import com.graphhopper.routing.util.DefaultEdgeFilter;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.util.Weighting;
import com.graphhopper.routing.util.WeightingMap;
import com.graphhopper.storage.CHGraph;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.index.QueryResult;
import com.graphhopper.util.DouglasPeucker;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.PathMerger;
import com.graphhopper.util.StopWatch;
import com.graphhopper.util.TranslationMap;
import com.graphhopper.util.shapes.GHPoint;

import utils.mod.Util;

public class MyGraphHopper extends GraphHopper {

	TranslationMap trMap = new TranslationMap().doImport();
	ArrayList<Integer> forbiddenEdges=new ArrayList<Integer>();
	HashMap<String, WEdge> busyEdges=new HashMap<String, WEdge>();
	HashMap<String, WEdge> busyEdgesTemp=new HashMap<String, WEdge>();
	double stc=0.0;
	
	public MyGraphHopper updateBusyEdge(){
		for(String k:busyEdgesTemp.keySet()){
			if(busyEdges.containsKey(k)){
				WEdge weTemp =  busyEdgesTemp.get(k);
				WEdge we =	busyEdges.get(k);
				we.addFlux(weTemp.getBaseNode(), weTemp.getFlux(weTemp.getBaseNode()));
				we.addFlux(weTemp.getAdjNode(), weTemp.getFlux(weTemp.getAdjNode()));
				busyEdges.remove(k);
				busyEdges.put(k, we);
			}
			else{
				busyEdges.put(k, busyEdgesTemp.get(k));
			}
		}
		busyEdgesTemp.clear();
		return this;
	}
	public MyGraphHopper determineBusyEdge(HashMap<String, WEdge> busyEdges){
		this.busyEdges=busyEdges;
		return this;
	}
	
	public MyGraphHopper determineForbiddenEdges(ArrayList<Integer> forbiddenEdges) {
		this.forbiddenEdges = forbiddenEdges;
		return this;
	}

	public MyGraphHopper determineStc(double stc) {
		this.stc = stc;
		return this;
	}

	@Override
	public Weighting createWeighting( WeightingMap wMap, FlagEncoder encoder )	{
		String weighting = wMap.getWeighting();
		if ("TRAFFIC".equalsIgnoreCase(weighting)){
			return new TrafficWeighting(encoder, busyEdges, forbiddenEdges, stc);
		}
		else{
				return super.createWeighting(wMap, encoder);
		}
    }

	

////  Ho dovuto aggiungere getPaths al mio codice perché nella libreria di GraphHopper è del tipo "protected"

	public static List<Path> myGetPaths( MyGraphHopper gh, GHRequest request, GHResponse rsp )	{
		if ( gh.getGraphHopperStorage() == null || !true)
			throw new IllegalStateException("Call load or importOrLoad before routing");
 		if ( gh.getGraphHopperStorage().isClosed())
 			throw new IllegalStateException("You need to create a new GraphHopper instance as it is already closed");
 		
 		String vehicle = request.getVehicle();
 		if (vehicle.isEmpty())
 			vehicle = "car";
 			
 		if (!gh.getEncodingManager().supports(vehicle))
 		{
 			rsp.addError(new IllegalArgumentException("Vehicle " + vehicle + " unsupported. "
 					+ "Supported are: " + gh.getEncodingManager()));
 			return Collections.emptyList();
 		}
 		
 		TraversalMode tMode;
 		String tModeStr = request.getHints().get("traversal_mode",gh.getTraversalMode().toString());
 		try
 	    {
 			tMode = TraversalMode.fromString(tModeStr);
 	    } catch (Exception ex)
 	    {
 	    	rsp.addError(ex);
 	    	return Collections.emptyList();
 	    }
 		
 		List<GHPoint> points = request.getPoints();
 		if (points.size() < 2)
 		{
 			rsp.addError(new IllegalStateException("At least 2 points have to be specified, but was:" + points.size()));
 			return Collections.emptyList();
 		}
 		long visitedNodesSum = 0;
 		FlagEncoder encoder = gh.getEncodingManager().getEncoder(vehicle);
 		EdgeFilter edgeFilter = new DefaultEdgeFilter(encoder);
 		
 		StopWatch sw = new StopWatch().start();
 		List<QueryResult> qResults = new ArrayList<QueryResult>(points.size());
 		for (int placeIndex = 0; placeIndex < points.size(); placeIndex++)
 		{
 			GHPoint point = points.get(placeIndex);
 	 	          QueryResult res = gh.getLocationIndex().findClosest(point.lat, point.lon, edgeFilter);
 	 	          if (!res.isValid())
 	 	        	  rsp.addError(new IllegalArgumentException("Cannot find point " + placeIndex + ": " + point));
 	 	          
 	 	          qResults.add(res);
 		}
 		
 		if (rsp.hasErrors())
 			return Collections.emptyList();
 		
 		String debug = "idLookup:" + sw.stop().getSeconds() + "s";
 		
 		Weighting weighting;
 		Graph routingGraph =  gh.getGraphHopperStorage();
 		
 		if (gh.isCHEnabled())
 		{
 			boolean forceCHHeading = request.getHints().getBool("force_heading_ch", false);
 			if (!forceCHHeading && request.hasFavoredHeading(0))
 				throw new IllegalStateException("Heading is not (fully) supported for CHGraph. See issue #483");
 			weighting = gh.getWeightingForCH(request.getHints(), encoder);
 			routingGraph = gh.getGraphHopperStorage().getGraph(CHGraph.class, weighting);
 		} else
 			weighting = gh.createWeighting(request.getHints(), encoder);
 		RoutingAlgorithmFactory tmpAlgoFactory = gh.getAlgorithmFactory(weighting);
 		QueryGraph queryGraph = new QueryGraph(routingGraph);
 		queryGraph.lookup(qResults);
 		weighting = gh.createTurnWeighting(weighting, queryGraph, encoder);
 		
 		List<Path> paths = new ArrayList<Path>(points.size() - 1);
 		QueryResult fromQResult = qResults.get(0);
 		
 		double weightLimit = request.getHints().getDouble("defaultWeightLimit", Double.MAX_VALUE);
 		String algoStr = request.getAlgorithm().isEmpty() ? AlgorithmOptions.DIJKSTRA_BI : request.getAlgorithm();
 		AlgorithmOptions algoOpts = AlgorithmOptions.start().
 				algorithm(algoStr).traversalMode(tMode).flagEncoder(encoder).weighting(weighting)/*.hints(request.getHints())*/.
 				build();
 		
 		boolean viaTurnPenalty = request.getHints().getBool("pass_through", false);
 		for (int placeIndex = 1; placeIndex < points.size(); placeIndex++)
 		{
 			if (placeIndex == 1)
 			{
 				// enforce start direction
 				queryGraph.enforceHeading(fromQResult.getClosestNode(), request.getFavoredHeading(0), false);
 			} else if (viaTurnPenalty)
 			{
 				// enforce straight start after via stop
 				EdgeIteratorState incomingVirtualEdge = paths.get(placeIndex - 2).getFinalEdge();
 				queryGraph.enforceHeadingByEdgeId(fromQResult.getClosestNode(), incomingVirtualEdge.getEdge(), false);
 			}
 			QueryResult toQResult = qResults.get(placeIndex);
 			// enforce end direction
 			queryGraph.enforceHeading(toQResult.getClosestNode(), request.getFavoredHeading(placeIndex), true);
 			
 			sw = new StopWatch().start();
 			RoutingAlgorithm algo = tmpAlgoFactory.createAlgo(queryGraph, algoOpts);
 	        algo.setWeightLimit(weightLimit);
 	        debug += ", algoInit:" + sw.stop().getSeconds() + "s";
 	 		
 	        sw = new StopWatch().start();
 	        Path path = algo.calcPath(fromQResult.getClosestNode(), toQResult.getClosestNode());
 	        if (path.getTime() < 0){
 	        	System.out.println(path.toDetailsString());
 	        	System.out.println("path.getTime() < 0 "+path.getTime());
 	        	throw new RuntimeException("Time was negative. Please report as bug and include:" + request);
 	        }
 	        	
 	        paths.add(path);
 	        debug += ", " + algo.getName() + "-routing:" + sw.stop().getSeconds() + "s, " + path.getDebugInfo();
 	            
 	        // reset all direction enforcements in queryGraph to avoid influencing next path
 	        queryGraph.clearUnfavoredStatus();

 	        visitedNodesSum += algo.getVisitedNodes();
 	        fromQResult = toQResult;
 		}

 		if (rsp.hasErrors())
 			return Collections.emptyList();
 		
 		if (points.size() - 1 != paths.size())
 			throw new RuntimeException("There should be exactly one more places than paths. places:" + points.size() + ", paths:" + paths.size());
 		
 		rsp.setDebugInfo(debug);
 		rsp.getHints().put("visited_nodes.sum", visitedNodesSum);
 		rsp.getHints().put("visited_nodes.average", (float) visitedNodesSum / (points.size() - 1));
 		return paths;
 	}
	
	public static void addForbiddenEdge(EdgeIteratorState e, String percorsoInput) throws Exception{
		boolean found = false;
		BufferedReader br = new BufferedReader(new FileReader(percorsoInput+"temp/forbiddenEdgesList.txt"));
		String tot = br.readLine();	
		if(!tot.isEmpty()){
			String s[] = tot.split("-");
			for(int i =0; i<s.length; i++){
				if(s[i].equals(e.getEdge()+"")){
					found = true;
				}
			}
		}
		if(!found)	tot += e.getEdge()+"-";
		br.close();
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(percorsoInput+"temp/forbiddenEdgesList.txt"));
		bw.write(tot);
		bw.close();
	}
	
	public HashMap<String, WEdge> getBusyEdges (){
		return this.busyEdges;
	}
	public HashMap<String, WEdge> getBusyEdgesTemp (){
		return this.busyEdgesTemp;
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
//				double first=(fd/fEnc.getSpeed(thisPath.get(0).getFlags()))*3.6;
//				double last=(ld/fEnc.getSpeed(thisPath.get(thisPath.size()-1).getFlags()))*3.6;
//				return res.setRouteWeight(res.getRouteWeight()-(first+last)).setDistance(res.getDistance()-(fd+ld));
				for(int i=1; i<thisPath.size()-1; i++){
					time+=(thisPath.get(i).getDistance()/fEnc.getSpeed(thisPath.get(i).getFlags()))*3.6;
				}
				return res.setRouteWeight(time).setDistance(res.getDistance()-(fd+ld));
			}
			return super.route(req).setRouteWeight(0);
		}
		else return super.route(req);
	}
	
	public MyGHResponse route( GHRequest request, EncodingManager eM, int orario, double f, String id ){
		MyGHResponse response = new MyGHResponse();
		
		List<Path> paths = getPaths(request, response);
		
		double timeTot =0;
		boolean b=true;
		Integer[][] i =new Integer[1][2];
		i[0][0]=-1;
		i[0][1]=-1;
		if(paths.size()>1) System.err.println("paths.size()>1!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		if (response.hasErrors())
			return response.setTimeTot(-1).setPath(i);

//      _____ da MapfromMod2 ________
		
		else{
			List<EdgeIteratorState> thisPath=paths.get(0).calcEdges();
			if(thisPath.size()>2)  {
				Integer[][] p= new Integer[thisPath.size()-2][2];
				String df="";
				for (int j=1; j<thisPath.size()-1; j++){
//					System.out.println(thisPath.get(j).getEdge()+" "+thisPath.get(j).getDistance());
//					if(timeTot<3600){
					if(timeTot<36000000){
						p[j-1][0]=thisPath.get(j).getEdge();
						p[j-1][1]=thisPath.get(j).getBaseNode();
//						if(busyEdges.containsKey(thisPath.get(j).getEdge()+"")){
//							timeTot+=busyEdges.get(thisPath.get(j).getEdge()+"").addFlux(thisPath.get(j).getBaseNode(), f).getTime(thisPath.get(j), eM.getEncoder("car"));
//						}
//						else {
//							WEdge we = new WEdge(thisPath.get(j), f, stc, false);
////							le prossime due righe da invertire se il flusso lo si alloca in seguito al routing
//							timeTot+=we.getTime(thisPath.get(j), eM.getEncoder("car"));
//							busyEdges.put(we.getId()+"", we);
//						}
						
						if(busyEdges.containsKey(thisPath.get(j).getEdge()+""))
							timeTot+=busyEdges.get(thisPath.get(j).getEdge()+"").getTime(thisPath.get(j).getBaseNode(), eM.getEncoder("car"));
						else 
							timeTot+=(new WEdge(thisPath.get(j), 0, stc, false)).getTime(thisPath.get(j).getBaseNode(), eM.getEncoder("car"));
						
						if(busyEdgesTemp.containsKey(thisPath.get(j).getEdge()+""))	busyEdgesTemp.get(thisPath.get(j).getEdge()+"").addFlux(thisPath.get(j).getBaseNode(), f);	
						else busyEdgesTemp.put(thisPath.get(j).getEdge()+"", new WEdge(thisPath.get(j), f, stc, false));
						
//		v/c				System.out.println(busyEdgesTemp.get(thisPath.get(j).getEdge()+"").getFlux(busyEdgesTemp.get(thisPath.get(j).getEdge()+"").getBaseNode())/busyEdgesTemp.get(thisPath.get(j).getEdge()+"").getCapicity(eM.getEncoder("car")));
					}
					else{
						if(b){
							b=false;
							df+=(thisPath.get(j).getEdge()+"-"+thisPath.get(j).getBaseNode()+"-"+f+"-"+(orario+1)+"\n");
						}
						p[j-1][0]=-1;
						p[j-1][1]=-1;
					}
					if(timeTot>72000) System.err.println(timeTot+" "+response.getTime());
				}
				if(b){
					response.setTimeTot(timeTot);
					response.setPath(p);
				}
				else{
					response.setTimeTot(-1);
					response.setPath(p).setDelayInfo(df);
				}
			}
			else{
				response.setTimeTot(-1);
				response.setPath(i);
			}
		} 
		
//		_____________________
	
		boolean tmpEnableInstructions = request.getHints().getBool("instructions", true);
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

	public MyGraphHopper addBusyEdge(WEdge we){
		busyEdges.put(we.getId()+"", we);
		return this;
	}
}