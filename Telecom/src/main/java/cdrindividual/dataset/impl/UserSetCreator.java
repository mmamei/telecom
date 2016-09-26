package cdrindividual.dataset.impl;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;

import region.Placemark;
import utils.Config;
import utils.Logger;



/*
 * 
 * 
 * DEPRECATED USE USER CDR COUNTER INSTEAD !!!!!!!!!!!!!
 * 
 * 
 */

public class UserSetCreator extends BufferAnalyzerConstrained {
	
	private Set<String> users;
	
	UserSetCreator(Placemark placemark, String user_list_name) {
		super(placemark, user_list_name);
		users = new HashSet<String>();
	}

	
	protected void analyze(String username, String imsi, String celllac,long timestamp, Calendar cal,String header) {
		if(username.equals("33a8b88a4ed464bcea3c9514d683f8456b573811a31de923d62e5def88889")) {		
			System.out.println(celllac+" --> "+cal.getTime());
		}
		users.add(username);
	}

	protected void finish() {
		
		try {
			File dir = new File(Config.getInstance().base_folder+"/UserSetCreator");
			dir.mkdirs();
			PrintWriter out = new PrintWriter(new FileWriter(dir+"/"+this.getString()+".csv"));
			for(String u: users)
				out.println(u);
			out.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
	/*
	 * 
	 * 
	 * DEPRECATED USE USER CDR COUNTER INSTEAD !!!!!!!!!!!!!
	 * 
	 * 
	 */
	
	
	public static void main(String[] args) {
		/*
		Config.getInstance().pls_folder = Config.getInstance().pls_root_folder+"/file_pls_piem"; 
		Config.getInstance().pls_start_time = new GregorianCalendar(2014,Calendar.OCTOBER,20,0,0,0);
		Config.getInstance().pls_end_time = new GregorianCalendar(2014,Calendar.NOVEMBER,14,23,59,59);
		Placemark placemark= Placemark.getPlacemark("Torino");
		*/
		Config.getInstance().pls_folder = Config.getInstance().pls_root_folder+"/file_pls_pu"; 
		Config.getInstance().pls_start_time = new GregorianCalendar(2014,Calendar.SEPTEMBER,1,0,0,0);
		Config.getInstance().pls_end_time = new GregorianCalendar(2014,Calendar.SEPTEMBER,31,23,59,59);
		Placemark placemark= Placemark.getPlacemark("Lecce");
		
		
		
		BufferAnalyzerConstrained ba = new UserSetCreator(placemark,null);
		ba.run();
		Logger.logln("Done!");
	}
	
	
	/*
	 * This main serve to convert from UsersCSVCreator files (one file per user) in a UserSetCreator file (one file with all the users)
	 */
	
	public static void main2(String[] args) throws Exception {
		String area = "lomb";
		File dir = new File(Config.getInstance().base_folder+"/UsersCSVCreator/file_pls_"+area+"_users_200_10000");
		PrintWriter out = new PrintWriter(Config.getInstance().base_folder+"/UserSetCreator/LDAPOP_"+area+".csv");
		for(String f: dir.list()) {
			out.println(f.substring(0,f.indexOf(".csv")));
		}
		out.close();
		System.out.println("Done");
	}
	
	
}
