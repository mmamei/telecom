package utils;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Random;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.inference.TestUtils;
import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;
import org.apache.commons.math3.stat.regression.SimpleRegression;

import utils.fastdtw.dtw.DTW;
import utils.fastdtw.dtw.FastDTW;
import utils.fastdtw.timeseries.TimeSeries;
import utils.fastdtw.timeseries.TimeSeriesPoint;
import utils.fastdtw.util.DistanceFunctionFactory;
import utils.time.TimeConverter;
import visual.java.GraphPlotter;

public class StatsUtils {
	
	
	public static void main(String[] args) {
		double[] z = new double[100000];
		Random r = new Random();
		for(int i=0; i<z.length;i++)
			z[i] = r.nextGaussian();
		checkNormalDistrib(z,true);
		
		DescriptiveStatistics ds = new DescriptiveStatistics();
		for(int i=0; i<z.length;i++)
			ds.addValue(r.nextDouble());
		
		double m = ds.getMean();
		double s = ds.getStandardDeviation();
		z = ds.getValues();
		for(int i=0; i<z.length;i++)
			z[i] = (z[i] - m)/s;
		checkNormalDistrib(z,true);
	}

	
	
	/*
	 * This function used a Chi2-test to see whether z comes from N(0,1) o not
	 */
	
