package cdrindividual.tourist;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import cdrindividual.CDR;
import cdrindividual.presence_at_event.PresenceCounter;
import region.CityEvent;
import region.Placemark;
import utils.Config;
import utils.CopyAndSerializationUtils;
import utils.Sort;
import visual.r.RPlotter;
import visual.text.TextPlotter;

public class TouristEventAnalysis {
	
	
	public static void main(String[] args) throws Exception {
		
		int bestR = 700;
		
		Config.getInstance().pls_start_time = new GregorianCalendar(2014,Calendar.OCTOBER,20,0,0,0);
		Config.getInstance().pls_end_time = new GregorianCalendar(2014,Calendar.OCTOBER,31,23,59,59);
		Placemark placemark = new Placemark("Lingotto Fiere",new double[]{45.0320367,7.6653584},bestR);
		Calendar st = new GregorianCalendar(2014,Calendar.OCTOBER,23,0,0,0);
		Calendar et = new GregorianCalendar(2014,Calendar.OCTOBER,27,23,0,0);
		CityEvent ce = new CityEvent(placemark,st,et,200000); // salone del gusto 2014	
		
		Calendar bst = new GregorianCalendar(2014,Calendar.OCTOBER,21,0,0,0);
		Calendar bet = new GregorianCalendar(2014,Calendar.NOVEMBER,14,23,0,0);
		CityEvent baseperiod = new CityEvent(placemark,bst,bet,200000); // salone del gusto 2014	
		
		System.out.println("BESTR = "+bestR);
	
		Map<String,Double> participants = getParticipants(Config.getInstance().base_folder+"/UserEventCounter/file_pls_piem_Torino_cellXHour_Oct2014.csv",ce,baseperiod);
		System.out.println("TOT = "+participants.size());
		double tot = 0;
		for(double prob: participants.values())
			tot += prob;
		System.out.println("TOT PROB. = "+tot);
		tot = Math.round(5.75 * tot + 17139);
		System.out.println("ESTIMATED ATT = "+tot);
		
		
		
		Map<String,Integer> prof2index = new HashMap<String,Integer>();
		int index = 0;
		for(String profile: GTExtractor.PROFILES) {
			prof2index.put(profile, index);
			index ++;
		}
		
		double[] profcount = new double[GTExtractor.PROFILES.length];
		
		Map<String,Double> countrycount = new HashMap<String,Double>();
		
		Map<String,String> user_prof = (Map<String,String>)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/Tourist/Torino_Oct2014_noregion_classes.ser"));
		
		for(String u: participants.keySet()) {
			double prob = participants.get(u);
			String[] user_country = u.split(";");
			String user = user_country[0];
			String country = user_country[1];
			String p = user_prof.get(user);
			profcount[prof2index.get(p)]+=prob;
			
			Double d = countrycount.get(country);
			if(d==null) d = 0.0;
			countrycount.put(country,d+prob);
		}
		

		
		String[] countries = new String[6];
		double[] prob = new double[countries.length];
		int i = 0;
		LinkedHashMap<String, Double> o_countrycount = Sort.sortHashMapByValuesD(countrycount,Collections.reverseOrder());
		for(String country: o_countrycount.keySet()) {
			countries[i] = country;
			prob[i] = o_countrycount.get(country);
			i++;
			if(i >= countries.length) break;
		}
		
		double sum = 0;
		for(i=0; i<profcount.length;i++)
			sum += profcount[i];
		for(i=0; i<profcount.length;i++)
			profcount[i] = Math.round(100.0*profcount[i]/sum);
		
		sum = 0;
		for(i=0; i<prob.length;i++)
			sum += prob[i];
		for(i=0; i<prob.length;i++)
			prob[i] = Math.round(100.0*prob[i]/sum);;
		 
		for(String profile: GTExtractor.PROFILES)
			System.out.println(profile+" ==> "+profcount[prof2index.get(profile)]);
		for(i=0; i<countries.length;i++)
			System.out.println(countries[i]+" ==> "+prob[i]);
		
		RPlotter.drawBar(GTExtractor.PROFILES, profcount, "profiles", "%", Config.getInstance().paper_folder+"/img/event/profiles.pdf", null);
		RPlotter.drawBar(countries, prob, "countries", "%", Config.getInstance().paper_folder+"/img/event/countries.pdf", null);
		
		Map<String,Object> tm = new HashMap<String,Object>();
		tm.put("bestR", bestR);
		tm.put("tot", tot);
		TextPlotter.getInstance().run(tm, "src/analysis/tourist/TouristEventAnalysis.ftl", Config.getInstance().paper_folder+"/img/event/TouristEventAnalysis.tex");
	}
	

	
	public static Map<String,Double> getParticipants(String cellXHourFile, CityEvent ce, CityEvent baseperiod) throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(new File(cellXHourFile)));
		
		// username, confidence
		Map<String,Double> participants = new HashMap<String,Double>(); 
		
		Map<String,String> mncT = TouristStatistics.mncT();
		
		
		String line;
		while((line=br.readLine())!=null) {
			if(line.startsWith("//")) continue;
			List<CDR> l = CDR.getDataFormUserEventCounterCellacXHourLine(line);
			
			
			for(CDR e: l) {
				if(ce.spot.contains(e.getCellac()) && e.getCalendar().after(ce.st) && e.getCalendar().before(ce.et)) {
					double f1 = PresenceCounter.fractionOfTimeInWhichTheUserWasAtTheEvent(l,ce,null,false);
					f1 = f1 * 5;
					if(f1 > 1) f1 = 1;
					double f2 = PresenceCounter.fractionOfTimeInWhichTheUserWasAtTheEvent(l,baseperiod,ce,false);
					participants.put(e.getUsername()+";"+mncT.get(e.getIMSI().substring(0,3)), f1*(1-f2));
					break;
				}
				if(e.getCalendar().after(ce.et))
					break;
			}
		}
		br.close();
		
		return participants;
	}
	
}
