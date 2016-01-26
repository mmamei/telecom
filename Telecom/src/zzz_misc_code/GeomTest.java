package zzz_misc_code;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.WKTReader;

public class GeomTest {

	public static void main(String[] args) throws Exception {
		Map<String,Geometry> rm = new HashMap<String,Geometry>();
		BufferedReader br = new BufferedReader(new FileReader("C:/BASE/ODMatrix/ODMatrixHW_file_pls_piem/latlon.csv"));
		String line;
		line = br.readLine(); // skip header
		while((line=br.readLine())!=null) {
			String[] e = line.split("\t");
			rm.put(e[1], new WKTReader().read(e[0].replaceAll("\"", "")));
		}
		br.close();
		
		// select a region
		String name = "0,0";
		
		// get geometry, boundig bot e coordinates of the bbox
		Geometry g = rm.get(name);
		Coordinate[] coord = g.getEnvelope().getCoordinates(); // get bounding box
		double minlon = coord[0].x;
		double minlat = coord[0].y; // coord[0] is bottom left corner
		double maxlon = coord[2].x;
		double maxlat = coord[2].y; // coord[2] is top rigth corner
		
		
		
		// useful to generate Geometry objects
		GeometryFactory gf = new GeometryFactory();
		
		
		// generate random point uniformly distributed over geometry
		double lon = 0;
		double lat = 0;
		Point p = null;
		do {
			// generate random lon,lat uniformly over the bbox
			lon = minlon + Math.random() * (maxlon - minlon);
			lat = minlat + Math.random() * (maxlat - minlat);
			// conver in Point object
			p = gf.createPoint(new Coordinate(lon,lat));
		}while(!g.contains(p)); // check if the point is within the geometry, it not repeat
		
		
		// print random point
		System.out.println(lat+""+lon+" is a random point inside "+name);
		
		
	}		
}
