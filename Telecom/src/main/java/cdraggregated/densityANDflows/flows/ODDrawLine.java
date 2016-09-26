package cdraggregated.densityANDflows.flows;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.gps.utils.LatLonPoint;

import otherdata.TIbigdatachallenge2015.IstatCensus2011;
import region.Region;
import region.RegionI;
import region.RegionMap;
import utils.AddMap;
import utils.Config;
import utils.CopyAndSerializationUtils;
import utils.Mail;
import visual.r.RRoadNetwork;
import visual.text.TextPlotter;
import cdraggregated.densityANDflows.ObjectID2IstatCode;
import cdraggregated.densityANDflows.ZoneConverter;
import cdraggregated.densityANDflows.flows.scale.ScaleOnResidents;

public class ODDrawLine {
	
	public static boolean LOG = false;
	public static boolean INTERCEPT = true;
	public static double THRESHOLD = 20;
	public static double ABSOLUTE_MAX = 10000;
	
	public static void main(String[] args) throws Exception{
		
		RRoadNetwork.VIEW = true;
		File basedir = null;
		FilenameFilter ff = null;
		RegionMap rm = null;
		ZoneConverter zc = null;
		String subdir = null;
		
		
		//*************************************************************************************
		//									SMOD OD MATRICES 		 						
		//*************************************************************************************
		
		THRESHOLD = 100;
		/*
		basedir = new File(Config.getInstance().base_folder+"/ODMatrix/matrici_piemonte/orarie");
		ff = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
			return name.matches("[0-9]+_mod_20150527[0-9]+_20150527[0-9]+_calabrese_piemonte_comuni\\+torinoasc.txt");
			}
		};
		rm = (RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/odpiemonte.ser"));
		subdir = "20150527_calabrese-piemonte-"+rm.getName()+"-LINE";
		zc = null;
		run(basedir,ff,rm,subdir,zc);
		
		
		basedir = new File(Config.getInstance().base_folder+"/ODMatrix/matrici_emilia/orarie");
		ff = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
			return name.matches("[0-9]+_mod_20150416[0-9]+_20150416[0-9]+_calabrese_emilia_regione\\+ascbologna.txt");
			}
		};
		rm = (RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/odemiliaromagna.ser"));
		subdir = "20150416_calabrese-emilia-"+rm.getName()+"-LINE";
		zc = null;
		run(basedir,ff,rm,subdir,zc);
		
		
		//String scaled = "";
		String scaled = "-scaled-on-tot-population";
		basedir = new File(Config.getInstance().base_folder+"/ODMatrix/matrici_lombardia/orarie"+scaled);
		ff = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
			return name.matches("[0-9]+_mod_20150504[0-9]+_20150504[0-9]+_calabrese_lombardia_comuni_istat.txt");
			}
		};
		rm = (RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/odlombardia.ser"));
		subdir = "20150504_calabrese-lombardia"+scaled+"-"+rm.getName()+"-LINE";
		zc = null;
		run(basedir,ff,rm,subdir,zc);
		*/
		
		
		//*************************************************************************************
		//										TIME OD MATRICES 		 						
		//*************************************************************************************
		String scaled = "-scaled-on-residents";
		basedir = new File(Config.getInstance().base_folder+"/ODMatrix/ODMatrixTime_file_pls_piem_file_pls_piem_01-06-2015-01-07-2015_minH_0_maxH_25_ABOVE_8limit_20000_cellXHour_odpiemonte_15-06-2015"+scaled);
		System.out.println(basedir);
		ff = null;
		rm = (RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/odpiemonte.ser"));
		subdir = basedir.getName().replaceAll("_", "-")+"-LINE";
		//ZoneConverter zc = new Francia2IstatCode();
		zc = new ObjectID2IstatCode("G:/DATASET/GEO/telecom-2015-od/odpiemonte.csv");
		THRESHOLD = 10;
		run(basedir,ff,rm,subdir,zc);
		
		
		/*
		//*************************************************************************************
		//										HW OD MATRICES 		 						
		//*************************************************************************************
		
		basedir = new File(Config.getInstance().base_folder+"/ODMatrix/ODMatrixHW_file_pls_lomb_file_pls_lomb_01-03-2014-30-03-2014_minH_0_maxH_25_ABOVE_8limit_20000_cellXHour_odlombardia");
		ff = null;
		rm = (RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/odlombardia.ser"));
		subdir = basedir.getName().replaceAll("_", "-")+"-LINE";
		//ZoneConverter zc = new Francia2IstatCode();
		zc = new ObjectID2IstatCode("G:/DATASET/GEO/telecom-2015-od/odlombardia.csv");
		THRESHOLD = 1;
		run(basedir,ff,rm,subdir,zc);
		
		
		basedir = new File(Config.getInstance().base_folder+"/ODMatrix/ODMatrixHW_file_pls_piem_file_pls_piem_01-06-2015-01-07-2015_minH_0_maxH_25_ABOVE_8limit_20000_cellXHour_odpiemonte");
		ff = null;
		rm = (RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/odpiemonte.ser"));
		subdir = basedir.getName().replaceAll("_", "-")+"-LINE";
		//ZoneConverter zc = new Francia2IstatCode();
		zc = new ObjectID2IstatCode("G:/DATASET/GEO/telecom-2015-od/odpiemonte.csv");
		THRESHOLD = 1;
		run(basedir,ff,rm,subdir,zc);
		
		
		basedir = new File(Config.getInstance().base_folder+"/ODMatrix/ODMatrixHW_file_pls_er_file_pls_er_01-04-2015-30-04-2015_minH_0_maxH_25_ABOVE_8limit_20000_cellXHour_odemiliaromagna");
		ff = null;
		rm = (RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/odemiliaromagna.ser"));
		subdir = basedir.getName().replaceAll("_", "-")+"-LINE";
		//ZoneConverter zc = new Francia2IstatCode();
		zc = new ObjectID2IstatCode("G:/DATASET/GEO/telecom-2015-od/odemilia-romagna.csv");
		THRESHOLD = 1;
		run(basedir,ff,rm,subdir,zc);
		*/
		
		Mail.send("Draw line complete");
		
	}
	
	public static void run(File basedir, FilenameFilter ff, RegionMap rm, String subdir, ZoneConverter zc) throws Exception{
		
		String video_dir = Config.getInstance().base_folder+"/Videos/"+subdir;
		new File(video_dir).mkdirs();
		
		
		if(ff == null) 
			ff = new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return true;
				}};
		
		
		File[] files = basedir.listFiles(ff);
		
