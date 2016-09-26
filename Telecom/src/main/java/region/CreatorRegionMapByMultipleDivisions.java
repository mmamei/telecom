package region;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import utils.Config;
import utils.CopyAndSerializationUtils;
import visual.kml.KML;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.index.strtree.ItemBoundable;
import com.vividsolutions.jts.index.strtree.ItemDistance;
import com.vividsolutions.jts.index.strtree.STRtree;



public class CreatorRegionMapByMultipleDivisions {
	
	public static void main(String[] args) throws Exception {
		//RegionMap rm = create("G:/DATASET/D4D_SENEGAL/ContextData/SITE_ARR_LONLAT.CSV",3,2, true,"SenegalSplitRegions",new double[][]{{12.2426260054745,-17.69206544590599},{16.664041360519,-11.18124679877612}},5);
		RegionMap rm = create("G:/DATASET/D4D_IVORYCOAST/ORIGINAL/data/ANT_POS.TSV",2,1, false,"IvoryCoastSplitRegions",new double[][]{{4.320512785407216,-8.780925336163858},{10.69040822561101,-1.902034841193251}},5);
		
		//rm.printKML();
		CopyAndSerializationUtils.save(new File(Config.getInstance().base_folder+"/RegionMap/"+rm.getName()+".ser"), rm);
		System.out.println("TOT CELLS "+rm.getNumRegions());
		
		
		
		//RegionMap subpref = (RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/senegal_subpref.ser"));
		//Region2Region.addNameFeatures2ShpCsvFile("G:/DATASET/GEO/senegal/senegal_subpref.csv", "G:/DATASET/GEO/senegal/senegal_grid.csv", subpref, rm, "NAME_4", "NAME_5");
		
		RegionMap subpref = (RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/ivorycoast_subpref.ser"));
		Region2Region.addNameFeatures2ShpCsvFile("G:/DATASET/GEO/ivorycoast/ivorycoast_subpref.csv", "G:/DATASET/GEO/ivorycoast/ivorycoast_grid.csv", subpref, rm, "NAME_4", "NAME_5");
		
		System.out.println("Done");
	}
	
	
	public static RegionMap create(String file, int lat_i, int lon_i, boolean header, String rmname, double[][] latlonBbox, int maxNum) throws Exception{
		Map<String,double[]> antennaLatLon = processAntennaLocations(file,lat_i,lon_i,header);
		//printAntennaKML("C:/BASE/"+rmname+"-antenna.kml",rmname,antennaLatLon);
		
		
		STRtree t = new STRtree();
		for(String id: antennaLatLon.keySet()) {
			double[] latlon = antennaLatLon.get(id);
			t.insert(new Envelope(new Coordinate(latlon[0],latlon[1])), id);
		}		
		
		RegionMap rm = new RegionMap(rmname);
		
		splitRegion(rm,t,maxNum,"0",latlonBbox);
		
		return rm;
		
	}
	
	
	static GeometryFactory geomFact = new GeometryFactory();
	private static void splitRegion(RegionMap rm, STRtree t, int maxNum, String name, double[][] latlonBbox) {
		
		double x1 = latlonBbox[0][0];
		double x2 = latlonBbox[1][0];
		double y1 = latlonBbox[0][1];
		double y2 = latlonBbox[1][1];
		
		Envelope e = new Envelope(x1,x2,y1,y2);
		List result = t.query(e);
		if(result.size() > maxNum) {
			
			double x12 = (x1+x2)/2;
			double y12 = (y1+y2)/2;
			
			double[][] q1 = new double[][]{{x1,y1},{x12,y12}};
			double[][] q2 = new double[][]{{x12,y12},{x2,y2}};
			double[][] q3 = new double[][]{{x12,y1},{x2,y12}};
			double[][] q4 = new double[][]{{x1,y12},{x12,y2}};
			
			splitRegion(rm,t,maxNum,name+"1",q1); // ricorsione
			splitRegion(rm,t,maxNum,name+"2",q2);
			splitRegion(rm,t,maxNum,name+"3",q3);
			splitRegion(rm,t,maxNum,name+"4",q4);
		} 
		else if(result.size() > 0)
			rm.add(new Region(name,geomFact.createMultiPolygon(new Polygon[]{polygonFromBBox(latlonBbox)})));
	}
	
	
	static Polygon polygonFromBBox(double[][] latlonBbox) {
		double x1 = latlonBbox[0][0];
		double x2 = latlonBbox[1][0];
		double y1 = latlonBbox[0][1];
		double y2 = latlonBbox[1][1];
		return geomFact.createPolygon(new Coordinate[]{new Coordinate(y1,x1),new Coordinate(y1,x2),new Coordinate(y2,x2),new Coordinate(y2,x1),new Coordinate(y1,x1)});
	}

	static ItemDistance idist = new ItemDistance(){
		@Override
		public double distance(ItemBoundable arg0, ItemBoundable arg1) {
			if(arg0.getItem().equals(arg1.getItem())) return Double.MAX_VALUE;
			Envelope e1 = (Envelope)arg0.getBounds();
			Envelope e2 = (Envelope)arg1.getBounds();
			return e1.distance(e2);
	}};
			
	
	private static Map<String,double[]> processAntennaLocations(String file, int lat_i, int lon_i, boolean header) {
		Map<String,double[]> antennaLatLonR = new HashMap<>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;
			if(header) br.readLine();
			while((line = br.readLine())!=null){
				String[] e = line.split("\t|,");
				String id = e[0];
				double lat = Double.parseDouble(e[lat_i]);
				double lon = Double.parseDouble(e[lon_i]);
				antennaLatLonR.put(id, new double[]{lat,lon});
			}
			br.close();	
		}catch(Exception e) {
			e.printStackTrace();
		}
		return antennaLatLonR;
	}
	
	private static void printAntennaKML(String file, String name, Map<String,double[]> antennaLatLon) {
		try {
			PrintWriter out = new PrintWriter(new FileWriter(file));
			KML kml = new KML();
			kml.printHeaderFolder(out, name);
			
			out.println("<Style id='red'>");
			out.println("<IconStyle>");
			out.println("<color>ff0000ff</color>");
			out.println("<scale>1.2</scale>");
			out.println("<Icon>");
			out.println("<href>http://maps.google.com/mapfiles/kml/shapes/shaded_dot.png</href>");
			out.println("</Icon>");
			out.println("</IconStyle>");
			out.println("</Style>");
					
			for(String id: antennaLatLon.keySet()) {
				double[] ll = antennaLatLon.get(id);
				out.println("<Placemark>");
				out.println("<name>"+id+"</name>");
				out.println("<styleUrl>#red</styleUrl>");
				out.println("<Point>");
				out.println("<coordinates>"+ll[1]+","+ll[0]+",0</coordinates>");
				out.println("</Point>");
				out.println("</Placemark>");
			}
			kml.printFooterFolder(out);
			out.close();
		}catch(Exception e){
				e.printStackTrace();
		}
	}
}
