package visual.html;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Map;

import utils.mod.Util;

import com.vividsolutions.jts.geom.Geometry;

public class ODMatrixGoogleMaps {
//	orig universal/HTMLWriter2	scribe il file html un segmento alla volta

	public static void simpleHTML(Map<String, Double> streets, Map<String, Double> streetsForecast, Map<String, Geometry> geoList, String[] comToVisualize, String[] provToVisualize, String percorsoOutput) throws Exception {
		streets = Util.reduceMap(comToVisualize, provToVisualize, streets, geoList);
		System.out.println("streetsOk size: "+streets.size());
		File file= new File(percorsoOutput+"/Mappa.html");
		file.createNewFile();
		FileWriter fw = new FileWriter(file); 
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write("<html xmlns=http://www.w3.org/1999/xhtml xmlns:v=\"urn:schemas-microsoft-com:vml\">\n"
				+ "<head>\n"
				+ "<script\n"
				+ "src=\"http://maps.googleapis.com/maps/api/js\">\n"
				+ "</script>\n"
				+ "<script src=\"http://maps.googleapis.com/maps/api/js?key=AIzaSyBPaPK8TLhtlXovEJkzNhgLyDY9av2UVps\"></script>\n"
				+ "<script>\n"
				+ "function initialize() {\n"
				+ "	var mapProp = {	center:new google.maps.LatLng(45.4664539,9.170471), zoom:11, mapTypeId:google.maps.MapTypeId.ROADMAP};\n"
				+ "	var map=new google.maps.Map(document.getElementById(\"googleMap\"), mapProp);\n"
				+ "\n");
//		double Max = calcMax(streets);
//		Max=1;
		int n=0;
		
		for(String s:streetsForecast.keySet()){
			n++;
			String c[]=s.split(":");
			String cp[]=c[0].split(",");
			String ca[]=c[1].split(",");
			bw.write("var p"+n+"=[new google.maps.LatLng("+cp[0]+", "+cp[1]+"), new google.maps.LatLng("+ca[0]+", "+ca[1]+")];\n"
	    	+ "	var Pt"+n+"=new google.maps.Polyline({"
	    	+ "	path:p"+n+",\n"
			+ "	strokeColor:\"#0000FF\","
	    	+ "	strokeWeight:6,"
	    	+ " strokeOpacity:0.5"
	    	+ "	});"
	    	+ "	Pt"+n+".setMap(map);\n");
			
			System.out.println(streetsForecast.get(s)+"  #0000FF  "+n);
		}
		
		
		
		for(String s:streets.keySet()){
				n++;
				String c[]=s.split(":");
				String cp[]=c[0].split(",");
				String ca[]=c[1].split(",");
				bw.write("var p"+n+"=[new google.maps.LatLng("+cp[0]+", "+cp[1]+"), new google.maps.LatLng("+ca[0]+", "+ca[1]+")];\n"
		    	+ "	var Pt"+n+"=new google.maps.Polyline({"
		    	+ "	path:p"+n+",\n"
				+ "	strokeColor:\""+calcColorGoogle(streets.get(s))+"\","
		    	+ "	strokeWeight:3"
		    	+ "	});"
		    	+ "	Pt"+n+".setMap(map);\n");
				
	//			System.out.println(streets.get(s)+"  "+calcColor(streets.get(s),Max)+"  "+n);
		}
		bw.write(""
				+ "	}\n"
				+ "google.maps.event.addDomListener(window, 'load', initialize);\n"
				+ "</script>\n"
				+ "</head>\n"
				+ "<body>\n"
				+ "<div id=\"googleMap\" style=\"width:1280px;height:631px;\"></div>\n"
				+ "</body>\n"
				+ "</html>\n");
		System.out.println("Fine HTML");
		bw.close();
	}
	
	private static String calcColorGoogle(Double voc) {
		// GREEN = 84CA50
		// ORANGE = F07D02
		// RED = E60000
		// DARK RED = 9E1313
		// Verde: non ci sono ritardi dovuti al traffico.
		String color = "#84CA50";
		// Arancione: volume di traffico medio.
		if (voc > 0.5)
			color = "#F07D02";
		// Rosso: ritardi dovuti al traffico.
		if (voc > 1)
			color = "#E60000";
		// Più scuro è il rosso, più ridotta è la velocità del traffico sulla
		// strada.
		if (voc > 1.2)
			color = "#9E1313";
		return color;
	}
}
