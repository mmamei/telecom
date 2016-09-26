package cdrindividual.densityANDflows.flows;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import region.CreatorRegionMapGrid;
import region.RegionI;
import region.RegionMap;
import utils.Config;
import utils.CopyAndSerializationUtils;
import utils.ListMapArrayBasicUtils;
import utils.Logger;
import visual.r.RPlotter;
import cdrindividual.Constraints;
import cdrindividual.densityANDflows.density.UserPlaces;

public class ODMatrixHW {
	public static void main(String[] args) throws Exception {
		
		
		//String regionMap = "FIX_Piemonte.ser";
		//String regionMap = "grid5";
		String regionMap = "odpiemonte.ser";
		
		
		//String places_file = Config.getInstance().base_folder+"/PlaceRecognizer/file_pls_piem_users_200_100/results.csv";
		//String places_file = Config.getInstance().base_folder+"/PlaceRecognizer/file_pls_piem_file_pls_piem_01-06-2015-01-07-2015_minH_0_maxH_25_ABOVE_400limit_1000_cellXHour/results.csv";
		String places_file = Config.getInstance().base_folder+"/PlaceRecognizer/file_pls_piem_file_pls_piem_01-06-2015-01-07-2015_minH_0_maxH_25_ABOVE_8limit_20000_cellXHour/results.csv";
		String time_file = Config.getInstance().base_folder+"/PlaceRecognizer/file_pls_piem_file_pls_piem_01-06-2015-01-07-2015_minH_0_maxH_25_ABOVE_8limit_20000_cellXHour/resultsTime.csv";
		
		
		
		ODMatrixHW od = new ODMatrixHW();
		
		
		
		String js = od.runAll(places_file,time_file, regionMap, "",45.0813,7.6417,45.0347,7.698);
		//System.out.println(js);
		Logger.log("Done!");
	}
	
