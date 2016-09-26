package cdraggregated.synch;

import static cdraggregated.synch.TableNames.Country.*;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import otherdata.TIbigdatachallenge2015.Deprivation;
import otherdata.TIbigdatachallenge2015.MEF_IRPEF;
import otherdata.TIbigdatachallenge2015.SocialCapital;
import otherdata.d4d.afrobarometer.AfroBarometer;
import otherdata.d4d.ophi.Ophi;
import region.Region2Region;
import region.RegionI;
import region.RegionMap;
import utils.AddMap;
import visual.kml.KMLColorMap;
import visual.kml.KMLHeatMap;
import visual.r.RHeatMap;
import cdraggregated.synch.TableNames.Country;
import cdraggregated.synch.timedensity.TimeDensity;
import cdraggregated.synch.timedensity.TimeDensityFactory;

public class Evaluation {
	
	
	static List<String> cities_whose_map_we_draw = new ArrayList<String>();
	static {
		cities_whose_map_we_draw.add("roma");
		cities_whose_map_we_draw.add("Diourbel");
		cities_whose_map_we_draw.add("Abidjan");
	}
	
	static final String YLAB = SynchCompute.USE_FEATURE;
	
	static String YLAB_SIMPLE = "Synch";
	static {
		if(SynchCompute.USEF.equals(SynchCompute.Feature.I)) YLAB_SIMPLE = "I";
		if(SynchCompute.USEF.equals(SynchCompute.Feature.RSQ)) YLAB_SIMPLE = "R2";
		if(SynchCompute.USEF.equals(SynchCompute.Feature.EU)) YLAB_SIMPLE = "EU";
	}
	
