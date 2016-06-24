package cdraggregated;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
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
import utils.AddMap;
import utils.Config;
import utils.CopyAndSerializationUtils;
import utils.ListMapArrayBasicUtils;
import utils.Mail;
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
import cdraggregated.SynchAnalysis.Analysis;
import cdraggregated.SynchCompute.Feature;

public class SynchClustering {
	
	public static boolean QUIET = true;
	
	public static boolean USE_RELATIVE_TH = true;
	
	
	private enum Cluster {XMEANS,DBSCAN,ALLTOGETHER,ALLDISJOINT};
	public static Cluster USEC = Cluster.DBSCAN;
	
	public static boolean PCA = false;
	
	public static void main(String[] args) throws Exception {
		
		
		/// RUN ALL
		RPlotter.VIEW = false;
		for(Cluster usec: Cluster.values()) {
			USEC = usec;
			for(Feature f: SynchCompute.Feature.values()) {
				SynchCompute.USEF = f;
				for (boolean th: new boolean[]{true,false}) {
					USE_RELATIVE_TH = th;
					for (boolean pca: new boolean[]{true,false}) {
						PCA = pca;
						for(int tw: new int[]{-1,24}) {
							SynchCompute.TIME_WINDOW = tw;
							
							if(SynchCompute.USEF.equals(Feature.I)) SynchCompute.USE_FEATURE = (SynchAnalysis.TYPE.equals(Analysis.DEMOGRAPHIC_RES)) ? "expression(avg[i]~I~'('~res[r]~';'~res[i!=r]~')')" : "expression(I~'(res;!res)')";
							if(SynchCompute.USEF.equals(Feature.RSQ)) SynchCompute.USE_FEATURE = (SynchAnalysis.TYPE.equals(Analysis.DEMOGRAPHIC_RES)) ? "expression(avg[i]~bar(R)^{2}~'('~res[r]~','~res[i!=r]~')')" : "expression(bar(R)^{2}~'(res,!res)')";
							if(SynchCompute.USEF.equals(Feature.EU)) SynchCompute.USE_FEATURE = (SynchAnalysis.TYPE.equals(Analysis.DEMOGRAPHIC_RES)) ? "expression(avg[i]~EU~'('~res[r]~';'~res[i!=r]~')')" : "expression(EU~'(res;!res)')";
							
							
							System.err.println(USEC+" "+SynchCompute.USEF+" REL_TH = "+USE_RELATIVE_TH+" PCA = "+PCA+" TW = "+SynchCompute.TIME_WINDOW);
							go();
						}
					}
				}
			}
		}
		
		Mail.send("batch complete");
		
		
		/*
		/// RUN SINGLE
		
		USEC = Cluster.XMEANS;
		PCA = true;
		SynchCompute.USEF = SynchCompute.Feature.RSQ;
		
		if(SynchCompute.USEF.equals(Feature.I)) SynchCompute.USE_FEATURE = (SynchAnalysis.TYPE.equals(Analysis.DEMOGRAPHIC_RES)) ? "expression(avg[i]~I~'('~res[r]~';'~res[i!=r]~')')" : "expression(I~'(res;!res)')";
		if(SynchCompute.USEF.equals(Feature.RSQ)) SynchCompute.USE_FEATURE = (SynchAnalysis.TYPE.equals(Analysis.DEMOGRAPHIC_RES)) ? "expression(avg[i]~bar(R)^{2}~'('~res[r]~','~res[i!=r]~')')" : "expression(bar(R)^{2}~'(res,!res)')";
		if(SynchCompute.USEF.equals(Feature.EU)) SynchCompute.USE_FEATURE = (SynchAnalysis.TYPE.equals(Analysis.DEMOGRAPHIC_RES)) ? "expression(avg[i]~EU~'('~res[r]~';'~res[i!=r]~')')" : "expression(EU~'(res;!res)')";
		
		USE_RELATIVE_TH = true;
		SynchCompute.TIME_WINDOW = 24;
		go();
		*/
		
	}
	
	static String STRINGF = "F";
	
	static List<String> bigCities = new ArrayList<String>();
	static {
		bigCities.add("napoli");bigCities.add("bari");bigCities.add("palermo");bigCities.add("roma");bigCities.add("venezia");bigCities.add("torino");bigCities.add("milano");
	}
	
