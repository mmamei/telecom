package utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
	
	public static <T,V,X> Map<T,V> concatenate(Map<T,X> a, Map<X,V> b) {
		Map<T,V> result = new HashMap<>();
		for(T x: a.keySet())
			result.put(x, b.get(a.get(x)));
		return result;
	}
	
	
	
	public static <K,V> Map<V,Set<K>> invert(Map<K,V> m) {
		Map<V,Set<K>> r = new HashMap<V,Set<K>>();
		for(K k: m.keySet()) {
			V v = m.get(k);
			Set<K> x = r.get(v);
			if(x == null) {
				x = new HashSet<K>();
				r.put(v, x);
			}
			x.add(k);
		}
		return r;
	}
	
	public static void main(String[] args) {
		
		Map<String,String> m1 = new HashMap<>();
		m1.put("a", "x");
		m1.put("b", "y");
		
		Map<String,Integer> m2 = new HashMap<>();
		m2.put("x", 1);
		m2.put("y", 2);
		
		Map<String,Integer> m3 = concatenate(m1,m2);
		System.out.println(m3);
	}
	
	
}
