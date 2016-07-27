package cdraggregated.densityANDflows.flows;

import java.io.File;
import java.util.HashMap;
import java.util.Set;

import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.util.PointList;
import com.vividsolutions.jts.geom.Geometry;

import utils.mod.CoordinateUtil;
import utils.mod.Util;
import utils.mod.Write;
import utils.mygraphhopper.MyGraphHopper;
import utils.mygraphhopper.WEdge;

public class Visualizer {

	static String percorsoOutput = "C:/Users/marco/Desktop/mapOutput";
	
	public static void main(String[] args) throws Exception{
//		45.4664539,9.170471 file html, 45.4597116,9.170471 url maps
		MyGraphHopper gh = new MyGraphHopper();
		gh.setPreciseIndexResolution(10000);
		gh.setOSMFile("D:/CODE/Project/Dati/Map/italy-latest.osm");
		gh.setGraphHopperLocation("D:/CODE/Project/Dati/Map");
		EncodingManager eM = new EncodingManager(EncodingManager.CAR);
		gh.setEncodingManager(eM);
		gh.setCHEnable(false);
		gh.determineStc(0.0);
		gh.importOrLoad();
		String[] provToVisualize = {"015"};
		String[] comToVisualize = {};
		HashMap<String,Geometry> polycoord = new HashMap<String,Geometry>();
		polycoord=CoordinateUtil.setPolycoord("D:/CODE/Project/Dati/Geometry/comuniUpdated.csv", true);
		
		
		HashMap<String, WEdge> edges = Util.deserializeWedges(new File("D:/CODE/Project/temp/edges.csv"), gh);
		gh.determineBusyEdge(edges);
		HashMap<String, Double> streetsForecast = new HashMap<String, Double>();
		HashMap<String, Double> streets = new HashMap<String, Double>();
		Set<String> keySet = edges.keySet();
		PointList p = new PointList();
		FlagEncoder encoder = gh.getEncodingManager().getEncoder("car");
		for(String id:keySet){
			double d=Math.max(edges.get(id).getFlux(edges.get(id).getBaseNode())/edges.get(id).getCapicity(encoder),edges.get(id).getFlux(edges.get(id).getAdjNode())/edges.get(id).getCapicity(encoder));
			if(d>0.5)
			p = edges.get(id).getPoints();
//			System.out.println(edges.get(id).getVia());
			for(int j=0;j<p.size()-1;j++){
				if((p.getLat(j)<45.6931&&p.getLat(j)>45.235)||(p.getLon(j)<9.84&&p.getLon(j)>8.46)){
					String da = Util.round(p.getLat(j), 4)+","+Util.round(p.getLon(j), 4);
					String a = Util.round(p.getLat(j+1), 4)+","+Util.round(p.getLon(j+1), 4);			
					if(!da.equals(a)){
						streets.put(da+":"+a, (d));
					}
				}
			}
		}
		Write.simpleHTML(streets, streetsForecast, polycoord, comToVisualize, provToVisualize, percorsoOutput);
		
	}

}
