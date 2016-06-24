package utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

public class ListMapArrayBasicUtils {
	
	public static double[] toArray(List<Double> x) {
		double[] y = new double[x.size()];
		for(int i=0; i<y.length;i++)
			y[i] = x.get(i);
		return y;
	}
	
	
	public static double avg(double[] x) {
		DescriptiveStatistics ds = new DescriptiveStatistics();
		for(double v: x)
			ds.addValue(v);
		return ds.getMean();
	}
	
	public static double median(double[] x) {
		DescriptiveStatistics ds = new DescriptiveStatistics();
		for(double v: x)
			ds.addValue(v);
		return ds.getPercentile(50);
	}
	
	public static double[] log(double[] x) {
		double[] d = new double[x.length];
		for(int i=0; i<d.length;i++)
			d[i] = Math.log(x[i]);
		return d;
	}
	
	public static double[] abs(double[] x) {
		double[] d = new double[x.length];
		for(int i=0; i<d.length;i++)
			d[i] = Math.abs(x[i]);
		return d;
	}
	
	public static double[] diff(double[] a, double[] b) {
		double[] d = new double[a.length];
		for(int i=0; i<d.length;i++)
			d[i] = Math.abs(a[i]-b[i]);
		return d;
	}
	
	public static double[] bin(double[] x, int n) {
		double max = 0;
		for(double v: x)
			max = Math.max(max, v);
		double[] bx = new double[x.length];
		for(int i=0; i<bx.length;i++)
			bx[i] = Math.floor(1.0 * n * x[i] / max);
		return bx;
	}
	
	public static double avg(List<Double> x) {
		DescriptiveStatistics ds = new DescriptiveStatistics();
		for(double v: x)
			ds.addValue(v);
		return ds.getMean();
	}
	
	public static double median(List<Double> x) {
		DescriptiveStatistics ds = new DescriptiveStatistics();
		for(double v: x)
			ds.addValue(v);
		return ds.getPercentile(50);
	}
	
	public static Map<String,Double> avg(Map<String,List<Double>> x) {
		Map<String,Double> d = new HashMap<String,Double>();
		for(String k: x.keySet()) {
			d.put(k, ListMapArrayBasicUtils.avg(x.get(k)));
		}
		return d;
	}
	
	
}
