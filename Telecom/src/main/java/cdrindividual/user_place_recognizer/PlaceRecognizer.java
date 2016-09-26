package cdrindividual.user_place_recognizer;

import java.io.File;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.gps.utils.LatLonPoint;

import utils.Config;
import utils.CopyAndSerializationUtils;
import utils.FileUtils.HowToDealWithFileHeader;
import utils.FilterAndCounterUtils;
import utils.HourDescriptiveStatistics;
import utils.Logger;
import utils.multithread.MultiWorker;
import utils.multithread.WorkerCallbackI;
import cdrindividual.CDR;
import cdrindividual.dataset.DataFactory;
import cdrindividual.dataset.EventFilesFinderI;
import cdrindividual.dataset.impl.UsersCSVCreator;
import cdrindividual.user_place_recognizer.clustering.AgglomerativeClusterer;
import cdrindividual.user_place_recognizer.weight_functions.WeightFunction;
import cdrindividual.user_place_recognizer.weight_functions.WeightOnDay;
import cdrindividual.user_place_recognizer.weight_functions.WeightOnDiversity;
import cdrindividual.user_place_recognizer.weight_functions.WeightOnTime;
import cdrindividual.user_place_recognizer.weight_functions.Weights;

public class PlaceRecognizer implements WorkerCallbackI<String> {
	
	
	public static boolean SAVE_CLUSTERS = false;
	
	public static boolean VERBOSE = false;
	
	private static PlaceRecognizer instance = null;
	
	private PlaceRecognizer() {
	}
	
	public static PlaceRecognizer getInstance() {
		if(instance == null)
			instance = new PlaceRecognizer();
		return instance;
	}
	
	
	
	public Object[] analyze(String username, String kind_of_place, List<CDR> events, double alpha, double beta, double delta, double rwf) {
 
		
		List<CDR> workingset = CopyAndSerializationUtils.clone(events);
	
				
		Logger.logln("Processing "+(username.length() > 5 ? username.substring(0,5) : username)+" "+kind_of_place);
		
		double[][] weights = Weights.get(kind_of_place);
		
		List<CDR> refEvents = Thresholding.buildReferenceTower(workingset,weights);
		workingset.addAll(refEvents);
		
		
		String tperiod = events.get(0).getTimeStamp()+"-"+events.get(events.size()-1).getTimeStamp();
		
		Map<Integer, Cluster> clusters = null;
		
		if(!SAVE_CLUSTERS)
			clusters = new AgglomerativeClusterer(3,weights,delta).buildCluster(workingset);
		else {
			File dir = new File(Config.getInstance().base_folder+"/PlaceRecognizer/Clusters");
			dir.mkdirs();
			File f = new File(dir+"/"+username+"-"+kind_of_place+"-"+tperiod+"-"+delta+".ser");
			if(f.exists()) 
				clusters = (Map<Integer, Cluster>)CopyAndSerializationUtils.restore(f);
			else {
				clusters = new AgglomerativeClusterer(3,weights,delta).buildCluster(workingset);
				CopyAndSerializationUtils.save(f, clusters);
			}
		}
		// rename the reference cluster to -1
		int found = -1;
		for(int k : clusters.keySet()) {
			Cluster c = clusters.get(k);
			if(c.getEvents().get(0).getCellac().equals(Thresholding.REF_NETWORK_CELLAC)) {
				found = k;
				break;
			}	
		}
		if(found != -1) clusters.put(-1, clusters.remove(found));
		
		/*
		System.out.println("Number of clusters --> "+clusters.size());
		for(int key:  clusters.keySet()) {
			System.out.println("  "+key+" --> "+clusters.get(key).size());
			for(PLSEvent pe :clusters.get(key).getEvents()) 
				System.out.print(pe.getCellac()+", ");
			System.out.println();
		}
		*/
		WeightFunction[] wfunctions = new WeightFunction[]{
				new WeightOnTime(1.0,weights),
				new WeightOnDay(alpha),
				new WeightOnDiversity(beta,weights)
		};
		
		
		for(Cluster c: clusters.values()) 
		for(WeightFunction wf : wfunctions) 
			wf.weight(c);
		
		
		double threshold = Double.MAX_VALUE;
		if(clusters.get(-1) != null) 
			threshold = Thresholding.weight2Threshold(kind_of_place, FilterAndCounterUtils.getNumDays(workingset), clusters.get(-1), rwf);
		
		List<Integer> selectedClustersKeys = new ArrayList<Integer>();
		List<LatLonPoint> placemarks = new ArrayList<LatLonPoint>();
		for(int k : clusters.keySet()) {
			if(k==-1) continue;
			Cluster c = clusters.get(k);
			if(c.totWeight() > threshold) {
							
				LatLonPoint p = c.getCenter(weights);
				if(p!=null) {
					placemarks.add(p);
					selectedClustersKeys.add(k);
				}
			}
		}		
		return new Object[]{clusters, placemarks,selectedClustersKeys};
	}
	
	
	
