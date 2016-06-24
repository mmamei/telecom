package cdraggregated.densityANDflows.density;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import region.RegionI;
import region.RegionMap;
import utils.Config;
import utils.CopyAndSerializationUtils;
import visual.r.RHeatMap;
import visual.text.TextPlotter;

public class DensityPlotter {
	
	public static void main(String[] args) throws Exception {
		
	
		//String file = "file_pls_piem_file_pls_piem_01-06-2015-01-07-2015_minH_0_maxH_25_ABOVE_400limit_1000_cellXHour-comuni2012-HOME-null.ser";
		//String file = "file_pls_piem_file_pls_piem_01-06-2015-01-07-2015_minH_0_maxH_25_ABOVE_8limit_5000_cellXHour-comuni2012-HOME-null.ser";
		String file = "file_pls_lomb_file_pls_lomb_01-03-2014-30-03-2014_minH_0_maxH_25_ABOVE_8limit_20000_cellXHour-MilanoCenter-HOME-null.ser";
		
		String rm_name = file.split("-")[6];
		System.out.println(rm_name);
		
		Map<String,Double> space_density = (Map<String,Double>)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/AggregatedSpaceDensity/"+file));
		System.out.println(space_density);
		
		
		RegionMap rm = (RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/"+rm_name+".ser"));
		plotSpaceDensity(file.substring(0,file.indexOf(".ser")),space_density,rm,false,0);
	}
	
	

	public static void plotSpaceDensity(String title, Map<String,Double> space_density, RegionMap rm, boolean log, double threshold) throws Exception {
		//File d = new File(Config.getInstance().web_kml_folder);
		//d.mkdirs();
		title = title.replaceAll("_", "-");
		String img = "img/density/"+title+".png";
		RHeatMap.drawChoroplethMap(Config.getInstance().paper_folder+"/"+img,"density",space_density,rm,log,true,threshold);
		//KMLHeatMap.drawHeatMap(d.getAbsolutePath()+"/"+title+".kml",space_density,rm,title,true);
		//HeatMapGoogleMaps.draw(d.getAbsolutePath()+"/"+title+".html", title, space_density, rm, threshold);
		
		
		/**** TEST TO WRITE CSV FILE */
		
		PrintWriter out = new PrintWriter(new FileWriter(Config.getInstance().base_folder+"/test-vr-"+title+"-.csv"));
		out.println("lat,lon,val");
		for(RegionI r: rm.getRegions()) {
			Double val = space_density.get(r.getName());
			//System.out.println(r.getName()+" = "+val);
			if(val != null && val > threshold) 
				out.println(r.getLatLon()[0]+","+r.getLatLon()[1]+","+val);
		}
		out.close();
		
		
		
		Map<String,Object> tm = new HashMap<String,Object>();
		tm.put("img", img);
		tm.putAll(parseFileName(title));
		tm.put("gt", "istat");
		
		TextPlotter.getInstance().run(tm,"src/cdraggregated/densityANDflows/density/DensityPlotter.ftl", Config.getInstance().paper_folder+"/"+img.replaceAll(".png", ".tex"));		
		
	}
	
	private static Map<String,String> REGION2REGION = new HashMap<>();
	static {
		REGION2REGION.put("piem", "Piemonte");
		REGION2REGION.put("lomb", "Lombardia");
		REGION2REGION.put("er", "Emilia Romagna");
	}
	
	public static Map<String,Object> parseFileName(String file) {
		Map<String,Object> tm = new HashMap<String,Object>();
		// parse title
		String[] e = file.split("-|_");
		tm.put("region",REGION2REGION.get(e[2]));
		tm.put("startdate", e[6]+"-"+e[7]+"-"+e[8]);
		tm.put("enddate", e[9]+"-"+e[10]+"-"+e[11]);
		tm.put("minh", e[13]);
		tm.put("maxh", e[15]);
		tm.put("above", e[17].replaceAll("limit", ""));
		tm.put("limit", e[18]);
		tm.put("rm",e[20]);
		tm.put("kop", e[21]);
		tm.put("!kop", e[22]);
		return tm;
	}
	
	
	
}
