package cdrindividual.tourist;

import java.io.File;
import java.util.Calendar;
import java.util.GregorianCalendar;

import cdrindividual.CDRSpaceDensity;
import cdrindividual.tourist.profiles.Resident;
import region.Placemark;
import region.RegionMap;
import utils.Config;
import utils.CopyAndSerializationUtils;
import utils.Mail;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;
import weka.filters.unsupervised.instance.RemoveWithValues;
import weka.filters.supervised.instance.Resample;

public class WekaPreprocess {
	
	
	/*
	 * This program pre-process all the files.
	 * It removes instances with missing values, removes the mnt field and subsample 2-4% of the population.
	 */
	
	public static Integer MAX_DAYS = null;
	public static final boolean COMPUTE_WITH_MAX_DAYS = false;
	public static final String KIND_OF_MAP = "TouristArea";
	
	public static void main(String[] args) throws Exception {
		//for(double FR = 0.2; FR <= 1.0; FR = FR + 0.2)
		//	runFR(FR);
		runFR(0.6);
	}
	
	public static void runFR(double FR) throws Exception {
		Resident.FR  = FR;
		
		
		
		GTExtractor.main(null);
		Mail.send("GTExtractor "+FR+" completed!");
		
		runAll();
		if(COMPUTE_WITH_MAX_DAYS)
		for(MAX_DAYS = 1; MAX_DAYS<=7; MAX_DAYS++)
			runAll();
		Mail.send("WekaPreprocess "+FR+" completed!");
		
		WekaTrainer.main(null);
		Mail.send("WekaTrainer "+FR+" completed!");
		
		WekaUseClassifier.main(null);
		Mail.send("WekaUseClassifier "+FR+" completed!");
		
		TouristStatistics.main(null);
		Mail.send("TouristStatistics "+FR+" completed!");
		
		//WekaCompareTimeLimitedData.main(null);
		//Mail.send("WekaCompareTimeLimitedData completed!");
		
		System.out.println("Done");
	}
	
	
	public static void runAll() throws Exception {
				
		//Config.getInstance().pls_start_time = new GregorianCalendar(2014,Calendar.AUGUST,1,0,0,0);
		//Config.getInstance().pls_end_time = new GregorianCalendar(2014,Calendar.AUGUST,31,23,59,59);
		//runProcess("file_pls_pu_","Lecce","_Aug2014",null);
		//runProcess("file_pls_pu_","Lecce","_Aug2014",Config.getInstance().base_folder+"/RegionMap/Lecce"+KIND_OF_MAP+".ser");
		
		//Config.getInstance().pls_start_time = new GregorianCalendar(2014,Calendar.SEPTEMBER,1,0,0,0);
		//Config.getInstance().pls_end_time = new GregorianCalendar(2014,Calendar.SEPTEMBER,31,23,59,59);
		//runProcess("file_pls_pu_","Lecce","_Sep2014",null);
		
		//Config.getInstance().pls_start_time = new GregorianCalendar(2014,Calendar.OCTOBER,1,0,0,0);
		//Config.getInstance().pls_end_time = new GregorianCalendar(2014,Calendar.OCTOBER,31,23,59,59);
		//runProcess("file_pls_piem_","Torino","_Oct2014",null);
		//runProcess("file_pls_piem_","Torino","_Oct2014",Config.getInstance().base_folder+"/RegionMap/Torino"+KIND_OF_MAP+".ser");
			
		Config.getInstance().pls_start_time = new GregorianCalendar(2013,Calendar.JULY,1,0,0,0);
		Config.getInstance().pls_end_time = new GregorianCalendar(2013,Calendar.JULY,31,23,59,59);
		//runProcess("file_pls_ve_","Venezia","_July2013",null);
		runProcess("file_pls_fi_","Firenze","_July2013",null);
		//runProcess("file_pls_fi_","Venezia","_July2013",Config.getInstance().base_folder+"/RegionMap/Venezia"+KIND_OF_MAP+".ser");	
		//runProcess("file_pls_fi_","Firenze","_July2013",Config.getInstance().base_folder+"/RegionMap/Firenze"+KIND_OF_MAP+".ser");	
				
		Config.getInstance().pls_start_time = new GregorianCalendar(2014,Calendar.MARCH,1,0,0,0);
		Config.getInstance().pls_end_time = new GregorianCalendar(2014,Calendar.MARCH,31,23,59,59);
		//runProcess("file_pls_ve_","Venezia","_March2014",null);
		runProcess("file_pls_fi_","Firenze","_March2014",null);
		//runProcess("file_pls_ve_","Venezia","_March2014",Config.getInstance().base_folder+"/RegionMap/Venezia"+KIND_OF_MAP+".ser");
		//runProcess("file_pls_fi_","Firenze","_March2014",Config.getInstance().base_folder+"/RegionMap/Firenze"+KIND_OF_MAP+".ser");
	}
	
