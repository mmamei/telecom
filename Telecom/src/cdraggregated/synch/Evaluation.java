package cdraggregated.synch;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import otherdata.TIbigdatachallenge2015.Deprivation;
import otherdata.TIbigdatachallenge2015.MEF_IRPEF;
import otherdata.TIbigdatachallenge2015.SocialCapital;
import region.RegionMap;
import utils.AddMap;
import utils.Config;
import utils.CopyAndSerializationUtils;
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
		//cities_whose_map_we_draw.add("milano");
	}
	
	static final String YLAB = SynchCompute.USE_FEATURE;
	
	static String YLAB_SIMPLE = "Synch";
	static {
		if(SynchCompute.USEF.equals(SynchCompute.Feature.I)) YLAB_SIMPLE = "I";
		if(SynchCompute.USEF.equals(SynchCompute.Feature.RSQ)) YLAB_SIMPLE = "R2";
		if(SynchCompute.USEF.equals(SynchCompute.Feature.EU)) YLAB_SIMPLE = "EU";
	}
	
	public static void evaluate(List<String> cities, Country country, List<StatsCollection> city_stats) {
		String dir =SynchCompute.getDir();
		new File(dir).mkdirs();
		System.err.println(dir);
		writeFeatureFile(cities,city_stats,dir);
		drawMap(cities,country,city_stats,dir);
		drawBoxPlots(cities,city_stats,dir);
		//drawCorrelationsOLD(cities,city_stats,dir);
	}
	
	
	
	private static void drawMap(List<String> cities,  Country country, List<StatsCollection> city_stats, String dir) {
		for(int i=0; i<cities.size();i++) {
			String city = cities.get(i);
			
			if(!cities_whose_map_we_draw.contains(city)) continue;
			
			
			StatsCollection stats = city_stats.get(i);
			RegionMap rm = (RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/tic-comuni2012-"+city+".ser"));
			TimeDensity td = TimeDensityFactory.getInstance(city,country);
			Map<String,String> mapping = td.getMapping(rm); // 2026_3_0_3_1=63073, 
			Map<String,Integer> assignments = KMLColorMap.toIntAssignments(mapping); // 2026_3_0_3_1=0,
			
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
			
			reallyDraw(dir,city,"within",rm,comune2assignement,cellXComuneCount,stats.intraXcomune);
			reallyDraw(dir,city,"between",rm,comune2assignement,cellXComuneCount,stats.interXcomune);
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
			kml_descriptions.put(comune, "num_cell="+cellXComuneCount.get(comune)+",mean="+mean+", median="+ds.getPercentile(50));
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
	
	
	
	
	
	private static void drawBoxPlots(List<String> cities,  List<StatsCollection> city_stats,String dir) {
		List<double[]> intra = new ArrayList<double[]>();
		List<double[]> inter = new ArrayList<double[]>();	
		for(StatsCollection sc: city_stats) {
			intra.add(sc.intra.getValues());
			inter.add(sc.inter.getValues());
		}
		writeCSV(cities,intra,dir+"/within.csv");
		writeCSV(cities,inter,dir+"/between.csv");
		RPlotterWithinBetweenSynch.drawBoxplot(dir+"/within.csv", dir+"/between.csv", "", YLAB_SIMPLE, dir+"/boxplot.png");
	}
	
	/*
	private static void drawCorrelationsOLD(List<String> cities,  List<StatsCollection> city_stats,String dir) {
		
		
		MEF_IRPEF mi = MEF_IRPEF.getInstance();
		Map<String,Double> avg_comuni = new HashMap<String,Double>();
		Map<String,Double> avg_province = new HashMap<String,Double>();
		AddMap avg_regioni = new AddMap();
		
		
		for(int i=0; i<cities.size();i++) { // [napoli, bari, caltanissetta, siracusa, benevento, palermo, campobasso, roma, siena, ravenna, ferrara, venezia, torino, asti, milano]
			String key = (cities.get(i)+"-"+(TableNames.city2region.get(cities.get(i)).replaceAll("-", " ")).toLowerCase());
			//System.out.println("*** "+key);
			String cid = MEF_IRPEF_BLOG.name2id().get(key); // comune id (es. 63049 = Napoli)
			//System.out.println(key+" ==> "+cid);
			
			double avg_inter = city_stats.get(i)[2].getMean();
			avg_comuni.put(cid, avg_inter); 
			avg_province.put(cities.get(i).toUpperCase(), avg_inter);
			avg_regioni.add(TableNames.city2region.get(cities.get(i)), avg_inter);
		}
		
		
		avg_regioni.mean(); // there can be multiple data for each region
		
		Deprivation dp = Deprivation.getInstance();
		RPlotter.plotCorrelation(avg_regioni,dp.getDepriv(),YLAB,"deprivation",dir+"/clustering-deprivazione.png",null,true);
		
		Map<String,String> id2name = MEF_IRPEF_BLOG.id2name();
		
		RPlotter.plotCorrelation(avg_comuni,mi.redditoPC(false),YLAB,"per-capita income",dir+"/clustering-redPC.png",id2name,true);
		//SynchAnalysis.plotCorrelation(avg_comuni,mi.gini(false),SynchAnalysis.USE_FEATURE,"Gini",dir+"/clustering-gini.png",id2name,true);
		RPlotter.plotCorrelation(avg_province,mi.redditoPCProvince(),YLAB,"per-capita income prov",dir+"/clustering-redPCP.png",null,true);
		
	
		SocialCapital sc = SocialCapital.getInstance();
		RPlotter.plotCorrelation(avg_province,sc.getAssoc(),YLAB,"assoc",dir+"/clustering-assoc.png",null,true);
		RPlotter.plotCorrelation(avg_province,sc.getReferendum(),YLAB,"referendum",dir+"/clustering-referendum.png",null,true);
		RPlotter.plotCorrelation(avg_province,sc.getBlood(),YLAB,"blood",dir+"/clustering-blood.png",null,true);
		RPlotter.plotCorrelation(avg_province,sc.getSocCap(),YLAB,"soccap",dir+"/clustering-soccap.png",null,true);
		
	}
	*/
	
	static boolean MEAN = false; // false = MEDIAN
	
	private static void writeFeatureFile(List<String> cities,  List<StatsCollection> city_stats,String dir) {
		try {
			MEF_IRPEF mi = MEF_IRPEF.getInstance();
			SocialCapital sc = SocialCapital.getInstance();
			
			Map<String,Double> map_depriv = Deprivation.getInstance().getDepriv();
			Map<String,Double> map_rpc = mi.redditoPCProvince();
			Map<String,Double> map_blood = sc.getBlood();
			Map<String,Double> map_assoc = sc.getAssoc();
			Map<String,Double> map_referendum = sc.getReferendum();
			Map<String,Double> map_soccap = sc.getSocCap();
			
			
			
			PrintWriter out = new PrintWriter(new FileWriter(dir+"/features-data.csv"));
			out.println("city,regione,avg_intra,avg_inter,sd_intra,sd_inter,avg,sd,depriv,rpc,blood,assoc,referendum,soccap");
			
			//List<double[]> bond_bridge = computeBondingBridgingBruno(city_stats);
			
			for(int i=0; i<cities.size();i++) { // [napoli, bari, caltanissetta, siracusa, benevento, palermo, campobasso, roma, siena, ravenna, ferrara, venezia, torino, asti, milano]
				
				String provincia = cities.get(i).toUpperCase();
				String regione = TableNames.city2region.get(cities.get(i));
				
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
				
				double depriv = map_depriv.get(regione);
				double rpc = map_rpc.get(provincia);
				double blood = map_blood.get(provincia);
				double assoc = map_assoc.get(provincia);
				double referendum = map_referendum.get(provincia);
				Double soccap = map_soccap.get(provincia);
				
				String line = cities.get(i)+","+regione+","+avg_intra+","+avg_inter+","+sd_intra+","+sd_inter+","+avg+","+sd+","+depriv+","+rpc+","+blood+","+assoc+","+referendum+","+soccap;
				System.out.println(line);
				out.println(line);
			}
			out.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	/*
	// compute bonding and bridging features like Bruno proposed in 31-5-2016 mail
	private static List<double[]> computeBondingBridgingBruno(List<StatsCollection> city_stats) {
		List<double[]> bond_bridge = new ArrayList<>();
		for(StatsCollection stc: city_stats) {
			DescriptiveStatistics bond = new DescriptiveStatistics();
			DescriptiveStatistics bridge = new DescriptiveStatistics();
			for(int i=0;i<stc.intraXcomune.length;i++) {
				if(stc.intraXcomune[i].getN() > 0) {
					bond.addValue(stc.intraXcomune[i].getStandardDeviation());
					bridge.addValue(stc.intraXcomune[i].getMean());
				}
			}
			bond_bridge.add(new double[]{bond.getMean(),bridge.getStandardDeviation()});
		}
		return bond_bridge;
	}
	*/
	
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