	public String runAll(String places_file, String time_file, String regionMap, String sconstraints,double minlat,double minlon, double maxlat,double maxlon) {
		return runAll(places_file,time_file,regionMap,new Constraints(sconstraints),minlat,minlon,maxlat,maxlon);
	}
	
	
	public String runAll(String places_file, String time_file, String regionMap, Constraints constraints,double minlat,double minlon, double maxlat,double maxlon) {
		
		try {			
			Map<String,UserPlaces> up = UserPlaces.readUserPlaces(places_file,time_file);
			//plotHWTimeDistribution(up);
			
			
			// load the region map
			RegionMap rm = null;
			if(regionMap.startsWith("grid")) {
				/*
				double minlon = Double.MAX_VALUE;
				double minlat = Double.MAX_VALUE;
				double maxlon = -Double.MAX_VALUE;
				double maxlat = -Double.MAX_VALUE;
				// get user places bbox
				for(UserPlaces x: up.values()) 
				for(List<double[]> l: x.lonlat_places.values()) 
				for(double[] lonlat: l) {
					minlon = Math.min(minlon, lonlat[0]);
					minlat = Math.min(minlat, lonlat[1]);
					maxlon = Math.max(maxlon, lonlat[0]);
					maxlat = Math.max(maxlat, lonlat[1]);
				}	
				*/		
				double[][] lonlat_bbox = new double[][]{{minlon,minlat},{maxlon,maxlat}};
				int size = Integer.parseInt(regionMap.substring("grid".length()));
				rm = CreatorRegionMapGrid.process("grid", lonlat_bbox, size);
			}
			else rm = (RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/"+regionMap));
						
			
			
			// hour --> list_od (Map<Move,Double>)
			Map<Integer, Map<Move,Double>> hour2listod = new HashMap<>();
			for(int i=0; i<24;i++)
				hour2listod.put(i, new HashMap<Move,Double>());
			
			
			for(UserPlaces p: up.values()) {
				List<double[]> homes = p.lonlat_places.get("HOME");
				List<double[]> works = p.lonlat_places.get("WORK");
				
				List<double[]> homest = p.clustersize_minTime_maxTime.get("HOME");
				List<double[]> workst = p.clustersize_minTime_maxTime.get("WORK");
				
				if(homes != null && works !=null && homest != null && workst !=null) {
					double z = 1.0 / homes.size() * works.size();
					
					for(int i=0; i<homes.size();i++)
					for(int j=0; j<works.size();j++) {
						
						double[] h = homes.get(i);
						double[] w = works.get(j);
						
						double[] ht = homest.get(i);
						double[] wt = workst.get(j);
						
						RegionI rh = rm.get(h[0], h[1]);
						RegionI rw = rm.get(w[0], w[1]);
						
						int hour_h2w = (int)(Math.round((ht[2] + wt[1])/2));
						int hour_w2h = (int)(Math.round((ht[1] + wt[2])/2));
						
						if(hour_h2w > 23) hour_h2w = 23;
						if(hour_w2h > 23) hour_w2h = 23;
						
	
						if(rh!=null && rw!=null) {
							
							// home to work
							Move h2w = new Move(rh,rw);
							Double c = hour2listod.get(hour_h2w).get(h2w);
							c = c == null ? z : c+z;
							hour2listod.get(hour_h2w).put(h2w, c);
							
							// work to home
							Move w2h = new Move(rw,rh);
							c = hour2listod.get(hour_w2h).get(w2h);			
							c = c == null ? z : c+z;
							hour2listod.get(hour_w2h).put(w2h, c);
							
						}
					}
				}
			}
			
			// save result in od matrix files
			String name = places_file.substring((Config.getInstance().base_folder+"/PlaceRecognizer/").length(), places_file.indexOf("/results.csv"))+"_"+rm.getName();
			for(int h: hour2listod.keySet()) {
				ODMatrixPrinter.print("ODMatrixHW_"+name,hour2listod.get(h),rm,"HW",h,h+1);
			}
			
			//return ODMatrixVisual.draw("ODMatrixHW_"+region,"ODMatrixHW_"+region,false,region);
			// prepare for drawing
			//return ODMatrixVisual.draw("ODMatrixHW_"+region,list_od,false,region,rm);
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	public static void plotHWTimeDistribution(Map<String,UserPlaces> up) {
		List<Double> home_minTime = new ArrayList<Double>();
		List<Double> home_maxTime = new ArrayList<Double>();
		
		List<Double> work_minTime = new ArrayList<Double>();
		List<Double> work_maxTime = new ArrayList<Double>();
		
		for(UserPlaces p: up.values()) {
			List<double[]> homesT = p.clustersize_minTime_maxTime.get("HOME");
			List<double[]> worksT = p.clustersize_minTime_maxTime.get("WORK");
			if(homesT != null) 
				for(double[] clustersize_minTime_maxTime: homesT){
					home_minTime.add(clustersize_minTime_maxTime[1]);
					home_maxTime.add(clustersize_minTime_maxTime[2]);	
				}
			
			if(worksT != null) 
				for(double[] clustersize_minTime_maxTime: worksT){
					work_minTime.add(clustersize_minTime_maxTime[1]);
					work_maxTime.add(clustersize_minTime_maxTime[2]);	
				}
		}
		
		
		List<double[]> y = new ArrayList<>();
		y.add(ListMapArrayBasicUtils.toArray(home_minTime));
		y.add(ListMapArrayBasicUtils.toArray(home_maxTime));
		y.add(ListMapArrayBasicUtils.toArray(work_minTime));
		y.add(ListMapArrayBasicUtils.toArray(work_maxTime));
		
		List<String> names = new ArrayList<>();
		names.add("HminT");
		names.add("HmaxT");
		names.add("WminT");
		names.add("WmaxT");
		
		RPlotter.drawBoxplot(y, names, "places-time", "hours", Config.getInstance().paper_folder+"/img/home-work-time.pdf", 20, null);
	}
	
}