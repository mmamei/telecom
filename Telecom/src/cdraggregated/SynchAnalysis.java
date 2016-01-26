package cdraggregated;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import otherdata.TIbigdatachallenge2015.Deprivation;
import otherdata.TIbigdatachallenge2015.IstatCensus2011;
import otherdata.TIbigdatachallenge2015.MEF_IRPEF;
import otherdata.TIbigdatachallenge2015.MEF_IRPEF_BLOG;
import otherdata.TIbigdatachallenge2015.SocialCapital;
import region.RegionI;
import region.RegionMap;
import utils.AddMap;
import utils.AddMapL;
import utils.Config;
import utils.CopyAndSerializationUtils;
import utils.StatsUtils;
import utils.time.TimeConverter;
import visual.html.GoogleChartGraph;
import visual.kml.KMLHeatMap;
import visual.r.RPlotter;
import JavaMI.Entropy;

public class SynchAnalysis {
	
	/*
	 * Fare un bello studio sulla composizione in termini di caos del traffico generato in ogni comune.
	 * Potremmo misurare l'entropia di quella distribuzione e vedre quanta eterogeneità c'è in quella zona.
	 * Anche quetsa feature si potrebbe correlare con i dati economici.
	 * nota: i caps potrebbero essere aggragti a livello di comune-privincia-regione in modo da avere distribuzioni migliori.
	 * forse è meglio fare un altra classe ed agire su time density from aggregated data
	 */
	

	public static boolean CAPOLUOGO_ONLY = true; 
	
	public static boolean PRINT_CORR_MATRIX = false;
	
	public static final String[] COMPANY_CONSTRAINTS = null;//new String[]{"01-32","grande",""};
	public static final int LIMIT = -1;
	
	
	private enum Analysis {CALLOUT_IT,CALLOUT_IT_VS_NON_IT,DEMOGRAPHIC_RES,DEMOGRAPHIC_RES_VS_NON_RES,DEMOGRAPHIC_IT_VS_NON_IT,DEMOGRAPHIC_ALL};
	//public static Analysis TYPE = Analysis.DEMOGRAPHIC_RES_VS_NON_RES;
	public static Analysis TYPE = Analysis.DEMOGRAPHIC_RES;

	private enum Feature {RSQ, I};
	public static final Feature USEF = Feature.RSQ;
	
	
	public static boolean KML_PLOT_Z = true;
	
	//This parameter is a threshold on the synch region. It is used with DEMOGRAPHIC_RES
	//Only regions with an avg value above this threshold are considered
	public static int DEMOGRAPHIC_RES_THRESHOLD = 200;
	
	//These parameters are thresholds on the synch region. It is used with DEMOGRAPHIC_RES_VS_NON_RES
	//Only regions with an avg value above these thresholds are considered.
	//DEMOGRAPHIC_RES_THRESHOLD is used for residetns. DEMOGRAPHIC_NOT_RES_THRESHOLD is used for non residents
	public static int  DEMOGRAPHIC_NOT_RES_THRESHOLD = 20;
	
	public static void main(String[] args) throws Exception {
		batch();
		//go();
	}
	
	public static void batch() throws Exception {
		PrintWriter out = null;
		
		/*
		TYPE = Analysis.DEMOGRAPHIC_RES;
		out = new PrintWriter(new FileWriter(Config.getInstance().base_folder+"/TIC2015/batch_demographic_res.csv"));
		for(DEMOGRAPHIC_RES_THRESHOLD = 50; DEMOGRAPHIC_RES_THRESHOLD <= 600;DEMOGRAPHIC_RES_THRESHOLD+=50) {
			List<Double> all_r2 = go();
			out.print(DEMOGRAPHIC_RES_THRESHOLD);
			for(double r: all_r2)
				out.print(";"+r);
			out.println();
		}
		out.close();
		*/
		
		TYPE = Analysis.DEMOGRAPHIC_RES_VS_NON_RES;
		out = new PrintWriter(new FileWriter(Config.getInstance().base_folder+"/TIC2015/batch_demographic_res_not_res.csv"));
		for(DEMOGRAPHIC_RES_THRESHOLD = 50; DEMOGRAPHIC_RES_THRESHOLD <= 600;DEMOGRAPHIC_RES_THRESHOLD+=50) {
			List<Double> all_r2 = go();
			out.print(DEMOGRAPHIC_RES_THRESHOLD);
			for(double r: all_r2)
				out.print(";"+r);
			out.println();
		}
		out.close();
	}
	
