package cdraggregated;

import java.io.File;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import region.RegionMap;
import utils.Config;
import utils.CopyAndSerializationUtils;
import utils.StatsUtils;
import visual.kml.KMLColorMap;
import visual.r.RPlotter;
import weka.clusterers.ClusterEvaluation;
import weka.clusterers.FilteredClusterer;
import weka.clusterers.XMeans;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.NormalizableDistance;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.unsupervised.attribute.Remove;

public class SynchClustering {
	
	
	public static void main(String[] args) throws Exception {
		//String[] city = new String[]{"torino","milano","venezia","roma","napoli","bari","palermo"};
		
		String[] cities = new String[]{
				"caltanissetta",
				"siracusa",
				"benevento",
				//"palermo",
				"campobasso",
				//"napoli",
				"asti",
				//"bari",
				"ravenna",
				"ferrara",
				//"venezia",
				//"torino",
				"modena",
				//"roma",
				"siena",
				//"milano"
		};
		
		
		
		List<String> ln = new ArrayList<String>();
		List<double[]> all = new ArrayList<double[]>();
		List<double[]> intra = new ArrayList<double[]>();
		List<double[]> inter = new ArrayList<double[]>();
		for(String city: cities) {
			System.out.println("\n\n*************************************************** START PROCESSING "+city.toUpperCase());
			DescriptiveStatistics[] all_intra_inter = run(city);
			ln.add(city);
			all.add(all_intra_inter[0].getValues());
			intra.add(all_intra_inter[1].getValues());
			inter.add(all_intra_inter[2].getValues());
			System.out.println("\n\n*************************************************** END PROCESSING "+city.toUpperCase());
		}
		
		RPlotter.drawBoxplot(all,ln,"comuni2012","all",Config.getInstance().base_folder+"/Images/boxplot-cluster-all.png",20,null);
		RPlotter.drawBoxplot(inter,ln,"comuni2012","inter",Config.getInstance().base_folder+"/Images/boxplot-cluster-inter.png",20,null);
		RPlotter.drawBoxplot(intra,ln,"comuni2012","intra",Config.getInstance().base_folder+"/Images/boxplot-cluster-intra.png",20,null);
		
	}
	
	
	public static DescriptiveStatistics[] run(String city) throws Exception {
		
		
		String arff = Config.getInstance().base_folder+"/TIC2015/cache/single/"+city+"-Demo-callsLM_"+city.substring(0,2).toUpperCase()+"_COMUNI2012-resident-CLEAN.arff";
		DataSource source = new DataSource(arff);
		Instances data = source.getDataSet();
	
		
		Remove rm = new Remove();
		rm.setAttributeIndices("1");  // remove 1st attribute
		
		XMeans actualClusterer = new XMeans();
		actualClusterer.setDistanceF(new R2Distance());
		actualClusterer.setMinNumClusters(1);
		actualClusterer.setMaxNumClusters(10);
		
		//EM actualClusterer = new EM();
		
		
		//HierarchicalClusterer actualClusterer = new HierarchicalClusterer();
		//actualClusterer.setOptions(new String[]{"-L","MEAN"});
		//actualClusterer.setDistanceFunction(new R2Distance());
		
		FilteredClusterer clusterer = new FilteredClusterer();
		clusterer.setFilter(rm);
		
		clusterer.setClusterer(actualClusterer);
		
		clusterer.buildClusterer(data);
		
		
		ClusterEvaluation eval = new ClusterEvaluation();
		
		eval.setClusterer(clusterer);                                   // the cluster to evaluate
		eval.evaluateClusterer(data);                                	// data to evaluate the clusterer on
		
		
		
		double[] assignments = eval.getClusterAssignments();
		//for(int i=0; i<assignments.length;i++)
		//	System.out.println(assignments[i]+" "+data.instance(i).stringValue(0));
		
		
		System.out.println(eval.clusterResultsToString());
		//System.out.println(eval.getLogLikelihood());
		
		
		DescriptiveStatistics all = new DescriptiveStatistics();
		DescriptiveStatistics intra = new DescriptiveStatistics();
		DescriptiveStatistics inter = new DescriptiveStatistics();
		
		DescriptiveStatistics[] per_intra_cluster_stat = new DescriptiveStatistics[eval.getNumClusters()];
		for(int i=0; i<per_intra_cluster_stat.length;i++)
			per_intra_cluster_stat[i] = new DescriptiveStatistics();
		
		
		DescriptiveStatistics[] per_region_stat = new DescriptiveStatistics[data.numInstances()];
		for(int i=0; i<per_region_stat.length;i++)
			per_region_stat[i] = new DescriptiveStatistics();
		 
		
		R2Distance d = new R2Distance();
		for(int i=0; i<data.numInstances();i++){
			
			for(int j=0;j<data.numInstances();j++) {
				
				if(i==j) continue;
	
				//if(data.instance(i).stringValue(0).equals("85008") && data.instance(j).stringValue(0).equals("87040"))
				//	System.out.println("here");
				
				double r2 = d.r2(data.instance(i), data.instance(j));
				//System.out.println(data.instance(i).stringValue(0)+" VS. "+data.instance(j).stringValue(0)+" = "+r2);
				
				per_region_stat[i].addValue(r2);
				all.addValue(r2);
				if(assignments[i] == assignments[j]) {
					intra.addValue(r2);
					per_intra_cluster_stat[(int)assignments[i]].addValue(r2);
				}
				else inter.addValue(r2);
			}
		
		}
		
		DecimalFormat F = new DecimalFormat("#.##",DecimalFormatSymbols.getInstance(Locale.US));
		Map<String,String> desc = new HashMap<String,String>();
		for(int i=0; i<data.numInstances();i++){
			String rname = data.instance(i).stringValue(0);
			int cluster = (int)assignments[i];
			DescriptiveStatistics intra_cluster = per_intra_cluster_stat[cluster];
			DescriptiveStatistics rstat = per_region_stat[i];
			desc.put(rname, "<b>Cluster = </b>"+cluster+"<br><b>Avg. R2 Intra Cluster = </b>"+F.format(intra_cluster.getMean())+"<br><b>Avg. R2 in Region = </b>"+F.format(rstat.getMean()));
		}
		
		
		drawClusterKML(city,assignments,data,desc);
		
		
		//System.out.println("ALL "+all.getMean());
		//System.out.println("INTRA "+intra.getMean());
		//System.out.println("INTER "+inter.getMean());
		return new DescriptiveStatistics[]{all,intra,inter};
	}
	
	
	public static void drawClusterKML(String city,double[] assignments,Instances data,Map<String,String> desc) {
		
		
		String file = Config.getInstance().base_folder+"/TIC2015/"+city+"_cluster.kml";
		
		Map<String,Integer> assign = new HashMap<String,Integer>();
		for(int i=0; i<assignments.length;i++)
			assign.put(data.instance(i).stringValue(0), (int)assignments[i]);
		
		
		
		RegionMap rm = (RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/tic-comuni2012-"+city+".ser"));
		rm.setName(city+"_cluster");
		try {
			if(desc == null)
				KMLColorMap.drawColorMap(file, assign, rm, "Cluster");
			else 
				KMLColorMap.drawColorMap(file, assign, rm, desc);
		}catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	
	
	
}



class R2Distance extends NormalizableDistance {


public R2Distance() {
  super();
}


public R2Distance(Instances data) {
  super(data);
}



/**
 * Calculates the distance between two instances.
 * 
 * @param first 	the first instance
 * @param second 	the second instance
 * @return 		the distance between the two given instances
 */
public double distance(Instance first, Instance second) {
	return 1.0-r2(first,second);
}


public static final int TIME_WINDOW = -1;

public double r2(Instance first, Instance second) {
	
	double[] x = cast2Array(first);
	double[] y = cast2Array(second);
	
	
	
	if(TIME_WINDOW == -1) return StatsUtils.r2(x,y);
	
	
	double[] reduced1 = new double[TIME_WINDOW];
	double[] reduced2 = new double[TIME_WINDOW];
	
	DescriptiveStatistics stat = new DescriptiveStatistics();
	
	for(int i=0; i<x.length - TIME_WINDOW; i=i+TIME_WINDOW) {
		
		System.arraycopy(x, i, reduced1, 0, reduced1.length);
		System.arraycopy(y, i, reduced2, 0, reduced2.length);
		stat.addValue(StatsUtils.r2(reduced1,reduced2));
	}
	
	return stat.getPercentile(50);
}


private double[] cast2Array(Instance x) {
	double[] v = new double[x.numAttributes()-1];
	for(int i=1;i<v.length;i++)
		v[i-1] = x.value(i);
	return v;
}



@Override
public String getRevision() {
	// TODO Auto-generated method stub
	return null;
}


@Override
public String globalInfo() {
	// TODO Auto-generated method stub
	return null;
}


@Override
protected double updateDistance(double arg0, double arg1) {
	// TODO Auto-generated method stub
	return 0;
}

}





