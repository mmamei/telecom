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

import cdraggregated.SynchAnalysis.Feature;
import otherdata.TIbigdatachallenge2015.Deprivation;
import otherdata.TIbigdatachallenge2015.IstatCensus2011;
import otherdata.TIbigdatachallenge2015.MEF_IRPEF;
import otherdata.TIbigdatachallenge2015.MEF_IRPEF_BLOG;
import otherdata.TIbigdatachallenge2015.SocialCapital;
import region.RegionMap;
import utils.AddMap;
import utils.Config;
import utils.CopyAndSerializationUtils;
import visual.kml.KMLColorMap;
import visual.r.RPlotter;
import visual.text.TextPlotter;
import weka.clusterers.ClusterEvaluation;
import weka.clusterers.Clusterer;
import weka.clusterers.DBSCAN;
import weka.clusterers.FilteredClusterer;
import weka.clusterers.XMeans;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.NormalizableDistance;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.unsupervised.attribute.PrincipalComponents;
import weka.filters.unsupervised.attribute.Remove;

public class SynchClustering {
	
	public static boolean QUIET = true;
	
	public static boolean USE_RELATIVE_TH = true;
	
	public static int DEMOGRAPHIC_RES_THRESHOLD = 200;
	
	private enum Cluster {XMEANS,DBSCAN};
	public static Cluster USEC = Cluster.DBSCAN;
	
	public static boolean PCA = true;
	
	public static void main(String[] args) throws Exception {
		
		
		/// RUN ALL
		/*
		for(Cluster usec: Cluster.values()) {
			USEC = usec;
			for(Feature f: SynchAnalysis.Feature.values()) {
				SynchAnalysis.USEF = f;
				for (boolean th: new boolean[]{true,false}) {
					USE_RELATIVE_TH = th;
					for (boolean pca: new boolean[]{true,false}) {
						PCA = pca;
						for(int tw: new int[]{-1,24}) {
							SynchAnalysis.TIME_WINDOW = tw;
							System.err.println(USEC+" "+SynchAnalysis.USEF+" REL_TH = "+USE_RELATIVE_TH+" PCA = "+PCA+" TW = "+SynchAnalysis.TIME_WINDOW);
							go();
						}
					}
				}
			}
		}
		*/
		
		/// RUN SINGLE
		USEC = Cluster.DBSCAN;
		PCA = true;
		SynchAnalysis.USEF = SynchAnalysis.Feature.I;
		USE_RELATIVE_TH = false;
		SynchAnalysis.TIME_WINDOW = 24;
		go();
		
		
	}
	
