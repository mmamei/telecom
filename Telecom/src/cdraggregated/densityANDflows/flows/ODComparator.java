package cdraggregated.densityANDflows.flows;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import utils.AddMap;
import utils.Config;
import visual.r.RPlotter;
import visual.text.TextPlotter;
import cdraggregated.densityANDflows.Francia2IstatCode;
import cdraggregated.densityANDflows.ObjectID2IstatCode;
import cdraggregated.densityANDflows.ZoneConverter;

public class ODComparator {
	
	public static final boolean LOG = true;
	public static final int THRESHOLD = 10;
	
	// istatH
	// 1 prima delle 7,15;
	// 2 dalle 7,15 alle 8,14;
	// 3 dalle 8,15 alle 9,14;
	// 4 dopo le 9,14;
	
	public static void main(String[] args) throws Exception {
		//go("ODMatrixHW_file_pls_piem_file_pls_piem_01-06-2015-01-07-2015_minH_0_maxH_25_ABOVE_8limit_5000_cellXHour_odpiemonte",null,"Piemonte",1,new Francia2IstatCode(),new Francia2IstatCode());
		go("ODMatrixHW_file_pls_lomb_file_pls_lomb_01-03-2014-30-03-2014_minH_0_maxH_25_ABOVE_8limit_20000_cellXHour_odlombardia",null,"Lombardia",1,new ObjectID2IstatCode("G:/DATASET/GEO/telecom-2015-od/odlombardia.csv"),new Francia2IstatCode(),null);
	}
	
	
	public static void go(String od_dir,FilenameFilter ff,String istatRegion,int istatH, ZoneConverter zc1, ZoneConverter zc2, String outdir) throws Exception {
		
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
		
		for(File f: new File(Config.getInstance().base_folder+"/ODMatrix/"+od_dir).listFiles(ff)) {
			boolean ok = false;
			if(f.getName().equals("latlon.csv")) continue;
			
			// if starts with od it follows the HW format. Otherwise the SMOD format
			int h = f.getName().startsWith("od") ? Integer.parseInt(f.getName().split("-")[2]) : Integer.parseInt(f.getName().split("_")[3].substring(8, 10));
			
			if(istatH == 1 && h <= 7) ok = true;
			if(istatH == 2 && h == 8) ok = true;
			if(istatH == 3 && h == 9) ok = true;
			if(istatH == 4 && h > 9 && h < 12) ok = true;
			if(ok) {
				if(tm1==null) tm1= parseHeader(f.getAbsolutePath());
				od1.addAll(parse(f.getAbsolutePath(),zc1));	
				System.out.println("Adding... "+f);
			}
		}
		
		
		String file2 = Config.getInstance().base_folder+"/ODMatrix/MatriceOD_-_"+istatRegion+"_orario_uscita_-_"+istatH+".csv";
		
		Map<String,Double> od2 = parse(file2, zc2);
		Map<String,Object> tm2 = parseHeader(file2);
		
		
		
		process(od1,od2,tm1,tm2,Config.getInstance().paper_folder+"/img/od/"+outdir);
	}
	