		File od_img_out_dir = new File(Config.getInstance().paper_folder+"/img/od/"+subdir.replaceAll("_", "-"));
		od_img_out_dir.mkdirs();
		
		
	
		
		
		Map<Integer,AddMap> hour_od = new HashMap<>();
		for(File f: basedir.listFiles(ff)) {
			if(f.getName().equals("latlon.csv")) continue;
			int h = f.getName().startsWith("od") ? Integer.parseInt(f.getName().split("-")[2]) : Integer.parseInt(f.getName().split("_")[3].substring(8, 10));
			AddMap odx = ODParser.parse(f.getAbsolutePath(),zc);
			System.out.println("Done "+h);
			hour_od.put(h, odx);
		}
		ABSOLUTE_MAX = getAbsoluteMaxmum(hour_od);
		System.out.println("===========> "+ABSOLUTE_MAX);

		for(File f: files) {
			if(f.getName().equals("latlon.csv")) continue;
			
			Map<String,Object> tm = ODParser.parseHeader(f.toString());
			// Istante di inizio: Sat, 23 May 2015 01:00
			String[] orario = ((String)tm.get("Istante di inizio")).split(" ");
			String h = orario[orario.length-1];
			if(h.contains(":")) h = h.substring(0,h.indexOf(":"));
			if(h.length()==1) h = "0"+h;
			System.out.println("==>"+h);
			
			String imgFile = go(f.getAbsolutePath(), Integer.parseInt(h), rm, od_img_out_dir.getAbsolutePath(),zc,hour_od);
			Files.copy(Paths.get(imgFile), Paths.get(video_dir+"/img"+h+".png"), StandardCopyOption.REPLACE_EXISTING);
		}
		System.out.println("Creating video");
		