	public static void go() throws Exception {
		
		
		if(SynchCompute.USEF.equals(SynchCompute.Feature.I)) STRINGF = "I";
		if(SynchCompute.USEF.equals(SynchCompute.Feature.RSQ)) STRINGF = "R2";
		if(SynchCompute.USEF.equals(SynchCompute.Feature.EU)) STRINGF = "EU";
		
		String[] cities = SynchAnalysis.CITIES;
		
		List<String> ln = new ArrayList<String>();
		List<double[]> all = new ArrayList<double[]>();
		List<double[]> intra = new ArrayList<double[]>();
		List<double[]> inter = new ArrayList<double[]>();
		double[] intrainterdiff = new double[cities.length];
		
		
		
		for(int i=0; i<cities.length;i++) {
			String city = cities[i];
			
			if(!QUIET) System.out.println("\n\n*************************************************** START PROCESSING "+city.toUpperCase());
			DescriptiveStatistics[] all_intra_inter = run(city);
			ln.add(city);
			all.add(all_intra_inter[0].getValues());
			intra.add(all_intra_inter[1].getValues());
			inter.add(all_intra_inter[2].getValues());
			
			// This was a piece of code that in the case iter distribution is 0 (1 cluster) use itra distribution instead 
			//if(all_intra_inter[2].getValues().length !=0) inter.add(all_intra_inter[2].getValues());
			//else inter.add(all_intra_inter[1].getValues()); // if inter is empty (meaning just 1 clutser, usa intra instead
			
			//intrainterdiff[i] = all_intra_inter[1].getMean()-all_intra_inter[2].getMean();
			intrainterdiff[i] = all_intra_inter[1].getPercentile(50)-all_intra_inter[2].getPercentile(50);
			if(!QUIET) System.out.println("\n\n*************************************************** END PROCESSING "+city.toUpperCase());
		}
		
		
		String dir = Config.getInstance().paper_folder+"/img/batch/"+USEC+(PCA?"wPCA":"")+"-"+(USE_RELATIVE_TH?"REL-TH":"ABS-TH")+"-"+SynchCompute.USEF+""+SynchCompute.TIME_WINDOW;
		new File(dir).mkdirs();
		System.err.println(dir);
		//RPlotter.VIEW = true;
		
		RPlotter.drawBoxplot(all,ln,"comuni2012","all-"+STRINGF,dir+"/boxplot-cluster-all.png",20,null);
		RPlotter.drawBoxplot(inter,ln,"comuni2012","between-"+STRINGF,dir+"/boxplot-cluster-between.png",20,null);
		RPlotter.drawBoxplot(intra,ln,"comuni2012","within-"+STRINGF,dir+"/boxplot-cluster-within.png",20,null);
		
		//System.exit(0);
		
		
		//RPlotter.drawBar(ln.toArray(new String[cities.length]), intrainterdiff, "cities", "intra - inter", dir+"/intra-inter.png", "");
		
		MEF_IRPEF mi = MEF_IRPEF.getInstance();
		Map<String,Double> avg_comuni = new HashMap<String,Double>();
		Map<String,Double> avg_province = new HashMap<String,Double>();
		Map<String,Double> avg_regioni = new HashMap<String,Double>();
		for(int i=0; i<ln.size();i++) { // [napoli, bari, caltanissetta, siracusa, benevento, palermo, campobasso, roma, siena, ravenna, ferrara, venezia, torino, asti, milano]
			String key = (ln.get(i)+"-"+(SynchAnalysis.city2region.get(ln.get(i)).replaceAll("-", " ")).toLowerCase());
			//System.out.println("*** "+key);
			String cid = MEF_IRPEF_BLOG.name2id().get(key); // comune id (es. 63049 = Napoli)
			//System.out.println(key+" ==> "+cid);
			
			double avg_inter = ListMapArrayBasicUtils.avg(inter.get(i));
			avg_comuni.put(cid, avg_inter); 
			avg_province.put(ln.get(i).toUpperCase(), avg_inter);
			avg_regioni.put(SynchAnalysis.city2region.get(ln.get(i)), avg_inter);
			
		}
		Deprivation dp = Deprivation.getInstance();
		RPlotter.plotCorrelation(avg_regioni,dp.getDepriv(),SynchCompute.USE_FEATURE,"deprivation",dir+"/clustering-deprivazione.png",null,true);
		
		Map<String,String> id2name = MEF_IRPEF_BLOG.id2name();
		
		RPlotter.plotCorrelation(avg_comuni,mi.redditoPC(false),SynchCompute.USE_FEATURE,"per-capita income",dir+"/clustering-redPC.png",id2name,true);
		//SynchAnalysis.plotCorrelation(avg_comuni,mi.gini(false),SynchAnalysis.USE_FEATURE,"Gini",dir+"/clustering-gini.png",id2name,true);
		RPlotter.plotCorrelation(avg_province,mi.redditoPCProvince(),SynchCompute.USE_FEATURE,"per-capita income prov",dir+"/clustering-redPCP.png",null,true);
		
		
		
		SocialCapital sc = SocialCapital.getInstance();
		RPlotter.plotCorrelation(avg_province,sc.getAssoc(),SynchCompute.USE_FEATURE,"assoc",dir+"/clustering-assoc.png",null,true);
		RPlotter.plotCorrelation(avg_province,sc.getReferendum(),SynchCompute.USE_FEATURE,"referendum",dir+"/clustering-referendum.png",null,true);
		RPlotter.plotCorrelation(avg_province,sc.getBlood(),SynchCompute.USE_FEATURE,"blood",dir+"/clustering-blood.png",null,true);
		RPlotter.plotCorrelation(avg_province,sc.getSocCap(),SynchCompute.USE_FEATURE,"soccap",dir+"/clustering-soccap.png",null,true);
		
		
		Map<String,Object> tm = new HashMap<String,Object>();
		//System.out.println(dir.toString());
		tm.put("dir", dir.toString().substring(dir.toString().indexOf("img")));
		tm.put("clustering", USEC);
		tm.put("distance", SynchCompute.USEF);
		tm.put("time_window", SynchCompute.TIME_WINDOW);
		tm.put("pca",PCA);
		tm.put("threshold",(USE_RELATIVE_TH?"relative":"absolute"));
		TextPlotter.getInstance().run(tm,"src/cdraggregated/SynchClustering.ftl", dir+"/text.tex");		
		
		
		
	}
	
