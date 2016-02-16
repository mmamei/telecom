package cdraggregated.densityANDflows.flows;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import utils.Config;
import visual.r.RPlotter;
import visual.text.TextPlotter;

public class ODComparator {
	
	public static final boolean LOG = true;
	
	
	public static void main(String[] args) throws Exception {
		process("ODMatrixHW_file_pls_piem_file_pls_piem_01-06-2015-01-07-2015_minH_0_maxH_25_ABOVE_400limit_1000_cellXHour_odpiemonte.ser",
				"MatriceOD_-_Piem_orario_uscita_-_1.csv");
	}
	
	public static void process(String file1, String file2) throws Exception {
	
		Map<String,Double> od = parse(Config.getInstance().base_folder+"/ODMatrix/"+file1+"/od.csv");
		Map<String,Double> od_istat = parse(Config.getInstance().base_folder+"/ODMatrix/"+file2);
		
		List<Double> lx = new ArrayList<Double>();
		List<Double> ly = new ArrayList<Double>();
		
		for(String k: od.keySet()) {
			double v1 = od.get(k);
			Double v2 = od_istat.get(k);
			if(v2!=null && v1 > 0 && v2 > 0) {
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
		
		file1 = file1.replaceAll("_|\\.", "-");
		file2 = file2.replaceAll("_|\\.", "-");
		
		
		
		String imgFile = "img/od/"+file1+"-VS-"+file2+".pdf";
		
		String title = "Piemonte";
		String xlab = "Estimated"+(LOG?" (log)":"");
		String ylab = "GroundTruth"+(LOG?" (log)":"");
		
		RPlotter.drawScatter(x, y, xlab, ylab, Config.getInstance().paper_folder+"/"+imgFile, "stat_smooth(method=lm,colour='black') + geom_point(alpha=0.4,size = 5)");
		
		//create the map for text plotter with all relevant information
		Map<String,Object> tm = new HashMap<String,Object>();
		tm.put("region","Piemonte");
		tm.put("log", LOG);
		tm.put("img", imgFile);
		tm.putAll(parseFileName(file1));
		
		TextPlotter.getInstance().run(tm,"src/cdraggregated/densityANDflows/flows/ODComparator.ftl", Config.getInstance().paper_folder+"/"+imgFile.replaceAll(".pdf", ".tex"));
				
		
	}
	
	private static Map<String,String> REGION2REGION = new HashMap<>();
	static {
		REGION2REGION.put("piem", "Piemonte");
		REGION2REGION.put("lomb", "Lombardia");
	}
	
	//ODMatrixHW_file_pls_piem_file_pls_piem_01-06-2015-01-07-2015_minH_0_maxH_25_ABOVE_400limit_1000_cellXHour_odpiemonte.ser
	public static Map<String,Object> parseFileName(String file) {
		Map<String,Object> tm = new HashMap<String,Object>();
		// parse title
		String[] e = file.split("-|_");
		tm.put("region",REGION2REGION.get(e[3]));
		tm.put("startdate", e[7]+"-"+e[8]+"-"+e[9]);
		tm.put("enddate", e[10]+"-"+e[11]+"-"+e[12]);
		tm.put("minh", e[14]);
		tm.put("maxh", e[16]);
		tm.put("above", e[18].replaceAll("limit", ""));
		tm.put("limit", e[19]);
		tm.put("zone", e[21].substring(2));
		return tm;
	}
	
	
	public static Map<String,Double> parse(String file) throws Exception {
		Map<String,Double> od = new HashMap<String,Double>();
		BufferedReader br = new BufferedReader(new FileReader(file));
		
		String line;
		for(int i=0; i<14;i++) br.readLine(); // skip header
		
		
		line = br.readLine().trim(); // read first row
		String[] cod = line.split("\t");
		
		int i = 0;
		while((line=br.readLine())!=null) {
			String[] codvals = line.split("\t");
			if(!codvals[0].equals(cod[i])) System.err.println("error");
			for(int j=1;j<codvals.length;j++)
				od.put(convert(cod[i])+"-"+convert(cod[j-1]), Double.parseDouble(codvals[j]));
			i++;
		}
		br.close();
		return od;
		
	}
	
	public static String convert(String cod) {
		String c = cod.replaceAll("-", "");
		while(c.startsWith("0")) c = c.substring(1);
		return c;
	}
	
	
}
