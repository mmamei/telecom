package cdraggregated.densityANDflows;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

import region.RegionMap;
import utils.Config;
import utils.CopyAndSerializationUtils;
import utils.Mail;
import visual.r.RRoadNetwork;
import cdraggregated.densityANDflows.flows.MAPfromMOD;
import cdraggregated.densityANDflows.flows.MODfromISTAT;
import cdraggregated.densityANDflows.flows.ODComparator;

public class RunAllODAnalysisSMOD {
	
	public static void main(String[] args) throws Exception {
		/*
		go("matrici_piemonte/orarie","C:/BASE/ODMatrix/matrici_piemonte/map/piemonte.csv","lovisolo_piemonte_comuni+torinoasc","20150523",
				"TorinoCenter.ser","C:/DATASET/osm/piem/piem.osm",100,new Double[]{1.0},1,new ObjectID2IstatCode("G:/DATASET/GEO/telecom-2015-od/odpiemonte.csv"),new Francia2IstatCode());
		*/
		
		/*
		go("matrici_lombardia/orarie","G:/DATASET/GEO/telecom-2015-od/odlombardia.csv","calabrese_lombardia_comuni_istat","20150504",
				"MilanoCenter.ser","C:/DATASET/osm/lomb/lomb.osm",500,new Double[]{1.0},1,new ObjectID2IstatCode("G:/DATASET/GEO/telecom-2015-od/odlombardia.csv"),new Francia2IstatCode());
		*/
		
		go("matrici_emilia/orarie","G:/DATASET/GEO/telecom-2015-od/odemilia-romagna.csv","lovisolo_emilia_regione+ascbologna","20150416",
				"ModenaCenter.ser","C:/DATASET/osm/er/emilia-romagna.osm",500,new Double[]{1.0},1,new ObjectID2IstatCode("G:/DATASET/GEO/telecom-2015-od/odemilia-romagna.csv"),new Francia2IstatCode());
		
	}
	
	/*
	 * od_dir is where od matirces file are located
	 * fileCoord is the csv file (from gis) representing the zones of the od matrices being analyzed
	 * type is the type of od matrix to be considered. It is used to derive the name of the file to be considered
	 * day is the day to be considered in the form yyyymmdd
	 * od_region_ser is the region map used to draw the od matrix projected into the road network
	 * osmFile used by graphhopper
	 *  
	 */
	public static void go(String od_dir,String fileCoord,final String type, final String day, 
			String od_region_ser, String osmFile, double od_theshold, Double[] ita,int istatH,ZoneConverter zc1, ZoneConverter zc2) throws Exception {
		
		
		long startTime = System.currentTimeMillis();
		long endTime = System.currentTimeMillis();
		long dmins = 0;
	
		
		
		
		// 1. project into road
		
		
		MAPfromMOD.tolleranza = od_theshold;
		MAPfromMOD.ita = ita;
		RRoadNetwork.VIEW = false;
		
		RegionMap rm = (RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/"+od_region_ser));
		
		String baseName = "ODMatrixSMOD-"+day+"-"+type+"-"+rm.getName();
		baseName = baseName.replaceAll("_", "-");
		
		System.out.println(baseName);
		
		
		String video_dir = Config.getInstance().base_folder+"/Videos/"+baseName;
		new File(video_dir).mkdirs();
		File dir = new File("C:/BASE/ODMatrix/"+od_dir);
		
		FilenameFilter ff = new FilenameFilter() {
								@Override
								public boolean accept(File dir, String name) {
								return name.matches("[0-9]+_mod_"+day+"[0-9]+_"+day+"[0-9]+_"+type.replaceAll("\\+", "\\\\+")+".txt");
								}
		};
		
		
		
		File[] files = dir.listFiles(ff);
		
		
		File od_img_out_dir = new File(Config.getInstance().paper_folder+"/img/od/"+baseName);
		od_img_out_dir.mkdirs();
		
		
		
		for(File f: files) {
			String imgFile = MAPfromMOD.go(f.getAbsolutePath(), fileCoord, rm, osmFile, od_img_out_dir.getAbsolutePath());
			Map<String,Object> tm = ODComparator.parseHeader(f.toString());
			// Istante di inizio: Sat, 23 May 2015 01:00
			String[] orario = ((String)tm.get("Istante di inizio")).split(" ");
			String h = orario[orario.length-1];
			if(h.contains(":")) h = h.substring(0,h.indexOf(":"));
			if(h.length()==1) h = "0"+h;
			System.out.println("==>"+h);
			
			Files.copy(Paths.get(imgFile), Paths.get(video_dir+"/img"+h+".png"), StandardCopyOption.REPLACE_EXISTING);
		}
		System.out.println("Creating video");
		
		new File(video_dir+"/out.mp4").delete();
		// requires installing ffmpeg!!!!
		// 0.5 is the number of second per frame
		// 600 is the size of the video
		Runtime.getRuntime().exec("G:/Programmi/ffmpeg/bin/ffmpeg.exe -framerate 1/0.5 -i "+video_dir+"/img%02d.png  -filter:v scale=600:-1 -c:v libx264 -r 30 -pix_fmt yuv420p "+video_dir+"/out.mp4");
		
		
		endTime = System.currentTimeMillis();
		dmins = (endTime - startTime) / 1000 / 60;
		startTime = endTime;
		if(dmins > 5) Mail.send("step MAPfromMOD completed after "+dmins+" mins");
		
		
		// 2. extract od matrix from istat data
		
		String osmName = new File(osmFile).getName();
		
		
		Map<String, Integer[]> map = new HashMap<>();						//		1 Piemonte
		map.put("emilia-romagna.osm", new Integer[]{8});					//		2 Valle d'Aosta
		map.put("fi.osm", new Integer[]{9});								//		3 Lombardia
		map.put("lomb.osm", new Integer[]{3});								//		4 Trentino Alto Adige
		map.put("piem.osm", new Integer[]{1});								//		5 Veneto
		map.put("pu.osm", new Integer[]{16});								//		6 Friuli-Venezia Giulia
		map.put("ve.osm", new Integer[]{5});								//		7 Liguria
																			//		8 Emilia Romagna 
																			//		9 Toscana
																			//		10 Umbria
																			//		11 Marche
																			//		12 Lazio
																			//		13 Abruzzo
																			//		14 Molise
																			//		15 Campania
																			//		16 Puglia
																			//		17 Basalicata
																			//		18 Calabria
																			//		19 Sicilia
																			//		20 Sardegna
				
		
		
		
		File istatf= new File(Config.getInstance().base_folder+"/ODMatrix/MatriceOD_-_"+MODfromISTAT.REGIONI[map.get(osmName)[0]]+"_orario_uscita_-_"+istatH+".csv");
		if(istatf.exists()) 
			System.out.println(istatf+" already there!");
		else
			MODfromISTAT.run(new String[]{"07","11"},map.get(osmName),istatH);
		 
		// 3. compare with istat
				
		ODComparator.compareWIstat(od_dir,ff,MODfromISTAT.REGIONI[map.get(osmName)[0]],istatH,zc1,zc2,baseName);
				
				
		endTime = System.currentTimeMillis();
		dmins = (endTime - startTime) / 1000 / 60;
		startTime = endTime;
		if(dmins > 5) Mail.send("step compare od with istat completed after "+dmins+" mins");
				
		System.out.println("Done");
		
		
	}
	
}
