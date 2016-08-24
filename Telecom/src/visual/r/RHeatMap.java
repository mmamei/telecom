package visual.r;

import java.awt.Desktop;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.rosuda.REngine.Rserve.RConnection;

import region.RegionI;
import region.RegionMap;
import utils.Config;
import utils.CopyAndSerializationUtils;
import utils.Logger;
import cdrindividual.densityANDflows.density.PopulationDensityPlaces;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

public class RHeatMap {
	
	
	
	public static void main(String[] args) throws Exception {
		PopulationDensityPlaces pdp = new PopulationDensityPlaces();
		
		//pdp.runAll(Config.getInstance().base_folder+"/PlaceRecognizer/file_pls_piem_users_200_100/results.csv", "FIX_Piemonte.ser", "HOME", "SATURDAY_NIGHT","",0,0,0,0);
		
		
		//pdp.runAll(Config.getInstance().base_folder+"/PlaceRecognizer/file_pls_piem_users_200_10000/results_piem.csv", "torino_circoscrizioni_geo.ser", "SATURDAY_NIGHT", "HOME","",0,0,0,0);
		//pdp.runAll(Config.getInstance().base_folder+"/PlaceRecognizer/file_pls_lomb_users_200_10000/results_lomb.csv", "milano_circoscrizioni_geo.ser", "SUNDAY", "HOME","",0,0,0,0);
		
		//pdp.runAll(Config.getInstance().base_folder+"/PlaceRecognizer/fast_home_Torino.csv", "torino_circoscrizioni_geo.ser", "HOME", null,"",0,0,0,0);
		//pdp.runAll(Config.getInstance().base_folder+"/PlaceRecognizer/file_pls_lomb_users_200_10000/results_lomb.csv", "lombardia-od-2015.ser", "HOME", null,"",0,0,0,0);
		
		
		
		
		RegionMap rm = (RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/tic-roma-grid.ser"));
		Map<String,Double> density = new HashMap<String,Double>();
		for(RegionI r: rm.getRegions())
			if(r.getName().startsWith("3420"))
				density.put(r.getName(), 1.0);
		
		drawChoroplethMap(Config.getInstance().base_folder+"/Images/"+rm.getName()+".png","test",density,rm,false,true,0);
				
		Logger.logln("Done!");
	}
	
	
	
	public static boolean VIEW = true;
	private static RConnection c = null;
	
	
	
	public static void drawChoroplethMap(String file, String title, Map<String,Double> density, RegionMap rm, boolean log, boolean ggmap, double threshold) {
		
		
		// extract arrays with all the coordinates, ids, and density values
		List<Double> llat = new ArrayList<Double>();
		List<Double> llon = new ArrayList<Double>();
		List<String> lid = new ArrayList<String>();
		List<Double> lval = new ArrayList<Double>();
		
		
		for(RegionI r : rm.getRegions()) {
			Double val = density.get(r.getName());
			if(val!=null && val > threshold) {
				Coordinate[] coord = r.getGeom().getCoordinates();
				for(int i=0; i<coord.length;i++) {
					llat.add(coord[i].y);
					llon.add(coord[i].x);
					lid.add(r.getName());
					if(val == null) val = 0.0;
					if(log && val != 0) val = Math.log(val);
					lval.add(val);
				}
			}
		}
		
		double[] lonlatBbox = new double[]{200,200,-200,-200};
		double[] lat = new double[llat.size()];
		double[] lon = new double[llon.size()];
		String[] id = new String[lid.size()];
		double[] val = new double[lval.size()];
		
		for(int i=0;i<lat.length;i++) {
			lat[i] = llat.get(i);
			lon[i] = llon.get(i);
			id[i] = lid.get(i);
			val[i] = lval.get(i);
			lonlatBbox[0] = Math.min(lon[i], lonlatBbox[0]);
			lonlatBbox[1] = Math.min(lat[i], lonlatBbox[1]);
			lonlatBbox[2] = Math.max(lon[i], lonlatBbox[2]);
			lonlatBbox[3] = Math.max(lat[i], lonlatBbox[3]);
		}
		
		
		
		lonlatBbox = getProperLonlatBbox(lonlatBbox);
		
		if(lonlatBbox == null) {
			System.err.println("Cannot create map for "+file);
			return;
		}
		
		System.out.println(file+" bbox = ["+lonlatBbox[0]+","+lonlatBbox[1]+"]["+lonlatBbox[2]+","+lonlatBbox[3]+"]");
		
		
		String code = "";
		
		try {
			c = new RConnection();// make a new local connection on default port (6311)
			c.assign("lat", lat);
			c.assign("lon", lon);
			c.assign("id", id);
			c.assign("val", val);
			c.assign("bbox",lonlatBbox);
			
			//ggmap = false;
			
			code = "library(ggplot2);"
				 + "library(ggmap);"
				 + "z<-data.frame(lat,lon,id,val);"
				// BELOW ARE TWO STYLES FOR MAP, UNCOMMENT THE ONE TO USE
				 + "amap.map = get_map(location = c(bbox), maptype='terrain', color='bw', crop=FALSE);"
				 //+ "amap.map = get_map(location = c(bbox), source='stamen', maptype='watercolor', crop=FALSE);"
				 + (ggmap?   "ggmap(amap.map)" : "ggplot()")
				 // *************************************************
				 // BELOW ARE TWO STYLES FOR MAP, UNCOMMENT THE ONE TO USE. FIRST FOR CHOROPLETH, SECOND FOR SHAPE ONLY
				 + "+ scale_fill_continuous(low = 'red', high = 'blue',guide = guide_colorbar(title='"+title+"'))"
				 + "+ geom_polygon(data = z, aes(x = lon, y = lat, group = id, fill = val), colour = 'black', alpha = 0.5) + theme_bw() + theme(legend.position = c(0.95, 0.1));"
				 //+ "+ geom_polygon(data = z, aes(x = lon, y = lat, group = id, fill = val), colour = 'black', alpha = 0.1) + theme_bw() + theme(legend.position = 'none');"
				 + "ggsave('"+file+"',width=10, height=10);";
         
			  c.eval(code);	
			  //System.err.println(RPlotter.printRVector("id",id));
			  //System.err.println(RPlotter.printRVector("lat",lat));
			  //System.err.println(RPlotter.printRVector("lon",lon));
			  //System.err.println(RPlotter.printRVector("val",val));
			  //System.err.println(RPlotter.printRVector("bbox",lonlatBbox));
			  //System.err.println(code.replaceAll(";", ";\n"));
			  
			  
	          c.close();
	          if(VIEW) Desktop.getDesktop().open(new File(file));
	     			
		}catch(Exception e) {
			if(e.getMessage().startsWith("Cannot connect")) {
             	System.err.println("You must launch the following code in R");
             	System.err.println("library(Rserve)");
             	System.err.println("Rserve()");
            }
            else {
            	c.close();
            	e.printStackTrace();
            	System.err.println(RPlotter.printRVector("id",id));
            	System.err.println(RPlotter.printRVector("lat",lat));
  			  	System.err.println(RPlotter.printRVector("lon",lon));
  			  	System.err.println(RPlotter.printRVector("val",val));
  			  	System.err.println(RPlotter.printRVector("bbox",lonlatBbox));
            	System.err.println(code.replaceAll(";", ";\n"));
            }
		}
	}
	
	
	private static double[] getProperLonlatBbox(final double[] lonlatBbox) {
		String code = null;
		try {
			c = new RConnection();// make a new local connection on default port (6311)
			
			double buffer = 0;
			double[] use_lonlatBbox = new double[4];
			double[] real_lonlatBbox = new double[4];
			while(!(real_lonlatBbox[0] < lonlatBbox[0] && real_lonlatBbox[1] < lonlatBbox[1] && 
				  real_lonlatBbox[2] > lonlatBbox[2] && real_lonlatBbox[3] > lonlatBbox[3])) {
				
				use_lonlatBbox[0] = lonlatBbox[0] - buffer;
				use_lonlatBbox[1] = lonlatBbox[1] - buffer;
				use_lonlatBbox[2] = lonlatBbox[2] + buffer;
				use_lonlatBbox[3] = lonlatBbox[3] + buffer;
				//System.err.println(RPlotter.printRVector("bbox",use_lonlatBbox));
				c.assign("bbox",use_lonlatBbox);	
				code = "library(ggplot2);"
					 + "library(ggmap);"
					 // BELOW ARE TWO STYLES FOR MAP, UNCOMMENT THE ONE TO USE
					 + "amap.map = get_map(location = c(bbox), maptype='terrain', color='bw', crop=FALSE);"
					 //+ "amap.map = get_map(location = c(bbox), source='stamen', maptype='watercolor', crop=FALSE);"
					 + "bbox <- attr(amap.map, 'bb');";
	         
				c.eval(code);
				
				
				real_lonlatBbox[0] = c.eval("bbox$ll.lon").asDouble();
				real_lonlatBbox[1] = c.eval("bbox$ll.lat").asDouble();
				real_lonlatBbox[2] = c.eval("bbox$ur.lon").asDouble();
				real_lonlatBbox[3] = c.eval("bbox$ur.lat").asDouble();
				buffer += 0.1;
				//System.out.println("-------------------- "+buffer);
			}
			
			
			
	        c.close();
	        return use_lonlatBbox;
	        
	        
		}catch(Exception e) {
			if(e.getMessage().startsWith("Cannot connect")) {
             	System.err.println("You must launch the following code in R");
             	System.err.println("library(Rserve)");
             	System.err.println("Rserve()");
            }
            else {
            	c.close();
            	e.printStackTrace();
            	System.err.println(code);
            }
		}
		
		return null;
		
	}
	
	public static void drawHeatMap(String file, Map<String,Double> density, RegionMap rm, boolean log, String text) {
		List<double[]> points = new ArrayList<double[]>();
		
		double max = 0;
		for(RegionI r: rm.getRegions()) {
			Double val = density.get(r.getName());
			if(log) val = Math.log10(val);
			if(val != null) max = Math.max(max,val);
		}
		
		for(RegionI r: rm.getRegions()) {
			Double val = density.get(r.getName());
			if(log) val = Math.log10(val);
			if(val != null) {
				double[][] lonlatbbox = r.getBboxLonLat(); // {{minlon,minlat},{maxlon,maxlat}}
				
				//System.out.println(lonlatbbox[0][0]+","+lonlatbbox[0][1]+","+lonlatbbox[1][0]+","+lonlatbbox[1][1]);
				int npoint = (int)(1000.0 * val / max);
				for(int i=0; i<npoint;i++) {
				// generate a random point within the bounding box
					
					double lon = lonlatbbox[0][0] + Math.random() * (lonlatbbox[1][0] - lonlatbbox[0][0]);
					double lat = lonlatbbox[0][1] + Math.random() * (lonlatbbox[1][1] - lonlatbbox[0][1]);
					points.add(new double[]{lon,lat});
				}
			}
		}
		
		
		double[] lon = new double[points.size()];
		double[] lat = new double[points.size()];
		for(int i=0; i<points.size();i++){
			lon[i] = points.get(i)[0];
			lat[i] = points.get(i)[1];
		}
		//System.out.println(points.size());
		
		Envelope e = rm.getEnvelope();
		double[] lonlatBbox = new double[4];
		lonlatBbox[0] = e.getMinX();
		lonlatBbox[1] = e.getMinY();
		lonlatBbox[2] = e.getMaxX();
		lonlatBbox[3] = e.getMaxY();
		
		drawHeatMap(lat,lon,lonlatBbox,file,text);
		
	}
	
	private static void drawHeatMap(double[] lat, double [] lon, double[] lonlatBbox, String file, String text) {
		String code = null;
		try {
			file = file.replaceAll("_", "-");
			System.out.println(file);
            c = new RConnection();// make a new local connection on default port (6311)
            
          
            c.assign("lat", lat);
            c.assign("lon", lon);
            c.assign("bbox",lonlatBbox);
            
           
            
            // install.packages('ggmap');
            	   code = "library(ggmap);"+
         				  "W <- data.frame(lat,lon);"+
         				  "amap <- c(bbox);"+
         				  "amap.map = get_map(location = amap, maptype='terrain', color='bw');"+
         				  "ggmap(amap.map, extent = 'device', legend='bottomright')+"+
         				  "geom_density2d(data = W, aes(x = lon, y = lat), colour='black') +"+
         				  "stat_density2d(data = W, aes(x = lon, y = lat, fill = ..level.., alpha = ..level..),size = 0.01, bins = 16, geom = 'polygon') + scale_fill_gradient(name='"+text+"',low = 'yellow', high = 'red') + scale_alpha(range = c(0.00, 0.25), guide = FALSE)+"+
         				  "theme(axis.title = element_blank(), text = element_text(size = 18));"+
         				  "ggsave('"+file+"',width=10, height=10);";
            
            //System.out.println(code);
            c.eval(code);
            c.close();
            if(VIEW) Desktop.getDesktop().open(new File(file));
        } catch (Exception e) {
        	if(e.getMessage().startsWith("Cannot connect")) {
             	System.err.println("You must launch the following code in R");
             	System.err.println("library(Rserve)");
             	System.err.println("Rserve()");
            }
            else {
            	c.close();
            	e.printStackTrace();
            	System.err.println(code);
            }
        }      
	}
}
