package visual.r;

import java.awt.Desktop;
import java.io.File;
import java.util.List;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.rosuda.REngine.Rserve.RConnection;

import region.RegionMap;

import com.vividsolutions.jts.geom.Envelope;

public class RRoadNetwork {
	
	public static boolean VIEW = true;
	private static RConnection c = null;
	
	public static void draw(String title, List<double[][]> latlon_segments, List<Double> weights, List<String> colors, boolean directed, RegionMap rm, String file, String opts) {
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
		 //System.out.println(printRVector("bbox",lonlatBbox,10));
		 
		 // extract all the longitudes, latitudes, weights, colors in arrays
		 double[] start_lat = new double[latlon_segments.size()];
		 double[] start_lon = new double[latlon_segments.size()];
		 double[] end_lat = new double[latlon_segments.size()];
		 double[] end_lon = new double[latlon_segments.size()];
		 double[] w = new double[weights.size()];
		 
		 DescriptiveStatistics ds = new DescriptiveStatistics();
		 
		 for(int i=0; i<latlon_segments.size();i++) {
			 double[][] lls = latlon_segments.get(i);
			 start_lat[i] = lls[0][0];
			 start_lon[i] = lls[0][1];
			 end_lat[i] = lls[1][0];
			 end_lon[i] = lls[1][1];
			 w[i] = weights.get(i);
			 ds.addValue(w[i]);
		 }
		 c.assign("start_lat",start_lat);
		 c.assign("start_lon",start_lon);
		 c.assign("end_lat",end_lat);
		 c.assign("end_lon",end_lon);
		 c.assign("w", w);
		 
		 //System.out.println("################################################################"+ds.getMean()+" "+ds.getStandardDeviation());
		 if(ds.getStandardDeviation() < 0.0001) System.err.println("Wight standard deviation is 0!!! (RRoadNetwork: 59)");
		 
		 
		 System.out.println(printRVector("start_lat",start_lat,10));
		 System.out.println(printRVector("start_lon",start_lon,10));
		 System.out.println(printRVector("end_lat",end_lat,10));
		 System.out.println(printRVector("end_lon",end_lon,10));
		 System.out.println(printRVector("w",w,10));
		 
		 double annotx = 0.8 * lonlatBbox[0] + 0.2 * lonlatBbox[2];
		 double annoty = 0.28 * lonlatBbox[1] + 0.72 * lonlatBbox[3];
		 
		 //System.out.println(annotx+","+annoty);
		 
		 String code = "library(ggplot2);"
		 			 + "library(ggmap);"+
				       "amap <- c(bbox);"+
				       (w.length > 0 ?"z<-data.frame(start_lon,start_lat,end_lon,end_lat,w);":"")+
				       "amap.map = get_map(location = amap, maptype='terrain', color='bw');"+
				       //"ggmap(amap.map, extent = 'device', legend='bottomright')+"+
				       "ggmap(amap.map, extent = 'device', legend='bottomright')+"+
				       //"theme(axis.title = element_blank(), text = element_text(size = 18))+"+
				       (w.length > 0 ?"geom_segment(data=z,aes(x=start_lon,y=start_lat,xend=end_lon,yend=end_lat,size=1.0,colour=w),lineend = 'round')+":"")+
				       //"guides(color=guide_legend(), size = guide_legend())+"+
				       (opts!=null ? "annotate(\"text\", size=11, fontface='bold', x="+annotx+", y="+annoty+", adj=0, label=\""+opts+"\")+":"")+
				       "ggsave('"+file.replaceAll("\\\\", "/")+"',width=10, height=10);";
   
		 System.out.println(code.replaceAll(";", ";\n"));
		 
		 c.eval(code);
		 
         c.close();
         if(VIEW) Desktop.getDesktop().open(new File(file));
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static String printRVector(String name, double[] x, int max) {
		if(x.length==0) return "";
		StringBuffer sb = new StringBuffer(name+"<-c("+x[0]);
		for(int i=1; i<Math.min(x.length, max);i++)
			sb.append(","+x[i]);
		sb.append(");");
		return sb.toString();
	}
}
