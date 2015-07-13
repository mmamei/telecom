package analysis;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.math3.stat.regression.SimpleRegression;

import utils.Config;
import utils.fastdtw.dtw.DTW;
import utils.fastdtw.dtw.FastDTW;
import utils.fastdtw.timeseries.TimeSeries;
import utils.fastdtw.timeseries.TimeSeriesPoint;
import utils.fastdtw.util.DistanceFunctionFactory;
import utils.time.TimeConverter;
import visual.html.GoogleChartGraph;
import visual.r.RPlotter;

public class TimeDensityFromAggregatedData {
	
	private String city;
	private String type;
	
	public TimeConverter tc = null;
	public Map<String,double[]> map = new HashMap<String,double[]>();
	
	
	public TimeDensityFromAggregatedData(String city, String type, String file, int[] readIndexes, Set<String> okMeta) {
		this.city = city;
		this.type = type;
		try {
			tc = TimeConverter.getInstance();
			if(file.endsWith(".tar.gz")) {
				TarArchiveInputStream tarInput = new TarArchiveInputStream(new GzipCompressorInputStream(new FileInputStream(file)));
				TarArchiveEntry currentEntry = tarInput.getNextTarEntry();
				
				while (currentEntry != null) {
					processFile(new BufferedReader(new InputStreamReader(tarInput)),readIndexes,okMeta); // Read directly from tarInput
				    //System.out.println("Reading File = " + currentEntry.getName()); 
				    currentEntry = tarInput.getNextTarEntry(); 
				}
				tarInput.close();
			}
			else {
				processFile(new BufferedReader(new FileReader(file)),readIndexes,okMeta);
			}
			
		}catch(Exception e) {
			e.printStackTrace();
		}	
	}
	
	private void processFile(BufferedReader br,int[] readIndexes,Set<String> okMeta) {
		String line = null;
		try {
			while ((line = br.readLine()) != null) {
			       String[] x = line.split("\t");
			       long time = Long.parseLong(x[readIndexes[0]]) * 1000;
			       String cell = x[readIndexes[1]];
			       double value = Double.parseDouble(x[readIndexes[2]]);
			       String meta = x[readIndexes[3]];
			       if(okMeta == null || okMeta.contains(meta)) {
			       //double calltime = Double.parseDouble(x[4]);
				       double[] v = map.get(cell);
				       if(v == null) {
				       	   v = new double[tc.getTimeSize()];
				       	   map.put(cell, v);
				       }
				       //System.out.println(time+" --> "+new Date(time));
				       v[tc.time2index(time)]+= value;
			       }
			}
			//br.close();
		}catch(Exception e) {
			System.err.println(line);
			e.printStackTrace();
		}	
	}
	
	
	public void add(TimeDensityFromAggregatedData td) {
		try {
			if(!city.equals(td.city)) throw new Exception("Cannot add TimeDensityFromAggregatedData of different cities");
			type = type+"-"+td.type;
			for(String r: map.keySet()) {
				double[] v = map.get(r);
				double[] w = td.map.get(r);
				if(w != null)
					for(int i=0; i<v.length;i++)
						v[i] += w[i];
			}
			for(String r: td.map.keySet()) {
				if(map.get(r) == null)
					map.put(r, td.map.get(r));
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
	
	public double pearson(double[] a, double[] b) {
		SimpleRegression r = new SimpleRegression();
		Calendar cal = Calendar.getInstance();
		for(int i=0;i<a.length;i++) {
			cal.setTimeInMillis(tc.index2time(i));
			int hour = cal.get(Calendar.HOUR_OF_DAY);
			int day_of_week = cal.get(Calendar.DAY_OF_WEEK);
			if(hour>7 && hour<18 && day_of_week != Calendar.SATURDAY && day_of_week != Calendar.SUNDAY)
			r.addData(a[i], b[i]);
		}
		return r.getR();
	}
	
	
	public double maxxCorr(double[] a, double[] b) {
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
    public double[] xcorr(double[] a, double[] b)
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
    public double[] xcorr(double[] a)
    {
        return xcorr(a, a);
    }

    /**
     * Computes the cross correlation between sequences a and b.
     * maxlag is the maximum lag to
     */
    public double[] xcorr(double[] a, double[] b, int maxlag)
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
	public double ed(double[] x, double[] y) {
		double d = 0;
		for(int i=0; i<x.length;i++)
			d+= Math.pow(x[i]-y[i], 2);
		return Math.sqrt(d);
	}
	
	public double dtw(double[] x, double[] y) {
		TimeSeries tsI = new TimeSeries(1);
		for(int i=0; i<x.length;i++)
			tsI.addLast(i, new TimeSeriesPoint(new double[] {x[i]}));
		
		TimeSeries tsJ = new TimeSeries(1);
		for(int i=0; i<y.length;i++)
			tsJ.addLast(i, new TimeSeriesPoint(new double[] {y[i]}));
		
		return DTW.getWarpDistBetween(tsI, tsJ, DistanceFunctionFactory.EUCLIDEAN_DIST_FN);
	}
	
	public double fdtw(double[] x, double[] y) {
		TimeSeries tsI = new TimeSeries(1);
		for(int i=0; i<x.length;i++)
			tsI.addLast(i, new TimeSeriesPoint(new double[] {x[i]}));
		
		TimeSeries tsJ = new TimeSeries(1);
		for(int i=0; i<y.length;i++)
			tsJ.addLast(i, new TimeSeriesPoint(new double[] {y[i]}));
		
		return FastDTW.getWarpDistBetween(tsI, tsJ, DistanceFunctionFactory.EUCLIDEAN_DIST_FN);
	}


	
	public String getCity() {
		return city;
	}
	
	public String getType() {
		return type;
	}
	
	public void plot(String cell) {
		String[] x = tc.getTimeLabels();

		List<double[]> v = new ArrayList<double[]>();
		v.add(map.get(cell));
		
		List<String> names = new ArrayList<String>();
		names.add(cell);
		
		
		RPlotter.drawLine(x, v, names, "cell", "date", city+" "+type, Config.getInstance().base_folder+"/Images/tdc-"+city+"-"+type+".pdf", null);
		
		try {
			PrintWriter out = new PrintWriter(new FileWriter(Config.getInstance().base_folder+"/Images/tdc-"+city+"-"+type+".html"));
			out.println(GoogleChartGraph.getGraph(x, v, names, "date", type));
			out.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		String city = "venezia";
		String type = "CallOut";
		TimeDensityFromAggregatedData td = new TimeDensityFromAggregatedData(city,type,"G:/DATASET/TI-CHALLENGE-2015/TELECOM/"+city+"/"+type+".tar.gz",new int[]{0,1,2,3},null);
		//td.plot("3693_3_1_3_1");
		
		double fdtw = td.fdtw(td.map.get("3693_3_1_3_1"), td.map.get("3693_3_1_2_1"));
		System.out.println("fdtw = "+fdtw);
		
		double dtw = td.dtw(td.map.get("3693_3_1_3_1"), td.map.get("3693_3_1_2_1"));
		System.out.println("dtw = "+dtw);
		
		double ed = td.ed(td.map.get("3693_3_1_3_1"), td.map.get("3693_3_1_2_1"));
		System.out.println("ed = "+ed);
		
		System.out.println("Done!");
	}
		
		
		
}
