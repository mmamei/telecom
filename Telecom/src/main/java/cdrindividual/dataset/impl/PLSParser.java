package cdrindividual.dataset.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import utils.Config;
import utils.FileUtils.HowToDealWithFileHeader;
import utils.multithread.MultiWorker;
import utils.multithread.WorkerCallbackI;

public class PLSParser implements WorkerCallbackI<File> {
	
	
	public static final boolean MULTITHREAD = false;
	
	
	public static int MIN_HOUR = 0;
	public static int MAX_HOUR = 25;
	
	
	public static boolean REMOVE_BOGUS = true;
	
	private static boolean QUIET = false;
	
	//private static Config conf = null;
	//private static final int BUFFER_SIZE = 1048576;
	
	
	private static Calendar startTime = null;
	private static Calendar endTime = null;
	
	private static String dir;
	private static long sTime,eTime;
	private static int mins;
	
	private Set<String> bogus;
	private BufferAnalyzer analyzer = null;
	private static PLSParser instance = null;
	private PLSParser() {
		
	}
	public static PLSParser getInstance() {
		if(instance == null) 
			instance = new PLSParser();
		return instance;
	}
	
	void parse(BufferAnalyzer ba) throws Exception {
		
		startTime = ba.getStartTime();
		endTime = ba.getEndTime();	
		
		dir = Config.getInstance().pls_folder;
		sTime = System.currentTimeMillis();
		
		File directory = new File(dir);
		
		if(REMOVE_BOGUS && bogus == null) {
			bogus = new HashSet<String>();
			System.out.println("Loading bogus users ... "+Config.getInstance().pls_folder);
			System.out.println(directory.getAbsolutePath());
			String d = directory.getAbsolutePath().substring(Config.getInstance().pls_root_folder.length()+1);
			if(d.indexOf("\\") > 0) d = d.substring(0,d.indexOf("\\"));
			File f = new File(Config.getInstance().base_folder+"/UserCDRCounter/"+d+"_bogus.csv");
			if(f.exists()) {
				BufferedReader br = new BufferedReader(new FileReader(f));
				String line;
				while((line=br.readLine())!=null) 
					bogus.add(line.split(",")[0]);
				br.close();
				System.out.println(bogus.size()+" BOGUS USERS FOUND");
			}
			else {
				System.err.println(Config.getInstance().base_folder+"/UserEventCounter/"+d+"_bogus.csv NOT FOUND");
			}
		}
		this.analyzer = ba;
		
		if(MULTITHREAD)
			MultiWorker.run(directory.getAbsolutePath(), HowToDealWithFileHeader.NO_HEADER,this);
		else
			analyzeDirectory(directory);
		
		eTime = System.currentTimeMillis();
		mins = (int)((eTime - sTime) / 60000);
		//Logger.logln("Completed after "+mins+" mins");
	}
	
	
	//private  static final String[] MONTHS = new String[]{"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
	
	
	
	//private static Map<String,String> allDays = new TreeMap<String,String>();
	
	private void analyzeDirectory(File directory) throws Exception {	
		File[] items = directory.listFiles();
		for(int i=0; i<items.length;i++)
			runMultiThread(items[i]);	
	} 
	
	
	@Override
	public void runMultiThread(File item) {
		
		try {
			if(item.isFile()) {
				Calendar end_cal = new GregorianCalendar();
				String n = item.getName();
				
				end_cal.setTimeInMillis(Long.parseLong(n.substring(n.lastIndexOf("_")+1, n.indexOf(".zip"))));
				
				Calendar begin_cal = (Calendar)end_cal.clone();
				begin_cal.add(Calendar.MINUTE, -30); // a pls file with time T contains events from T-30 min, to T
				
				if(end_cal.before(startTime) || begin_cal.after(endTime)) return;
				

				
				if(end_cal.get(Calendar.HOUR_OF_DAY) < MIN_HOUR || begin_cal.get(Calendar.HOUR_OF_DAY) > MAX_HOUR) return;
				
				if(!QUIET) System.out.println("BEGIN "+n+" ==> "+begin_cal.getTime()+", "+end_cal.getTime());
				
				//String key = end_cal.get(Calendar.DAY_OF_MONTH)+"/"+MONTHS[end_cal.get(Calendar.MONTH)]+"/"+end_cal.get(Calendar.YEAR);
				//String h = allDays.get(key);
				//allDays.put(key, h==null? end_cal.get(Calendar.HOUR_OF_DAY)+"-" : h+end_cal.get(Calendar.HOUR_OF_DAY)+"-");
				
				analyzeFile(item, bogus);
				
				//if(!QUIET) System.out.println("FINISH "+n+" ==> "+begin_cal.getTime()+", "+end_cal.getTime());

			}
			else if(item.isDirectory())
				if(MULTITHREAD)
					MultiWorker.run(item.getAbsolutePath(), HowToDealWithFileHeader.NO_HEADER, this);
				else
					analyzeDirectory(item);
		} catch(Exception e) {
			System.out.println("Problems with file "+item.getAbsolutePath());
			e.printStackTrace();
		}	
	}
	
	
	
	
	private void analyzeFile(File plsFile, Set<String> bogus) {	
		
		ZipFile zf = null;
		BufferedReader br = null;
		try {
			zf = new ZipFile(plsFile);
			ZipEntry ze = (ZipEntry) zf.entries().nextElement();
			br = new BufferedReader(new InputStreamReader(zf.getInputStream(ze)));
			String line;
			String u;
			while((line=br.readLine())!=null) { 
				u = line.substring(0,line.indexOf("\t"));
				if(bogus==null || !bogus.contains(u)) 
					analyzer.analyze(line);
			}
		}catch(Exception e) {
			System.err.println("Problems with file: "+plsFile.getAbsolutePath());
			e.printStackTrace();
		}
		try {
			br.close();
			zf.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
}
