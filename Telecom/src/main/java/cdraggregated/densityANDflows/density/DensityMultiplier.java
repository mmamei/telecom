package cdraggregated.densityANDflows.density;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.math3.stat.regression.SimpleRegression;

import otherdata.TIbigdatachallenge2015.IstatCensus2011;
import utils.AddMap;
import utils.Config;
import utils.CopyAndSerializationUtils;

public class DensityMultiplier {
	
	public static void main(String[] args) throws Exception {
		String file = "file_pls_piem_file_pls_piem_01-06-2015-01-07-2015_minH_0_maxH_25_ABOVE_8limit_20000_cellXHour-comuni2012-HOME-null.ser";
		//String file = "file_pls_piem_file_pls_piem_01-06-2015-01-07-2015_minH_0_maxH_25_ABOVE_400limit_1000_cellXHour-comuni2012-HOME-null.ser";
		Map<String,Double> space_density = (Map<String,Double>)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/AggregatedSpaceDensity/"+file));
		
		String istatTitle = "istat-demographic-2011";
		AddMap istat = IstatCensus2011.getInstance().computeDensity(0, false, false);
		
		
		DensityComparator.compare(file.replaceAll(".ser", ""),space_density,istatTitle,istat);
		
		Map<String,double[]> mq = getScale(space_density,istat,10,false,true);
		Map<String,Double> scaled_density = scale(space_density,mq);
		String title1 = "scaled"+file.replaceAll(".ser", "");
		
		DensityComparator.compare(title1,scaled_density,istatTitle,istat);
		
		System.out.println("Done!");
	}
	
	
	public static Map<String,double[]> getScale(Map<String,Double> density1, Map<String,Double> density2, double threshold, boolean intercept, boolean individual) {
			
		
		if(individual) {
			Map<String,double[]> scale= new HashMap<>();
			for(String r: density1.keySet()) {
				double v1 = density1.get(r);
				Double v2 = density2.get(r);
				if(v2 != null && v1> threshold && v2 > threshold) 
					scale.put(r, new double[]{v2/v1,0});
			}
			return scale;
		}
		else {
			SimpleRegression sr = new SimpleRegression(intercept);
			for(String r: density1.keySet()) {
				double v1 = density1.get(r);
				Double v2 = density2.get(r);
				if(v2 != null && v1> threshold && v2 > threshold) {
					sr.addData(v1, v2);
				}
			}
			
			System.out.println("---------------> "+sr.getR()+" "+sr.getSlope()+" "+sr.getIntercept());
			
			Map<String,double[]> scale= new HashMap<>();
			for(String r: density1.keySet())
				scale.put(r, new double[]{sr.getSlope(),sr.getIntercept()});
			return scale;
		}
	}
	
	
	public static Map<String,Double> scale(Map<String,Double> density1, Map<String,double[]> scale) {
		Map<String,Double> scaled = new HashMap<>();
		for(String r: density1.keySet()) {
			double[] mq = scale.get(r);
			if(mq!=null)
				scaled.put(r, density1.get(r) * mq[0] + mq[1]);
			else
				scaled.put(r, 0.0);
		}
		return scaled;
	}
}