	public static final String[] KIND_OF_PLACES = new String[]{"HOME","WORK"};
	private static final SimpleDateFormat F = new SimpleDateFormat("yyyy-MM-dd-hh");
	public Map<String, List<LatLonPoint>> runSingle(String sday, String eday, String user, double lon1, double lat1, double lon2, double lat2) {
		
		System.out.println("****** PARAMS");
		System.out.println(sday+","+eday+","+user+","+lon1+","+lat1+","+lon2+","+lat2);
		System.out.println("****** PARAMS");
		
		Map<String, List<LatLonPoint>> results = null;
		try {
			EventFilesFinderI eff = DataFactory.getEventFilesFinder();
			String dir = eff.find(sday,"12",eday,"12",lon1,lat1,lon2,lat2);
			if(dir == null) return null;
			
			Config.getInstance().pls_folder = new File(Config.getInstance().pls_root_folder+"/"+dir).toString(); 
			Config.getInstance().pls_start_time.setTime(F.parse(sday+"-0"));
			Config.getInstance().pls_end_time.setTime(F.parse(eday+"-23"));
			List<CDR> events = UsersCSVCreator.process(user).getEvents(); 
			results = new HashMap<String, List<LatLonPoint>>();
			PlaceRecognizerLogger.openKMLFile(Config.getInstance().web_kml_folder+"/"+user+".kml");
			for(String kind_of_place:KIND_OF_PLACES) {
				Object[] clusters_points = analyze(user,kind_of_place,events,0.25,0.25,2000,0.6);
				Map<Integer, Cluster> clusters = (Map<Integer, Cluster>)clusters_points[0];
				List<LatLonPoint> points = (List<LatLonPoint>)clusters_points[1];
				results.put(kind_of_place, points);
				
				if(VERBOSE) PlaceRecognizerLogger.log(user, kind_of_place, clusters);
				PlaceRecognizerLogger.logcsv(user,kind_of_place,points);
				PlaceRecognizerLogger.logkml(kind_of_place, clusters, points);
			}
			PlaceRecognizerLogger.closeKMLFile();
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		return results;
	}
	
	
	// USED IN BATCH RUN *****
	
	Map<String, List<LatLonPoint>> allResults = new HashMap<String, List<LatLonPoint>>();
	public synchronized void collect(Map<String, Object[]> res) {
		
		String username = res.keySet().iterator().next().split("\\*")[0];
		if(KML_OUTPUT) PlaceRecognizerLogger.openUserFolderKML(username);
		for(String k: res.keySet()) {
			String[] user_kop = k.split("\\*");
			String user = user_kop[0];
			String kind_of_place = user_kop[1];
			Object[] clusters_points = res.get(k);
			Map<Integer, Cluster> clusters = (Map<Integer, Cluster>)clusters_points[0];
			List<LatLonPoint> points = (List<LatLonPoint>)clusters_points[1];
			List<Integer> selectedClustersKeys = (List<Integer>)clusters_points[2];
			
			List<String> timeInfo = new ArrayList<String>();
			for(int key: selectedClustersKeys) {
				timeInfo.add(getTimeInfo(clusters.get(key),kind_of_place));		
			}
			
			allResults.put(k, points);
			if(VERBOSE) PlaceRecognizerLogger.log(user, kind_of_place, clusters);
			PlaceRecognizerLogger.logcsv(user,kind_of_place,points);
			PlaceRecognizerLogger.logTimecsv(user,kind_of_place,timeInfo);
			if(KML_OUTPUT) PlaceRecognizerLogger.logkml(kind_of_place, clusters, points);
			
		}
		if(KML_OUTPUT) PlaceRecognizerLogger.closeUserFolderKML();
	}
	
	
	static final DecimalFormat DF = new DecimalFormat("##.#",new DecimalFormatSymbols(Locale.US));
	public String getTimeInfo(Cluster c, String kop) {
		
		HourDescriptiveStatistics ds = new HourDescriptiveStatistics();
		
		for(CDR cdr: c.getEvents()) {
			int dow = cdr.getCalendar().get(Calendar.DAY_OF_WEEK);
			if(dow == Calendar.SATURDAY || dow == Calendar.SUNDAY) continue;
			ds.add(cdr.getCalendar().get(Calendar.HOUR_OF_DAY));
		}
		
		
		// variance ranges from 0 when time difference between hours is 0 to 1 when time difference between hours is 12 (that is the max distance in the clock).
		// Therefore ds.variance() * 12 correspond to the variation in hours associated with that data.
		// Thus mean +- (variance * 12) roughly corresponds to the variability (maybe a /2 would be appropriate but event in this way the interval is rather small)
		
		double lower = (ds.mean() - ds.variance() * 12) % 24;
		if(lower < 0) lower += 24;
		
		
		double upper = (ds.mean() + ds.variance() * 12) % 24;
		
		
		
		return c.size()+";"+DF.format(lower)+";"+DF.format(upper);
	}
	
	
	
	
	
	
	public String[] getComputedResults() {
		List<String> results = new ArrayList<String>();
		File dir = new File(Config.getInstance().base_folder+"/PlaceRecognizer");
		for(File subdir: dir.listFiles()) {
			File f = new File(subdir+"/results.csv");
			if(f.exists())
				results.add(f.getAbsolutePath());
		}
		return results.toArray(new String[results.size()]);
	}
	
	
	public static boolean KML_OUTPUT = false;
	
	public static void main(String[] args) throws Exception {
		
		/**************************************************************************************************************************/
		/**************************************   				 BATCH RUN 					***************************************/
		/**************************************************************************************************************************/
		
		//Config.getInstance().changeDataset("ivory-set3");
		//String dir = "file_pls_ivory_users_2000_10000";
		
		
		String fileName = "file_pls_piem_file_pls_piem_01-06-2015-01-07-2015_minH_0_maxH_25_ABOVE_8limit_5000_cellXHour";
		String in_file = Config.getInstance().base_folder+"/UserCellXHour/"+fileName+".csv";
		String out_dir = Config.getInstance().base_folder+"/PlaceRecognizer/"+fileName;
		File d = new File(out_dir);
		if(!d.exists()) d.mkdirs();
		
		PlaceRecognizerLogger.openTotalCSVFile(out_dir+"/results.csv");
		PlaceRecognizerLogger.openTotalTimeCSVFile(out_dir+"/resultsTime.csv");
		if(KML_OUTPUT) PlaceRecognizerLogger.openKMLFile(out_dir+"/results.kml");
		
		PlaceRecognizer pr = PlaceRecognizer.getInstance();
		
		MultiWorker.run(in_file,HowToDealWithFileHeader.ONE_LINE,pr);
		
		
		
		if(KML_OUTPUT) PlaceRecognizerLogger.closeKMLFile();
		PlaceRecognizerLogger.closeTotalCSVFile();
		PlaceRecognizerLogger.closeTotalTimeCSVFile();
		//PlaceRecognizerEvaluator rs = new PlaceRecognizerEvaluator(2000);
		//rs.evaluate(allResults);
		
		
		/**************************************************************************************************************************/
		/**************************************   				SINGLE RUN 					***************************************/
		/**************************************************************************************************************************/
		
		/*
		PlaceRecognizer pr = new PlaceRecognizer();
		Map<String, List<LatLonPoint>> res = pr.runSingle("2012-03-06", "2012-03-07", "362f6cf6e8cfba0e09b922e21d59563d26ae0207744af2de3766c5019415af", 7.6855,45.0713,  7.6855,45.0713);
		//pr.runSingle("2012-03-06", "2012-04-30", "7f3e4f68105e863aa369e5c39ab5789975f0788386b45954829346b7ca63", 7.6855,45.0713,  7.6855,45.0713);
		for(String k: res.keySet()) {
			System.out.println(k);
			for(LatLonPoint p: res.get(k))
				System.out.println(p.getLongitude()+","+p.getLatitude());
		}
		*/
		
		
		
		Logger.logln("Done!");
		
	}

	

	@Override
	public void runMultiThread(String x) {
		List<CDR> events = CDR.getDataFormUserEventCounterCellacXHourLine(x);
		String username = events.get(0).getUsername();
		Map<String, Object[]> res = new HashMap<String, Object[]>();
		
		for(String kind_of_place:KIND_OF_PLACES) {
			
			// if(!kind_of_place.equals("HOME")) continue;
			
			res.put(username+"*"+kind_of_place, analyze(username,kind_of_place,events,0.25,0.25,2000,0.6));
		}
		collect(res);
		
	}
}