	static String STRINGF = "F";
	public static void go() throws Exception {
		
		
		if(SynchAnalysis.USEF.equals(SynchAnalysis.Feature.I)) STRINGF = "I";
		if(SynchAnalysis.USEF.equals(SynchAnalysis.Feature.RSQ)) STRINGF = "R2";
		if(SynchAnalysis.USEF.equals(SynchAnalysis.Feature.EU)) STRINGF = "EU";
		
		String[] cities = SynchAnalysis.CITIES;
		
		
		
		List<String> ln = new ArrayList<String>();
		List<double[]> all = new ArrayList<double[]>();
		List<double[]> intra = new ArrayList<double[]>();
		List<double[]> inter = new ArrayList<double[]>();
		double[] intrainterdiff = new double[cities.length];
		
		
		List<String> bigCities = new ArrayList<String>();
		bigCities.add("napoli");bigCities.add("bari");bigCities.add("palermo");bigCities.add("roma");bigCities.add("venezia");bigCities.add("torino");bigCities.add("milano");
		
		for(int i=0; i<cities.length;i++) {
			String city = cities[i];
			
			
			//if(city.equals("roma") || city.equals("milano")) DEMOGRAPHIC_RES_THRESHOLD = 0;
			if(bigCities.contains(city)) DEMOGRAPHIC_RES_THRESHOLD = 200;
			else DEMOGRAPHIC_RES_THRESHOLD = 100;
			
			
			//DEMOGRAPHIC_RES_THRESHOLD = SynchAnalysis.CITY_POP.get(city) / 1000;
			
			
			if(!QUIET) System.out.println("\n\n*************************************************** START PROCESSING "+city.toUpperCase());
			DescriptiveStatistics[] all_intra_inter = run(city);
			ln.add(city);
			all.add(all_intra_inter[0].getValues());
			intra.add(all_intra_inter[1].getValues());
			if(all_intra_inter[2].getValues().length !=0) inter.add(all_intra_inter[2].getValues());
			else inter.add(all_intra_inter[1].getValues()); // if inter is empty (meaning just 1 clutser, usa intra instead
			//intrainterdiff[i] = all_intra_inter[1].getMean()-all_intra_inter[2].getMean();
			intrainterdiff[i] = all_intra_inter[1].getPercentile(50)-all_intra_inter[2].getPercentile(50);
			if(!QUIET) System.out.println("\n\n*************************************************** END PROCESSING "+city.toUpperCase());
		}
		
		
		String dir = Config.getInstance().paper_folder+"/img/batch/"+USEC+(PCA?"wPCA":"")+"-"+(USE_RELATIVE_TH?"REL-TH":"ABS-TH")+"-"+SynchAnalysis.USEF+""+SynchAnalysis.TIME_WINDOW;
		new File(dir).mkdirs();
		
		RPlotter.VIEW = false;
		
		RPlotter.drawBoxplot(all,ln,"comuni2012","all-"+STRINGF,dir+"/boxplot-cluster-all.png",20,null);
		RPlotter.drawBoxplot(inter,ln,"comuni2012","inter-"+STRINGF,dir+"/boxplot-cluster-inter.png",20,null);
		RPlotter.drawBoxplot(intra,ln,"comuni2012","intra-"+STRINGF,dir+"/boxplot-cluster-intra.png",20,null);
		
		//RPlotter.drawBar(ln.toArray(new String[cities.length]), intrainterdiff, "cities", "intra - inter", dir+"/intra-inter.png", "");
		
		MEF_IRPEF mi = MEF_IRPEF.getInstance();
		Map<String,Double> avg_comuni = new HashMap<String,Double>();
		Map<String,Double> avg_province = new HashMap<String,Double>();
		Map<String,Double> avg_regioni = new HashMap<String,Double>();
		for(int i=0; i<ln.size();i++) {
			String key = (ln.get(i)+"-"+(SynchAnalysis.city2region.get(ln.get(i)).replaceAll("-", " ")).toLowerCase());
			//System.out.println("*** "+key);
			String cid = MEF_IRPEF_BLOG.name2id().get(key);
			//System.out.println(key+" ==> "+cid);
			avg_comuni.put(cid, avg(inter.get(i)));
			avg_province.put(ln.get(i).toUpperCase(), avg(inter.get(i)));
			avg_regioni.put(SynchAnalysis.city2region.get(ln.get(i)), avg(inter.get(i)));
			
		}
		Deprivation dp = Deprivation.getInstance();
		SynchAnalysis.plotCorrelation(avg_regioni,dp.getDepriv(),SynchAnalysis.USE_FEATURE,"deprivation",dir+"/clustering-deprivazione.png",null,true);
		
		Map<String,String> id2name = MEF_IRPEF_BLOG.id2name();
		
		SynchAnalysis.plotCorrelation(avg_comuni,mi.redditoPC(false),SynchAnalysis.USE_FEATURE,"pro-capita income",dir+"/clustering-redPC.png",id2name,true);
		//SynchAnalysis.plotCorrelation(avg_comuni,mi.gini(false),SynchAnalysis.USE_FEATURE,"Gini",dir+"/clustering-gini.png",id2name,true);
		
		
		
		SocialCapital sc = SocialCapital.getInstance();
		SynchAnalysis.plotCorrelation(avg_province,sc.getAssoc(),SynchAnalysis.USE_FEATURE,"assoc",dir+"/clustering-assoc.png",null,true);
		SynchAnalysis.plotCorrelation(avg_province,sc.getReferendum(),SynchAnalysis.USE_FEATURE,"referendum",dir+"/clustering-referendum.png",null,true);
		SynchAnalysis.plotCorrelation(avg_province,sc.getBlood(),SynchAnalysis.USE_FEATURE,"blood",dir+"/clustering-blood.png",null,true);
		
		Map<String,Object> tm = new HashMap<String,Object>();
		//System.out.println(dir.toString());
		tm.put("dir", dir.toString().substring(dir.toString().indexOf("img")));
		tm.put("clustering", USEC);
		tm.put("distance", SynchAnalysis.USEF);
		tm.put("time_window", SynchAnalysis.TIME_WINDOW);
		tm.put("pca",PCA);
		tm.put("threshold",(USE_RELATIVE_TH?"relative":"absolute"));
		TextPlotter.getInstance().run(tm,"src/cdraggregated/SynchClustering.ftl", dir+"/text.tex");		
		
		
		
	}
	
