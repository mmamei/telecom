package cdrindividual.tourist;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cdrindividual.CDR;
import cdrindividual.tourist.profiles.Commuter;
import cdrindividual.tourist.profiles.Excursionist;
import cdrindividual.tourist.profiles.Profile;
import cdrindividual.tourist.profiles.Resident;
import cdrindividual.tourist.profiles.Tourist;
import cdrindividual.tourist.profiles.Transit;
import region.Placemark;
import utils.Config;
import utils.CopyAndSerializationUtils;
import utils.Logger;

public class GTExtractor {
	
	public static final String[] PROFILES = {"Resident","Tourist","Commuter","Transit","Excursionist"};
	public final static Map<String,Integer> P2I; // profile to index
	public static String STRING_PROFILES = "";
	static {
		P2I = new HashMap<String,Integer>();
		for(int i=0; i<PROFILES.length;i++) {
			STRING_PROFILES += ","+PROFILES[i];
			P2I.put(PROFILES[i], i);
		}
		STRING_PROFILES = STRING_PROFILES.substring(1);
	}
	
	
	public static void main(String[] args) throws Exception {
		
		//Config.getInstance().pls_start_time = new GregorianCalendar(2014,Calendar.OCTOBER,1,0,0,0);
		//Config.getInstance().pls_end_time = new GregorianCalendar(2014,Calendar.OCTOBER,31,23,59,59);
		//runProcess("file_pls_piem_","Torino","_Oct2014","_April2014");
		
			
		//Config.getInstance().pls_start_time = new GregorianCalendar(2014,Calendar.AUGUST,1,0,0,0);
		//Config.getInstance().pls_end_time = new GregorianCalendar(2014,Calendar.AUGUST,31,23,59,59);
		//runProcess("file_pls_pu_","Lecce","_Aug2014","_Sep2014");
		
		
		//Config.getInstance().pls_start_time = new GregorianCalendar(2014,Calendar.SEPTEMBER,1,0,0,0);
		//Config.getInstance().pls_end_time = new GregorianCalendar(2014,Calendar.SEPTEMBER,31,23,59,59);
		//runProcess("file_pls_pu_","Lecce","_Sep2014","_Aug2014");
		
		
		Config.getInstance().pls_start_time = new GregorianCalendar(2013,Calendar.JULY,1,0,0,0);
		Config.getInstance().pls_end_time = new GregorianCalendar(2013,Calendar.JULY,31,23,59,59);
		//runProcess("file_pls_ve_","Venezia","_July2013","_March2014");
		runProcess("file_pls_fi_","Firenze","_July2013","_March2014");
			
				
		Config.getInstance().pls_start_time = new GregorianCalendar(2014,Calendar.MARCH,1,0,0,0);
		Config.getInstance().pls_end_time = new GregorianCalendar(2014,Calendar.MARCH,31,23,59,59);
		//runProcess("file_pls_ve_","Venezia","_March2014","_July2013");
		runProcess("file_pls_fi_","Firenze","_March2014","_July2013");
		
	}
	
	
	
	private static Set<String> getUsers(String pre, String placemarkName,String post) throws Exception {	
		String file = Config.getInstance().base_folder+"/UserEventCounter/"+pre+placemarkName+"_cellXHour"+post+".csv";
		String line;
		File f = new File(file);
		BufferedReader br = new BufferedReader(new FileReader(f));
		
		Set<String> users = new HashSet<String>(); // user profile
		
		// read header
		line = br.readLine();
		while((line = br.readLine())!=null) {	
			String[] p = line.split(",");
			users.add(p[0]);
		}
		br.close();
		return users;
	}
	
	
	
