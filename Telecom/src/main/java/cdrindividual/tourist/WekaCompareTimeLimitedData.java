package cdrindividual.tourist;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import utils.Config;
import visual.r.RPlotter;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;
import weka.filters.unsupervised.instance.RemoveWithValues;
import weka.filters.unsupervised.instance.Resample;

public class WekaCompareTimeLimitedData {
	
	private static final String CLASSIFIER = "j48";
	
	public static void main(String[] args) throws Exception {
		List<double[]> pctCorrect_noregion = runAll(new String[]{
				"Venezia_March2014_noregion"
				,"Firenze_March2014_noregion"    // note, it is very useful to put the commas this way to easily comment in-out
				,"Lecce_Aug2014_noregion"
				,"Torino_Oct2014_noregion"
				});
		List<double[]> pctCorrect_region = runAll(new String[]{
				"Venezia_March2014_Venezia"+WekaPreprocess.KIND_OF_MAP
				,"Firenze_March2014_Firenze"+WekaPreprocess.KIND_OF_MAP
				,"Lecce_Aug2014_Lecce"+WekaPreprocess.KIND_OF_MAP
				,"Torino_Oct2014_Torino"+WekaPreprocess.KIND_OF_MAP
				});
		
		
		
		for(int days=0; days<pctCorrect_noregion.get(0).length;days++) {
			double max = 0;
			double avg = 0;
			for(int i=0; i<pctCorrect_noregion.size();i++) {
				double improvement = pctCorrect_region.get(i)[days] - pctCorrect_noregion.get(i)[days];
				avg += improvement;
				max = Math.max(max, improvement);
			}
			avg /= pctCorrect_noregion.size();
			String label = days == pctCorrect_noregion.get(0).length -1 ? "All" : String.valueOf(days+1);
			System.out.println(label+" ==> avg =  "+avg+", max = "+max);
		}
		
	}
	
	public static List<double[]> runAll(String[] experiments) throws Exception {
		String[] x = null;
		
		List<String> names = new ArrayList<String>();
		List<double[]> y = new ArrayList<double[]>();
		for(String exp: experiments) {
			Map<String, Evaluation> map = process(exp);       
			
			
			if(x == null) {
				 x = new String[map.size()];
				for(int i=0;i<x.length-1;i++)
					x[i] = String.valueOf(i+1);
				x[x.length-1] = "All";
			}
			
			
			
			double[] pctCorrect = new double[map.size()];
			List<double[]> recall = new ArrayList<double[]>();
			for(int i=0; i<GTExtractor.PROFILES.length;i++)
				recall.add(new double[map.size()]);
			
			
			// this for-loop basically runs through the x-axis
			for(String k: map.keySet()) {
				int index = pctCorrect.length-1;
				int i = k.indexOf("maxdays");
				if(i > 0)  index = Integer.parseInt(k.substring(i+"maxdays".length(),k.indexOf(".arff"))) -1;
				
				// now index is the x-value to be considered
				
				Evaluation eval =  map.get(k);
				
				pctCorrect[index] =eval.pctCorrect();
				
				for(int j=0; j<recall.size();j++) {
					recall.get(j)[index] = 100.0 * eval.recall(j);
				}
			}
			
				
			List<String> profiles = new ArrayList<String>();
			for(String p:  GTExtractor.PROFILES)
				profiles.add(p);
	
			
			
			RPlotter.drawLine(x, recall,profiles, "profiles", "days of data", "recall %", Config.getInstance().paper_folder+"/img/timelimiteddata/recall"+exp+"-"+CLASSIFIER+".pdf", "theme(legend.position=c(0.8, 0.5),legend.background = element_rect(size=1))");
			
			
			
			names.add(exp.substring(0,exp.lastIndexOf("_")));
			y.add(pctCorrect);
		}
		
		String filename = experiments[0].endsWith("_noregion") ? "accuracy-noregion-"+CLASSIFIER+".pdf" : "accuracy-"+WekaPreprocess.KIND_OF_MAP+"-"+CLASSIFIER+".pdf";
		
		RPlotter.drawLine(x, y, names, "Regions", "days of data", "accuracy", Config.getInstance().paper_folder+"/img/timelimiteddata/"+filename, "theme(legend.position=c(0.8, 0.5),legend.background = element_rect(size=1))");
		
		return y;
		
	
	}
	
	public static Map<String, Evaluation> process(String prefix) throws Exception {
		
		// place --> pct correct for various classifiers
		Map<String, Evaluation> map = new TreeMap<String,Evaluation>();
	
		File dir = new File(Config.getInstance().base_folder+"/Tourist");
		for(File f: dir.listFiles()) {
			
			if(f.getName().startsWith(prefix) && f.getAbsolutePath().endsWith(".arff")) {
				
				File classifier = new File(Config.getInstance().base_folder+"/Tourist/Resampled/"+f.getName().replaceAll(".arff", "_resampled_"+CLASSIFIER+".model"));
				if(classifier.exists()) 
					map.put(f.getName(), classify(f.getAbsolutePath(),classifier.getAbsolutePath(),100));
				else
					System.out.println("Classifier "+CLASSIFIER+" for "+f.getName()+" not found!");
			}	
		}
		
	
		return map;		
	}
	
	
	
	
	public static Evaluation classify(String arff, String model, double resample) throws Exception {
		
		//System.out.println("\n*************************************************************************************************************");
		//System.out.println("RUNNING CLASSIFIER "+model+"\n");
		
		DataSource source = new DataSource(arff);
		Instances data = source.getDataSet();
		data.setClassIndex(data.attribute("class").index());
		
		
		/*
		System.out.println("***** Attributes:");
		for(int i=0; i<data.numAttributes();i++)
			System.out.println((i+1)+" --> "+data.attribute(i).name());
		System.out.println("***** Num instances: "+data.numInstances());
		*/
		
		
		RemoveWithValues rwv = new RemoveWithValues();
		rwv.setAttributeIndex("6"); // num_days_in_area
		rwv.setSplitPoint(0.5);
		rwv.setInputFormat(data);
		data = Filter.useFilter(data, rwv);
		
		Resample res = new Resample();
		res.setSampleSizePercent(resample);
		res.setNoReplacement(true);
		res.setInputFormat(data);
		data = Filter.useFilter(data, res);
	
		Classifier cls = (Classifier) weka.core.SerializationHelper.read(model);
		
		Remove rem = new Remove();
		rem.setAttributeIndices("1,2");
		rem.setInputFormat(data);
		Instances cdata = Filter.useFilter(data, rem);
		
		
		
		Evaluation eval = new Evaluation(cdata);
		eval.evaluateModel(cls, cdata);
		//System.out.println(eval.toSummaryString("\nResults\n======\n", false));
		//System.out.println("RUNNING CLASSIFIER "+model);
		//System.out.println(eval.toMatrixString());
		
		return eval;
	}
}