	static AddMap ISTAT = IstatCensus2011.getInstance().computeDensity(0, false, false);
	public static DescriptiveStatistics[] run(String city) throws Exception {
		
		
		Instances datan = new DataSource(Config.getInstance().base_folder+"/TIC2015/cache/single/"+city+"-Demo-callsLM_"+city.substring(0,2).toUpperCase()+"_COMUNI2012-resident-CLEAN.arff").getDataSet();
		
		Instances dataz = new DataSource(Config.getInstance().base_folder+"/TIC2015/cache/single/"+city+"-Demo-callsLM_"+city.substring(0,2).toUpperCase()+"_COMUNI2012-resident-CLEANZ.arff").getDataSet();
		
		
		
		
		
		Instances fdataz = new Instances(dataz,0,0);
		for(int i=0; i<dataz.numInstances();i++) {
			Double d = ISTAT.get(dataz.instance(i).stringValue(0));
			double avg = avg(cast2Array(datan.instance(i)));
			//System.out.println(dataz.instance(i).stringValue(0)+" ==> "+d.intValue()+" VS. "+(int)avg);
			
			if((USE_RELATIVE_TH && avg/d > 0.1) || (!USE_RELATIVE_TH && avg > DEMOGRAPHIC_RES_THRESHOLD)) 
				fdataz.add(dataz.instance(i));
		}
		
		
		
		
		

		
		
		
		Clusterer actualClusterer = null;
		
		if(USEC.equals(Cluster.XMEANS)) {
			XMeans xm = new XMeans();
			xm.setDistanceF(new CustomDistance());
			xm.setMinNumClusters(1);
			xm.setMaxNumClusters(10);
			actualClusterer = xm;
		}
		if(USEC.equals(Cluster.DBSCAN)){
			DBSCAN dbsc = new DBSCAN();
			dbsc.setMinPoints(0);
			dbsc.setEpsilon(1.5);
			actualClusterer = dbsc;
		}
		
		//HierarchicalClusterer actualClusterer = new HierarchicalClusterer();
		//actualClusterer.setOptions(new String[]{"-L","SINGLE"});
		//actualClusterer.setDistanceFunction(new R2Distance());
		
		
		
		
		FilteredClusterer clusterer2 = new FilteredClusterer();
		clusterer2.setFilter(new PrincipalComponents());
		clusterer2.setClusterer(actualClusterer);
		
		Remove rm = new Remove();
		rm.setAttributeIndices("1");  // remove 1st attribute
		FilteredClusterer clusterer = new FilteredClusterer();
		clusterer.setFilter(rm);
		if(PCA) clusterer.setClusterer(clusterer2);
		else clusterer.setClusterer(actualClusterer);
		
		clusterer.buildClusterer(dataz);
		
		
		ClusterEvaluation eval = new ClusterEvaluation();
		
		eval.setClusterer(clusterer);                                   // the cluster to evaluate
		eval.evaluateClusterer(dataz);                                	// data to evaluate the clusterer on
		
		
		
		double[] assignments = eval.getClusterAssignments();
		//for(int i=0; i<assignments.length;i++)
		//	System.out.println(assignments[i]+" "+data.instance(i).stringValue(0));
		if(!QUIET) System.out.println("NUM INSTANCES = "+dataz.numInstances()+", NUM CLUSTERS = "+eval.getNumClusters());
		
		//System.out.println(eval.clusterResultsToString());
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
		 
		
		CustomDistance d = new CustomDistance();
		for(int i=0; i<fdataz.numInstances();i++){
			
			//System.out.print(data.instance(i).stringValue(0)+" ==> ");
			
			for(int j=0;j<fdataz.numInstances();j++) {
				
				if(i==j) continue;
	
				//if(data.instance(i).stringValue(0).equals("85008") && data.instance(j).stringValue(0).equals("87040"))
				//	System.out.println("here");
							
				double s = d.sim(fdataz.instance(i), fdataz.instance(j));
				//System.out.println(data.instance(i).stringValue(0)+" VS. "+data.instance(j).stringValue(0)+" = "+r2);
				
				
				//if(data.instance(i).stringValue(0).equals("36039"))
				//	System.err.println(data.instance(j).stringValue(0)+" = "+r2);
				
				per_region_stat[i].addValue(s);
				all.addValue(s);
				if(assignments[i] == assignments[j]) {
					intra.addValue(s);
					per_intra_cluster_stat[(int)assignments[i]].addValue(s);
				}
				else inter.addValue(s);
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



class CustomDistance extends NormalizableDistance {


public CustomDistance() {
  super();
}


public CustomDistance(Instances data) {
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
	return 1.0-sim(first,second);
}




// similarity as opposed to distance
public double sim(Instance first, Instance second) {
	
	double[] x = SynchClustering.cast2Array(first);
	double[] y = SynchClustering.cast2Array(second);
	
	if(SynchAnalysis.TIME_WINDOW == -1) return SynchAnalysis.reallyComputeFeature(x,y);
	
	
	double[] reduced1 = new double[SynchAnalysis.TIME_WINDOW];
	double[] reduced2 = new double[SynchAnalysis.TIME_WINDOW];
	
	DescriptiveStatistics stat = new DescriptiveStatistics();
	
	for(int i=0; i<x.length - SynchAnalysis.TIME_WINDOW; i=i+SynchAnalysis.TIME_WINDOW) {
		
		System.arraycopy(x, i, reduced1, 0, reduced1.length);
		System.arraycopy(y, i, reduced2, 0, reduced2.length);
		stat.addValue(SynchAnalysis.reallyComputeFeature(reduced1,reduced2));
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





