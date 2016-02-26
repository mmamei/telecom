package cdraggregated.densityANDflows.flows;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import utils.Config;
import visual.r.RPlotter;
import visual.text.TextPlotter;
import cdraggregated.densityANDflows.Francia2IstatCode;
import cdraggregated.densityANDflows.ObjectID2IstatCode;
import cdraggregated.densityANDflows.ZoneConverter;

public class ODComparator {
	
	public static final boolean LOG = true;
	public static final int THRESHOLD = 10;
	
	public static void main(String[] args) throws Exception {
		
		
		//String file1 = Config.getInstance().base_folder+"/ODMatrix/emilia-romagna/4421_mod_201509140800_201509140900_calabrese_emilia_regione+ascbologna.txt";
		//String file2 = Config.getInstance().base_folder+"/ODMatrix/MatriceOD_-_Emilia Romagna_orario_uscita_-_1.csv";
		//ZoneConverter zc1 = new ObjectID2IstatCode("G:/DATASET/GEO/EmiliaRomagna/EmiliaRomagna.csv"); 
		//ZoneConverter zc2 = new Francia2IstatCode();
		
		
		String file1 = Config.getInstance().base_folder+"/ODMatrix/ODMatrixHW_file_pls_piem_file_pls_piem_01-06-2015-01-07-2015_minH_0_maxH_25_ABOVE_8limit_5000_cellXHour_odpiemonte/od.csv";
		String file2 = Config.getInstance().base_folder+"/ODMatrix/MatriceOD_-_Piemonte_orario_uscita_-_1.csv";
		ZoneConverter zc1 = new Francia2IstatCode(); 
		ZoneConverter zc2 = new Francia2IstatCode();
		
		process(file1,file2,zc1,zc2);
	}
	
	public static void process(String file1, String file2, ZoneConverter zc1, ZoneConverter zc2) throws Exception {
	
		Map<String,Double> od = parse(file1, zc1);
		Map<String,Double> od_istat = parse(file2, zc2);
		
		List<Double> lx = new ArrayList<Double>();
		List<Double> ly = new ArrayList<Double>();
		
		for(String k: od.keySet()) {
			double v1 = od.get(k);
			Double v2 = od_istat.get(k);
			if(v2!=null && v1 > THRESHOLD && v2 > THRESHOLD) {
				//System.out.println(k+","+v1+","+v2);
				lx.add(v1);
				ly.add(v2);
			}
		}
				
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
		
		
		Map<String,Object> tm1= parseHeader(file1);
		Map<String,Object> tm2 = parseHeader(file2);
		
		file1 = file1.replaceAll("_|\\.", "-");
		file2 = file2.replaceAll("_|\\.", "-");
		
		String imgFile = "img/od/"+tm1.get("name")+"-VS-"+tm2.get("name")+".pdf";
		
		tm1.put("log", LOG);
		tm1.put("img", imgFile);
		
		
		String xlab = "Estimated"+(LOG?" (log)":"");
		String ylab = "GroundTruth"+(LOG?" (log)":"");
		
		RPlotter.drawScatter(x, y, xlab, ylab, Config.getInstance().paper_folder+"/"+imgFile, "stat_smooth(method=lm,colour='black') + geom_point(alpha=0.4,size = 5)");
		
		//create the map for text plotter with all relevant information
		
		TextPlotter.getInstance().run(tm1,"src/cdraggregated/densityANDflows/flows/ODComparator.ftl", Config.getInstance().paper_folder+"/"+imgFile.replaceAll(".pdf", ".tex"));
				
		
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
				
				if(line.matches("-+")) break;
				
				String[] el = line.split(":");
				tm.put(el[0].replaceAll("-","").trim(), el[1].trim());
				all.append("-"+el[1].trim().replaceAll(" ", "-"));
			}
			System.out.println("Procesed "+(headerRows+1)+" header rows");
			
			br.close();
		}catch(Exception e) {
			e.printStackTrace();
		}

		
		tm.put("name", all.substring(1));
		
		for(String k: tm.keySet())
			System.out.println(k+" --> "+tm.get(k));
		
		
		
		return tm;
	}
	
	
	public static Map<String,Double> parse(String file, ZoneConverter zc) throws Exception {
		Map<String,Double> od = new HashMap<String,Double>();
		BufferedReader br = new BufferedReader(new FileReader(file));
		
		String line;
		for(int i=0; i<14;i++) br.readLine(); // skip header
		
		
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
				od.put(a+"-"+b, Double.parseDouble(codvals[j]));
			}
				
				
			i++;
		}
		br.close();
		return od;
		
	}
	
	
	
	
}
