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

public class DensityIstatComparator {
	
	
	public static boolean LOG = true;
	public static boolean INTERCEPT = true;
	public static int THRESHOLD = 0;
	
	public static void main(String[] args) throws Exception {
		
	
		
		String file = "comuni2012-HOME-null_comuni2012.ser";
		Map<String,Double> space_density = (Map<String,Double>)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/AggregatedSpaceDensity/"+file));
		
		
		IstatCensus2011 ic = IstatCensus2011.getInstance();
		AddMap istat = ic.computeDensity(0, false, false);
		
		
		compareWithISTAT(file.split("_")[0]+"VSistat",space_density,istat);
	}
	
	
	
	
	public static void compareWithISTAT(String title, Map<String,Double> density, Map<String,Double> istat) throws Exception {
				
		int size = 0;
		for(String r: density.keySet()) {
			int estimated = density.get(r).intValue();
			Double groundtruth = istat.get(r);
			if(groundtruth != null && estimated> THRESHOLD) {
				size++;
				//System.out.println(r+","+estimated+","+groundtruth);
			}
		}
		
	
		File d = new File(Config.getInstance().base_folder+"/IstatComparator");
		if(!d.exists()) d.mkdirs();
		
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(d+"/"+title+"_hist.csv")));
		out.println("estimated;groundtruth");
		
		
		double[][] result = new double[size][2];
		size = 0;
		for(String r: density.keySet()) {
			int estimated = density.get(r).intValue();
			Double groundtruth = istat.get(r);
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
		
		
		RPlotter.drawScatter(x, y, xlab, ylab, Config.getInstance().paper_folder+"/img/Density/"+title+"VSistat.png", "stat_smooth(method=lm,colour='black') + geom_point(alpha=0.4,size = 5)");
		
		//create the map for text plotter with all relevant information
		Map<String,Object> tm = new HashMap<String,Object>();
		tm.put("region","Piemonte");
		tm.put("r", sr.getR());
		tm.put("log", LOG);
		tm.put("img", title+"VSistat.png");
		
		TextPlotter.getInstance().run(tm,"src/cdraggregated/densityANDflows/density/DensityIstatComparator.ftl", Config.getInstance().paper_folder+"/img/Density/density_piemonte.tex");
		
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