	public static void process(Map<String,Double> od1, Map<String,Double> od2, Map<String,Object> tm1, Map<String,Object> tm2, String odir) throws Exception {
	
		odir = odir.replaceAll("_", "-");
		
		System.out.println(odir);
		new File(odir).mkdirs();
		
		List<Double> lx = new ArrayList<Double>();
		List<Double> ly = new ArrayList<Double>();
		
		for(String k: od1.keySet()) {
			double v1 = od1.get(k);
			Double v2 = od2.get(k);
			if(v2!=null && v1 > THRESHOLD && v2 > THRESHOLD) {
				//System.out.println(k+","+v1+","+v2);
				lx.add(v1);
				ly.add(v2);
			}
		}
		
		
		System.out.println("OD1 SIZE = "+od1.size()+" OD2 SIZE = "+od2.size()+" N. COMPARISONS = "+lx.size());
				
		double[] x = new double[lx.size()];
		double[] y = new double[ly.size()];
		for(int i=0; i<x.length;i++) {
			x[i] = lx.get(i);
			y[i] = ly.get(i);
			if(LOG) {
				x[i] = Math.log(x[i]);
				y[i] = Math.log(y[i]);
			}
		}
		
		String imgFile = "Compare2-"+tm2.get("name")+".pdf";
		
		tm1.put("log", LOG);
		tm1.put("img", odir.substring(Config.getInstance().paper_folder.length()+1)+"/"+imgFile);
		
		
		String xlab = "Estimated"+(LOG?" (log)":"");
		String ylab = "GroundTruth"+(LOG?" (log)":"");
		
		
		
		RPlotter.drawScatter(x, y, xlab, ylab, odir+"/"+imgFile, "stat_smooth(method=lm,colour='black') + geom_point(alpha=0.4,size = 5)");
		
		//create the map for text plotter with all relevant information
		
		TextPlotter.getInstance().run(tm1,"src/cdraggregated/densityANDflows/flows/ODComparator.ftl", odir+"/"+imgFile.replaceAll(".pdf", ".tex"));
				
		
	}
	
	private static Map<String,String> REGION2REGION = new HashMap<>();
	static {
		REGION2REGION.put("piem", "Piemonte");
		REGION2REGION.put("lomb", "Lombardia");
	}
	
	
	
	/*
--------------------------------------------------------------
Matrice Origine Destinazione
--------------------------------------------------------------
- Metodo: HW
- Zona interessata: odpiemonte
- Istante di inizio: 01-06-2015
- Soglia per italiani: 400
- Applicato filtro privacy: 1
- # di utenti su utenti del campione: 1000
- Tipologia utenti: Tutti
- Istante di fine: 01-07-2015
- Soglia per stranieri: 400
-------------------------------------------------------------- 
	*/
	

	
	public static Map<String,Object> parseHeader(String file) {
		Map<String,Object> tm = new HashMap<String,Object>();
		StringBuffer all = new StringBuffer();
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			
			br.readLine();
			br.readLine();
			br.readLine();
			String line;
			int headerRows = 0;
			while((line = br.readLine()).trim().length()>0) {
				line = line.replaceAll("_", "-");
				headerRows ++;
				if(line.matches("-+")) break;
				
				String[] el = line.split(":");
				tm.put(el[0].replaceAll("-","").trim(), el[1].trim());
				all.append("-"+el[1].trim().replaceAll(" ", "-"));
			}
			System.out.println("Processed "+(headerRows+1)+" header rows");
			
			br.close();
		}catch(Exception e) {
			e.printStackTrace();
		}

		
		tm.put("name", all.substring(1));
		
		for(String k: tm.keySet())
			System.out.println(k+" --> "+tm.get(k));
		
		
		
		return tm;
	}
	
	
	public static AddMap parse(String file, ZoneConverter zc) throws Exception {
		
		
		System.out.println(file);
		
		
		AddMap od = new AddMap();
		BufferedReader br = new BufferedReader(new FileReader(file));
		int headerRows = 0;
		String line;
		while((line = br.readLine()).trim().length()>0)
			headerRows++;
		System.out.println("Skipped "+(headerRows+1)+" header rows");
		
		//line = br.readLine();
		
		
		
		
		line = br.readLine().trim(); // read first row
		
		
		if(line.startsWith("\t\t"))
			line = line.replaceFirst("\t", "");
		
		String[] cod = line.split("\t");
		
		int i = 0;
		while((line=br.readLine())!=null) {
			
			
			if(line.startsWith("\t"))
				line = line.replaceFirst("\t", "");
			
			String[] codvals = line.split("\t");
			if(!codvals[0].equals(cod[i])) System.err.println("error");
			for(int j=1;j<codvals.length;j++) {
				String a = zc == null ? cod[i] : zc.convert(cod[i]);
				String b = zc == null ? cod[j-1] : zc.convert(cod[j-1]);
				double x = Double.parseDouble(codvals[j]);
				if(x > 0)
					od.add(a+"-"+b, x);
			}
				
				
			i++;
		}
		br.close();
		return od;
		
	}
	
	
	
	
}
