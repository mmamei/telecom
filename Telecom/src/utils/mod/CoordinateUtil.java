package utils.mod;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;

import com.graphhopper.util.shapes.GHPoint;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.WKTReader;

public class CoordinateUtil {
	
//	riempie l'hashmap coord con codIstat e le sue relative coordinate puntuali
	public static HashMap<String, GHPoint> setCoord(String fileMOD, String fileCoord) throws Exception{		
		
		HashMap<String, GHPoint> coord = new HashMap<String, GHPoint>();
		
		BufferedReader c = new BufferedReader(new FileReader(fileCoord));
		String line="";
		
		BufferedReader od = new BufferedReader(new FileReader(fileMOD));
		String lineOD="";
		for(int i=0;i<14;i++)	od.readLine();
		lineOD=od.readLine();
		String[] codIstat=lineOD.split("\t");
		
		while((line=c.readLine())!=null){
			String[] l=line.split("\t");
			for(int i=1; i<codIstat.length; i++) {
				if(codIstat[i].trim().equals(l[0].trim())){
					String s []= l[1].trim().split(",");
					GHPoint p = new GHPoint();
					p.lat = Double.parseDouble(s[0]); 
					p.lon = Double.parseDouble(s[1]); 
					coord.put(codIstat[i], p);
				}
			}
		}
		c.close();
		od.close();
		return coord;
	}
	
//	riempie l'hashmap coord con codIstat e le sue relative coordinate puntuali, usando una matrice O/D booleana per alleggerire il carico di lavprp sulla ram.
//	usata per i dati ISTAT
	public static HashMap<String, GHPoint> setCoord(String fileMBOD, String fileMOD, String fileCoord) throws Exception{		//riempie l'hashmap coord con codIstat e le sue relative coordinate
		HashMap<String, GHPoint> coord = new HashMap<String, GHPoint>();
		BufferedReader br = new BufferedReader(new FileReader(fileCoord));
		BufferedReader bs = new BufferedReader(new FileReader(fileMBOD));
		String line=bs.readLine();
		String[] codIstat=line.split("\t");
		
		while((line=br.readLine())!=null){
			String[] l=line.split(";");
			for(int i=1; i<codIstat.length; i++) {
				GHPoint p = new GHPoint(Double.parseDouble(l[3]), Double.parseDouble(l[4]));
				if(codIstat[i].equals(l[1]+"-"+l[2]))	coord.put(codIstat[i], p);
			}
		}
		br.close();
		bs.close();
		return coord;		
	}
	
	
	
//	riempie l'hashmap PolyCoord con codIstat e le sue relative coordinate geometriche
	public static HashMap<String,Geometry> setPolycoord(String nomefile) throws Exception	{
		HashMap<String,Geometry> p = new HashMap<String,Geometry> ();
		String line;
		BufferedReader bfr = new BufferedReader(new FileReader(nomefile));
		line=bfr.readLine();	// skip header
		while((line = bfr.readLine())!=null) {
			String[] e = line.split("\t");
			//p.put(Util.uniformeCode(e[7]), new WKTReader().read(e[0].replaceAll("\"", "")));
			p.put(e[1], new WKTReader().read(e[0].replaceAll("\"", "")));
		}
		bfr.close();
		return p;
	}
	
	
//	usata per i dati se in polyMode crea un punto random nell'area geografica data
	public static GHPoint GenerateRandomPoint(Geometry g){
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
		}	while(!g.contains(p)); // check if the point is within the geometry, it not repeat
		
		return new GHPoint(lat,lon);
	}
	
	
	
	
}