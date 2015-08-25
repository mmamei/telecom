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

import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;

import region.RegionI;
import region.RegionMap;
import utils.AddMap;
import utils.Config;
import utils.CopyAndSerializationUtils;
import utils.GrangerTest;
import utils.StatsUtils;
import utils.time.TimeConverter;
import visual.html.GoogleChartGraph;
import visual.kml.KMLHeatMap;
import visual.r.RPlotter;
import zzz_misc_code.TestEntropy;
import JavaMI.Entropy;
import analysis.densityANDflows.density.LoadDensityFromCompanyData;
import analysis.istat.Deprivation;
import analysis.istat.IstatCensus2011;
import analysis.istat.MEF_IRPEF;
import analysis.istat.MEF_IRPEF_BLOG;
import analysis.istat.SocialCapital;

public class SynchAnalysis {
	
	
	
	
	/*
	 * Fare un bello studio sulla composizione in termini di caos del traffico generato in ogni comune.
	 * Potremmo misurare l'entropia di quella distribuzione e vedre quanta eterogeneità c'è in quella zona.
	 * Anche quetsa feature si potrebbe correlare con i dati economici.
	 * nota: i caps potrebbero essere aggragti a livello di comune-privincia-regione in modo da avere distribuzioni migliori.
	 * forse è meglio fare un altra classe ed agire su time density from aggregated data
	 */
	
	
	
	
	public static boolean CAPOLUOGO_ONLY = false;
	
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
	
	
	public static final String F_AVGZH = "Avg_ZH";
	public static final String F_ENTROPY_LAG = "Entropy Lag";
	public static final String F_ENTROPY = "Entropy";
	public static final String F_ENTROPY_CORRECTED = "Entropy Corrected";
	public static final String F_R = "R";
	public static final String F_R2 = "R-Sq";
	public static final String F_GRANGER = "Granger";
	public static final String F_R2_CORR = "R-Sq-Corr";
	public static final String USE_FEATURE = F_ENTROPY_CORRECTED;	
	
	
	
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
		
		//List<double[]> lvalues_caps = new ArrayList<double[]>();
		List<double[]> lvalues_comuni2012 = new ArrayList<double[]>();
		List<double[]> lvalues_comuni2014 = new ArrayList<double[]>();
		List<double[]> lvalues_prov2011 = new ArrayList<double[]>();
		List<double[]> lvalues_regioni = new ArrayList<double[]>();
		
		Map<String,Double> all_density_comuni2012 = new HashMap<String,Double>();
		Map<String,Double> all_density_comuni2014 = new HashMap<String,Double>();
		Map<String,Double> all_density_prov2011 = new HashMap<String,Double>();
		Map<String,Double> all_density_regioni = new HashMap<String,Double>();
		
