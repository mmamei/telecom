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

import otherdata.TIbigdatachallenge2015.Deprivation;
import otherdata.TIbigdatachallenge2015.IstatCensus2011;
import otherdata.TIbigdatachallenge2015.MEF_IRPEF;
import otherdata.TIbigdatachallenge2015.MEF_IRPEF_BLOG;
import otherdata.TIbigdatachallenge2015.SocialCapital;
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
	
	public static int DEMOGRAPHIC_RES_THRESHOLD = 100;
	
	public static void main(String[] args) throws Exception {
		
		String[] cities = SynchAnalysis.CITIES;
		
		
		
		List<String> ln = new ArrayList<String>();
		List<double[]> all = new ArrayList<double[]>();
		List<double[]> intra = new ArrayList<double[]>();
		List<double[]> inter = new ArrayList<double[]>();
		double[] intrainterdiff = new double[cities.length];
		
		for(int i=0; i<cities.length;i++) {
			String city = cities[i];
			System.out.println("\n\n*************************************************** START PROCESSING "+city.toUpperCase());
			DescriptiveStatistics[] all_intra_inter = run(city);
			ln.add(city);
			all.add(all_intra_inter[0].getValues());
			intra.add(all_intra_inter[1].getValues());
			if(all_intra_inter[2].getValues().length !=0) inter.add(all_intra_inter[2].getValues());
			else inter.add(all_intra_inter[1].getValues()); // if inter is empty (meaning just 1 clutser, usa intra instead
			//intrainterdiff[i] = all_intra_inter[1].getMean()-all_intra_inter[2].getMean();
			intrainterdiff[i] = all_intra_inter[1].getPercentile(50)-all_intra_inter[2].getPercentile(50);
			System.out.println("\n\n*************************************************** END PROCESSING "+city.toUpperCase());
		}
		
		RPlotter.drawBoxplot(all,ln,"comuni2012","all",Config.getInstance().base_folder+"/Images/boxplot-cluster-all.png",20,null);
		RPlotter.drawBoxplot(inter,ln,"comuni2012","inter",Config.getInstance().base_folder+"/Images/boxplot-cluster-inter.png",20,null);
		RPlotter.drawBoxplot(intra,ln,"comuni2012","intra",Config.getInstance().base_folder+"/Images/boxplot-cluster-intra.png",20,null);
		
		RPlotter.drawBar(ln.toArray(new String[cities.length]), intrainterdiff, "cities", "intra - inter", Config.getInstance().base_folder+"/Images/intra-inter.png", "");
		
		MEF_IRPEF mi = MEF_IRPEF.getInstance();
		Map<String,Double> avg_comuni = new HashMap<String,Double>();
		Map<String,Double> avg_province = new HashMap<String,Double>();
		Map<String,Double> avg_regioni = new HashMap<String,Double>();
		for(int i=0; i<ln.size();i++) {
			String key = (ln.get(i)+"-"+(SynchAnalysis.city2region.get(ln.get(i)).replaceAll("-", " ")).toLowerCase());
			String cid = MEF_IRPEF_BLOG.name2id().get(key);
			System.out.println(key+" ==> "+cid);
			avg_comuni.put(cid, avg(inter.get(i)));
			avg_province.put(ln.get(i).toUpperCase(), avg(inter.get(i)));
			avg_regioni.put(SynchAnalysis.city2region.get(ln.get(i)), avg(inter.get(i)));
			
		}
		Deprivation dp = Deprivation.getInstance();
		SynchAnalysis.plotCorrelation(avg_regioni,dp.getDepriv(),"R2","deprivation",Config.getInstance().base_folder+"/Images/clustering-deprivazione.png",null,true);
		
		Map<String,String> id2name = MEF_IRPEF_BLOG.id2name();
		
		SynchAnalysis.plotCorrelation(avg_comuni,mi.redditoPC(false),"R2","pro-capita income",Config.getInstance().base_folder+"/Images/clustering-redPC.png",id2name,true);
		SynchAnalysis.plotCorrelation(avg_comuni,mi.gini(false),"R2","Gini",Config.getInstance().base_folder+"/Images/clustering-gini.png",id2name,true);
		
		
		
		SocialCapital sc = SocialCapital.getInstance();
		SynchAnalysis.plotCorrelation(avg_province,sc.getAssoc(),"R2","assoc",Config.getInstance().base_folder+"/Images/clustering-assoc.png",null,true);
		SynchAnalysis.plotCorrelation(avg_province,sc.getReferendum(),"R2","referendum",Config.getInstance().base_folder+"/Images/clustering-referendum.png",null,true);
		SynchAnalysis.plotCorrelation(avg_province,sc.getBlood(),"R2","blood",Config.getInstance().base_folder+"/Images/clustering-blood.png",null,true);
		
		
		
	}
	
	
	public static DescriptiveStatistics[] run(String city) throws Exception {
		
		
		Instances datan = new DataSource(Config.getInstance().base_folder+"/TIC2015/cache/single/"+city+"-Demo-callsLM_"+city.substring(0,2).toUpperCase()+"_COMUNI2012-resident-CLEAN.arff").getDataSet();
		
		Instances dataz = new DataSource(Config.getInstance().base_folder+"/TIC2015/cache/single/"+city+"-Demo-callsLM_"+city.substring(0,2).toUpperCase()+"_COMUNI2012-resident-CLEANZ.arff").getDataSet();
		
		
		Instances fdataz = new Instances(dataz,0,0);
		for(int i=0; i<dataz.numInstances();i++)
			if(avg(cast2Array(datan.instance(i))) > DEMOGRAPHIC_RES_THRESHOLD) 
				fdataz.add(dataz.instance(i));
		

		
		Remove rm = new Remove();
		rm.setAttributeIndices("1");  // remove 1st attribute
		
		XMeans actualClusterer = new XMeans();
		actualClusterer.setDistanceF(new R2Distance());
		actualClusterer.setMinNumClusters(4);
		actualClusterer.setMaxNumClusters(10);
		
		
		
		//HierarchicalClusterer actualClusterer = new HierarchicalClusterer();
		//actualClusterer.setOptions(new String[]{"-L","SINGLE"});
		//actualClusterer.setDistanceFunction(new R2Distance());
		
		FilteredClusterer clusterer = new FilteredClusterer();
		clusterer.setFilter(rm);
		
		clusterer.setClusterer(actualClusterer);
		
		clusterer.buildClusterer(dataz);
		
		
		ClusterEvaluation eval = new ClusterEvaluation();
		
		eval.setClusterer(clusterer);                                   // the cluster to evaluate
		eval.evaluateClusterer(dataz);                                	// data to evaluate the clusterer on
		
		
		
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
		
		
		DescriptiveStatistics[] per_region_stat = new DescriptiveStatistics[fdataz.numInstances()];
		for(int i=0; i<per_region_stat.length;i++)
			per_region_stat[i] = new DescriptiveStatistics();
		 
		
		R2Distance d = new R2Distance();
		for(int i=0; i<fdataz.numInstances();i++){
			
			//System.out.print(data.instance(i).stringValue(0)+" ==> ");
			
			for(int j=0;j<fdataz.numInstances();j++) {
				
				if(i==j) continue;
	
				//if(data.instance(i).stringValue(0).equals("85008") && data.instance(j).stringValue(0).equals("87040"))
				//	System.out.println("here");
				
				
				
				
				double r2 = d.r2(fdataz.instance(i), fdataz.instance(j));
				//System.out.println(data.instance(i).stringValue(0)+" VS. "+data.instance(j).stringValue(0)+" = "+r2);
				
				
				//if(data.instance(i).stringValue(0).equals("36039"))
				//	System.err.println(data.instance(j).stringValue(0)+" = "+r2);
				
				per_region_stat[i].addValue(r2);
				all.addValue(r2);
				if(assignments[i] == assignments[j]) {
					intra.addValue(r2);
					per_intra_cluster_stat[(int)assignments[i]].addValue(r2);
				}
				else inter.addValue(r2);
			}
			
			//System.out.println(per_region_stat[i].getMean());
			
		
		}
		
		DecimalFormat F = new DecimalFormat("#.##",DecimalFormatSymbols.getInstance(Locale.US));
		Map<String,String> desc = new HashMap<String,String>();
		for(int i=0; i<fdataz.numInstances();i++){
			String rname = fdataz.instance(i).stringValue(0);
			int cluster = (int)assignments[i];
			DescriptiveStatistics intra_cluster = per_intra_cluster_stat[cluster];
			DescriptiveStatistics rstat = per_region_stat[i];
			desc.put(rname, "<b>Cluster = </b>"+cluster+"<br><b>Avg. R2 Intra Cluster = </b>"+F.format(intra_cluster.getMean())+"<br><b>Avg. R2 in Region = </b>"+F.format(rstat.getMean()));
		}
		
		
		drawClusterKML(city,assignments,dataz,desc);
		
		
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
	
	public static double[] cast2Array(Instance x) {
		double[] v = new double[x.numAttributes()-1];
		for(int i=1;i<v.length;i++)
			v[i-1] = x.value(i);
		return v;
	}
	
	public static double avg(double[] v) {
		double sum = 0;
		for(double x:v)
			sum+=x;
		return sum/v.length;
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

public static int DEMOGRAPHIC_RES_THRESHOLD = 100;
public static final int TIME_WINDOW = -1;

public double r2(Instance first, Instance second) {
	
	double[] x = SynchClustering.cast2Array(first);
	double[] y = SynchClustering.cast2Array(second);
	
	if(TIME_WINDOW == -1) return StatsUtils.r2(x,y);
	
	
	double[] reduced1 = new double[TIME_WINDOW];
	double[] reduced2 = new double[TIME_WINDOW];
	
	DescriptiveStatistics stat = new DescriptiveStatistics();
	
	for(int i=0; i<x.length - TIME_WINDOW; i=i+TIME_WINDOW) {
		
		System.arraycopy(x, i, reduced1, 0, reduced1.length);
		System.arraycopy(y, i, reduced2, 0, reduced2.length);
		stat.addValue(StatsUtils.r2(reduced1,reduced2));
	}
	
	//return stat.getPercentile(50);
	return stat.getMean();
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





