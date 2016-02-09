package utils.mygraphhopper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

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
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.util.Weighting;
import com.graphhopper.routing.util.WeightingMap;
import com.graphhopper.storage.CHGraph;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.index.QueryResult;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.StopWatch;
import com.graphhopper.util.shapes.GHPoint;



public class MyGraphHopper extends GraphHopper {
	
	ArrayList<Integer> forbiddenEdges=new ArrayList<Integer>();
	HashMap<Integer, WEdge> busyEdges=new HashMap<Integer, WEdge>();
	
	public void determineForbiddenEdges(ArrayList<Integer> forbiddenEdges) {
		this.forbiddenEdges = forbiddenEdges;
	}
	public void determineBusyEdges(HashMap<Integer, WEdge> busyEdges) {
		this.busyEdges = busyEdges;
	}
	
	
	@Override
	public Weighting createWeighting( WeightingMap wMap, FlagEncoder encoder )	{
		String weighting = wMap.getWeighting();
		if ("TRAFFIC".equalsIgnoreCase(weighting)){
//        	System.out.println("busyEdges"+busyEdges.toString());
			return new TrafficWeighting(encoder, busyEdges, forbiddenEdges);
		}
		else{
				return super.createWeighting(wMap, encoder);
		}
    }
	
 // Ho dovuto aggiungere getPaths al mio codice perché nella libreria di GraphHopper è del tipo "protected"
	public static List<Path> getPaths( MyGraphHopper gh, GHRequest request, GHResponse rsp )	{
		
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
 	        if (path.getTime() < 0)
 	        	throw new RuntimeException("Time was negative. Please report as bug and include:" + request);
 	        
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
	
	public static void addForbiddenEdge(EdgeIteratorState e) throws Exception{
		System.out.println("fe = "+e.getEdge());
		boolean found = false;
		BufferedReader br = new BufferedReader(new FileReader("temp/forbiddenEdgesList.txt"));
		String rs = br.readLine();	//skip header
		String tot = rs+"\n";
		while((rs = br.readLine())!= null){
			tot += rs+"\n";
			if(rs.equals(e.getEdge()+"")){
				found = true;
			}
		}
		BufferedWriter bw = new BufferedWriter(new FileWriter("temp/forbiddenEdgesList.txt"));
		bw.write(tot);
		if(!found){
			bw.write(e.getEdge()+"\n");
		}
		
		bw.close();
		br.close();
	}	
	
	public static ArrayList<Integer> getForbiddenEdge() throws Exception{
		ArrayList<Integer> forbiddenEdges = new ArrayList<Integer>();
		/*BufferedReader brd = new BufferedReader(new FileReader("temp/forbiddenEdgesList.txt"));
		String rs = "";
		while(rs!= ""){
			rs = brd.readLine();
			System.out.println(rs);
			forbiddenEdges.add(Integer.parseInt(rs.trim()));
			System.out.println("ok");
		}
		brd.close();*/
		return forbiddenEdges;
	}	
}