package cdraggregated.synch;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import cdraggregated.synch.SynchCompute.Feature;
import region.RegionMap;
import utils.Config;
import utils.CopyAndSerializationUtils;
import utils.ListMapArrayBasicUtils;
import visual.kml.KMLColorMap;

public class SynchClustering extends Thread {
	
	public static void main(String [] args) throws Exception {
		runExperiment(TimeDensityTIM.UseResidentType.ALL,24,Feature.I);
	}
	
	
	
	
	public static void runExperiment( TimeDensityTIM.UseResidentType resType, int time_window, SynchCompute.Feature use_feature) throws Exception {
		
		System.out.println("RUNNING EXPERIMENT WITH: RESTYPE = "+resType+", TIME WINDOW = "+time_window+", FEATURE = "+use_feature);
		
		TimeDensityTIM.res_type = resType;
		SynchCompute.TIME_WINDOW = time_window;
		SynchCompute.USEF = use_feature;
		
		List<String> cities = new ArrayList<>();
		//cities.add("napoli");
		//cities.add("bari");
		//cities.add("caltanissetta");
		//cities.add("siracusa");
		//cities.add("benevento");
		//cities.add("palermo");
		//cities.add("campobasso");
		cities.add("roma");
		//cities.add("siena");
		//cities.add("ravenna");
		//cities.add("ferrara");
		//cities.add("modena");
		//cities.add("venezia");
		//cities.add("torino");
		//cities.add("asti");
		//cities.add("milano");
		
		
		
		List<StatsCollection> city_stats = new ArrayList<>();
		List<SynchClustering> executors = new ArrayList<>();
		for(String city: cities) 
			executors.add(new SynchClustering(city));
		for(SynchClustering e: executors)
			e.start();
		for(SynchClustering e: executors)
			e.join();
		for(int i=0; i<executors.size();i++)
			city_stats.add(executors.get(i).getStats());
		
		Evaluation.evaluate(cities, city_stats);
		
		
		System.out.println("Done");
	}
	
	
	private String city;
	private StatsCollection result;
	public SynchClustering(String city) {
		this.city = city;
	}
	
	public void run() {
		
		System.out.println("Processing "+city+"....");
		
		DescriptiveStatistics all = new DescriptiveStatistics();
		DescriptiveStatistics intra = new DescriptiveStatistics();
		DescriptiveStatistics inter = new DescriptiveStatistics();
		
		
		TimeDensity td = new TimeDensityTIM(city,Config.getInstance().dataset_folder+"/TI-CHALLENGE-2015/DEMOGRAPHIC/"+city+"/callsLM_"+city.substring(0,2).toUpperCase()+"_COMUNI2012");
		Map<String,Integer> assignments = KMLColorMap.toIntAssignments(td.getMapping((RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/tic-comuni2012-"+city+".ser"))));
		
		
		//count assignments to get the number of comuni
		Set<Integer> allassignments = new HashSet<>();
		for(Integer a: assignments.values())
			allassignments.add(a);
		int n_comuni = allassignments.size();
		
		DescriptiveStatistics[] intraXcomune = new DescriptiveStatistics[n_comuni];
		DescriptiveStatistics[] interXcomune = new DescriptiveStatistics[n_comuni];
		for(int i=0; i<intraXcomune.length;i++) {
			intraXcomune[i] = new DescriptiveStatistics();
			interXcomune[i] = new DescriptiveStatistics();
		}
		
		List<String> k = td.getKeys();
		for(int i=0; i<k.size();i++)
		for(int j=i+1;j<k.size();j++) {
			String k_i = k.get(i);
			String k_j = k.get(j);
			Integer assignment_i = assignments.get(k_i);
			Integer assignment_j = assignments.get(k_j);
			
			if(assignment_i == null) {
				//System.out.println("Missing assignment for "+city+" "+k_i);
				continue;
			}
			if(assignment_j == null) {
				//System.out.println("Missing assignment for "+city+" "+k_j);
				continue;
			}
			
			//double s = SynchCompute.reallyComputeFeature(td.getz(k_i),td.getz(k_j));
			List<Double> ls = SynchCompute.computeFeature(td.get(k_i),td.get(k_j));
			double s = ListMapArrayBasicUtils.avg(ls);
			
			//for(double s:ls) {
				all.addValue(s);
				if(assignment_i == assignment_j) {
					intra.addValue(s);
					intraXcomune[assignment_i].addValue(s);
				}
				else {
					inter.addValue(s);
					interXcomune[assignment_i].addValue(s);
					interXcomune[assignment_j].addValue(s);
				}
			//}
		}
		
		
		result = new StatsCollection(all,intra,inter,intraXcomune,interXcomune);
	}
	
	public StatsCollection getStats() {
		return result;
	}
}
