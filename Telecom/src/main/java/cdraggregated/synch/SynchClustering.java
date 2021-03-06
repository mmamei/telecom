package cdraggregated.synch;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import region.RegionMap;
import utils.Config;
import utils.CopyAndSerializationUtils;
import utils.ListMapArrayBasicUtils;
import visual.kml.KMLColorMap;
import cdraggregated.synch.SynchCompute.Feature;
import cdraggregated.synch.TableNames.Country;
import static cdraggregated.synch.TableNames.Country.*;
import cdraggregated.synch.timedensity.TimeDensity;
import cdraggregated.synch.timedensity.TimeDensityFactory;
import cdraggregated.synch.timedensity.TimeDensityTIM;

public class SynchClustering extends Thread {
	
	public static void main(String [] args) throws Exception {
		//runExperiment(Italy,TimeDensityTIM.UseResidentType.ALL,24,Feature.I,"2015-03-31:0:0:0","2015-04-30:23:59:59");
		
		runExperiment(Senegal,TimeDensityTIM.UseResidentType.ALL,24,Feature.I, "2013-04-01:0:0:0","2013-04-31:23:59:59");
		//runExperiment(Senegal,TimeDensityTIM.UseResidentType.ALL,24,Feature.I, "2013-06-01:0:0:0","2013-06-31:23:59:59");
		//runExperiment(Senegal,TimeDensityTIM.UseResidentType.ALL,24,Feature.I, "2013-10-01:0:0:0","2013-10-31:23:59:59");
		
		//runExperiment(IvoryCoast,TimeDensityTIM.UseResidentType.ALL,24,Feature.I,"2012-02-01:0:0:0","2012-02-28:23:59:59");
		//runExperiment(IvoryCoast,TimeDensityTIM.UseResidentType.ALL,24,Feature.I,"2012-03-01:0:0:0","2012-03-31:23:59:59");
		//runExperiment(IvoryCoast,TimeDensityTIM.UseResidentType.ALL,24,Feature.I,"2012-04-01:0:0:0","2012-04-28:23:59:59");
	}
	

	public static void runExperiment(Country country, TimeDensityTIM.UseResidentType resType, int time_window, SynchCompute.Feature use_feature, String startTime, String endTime) throws Exception {
		
		System.out.println("RUNNING EXPERIMENT WITH: RESTYPE = "+resType+", TIME WINDOW = "+time_window+", FEATURE = "+use_feature);
		
		TimeDensityTIM.res_type = resType;
		SynchCompute.TIME_WINDOW = time_window;
		SynchCompute.USEF = use_feature;
		
		List<String> cities = TableNames.getAvailableProvinces(country);
		//List<String> cities = new ArrayList<>(); cities.add(TableNames.getAvailableProvinces(country).get(0)); cities.add(TableNames.getAvailableProvinces(country).get(1));
		
		
		//cities.remove("Folon");
		//cities.remove("Bafing");
		//cities.remove("Worodougou");
		//cities.remove("Yamoussoukro");
		
		
		List<StatsCollection> city_stats = new ArrayList<>();
		List<SynchClustering> executors = new ArrayList<>();
		for(String city: cities) 
			executors.add(new SynchClustering(city,country, startTime, endTime));
		for(SynchClustering e: executors)
			e.start();
		for(SynchClustering e: executors)
			e.join();
		for(int i=0; i<executors.size();i++)
			city_stats.add(executors.get(i).getStats());
		
		Evaluation.evaluate(cities, country, startTime, endTime, city_stats);
		
		
		System.out.println("Done");
	}
	
	
	private Country country;
	private String city;
	private StatsCollection result;
	private String startTime;
	private String endTime;
	public SynchClustering(String city, Country country, String startTime, String endTime) {
		this.city = city;
		this.country = country;
		this.startTime = startTime;
		this.endTime = endTime;
	}
	
	public void run() {
		
		System.out.println("Processing "+city+"....");
		
		DescriptiveStatistics all = new DescriptiveStatistics();
		DescriptiveStatistics intra = new DescriptiveStatistics();
		DescriptiveStatistics inter = new DescriptiveStatistics();
		
		TimeDensity td = TimeDensityFactory.getInstance(city,country,startTime,endTime);
		
		
		RegionMap rm = TableNames.getRegionMap(city, country);
		
		Map<String,Integer> assignments = KMLColorMap.toIntAssignments(td.getMapping(rm));
		
		
		//count assignments to get the number of comuni
		Set<Integer> allassignments = new HashSet<>();
		for(Integer a: assignments.values())
			allassignments.add(a);
		int n_comuni = allassignments.size();
		
		System.out.println("Province "+city+" has "+n_comuni+" comuni with "+assignments.size()+" cells");
		
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
			List<Double> ls = SynchCompute.computeFeature(td.get(k_i),td.get(k_j),td.getTimeConverter());
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
