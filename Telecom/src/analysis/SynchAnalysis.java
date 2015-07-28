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

import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.gps.utils.LatLonUtils;

import region.RegionI;
import region.RegionMap;
import utils.Config;
import utils.CopyAndSerializationUtils;
import utils.GrangerTest;
import utils.StatsUtils;
import utils.time.TimeConverter;
import visual.html.GoogleChartGraph;
import visual.kml.KMLHeatMap;
import visual.r.RPlotter;
import analysis.densityANDflows.density.LoadDensityFromAggregatedData;
import analysis.densityANDflows.density.LoadDensityFromCompanyData;

public class SynchAnalysis {
	
	public static boolean PRINT_CORR_MATRIX = false;
	
	public static final String[] COMPANY_CONSTRAINTS = null;//new String[]{"01-32","grande",""};
	public static final int LIMIT = -1;
	
	public static final int CALLOUT = 0;
	public static final int DEMOGRAPHIC = 1;
	public static final int DEMOGRAPHIC2 = 2;
	public static final int TYPE = DEMOGRAPHIC2;
	
	
	
	public static void main(String[] args) throws Exception {
		String[] city = new String[]{"torino","milano","venezia","roma","napoli","bari","palermo"};
		String[] files = new String[city.length];
		
		Map<String, List<SynchConstraints>> map_constraints = new HashMap<String,List<SynchConstraints>>();
		String type = "";
		int[] readIndexes = null;
		
		
		if(TYPE == CALLOUT) {
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
		
		if(TYPE == DEMOGRAPHIC) {
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
		if(TYPE == DEMOGRAPHIC2) {
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
		
		
		
		
		

		String[] names = new String[city.length];
		double[] values = new double[city.length];
		List<double[]> lx = new ArrayList<double[]>();
		List<double[]> ly = new ArrayList<double[]>();
		List<String> ln = new ArrayList<String>();
		List<double[]> lvalues = new ArrayList<double[]>();
		
		for(int i=0; i<city.length;i++) {
			names[i] = city[i].substring(0, 1).toUpperCase() + city[i].substring(1,2); // capitalize first letter and consider only the first two letters
			
			SimpleRegression sr = new SimpleRegression();
			double[][] xy = process(city[i],type,files[i],readIndexes,map_constraints.get(city[i]));
			double[] x = xy[0];
			double[] y = xy[1];
			lx.add(x);
			ly.add(y);
			ln.add(names[i]);
			for(int t=0; t<x.length;t++)
				sr.addData(x[t], y[t]);
			values[i] = sr.getR();
			
			
			lvalues.add(y);
			
			
			System.out.println(names[i]+" --> "+values[i]);
			//RPlotter.drawScatter(x, y, "dist (km)", "pearson", Config.getInstance().base_folder+"/Images/scatter-"+names[i]+"-"+type+"-synch3.pdf",null);
		}
		//RPlotter.drawScatter(lx, ly, ln, "cities", "dist", "R^2", Config.getInstance().base_folder+"/Images/scatter-"+type+"-synch3.pdf",null);
		//RPlotter.drawBar(names, values, "cities", "R^2", Config.getInstance().base_folder+"/Images/bar-"+type+"-synch3.pdf",null);
		RPlotter.drawBoxplot(lvalues,ln,"cities","R^2",Config.getInstance().base_folder+"/Images/boxplot-"+type+"-synch3.pdf",null);
		
		
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
	
	
	
	public static double[][] process(String city, String type, String file, int[] readIndexes, List<SynchConstraints> constraints) throws Exception {
		
		
		// select the regions to be considered
		
		LinkedHashMap<String, Double> company_map = LoadDensityFromCompanyData.getInstance(city,COMPANY_CONSTRAINTS);
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
		for(SynchConstraints constraint : constraints)
			tds.add(new TimeDensityFromAggregatedData(city,type,file,readIndexes,constraint));
		
		RegionMap rm = (RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/tic-"+city+"-gird.ser"));
		
		String suffix = LIMIT > 0 ? "limited to "+LIMIT : "";
		System.out.println("processign "+city+" -- regions size --> "+rm.getNumRegions()+" "+suffix);
		for(i=0; i<constraints.size();i++)
			System.out.println("processing "+city+" -- "+constraints.get(i).title+" size --> "+tds.get(i).map.size()+" "+suffix);
		
		//LoadDensityFromAggregatedData.process(city, tds, COMPANY_CONSTRAINTS, LIMIT);
		
		if(tds.size() == 1) return getDistCorr(regions,rm,tds.get(0));
		if(tds.size() == 2) return getDistCorr(city,regions,rm,tds.get(0),tds.get(1));
		return null;
	}
	
	
	
	
	public static double[][] getDistCorr(String[] regions, RegionMap rm, TimeDensityFromAggregatedData td) {
		int n = regions.length;
		double[] x = new double[n*(n-1)/2];
		double[] y = new double[n*(n-1)/2];
		
		// now regions contains all the regions I have to consider
		int c = 0;
		for(int i=0; i<regions.length;i++)
		for(int j=i+1;j<regions.length;j++) {
			double[] zetai = /*td.map.get(regions[i]);*/ StatsUtils.getZH(td.map.get(regions[i]),td.tc);
			double[] zetaj = /*td.map.get(regions[j]);*/ StatsUtils.getZH(td.map.get(regions[j]),td.tc);
			
			
			double corr = StatsUtils.r2(filter(zetai,td.tc),filter(zetaj,td.tc));
			//double corr = GrangerTest.granger(filter(zetai,td.tc),filter(zetaj,td.tc),0)[3];
			
			
			if(Double.isNaN(corr)) corr = 0;
			double dist = LatLonUtils.getHaversineDistance(rm.getRegion(regions[i]).getCenterPoint(),rm.getRegion(regions[j]).getCenterPoint());
			//System.out.println(dist+" --> "+corr);
			//sr.addData(dist, corr);
			x[c] = Math.round(dist/1000);
			y[c] = corr;
			c++;
		}	
		return new double[][]{x,y};
	}
	
	public static double[][] getDistCorr(String city, String[] regions, RegionMap rm, TimeDensityFromAggregatedData td1, TimeDensityFromAggregatedData td2) {
		double[][] dc = new double[2][regions.length];
		
		Map<String,Double> density = new HashMap<String,Double>();
		
		
		for(int i=0; i<regions.length;i++) {
			
			double corr = 0;
			if(td1.map.get(regions[i]) != null && td2.map.get(regions[i]) != null) {
			
				double[] zeta1 = /*td1.map.get(regions[i]);*/ StatsUtils.getZH(td1.map.get(regions[i]),td1.tc);
				double[] zeta2 = /*td2.map.get(regions[i]);*/ StatsUtils.getZH(td2.map.get(regions[i]),td2.tc);
				corr = StatsUtils.r2(filter(zeta1,td1.tc),filter(zeta2,td2.tc));
				dc[1][i] = corr;
			}
			density.put(regions[i], corr);
		}
		
		//System.out.println("DENSITY:");
		//for(String r: density.keySet())
		//	System.out.println(r+" = "+density.get(r));
		
		
		String[] x = td1.tc.getTimeLabels();
		
		Map<String,String> desc = new HashMap<String,String>();
		
		for(String r: density.keySet()) {
			
			String description = "";
			
			if(td1.map.get(r) != null && td2.map.get(r) != null) {
				List<double[]> y = new ArrayList<double[]>();
				y.add(StatsUtils.getZH(td1.map.get(r),td1.tc));
				y.add(StatsUtils.getZH(td2.map.get(r),td1.tc));
				
				List<String> names = new ArrayList<String>();
				names.add(td1.getType());
				names.add(td2.getType());
				description = GoogleChartGraph.getGraph(x, y, names, "data", "y");
			}
			desc.put(r, description);
		}
		
		
		try {
			KMLHeatMap.drawHeatMap(Config.getInstance().base_folder+"/TIC2015/"+city+"-correlation.kml",density,rm,desc,false);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return dc;
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
