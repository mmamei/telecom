package cdraggregated;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import utils.ListMapArrayBasicUtils;
import utils.StatsUtils;
import utils.time.TimeConverter;
import JavaMI.Entropy;

public class SynchCompute {
	
	static int TIME_WINDOW = -1;
	//private static int TIME_WINDOW = 24;
	
	
	enum Feature {RSQ, I,EU};
	public static Feature USEF = Feature.RSQ;
	
	static String USE_FEATURE = "F";
	static {
		if(USEF.equals(Feature.I)) USE_FEATURE = "expression(avg[i]~I~'('~res[r]~';'~res[i!=r]~')')";
		if(USEF.equals(Feature.RSQ)) USE_FEATURE =  "expression(avg[i]~bar(R)^{2}~'('~res[r]~','~res[i!=r]~')')";
		if(USEF.equals(Feature.EU)) USE_FEATURE = "expression(avg[i]~EU~'('~res[r]~';'~res[i!=r]~')')";
	}
	
	static List<Double> computeFeature(double[] series1, double[] series2) {
		
		
		List<Double> result = new ArrayList<Double>();
		
		try {
			TimeConverter tc = TimeConverter.getInstance();
			
			boolean Z = true;
	        boolean FILTER = false;
	        int BIN = USEF.equals(Feature.I)? 5 : 0;
	        
			//int LAG = FILTER ? 50 : 24*7;
			
			double[] fseries1 = Z ? (StatsUtils.getZH(series1,tc)) : series1;
			double[] fseries2 = Z ? (StatsUtils.getZH(series2,tc)) : series2;
			
			fseries1 = FILTER ? filter(fseries1,tc) : fseries1;
			fseries2 = FILTER ? filter(fseries2,tc) : fseries2;
			
			
			fseries1 = BIN > 0 ? ListMapArrayBasicUtils.bin(fseries1,BIN) : fseries1;
			fseries2 = BIN > 0 ? ListMapArrayBasicUtils.bin(fseries2,BIN) : fseries2;
			
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
	
	
	static double reallyComputeFeature(double[] s1, double[] s2) {
		double x = 0;
		
		if(USEF.equals(Feature.I))
			x = Entropy.calculateEntropy(s1) - Entropy.calculateConditionalEntropy(s1, s2);
			
		if(USEF.equals(Feature.RSQ)) 
			//int L = 1;
			//OLSMultipleLinearRegression h0 = new OLSMultipleLinearRegression();
			//h0.setNoIntercept(true);
			//h0.newSampleData(GrangerTest.strip(L, s1), GrangerTest.createLaggedSide(L, s2));
			//x = h0.calculateRSquared();
			x = StatsUtils.r2(s1,s2);

		
		if(USEF.equals(Feature.EU)) {
			for(int i=0; i<s1.length;i++) 
				x += Math.pow(s1[i] - s2[i],2);
			x = 1-Math.sqrt(x);
			
		}
		return x;
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
