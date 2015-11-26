package cdraggregated.densityANDflows.flows;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import region.Placemark;
import region.RegionMap;
import utils.Config;
import utils.Logger;
import visual.html.ArrowsGoogleMaps;
import visual.kml.KML;
import visual.kml.KMLArrow;
import visual.r.RRoadNetwork;
import cdrindividual.densityANDflows.flows.Move;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.util.PointList;

public class ODMatrixVisual {
		

	public static final String TRASPORTATION_MODE = EncodingManager.CAR;
	
    
    // main for testing purposes
    public static void main2(String[] args) throws Exception {
    	String ghLoc = "C:/DATASET/osm/piem";
	    String testOsm = "C:/DATASET/osm/piem/piem.pbf";
    	GraphHopper gh = new GraphHopper().setInMemory(true, true).setEncodingManager(new EncodingManager(TRASPORTATION_MODE)).setGraphHopperLocation(ghLoc).setOSMFile(testOsm);
		gh.setPreciseIndexResolution(10000); // to be set about the grid size
		gh.importOrLoad();
		
		double start_lat = 45.077157;
		double start_lon = 7.629951;
		
		double end_lat = 44.968199;
		double end_lon = 7.621368;
		
		
		
		
		GHResponse ph = gh.route(new GHRequest(start_lat,start_lon,end_lat,end_lon));
		if(ph.isFound()) {
	        PointList list = ph.getPoints();
	        for(int i=1; i<list.getSize();i++) {
	        	
	        	double prevlat = list.getLatitude(i-1);
	        	double prevlon = list.getLongitude(i-1);
	        	double lat = list.getLatitude(i);
	        	double lon = list.getLongitude(i);
	        	
	        	int previndex = gh.getIndex().findID(prevlat, prevlon);
	        	int index = gh.getIndex().findID(lat, lon);
	        	
	        	//int edge = gh.getGraph().createEdgeExplorer().
	        	
	        	System.out.println("("+prevlat+","+prevlon+") ==> ("+lat+","+lon+") ==> "+gh.route(new GHRequest(list.getLatitude(i-1),list.getLongitude(i-1),list.getLatitude(i),list.getLongitude(i))).getInstructions().toString());
	        } 
		}
    }
    
    
    /*
     * Questo codice è un po' tutto da rifare.
     * Innanzitutto non si tratta semplicemente di un visual, ma di un assegnamento della matrice OD sulle strade.
     * Poi non dovrebbe più passare per la classe Move.
     * Anche il fatto che il file della regione osm si debba chiamare "file_pls_piem" è brutto.
     * Derva dal codice precedente e da come il metodo è invocato da ad esempio ODMatrixHW
     */
    
    
    public static void main(String[] args) throws Exception {
    	draw("ODMatrixHW_file_pls_piem","ODMatrixHW_file_pls_piem",false,"file_pls_piem");
    }
    
    
    public static String draw(String title, String od_dir, boolean directed, String osm_region) throws Exception {
    	
    	RegionMap rm = new RegionMap(title);
    	BufferedReader br = new BufferedReader(new FileReader(Config.getInstance().base_folder+"/ODMatrix/"+od_dir+"/latlon.csv"));
    	String line;
    	while((line = br.readLine())!=null) {
    		String[] e = line.split("\t");
    		String name = e[0];
    		String[] latlon = e[1].split(",");
    		double lat = Double.parseDouble(latlon[0]);
    		double lon = Double.parseDouble(latlon[1]);
    		rm.add(new Placemark(name,new double[]{lat,lon},1));
    	}
    	br.close();
    	
    	Map<Move,Double> list_od = new HashMap<Move,Double>();
    	br = new BufferedReader(new FileReader(Config.getInstance().base_folder+"/ODMatrix/"+od_dir+"/od.csv"));
    	
    	String[] header = br.readLine().split("\t");
    	while((line = br.readLine())!=null) {
    		String[] e = line.split("\t");
    		for(int i=1;i<e.length;i++) {
    			double v = Double.parseDouble(e[i]);
    			if(v > 0)
    				list_od.put(new Move(rm.getRegion(e[0]),rm.getRegion(header[i])), v);
    		}
    	}
    	br.close();
    	
    	
    	return draw(title,list_od,directed,osm_region,rm);
    }
    
