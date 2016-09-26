package utils.mygraphhopper;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.util.PointList;
import com.graphhopper.util.shapes.GHPoint;
import org.apache.log4j.PropertyConfigurator;
import utils.Config;
import visual.html.ArrowsGoogleMaps;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainTest {
	
	public static void main(String[] args) throws Exception {
		PropertyConfigurator.configure("Telecom/src/main/resources/log4j.properties");
		double stc = 0;
		MyGraphHopper gh = new MyGraphHopper();
		gh.setPreciseIndexResolution(10000);
		gh.setOSMFile("C:/DATASET/osm/er/emilia-romagna.osm");
		gh.setGraphHopperLocation("C:/DATASET/osm/er/");
		EncodingManager eM = new EncodingManager(EncodingManager.CAR);
		gh.forDesktop();
		gh.setEncodingManager(eM);
		gh.setCHEnable(false);
		gh.setStc(stc);
		gh.importOrLoad();
		gh.setWayPointMaxDistance(100);
		//LocationIndex index = gh.getLocationIndex();
		//GraphHopperStorage graph = gh.getGraphHopperStorage();
		
		
		
		GHPoint from = new GHPoint(44.629180, 10.870264);
		GHPoint to = new GHPoint(44.686414, 10.665721);
		
		
		GHRequest req = new GHRequest(from,to);
		req.setWeighting("TRAFFIC");
		
		
		//GHResponse res = gh.route(req, eM);
		
		int[] num_cars = new int[]{100000,10,3000};
		for(int k=0; k<num_cars.length;k++) {
			GHResponse res = gh.route(req, eM, num_cars[k]);
			System.out.println("ROUTING SEGMENT "+k+" WITH "+num_cars[k]+" CARS, TRIP TIME "+res.getTime()/(1000*60)+" mins");
			gh.updateBusyEdge();
		}
		
		List<double[][]> segments = new ArrayList<>();
		List<Double> w = new ArrayList<Double>();
		List<String> colors = new ArrayList<String>();
		Map<String, WEdge> be = gh.getBusyEdges();
		for(WEdge e: be.values()) {
			PointList pl =e.getPoints();
			for(int i=1;i<pl.getSize();i++) {
				segments.add(new double[][]{{pl.getLatitude(i-1), pl.getLongitude(i-1)},{pl.getLatitude(i), pl.getLongitude(i)}});
				w.add(2.0);
				colors.add(calcColorGoogle(e.getFluxBothDirections()));
			}
		}
		
		//printFluxDistribution(be);
	
		ArrowsGoogleMaps.draw(Config.getInstance().base_folder+"/GraphHopperTest.html","GraphHopperTest",segments,w,colors,true);
		
		System.out.println("Done");
	}
	
	// voc = volume over capacity
	private static String calcColorGoogle(Double voc) {
		// Verde: non ci sono ritardi dovuti al traffico.
		String color = "#84CA50";
		// Arancione: volume di traffico medio.
		if (voc > 0.5) color = "#F07D02";
		// Rosso: ritardi dovuti al traffico.
		if (voc > 1) color = "#E60000";
		// Rosso scuro
		if (voc > 1.2) color = "#9E1313";
		return color;
	}
	
	
	private static void printFluxDistribution(Map<String,WEdge> m) {
		for(String key: m.keySet()) {
			WEdge we = m.get(key);
			System.out.println(we.getStreetName()+" ("+(int)we.getDistance()+" meters) "+we.getFlux(we.getBaseNode())+" "+we.getFlux(we.getAdjNode()));
		}
	}
}
