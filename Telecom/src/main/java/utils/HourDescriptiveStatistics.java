package utils;

import java.util.ArrayList;
import java.util.List;

public class HourDescriptiveStatistics {
	
	private List<Double> hours;
	
	public HourDescriptiveStatistics() {
		hours = new ArrayList<Double>();
	}
	public void add(double h) {
		hours.add(h);
	}
	public void addAll(double[] hours) {
		for(double h: hours)
			add(h);
	}
	public void addAll(List<Double> hours) {
		this.hours.addAll(hours);
	}
	
	public double mean() {
		double sums = 0, sumc = 0;
		for(double h: hours) {
			double theta = h * Math.PI / 12;
			sums += Math.sin(theta);
			sumc += Math.cos(theta);
		}
		
		double mean_theta = Math.atan2(sums, sumc);
		if(mean_theta < 0) mean_theta += 2*Math.PI;
		
		return mean_theta * 12 / Math.PI;
	}
	
	
	public double variance() {
		double sums = 0, sumc = 0;
		for(double h: hours) {
			double theta = h * Math.PI / 12;
			sums += Math.sin(theta);
			sumc += Math.cos(theta);
		}
		double r = Math.sqrt(sums*sums + sumc*sumc);
		double var = 1 - r/hours.size();
		return var;
	}
	
	
	
	public static void main(String[] args) {
		double[][] hours = new double[][]{{0,23},{1,23},{2,23},{3,23},{4,23},{5,23},{6,23},{7,23},{8,23},{9,23},{10,23},{11,23},{12,23},{13,23},{14,23},{15,23},{16,23},{17,23},{18,23},{19,23},{20,23},{21,23},{22,23}};
		for(int i=0; i<hours.length;i++) {
			HourDescriptiveStatistics hds = new HourDescriptiveStatistics();
			hds.addAll(hours[i]);
			System.out.println(24-Math.abs(hours[i][0]-hours[i][1])+"\tmean\t"+hds.mean()+"\tvariance\t"+hds.variance());
		}
	}
	
}
