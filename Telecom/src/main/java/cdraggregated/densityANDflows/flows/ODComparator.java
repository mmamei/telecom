package cdraggregated.densityANDflows.flows;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.stat.regression.SimpleRegression;

import utils.AddMap;
import utils.Config;
import visual.r.RPlotter;
import visual.text.TextPlotter;
import cdraggregated.densityANDflows.Francia2IstatCode;
import cdraggregated.densityANDflows.ObjectID2IstatCode;
import cdraggregated.densityANDflows.ZoneConverter;
import cdraggregated.densityANDflows.flows.scale.ScaleOnResidents;

public class ODComparator {
	
	public static boolean LOG = true;
	public static boolean INTERCEPT = true;
	public static int THRESHOLD = 10;
	
	
	
	// istatH
	// 1 prima delle 7,15;
	// 2 dalle 7,15 alle 8,14;
	// 3 dalle 8,15 alle 9,14;
	// 4 dopo le 9,14;
	
	public static void main(String[] args) throws Exception {
		
		ScaleOnResidents.PLOT_DEBUG = false;
		/*
		for(int istatH=1;istatH<=4;istatH++)
		compareWIstat("ODMatrixHW_file_pls_lomb_file_pls_lomb_01-03-2014-30-03-2014_minH_0_maxH_25_ABOVE_8limit_20000_cellXHour_odlombardia",null,"Lombardia",istatH,new ObjectID2IstatCode("G:/DATASET/GEO/telecom-2015-od/odlombardia.csv"),null);
	
		for(int istatH=1;istatH<=4;istatH++)
		compareWIstat("ODMatrixHW_file_pls_piem_file_pls_piem_01-06-2015-01-07-2015_minH_0_maxH_25_ABOVE_8limit_20000_cellXHour_odpiemonte",null,"Piemonte",istatH,new ObjectID2IstatCode("G:/DATASET/GEO/telecom-2015-od/odpiemonte.csv"),null);
		
		for(int istatH=1;istatH<=4;istatH++)
		compareWIstat("ODMatrixHW_file_pls_er_file_pls_er_01-04-2015-30-04-2015_minH_0_maxH_25_ABOVE_8limit_20000_cellXHour_odemiliaromagna",null,"Emilia Romagna",istatH,new ObjectID2IstatCode("G:/DATASET/GEO/telecom-2015-od/odemilia-romagna.csv"),null);
		*/
		
		for(int istatH=1;istatH<=4;istatH++)
		compareWIstat("ODMatrixHW_file_pls_lomb_file_pls_lomb_01-03-2014-30-03-2014_minH_0_maxH_25_ABOVE_8limit_20000_cellXHour_odlombardia-scaled-on-residents",null,"Lombardia",istatH,null,null);
		
		for(int istatH=1;istatH<=4;istatH++)
		compareWIstat("ODMatrixHW_file_pls_piem_file_pls_piem_01-06-2015-01-07-2015_minH_0_maxH_25_ABOVE_8limit_20000_cellXHour_odpiemonte-scaled-on-residents",null,"Piemonte",istatH,null,null);
			
		for(int istatH=1;istatH<=4;istatH++)
		compareWIstat("ODMatrixHW_file_pls_er_file_pls_er_01-04-2015-30-04-2015_minH_0_maxH_25_ABOVE_8limit_20000_cellXHour_odemiliaromagna-scaled-on-residents",null,"Emilia Romagna",istatH,null,null);
			
		
		/*
		THRESHOLD = 100;
		FilenameFilter ff= null;
		
		ff = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
			return name.matches("[0-9]+_mod_20150504[0-9]+_20150504[0-9]+_calabrese_lombardia_comuni_istat.txt");
			}
		};
		for(int istatH=1;istatH<=4;istatH++)
			compareWIstat("matrici_lombardia/orarie",ff,"Lombardia",istatH,new ObjectID2IstatCode("G:/DATASET/GEO/telecom-2015-od/odlombardia.csv"),null);
		
		ff = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
			return name.matches("[0-9]+_mod_20150527[0-9]+_20150527[0-9]+_calabrese_piemonte_comuni\\+torinoasc.txt");
			}
		};
		for(int istatH=1;istatH<=4;istatH++)
			compareWIstat("matrici_piemonte/orarie",ff,"Piemonte",istatH,new ObjectID2IstatCode("G:/DATASET/GEO/telecom-2015-od/odpiemonte.csv"),null);
		
		
		ff = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
			return name.matches("[0-9]+_mod_20150416[0-9]+_20150416[0-9]+_calabrese_emilia_regione\\+ascbologna.txt");
			}
		};
		for(int istatH=1;istatH<=4;istatH++)
			compareWIstat("matrici_emilia/orarie",ff,"Emilia Romagna",istatH,new ObjectID2IstatCode("G:/DATASET/GEO/telecom-2015-od/odemilia-romagna.csv"),null);
		*/
		
		
		
		
		/*
		THRESHOLD = 10;
		FilenameFilter ff = null;
		ff = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
			return name.matches("[0-9]+_mod_20150504[0-9]+_20150504[0-9]+_calabrese_lombardia_comuni_istat.txt");
			}
		};
		compareBetweenODs("ODMatrixHW_file_pls_lomb_file_pls_lomb_01-03-2014-30-03-2014_minH_0_maxH_25_ABOVE_8limit_20000_cellXHour_odlombardia",null,"matrici_lombardia/orarie",ff,
				null,null,"ODMatrixHW_file_pls_lomb_file_pls_lomb_01-03-2014-30-03-2014_minH_0_maxH_25_ABOVE_8limit_20000_cellXHour_odlombardia-VS-20150504_calabrese_lombardia_comuni_istat");
		
		
		
		ff = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
			return name.matches("[0-9]+_mod_20150527[0-9]+_20150527[0-9]+_calabrese_piemonte_comuni\\+torinoasc.txt");
			}
		};
		compareBetweenODs("ODMatrixHW_file_pls_piem_file_pls_piem_01-06-2015-01-07-2015_minH_0_maxH_25_ABOVE_8limit_20000_cellXHour_odpiemonte",null,"matrici_piemonte/orarie",ff,
				null,null,"ODMatrixHW_file_pls_piem_file_pls_piem_01-06-2015-01-07-2015_minH_0_maxH_25_ABOVE_8limit_20000_cellXHour_odpiemonte-VS-20150504_calabrese_piemonte_comuni_istat");
		
		
		
		ff = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
			return name.matches("[0-9]+_mod_20150416[0-9]+_20150416[0-9]+_calabrese_emilia_regione\\+ascbologna.txt");
			}
		};
		compareBetweenODs("ODMatrixHW_file_pls_er_file_pls_er_01-04-2015-30-04-2015_minH_0_maxH_25_ABOVE_8limit_20000_cellXHour_odemiliaromagna",null,"matrici_emilia/orarie",ff,
				null,null,"ODMatrixHW_file_pls_er_file_pls_er_01-04-2015-30-04-2015_minH_0_maxH_25_ABOVE_8limit_20000_cellXHour_odemiliaromagna-VS-20150504_calabrese_emilia_comuni_istat");
			
		
		*/
	}
	
	
	public static void compareBetweenODs(String od1, FilenameFilter ff1, String od2, FilenameFilter ff2, ZoneConverter zc1, ZoneConverter zc2,String outdir) throws Exception {
		

		if(ff1 == null) 
			ff1 = new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return true;
				}};
		if(ff2 == null) 
			ff2 = new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return true;
				}};
		
		
		File[] f1 = new File[24];
		for(File f: new File(Config.getInstance().base_folder+"/ODMatrix/"+od1).listFiles(ff1)) {
			if(f.getName().equals("latlon.csv")) continue;
			// if starts with od it follows the HW format. Otherwise the SMOD format
			int h = f.getName().startsWith("od") ? Integer.parseInt(f.getName().split("-")[2]) : Integer.parseInt(f.getName().split("_")[3].substring(8, 10));
			f1[h-1] = f;
		}
		
		File[] f2 = new File[24];
		
		for(File f: new File(Config.getInstance().base_folder+"/ODMatrix/"+od2).listFiles(ff2)) {
			if(f.getName().equals("latlon.csv")) continue;
			// if starts with od it follows the HW format. Otherwise the SMOD format
			int h = f.getName().startsWith("od") ? Integer.parseInt(f.getName().split("-")[2]) : Integer.parseInt(f.getName().split("_")[3].substring(8, 10));
			f2[h-1] = f;
		}
		
		
		
		String[] h = new String[24];
		double[] r2 = new double[24];
		for(int i=0;i<f1.length;i++) {
			if(f1[i]!=null && f2[i]!=null) {
				Map<String,Double> odm1 = ODParser.parse(f1[i].getAbsolutePath(),zc1);
				Map<String,Double> odm2 = ODParser.parse(f2[i].getAbsolutePath(),zc2);
				System.out.println(f1[i].getName()+" VS. "+f2[i].getName());
				
				Map<String,Object> tm1 = ODParser.parseHeader(f1[i].getAbsolutePath());
				Map<String,Object> tm2 = ODParser.parseHeader(f2[i].getAbsolutePath());
				r2[i] = process(odm1,odm2,tm1,tm2,null);
			}
			h[i] = String.valueOf(i);
		}
		
		
		new File(Config.getInstance().paper_folder+"/img/od/"+outdir.replaceAll("_", "-")).mkdirs();
		
		RPlotter.FONT_SIZE = 28;
		RPlotter.drawBar(h, r2, "hour", "r2", Config.getInstance().paper_folder+"/img/od/"+outdir+"/r2.pdf", "");
		
	}
	
	
	
	
	
	public static double compareWIstat(String od_dir,FilenameFilter ff,String istatRegion,int istatH, ZoneConverter zc1, String outdir) throws Exception {
		
		//String file1 = Config.getInstance().base_folder+"/ODMatrix/emilia-romagna/4421_mod_201509140800_201509140900_calabrese_emilia_regione+ascbologna.txt";
		//String file2 = Config.getInstance().base_folder+"/ODMatrix/MatriceOD_-_Emilia Romagna_orario_uscita_-_1.csv";
		//ZoneConverter zc1 = new ObjectID2IstatCode("G:/DATASET/GEO/EmiliaRomagna/EmiliaRomagna.csv"); 
		//ZoneConverter zc2 = new Francia2IstatCode();
		
		if(outdir == null)
			outdir = od_dir;
		
		if(ff == null) 
			 ff = new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return true;
				}};
		
		
		AddMap od1 = new AddMap();
		Map<String,Object> tm1 = null;
		
		File based = new File(Config.getInstance().base_folder+"/ODMatrix/"+od_dir);
		System.out.println(based);
		for(File f: based.listFiles(ff)) {
			boolean ok = false;
			if(f.getName().equals("latlon.csv")) continue;
			
			// if starts with od it follows the HW format. Otherwise the SMOD format
			int h = f.getName().startsWith("od") ? Integer.parseInt(f.getName().split("-")[2]) : Integer.parseInt(f.getName().split("_")[3].substring(8, 10));
			
			if(istatH == 1 && h <= 7) ok = true;
			if(istatH == 2 && h == 8) ok = true;
			if(istatH == 3 && h == 9) ok = true;
			if(istatH == 4 && h > 9 && h < 12) ok = true;
			if(ok) {
				if(tm1==null) tm1= ODParser.parseHeader(f.getAbsolutePath());
				AddMap odx = ODParser.parse(f.getAbsolutePath(),zc1);
				od1.addAll(odx);	
				System.out.println("Adding... "+f);
			}
		}
		
		
		String istatFile = Config.getInstance().base_folder+"/ODMatrix/MatriceOD_-_"+istatRegion+"_orario_uscita_-_"+istatH+".csv";
		
		Map<String,Double> od2 = ODParser.parse(istatFile, new Francia2IstatCode());
		Map<String,Object> tm2 = ODParser.parseHeader(istatFile);
		
		tm1.put("log", LOG);
		tm1.put("istatH", istatH);
		tm1.put("region",istatRegion);
		
		return process(od1,od2,tm1,tm2,Config.getInstance().paper_folder+"/img/od/"+outdir);
	}
	
	
	public static double process(Map<String,Double> od1, Map<String,Double> od2, Map<String,Object> tm1, Map<String,Object> tm2, String odir) throws Exception {
	
		
		
		List<Double> lx = new ArrayList<Double>();
		List<Double> ly = new ArrayList<Double>();
		
		
		for(String k: od1.keySet()) {
			double v1 = od1.get(k);
			Double v2 = od2.get(k);
			if(v2 == null) v2 = 0.0;
			if(v1 > THRESHOLD && v2 > THRESHOLD) {
				//System.out.println(k+","+v1+","+v2);
				lx.add(v1);
				ly.add(v2);
			}
		}
		
		
		System.out.println("OD1 SIZE = "+od1.size()+" OD2 SIZE = "+od2.size()+" N. COMPARISONS = "+lx.size());
		SimpleRegression sr = new SimpleRegression(INTERCEPT);
		double[] x = new double[lx.size()];
		double[] y = new double[ly.size()];
		for(int i=0; i<x.length;i++) {
			x[i] = lx.get(i);
			y[i] = ly.get(i);
			if(LOG) {
				x[i] = Math.log(x[i]);
				y[i] = Math.log(y[i]);
				sr.addData(x[i], y[i]);
			}
		}
		
		
		double r2 = 0;
		if(sr.getN() > 2) 
			r2 = sr.getRSquare();
		
		if(odir!=null) {
			odir = odir.replaceAll("_", "-");
			System.out.println(odir);
			new File(odir).mkdirs();
			
			String imgFile = "Compare2-"+tm2.get("name")+".pdf";
			
			
			System.out.println(odir+" ===================> r2 ="+r2);
			
			tm1.put("img", odir.substring(Config.getInstance().paper_folder.length()+1)+"/"+imgFile);
			tm1.put("r2", r2);
			
			String xlab = "Estimated"+(LOG?" (log)":"");
			String ylab = "GroundTruth"+(LOG?" (log)":"");
			

			RPlotter.drawScatter(x, y, xlab, ylab, odir+"/"+imgFile, "stat_smooth(method=lm,colour='black') + geom_point(alpha=0.4,size = 5)");
			
			//create the map for text plotter with all relevant information
			
			TextPlotter.getInstance().run(tm1,"src/cdraggregated/densityANDflows/flows/ODComparator.ftl", odir+"/"+imgFile.replaceAll(".pdf", ".tex"));
		}
		return r2;
	}
	
	
	
	
	
	
	
	
	
	
	
}
