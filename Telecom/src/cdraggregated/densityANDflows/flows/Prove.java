package cdraggregated.densityANDflows.flows;

import java.util.ArrayList;

import utils.mod.Comune;
import utils.mod.Util;

public class Prove {
//	paradosso di braes
	static String percorsoInput = "D:/CODE/Project";
	public static void main(String[] args) throws Exception{
//		MyGraphHopper gh = new MyGraphHopper();
//		gh.setPreciseIndexResolution(1000);
//		gh.setOSMFile(percorsoInput+"/Dati/Map/italy-latest.osm");
//		gh.setGraphHopperLocation(percorsoInput+"/Dati/Map");
//		
//		gh.setEncodingManager(new EncodingManager(EncodingManager.CAR));
//		gh.setCHEnable(false);
//		gh.setPreciseIndexResolution(999999999);
//		gh.forDesktop();
//		gh.importOrLoad();
//		
//		EdgeIteratorState edgeDa;
//		EdgeIteratorState edgeA;
//		LocationIndex index = gh.getLocationIndex();
////		GraphHopperStorage graph = gh.getGraphHopperStorage();
//		
//		edgeDa = index.findClosest( 45.159692084417316,10.791169730223025, EdgeFilter.ALL_EDGES).getClosestEdge();
//		System.out.println(edgeDa.getEdge());
//		CarFlagEncoder encoder = new CarFlagEncoder();
//
//		GraphHopperStorage graph = gh.getGraphHopperStorage();
//		LocationIndexMatch locationIndex = new LocationIndexMatch(graph,
//		                (LocationIndexTree) gh.getLocationIndex());
////		MapMatching mapMatching = new MapMatching(graph, locationIndex, encoder);
//		
//		GHResponse res = gh.route(new GHRequest(45.163733,10.816898, 45.159692084417316,10.791169730223025).setWeighting("traffic"));
//		if(res.hasErrors()) System.out.println(res.getErrors());
//		else System.out.println(res.getPoints().toString());
//		System.out.println(50/3.6);
		System.out.println((100/(50/3.6)));
		System.out.println((100/(50/3.6))*(1+0.15*Math.pow((1000/1000),4.0)));
		
	}
}