    public static String draw(String title, Map<Move,Double> list_od, boolean directed, String osm_region, RegionMap rm) throws Exception {
    	
    	List<double[][]> points = new ArrayList<double[][]>();
		List<Double> w = new ArrayList<Double>();	
		List<String> colors = new ArrayList<String>();
		DescriptiveStatistics stats = new DescriptiveStatistics();
		
    	
    	Map<String,Double> map = getSegmentOD(list_od,directed,osm_region);
    	
    	
		for(Double x: map.values()) 
			stats.addValue(x);
		double p25 = stats.getPercentile(25); 
		double max = stats.getMax();
		
		
		
		for(String k: map.keySet()) {
			double weight = map.get(k);
			if(weight > p25) {
				points.add(toCoord(k));
				w.add(10 * weight / max);
				colors.add("#ff0000");
			}
		}
		
		
		String dir = Config.getInstance().web_kml_folder;
		File d = new File(dir);
		if(!d.exists()) d.mkdirs();
		
		RRoadNetwork.draw(title, points, w, colors, directed, rm, Config.getInstance().base_folder+"/Images/"+title+".png", null);
		printKML(dir+"/od_tmp.kml",title,points,w,colors,false);
		return ArrowsGoogleMaps.draw(dir+"/"+title+".html",title,points,w,colors,false);
    }
    
    
    // this is the same method as before but changes color for incoming/outgoing routes
    public static void draw(String title, Map<Move,Double> incoming_od, Map<Move,Double> outgoing_od, boolean directed, String region) throws Exception {
    	Map<String,Double> in_map = getSegmentOD(incoming_od,directed,region);
    	Map<String,Double> out_map = getSegmentOD(outgoing_od,directed,region);
    	
    	DescriptiveStatistics stats = new DescriptiveStatistics();
		for(Double x: in_map.values()) 
			stats.addValue(x);
		for(Double x: out_map.values()) 
			stats.addValue(x);
		double p25 = stats.getPercentile(25); 
		double max = stats.getMax();
		
		List<double[][]> points = new ArrayList<double[][]>();
		List<Double> w = new ArrayList<Double>();	
		List<String> colors = new ArrayList<String>();
		
		for(String k: in_map.keySet()) {
			double weight = in_map.get(k);
			if(weight > p25) {
				points.add(toCoord(k));
				w.add(10 * weight / max);
				colors.add("#ff0000");
			}
		}
		for(String k: out_map.keySet()) {
			double weight = out_map.get(k);
			if(weight > p25) {
				points.add(toCoord(k));
				w.add(10 * weight / max);
				colors.add("#0000ff");
			}
		}
		
		
		String dir = "BASE/ODMatrix";
		File d = new File(dir);
		if(!d.exists()) d.mkdirs();
		
		ArrowsGoogleMaps.draw(dir+"/"+title+".html",title,points,w,colors,false);
		printKML(dir+"/"+title+".kml",title,points,w,colors,false);
    }
    
    
    
