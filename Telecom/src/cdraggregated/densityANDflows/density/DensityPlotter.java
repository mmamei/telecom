package cdraggregated.densityANDflows.density;

import java.io.File;
import java.util.Map;

import region.RegionMap;
import utils.Config;
import utils.CopyAndSerializationUtils;
import visual.html.HeatMapGoogleMaps;
import visual.kml.KMLHeatMap;
import visual.r.RHeatMap;

public class DensityPlotter {
	
	public static void main(String[] args) throws Exception {
		
		String file = "comuni2012-HOME-null_comuni2012.ser";
		Map<String,Double> space_density = (Map<String,Double>)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/AggregatedSpaceDensity/"+file));
		RegionMap rm = (RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/"+file.split("_")[1]));
		plotSpaceDensity(file.substring(0,file.indexOf(".ser")),space_density,rm,0);
	}
	
	

	public static void plotSpaceDensity(String title, Map<String,Double> space_density, RegionMap rm, double threshold) throws Exception {
		//File d = new File(Config.getInstance().web_kml_folder);
		//d.mkdirs();
		RHeatMap.drawChoroplethMap(Config.getInstance().base_folder+"/Images/"+title+".png",space_density,rm,true,"",true,threshold);
		//KMLHeatMap.drawHeatMap(d.getAbsolutePath()+"/"+title+".kml",space_density,rm,title,true);
		//HeatMapGoogleMaps.draw(d.getAbsolutePath()+"/"+title+".html", title, space_density, rm, threshold);
	}
	
	
}
