package cdraggregated.densityANDflows.density;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.stat.regression.SimpleRegression;

import otherdata.TIbigdatachallenge2015.IstatCensus2011;
import utils.AddMap;
import utils.Config;
import utils.CopyAndSerializationUtils;
import utils.Logger;
import visual.r.RPlotter;
import visual.text.TextPlotter;

public class DensityComparator {
	
	
	public static boolean LOG = true;
	public static boolean INTERCEPT = true;
	public static int THRESHOLD = 0;
	
	
	/*
	 * This class is used to compare densities from different datasets.
	 * We typically use to compare ISTAT demographic data with CDR density.
	 */
	
	
	public static void main(String[] args) throws Exception {
	
		String file = "file_pls_piem_file_pls_piem_01-06-2015-01-07-2015_minH_0_maxH_25_ABOVE_400limit_1000_cellXHour-comuni2012-HOME-null.ser";
		Map<String,Double> space_density = (Map<String,Double>)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/AggregatedSpaceDensity/"+file));
		
		String istatTitle = "istat-demographic-2011";
		AddMap istat = IstatCensus2011.getInstance().computeDensity(0, false, false);
		
		compare(file.replaceAll(".ser", ""),space_density,istatTitle,istat);
	}
	
	
	
	
	public static void compare(String title1, Map<String,Double> density1, String title2, Map<String,Double> density2) throws Exception {
				
		int size = 0;
		for(String r: density1.keySet()) {
			int estimated = density1.get(r).intValue();
			Double groundtruth = density2.get(r);
			if(groundtruth != null && estimated> THRESHOLD) {
				size++;
				//System.out.println(r+","+estimated+","+groundtruth);
			}
		}
		
	
		File d = new File(Config.getInstance().base_folder+"/DensityComparator");
		if(!d.exists()) d.mkdirs();
		
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(d+"/"+title1+"_VS_"+title2+".csv")));
		out.println("estimated;groundtruth");
		
		
		double[][] result = new double[size][2];
		size = 0;
		for(String r: density1.keySet()) {
			int estimated = density1.get(r).intValue();
			Double groundtruth = density2.get(r);
			if(groundtruth != null && estimated>0 && groundtruth>0) {
				out.println(estimated+";"+groundtruth);
				if(estimated > THRESHOLD){
					result[size][0] = LOG? Math.log10(estimated) : estimated;
					result[size][1] = LOG? Math.log10(groundtruth): groundtruth;
					size++;
				}
			}
		}
		out.close();
		
		SimpleRegression sr = new SimpleRegression(INTERCEPT);
		sr.addData(result);
		printInfo(sr);
		
		
		double[] x = new double[size];
		double[] y = new double[size];
		for(int i=0; i<x.length;i++) {
			x[i] = result[i][0];
			y[i] = result[i][1];
		}
			
 		
		
		List<double[][]> ldata = new ArrayList<double[][]>();
		ldata.add(result);
		List<String> labels = new ArrayList<String>();
		labels.add("population density");
		
		String xlab = "Estimated"+(LOG?" (log)":"");
		String ylab = "GroundTruth"+(LOG?" (log)":"");
		
		
		
		title1 = title1.replaceAll("_", "-");
		title2 = title2.replaceAll("_", "-");
		
		String imgFile = "img/density/"+title1+"_VS_"+title2+(LOG?"_LOG":"")+".png";
		RPlotter.drawScatter(x, y, xlab, ylab, Config.getInstance().paper_folder+"/"+imgFile, "stat_smooth(method=lm,colour='black') + geom_point(alpha=0.4,size = 5)");
		
		//create the map for text plotter with all relevant information
		Map<String,Object> tm = new HashMap<String,Object>();
		
		tm.put("r2", sr.getRSquare());
		tm.put("log", LOG);
		tm.put("img", imgFile);
		
		// parse title
		tm.putAll(DensityPlotter.parseFileName(title1));
		
		TextPlotter.getInstance().run(tm,"src/cdraggregated/densityANDflows/density/DensityComparator.ftl", Config.getInstance().paper_folder+"/"+imgFile.replaceAll(".png", ".tex"));
		
	}
	
	
	
	public static void printInfo(SimpleRegression sr) {
		Logger.logln("r="+sr.getR()+", r^2="+sr.getRSquare()+", sse="+sr.getSumSquaredErrors());
		
		double s = sr.getSlope();
		double sconf = sr.getSlopeConfidenceInterval(); 
		
		double i = sr.getIntercept();
		double iconf = sr.getInterceptStdErr();
		
		Logger.logln("Y = "+s+" * X + "+i);
		Logger.logln("SLOPE CONF INTERVAL =  ["+(s-sconf)+","+(s+sconf)+"]");
		Logger.logln("INTERCEPT CONNF INTERVAL =  ["+(i-iconf)+","+(i+iconf)+"]");
		
	}
}
