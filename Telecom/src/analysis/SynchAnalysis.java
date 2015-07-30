package analysis;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import region.RegionI;
import region.RegionMap;
import utils.AddMap;
import utils.Config;
import utils.CopyAndSerializationUtils;
import utils.StatsUtils;
import utils.time.TimeConverter;
import visual.html.GoogleChartGraph;
import visual.kml.KMLHeatMap;
import visual.r.RPlotter;
import analysis.densityANDflows.density.LoadDensityFromCompanyData;
import analysis.istat.IstatCensus2011;
import analysis.istat.MEF_IRPEF;

public class SynchAnalysis {
	
	public static boolean PRINT_CORR_MATRIX = false;
	
	public static final String[] COMPANY_CONSTRAINTS = null;//new String[]{"01-32","grande",""};
	public static final int LIMIT = 20;
	
	public static final int CALLOUT_IT = 0;
	public static final int CALLOUT_IT_VS_NON_IT = 1;
	public static final int DEMOGRAPHIC_RES = 2;
	public static final int DEMOGRAPHIC_RES_VS_NON_RES = 3;
	public static final int DEMOGRAPHIC_IT_VS_NON_IT = 4;
	public static final int DEMOGRAPHIC_ALL = 5;
	public static final int TYPE = DEMOGRAPHIC_RES_VS_NON_RES;
	
	
	
