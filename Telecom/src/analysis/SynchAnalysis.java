package analysis;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
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
import analysis.istat.Deprivation;
import analysis.istat.IstatCensus2011;
import analysis.istat.MEF_IRPEF;
import analysis.istat.MEF_IRPEF_BLOG;
import analysis.istat.SocialCapital;

public class SynchAnalysis {
	
	public static boolean PRINT_CORR_MATRIX = false;
	
	public static final String[] COMPANY_CONSTRAINTS = null;//new String[]{"01-32","grande",""};
	public static final int LIMIT = -1;
	
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
				files[i] = Config.getInstance().dataset_folder+"/TI-CHALLENGE-2015/TELECOM/"+city[i]+"/"+type+".tar.gz";
				
			
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
				files[i] = Config.getInstance().dataset_folder+"/TI-CHALLENGE-2015/TELECOM/"+city[i]+"/"+type+".tar.gz";
				
			
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
				files[i] = Config.getInstance().dataset_folder+"/TI-CHALLENGE-2015/DEMOGRAPHIC/"+city[i]+"/callsLM_"+city[i].substring(0,2).toUpperCase()+"_CAP";
			
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
				files[i] = Config.getInstance().dataset_folder+"/TI-CHALLENGE-2015/DEMOGRAPHIC/"+city[i]+"/callsLM_"+city[i].substring(0,2).toUpperCase()+"_CAP";
			
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
				files[i] = Config.getInstance().dataset_folder+"/TI-CHALLENGE-2015/DEMOGRAPHIC/"+city[i]+"/callsLM_"+city[i].substring(0,2).toUpperCase()+"_CAP";
			
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
				files[i] =Config.getInstance().dataset_folder+"/TI-CHALLENGE-2015/DEMOGRAPHIC/"+city[i]+"/callsLM_"+city[i].substring(0,2).toUpperCase()+"_CAP";
			
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
		
		Map<String,Double> all_density_comuni2012 = new HashMap<String,Double>();
		Map<String,Double> all_density_comuni2014 = new HashMap<String,Double>();
		Map<String,Double> all_density_prov2011 = new HashMap<String,Double>();
		Map<String,Double> all_density_regioni = new HashMap<String,Double>();
		
