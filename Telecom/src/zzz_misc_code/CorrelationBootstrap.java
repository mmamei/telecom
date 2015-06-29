package zzz_misc_code;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.regression.SimpleRegression;



public class CorrelationBootstrap {
	
	public static void main(String[] args) throws Exception {
		
		
		List<int[]> values = new ArrayList<int[]>();
		BufferedReader br = new BufferedReader(new FileReader("C:/BASE/IstatComparator/Piemonte_hist.csv"));
		String line;
		br.readLine(); // header
		while((line=br.readLine())!=null) {
			String[] xy = line.split(";");
			int x = Integer.parseInt(xy[0]);
			int y = Integer.parseInt(xy[1]);
			values.add(new int[]{x,y});
		}
		br.close();
		
		
		SimpleRegression sr = new SimpleRegression(false);
		for(int[] v: values)
			sr.addData(v[0],v[1]);
		
		System.out.println(sr.getR());
		System.out.println(sr.getSignificance());
		
		DescriptiveStatistics ds = new DescriptiveStatistics();
		for(int i=0; i<100;i++) {
			sr = new SimpleRegression(false);
			for(int[] v: sample2(values,100))
				sr.addData(v[0],v[1]);
			ds.addValue(sr.getR());
		}
		
		System.out.println(ds.getMean());
		System.out.println(ds.getStandardDeviation());
		System.out.println(ds.getPercentile(1));
		System.out.println(ds.getPercentile(99));
		
	}
	
	
	public static List<int[]> sample (List<int[]> values, double p) {
		List<int[]> sampled = new ArrayList<int[]>();
		for(int[] v: values)
			if(Math.random()<p)
				sampled.add(v);
		return sampled;
	}
	
	public static List<int[]> sample2 (List<int[]> values, int n) {
		List<int[]> sampled = new ArrayList<int[]>();
		for(int i=0; i<n;i++) 
			sampled.add(values.get((int)(Math.random() * values.size())));
		return sampled;
	}
}
