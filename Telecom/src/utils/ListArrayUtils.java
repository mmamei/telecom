package utils;

import java.util.List;

public class ListArrayUtils {
	
	public static double[] toArray(List<Double> x) {
		double[] y = new double[x.size()];
		for(int i=0; i<y.length;i++)
			y[i] = x.get(i);
		return y;
	}
	
}