	public static void evaluate(List<String> cities, Country country, String startTime, String endTime, List<StatsCollection> city_stats) {
		String dir =SynchCompute.getDir();
		new File(dir).mkdirs();
		System.err.println(dir);
		try {
			drawBoxPlots(cities,country,startTime, endTime, city_stats,dir);
			writeFeatureFile(cities,country,startTime, endTime, city_stats,dir);
			drawMap(cities,country,startTime, endTime, city_stats,dir);
		}catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	
	
	
	private static void drawMap(List<String> cities,  Country country, String startTime, String endTime, List<StatsCollection> city_stats, String dir) throws Exception {
		for(int i=0; i<cities.size();i++) {
			String city = cities.get(i);
			
			if(cities_whose_map_we_draw != null && !cities_whose_map_we_draw.contains(city)) continue;
			
			
			StatsCollection stats = city_stats.get(i);
			
			RegionMap rm = TableNames.getRegionMap(city, country);
			
			
			TimeDensity td = TimeDensityFactory.getInstance(city,country,startTime,endTime);
			Map<String,String> mapping = td.getMapping(rm); // 2026_3_0_3_1=63073, 
			Map<String,Integer> assignments = KMLColorMap.toIntAssignments(mapping); // 2026_3_0_3_1=0,
			
			
			Set<String> comuni = new HashSet<String>();
			for(String c: mapping.values())
				comuni.add(c);
					
			// trimmed map. Considers only regions (i.e., comuni) that are within the city. 
			RegionMap trm = new RegionMap(city);
			for(RegionI r: rm.getRegions())
				if(comuni.contains(r.getName()))
				trm.add(r);
			
			
			AddMap cellXComuneCount = new AddMap();
			Map<String,Integer> comune2assignement = new HashMap<>();
			for(String cell: mapping.keySet()) {
				String comune = mapping.get(cell);
				cellXComuneCount.add(comune, 1);
				Integer assignment = assignments.get(cell);
				Integer prev_assignment = comune2assignement.get(comune);
				
				if(assignment!=null && prev_assignment!=null && assignment!=prev_assignment)
					System.err.println("mmmmmmmm------- "+comune);
				
				if(assignment != null)
					comune2assignement.put(comune, assignment);
			}
			
			new File(dir+"/maps").mkdirs();
			
			
			
			reallyDraw(dir+"/maps",city,"within-"+startTime.substring(0, startTime.lastIndexOf("-")),trm,comune2assignement,cellXComuneCount,stats.intraXcomune);
			reallyDraw(dir+"/maps",city,"between-"+startTime.substring(0, startTime.lastIndexOf("-")),trm,comune2assignement,cellXComuneCount,stats.interXcomune);
		}
		
	}
	
	private static void reallyDraw(String dir, String city, String title, RegionMap rm, Map<String,Integer> comune2assignement, AddMap cellXComuneCount , DescriptiveStatistics[] stats) {
		Map<String,Double> density = new HashMap<>();
		Map<String,String> kml_descriptions = new HashMap<>();
		for(String comune: comune2assignement.keySet()) {
			DescriptiveStatistics ds = stats[comune2assignement.get(comune)];
			double mean = ds.getMean();
			if(Double.isNaN(mean))
				mean = 0;
			density.put(comune, mean);
			kml_descriptions.put(comune.toLowerCase(), "num_cell="+cellXComuneCount.get(comune)+",mean="+mean+", median="+ds.getPercentile(50));
		}
		
		
		
		try {
			KMLHeatMap.drawHeatMap(dir+"/map-"+city+"-"+title+".kml", density, rm, kml_descriptions, false);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// print density to csv
		try {
			PrintWriter out = new PrintWriter(new FileWriter(dir+"/map-"+city+"-"+title+".csv"));
			for(String k: density.keySet())
				out.println(k+","+density.get(k));
			out.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		RHeatMap.drawChoroplethMap(dir+"/map-"+city+"-"+title+".png", title+" synch", density, rm, false, true, -1);
	}
	
	
	
	
	
	private static void drawBoxPlots(List<String> cities,  Country country, String startTime, String endTime, List<StatsCollection> city_stats,String dir) throws Exception {
		List<double[]> intra = new ArrayList<double[]>();
		List<double[]> inter = new ArrayList<double[]>();	
		for(StatsCollection sc: city_stats) {
			double[] v = sc.intra.getValues();
			//if(v==null || v.length==0) v = new double[]{0};
			intra.add(v);
			
			v = sc.inter.getValues();
			//if(v==null || v.length==0) v = new double[]{0};
			inter.add(v);
		}
		writeCSV(cities,intra,dir+"/within"+country+"-"+startTime.substring(0, startTime.lastIndexOf("-"))+".csv");
		writeCSV(cities,inter,dir+"/between"+country+"-"+startTime.substring(0, startTime.lastIndexOf("-"))+".csv");
		RPlotterWithinBetweenSynch.drawBoxplot(dir+"/within"+country+"-"+startTime.substring(0, startTime.lastIndexOf("-"))+".csv", dir+"/between"+country+"-"+startTime.substring(0, startTime.lastIndexOf("-"))+".csv", "", YLAB_SIMPLE, dir+"/boxplot"+country+"-"+startTime.substring(0, startTime.lastIndexOf("-"))+".png");
	}
	
	
	
	static boolean MEAN = false; // false = MEDIAN
	
	private static void writeFeatureFile(List<String> cities,  Country country, String startTime, String endTime, List<StatsCollection> city_stats, String dir) {
		try {
			
			
			Map<String,Map<String,Double>> socioeconomicV = getSocioEconomicVariables(country);
			PrintWriter out = new PrintWriter(new FileWriter(dir+"/features-data"+country+"-"+startTime.substring(0, startTime.lastIndexOf("-"))+".csv"));
			out.print("city,regione,avg_intra,avg_inter,sd_intra,sd_inter,avg,sd");
			for(String k: socioeconomicV.keySet())
				out.print(","+k);
			out.println();
			
			for(int i=0; i<cities.size();i++) { // [napoli, bari, caltanissetta, siracusa, benevento, palermo, campobasso, roma, siena, ravenna, ferrara, venezia, torino, asti, milano]
				
				String provincia = TableNames.city2province(cities.get(i),country);
				String regione = TableNames.city2region(cities.get(i),country);
				
				StatsCollection stc = city_stats.get(i);
				
				double avg_intra = MEAN ? stc.intra.getMean() : stc.intra.getPercentile(50);
				double avg_inter = MEAN ? stc.inter.getMean() : stc.inter.getPercentile(50);
				
				double sd_intra = MEAN ? stc.intra.getStandardDeviation() : (stc.intra.getPercentile(75) - stc.intra.getPercentile(25));
				double sd_inter = MEAN ? stc.inter.getStandardDeviation() : (stc.inter.getPercentile(75) - stc.inter.getPercentile(25));
				
				//System.out.print("intra = "+stc.intra.getN()+" inter = "+stc.inter.getN());
				
				for(double v: stc.intra.getSortedValues())
					stc.inter.addValue(v);
				
				//System.out.println(" all = "+stc.inter.getN());
				
				double avg = MEAN ? stc.inter.getMean() : stc.inter.getPercentile(50);
				double sd = MEAN ? stc.inter.getStandardDeviation() : (stc.inter.getPercentile(75) - stc.inter.getPercentile(25));
					
				out.print(cities.get(i)+","+regione+","+avg_intra+","+avg_inter+","+sd_intra+","+sd_inter+","+avg+","+sd);
				for(String k: socioeconomicV.keySet()) {
					Map<String,Double> m = socioeconomicV.get(k);
					Double v  = k.equals("depriv")? m.get(regione): m.get(provincia);
					out.print(","+v);
				}
				out.println(); 
			}
			out.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	private static Map<String,Map<String,Double>> getSocioEconomicVariables(Country country) {
		Map<String,Map<String,Double>> socioeconomicV = new TreeMap<>();
		if(country.equals(Italy)) {
			MEF_IRPEF mi = MEF_IRPEF.getInstance();
			SocialCapital sc = SocialCapital.getInstance();
			socioeconomicV.put("depriv", Deprivation.getInstance().getDepriv());
			socioeconomicV.put("rpc", mi.redditoPCProvince());
			socioeconomicV.put("blood", sc.getBlood());
			socioeconomicV.put("assoc", sc.getAssoc());
			socioeconomicV.put("referendum", sc.getReferendum());
			socioeconomicV.put("soccap", sc.getSocCap());
		}
		if(country.equals(IvoryCoast)) {
			//Ophi ophi = new Ophi("G:/DATASET/CENSUS/ophi/ophi-ivorycoast-province.csv");
			Ophi ophi = new Ophi("G:/DATASET/CENSUS/ophi/ophi-ivorycoast-province.csv",Region2Region.region2region("G:/DATASET/GEO/ivorycoast/ivorycoast_subpref.csv","NAME_2","NAME_1"));
			
			//AfroBarometer ab = new AfroBarometer("G:/DATASET/CENSUS/afrobarometer/afrobar-ivorycoast.csv");
			AfroBarometer ab = new AfroBarometer("G:/DATASET/CENSUS/afrobarometer/afrobar-ivorycoast.csv",Region2Region.region2region("G:/DATASET/GEO/ivorycoast/ivorycoast_subpref.csv","NAME_2","NAME_1"));
			
			socioeconomicV.put("ophi", ophi.getDepriv());
			socioeconomicV.put("assoc", ab.proportion("Q19B", new String[]{"Official Leader","Active Member"}));
			socioeconomicV.put("meeting", ab.proportion("Q20A", new String[]{"Yes, once or twice","Yes, several times","Yes, often"}));
			socioeconomicV.put("join2raise", ab.proportion("Q20B", new String[]{"Yes, once or twice","Yes, several times","Yes, often"}));
			socioeconomicV.put("vote", ab.proportion("Q21", new String[]{"You voted in the elections"}));
		}
		if(country.equals(Senegal)) {
			//AfroBarometer ab = new AfroBarometer("G:/DATASET/CENSUS/afrobarometer/afrobar-senegal.csv",Region2Region.region2region("G:/DATASET/GEO/senegal/senegal_subpref.csv","NAME_1","NAME_2"));
			AfroBarometer ab = new AfroBarometer("G:/DATASET/CENSUS/afrobarometer/afrobar-senegal.csv");
			
			//Ophi ophi = new Ophi("G:/DATASET/CENSUS/ophi/ophi-senegal-regioni.csv",Region2Region.region2region("G:/DATASET/GEO/senegal/senegal_subpref.csv","NAME_1","NAME_2"));
			Ophi ophi = new Ophi("G:/DATASET/CENSUS/ophi/ophi-senegal-regioni.csv");
			
			socioeconomicV.put("ophi", ophi.getDepriv());
			socioeconomicV.put("assoc", ab.proportion("Q19B", new String[]{"Official Leader","Active Member"}));
			socioeconomicV.put("meeting", ab.proportion("Q20A", new String[]{"Yes, once or twice","Yes, several times","Yes, often"}));
			socioeconomicV.put("join2raise", ab.proportion("Q20B", new String[]{"Yes, once or twice","Yes, several times","Yes, often"}));
			socioeconomicV.put("vote", ab.proportion("Q21_SEN", new String[]{"You voted in the elections"}));
		}
		
		return socioeconomicV;
		
	}
	
	private static void writeCSV(List<String> ln, List<double[]> dist, String file) {
		try {
			PrintWriter out = new PrintWriter(new FileWriter(file));
			for(int i=0; i<ln.size();i++) 
				for(double x: dist.get(i))
					out.println(ln.get(i)+","+x);
			out.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
		
	}
}