	public static boolean checkNormalDistrib(double[] z, boolean verbose) {
		return checkNormalDistrib(z,verbose,"Distribution");
	}
	public static boolean checkNormalDistrib(double[] z, boolean verbose, String title) {
		
		double[] sets = new double[]{Double.NEGATIVE_INFINITY,-3,-2,-1,0,1,2,3,Double.POSITIVE_INFINITY};
		
		NormalDistribution nd = new NormalDistribution();
		
		long[] count = new long[sets.length-1];
		for(int j=0; j<z.length;j++) {
			for(int i=0;i<sets.length-1;i++) 
				if(sets[i] < z[j] && z[j] <= sets[i+1]) {
					count[i]++;
					break;
				}
		}
		double[] dcount = toDouble(count);
		double[] expected_count = new double[count.length];
		try {
		for(int i=0;i<sets.length-1;i++) 
			expected_count[i] = nd.cumulativeProbability(sets[i], sets[i+1]) * z.length; 
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		if(verbose) {
			Logger.logln("["+toString(dcount,false)+"] should be ["+toString(expected_count,false)+"]");
			Logger.logln("["+toString(dcount,true)+"] should be ["+toString(expected_count,true)+"]");
		}
		
		boolean test = false;
		
		try {
			double p_value = TestUtils.chiSquareTest(expected_count, count);
			test = p_value >= 0.05;
			if(verbose) {
				if(!test) System.out.println("Reject H0: Data does not come from N(0,1)");
				if(test) System.out.println("Accept H0: Data comes from N(0,1)");
				
				String[] domain = new String[count.length];
				for(int i=0; i<domain.length;i++)
					domain[i] = sets[i]+"_"+sets[i+1];
				
				GraphPlotter g = GraphPlotter.drawGraph(title, title, "count", "x", "data", domain, dcount);
				g.addData("n(0,1)", expected_count);
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return test;
	}
	
	private static String toString(double[] x, boolean perc) {
		
		double sum=0;
		for(int i=0; i<x.length;i++)
			sum += x[i];
		
		String s = "";
		for(int i=0; i<x.length;i++) {
			s = s + "," + (perc? (int)(100*x[i]/sum)+"%" : (int)x[i]);
		}
		return s.substring(1);
	}
	
	private static double[] toDouble(long[] x) {
		double[] y = new double[x.length];
		for(int i=0; i<x.length;i++)
			y[i] = x[i];
		return y;
	}
	
	

	public static double[] getZH(DescriptiveStatistics stat, Calendar startTime) {
		
		DescriptiveStatistics[] hstats = new DescriptiveStatistics[24];
		for(int i=0; i<hstats.length;i++)
			hstats[i] = new DescriptiveStatistics();
		
		
		Calendar cal = (Calendar)startTime.clone();
		double[] vals = stat.getValues();
		for(int i=0; i<vals.length;i++) {
				hstats[cal.get(Calendar.HOUR_OF_DAY)].addValue(vals[i]);
			cal.add(Calendar.HOUR_OF_DAY, 1);
		}
		
		double[] hmeans = new double[24];
		double[] hsigmas = new double[24];
		
		for(int i=0; i<hstats.length;i++) {
			hmeans[i] = hstats[i].getMean();
			hsigmas[i] = hstats[i].getStandardDeviation();
		}
		
		
		double[] z = stat.getValues();
		
		
		cal = (Calendar)startTime.clone();
		for(int i=0; i<vals.length;i++) {
			
			if( hsigmas[cal.get(Calendar.HOUR_OF_DAY)] == 0)
				z[i] = 0;
			else
				z[i] = (z[i] - hmeans[cal.get(Calendar.HOUR_OF_DAY)]) / hsigmas[cal.get(Calendar.HOUR_OF_DAY)];
			cal.add(Calendar.HOUR_OF_DAY, 1);
		}
		
		
		for(int i=0; i<z.length;i++) {
			if(z[i] < 0) z[i] = 0;
		}
		return z;
	}
	
	

	
	public static double[] getZ(DescriptiveStatistics stat, Calendar startTime) {
		
		DescriptiveStatistics stat2 = new DescriptiveStatistics();
		Calendar cal = (Calendar)startTime.clone();
		double[] vals = stat.getValues();
		for(int i=0; i<vals.length;i++) {
			if(cal.get(Calendar.HOUR_OF_DAY) > 10 && vals[i] > 0)
				stat2.addValue(vals[i]);
			cal.add(Calendar.HOUR_OF_DAY, 1);
		}
		
		double mean = stat2.getMean();
		double sigma = stat2.getStandardDeviation();
		double[] z = stat.getValues();
		for(int i=0; i<z.length;i++) {
			z[i] = (z[i] - mean) / sigma;
			if(z[i] < 0) z[i] = 0;
		}
		return z;
	}
	
	
	
	public static double[] getZH(double[] x, TimeConverter tc) {
		
		DescriptiveStatistics[] stat = new DescriptiveStatistics[24];
		for(int i=0; i<stat.length;i++)
			stat[i] = new DescriptiveStatistics();
		
		Calendar cal = Calendar.getInstance();
		for(int i=0; i<x.length;i++) {
				cal.setTimeInMillis(tc.index2time(i));
				int h = cal.get(Calendar.HOUR_OF_DAY);
				stat[h].addValue(x[i]);
		}
			
		double[] mean = new double[24];
		double[] sd = new double[24];
		for(int i=0; i<24;i++) {
			mean[i] = stat[i].getMean();
			sd[i] = stat[i].getStandardDeviation();
		}
		
		double[] z = new double[x.length];
		for(int i=0; i<x.length;i++) {
			cal.setTimeInMillis(tc.index2time(i));
			int h = cal.get(Calendar.HOUR_OF_DAY);
			z[i] = sd[h] > 0 ? (x[i] - mean[h])/sd[h] : 0;
		}
		return z;
		
	}
	
	// divide by the mean rather than standard deviation
	public static double[] getZmH(double[] x, TimeConverter tc) {
		
		DescriptiveStatistics[] stat = new DescriptiveStatistics[24];
		for(int i=0; i<stat.length;i++)
			stat[i] = new DescriptiveStatistics();
		
		Calendar cal = Calendar.getInstance();
		for(int i=0; i<x.length;i++) {
				cal.setTimeInMillis(tc.index2time(i));
				int h = cal.get(Calendar.HOUR_OF_DAY);
				stat[h].addValue(x[i]);
		}
			
		double[] mean = new double[24];
		double[] sd = new double[24];
		for(int i=0; i<24;i++) {
			mean[i] = stat[i].getMean();
			sd[i] = stat[i].getStandardDeviation();
		}
		
		double[] z = new double[x.length];
		for(int i=0; i<x.length;i++) {
			cal.setTimeInMillis(tc.index2time(i));
			int h = cal.get(Calendar.HOUR_OF_DAY);
			z[i] = mean[h] > 0 ? (x[i] - mean[h])/mean[h] : 0;
		}
		return z;
		
	}
	
	
	
	public static double[] getZ(double[] x) {
		DescriptiveStatistics stat = new DescriptiveStatistics();
		for(int i=0; i<x.length;i++)
			stat.addValue(x[i]);
		double m = stat.getMean();
		double sd = stat.getStandardDeviation();
		double[] z = new double[x.length];
		for(int i=0; i<z.length;i++)
			z[i] = (x[i]-m)/sd;
		return z;
	}
	
	public static  double r2(double[] a, double[] b) {
		
		SimpleRegression r = new SimpleRegression();
		for(int i=0;i<a.length;i++) 
				r.addData(a[i], b[i]);
		
		double r2 = r.getRSquare();
		return Double.isNaN(r2) ? 0: r2;
		
		/*
		OLSMultipleLinearRegression h0 = new OLSMultipleLinearRegression();
		double[][] x = new double[b.length][1];
		for(int i=0; i<b.length;i++)
			x[i][0] = b[i];
		
		h0.newSampleData(a, x);
		return h0.calculateRSquared();
		*/
	}
	
	public static  double r(double[] a, double[] b) {
		
		SimpleRegression sr = new SimpleRegression();
		for(int i=0;i<a.length;i++) 
				sr.addData(a[i], b[i]);
		
		
		double r = sr.getR();
		return Double.isNaN(r) ? 0: r;
		
		/*
		OLSMultipleLinearRegression h0 = new OLSMultipleLinearRegression();
		double[][] x = new double[b.length][1];
		for(int i=0; i<b.length;i++)
			x[i][0] = b[i];
		
		h0.newSampleData(a, x);
		return h0.calculateRSquared();
		*/
	}
	
	
	public static double maxxCorr(double[] a, double[] b) {
		double[] xcorr = xcorr(a,b);
		double max = xcorr[0];
		for(int i=1; i<xcorr.length;i++)
			if(max < xcorr[i])
				max = xcorr[i];
		return max;
	}
	
	
	 /**
     * Computes the cross correlation between sequences a and b.
     */
    public static  double[] xcorr(double[] a, double[] b)
    {
        int len = a.length;
        if(b.length > a.length)
            len = b.length;

        return xcorr(a, b, len-1);

        // // reverse b in time
        // double[] brev = new double[b.length];
        // for(int x = 0; x < b.length; x++)
        //     brev[x] = b[b.length-x-1];
        // 
        // return conv(a, brev);
    }

    /**
     * Computes the auto correlation of a.
     */
    public static double[] xcorr(double[] a)
    {
        return xcorr(a, a);
    }

    /**
     * Computes the cross correlation between sequences a and b.
     * maxlag is the maximum lag to
     */
    public static double[] xcorr(double[] a, double[] b, int maxlag)
    {
        double[] y = new double[2*maxlag+1];
        Arrays.fill(y, 0);
        
        for(int lag = b.length-1, idx = maxlag-b.length+1; 
            lag > -a.length; lag--, idx++)
        {
            if(idx < 0)
                continue;
            
            if(idx >= y.length)
                break;

            // where do the two signals overlap?
            int start = 0;
            // we can't start past the left end of b
            if(lag < 0) 
            {
                //System.out.println("b");
                start = -lag;
            }

            int end = a.length-1;
            // we can't go past the right end of b
            if(end > b.length-lag-1)
            {
                end = b.length-lag-1;
                //System.out.println("a "+end);
            }

            //System.out.println("lag = " + lag +": "+ start+" to " + end+"   idx = "+idx);
            for(int n = start; n <= end; n++)
            {
                //System.out.println("  bi = " + (lag+n) + ", ai = " + n); 
                y[idx] += a[n]*b[lag+n];
            }
            //System.out.println(y[idx]);
        }

        return(y);
    }
	
	
	// euclidean distance
	public static double ed(double[] x, double[] y) {
		double d = 0;
		for(int i=0; i<x.length;i++)
			d+= Math.pow(x[i]-y[i], 2);
		return Math.sqrt(d);
	}
	
	public static double dtw(double[] x, double[] y) {
		TimeSeries tsI = new TimeSeries(1);
		for(int i=0; i<x.length;i++)
			tsI.addLast(i, new TimeSeriesPoint(new double[] {x[i]}));
		
		TimeSeries tsJ = new TimeSeries(1);
		for(int i=0; i<y.length;i++)
			tsJ.addLast(i, new TimeSeriesPoint(new double[] {y[i]}));
		
		return DTW.getWarpDistBetween(tsI, tsJ, DistanceFunctionFactory.EUCLIDEAN_DIST_FN);
	}
	
	public static  double fdtw(double[] x, double[] y) {
		TimeSeries tsI = new TimeSeries(1);
		for(int i=0; i<x.length;i++)
			tsI.addLast(i, new TimeSeriesPoint(new double[] {x[i]}));
		
		TimeSeries tsJ = new TimeSeries(1);
		for(int i=0; i<y.length;i++)
			tsJ.addLast(i, new TimeSeriesPoint(new double[] {y[i]}));
		
		return FastDTW.getWarpDistBetween(tsI, tsJ, DistanceFunctionFactory.EUCLIDEAN_DIST_FN);
	}
	
}