		RegionMap comuni2012 = (RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/comuni2012.ser"));
		RegionMap comuni2014 = (RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/comuni2014.ser"));
		RegionMap prov2011 = (RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/prov2011.ser"));
		RegionMap regioni = (RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/regioni.ser"));
		
		for(int i=0; i<city.length;i++) {
			names[i] = city[i].substring(0, 1).toUpperCase() + city[i].substring(1,2); // capitalize first letter and consider only the first two letters
			
			RegionMap rm = (RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/tic-"+city[i]+"-caps.ser"));
			Map<String,Double> density = process(city[i],type,files[i],readIndexes,rm,map_constraints.get(city[i]));
			

			all_density_comuni2012.putAll(reproject2map(city[i],type,density,rm,comuni2012,false));
			all_density_comuni2014.putAll(reproject2map(city[i],type,density,rm,comuni2014,false));
			all_density_prov2011.putAll(reproject2map(city[i],type,density,rm,prov2011,false));
			all_density_regioni.putAll(reproject2map(city[i],type,density,rm,regioni,false));
			
			ln.add(names[i]);
			
			
			double[] y = new double[density.size()];
			int c = 0;
			for(double v: density.values())
				y[c++] = v; 
			
			lvalues.add(y);
			
			
			//System.out.println(names[i]+" --> "+values[i]);
			//RPlotter.drawScatter(x, y, "dist (km)", "pearson", Config.getInstance().base_folder+"/Images/scatter-"+names[i]+"-"+type+"-synch3.pdf",null);
		}
		//RPlotter.drawScatter(lx, ly, ln, "cities", "dist", "R^2", Config.getInstance().base_folder+"/Images/scatter-"+type+"-synch3.pdf",null);
		//RPlotter.drawBar(names, values, "cities", "R^2", Config.getInstance().base_folder+"/Images/bar-"+type+"-synch3.pdf",null);
		RPlotter.drawBoxplot(lvalues,ln,"cities","R^2",Config.getInstance().base_folder+"/Images/boxplot-"+type+"-synch3.pdf",null);
		
		Deprivation dp = Deprivation.getInstance();
		plotCorrelation(all_density_regioni,dp.getDepriv(),"correlation","deprivazione",Config.getInstance().base_folder+"/Images/corr-deprivazione.pdf");
		for(String k: all_density_regioni.keySet())
			System.out.println("deprivazione..................... "+k+""+" "+all_density_regioni.get(k)+" .... "+dp.getDepriv().get(k));
		
		
		MEF_IRPEF mi = MEF_IRPEF.getInstance();
		plotCorrelation(all_density_comuni2014,mi.redditoPC(false),"correlation","reddito PC",Config.getInstance().base_folder+"/Images/corr-redPC.pdf");
		plotCorrelation(all_density_comuni2014,mi.gini(false),"correlation","Gini",Config.getInstance().base_folder+"/Images/corr-gini.pdf");
		
		
		IstatCensus2011 ic = IstatCensus2011.getInstance();
		int[] indices = new int[]{46,51,59,61};
		for(int i: indices)
			plotCorrelation(all_density_comuni2012,ic.computeDensity(i, true, false),"correlation",IstatCensus2011.DIMENSIONS[i],Config.getInstance().base_folder+"/Images/corr-"+IstatCensus2011.DIMENSIONS[i]+".pdf");
		
		
		SocialCapital sc = SocialCapital.getInstance();
		plotCorrelation(all_density_prov2011,sc.getAssoc(),"correlation","assoc",Config.getInstance().base_folder+"/Images/corr-assoc.pdf");
		plotCorrelation(all_density_prov2011,sc.getReferendum(),"correlation","referendum",Config.getInstance().base_folder+"/Images/corr-referendum.pdf");
		plotCorrelation(all_density_prov2011,sc.getBlood(),"correlation","blood",Config.getInstance().base_folder+"/Images/corr-blood.pdf");
		
		for(String k: all_density_prov2011.keySet())
			System.out.println("blood..................... "+k+""+" "+all_density_prov2011.get(k)+" .... "+sc.getBlood().get(k));
		
	}
	
	private static final DecimalFormat DF = new DecimalFormat("0.00",new DecimalFormatSymbols(Locale.US));
	private static final boolean LM = false;
	public static void plotCorrelation(Map<String,Double> mapx, Map<String,Double> mapy, String titx, String tity, String file) {
		
		
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
		
		
		
		
		//System.out.println(titx+" map = "+mapx.size()+" array = "+x.length);
		//System.out.println(tity+" map = "+mapy.size()+" array = "+y.length);
		
		double r = StatsUtils.r(x, y);
		String r2 = "annotate(\"text\", parse=TRUE, size=10, fontface='bold', x="+(r > 0 ? "-" : "")+"Inf, y=Inf, label=\"r^2 == "+DF.format(r*r)+"\", hjust="+(r > 0 ? "-0.2" : "1.2")+", vjust=1.2)";
		
		RPlotter.drawScatter(x,y, titx, tity, file, "stat_smooth("+(LM?"":"method=lm,")+"colour='black') + theme(legend.position='none') + geom_point(size = 5) + "+r2);
	}
	

	
	
	public static Map<String, String> mapConverter(RegionMap rm_from,RegionMap rm_to) {
			Map<String, String> conv = new HashMap<String,String>();
			for(RegionI r: rm_from.getRegions()) {
				String name_from = r.getName();
				String name_to = "";
				if(name_from.equals("30121")) name_to = "27042"; // venezia
				else if(name_from.equals("80053")) name_to = "63024"; // napoli castellammare		
				else name_to = rm_to.get(r.getLatLon()[1], r.getLatLon()[0]).getName();
				conv.put(name_from, name_to);
			}
			return conv;
	}
	
	private static Map<String,Double> reproject2map(String city, String type, Map<String,Double> density,RegionMap rm_from,RegionMap rm_to, boolean print) {
		AddMap res = new AddMap();
		AddMap cont = new AddMap(); // this is to divide res in order to compute means
		for(String name: density.keySet()) {
			String name_to = null;
			//System.out.println(rm_to.getName()+"............................................................");
			if(rm_to.getName().contains("comuni")) {
				if(name.equals("30121")) name_to = "27042"; // venezia
				else if(name.equals("80053")) name_to = "63024"; // napoli castellammare		
			}
			if(rm_to.getName().contains("prov")) {
				if(name.equals("20078")) name_to = "98LODI";
				else if(name.equals("30121")) name_to = "27VENEZIA";
				else if(name.equals("80058")) name_to = "63NAPOLI";
				else if(name.equals("80053")) name_to = "63NAPOLI";
				else if(name.equals("80076")) name_to = "63NAPOLI";
				else if(name.equals("80075")) name_to = "63NAPOLI";
				else if(name.equals("80074")) name_to = "63NAPOLI";
				else if(name.equals("80073")) name_to = "63NAPOLI";
				else if(name.equals("80071")) name_to = "63NAPOLI";
				else if(name.equals("80079")) name_to = "63NAPOLI";
				else if(name.equals("80077")) name_to = "63NAPOLI";		
			}
			if(rm_to.getName().contains("regioni")) {
				if(name.equals("30121")) name_to = "VENETO4527694";
				else if(name.equals("80058")) name_to = "CAMPANIA5701931";
				else if(name.equals("80053")) name_to = "CAMPANIA5701931";
				else if(name.equals("80076")) name_to = "CAMPANIA5701931";
				else if(name.equals("80075")) name_to = "CAMPANIA5701931";
				else if(name.equals("80074")) name_to = "CAMPANIA5701931";
				else if(name.equals("80073")) name_to = "CAMPANIA5701931";
				else if(name.equals("80071")) name_to = "CAMPANIA5701931";
				else if(name.equals("80079")) name_to = "CAMPANIA5701931";
				else if(name.equals("80077")) name_to = "CAMPANIA5701931";			
			}
			if(name_to == null) {
				RegionI r = rm_from.getRegion(name);
				RegionI r_to = rm_to.get(r.getLatLon()[1], r.getLatLon()[0]);
				
				if(r_to == null) {
					System.err.println(name+" -->" + r.getLatLon()[0]+","+r.getLatLon()[1]);
					name_to = null;
				}
				else name_to = r_to.getName();
			}
			if(rm_to.getName().contains("regioni")) {
				if(name_to.equals("BASILICATA597768")) name_to = "PUGLIA4020707";
				else if(name_to.equals("UMBRIA825826")) name_to = "LAZIO5112413";
			}
			
			
			if(rm_to.getName().contains("prov")) {
				if(name_to.equals("2VERCELLI")) name_to = null;  // come mai se metto 1TORINO influneza così tanto?
			}
			
			if(name_to!=null){
				res.add(name_to, density.get(name));
				cont.add(name_to, 1);
			}
		}
		
		
		// compute mean
		for(String k: res.keySet())
			res.put(k, res.get(k) / cont.get(k));
		
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
		
		/*
		LinkedHashMap<String, Double> company_map = LoadDensityFromCompanyData.getInstance(city,rm,COMPANY_CONSTRAINTS);
		String[] regions = new String[LIMIT > 0 ? Math.min(LIMIT, company_map.size()) : company_map.size()];
		int i = 0;
		for(String key: company_map.keySet()) {
			regions[i] = key;
			i++;
			if(LIMIT > 0 && i >= LIMIT) break;
		}
		*/
		
		String[] regions = new String[LIMIT > 0 ? Math.min(LIMIT, rm.getNumRegions()) : rm.getNumRegions()];
		int i=0;
		for(RegionI r : rm.getRegions()) {
			regions[i] = r.getName();
			i++;
			if(LIMIT > 0 && i >= LIMIT) break;
		}
		
		
		
		
		System.out.println(city+" regions = "+regions.length);
		
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
				
				double[] zetami = StatsUtils.getZmH(seriesi,td.tc);
				double[] zetamj = StatsUtils.getZmH(seriesj,td.tc);
				
				
				
				//corr+=StatsUtils.r2(filter(seriesi,td.tc),filter(seriesj,td.tc));
				corr+=StatsUtils.r2(filter(seriesi,td.tc),filter(seriesj,td.tc)) * avg(filter(zetami,td.tc)) * avg(filter(zetamj,td.tc)) ;
				
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
		Map<String,List<String>> rawDesc = new HashMap<String,List<String>>();
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
				
				double[] zetam1 = StatsUtils.getZmH(series1,td1.tc);
				double[] zetam2 = StatsUtils.getZmH(series2,td2.tc);
				
				
				
				
				//corrs[c] = StatsUtils.r2(filter(series1,td1.tc),filter(series2,td2.tc));	
				//corrs[c] = Entropy.calculateConditionalEntropy(bin(filter(series1,td1.tc),10),bin(filter(series2,td2.tc),10));	
				corrs[c] = avg(filter(zetam1,td1.tc)) * avg(filter(zetam2,td2.tc));
				
				/*
				double[][] ls1 = TestEntropy.createLagged(bin(filter(series1,td1.tc),10), 24);
				double[][] ls2 = TestEntropy.createLagged(bin(filter(series2,td2.tc),10), 24);
				double e1 = Entropy.calculateConditionalEntropy(ls1[0],ls1[1]);
				double e2 = Entropy.calculateConditionalEntropy(ls2[0],ls2[1]);
				corrs[c] = e1*e2;
				*/
				//corrs[c] = StatsUtils.r2(filter(series1,td1.tc),filter(series2,td2.tc)) * (avg(filter(zetam1,td1.tc)) + avg(filter(zetam2,td2.tc)));	
				
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
			rawDesc.put(regions[i], getRawDesc(names,y));
		}
		
		RegionMap comuni = (RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/comuni"+MEF_IRPEF.COMUNI_YEAR+".ser"));
		Map<String,String> map_converter = mapConverter(rm,comuni);
		

		try {
			//rm.setName(city+"-"+tds.get(0).getType()+"-correlation");
			//KMLHeatMap.drawHeatMap(Config.getInstance().base_folder+"/TIC2015/"+city+"-"+tds.get(0).getType()+"-correlation.kml",density,rm,desc,false);
			
			Map<String,Double> rdensity = reproject2map(city,tds.get(0).getType()+"-correlation",density,rm,comuni,false);
			Map<String,String> rdesc = new HashMap<String,String>();
			for(String k: desc.keySet())
				rdesc.put(map_converter.get(k), desc.get(k));
			
			KMLHeatMap.drawHeatMap(Config.getInstance().base_folder+"/TIC2015/"+city+"-"+tds.get(0).getType()+"-correlation.kml",rdensity,comuni,rdesc,false);
			
			
			printCSV(Config.getInstance().base_folder+"/TIC2015/"+city+"-raw.csv",rawDesc,map_converter);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return density;
	}
	
	
	private static List<String> getRawDesc(List<String> names, List<double[]> y) {
		List<String> raw = new ArrayList<String>();
		for(int i=0; i<names.size();i++) 
			raw.add(getRawDesc(names.get(i),y.get(i)));
		return raw;
	}
	
	
	private static final String SEP = ";";
	//private static final DecimalFormat DF = new DecimalFormat("",new DecimalFormatSymbols(Locale.US));
	private static String getRawDesc(String name, double[] y) {
		StringBuffer sb = new StringBuffer(name);
		for(int i=0; i<y.length;i++)
			sb.append(SEP+y[i]);
		return sb.toString();
	}
	
	
	private static void printCSV(String file, Map<String,List<String>> rawDesc, Map<String,String> map_converter) {
		try {
			Map<String,String> id2name = MEF_IRPEF_BLOG.id2name();
			PrintWriter out = new PrintWriter(new FileWriter(file));
			
			for(String key: rawDesc.keySet()) {
				String comune = map_converter.get(key);
				for(String desc: rawDesc.get(key))
					out.println(comune+SEP+id2name.get(comune)+SEP+desc);
			}
			out.close();
			
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	private static double[] bin(double[] x, int n) {
		double max = 0;
		for(double v: x)
			max = Math.max(max, v);
		double[] bx = new double[x.length];
		for(int i=0; i<bx.length;i++)
			bx[i] = Math.floor(1.0 * n * x[i] / max);
		return bx;
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