	public static List<Double> go() throws Exception {
		//String[] city = new String[]{"torino","milano","venezia","roma","napoli","bari","palermo"};
		
		String[] city = new String[]{
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
		
		String[] files = new String[city.length];
		
		Map<String, List<SynchConstraints>> map_constraints = new HashMap<String,List<SynchConstraints>>();
		String type = "";
		int[] readIndexes = null;
		
		Map<String,String> id2capolouogo = new HashMap<String,String>();
		id2capolouogo.put("torino", "1272");
		id2capolouogo.put("milano", "15146");
		id2capolouogo.put("venezia", "27042");
		id2capolouogo.put("roma", "58091");
		id2capolouogo.put("napoli", "63049");
		id2capolouogo.put("bari", "72006");
		id2capolouogo.put("palermo", "82053");

		id2capolouogo.put("benevento", "62008");
		id2capolouogo.put("caltanissetta", "85004");
		id2capolouogo.put("modena", "36023");
		id2capolouogo.put("siena", "52032");
		id2capolouogo.put("siracusa", "89017");
		id2capolouogo.put("asti", "5005");
		id2capolouogo.put("campobasso", "70006");
		id2capolouogo.put("ferrara", "38008");
		id2capolouogo.put("ravenna", "39014");
		
		
		
		
		if(TYPE.equals(Analysis.CALLOUT_IT)) {
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
		
		if(TYPE.equals(Analysis.CALLOUT_IT_VS_NON_IT)) {
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
		
		if(TYPE.equals(Analysis.DEMOGRAPHIC_RES)) {
			type = "Demo";
			readIndexes = new int[]{0,1,3,2};
			
			for(int i=0; i<city.length;i++)
				files[i] = Config.getInstance().dataset_folder+"/TI-CHALLENGE-2015/DEMOGRAPHIC/"+city[i]+"/callsLM_"+city[i].substring(0,2).toUpperCase()+"_COMUNI2012";
			
			for(int i=0; i<city.length;i++) {
				List<SynchConstraints> constraints = new ArrayList<SynchConstraints>();
				
				Set<String> caps = getComuni2012FromCity(city[i]);
				
				//Set<String> caps = new HashSet<String>();
				//caps.add(id2name.get(city[i]));
				
				
				constraints.add(new SynchConstraints("resident",caps));
				//constraints.add(new SynchConstraints("not-resident",caps,true));
				map_constraints.put(city[i],constraints);
			}
		}
		
		if(TYPE.equals(Analysis.DEMOGRAPHIC_RES_VS_NON_RES)) {
			type = "Demo";
			readIndexes = new int[]{0,1,3,2};
			
			for(int i=0; i<city.length;i++)
				files[i] = Config.getInstance().dataset_folder+"/TI-CHALLENGE-2015/DEMOGRAPHIC/"+city[i]+"/callsLM_"+city[i].substring(0,2).toUpperCase()+"_COMUNI2012";
			
			for(int i=0; i<city.length;i++) {
				List<SynchConstraints> constraints = new ArrayList<SynchConstraints>();
				
				Set<String> caps = getComuni2012FromCity(city[i]);
				
				//Set<String> caps = new HashSet<String>();
				//caps.add(id2name.get(city[i]));
				
				constraints.add(new SynchConstraints("resident",caps));
				constraints.add(new SynchConstraints("not-resident",caps,true));
				map_constraints.put(city[i],constraints);
			}
		}
		
		
		if(TYPE.equals(Analysis.DEMOGRAPHIC_IT_VS_NON_IT)) {
			type = "Demo";
			readIndexes = new int[]{0,1,3,2};
			
			for(int i=0; i<city.length;i++)
				files[i] = Config.getInstance().dataset_folder+"/TI-CHALLENGE-2015/DEMOGRAPHIC/"+city[i]+"/callsLM_"+city[i].substring(0,2).toUpperCase()+"_COMUNI2012";
			
			for(int i=0; i<city.length;i++) {
				List<SynchConstraints> constraints = new ArrayList<SynchConstraints>();
				constraints.add(new SynchConstraints("0"));
				constraints.add(new SynchConstraints("0",true));
				map_constraints.put(city[i],constraints);
			}
		}
		
		
		if(TYPE.equals(Analysis.DEMOGRAPHIC_ALL)) {
			type = "Demo";
			readIndexes = new int[]{0,1,3,2};
			
			for(int i=0; i<city.length;i++)
				files[i] =Config.getInstance().dataset_folder+"/TI-CHALLENGE-2015/DEMOGRAPHIC/"+city[i]+"/callsLM_"+city[i].substring(0,2).toUpperCase()+"_COMUNI2012";
			
			for(int i=0; i<city.length;i++) {
				List<SynchConstraints> constraints = new ArrayList<SynchConstraints>();
				
				//Set<String> caps = getComuni2012FromCity(city[i]);
				
				Set<String> caps = new HashSet<String>();
				caps.add(id2capolouogo.get(city[i]));
				
				
				
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
		
		List<double[]> lvalues_comuni2012 = new ArrayList<double[]>();
		List<double[]> lvalues_comuni2014 = new ArrayList<double[]>();
		List<double[]> lvalues_prov2011 = new ArrayList<double[]>();
		List<double[]> lvalues_regioni = new ArrayList<double[]>();
		
		AddMapL all_density_comuni2012 = new AddMapL();
		AddMapL all_density_comuni2014 = new AddMapL();
		AddMapL all_density_prov2011 = new AddMapL();
		AddMapL all_density_regioni = new AddMapL();
		
		
		RegionMap prov2011 = (RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/tic-prov2011.ser"));
		RegionMap regioni = (RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/regioni.ser"));
		
		
		
		for(int i=0; i<city.length;i++) {
			
			System.out.println("\n\n*************************************************** START PROCESSING "+city[i].toUpperCase());
			
			
			names[i] = city[i].substring(0, 1).toUpperCase() + city[i].substring(1,3); // capitalize first letter and consider only the first two letters
			
			RegionMap rm_comuni2012 = (RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/tic-comuni2012-"+city[i]+".ser"));
			List<TimeDensityFromAggregatedData> tds_comuni2012 = loadTimeDensityFromAggregatedData(city[i],type,files[i],readIndexes,rm_comuni2012,map_constraints.get(city[i]));
			Map<String,List<Double>> density_comuni2012 = process(tds_comuni2012);
			
			
			RegionMap rm_comuni2014 = (RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/tic-comuni2014-"+city[i]+".ser"));
			List<TimeDensityFromAggregatedData> tds_comuni2014 = loadTimeDensityFromAggregatedData(city[i],type,files[i],readIndexes,rm_comuni2014,map_constraints.get(city[i])); 
			Map<String,List<Double>> density_comuni2014 = process(tds_comuni2014);
			
			Map<String,List<Double>> density_prov2011 = null;
			Map<String,List<Double>> density_regioni = null;
			
			
			
			if(TYPE.equals(Analysis.DEMOGRAPHIC_RES)) {
				density_prov2011 = reproject2map(density_comuni2012,city[i],rm_comuni2012,prov2011);
				density_regioni = reproject2map(density_comuni2012,city[i],rm_comuni2012,regioni);
			}
			else {
				density_prov2011 = process(reproject2map(tds_comuni2012,prov2011));
				density_regioni = process(reproject2map(tds_comuni2012,regioni));
				
			}
			

			
			if(CAPOLUOGO_ONLY) {
				// keep only the capoluogo
				List<Double> value2012 =  density_comuni2012.get(id2capolouogo.get(city[i]));
				List<Double> value2014 =  density_comuni2014.get(id2capolouogo.get(city[i]));
				density_comuni2012.clear();
				density_comuni2014.clear();
				density_comuni2012.put(id2capolouogo.get(city[i]), value2012);
				density_comuni2014.put(id2capolouogo.get(city[i]), value2014);
			}
			
			
			
			all_density_comuni2012.addAll(density_comuni2012);
			all_density_comuni2014.addAll(density_comuni2014);
			all_density_prov2011.addAll(density_prov2011);
			all_density_regioni.addAll(density_regioni);
			
			ln.add(names[i]);
			
			lvalues_comuni2012.add(density2array(density_comuni2012));
			lvalues_comuni2014.add(density2array(density_comuni2014));
			lvalues_prov2011.add(density2array(density_prov2011));
			lvalues_regioni.add(density2array(density_regioni));
			
			
			System.out.println("*************************************************** END PROCESSING "+city[i].toUpperCase());
			
		}
		
		// regioni is the only one that can have multiple data. Therefore it is important to compute the mean otherwise I can have R^2 > 1
		//all_density_regioni.mean();
		
		RPlotter.drawBoxplot(lvalues_comuni2012,ln,"comuni2012",USE_FEATURE,Config.getInstance().base_folder+"/Images/boxplot-comuni2012-"+type+"-"+USE_FEATURE.substring(0,1)+".png",20,null);
		//RPlotter.drawBoxplot(lvalues_comuni2014,ln,"comuni2014",USE_FEATURE,Config.getInstance().base_folder+"/Images/boxplot-comuni2014-"+type+"-"+USE_FEATURE.substring(0,1)+".png",20,null);
		RPlotter.drawBoxplot(lvalues_prov2011,ln,"provinces",USE_FEATURE,Config.getInstance().base_folder+"/Images/boxplot-prov2011-"+type+"-"+USE_FEATURE.substring(0,1)+".png",20,null);
		//RPlotter.drawBoxplot(lvalues_regioni,ln,"regions",USE_FEATURE,Config.getInstance().base_folder+"/Images/boxplot-regioni-"+type+"-"+USE_FEATURE.substring(0,1)+".png",20,null);
		
		//if(!CAPOLUOGO_ONLY) System.exit(0);
		
		//System.exit(0);
		
		
		List<Double> all_r2 = new ArrayList<Double>();
		
		Deprivation dp = Deprivation.getInstance();
		plotCorrelation(avg(all_density_regioni),dp.getDepriv(),USE_FEATURE,"deprivation",Config.getInstance().base_folder+"/Images/"+USE_FEATURE.substring(0,1)+"-deprivazione.png",null,true);
		
		
		
		//for(String k: all_density_regioni.keySet())
		//	System.out.println("deprivazione..................... "+k+""+" "+all_density_regioni.get(k)+" .... "+dp.getDepriv().get(k));
		
		Map<String,String> id2name = MEF_IRPEF_BLOG.id2name();
		MEF_IRPEF mi = MEF_IRPEF.getInstance();
		all_r2.add(plotCorrelation(avg(all_density_comuni2014),mi.redditoPC(false),USE_FEATURE,"pro-capita income",Config.getInstance().base_folder+"/Images/"+USE_FEATURE.substring(0,1)+"-redPC.png",id2name,CAPOLUOGO_ONLY));
		//plotCorrelation(all_density_comuni2014,mi.gini(false),USE_FEATURE,"Gini",Config.getInstance().base_folder+"/Images/"+USE_FEATURE+"-gini.pdf",id2name);
		
		
		IstatCensus2011 ic = IstatCensus2011.getInstance();
		int[] indices = new int[]{46,51,59,61};
		for(int i: indices)
			all_r2.add(plotCorrelation(avg(all_density_comuni2012),ic.computeDensity(i, true, false),USE_FEATURE,IstatCensus2011.DIMENSIONS[i],Config.getInstance().base_folder+"/Images/"+USE_FEATURE.substring(0,1)+"-"+IstatCensus2011.DIMENSIONS[i]+".png",id2name,CAPOLUOGO_ONLY));
		
		
		SocialCapital sc = SocialCapital.getInstance();
		all_r2.add(plotCorrelation(avg(all_density_prov2011),sc.getAssoc(),USE_FEATURE,"assoc",Config.getInstance().base_folder+"/Images/"+USE_FEATURE.substring(0,1)+"-assoc.png",null,true));
		all_r2.add(plotCorrelation(avg(all_density_prov2011),sc.getReferendum(),USE_FEATURE,"referendum",Config.getInstance().base_folder+"/Images/"+USE_FEATURE.substring(0,1)+"-referendum.png",null,true));
		all_r2.add(plotCorrelation(avg(all_density_prov2011),sc.getBlood(),USE_FEATURE,"blood",Config.getInstance().base_folder+"/Images/"+USE_FEATURE.substring(0,1)+"-blood.png",null,true));
		
		//for(String k: all_density_prov2011.keySet())
		//	System.out.println("blood..................... "+k+""+" "+all_density_prov2011.get(k)+" .... "+sc.getBlood().get(k));
		
		
		//Map<String,Double> company_size_entropy = new HashMap<String,Double>();
		//company_size_entropy.put("TORINO",0.5508074048631459);
		//company_size_entropy.put("MILANO",0.6672230369732484);
		//company_size_entropy.put("VENEZIA",0.7115197865582921);
		//company_size_entropy.put("ROMA",0.5005666299625349);
		//company_size_entropy.put("NAPOLI",0.4354609564522476);
		//company_size_entropy.put("BARI",0.5043454658301506);
		//company_size_entropy.put("PALERMO",0.4642571074232869);
		//plotCorrelation(all_density_prov2011,company_size_entropy,USE_FEATURE,"companies size entropy",Config.getInstance().base_folder+"/Images/"+USE_FEATURE.substring(0,1)+"-companysizeentropy.png");
		
		return all_r2;
		
	}
	
	
	public static double[] density2array(Map<String,List<Double>> density) {
		
		List<Double> y = new ArrayList<Double>();
		int c = 0;
		for(List<Double> v: density.values())
			y.addAll(v);
		
		double[] yy = new double[y.size()];
		for(int i=0;i<yy.length;i++)
			yy[i] = y.get(i);
		return yy;
	}
 	
	private static final DecimalFormat DF = new DecimalFormat("0.00",new DecimalFormatSymbols(Locale.US));
	private static final boolean LM = false;
	
	
	public static double plotCorrelation(Map<String,Double> mapx, Map<String,Double> mapy, String titx, String tity, String file, Map<String,String> name2name, boolean use_labels) {
		
		
		List<Double> lx = new ArrayList<Double>();
		List<Double> ly = new ArrayList<Double>();
		List<String> labels = new ArrayList<String>();
		
		for(String k: mapx.keySet()) {
			//if(k.equals("97091")) continue;
			double vx = mapx.get(k);
			
			//if(vx < 1  || vx > 10.7) continue;
			
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
		double r2v = StatsUtils.r2(x, y);
		String r2 = "annotate(\"text\", parse=TRUE, size=10, fontface='bold', x="+(r > 0 ? "-" : "")+"Inf, y=Inf, label=\"r^2 == "+DF.format(r2v)+"\", hjust="+(r > 0 ? "-0.2" : "1.2")+", vjust=1.2)";
		
		if(use_labels) RPlotter.drawScatterWLabels(x,y,l,titx, tity, file, "stat_smooth("+(LM?"":"method=lm,")+"colour='black') + theme(legend.position='none') + "+r2);
		else RPlotter.drawScatter(x,y,titx, tity, file,"stat_smooth("+(LM?"":"method=lm,")+"colour='black') + theme(legend.position='none') + geom_point(alpha=0.2,size = 5) + "+r2);
		
		return r2v;

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
	
	
	static HashMap<String,String> city2region = new HashMap<String,String>();
	static {
		city2region.put("torino", "PIEMONTE");
		city2region.put("milano", "LOMBARDIA");
		city2region.put("venezia", "VENETO");
		city2region.put("roma", "LAZIO");
		city2region.put("napoli", "CAMPANIA");
		city2region.put("bari", "PUGLIA");
		city2region.put("palermo", "SICILIA");
		
		city2region.put("campobasso", "MOLISE");
		city2region.put("siracusa", "SICILIA");
		city2region.put("benevento", "CAMPANIA");
		city2region.put("caltanissetta", "SICILIA");
		city2region.put("modena", "EMILIA-ROMAGNA");
		city2region.put("siena", "TOSCANA");
		city2region.put("asti", "PIEMONTE");
		city2region.put("ferrara", "EMILIA-ROMAGNA");
		city2region.put("ravenna", "EMILIA-ROMAGNA");
	}
	
	static HashMap<String,String> city2province = new HashMap<String,String>();
	static {
	for(String province: new String[]{"torino","milano","venezia","roma","napoli","bari","palermo","campobasso","siracusa","benevento","caltanissetta","modena","siena","asti","ferrara","ravenna"})
		city2province.put(province,province.toUpperCase());
	}
	
	private static Map<String,List<Double>> reproject2map(Map<String,List<Double>> map, String city, RegionMap rm_from, RegionMap rm_to) {
		System.out.println(">>>>> REPROJECT DENSITY "+city+" FROM "+rm_from.getName()+" TO "+rm_to.getName());
		AddMapL res = new AddMapL();
		
		
		for(String f : map.keySet()) {
			List<Double> value = map.get(f);
			
			
			if(rm_to.getName().contains("regioni")) {
				String region = city2region.get(city);
				if(region!=null) res.add(region, value); 
				else System.err.println("ERROR IN "+city);
			}
			
			
			if(rm_to.getName().contains("prov2011")) {
				String province = city2province.get(city);
				if(province!=null) res.add(province, value); 
				else System.err.println("ERROR IN "+city);
			}
			
		}
		/*
		for(String r: res.keySet()) 
			res.put(r, res.get(r) / map.size());
		*/
		return res;
	}
	
	
	private static List<TimeDensityFromAggregatedData> reproject2map(List<TimeDensityFromAggregatedData> tds,RegionMap rm_to) {
		System.out.println(">>>>> REPROJECT "+tds.get(0).getCity()+" FROM "+tds.get(0).rm.getName()+" TO "+rm_to.getName());
		
		
		List<TimeDensityFromAggregatedData> res = new ArrayList<TimeDensityFromAggregatedData>();
		
		
		for(TimeDensityFromAggregatedData td: tds) {
			res.add(td.reproject2Map(rm_to));
		}
		
		
		return res;
	}
	
	
	private static Set<String> getComuni2012FromCity(String city) {
		Set<String> comuni = new HashSet<String>();
		RegionMap rm = (RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/tic-comuni2012-"+city+".ser"));
		for(RegionI r: rm.getRegions()) {
			String name = r.getName();
			if(name.indexOf("_")>0) {
				//System.out.print(name+"  -->  ");
				name = name.substring(0,name.indexOf("_"));
				//System.out.println(name);
			}
			comuni.add(name);
		}
		return comuni;
	}
	
	
	
	public static List<TimeDensityFromAggregatedData> loadTimeDensityFromAggregatedData(String city, String type, String file, int[] readIndexes, RegionMap rm, List<SynchConstraints> constraints) {
		List<TimeDensityFromAggregatedData> tds = new ArrayList<TimeDensityFromAggregatedData>();
		for(SynchConstraints constraint : constraints) {
			TimeDensityFromAggregatedData td = new TimeDensityFromAggregatedData(city,type,file,readIndexes,constraint,rm);
			System.out.println(td.getType()+" use map "+rm.getName());
			tds.add(td);
		}
		return tds;
	}
	
	
	public static Map<String,List<Double>> process(List<TimeDensityFromAggregatedData> tds) throws Exception {
			
		
		String suffix = LIMIT > 0 ? "limited to "+LIMIT : "";
		System.out.println("processign "+tds.get(0).getCity()+" -- regions size --> "+tds.get(0).rm.getNumRegions()+" "+suffix);
		for(int i=0; i<tds.size();i++)
			System.out.println("	processing "+tds.get(i).getCity()+" -- "+tds.get(i).getType()+" size --> "+tds.get(i).size()+" "+suffix);
		
		//LoadDensityFromAggregatedData.process(city, tds, COMPANY_CONSTRAINTS, LIMIT);
		
		
		String[] regions = new String[LIMIT > 0 ? Math.min(LIMIT, tds.get(0).rm.getNumRegions()) : tds.get(0).rm.getNumRegions()];
		System.out.println(tds.get(0).getCity()+" regions = "+regions.length);
		int i=0;
		for(RegionI r : tds.get(0).rm.getRegions()) {
			regions[i] = r.getName();
			i++;
			if(LIMIT > 0 && i >= LIMIT) break;
		}
		
		if(tds.size() == 1) return getDistCorr(tds.get(0).getCity(),regions,tds.get(0));
		else return getDistCorr(tds.get(0).getCity(),regions,tds);
	}
	
	
	
	// Questo si usa per DEMOGRAPHIC_RES *************************************************************************************************************************************************
	
	
	public static Map<String,List<Double>> getDistCorr(String city, String[] regions, TimeDensityFromAggregatedData td) {
		
		
		Map<String,List<Double>> density = new HashMap<String,List<Double>>();
		
		int tot = 0; // used for printing
		
		for(int i=0; i<regions.length;i++) {
					
			List<Double> corrs = new ArrayList<Double>();
			
			for(int j=0;j<regions.length;j++) {
				if(i==j) continue;
				
				if(td.get(regions[i])!=null && td.get(regions[j]) != null) {
					
					double[] seriesa = td.get(regions[i]);
					double[] seriesb = td.get(regions[j]);
					double avga = avg(seriesa);
					double avgb = avg(seriesb);
					if(avga > DEMOGRAPHIC_RES_THRESHOLD && avgb > DEMOGRAPHIC_RES_THRESHOLD) {
						
						//if(regions[i].equals("85008") && regions[j].equals("87040"))
						//	System.out.println("here");
						
						List<Double> v = computeFeature(td.get(regions[i]), td.get(regions[j]));
						corrs.addAll(v);
						
						
						//System.out.println(regions[i]+" VS. "+regions[j]+" = "+v.get(0)+" should be 1 = "+v.size());
					}
				}
				
				//double dist = LatLonUtils.getHaversineDistance(rm.getRegion(regions[i]).getCenterPoint(),rm.getRegion(regions[j]).getCenterPoint());
			}
			
			if(corrs.size() > 0) {
				density.put(regions[i], corrs);	
				tot+= corrs.size();
			}
		}
		System.err.println(city+": N. Regions = "+regions.length +"; N. Combination = "+ regions.length*(regions.length-1)+"; Actual Comb. = "+tot);
		return density;
	}
	
	
	// Questo si usa per DEMOGRAPHIC_RES_VS_NON_RES *********************************************************************************************************************************
	
	
	
	public static Map<String,List<Double>> getDistCorr(String city, String[] regions, List<TimeDensityFromAggregatedData> tds) {
		
		Map<String,List<Double>> density = new HashMap<String,List<Double>>();
		
		
		
		Map<String,String> desc = new HashMap<String,String>();
		Map<String,List<String>> rawDesc = new HashMap<String,List<String>>();
		for(int i=0; i<regions.length;i++) {
			
			List<Double> corrs = new ArrayList<Double>();
			int c=0;
			for(int a=0; a<tds.size();a++)
			for(int b=a+1;b<tds.size();b++) {
				if(tds.get(a).get(regions[i]) != null && tds.get(b).get(regions[i]) != null) {
					double[] seriesa = tds.get(a).get(regions[i]);
					double[] seriesb = tds.get(b).get(regions[i]);
					double avga = avg(seriesa);
					double avgb = avg(seriesb);
					if(avga > DEMOGRAPHIC_RES_THRESHOLD && avgb > DEMOGRAPHIC_NOT_RES_THRESHOLD) 
						corrs.addAll(computeFeature(seriesa,seriesb));
				}
			}
			
			if(corrs.size() == 0) continue;
			
			List<String> names = new ArrayList<String>();
			List<double[]> y = new ArrayList<double[]>();
			
			for(TimeDensityFromAggregatedData td:tds) {
				if(td.get(regions[i])!=null) {
					names.add(td.getType());
					try {
						y.add(KML_PLOT_Z ? StatsUtils.getZH(td.get(regions[i]),TimeConverter.getInstance()) : td.get(regions[i]));
					} catch(Exception e) {
						e.printStackTrace();
					}
				}
			}
			
			
			String[] x = tds.get(0).tc.getTimeLabels();
			desc.put(regions[i].toLowerCase(), GoogleChartGraph.getGraph(x, y, names, "data", "y"));
			density.put(regions[i], corrs);
			rawDesc.put(regions[i], getRawDesc(names,y));
		}
		
		//RegionMap comuni = (RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/comuni"+MEF_IRPEF.COMUNI_YEAR+".ser"));
		//Map<String,String> map_converter = mapConverter(rm,comuni);
		

		try {
			String orig_name = tds.get(0).rm.getName();
			tds.get(0).rm.setName(city+"-"+tds.get(0).getType()+"-"+tds.get(0).rm.getName()+"-correlation");
			KMLHeatMap.drawHeatMap(Config.getInstance().base_folder+"/TIC2015/"+tds.get(0).rm.getName()+".kml",avg(density),tds.get(0).rm,desc,false);
			tds.get(0).rm.setName(orig_name);
			//Map<String,Double> rdensity = reproject2map(city,tds.get(0).getType()+"-correlation",density,rm,comuni,false);
			//Map<String,String> rdesc = new HashMap<String,String>();
			//for(String k: desc.keySet())
			//	rdesc.put(map_converter.get(k), desc.get(k));
			
			//KMLHeatMap.drawHeatMap(Config.getInstance().base_folder+"/TIC2015/"+city+"-"+tds.get(0).getType()+"-correlation.kml",rdensity,comuni,rdesc,false);
			
			
			//printCSV(Config.getInstance().base_folder+"/TIC2015/"+city+"-raw.csv",rawDesc,map_converter);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return density;
	}
	
	private static String USE_FEATURE = "F";
	
	
	/*
	 * The TIME_WINDOW parameter is used to divide the time series 1 and 2 into chunks of TIME_WINDOW eelememnts.
	 * chuncks are compared 2-by-2 all the results are placed in List<Double> output.
	 * If TIME_WINDOW is set to -1 all the time serie is considered at once.
	 * So, with TIME_WINDOW = -1, I compute the synchrnization of two regions for the whole data period
	 * With TIME_WINDOW = 24, I compute the synchrnization of two regions for each day (24 h) all place the values seprately on the output list
	 */
	
	private static int TIME_WINDOW = 24;
	private static List<Double> computeFeature(double[] series1, double[] series2) {
		
		
		List<Double> result = new ArrayList<Double>();
		
		try {
			TimeConverter tc = TimeConverter.getInstance();
			
			boolean Z = true;
	        boolean FILTER = false;
	        int BIN = 0;
	        
			int LAG = FILTER ? 50 : 24*7;
			
			double[] fseries1 = Z ? (StatsUtils.getZH(series1,tc)) : series1;
			double[] fseries2 = Z ? (StatsUtils.getZH(series2,tc)) : series2;
			
			fseries1 = FILTER ? filter(fseries1,tc) : fseries1;
			fseries2 = FILTER ? filter(fseries2,tc) : fseries2;
			
			
			fseries1 = BIN > 0 ? bin(fseries1,BIN) : fseries1;
			fseries2 = BIN > 0 ? bin(fseries2,BIN) : fseries2;
			
			if(TIME_WINDOW == -1) {
				result.add(reallyComputeFeature(fseries1,fseries2)); // original
			}
			else {
				double[] reduced1 = new double[TIME_WINDOW];
				double[] reduced2 = new double[TIME_WINDOW];
				
				//double num = 0;
				//double den = 0;
				
				for(int i=0; i<fseries1.length - TIME_WINDOW; i=i+TIME_WINDOW) {
					
					System.arraycopy(fseries1, i, reduced1, 0, reduced1.length);
					System.arraycopy(fseries2, i, reduced2, 0, reduced2.length);
					result.add(reallyComputeFeature(reduced1,reduced2));
					//num += reallyComputeFeature(reduced1,reduced2);
					//den ++;
				}
				//result.add(num/den);
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	
	private static double reallyComputeFeature(double[] s1, double[] s2) {
		double x = 0;
		
		if(USEF.equals(Feature.I))
			x = Entropy.calculateEntropy(s1) - Entropy.calculateConditionalEntropy(s1, s2); ; USE_FEATURE = (TYPE.equals(Analysis.DEMOGRAPHIC_RES)) ? "expression(avg[i]~I~'('~res[r]~';'~res[i!=r]~')')" : "expression(I~'(res;!res)')";
		
		if(USEF.equals(Feature.RSQ)) {
			//int L = 1;
			//OLSMultipleLinearRegression h0 = new OLSMultipleLinearRegression();
			//h0.setNoIntercept(true);
			//h0.newSampleData(GrangerTest.strip(L, s1), GrangerTest.createLaggedSide(L, s2));
			//x = h0.calculateRSquared();
		
			x = StatsUtils.r2(s1,s2);
			
			USE_FEATURE = (TYPE.equals(Analysis.DEMOGRAPHIC_RES)) ? "expression(avg[i]~bar(R)^{2}~'('~res[r]~','~res[i!=r]~')')" : "expression(bar(R)^{2}~'(res,!res)')";
		}
		return x;
	}
	
	
	private static Map<String,Double> avg(Map<String,List<Double>> x) {
		Map<String,Double> d = new HashMap<String,Double>();
		for(String k: x.keySet())
			d.put(k, avg(x.get(k)));
		return d;
	}
	
	
	private static double[] log(double[] x) {
		double[] d = new double[x.length];
		for(int i=0; i<d.length;i++)
			d[i] = Math.log(x[i]);
		return d;
	}
	
	private static double[] abs(double[] x) {
		double[] d = new double[x.length];
		for(int i=0; i<d.length;i++)
			d[i] = Math.abs(x[i]);
		return d;
	}
	
	private static double[] diff(double[] a, double[] b) {
		double[] d = new double[a.length];
		for(int i=0; i<d.length;i++)
			d[i] = Math.abs(a[i]-b[i]);
		return d;
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
	
	private static double avg(List<Double> x) {
		double avg = 0;
		for(double v: x)
			avg+=v;
		return avg / x.size();
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
