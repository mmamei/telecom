package visual.r;

import java.awt.Desktop;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.rosuda.REngine.Rserve.RConnection;

import region.RegionMap;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

public class RRoadNetwork {
	
	public static boolean VIEW = true;
	private static RConnection c = null;
	
	
	
	public static void drawR(String title, HashMap<String, Double> streets, RegionMap rm, double absolute_max, boolean log, String file, String label) {
		//Formato HashMap RouteMatrix: <String: "lat1"+","+"lon1"+":"lat2"+","+"lon2", Double: num tot persone sulla strada>
		
		List<double[][]> latlon_segments = new ArrayList<>();
		List<Double> weights = new ArrayList<>();
		
		
		//GeometryFactory gf = new GeometryFactory();
		Envelope bbox =  rm.getEnvelope();
		
		for(String k: streets.keySet()) {
			String[] e = k.split(",|:");
			double lat1 = Double.parseDouble(e[0]);
			double lon1 = Double.parseDouble(e[1]);
			double lat2 = Double.parseDouble(e[2]);
			double lon2 = Double.parseDouble(e[3]);
			double w = streets.get(k);
			
			
			
			if(bbox.contains(lon1, lat1) || bbox.contains(lon2, lat2)) {
				latlon_segments.add(new double[][]{{lat1,lon1},{lat2,lon2}});
				weights.add(w);
			}
		}
		
		System.out.println(streets.size()+" ===================> "+weights.size());
		
		/*
		//test vr 
		try {
			PrintWriter out = new PrintWriter(new FileWriter(file.replaceAll(".png", ".csv")));
			out.println("lat1,lon1,lat2,lon2,w");
			for(String k: streets.keySet()) {
				String[] e = k.split(",|:");
				double lat1 = Double.parseDouble(e[0]);
				double lon1 = Double.parseDouble(e[1]);
				double lat2 = Double.parseDouble(e[2]);
				double lon2 = Double.parseDouble(e[3]);
				double w = streets.get(k);
				out.println(lat1+","+lon1+","+lat2+","+lon2+","+w);
			}
			out.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
		*/
		draw(title, latlon_segments, weights, absolute_max, log, rm, file, label);
	}
	
	
	
	
	public static String ALPHA = "0.1";
	public static String SIZE = "1";
	public static String COLOUR = "w";
	
	public static void draw(String title, List<double[][]> latlon_segments, List<Double> weights, double absolute_max, boolean log, RegionMap rm, String file, String opts) {
		try  {
		 file = file.replaceAll("_", "-");
		 c = new RConnection();// make a new local connection on default port (6311)
		 
		 // get the bbox for this map
		 Envelope e = rm.getEnvelope();
		 double[] lonlatBbox = new double[4];
		 lonlatBbox[0] = e.getMinX();
		 lonlatBbox[1] = e.getMinY();
		 lonlatBbox[2] = e.getMaxX();
		 lonlatBbox[3] = e.getMaxY();
		 c.assign("bbox",lonlatBbox);
		 System.out.println(RPlotter.printRVector("bbox",lonlatBbox));
		 
		
		 
		 // extract all the longitudes, latitudes, weights, colors in arrays
		 double[] start_lat = new double[latlon_segments.size()];
		 double[] start_lon = new double[latlon_segments.size()];
		 double[] end_lat = new double[latlon_segments.size()];
		 double[] end_lon = new double[latlon_segments.size()];
		 double[] w = new double[weights.size()];
		 
		
		 
		 boolean allSegmentsArePoints = true;
		 DescriptiveStatistics ds = new DescriptiveStatistics();
		 
		 for(int i=0; i<latlon_segments.size();i++) {
			 double[][] lls = latlon_segments.get(i);
			 start_lat[i] = lls[0][0];
			 start_lon[i] = lls[0][1];
			 end_lat[i] = lls[1][0];
			 end_lon[i] = lls[1][1];
			 w[i] = weights.get(i);
			 ds.addValue(w[i]);
			 
			 if(start_lat[i] != end_lat[i] || start_lon[i] != end_lon[i]) allSegmentsArePoints = false;
		 }
		 
		 if(allSegmentsArePoints)
			 w = new double[0];
		 
		 
		 c.assign("start_lat",start_lat);
		 c.assign("start_lon",start_lon);
		 c.assign("end_lat",end_lat);
		 c.assign("end_lon",end_lon);
		 c.assign("w", w);
		 
		 //System.out.println("################################################################"+ds.getMean()+" "+ds.getStandardDeviation());
		 Double stddev = ds.getStandardDeviation();
		 if(stddev.isNaN() || stddev < 0.0001) System.err.println("Weight standard deviation is 0!!! w.lenght = "+w.length+" (RRoadNetwork: 59)");
		 
		 
		 System.out.println(RPlotter.printRVector("start_lat",start_lat));
		 System.out.println(RPlotter.printRVector("start_lon",start_lon));
		 System.out.println(RPlotter.printRVector("end_lat",end_lat));
		 System.out.println(RPlotter.printRVector("end_lon",end_lon));
		 System.out.println(RPlotter.printRVector("w",w));
		 
		 
		 
		 
		 if(log) absolute_max = Math.log10(absolute_max);
		 
		 
		 String code = "library(ggplot2);"
		 			 + "library(ggmap);"+
				       "amap <- c(bbox);"+
		 			   (log? "w=log10(1+w);":"")+
				       (w.length > 0 ?"z<-data.frame(start_lon,start_lat,end_lon,end_lat,w);":"")+
				       "amap.map = get_map(location = amap, maptype='terrain', color='bw');"+
				       //"ggmap(amap.map, extent = 'device', legend='bottomright')+"+
				       "ggm <- ggmap(amap.map) + theme(legend.justification=c(1,0), legend.position=c(1,0));"+
				       "real_bbox <- attr(amap.map, 'bb');"+
				       "ggm = ggm + "+
				       (w.length > 0 ?"scale_alpha(limits=c(0,1),guide='none') + scale_size(limits=c(0,100),guide='none') +scale_colour_continuous(limits=c("+absolute_max/100+", "+absolute_max+"), low = 'red', high = 'blue',guide = guide_colorbar(title = "+(log?"'log10 Pers'":"'Num. Persons'")+"))+":"")+
				       //"theme(axis.title = element_blank(), text = element_text(size = 18))+"+
				       (w.length > 0 ?"geom_segment(data=z,aes(x=start_lon,y=start_lat,xend=end_lon,yend=end_lat,size="+SIZE+",colour="+COLOUR+",alpha="+ALPHA+"))+":"")+
				       //"guides(color=guide_legend(), size = guide_legend())+"+
				       (opts!=null ? "geom_label(size=10, fontface='bold', x=real_bbox$ur.lon,y=real_bbox$ur.lat,hjust='inward',vjust='inward',label.padding = unit(0.2, 'lines'),label=\""+opts+"\")+":"")+
				       "ggsave('"+file.replaceAll("\\\\", "/")+"',width=10, height=10);";
   
		 System.out.println(code.replaceAll(";", ";\n"));
		 
		 c.eval(code);
		 
         c.close();
         if(VIEW) Desktop.getDesktop().open(new File(file));
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