	private static Map<String,Double> getSegmentOD(Map<Move,Double> list_od, boolean directed, String region) throws Exception {
		
		String n = region.substring("file_pls_".length());
		String ghLoc = "C:/DATASET/osm/"+n;
	    String testOsm = "C:/DATASET/osm/"+n+"/"+n+".pbf";
	    GraphHopper gh = null;
	    try {
			gh = new GraphHopper().setInMemory(true, true).setEncodingManager(new EncodingManager(TRASPORTATION_MODE)).setGraphHopperLocation(ghLoc).setOSMFile(testOsm);
			gh.setPreciseIndexResolution(10000); // to be set about the grid size
			gh.importOrLoad();
	    } catch(Exception e) {
	    	System.err.println("Did you downloaded map files?");
	    	System.err.println("Go to http://geodati.fmach.it/gfoss_geodata/osm/italia_osm.html");
	    	throw e;
	    }
		
		
		
		if(!directed) {
			Map<Move,Double> list_od_undirected = new HashMap<Move,Double>();
			// change the list_od so that a --> b and b-->a are merged together
			for(Move m: list_od.keySet()) {
				Move m2 = new Move(m.s,m.d,false);
				Double v2 = list_od_undirected.get(m2);
				if(v2 == null) v2 = 0.0;
				v2 += list_od.get(m);
				list_od_undirected.put(m2, v2);
			}
			list_od = list_od_undirected;
		}
		
		
		//List<double[][]> points = new ArrayList<double[][]>();
		//List<Double> w = new ArrayList<Double>();
		Map<String,Double> map = new HashMap<String,Double>();
		
	
		for(Move m: list_od.keySet()) {
			double weight = list_od.get(m);
			if(!m.sameSourceAndDestination()) {
				double[] p1 = new double[]{m.s.getLatLon()[0],m.s.getLatLon()[1]};
				double[] p2 = new double[]{m.d.getLatLon()[0],m.d.getLatLon()[1]};
				double[][] route;
				GHResponse ph = gh.route(new GHRequest(m.s.getLatLon()[0],m.s.getLatLon()[1],m.d.getLatLon()[0],m.d.getLatLon()[1]));
		        
				if(ph.isFound()) {
					route = new double[ph.getPoints().getSize()][2];
			        PointList list = ph.getPoints();
			        for(int i=0; i<list.getSize();i++) {
			        	route[i][0] = list.getLatitude(i);
			        	route[i][1] = list.getLongitude(i);
			        }
				}
				else { 
					// if graphhopper does not return valid path, just use the straight line between points.
					Logger.logln("!!! NO ROUTE BETWEEN: "+ m + " USE STRAIGHT LINE INSTEAD");
					route = new double[][]{p1,p2};
				}
				
				for(int i=1; i<route.length;i++) {
					double[][] segment = new double[2][2];
					segment[0][0] = route[i-1][0];
					segment[0][1] = route[i-1][1];
					segment[1][0] = route[i][0];
					segment[1][1] = route[i][1];
					String k = toKey(segment);
					Double w = map.get(k);
					if(w == null) map.put(k, weight);
					else map.put(k, w + weight);
				}
			}
		}
		
		return map;
	}
	
	
	
	
	private static String toKey(double[][] segment) {
		String k =  segment[0][0]+","+segment[0][1]+","+segment[1][0]+","+segment[1][1];
		return k;
	}
	private static double[][] toCoord(String k) {
		String[] e = k.split(",");
		double[][] segment = new double[2][2];
		segment[0][0] = Double.parseDouble(e[0]);
		segment[0][1] = Double.parseDouble(e[1]);
		segment[1][0] = Double.parseDouble(e[2]);
		segment[1][1] = Double.parseDouble(e[3]);
		return segment;
	}
	
	
	private static void printKML(String file, String title, List<double[][]> points, List<Double> weights, List<String> colors, boolean directed) throws Exception {
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file)));
		KML kml = new KML();
		kml.printHeaderFolder(out, title);
		kml.printFolder(out, "incoming");
		for(int i=0; i<points.size();i++) {
			double[][] p = points.get(i);
			double w = weights.get(i);
			if(i>0 && !colors.get(i).equals(colors.get(i-1))) {
				kml.closeFolder(out);
				kml.printFolder(out, "outgoing");
			}
			//out.println(KMLArrow.printArrow(p[0][1], p[0][0], p[1][1], p[1][0], w, "#ff0000ff",directed));
			out.println(KMLArrow.printArrow(p, w, html2kmlColor(colors.get(i)),directed));
		}
		kml.closeFolder(out);
		kml.printFooterFolder(out);
		out.close();
	}
	
	private static String html2kmlColor(String rgb) {
    	String r = rgb.substring(1,3);
    	String g = rgb.substring(3,5);
    	String b = rgb.substring(5);
    	return "#ff"+b+g+r;
    }
	
}