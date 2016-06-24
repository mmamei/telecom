package cdraggregated.densityANDflows;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Map;

import region.RegionMap;
import utils.Config;
import utils.CopyAndSerializationUtils;
import utils.Mail;
import utils.multithread.MultiWorker;
import visual.r.RRoadNetwork;
import cdraggregated.densityANDflows.flows.MAPfromMOD;
import cdraggregated.densityANDflows.flows.ODParser;
import cdrindividual.dataset.impl.PLSParser;
import cdrindividual.dataset.impl.UserCDRCounter;
import cdrindividual.dataset.impl.UserCellacXHour;
import cdrindividual.densityANDflows.density.PopulationDensityPlaces;
import cdrindividual.densityANDflows.flows.ODMatrixHW;
import cdrindividual.user_place_recognizer.PlaceRecognizer;
import cdrindividual.user_place_recognizer.PlaceRecognizerLogger;

/*
 * This main is a single point to run a pipeline with ALL the OD analysis.
 * Starts from getting users, aggregate PLS data, computing HW, computing densities, computing OD
 * Analyzing correlations creating videos,....
 */

public class RunAllODAnalysisHW {

	static final boolean PLACE_RECOGNIZER_KML_OUTPUT = false;
	
	// istatH
	// 1 prima delle 7,15;
	// 2 dalle 7,15 alle 8,14;
	// 3 dalle 8,15 alle 9,14;
	// 4 dopo le 9,14;

	
	public static void main(String[] args) throws Exception {
		
		
		go("file_pls_er",new GregorianCalendar(2015,Calendar.APRIL,1),new GregorianCalendar(2015,Calendar.APRIL,30),8,20000,
				"ModenaCenter.ser","ModenaCenter.ser","C:/DATASET/osm/er/emilia-romagna.osm",1, new Double[]{1.0}, 1,null,null);
		
		go("file_pls_piem",new GregorianCalendar(2015,Calendar.JUNE,1),new GregorianCalendar(2015,Calendar.JUNE,31),8,20000,
				"TorinoCenter.ser","TorinoCenter.ser","C:/DATASET/osm/piem/piem.osm",1, new Double[]{1.0}, 1,null,null);
		
		
		go("file_pls_lomb",new GregorianCalendar(2014,Calendar.MARCH,1),new GregorianCalendar(2014,Calendar.MARCH,30),8,20000,
				"MilanoCenter.ser","MilanoCenter.ser","C:/DATASET/osm/lomb/lomb.osm",1, new Double[]{1.0}, 1,null,null);
		
		
		
		
		/*
		go("file_pls_piem",new GregorianCalendar(2015,Calendar.JUNE,1),new GregorianCalendar(2015,Calendar.JUNE,31),8,20000,
				"comuni2012.ser","odpiemonte.ser","C:/DATASET/osm/piem/piem.osm",1, new Double[]{1.0}, 1,
				new ObjectID2IstatCode("G:/DATASET/GEO/telecom-2015-od/odlombardia.csv"),new Francia2IstatCode());
		
		go("file_pls_lomb",new GregorianCalendar(2014,Calendar.MARCH,1),new GregorianCalendar(2014,Calendar.MARCH,30),8,20000,
				"comuni2012.ser","odlombardia.ser","C:/DATASET/osm/lomb/lomb.osm",1, new Double[]{1.0}, 1,
				new ObjectID2IstatCode("G:/DATASET/GEO/telecom-2015-od/odlombardia.csv"),new Francia2IstatCode());
		
		
		go("file_pls_er",new GregorianCalendar(2015,Calendar.APRIL,1),new GregorianCalendar(2015,Calendar.APRIL,30),8,20000,
				"comuni2012.ser","odemiliaromagna.ser","C:/DATASET/osm/er/emilia-romagna.osm",1, new Double[]{1.0}, 1,
				new ObjectID2IstatCode("G:/DATASET/GEO/telecom-2015-od/odemilia-romagna.csv"),new Francia2IstatCode());
				
		*/
		
	}
	
	
	// there are two separate 'density_region_ser' and 'od_region_ser' because often even though the polygons are the same, the name given to the region is different
	// in order to match different istat groundtruth names.
	// In particular:
	// * density_region_ser MUST be compatible with IstatCensus2011
	
	
	public static void go(String region, Calendar startCal, Calendar endCal, int MIN_PLS_X_DAY, int MAX_USERS_RETRIEVED,
			String density_region_ser, String od_region_ser, String osmFile,double od_theshold, Double[] ita, Integer istatH, ZoneConverter zc1, ZoneConverter zc2) throws Exception {
		
		long startTime = System.currentTimeMillis();
		long endTime = System.currentTimeMillis();
		long dmins = 0;
		
		// 1. run UserCDRCounter
		PLSParser.REMOVE_BOGUS = false;
		Integer minH = null;
		Integer maxH = null;
		File f025 = UserCDRCounter.runCdrCounter(region, startCal, endCal, minH, maxH);
		
		
		endTime = System.currentTimeMillis();
		dmins = (endTime - startTime) / 1000 / 60;
		startTime = endTime;
		if(dmins > 5) Mail.send("step UserCDRCounter completed after "+dmins+" mins");
		
		
		// 2. run UserCDRCounter at night (will be used to detect bogus)
		minH = 1;
		maxH = 3;
		File f13 = UserCDRCounter.runCdrCounter(region, startCal, endCal, minH, maxH);
		//UserCDRCounter.percentAnalysis(f13);
		
		endTime = System.currentTimeMillis();
		dmins = (endTime - startTime) / 1000 / 60;
		startTime = endTime;
		if(dmins > 5) Mail.send("step UserCDRCounter at night completed after "+dmins+" mins");
		
		
		
		// 3. get bogus users
		
		File outfile = new File(Config.getInstance().base_folder+"/UserCDRCounter"+"/"+region+"_bogus.csv");
		if(outfile.exists())
			System.out.println("bogus file already there");
		else
			UserCDRCounter.extractUsersAboveThreshold(f13,outfile, 2,-1);
		
		
		// 4. reset pls parser parameters
		
		PLSParser.REMOVE_BOGUS = true;
		PLSParser.MIN_HOUR = 0;
		PLSParser.MAX_HOUR = 25;
		
		// 5. extract users above threshold to be tested
		
		outfile = new File(Config.getInstance().base_folder+"/UserCDRCounter/"+f025.getName().replaceAll(".csv", "_ABOVE_"+MIN_PLS_X_DAY+(MAX_USERS_RETRIEVED > -1 ? "limit_"+MAX_USERS_RETRIEVED : "")+".csv"));
			
		if(outfile.exists())
			System.out.println(outfile+" already there!");
		else 
			UserCDRCounter.extractUsersAboveThreshold(f025,outfile, MIN_PLS_X_DAY,MAX_USERS_RETRIEVED);
		
		
		// 6. run UserCellacXHour to get data from selected users
		startTime = System.currentTimeMillis();
		File sercellXHour = UserCellacXHour.process(outfile.getAbsolutePath(),false);
		
		endTime = System.currentTimeMillis();
		dmins = (endTime - startTime) / 1000 / 60;
		startTime = endTime;
		if(dmins > 5) Mail.send("step UserCellacXHour completed after "+dmins+" mins");
		
		
		// 7. run place recognizer to detect home and work
		
		String fileName = sercellXHour.getName().replaceAll(".ser", "");
		String in_file = Config.getInstance().base_folder+"/UserCellXHour/"+fileName+".csv";
		String out_dir = Config.getInstance().base_folder+"/PlaceRecognizer/"+fileName;
	
		File d = new File(out_dir);
		if(d.exists()) 
			System.out.println(out_dir+" already there!");
		else {	
			d.mkdirs();
			PlaceRecognizerLogger.openTotalCSVFile(out_dir+"/results.csv");
			PlaceRecognizerLogger.openTotalTimeCSVFile(out_dir+"/resultsTime.csv");
			if(PLACE_RECOGNIZER_KML_OUTPUT) PlaceRecognizerLogger.openKMLFile(out_dir+"/results.kml");
			
			PlaceRecognizer pr = PlaceRecognizer.getInstance();
			
			MultiWorker.run(in_file,pr);
			
			if(PLACE_RECOGNIZER_KML_OUTPUT) PlaceRecognizerLogger.closeKMLFile();
			PlaceRecognizerLogger.closeTotalCSVFile();
			PlaceRecognizerLogger.closeTotalTimeCSVFile();
		}
		
		
		endTime = System.currentTimeMillis();
		dmins = (endTime - startTime) / 1000 / 60;
		startTime = endTime;
		if(dmins > 5) Mail.send("step Place Recognizer completed after "+dmins+" mins");
		
		// 8. run density home
		
		File f_aggregated_density = new File(Config.getInstance().base_folder+"/AggregatedSpaceDensity/"+fileName+"-"+density_region_ser.replaceAll(".ser", "")+"-HOME-null.ser");		
		if(f_aggregated_density.exists())
			System.out.println(f_aggregated_density+" already there!");
		else {
			PopulationDensityPlaces pdp = new PopulationDensityPlaces();
			pdp.runAll(out_dir+"/results.csv", density_region_ser, "HOME", null,"",0,0,0,0);
		}
		
		endTime = System.currentTimeMillis();
		dmins = (endTime - startTime) / 1000 / 60;
		startTime = endTime;
		if(dmins > 5) Mail.send("step compute density homes completed after "+dmins+" mins");
		
		
		// 9. run od matrices
		
		File od_output_dir = new File(Config.getInstance().base_folder+"/ODMatrix/ODMatrixHW_"+fileName+"_"+od_region_ser.replaceAll(".ser", ""));
		if(od_output_dir.exists())
			System.out.println(od_output_dir+" already there!");
		else {
			ODMatrixHW od = new ODMatrixHW();
			od.runAll(out_dir+"/results.csv",out_dir+"/resultsTime.csv", od_region_ser,"",0,0,0,0);
		}
		
		
		endTime = System.currentTimeMillis();
		dmins = (endTime - startTime) / 1000 / 60;
		startTime = endTime;
		if(dmins > 5) Mail.send("step compute od completed after "+dmins+" mins");
		
		// This end the cdr individual analysis.
		// Now I have both density and od aggregated information
		
		
		/*
		// 10. plot density homes
		boolean log = true;
		String file = f_aggregated_density.getName();
		String rm_name = density_region_ser.replaceAll(".ser", "");
		Map<String,Double> space_density = (Map<String,Double>)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/AggregatedSpaceDensity/"+file));
		RegionMap rm = (RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/"+rm_name+".ser"));
		DensityPlotter.plotSpaceDensity(file.substring(0,file.indexOf(".ser")),space_density,rm,log,0);
		
		endTime = System.currentTimeMillis();
		dmins = (endTime - startTime) / 1000 / 60;
		startTime = endTime;
		if(dmins > 5) Mail.send("step plot density homes completed after "+dmins+" mins");
		
		
		// 11. compare density homes with istat
		
		AddMap istat = IstatCensus2011.getInstance().computeDensity(0, false, false);
		DensityComparator.compare(file.replaceAll(".ser", ""),space_density,"istat-demographic-2011",istat);
		
		endTime = System.currentTimeMillis();
		dmins = (endTime - startTime) / 1000 / 60;
		startTime = endTime;
		if(dmins > 5) Mail.send("step compare density with istat completed after "+dmins+" mins");
		
		
		
		// 12. create od matrix line
		
		//File basedir = new File(Config.getInstance().base_folder+"/ODMatrix/ODMatrixHW_file_pls_lomb_file_pls_lomb_01-03-2014-30-03-2014_minH_0_maxH_25_ABOVE_8limit_20000_cellXHour_odlombardia");
		rm = (RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/"+od_region_ser));
		String subdir = od_output_dir.getName().replaceAll("_", "-")+"-LINE";
		ODDrawLine.SCALE = false;
		ODDrawLine.THRESHOLD = 1;
		ODDrawLine.run(od_output_dir,null,rm,subdir,zc1);
		
		
		
		// 13. extract od matrix from istat data
		
		Map<String, Integer[]> map = new HashMap<>();						//		1 Piemonte
		map.put("file_pls_er", new Integer[]{8});							//		2 Valle d'Aosta
		map.put("file_pls_fi", new Integer[]{9});							//		3 Lombardia
		map.put("file_pls_lomb", new Integer[]{3});							//		4 Trentino Alto Adige
		map.put("file_pls_piem", new Integer[]{1});							//		5 Veneto
		map.put("file_pls_pu", new Integer[]{16});							//		6 Friuli-Venezia Giulia
		map.put("file_pls_ve", new Integer[]{5});							//		7 Liguria
		map.put("file_pls_veneto", new Integer[]{5});						//		8 Emilia Romagna 	
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
		
		File istatf= new File(Config.getInstance().base_folder+"/ODMatrix/MatriceOD_-_"+MODfromISTAT.REGIONI[map.get(region)[0]]+"_orario_uscita_-_"+istatH+".csv");
		if(istatf.exists()) 
			System.out.println(istatf+" already there!");
		else
			MODfromISTAT.run(new String[]{"07","11"},map.get(region),istatH);
 
		// 14. compare with istat
		
		ODComparator.compareWIstat(od_output_dir.getName(),null,MODfromISTAT.REGIONI[map.get(region)[0]],istatH,zc1,null);
		
		
		endTime = System.currentTimeMillis();
		dmins = (endTime - startTime) / 1000 / 60;
		startTime = endTime;
		if(dmins > 5) Mail.send("step compare od with istat completed after "+dmins+" mins");
		
		*/
		
		// 15. project into road
		
		
		RegionMap rm = (RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/"+od_region_ser));
		String fileCoord = od_output_dir+"/latlon.csv";
				
		String video_dir = Config.getInstance().base_folder+"/Videos/"+od_output_dir.getName();
		new File(video_dir).mkdirs();
				
		MAPfromMOD.tolleranza = od_theshold;
		MAPfromMOD.ita = ita;
		RRoadNetwork.VIEW = false;
				
		File od_img_out_dir = new File(Config.getInstance().paper_folder+"/img/od/"+od_output_dir.getName().replaceAll("_", "-"));
		od_img_out_dir.mkdirs();
				
		System.out.println(od_output_dir);
				
		for(File f: od_output_dir.listFiles()) {
				if(f.getName().startsWith("od")) {
					String imgFile = MAPfromMOD.go(f.getAbsolutePath(), fileCoord, rm, osmFile, od_img_out_dir.getAbsolutePath());
						
					Map<String,Object> tm = ODParser.parseHeader(f.toString());
					String h = ((String)tm.get("Istante di inizio")).split(" ")[1];
					if(h.length()==1) h = "0"+h;
						
									
					Files.copy(Paths.get(imgFile), Paths.get(video_dir+"/img"+h+".png"), StandardCopyOption.REPLACE_EXISTING);
				}
		}
		new File(video_dir+"/out.mp4").delete();
		// requires installing ffmpeg!!!!
		// 0.5 is the number of second per frame
		// 600 is the size of the video
		Runtime.getRuntime().exec("G:/Programmi/ffmpeg/bin/ffmpeg.exe -framerate 1/0.5 -i "+video_dir+"/img%02d.png  -filter:v scale=600:-1 -c:v libx264 -r 30 -pix_fmt yuv420p "+video_dir+"/out.mp4");
				
				
		endTime = System.currentTimeMillis();
		dmins = (endTime - startTime) / 1000 / 60;
		startTime = endTime;
		if(dmins > 5) Mail.send("step road network completed after "+dmins+" mins");
	
		
		System.out.println("Done");
		
	}
	
}