	public static void main(String[] args) throws Exception {
		String[] city = new String[]{"torino","milano","venezia","roma","napoli","bari","palermo"};
		String[] files = new String[city.length];
		
		Map<String, List<SynchConstraints>> map_constraints = new HashMap<String,List<SynchConstraints>>();
		String type = "";
		int[] readIndexes = null;
		
		
		if(TYPE == CALLOUT_IT) {
			type = "CallOut";
			readIndexes = new int[]{0,1,2,3}; // time,cell,value,meta
			for(int i=0; i<city.length;i++)
				files[i] = "G:/DATASET/TI-CHALLENGE-2015/TELECOM/"+city[i]+"/"+type+".tar.gz";
				
			
			for(int i=0; i<city.length;i++) {
				List<SynchConstraints> constraints = new ArrayList<SynchConstraints>();
				Set<String> ok = new HashSet<String>();
				ok.add("39");
				constraints.add(new SynchConstraints("IT",ok));
				map_constraints.put(city[i],constraints);
				
			}
			
		}
		
		if(TYPE == CALLOUT_IT_VS_NON_IT) {
			type = "CallOut";
			readIndexes = new int[]{0,1,2,3}; // time,cell,value,meta
			for(int i=0; i<city.length;i++)
				files[i] = "G:/DATASET/TI-CHALLENGE-2015/TELECOM/"+city[i]+"/"+type+".tar.gz";
				
			
			for(int i=0; i<city.length;i++) {
				List<SynchConstraints> constraints = new ArrayList<SynchConstraints>();
				Set<String> ok = new HashSet<String>();
				ok.add("39");
				constraints.add(new SynchConstraints("IT",ok));
				constraints.add(new SynchConstraints("NOT-IT",ok,true));
				map_constraints.put(city[i],constraints);
				
			}
			
		}
		
		if(TYPE == DEMOGRAPHIC_RES) {
			type = "Demo";
			readIndexes = new int[]{0,1,3,2};
			
			for(int i=0; i<city.length;i++)
				files[i] = "G:/DATASET/TI-CHALLENGE-2015/DEMOGRAPHIC/"+city[i]+"/callsLM_"+city[i].substring(0,2).toUpperCase()+"_CAP";
			
			for(int i=0; i<city.length;i++) {
				List<SynchConstraints> constraints = new ArrayList<SynchConstraints>();
				Set<String> caps = getCAPS(city[i]);
				constraints.add(new SynchConstraints("resident",caps));
				//constraints.add(new SynchConstraints("not-resident",caps,true));
				map_constraints.put(city[i],constraints);
			}
		}
		
		if(TYPE == DEMOGRAPHIC_RES_VS_NON_RES) {
			type = "Demo";
			readIndexes = new int[]{0,1,3,2};
			
			for(int i=0; i<city.length;i++)
				files[i] = "G:/DATASET/TI-CHALLENGE-2015/DEMOGRAPHIC/"+city[i]+"/callsLM_"+city[i].substring(0,2).toUpperCase()+"_CAP";
			
			for(int i=0; i<city.length;i++) {
				List<SynchConstraints> constraints = new ArrayList<SynchConstraints>();
				Set<String> caps = getCAPS(city[i]);
				constraints.add(new SynchConstraints("resident",caps));
				constraints.add(new SynchConstraints("not-resident",caps,true));
				map_constraints.put(city[i],constraints);
			}
		}
		
		
		if(TYPE == DEMOGRAPHIC_IT_VS_NON_IT) {
			type = "Demo";
			readIndexes = new int[]{0,1,3,2};
			
			for(int i=0; i<city.length;i++)
				files[i] = "G:/DATASET/TI-CHALLENGE-2015/DEMOGRAPHIC/"+city[i]+"/callsLM_"+city[i].substring(0,2).toUpperCase()+"_CAP";
			
			for(int i=0; i<city.length;i++) {
				List<SynchConstraints> constraints = new ArrayList<SynchConstraints>();
				constraints.add(new SynchConstraints("0"));
				constraints.add(new SynchConstraints("0",true));
				map_constraints.put(city[i],constraints);
			}
		}
		
		
		if(TYPE == DEMOGRAPHIC_ALL) {
			type = "Demo";
			readIndexes = new int[]{0,1,3,2};
			
			for(int i=0; i<city.length;i++)
				files[i] = "G:/DATASET/TI-CHALLENGE-2015/DEMOGRAPHIC/"+city[i]+"/callsLM_"+city[i].substring(0,2).toUpperCase()+"_CAP";
			
			for(int i=0; i<city.length;i++) {
				List<SynchConstraints> constraints = new ArrayList<SynchConstraints>();
				Set<String> caps = getCAPS(city[i]);
				int cont = 0;
				for(String cap: caps) {
					constraints.add(new SynchConstraints(cap));
					cont++;
					if(cont >= 3) break;
				}
				
				map_constraints.put(city[i],constraints);
			}
		}
		
		
		
		

		String[] names = new String[city.length];
		double[] values = new double[city.length];
		List<String> ln = new ArrayList<String>();
		List<double[]> lvalues = new ArrayList<double[]>();
		
		Map<String,Double> all_density2012 = new HashMap<String,Double>();
		Map<String,Double> all_density2014 = new HashMap<String,Double>();
		
		RegionMap rm_to2012 = (RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/comuni2014.ser"));
		RegionMap rm_to2014 = (RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/comuni2014.ser"));
		
		for(int i=0; i<city.length;i++) {
			names[i] = city[i].substring(0, 1).toUpperCase() + city[i].substring(1,2); // capitalize first letter and consider only the first two letters
			
			RegionMap rm = (RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/tic-"+city[i]+"-caps.ser"));
			Map<String,Double> density = process(city[i],type,files[i],readIndexes,rm,map_constraints.get(city[i]));
			
			

			all_density2012.putAll(reproject2map(city[i],type,density,rm,rm_to2012,false));
			all_density2014.putAll(reproject2map(city[i],type,density,rm,rm_to2014,false));
			
			ln.add(names[i]);
			
			
			double[] y = new double[density.size()];
			int c = 0;
			for(double v: density.values())
				y[c++] = v; 
			
			lvalues.add(y);
			
			
			System.out.println(names[i]+" --> "+values[i]);
			//RPlotter.drawScatter(x, y, "dist (km)", "pearson", Config.getInstance().base_folder+"/Images/scatter-"+names[i]+"-"+type+"-synch3.pdf",null);
		}
		//RPlotter.drawScatter(lx, ly, ln, "cities", "dist", "R^2", Config.getInstance().base_folder+"/Images/scatter-"+type+"-synch3.pdf",null);
		//RPlotter.drawBar(names, values, "cities", "R^2", Config.getInstance().base_folder+"/Images/bar-"+type+"-synch3.pdf",null);
		RPlotter.drawBoxplot(lvalues,ln,"cities","R^2",Config.getInstance().base_folder+"/Images/boxplot-"+type+"-synch3.pdf",null);
		
		MEF_IRPEF mi = MEF_IRPEF.getInstance();
		plotCorrelation(all_density2014,mi.redditoPC(false),"correlation","reddito PC",Config.getInstance().base_folder+"/Images/corr-redPC.pdf");
		plotCorrelation(all_density2014,mi.gini(false),"correlation","Gini",Config.getInstance().base_folder+"/Images/corr-gini.pdf");
		
		IstatCensus2011 ic = IstatCensus2011.getInstance();
		int[] indices = new int[]{46,51,59,61};
		for(int i: indices)
			plotCorrelation(all_density2014,ic.computeDensity(i, false),"correlation",IstatCensus2011.DIMENSIONS[i],Config.getInstance().base_folder+"/Images/corr-"+IstatCensus2011.DIMENSIONS[i]+".pdf");
		
		
	}
	
	
	private static final boolean LM = true;
	private static void plotCorrelation(Map<String,Double> mapx, Map<String,Double> mapy, String titx, String tity, String file) {
		
		
		List<Double> lx = new ArrayList<Double>();
		List<Double> ly = new ArrayList<Double>();
		
		for(String k: mapx.keySet()) {
			double vx = mapx.get(k);
			Double vy = mapy.get(k);
			if(vy != null) {
				lx.add(vx);
				ly.add(vy);
			}
		}
		
		double[] x = new double[lx.size()];
		double[] y = new double[ly.size()];
		for(int i=0; i<x.length;i++) {
			x[i] = lx.get(i);
			y[i] = ly.get(i);
		}
		
		RPlotter.drawScatter(x,y, titx, tity, file, "stat_smooth("+(LM?"":"method=lm,")+"colour='black') + theme(legend.position='none') + geom_point(size = 5)");
	}
	

	
	
	private static Map<String,Double> reproject2map(String city, String type, Map<String,Double> density,RegionMap rm_from,RegionMap rm_to, boolean print) {
		AddMap res = new AddMap();
		for(String name: density.keySet()) {
			String name_to = "";
			if(name.equals("30121")) name_to = "27042"; // venezia
			else if(name.equals("80053")) name_to = "63024"; // napoli castellammare		
			else {
				RegionI r = rm_from.getRegion(name);
				System.out.print(name+": ("+r.getLatLon()[1]+","+r.getLatLon()[0]+") --> ");
				name_to = rm_to.get(r.getLatLon()[1], r.getLatLon()[0]).getName();
				System.out.println(name_to);
			}
			res.add(name_to, density.get(name));
		}
		
		if(print) {
			try {
				rm_to.setName(city+"-"+type+"-corr-reprojected");
				KMLHeatMap.drawHeatMap(Config.getInstance().base_folder+"/TIC2015/"+city+"-"+type+"-correlation.kml",res,rm_to,"",false);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return res;
	}
	
	
	private static Set<String> getCAPS(String city) {
		Set<String> caps = new HashSet<String>();
		RegionMap rm = (RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/tic-"+city+"-caps.ser"));
		for(RegionI r: rm.getRegions()) {
			String name = r.getName();
			if(name.indexOf("_")>0) {
				//System.out.print(name+"  -->  ");
				name = name.substring(0,name.indexOf("_"));
				//System.out.println(name);
			}
			caps.add(name);
		}
		return caps;
	}
	
	
	
	public static Map<String,Double> process(String city, String type, String file, int[] readIndexes, RegionMap rm, List<SynchConstraints> constraints) throws Exception {
		
		//RegionMap rm = (RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/regioni.ser"));
		
		// select the regions to be considered
		
		LinkedHashMap<String, Double> company_map = LoadDensityFromCompanyData.getInstance(city,rm,COMPANY_CONSTRAINTS);
		String[] regions = new String[LIMIT > 0 ? Math.min(LIMIT, company_map.size()) : company_map.size()];
		int i = 0;
		for(String key: company_map.keySet()) {
			regions[i] = key;
			i++;
			if(LIMIT > 0 && i >= LIMIT) break;
		}
		
		
		System.out.println("regions = "+regions.length);
		
		/*
		System.out.println("Regions to be analyzed");
		for(String region: regions)
			System.out.println("- "+region);
		*/
		
		List<TimeDensityFromAggregatedData> tds = new ArrayList<TimeDensityFromAggregatedData>();
		for(SynchConstraints constraint : constraints) {
			TimeDensityFromAggregatedData td = new TimeDensityFromAggregatedData(city,type,file,readIndexes,constraint,rm);
			System.out.println(td.getType());
			tds.add(td);
		}
		
		
		String suffix = LIMIT > 0 ? "limited to "+LIMIT : "";
		System.out.println("processign "+city+" -- regions size --> "+rm.getNumRegions()+" "+suffix);
		for(i=0; i<constraints.size();i++)
			System.out.println("processing "+city+" -- "+constraints.get(i).title+" size --> "+tds.get(i).size()+" "+suffix);
		
		//LoadDensityFromAggregatedData.process(city, tds, COMPANY_CONSTRAINTS, LIMIT);
		
		if(tds.size() == 1) return getDistCorr(regions,rm,tds.get(0));
		else return getDistCorr(city,regions,rm,tds);
	}
	
	
	
	
	public static Map<String,Double> getDistCorr(String[] regions, RegionMap rm, TimeDensityFromAggregatedData td) {
		
		
		Map<String,Double> density = new HashMap<String,Double>();
		
		for(int i=0; i<regions.length;i++) {
			
			double corr = 0;
			
			for(int j=0;j<regions.length;j++) {
				if(i==j) continue;
				double[] seriesi = td.get(regions[i]);
				double[] seriesj = td.get(regions[j]);
				double[] zetai = StatsUtils.getZH(seriesi,td.tc);
				double[] zetaj = StatsUtils.getZH(seriesj,td.tc);
				
	
				corr+=StatsUtils.r2(filter(seriesi,td.tc),filter(seriesj,td.tc));
				//double dist = LatLonUtils.getHaversineDistance(rm.getRegion(regions[i]).getCenterPoint(),rm.getRegion(regions[j]).getCenterPoint());
				
			}
			
			corr = corr / (regions.length - 1);
			
			density.put(regions[i], corr);
		}
		return density;
	}
	
	public static Map<String,Double> getDistCorr(String city, String[] regions, RegionMap rm, List<TimeDensityFromAggregatedData> tds) {
		
		Map<String,Double> density = new HashMap<String,Double>();
		
		String[] x = tds.get(0).tc.getTimeLabels();
		Map<String,String> desc = new HashMap<String,String>();
		
		for(int i=0; i<regions.length;i++) {
			
			int n = tds.size();
			double[] corrs = new double[n*(n-1)/2];
			int c=0;
			for(int a=0; a<n;a++)
			for(int b=a+1;b<n;b++) {
			
				TimeDensityFromAggregatedData td1 = tds.get(a);
				TimeDensityFromAggregatedData td2 = tds.get(b);
				double[] series1 = td1.get(regions[i]);
				double[] series2 = td2.get(regions[i]);
				
				
				
				double[] zeta1 = StatsUtils.getZH(series1,td1.tc);
				double[] zeta2 = StatsUtils.getZH(series2,td2.tc);
				corrs[c] = StatsUtils.r2(filter(series1,td1.tc),filter(series2,td2.tc));	
				c++;
			}
			
			double corr  = avg(corrs);
			
			List<String> names = new ArrayList<String>();
			List<double[]> y = new ArrayList<double[]>();
				
			for(TimeDensityFromAggregatedData td:tds) {
				names.add(td.getType());
				y.add(td.get(regions[i]));
			}
			desc.put(regions[i], GoogleChartGraph.getGraph(x, y, names, "data", "y"));
			density.put(regions[i], corr);
		}
		
		

		try {
			rm.setName(city+"-"+tds.get(0).getType()+"-correlation");
			KMLHeatMap.drawHeatMap(Config.getInstance().base_folder+"/TIC2015/"+city+"-"+tds.get(0).getType()+"-correlation.kml",density,rm,desc,false);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return density;
	}
	
	
	private static double avg(double[] x) {
		double avg = 0;
		for(double v: x)
			avg+=v;
		return avg / x.length;
	}
	
	private static double log2(double x) {
		return Math.log(x) / Math.log(2);
	}
	
	
	
	public static double[] filter(double[] x, TimeConverter tc) {
		double[] fx = new double[x.length];
		int size = 0;
		Calendar cal = Calendar.getInstance();
		for(int i=0; i<x.length;i++) {
			cal.setTimeInMillis(tc.index2time(i));
			int hour = cal.get(Calendar.HOUR_OF_DAY);
			int day_of_week = cal.get(Calendar.DAY_OF_WEEK);
			if(hour>7 && hour<18 && day_of_week != Calendar.SATURDAY && day_of_week != Calendar.SUNDAY) {
				fx[size] = x[i];
				size++;
			}
		}
		double[] fx2 = new double[size];
		System.arraycopy(fx, 0, fx2, 0, fx2.length);
		return fx2;
	}
	
	
}