		RegionMap comuni2012 = (RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/tic-comuni2012.ser"));
		RegionMap comuni2014 = (RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/tic-comuni2014.ser"));
		RegionMap prov2011 = (RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/prov2011.ser"));
		RegionMap regioni = (RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/regioni.ser"));
		
		for(int i=0; i<city.length;i++) {
			names[i] = city[i].substring(0, 1).toUpperCase() + city[i].substring(1,2); // capitalize first letter and consider only the first two letters
			
			RegionMap rm = (RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/tic-"+city[i]+"-caps.ser"));
			/*
			Map<String,Double> density_caps = process(city[i],type,files[i],readIndexes,rm,map_constraints.get(city[i]));
			Map<String,Double> density_comuni2012 = reproject2map(city[i],type,density_caps,rm,comuni2012,false);
			Map<String,Double> density_comuni2014 = reproject2map(city[i],type,density_caps,rm,comuni2014,false);
			Map<String,Double> density_prov2011 = reproject2map(city[i],type,density_caps,rm,prov2011,false);
			Map<String,Double> density_regioni = reproject2map(city[i],type,density_caps,rm,regioni,false);
			*/
			
			
			Map<String,Double> density_caps = process(city[i],type,files[i],readIndexes,rm,map_constraints.get(city[i]));
			Map<String,Double> density_comuni2012 = reproject2map(city[i],type,density_caps,rm,comuni2012,false);
			Map<String,Double> density_comuni2014 = reproject2map(city[i],type,density_caps,rm,comuni2014,false);
			Map<String,Double> density_prov2011 = reproject2map(city[i],type,density_caps,rm,prov2011,false);
			Map<String,Double> density_regioni = reproject2map(city[i],type,density_caps,rm,regioni,false);
			
			
			all_density_comuni2012.putAll(density_comuni2012);
			all_density_comuni2014.putAll(density_comuni2014);
			all_density_prov2011.putAll(density_prov2011);
			all_density_regioni.putAll(density_regioni);
			
			ln.add(names[i]);
			
			
			
			
			//lvalues_caps.add(density2array(density_caps));
			lvalues_comuni2012.add(density2array(density_comuni2012));
			lvalues_comuni2014.add(density2array(density_comuni2014));
			lvalues_prov2011.add(density2array(density_prov2011));
			lvalues_regioni.add(density2array(density_regioni));
			
			//System.out.println(names[i]+" --> "+values[i]);
			//RPlotter.drawScatter(x, y, "dist (km)", "pearson", Config.getInstance().base_folder+"/Images/scatter-"+names[i]+"-"+type+"-synch3.pdf",null);
		}
		//RPlotter.drawScatter(lx, ly, ln, "cities", "dist", "R^2", Config.getInstance().base_folder+"/Images/scatter-"+type+"-synch3.pdf",null);
		//RPlotter.drawBar(names, values, "cities", "R^2", Config.getInstance().base_folder+"/Images/bar-"+type+"-synch3.pdf",null);
		//RPlotter.drawBoxplot(lvalues_caps,ln,"caps",USE_FEATURE,Config.getInstance().base_folder+"/Images/boxplot-caps-"+type+"-"+USE_FEATURE+".pdf",null);
		RPlotter.drawBoxplot(lvalues_comuni2012,ln,"comuni2012",USE_FEATURE,Config.getInstance().base_folder+"/Images/boxplot-comuni2012-"+type+"-"+USE_FEATURE+".pdf",null);
		RPlotter.drawBoxplot(lvalues_comuni2014,ln,"comuni2014",USE_FEATURE,Config.getInstance().base_folder+"/Images/boxplot-comuni2014-"+type+"-"+USE_FEATURE+".pdf",null);
		RPlotter.drawBoxplot(lvalues_prov2011,ln,"prov2011",USE_FEATURE,Config.getInstance().base_folder+"/Images/boxplot-prov2011-"+type+"-"+USE_FEATURE+".pdf",null);
		RPlotter.drawBoxplot(lvalues_regioni,ln,"regioni",USE_FEATURE,Config.getInstance().base_folder+"/Images/boxplot-regioni-"+type+"-"+USE_FEATURE+".pdf",null);
		
		
		Deprivation dp = Deprivation.getInstance();
		plotCorrelation(all_density_regioni,dp.getDepriv(),USE_FEATURE,"deprivazione",Config.getInstance().base_folder+"/Images/"+USE_FEATURE+"-deprivazione.pdf");
		
		//for(String k: all_density_regioni.keySet())
		//	System.out.println("deprivazione..................... "+k+""+" "+all_density_regioni.get(k)+" .... "+dp.getDepriv().get(k));
		
		Map<String,String> id2name = MEF_IRPEF_BLOG.id2name();
		MEF_IRPEF mi = MEF_IRPEF.getInstance();
		plotCorrelation(all_density_comuni2014,mi.redditoPC(false),USE_FEATURE,"reddito PC",Config.getInstance().base_folder+"/Images/"+USE_FEATURE+"-redPC.pdf",id2name);
		plotCorrelation(all_density_comuni2014,mi.gini(false),USE_FEATURE,"Gini",Config.getInstance().base_folder+"/Images/"+USE_FEATURE+"-gini.pdf",id2name);
		
		
		IstatCensus2011 ic = IstatCensus2011.getInstance();
		int[] indices = new int[]{46,51,59,61};
		for(int i: indices)
			plotCorrelation(all_density_comuni2012,ic.computeDensity(i, true, false),USE_FEATURE,IstatCensus2011.DIMENSIONS[i],Config.getInstance().base_folder+"/Images/"+USE_FEATURE+"-"+IstatCensus2011.DIMENSIONS[i]+".pdf",id2name);
		
		
		SocialCapital sc = SocialCapital.getInstance();
		plotCorrelation(all_density_prov2011,sc.getAssoc(),USE_FEATURE,"assoc",Config.getInstance().base_folder+"/Images/"+USE_FEATURE+"-assoc.pdf");
		plotCorrelation(all_density_prov2011,sc.getReferendum(),USE_FEATURE,"referendum",Config.getInstance().base_folder+"/Images/"+USE_FEATURE+"-referendum.pdf");
		plotCorrelation(all_density_prov2011,sc.getBlood(),USE_FEATURE,"blood",Config.getInstance().base_folder+"/Images/"+USE_FEATURE+"-blood.pdf");
		
		//for(String k: all_density_prov2011.keySet())
		//	System.out.println("blood..................... "+k+""+" "+all_density_prov2011.get(k)+" .... "+sc.getBlood().get(k));
		
	}
	
	
	private static double[] density2array(Map<String,Double> density) {
		double[] y = new double[density.size()];
		int c = 0;
		for(double v: density.values())
			y[c++] = v; 
		return y;
	}
 	
	private static final DecimalFormat DF = new DecimalFormat("0.00",new DecimalFormatSymbols(Locale.US));
	private static final boolean LM = false;
	
	
	public static void plotCorrelation(Map<String,Double> mapx, Map<String,Double> mapy, String titx, String tity, String file) {
		plotCorrelation(mapx,mapy,titx,tity,file,null);
	}
	
	public static void plotCorrelation(Map<String,Double> mapx, Map<String,Double> mapy, String titx, String tity, String file, Map<String,String> name2name) {
		
		
		List<Double> lx = new ArrayList<Double>();
		List<Double> ly = new ArrayList<Double>();
		List<String> labels = new ArrayList<String>();
		
		for(String k: mapx.keySet()) {
			if(k.equals("97091")) continue;
			double vx = mapx.get(k);
			
			//if(vx < 1.2 || vx > 1.7) continue;
			
			Double vy = mapy.get(k);
			if(vy != null) {
				lx.add(vx);
				ly.add(vy);
				String ll = k.replace("'", "");
				if(name2name!=null && name2name.get(ll) != null) ll =  name2name.get(ll).toUpperCase().split("-")[0];
				labels.add(ll);
			}
		}
		
		double[] x = new double[lx.size()];
		double[] y = new double[ly.size()];
		String[] l = new String[labels.size()];
		for(int i=0; i<x.length;i++) {
			x[i] = lx.get(i);
			y[i] = ly.get(i);
			l[i] = labels.get(i);
		}
		
		//System.out.println(titx+" map = "+mapx.size()+" array = "+x.length);
		//System.out.println(tity+" map = "+mapy.size()+" array = "+y.length);
		
		double r = StatsUtils.r(x, y);
		String r2 = "annotate(\"text\", parse=TRUE, size=10, fontface='bold', x="+(r > 0 ? "-" : "")+"Inf, y=Inf, label=\"r^2 == "+DF.format(r*r)+"\", hjust="+(r > 0 ? "-0.2" : "1.2")+", vjust=1.2)";
		
		//RPlotter.drawScatter(x,y,titx, tity, file, "stat_smooth("+(LM?"":"method=lm,")+"colour='black') + theme(legend.position='none') + geom_point(size = 5) + "+r2);
		RPlotter.drawScatterWLabels(x,y,l,titx, tity, file, "stat_smooth("+(LM?"":"method=lm,")+"colour='black') + theme(legend.position='none') + "+r2);

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
		
		System.out.println(">>>>> REPROJECT "+city+" "+type+" FROM "+rm_from.getName()+" TO "+rm_to.getName());
		
		
		Map<String,String> id2name = MEF_IRPEF_BLOG.id2name();
		
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
				if(name.equals("20078")) name_to = "LODI";
				else if(name.equals("30121")) name_to = "VENEZIA";
				else if(name.equals("80058")) name_to = "NAPOLI";
				else if(name.equals("80053")) name_to = "NAPOLI";
				else if(name.equals("80076")) name_to = "NAPOLI";
				else if(name.equals("80075")) name_to = "NAPOLI";
				else if(name.equals("80074")) name_to = "NAPOLI";
				else if(name.equals("80073")) name_to = "NAPOLI";
				else if(name.equals("80071")) name_to = "NAPOLI";
				else if(name.equals("80079")) name_to = "NAPOLI";
				else if(name.equals("80077")) name_to = "NAPOLI";		
			}
			if(rm_to.getName().contains("regioni")) {
				if(name.equals("30121")) name_to = "VENETO";
				else if(name.equals("80058")) name_to = "CAMPANIA";
				else if(name.equals("80053")) name_to = "CAMPANIA";
				else if(name.equals("80076")) name_to = "CAMPANIA";
				else if(name.equals("80075")) name_to = "CAMPANIA";
				else if(name.equals("80074")) name_to = "CAMPANIA";
				else if(name.equals("80073")) name_to = "CAMPANIA";
				else if(name.equals("80071")) name_to = "CAMPANIA";
				else if(name.equals("80079")) name_to = "CAMPANIA";
				else if(name.equals("80077")) name_to = "CAMPANIA";			
			}
			if(name_to == null) {
				RegionI r = rm_from.getRegion(name);
				RegionI r_to = rm_to.get(r.getLatLon()[1], r.getLatLon()[0]);
				
				if(r_to == null) {
					//System.err.println(name+" -->" + r.getLatLon()[0]+","+r.getLatLon()[1]);
					name_to = null;
				}
				else name_to = r_to.getName();
			}
			
			if(rm_to.getName().contains("comuni")) {
				if(CAPOLUOGO_ONLY) {
					String cname = id2name.get(name_to);
					if(cname == null) name_to = null;
					else if(!cname.toLowerCase().startsWith(city.toLowerCase())) name_to = null;
				}
			}
			
			
			if(rm_to.getName().contains("regioni")) {
				if(name_to.equals("BASILICATA")) name_to = "PUGLIA";
				else if(name_to.equals("UMBRIA")) name_to = "LAZIO";
				
				if(city.equals("milano") && !name_to.equals("LOMBARDIA"))
					name_to = null;
			}
			
			
			if(rm_to.getName().contains("prov")) {
				//if(name_to.equals("VERCELLI")) {
				//	name_to = city.equals("torino") ? "TORINO" : null;  // come mai se metto 1TORINO influneza così tanto?
					
					//System.out.println("------- was vercelli now torino");
				//}
				if(name_to!=null && !name_to.equalsIgnoreCase(city)) name_to = null;
				
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
				rm_to.setName(city+"-"+type+"-"+USE_FEATURE+"-reprojected");
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
				corr+=computeFeature( td.get(regions[i]), td.get(regions[j]));
				
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
			for(int b=a+1;b<n;b++) 
				corrs[c++] = computeFeature(tds.get(a).get(regions[i]),tds.get(b).get(regions[i]));
			
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
	
	
	private static double computeFeature(double[] series1, double[] series2) {
		try {
			TimeConverter tc = TimeConverter.getInstance();
			if(USE_FEATURE.equals(F_AVGZH)) {
				double[] zetam1 = StatsUtils.getZmH(series1,tc);
				double[] zetam2 = StatsUtils.getZmH(series2,tc);
				return avg(filter(zetam1,tc)) * avg(filter(zetam2,tc));
			}
			else if(USE_FEATURE.equals(F_ENTROPY_LAG)) {
				double[][] ls1 = TestEntropy.createLagged(bin(series1,4), 24*7);
				double[][] ls2 = TestEntropy.createLagged(bin(series2,4), 24*7);
				
				//double[][] ls1 = TestEntropy.createLagged(bin(filter(series1,tc),10), 10);
				//double[][] ls2 = TestEntropy.createLagged(bin(filter(series2,tc),10), 10);
				
				
				double e1 = Entropy.calculateConditionalEntropy(ls1[0],ls1[1]);
				double e2 = Entropy.calculateConditionalEntropy(ls2[0],ls2[1]);
				return e1;
				
				//return Entropy.calculateConditionalEntropy(bin(filter(series1,tc),10),bin(filter(series2,tc),10));
			}
			else if(USE_FEATURE.equals(F_ENTROPY)) {
				//double e3 = Entropy.calculateConditionalEntropy(bin(filter(series1,tc),10),bin(filter(series2,tc),10));
				double e3 = Entropy.calculateConditionalEntropy(bin(series1,10),bin(series2,10));
				return e3;
			}
			else if(USE_FEATURE.equals(F_ENTROPY_CORRECTED)) {
				double[][] ls1 = TestEntropy.createLagged(bin(series1,10), 24*7);
				double[][] ls2 = TestEntropy.createLagged(bin(series2,10), 24*7);
				
				//double[][] ls1 = TestEntropy.createLagged(bin(filter(series1,tc),10), 10);
				//double[][] ls2 = TestEntropy.createLagged(bin(filter(series2,tc),10), 10);
				
				
				double e1 = Entropy.calculateConditionalEntropy(ls1[0],ls1[1]);
				if(e1 == 0) e1 = 1; // come mai a roma viene entropy = 0?
				
				double e2 = Entropy.calculateConditionalEntropy(ls2[0],ls2[1]);
				if(e2 == 0) e2 = 1; // come mai a roma viene entropy = 0?
				
				double e3 = Entropy.calculateConditionalEntropy(ls1[0],ls2[0]);
				return e3/(e1+e2);
				
				// una cosa simile a livello di concetto la ottengo mettendo una regressione lineaere (multipla?) quanto più è alta l'entropia tanto più la regressione linerare sbaglia
				
			}
			else if(USE_FEATURE.equals(F_GRANGER)) {
				return Math.sqrt(GrangerTest.granger(filter(series1,tc), filter(series2,tc), 24)[2]); // pValue,ftest,h0.calculateRSquared(),h1.calculateRSquared()
			}
			else if(USE_FEATURE.equals(F_R)) {
				return StatsUtils.r(filter(series1,tc),filter(series2,tc));
			}
			else if(USE_FEATURE.equals(F_R2)) {
				return StatsUtils.r2(filter(series1,tc),filter(series2,tc));
			}
			
			else if(USE_FEATURE.equals(F_R2_CORR)) {
				
				double[] zetam1 = StatsUtils.getZmH(series1,tc);
				double[] zetam2 = StatsUtils.getZmH(series2,tc);
				double avgzh = avg(filter(zetam1,tc)) * avg(filter(zetam2,tc));
				double r2 = StatsUtils.r2(filter(series1,tc),filter(series2,tc));
				return avgzh * r2;
			}
				
			
			else {
				throw (new Exception("Unknown feature"));
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		return 0;
		
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