		new File(video_dir+"/out.mp4").delete();
		// requires installing ffmpeg!!!!
		// 0.5 is the number of second per frame
		// 600 is the size of the video
		Runtime.getRuntime().exec("G:/Programmi/ffmpeg/bin/ffmpeg.exe -framerate 1/0.5 -i "+video_dir+"/img%02d.png  -filter:v scale=600:-1 -c:v libx264 -r 30 -pix_fmt yuv420p "+video_dir+"/out.mp4");
		

		System.out.println("The End!");
		Mail.send("Movie done");
	}
	
	
	public static double getAbsoluteMaxmum(Map<Integer,AddMap> hour_od) {
		DescriptiveStatistics ds = new DescriptiveStatistics();
		for(int h: hour_od.keySet()) {
			AddMap odx = hour_od.get(h);
			for(String k: odx.keySet()) {
				String[] from_to = k.split("-");
				if(!from_to[0].equals(from_to[1]) && odx.get(k) > 0)
					ds.addValue(odx.get(k));
			}
		}
		
		System.out.println("ABSOLUTE_MAX PERCENTILE");
		for(int i=1;i<=100;i++)
			System.out.println(i+" ==> "+ds.getPercentile(i));
		
		return Math.ceil(ds.getPercentile(99));
	}
	
	public static String go(String fileMOD, int h, RegionMap rm, String outdir, ZoneConverter zc, Map<Integer,AddMap> hour_od) throws Exception {
		fileMOD = fileMOD.replaceAll("\\\\", "/");
		AddMap odx = ODParser.parse(fileMOD,zc);		
		RegionMap converted_rm = rm;
		if(zc!=null) {
			converted_rm = new RegionMap(rm.getName());
			for(RegionI r: rm.getRegions()){
				converted_rm.add(new Region(zc.convert(r.getName()),r.getGeom()));
			}
		}
		
		
		
		//Formato HashMap RouteMatrix: <String: "lat1"+","+"lon1"+":"lat2"+","+"lon2", Double: num tot persone sulla strada>
		HashMap<String,Double> streets = new HashMap<>();
		for(String k: odx.keySet()) {
			
			double value =  odx.get(k); 
			if(value > ABSOLUTE_MAX) {
				String[] from_to = k.split("-");
				if(!from_to[0].equals(from_to[1]) && odx.get(k) > 0)
					System.out.println("Clamp "+value+" to "+ABSOLUTE_MAX);
				value = ABSOLUTE_MAX;
			}
			
			if(value > THRESHOLD) {
				String[] ab = k.split("-");
				RegionI from = converted_rm.getRegion(ab[0]);
				RegionI to = converted_rm.getRegion(ab[1]);
				
				if(from!=null && to!=null) {
					LatLonPoint pfrom = from.getCenterPoint();
					LatLonPoint pto = to.getCenterPoint();
					streets.put(pfrom.getLatitude()+","+pfrom.getLongitude()+":"+pto.getLatitude()+","+pto.getLongitude(),value);
				}
			}
		}
		
		double sum = 0;
		for(double x: streets.values())
			sum+=x;
		
		
		
		
		System.out.println("************** NUMBER OF SEGMENTS = "+streets.size()+" PEOPLE MOVING = "+((int)sum));
		System.out.println("-----------------------------------------------------------------------------------------------------------------------------");
		
		
		String name = fileMOD.substring("C:/BASE/ODMatrix/".length(), fileMOD.lastIndexOf("/"));
		name = name.replaceAll("_", "-");
		
		
		
		//create the map for text plotter with all relevant information
		Map<String,Object> tm = new HashMap<String,Object>();
		
		tm.putAll(ODParser.parseHeader(fileMOD));
		
		
		outdir = outdir.replaceAll("\\\\", "/");
		String imgFile = outdir+"/"+h+".png";
		tm.put("img", imgFile.substring(Config.getInstance().paper_folder.length()+1));
		
		String label = ((String)tm.get("Istante di inizio")).replaceAll(" ", ":")+" - "+((String)tm.get("Istante di fine")).replaceAll(" ", ":");
		
		
		RRoadNetwork.drawR(name,streets,rm,ABSOLUTE_MAX,LOG,imgFile,label);
		
		TextPlotter.getInstance().run(tm,"src/cdraggregated/densityANDflows/flows/MAPfromMOD.ftl", imgFile.replaceAll(".png", ".tex"));
			
	
		return imgFile;
		
		
	}
	
}