	static AddMap ISTAT = IstatCensus2011.getInstance().computeDensity(0, false, false);
	public static DescriptiveStatistics[] run(String city) throws Exception {
		
		
		Instances datan = new DataSource(Config.getInstance().base_folder+"/TIC2015/cache/single/"+city+"-Demo-callsLM_"+city.substring(0,2).toUpperCase()+"_COMUNI2012-resident-CLEAN.arff").getDataSet();
		Instances dataz = new DataSource(Config.getInstance().base_folder+"/TIC2015/cache/single/"+city+"-Demo-callsLM_"+city.substring(0,2).toUpperCase()+"_COMUNI2012-resident-CLEANZ.arff").getDataSet();
		
		Instances fdatan = new Instances(dataz,0,0);
		Instances fdataz = new Instances(dataz,0,0);
		for(int i=0; i<dataz.numInstances();i++) {
			Double d = ISTAT.get(dataz.instance(i).stringValue(0));
			double avg = ListMapArrayBasicUtils.avg(cast2Array(datan.instance(i)));
			//System.out.println(dataz.instance(i).stringValue(0)+" ==> "+d.intValue()+" VS. "+(int)avg);
			
			//if(dataz.instance(i).stringValue(0).equals("63049")){
			//	System.out.println("Analyzing 63049 = Napoli");
		    //}
			
			if((USE_RELATIVE_TH && avg/d > 0.01) || (!USE_RELATIVE_TH && avg > (bigCities.contains(city) ? 150: 100))) {
				fdataz.add(dataz.instance(i));
				fdatan.add(datan.instance(i));
			}
		}
		
	
		
		Clusterer actualClusterer = null;
		
		if(USEC.equals(Cluster.XMEANS)) {
			XMeans xm = new XMeans();
			if(!PCA) xm.setDistanceF(new CustomDistance());
			xm.setMinNumClusters(2);
			xm.setMaxNumClusters(fdataz.numInstances()-1);
			actualClusterer = xm;
		}
		if(USEC.equals(Cluster.DBSCAN)){
			DBSCAN dbsc = new DBSCAN();
			dbsc.setMinPoints(0);
			if(!PCA) {
				dbsc.setDistanceFunction(new CustomDistance());
				dbsc.setEpsilon(0.25); 
			}
			else 
				dbsc.setEpsilon(1.2); 
			
			actualClusterer = dbsc;
		}
		
		//HierarchicalClusterer actualClusterer = new HierarchicalClusterer();
		//actualClusterer.setOptions(new String[]{"-L","SINGLE"});
		//actualClusterer.setDistanceFunction(new R2Distance());
		
		double[] assignments = null;
		int numClusters = 0;
		if(actualClusterer!=null) {
		
			FilteredClusterer clusterer2 = new FilteredClusterer();
			clusterer2.setFilter(new PrincipalComponents());
			clusterer2.setClusterer(actualClusterer);
			
			Remove rm = new Remove();
			rm.setAttributeIndices("1");  // remove 1st attribute
			FilteredClusterer clusterer = new FilteredClusterer();
			clusterer.setFilter(rm);
			if(PCA) clusterer.setClusterer(clusterer2);
			else clusterer.setClusterer(actualClusterer);
			
			clusterer.buildClusterer(fdataz);
			
			
			ClusterEvaluation eval = new ClusterEvaluation();
			
			eval.setClusterer(clusterer);                                   // the cluster to evaluate
			eval.evaluateClusterer(fdataz);                                	// data to evaluate the clusterer on
			
			
			
			assignments = eval.getClusterAssignments();
			numClusters = eval.getNumClusters();
		}
		if(USEC.equals(Cluster.ALLDISJOINT)) {
			assignments = new double[fdataz.numInstances()];
			for(int i=0; i<assignments.length;i++)
				assignments[i] = i;
			numClusters = assignments.length;
			
		}
		if(USEC.equals(Cluster.ALLTOGETHER)) {
			assignments = new double[fdataz.numInstances()];
			numClusters = 1;
		}
		
		int[] cluster_count = new int[numClusters];
		for(double a: assignments){
			//System.out.println(a);
			cluster_count[(int)a]++;
		}
		
		StringBuffer s_cluster_count = new StringBuffer();
		for(int i=0; i<cluster_count.length;i++)
			s_cluster_count.append(","+i+":"+cluster_count[i]);
		
		
		String ok = (numClusters > 1 && numClusters < dataz.numInstances())? "OK" : "NOK";
		
		//for(int i=0; i<assignments.length;i++)
		//	System.out.println(assignments[i]+" "+data.instance(i).stringValue(0));
		System.out.println(ok+" ... "+city.toUpperCase()+": NUM INSTANCES = "+dataz.numInstances()+" NUM FILTERED INSTANCES = "+fdataz.numInstances()+", NUM CLUSTERS = "+numClusters+", CLUSTER COUNTS = ["+s_cluster_count.substring(1)+"]");
		
		//System.out.println(eval.clusterResultsToString());
		//System.out.println(eval.getLogLikelihood());
		
		
		DescriptiveStatistics all = new DescriptiveStatistics();
		DescriptiveStatistics intra = new DescriptiveStatistics();
		DescriptiveStatistics inter = new DescriptiveStatistics();
		
		DescriptiveStatistics[] per_intra_cluster_stat = new DescriptiveStatistics[numClusters];
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
					
					
					/*// DE NADAI DEBUG 
					if(city.equals("campobasso") && s > 1.2) 
						System.out.println("==> "+fdataz.instance(i).stringValue(0)+" VS "+fdataz.instance(j).stringValue(0)+" = "+s+" avg1 = "+avg(cast2Array(fdatan.instance(i)))+" avg2 = "+avg(cast2Array(fdatan.instance(j))));
					if(city.equals("campobasso") && (s >0.6 && s < 0.605)) 
						System.out.println("MMMM> "+fdataz.instance(i).stringValue(0)+" VS "+fdataz.instance(j).stringValue(0)+" = "+s+" avg1 = "+avg(cast2Array(fdatan.instance(i)))+" avg2 = "+avg(cast2Array(fdatan.instance(j))));
					if(city.equals("milano")) 
						System.out.println("==> "+fdataz.instance(i).stringValue(0)+" VS "+fdataz.instance(j).stringValue(0)+" = "+s+" avg1 = "+avg(cast2Array(fdatan.instance(i)))+" avg2 = "+avg(cast2Array(fdatan.instance(j))));
					
					
					
					if(fdatan.instance(i).stringValue(0).equals("70016")) printTimeSeriesForDebug("C:/Users/marco/Desktop",fdatan.instance(i));
					if(fdatan.instance(i).stringValue(0).equals("70024")) printTimeSeriesForDebug("C:/Users/marco/Desktop",fdatan.instance(i));
					if(fdatan.instance(i).stringValue(0).equals("70057")) printTimeSeriesForDebug("C:/Users/marco/Desktop",fdatan.instance(i));
					if(fdatan.instance(i).stringValue(0).equals("70040")) printTimeSeriesForDebug("C:/Users/marco/Desktop",fdatan.instance(i));
					*/
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
	
	
	public static void printTimeSeriesForDebug(String dir, Instance x) {
		try {
			String name = x.stringValue(0);
			PrintWriter out = new PrintWriter(new FileWriter(dir+"/"+name+".csv"));
			
			for(int i=1;i<x.numAttributes()-1;i++)
				out.println(x.value(i));
			
			out.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public static double[] cast2Array(Instance x) {
		double[] v = new double[x.numAttributes()-1];
		for(int i=1;i<v.length;i++)
			v[i-1] = x.value(i);
		return v;
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
	
	if(SynchCompute.TIME_WINDOW == -1) return SynchCompute.reallyComputeFeature(x,y);
	
	
	double[] reduced1 = new double[SynchCompute.TIME_WINDOW];
	double[] reduced2 = new double[SynchCompute.TIME_WINDOW];
	
	DescriptiveStatistics stat = new DescriptiveStatistics();
	
	for(int i=0; i<x.length - SynchCompute.TIME_WINDOW; i=i+SynchCompute.TIME_WINDOW) {
		
		System.arraycopy(x, i, reduced1, 0, reduced1.length);
		System.arraycopy(y, i, reduced2, 0, reduced2.length);
		stat.addValue(SynchCompute.reallyComputeFeature(reduced1,reduced2));
	}
	
	//return stat.getPercentile(50);
	//System.out.println("==> "+stat.getMean());
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