	public static void runProcess(String pre, String placemarkName,String month, String othermonth) throws Exception {
		
		Set<String> otherMonthSet = getUsers(pre,placemarkName,othermonth);
		
		String file = Config.getInstance().base_folder+"/UserEventCounter/"+pre+placemarkName+"_cellXHour"+month+".csv";
		String line;
		
		File f = new File(file);
		if(f == null) {
			Logger.logln("Run UserEventCounterCellacXHour first!");
			System.exit(0);
		}
		
		BufferedReader br = new BufferedReader(new FileReader(f));
		
		//NetworkMap nm = NetworkMapFactory.getNetworkMap(Config.getInstance().pls_start_time);
		String user_id,mnt;
		int num_pls,num_days,days_interval;
		List<CDR> list;
		
		Placemark placemark = Placemark.getPlacemark(placemarkName);
		
		Map<String,Profile> mp = new HashMap<String,Profile>(); // map profiles
		mp.put("Resident", new Resident(placemark,otherMonthSet));
		mp.put("Tourist", new Tourist(placemark,otherMonthSet));
		mp.put("Commuter", new Commuter(placemark,otherMonthSet));
		mp.put("Transit", new Transit(placemark));
		mp.put("Excursionist", new Excursionist(placemark,otherMonthSet));
		
		Map<String,Integer> mcont = new HashMap<String,Integer>(); // map profiles
		for(String p : mp.keySet())
			mcont.put(p, 0);
		
		Map<String,String> mu = new HashMap<String,String>(); // user profile
		
		int[][] gtConfMatrix = new int[mp.size()][mp.size()];
		int users_with_multiple_classes = 0;
		int n_total = 0;
		
		// read header
		line = br.readLine();
		int tot_days = Integer.parseInt(line.substring(line.indexOf("=")+1).trim());
		
		while((line = br.readLine())!=null) {	
			try {
				String[] p = line.split(",");
				
					
				user_id = p[0];
				mnt = p[1];
				num_pls = Integer.parseInt(p[2]);
				num_days = Integer.parseInt(p[3]);
				days_interval = Integer.parseInt(p[4]);
				list = CDR.getDataFormUserEventCounterCellacXHourLine(line);
				/*
				list = new ArrayList<PLSEvent>();
				// 2013-5-23:Sun:13:4018542484
				for(int i=5;i<p.length;i++) {
					String[] x = p[i].split(":|-");
					int y = Integer.parseInt(x[0]);
					int m = Integer.parseInt(x[1]) - 1; // beware! important calendar correction
					int d = Integer.parseInt(x[2]);
					int h = Integer.parseInt(x[4]);
					String time = String.valueOf(new GregorianCalendar(y,m,d,h,0).getTimeInMillis());
					list.add(new PLSEvent(user_id,mnt,x[5],time));
				}
				*/
				List<Integer> uprofiles = new ArrayList<Integer>();
				int how_many_classes = 0;
				int i = 0;
				for(String prof: mp.keySet()) {
					if(mp.get(prof).check(user_id,mnt,num_pls,num_days,days_interval,list,tot_days)) {
						how_many_classes++;
						mu.put(user_id, prof);
						mcont.put(prof,mcont.get(prof)+1);
						uprofiles.add(i);
					}
					i++;
				}
				
				if(how_many_classes > 1) {
					users_with_multiple_classes++;
					for(int ii: uprofiles) {
					for(int jj: uprofiles)
						if(ii!=jj) gtConfMatrix[ii][jj]++;
					}
				}	
				n_total ++;
				
				if(n_total % 10000 == 0) {
					System.out.println("Processed "+n_total+" users..."); 
					Logger.logln("USERS WITH MULTIPLE CLASSES "+users_with_multiple_classes+"/"+n_total);
				}
				
			} catch(Exception e) {
				System.err.println(line);
			}
		}
		br.close();
		
		for(String prof: mcont.keySet())
			Logger.logln(prof+" = "+mcont.get(prof)+"/"+n_total);
		Logger.logln("USERS WITH MULTIPLE CLASSES "+users_with_multiple_classes+"/"+n_total);
		Logger.logln("GT CONFUSION MATRIX");
		for(int i=0; i< gtConfMatrix.length;i++) {
			for(int j=0; j<gtConfMatrix.length;j++)
				System.out.print(gtConfMatrix[i][j]+"\t");
			System.out.println();
		}
		
		File dir = new File(Config.getInstance().base_folder+"/Tourist");
		dir.mkdirs();
		PrintWriter pw = new PrintWriter(new FileWriter(dir+"/"+placemarkName+"_gt_profiles"+month+".csv"));
		for(String user: mu.keySet())
			pw.println(user+","+mu.get(user));
		pw.close();
		
		CopyAndSerializationUtils.save(new File(Config.getInstance().base_folder+"/Tourist/"+placemarkName+"_gt_profiles"+month+".ser"), mu);
		
		Logger.logln("Done!");
	}	
}
