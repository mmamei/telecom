package analysis;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import region.RegionI;
import region.RegionMap;
import utils.Config;
import utils.CopyAndSerializationUtils;
import utils.StatsUtils;
import visual.r.RPlotter;
import analysis.densityANDflows.density.LoadDensityFromCompanyData;

public class SynchAnalysis {
	
	public static final String[] COMPANY_CONSTRAINTS = new String[]{"01-32","grande",""};
	public static final int LIMIT = 20;
	
	public static void main(String[] args) throws Exception {
		
		
		/*
		String[] city = new String[]{"torino","milano","venezia","roma","napoli","bari","palermo"};
		String type = "CallIn";
		String[] files = new String[city.length];
		for(int i=0; i<files.length;i++)
			files[i] = "G:/DATASET/TI-CHALLENGE-2015/TELECOM/"+city[i]+"/"+type+".tar.gz";
		int[] readIndexes = new int[]{0,1,2,3};
		Set<String> okMeta = new HashSet<String>();
		okMeta.add("39");
		*/
		
		String[] city = new String[]{"torino","milano","venezia","roma","napoli","bari","palermo"};
		String type = "resident";
		String[] files = new String[city.length];
		for(int i=0; i<files.length;i++)
			files[i] = "G:/DATASET/TI-CHALLENGE-2015/DEMOGRAPHIC/"+city[i]+"/callsLM_"+city[i].substring(0,2).toUpperCase()+"_CAP";
		int[] readIndexes = new int[]{0,1,3,2};
		
		Map<String, Set<String>> okMeta = new HashMap<String,Set<String>>();
		for(int i=0; i<files.length;i++) {
			Set<String> ok = new HashSet<String>();
			RegionMap rm = (RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/tic-"+city[i]+"-caps.ser"));
			for(RegionI r: rm.getRegions())
				ok.add(r.getName());
			okMeta.put(city[i], ok);	
		}
		
		
		List<double[]> values = new ArrayList<double[]>();
		List<String> names = new ArrayList<String>();
		
		for(int i=0; i<city.length;i++) {
			names.add(city[i].substring(0, 1).toUpperCase() + city[i].substring(1)); // capitalize first letter
			values.add(process(city[i],type,files[i],readIndexes,okMeta.get(city[i])).getValues());
		}
		RPlotter.drawBoxplot(values,names,"cities","pearson",Config.getInstance().base_folder+"/Images/boxplot-"+type+"-synch.pdf",null);
	}
	
	
	
	public static DescriptiveStatistics process(String city, String type, String file, int[] readIndexes, Set<String> okMeta) throws Exception {
		System.out.println("processign "+city);
		LinkedHashMap<String, Double> company_map = LoadDensityFromCompanyData.getInstance(city,COMPANY_CONSTRAINTS);
		
		DescriptiveStatistics ds = new DescriptiveStatistics();
		TimeDensityFromAggregatedData td = new TimeDensityFromAggregatedData(city,type,file,readIndexes,okMeta);
		
		List<Double> w = new ArrayList<Double>();
		List<double[]> z = new ArrayList<double[]>();
		
		int c = 0;
		for(String key: td.map.keySet()) {
			if(company_map.get(key)==null) continue;
			double[] zeta = StatsUtils.getZ(td.map.get(key));
			//double[] trimmed_zeta = new double[1000];
			//System.arraycopy(zeta, 0, trimmed_zeta, 0, 1000);
			z.add(zeta);
			Double x = company_map.get(key);
			if(x == null) x = 0.0;
			w.add(x);
			c++;
			if(LIMIT > 0 && c >= LIMIT) break;
		}
		
		/*
		for(String k: td.map.keySet()) {
			z.add(StatsUtils.getZ(td.map.get(k)));
			Double x = company_map.get(k);
			if(x == null) x = 0.0;
			w.add(x);
		}
		*/
		
		System.out.println(city+" --> "+z.size());
		int cont=0;
		for(int i=0; i<z.size();i++) {
			System.out.print(++cont+" ");
			if(cont%40 == 0) System.out.println();
			for(int j=i+1; j<z.size();j++) {
				//System.out.println(w.get(i)+" "+w.get(j));
				//ds.addValue(w.get(i)*w.get(j)*td.pearson(z.get(i), z.get(j)));
				ds.addValue(td.pearson(z.get(i), z.get(j)));
			}
		}	
		System.out.println();
		
		/*
		DescriptiveStatistics[] dsx = new DescriptiveStatistics[z.get(0).length];
		for(int i=0; i<dsx.length;i++)
			dsx[i] = new DescriptiveStatistics();
		Calendar cal = Calendar.getInstance();
		for(double[] zeta: z)
			for(int i=0; i<dsx.length;i++) {
				cal.setTimeInMillis(td.tc.index2time(i));
				int hour = cal.get(Calendar.HOUR_OF_DAY);
				if(hour>7 && hour<18)
					dsx[i].addValue(zeta[i]);
			}
		
		for(int i=0; i<dsx.length;i++)
			ds.addValue(dsx[i].getStandardDeviation());
		
		*/

	
		return ds;
	}
}