	public static void runProcess(String pre, String placemark, String post, String region) throws Exception {
		createARFF(pre,placemark,post,region);
		preprocess(placemark,post,region);
	}
	
	
	public static void createARFF(String pre, String city, String month, String regionSerFile) throws Exception {
		File f = regionSerFile == null ? null : new File(regionSerFile);
		//System.out.println(f.getName());
		String cellXHourFile =Config.getInstance().base_folder+"/UserEventCounter/"+pre+city+"_cellXHour"+month+".csv";
		String gt_ser_file = Config.getInstance().base_folder+"/Tourist/"+city+"_gt_profiles"+month+".ser";
		String weka_file = Config.getInstance().base_folder+"/Tourist/"+city+month+(regionSerFile==null ? "_noregion" : "_"+f.getName().substring(0,f.getName().lastIndexOf(".ser")))+".arff";
		
		if(MAX_DAYS!=null) weka_file = weka_file.replaceAll(".arff", "_maxdays"+MAX_DAYS+".arff");
		
		Placemark placemark = Placemark.getPlacemark(city);
		RegionMap rm = regionSerFile == null ? null : (RegionMap)CopyAndSerializationUtils.restore(new File(regionSerFile));
		CDRSpaceDensity.process(rm,cellXHourFile,gt_ser_file,null,MAX_DAYS,weka_file,placemark);
	}
	
	public static void preprocess(String city, String month, String regionSerFile) throws Exception {
		String inFile = Config.getInstance().base_folder+"/Tourist/"+city+month+(regionSerFile==null ? "_noregion" : "_"+regionSerFile.substring(regionSerFile.lastIndexOf("/")+1,regionSerFile.lastIndexOf(".ser")))+".arff";
		if(MAX_DAYS!=null) inFile = inFile.replaceAll(".arff", "_maxdays"+MAX_DAYS+".arff");
		System.out.println(inFile);
		DataSource source = new DataSource(inFile);
		Instances data = source.getDataSet();
		double resample = data.numInstances() < 200000 ? 4 : 2;
		data.setClassIndex(data.attribute("class").index());
		
		// filter out instances with missing (class) values.
		RemoveWithValues rwv = new RemoveWithValues();
		rwv.setMatchMissingValues(true);
		rwv.setInvertSelection(true);
		rwv.setInputFormat(data);
		data = Filter.useFilter(data, rwv);
		
		System.out.println("***** Attributes:");
		for(int i=0; i<data.numAttributes();i++)
			System.out.println((i+1)+" --> "+data.attribute(i).name());
		System.out.println("***** Num instances: "+data.numInstances());
		
		rwv = new RemoveWithValues();
		rwv.setAttributeIndex("6"); // num_days_in_area
		rwv.setSplitPoint(0.5);
		rwv.setInputFormat(data);
		data = Filter.useFilter(data, rwv);
		
		// resample 2-4% of the instances
		Resample res = new Resample();
		res.setSampleSizePercent(resample);
		res.setNoReplacement(true);
		res.setInputFormat(data);
		res.setBiasToUniformClass(1);
		data = Filter.useFilter(data, res);
		
		// remove attribute mnt
		Remove rem = new Remove();
		rem.setAttributeIndices("1,2");
		rem.setInputFormat(data);
		data = Filter.useFilter(data, rem);
		
		ArffSaver saver = new ArffSaver();
		saver.setInstances(data);
		String outFile = Config.getInstance().base_folder+"/Tourist/Resampled/"+city+month+(regionSerFile==null ? "_noregion" : "_"+regionSerFile.substring(regionSerFile.lastIndexOf("/")+1,regionSerFile.lastIndexOf(".ser")))+"_resampled.arff";
		if(MAX_DAYS!=null) outFile = outFile.replaceAll("_resampled.arff", "_maxdays"+MAX_DAYS+"_resampled.arff");
		saver.setFile(new File(outFile));
		saver.writeBatch();
	}
	
}
