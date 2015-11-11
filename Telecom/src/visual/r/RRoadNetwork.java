package visual.r;

import java.awt.Desktop;
import java.io.File;
import java.util.List;

import org.rosuda.REngine.Rserve.RConnection;

import analysis.densityANDflows.flows.ODMatrixHW;

import com.vividsolutions.jts.geom.Envelope;

import region.RegionMap;
import utils.Config;
import utils.CopyAndSerializationUtils;
import utils.Logger;

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
		 
		 
		 // extract all the longitudes, latitudes, weights, colors in arrays
		 double[] start_lat = new double[latlon_segments.size()];
		 double[] start_lon = new double[latlon_segments.size()];
		 double[] end_lat = new double[latlon_segments.size()];
		 double[] end_lon = new double[latlon_segments.size()];
		 double[] w = new double[weights.size()];
		 for(int i=0; i<latlon_segments.size();i++) {
			 double[][] lls = latlon_segments.get(i);
			 start_lat[i] = lls[0][0];
			 start_lon[i] = lls[0][1];
			 end_lat[i] = lls[1][0];
			 end_lon[i] = lls[1][1];
			 w[i] = weights.get(i);
		 }
		 c.assign("start_lat",start_lat);
		 c.assign("start_lon",start_lon);
		 c.assign("end_lat",end_lat);
		 c.assign("end_lon",end_lon);
		 c.assign("w", w);
		 
		 
		 
		 String code = "library(ggmap);"+
				       "amap <- c(bbox);"+
				       "z<-data.frame(start_lon,start_lat,end_lon,end_lat,w);"+
				       "amap.map = get_map(location = amap, maptype='terrain', color='bw');"+
				       "ggmap(amap.map, extent = 'device', legend='bottomright')+"+
				       "theme(axis.title = element_blank(), text = element_text(size = 18))+"+
				       "geom_segment(data=z,aes(x=start_lon,y=start_lat,xend=end_lon,yend=end_lat,size=w,colour=w),lineend = 'round')+"+
				       "guides(color=guide_legend(), size = guide_legend())+"+
				       "ggsave('"+file+"',width=10, height=10);";
   
		 System.out.println(code.replaceAll(";", ";\n"));
         c.eval(code);
         c.close();
         if(VIEW) Desktop.getDesktop().open(new File(file));
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
	
	public static void main(String[] args) throws Exception {
		String regionMap = "grid5";
		String places_file = Config.getInstance().base_folder+"/PlaceRecognizer/file_pls_piem_users_200_100/results.csv";
		ODMatrixHW od = new ODMatrixHW();
		od.runAll(places_file, regionMap, "",45.0813,7.6417,45.0347,7.698);
		Logger.log("Done!");
	}
	
}
