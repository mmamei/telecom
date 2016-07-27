package utils.multithread2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import utils.mod.Route;
import utils.mygraphhopper.WEdge;

public class Worker extends Thread {
	
	private static Set<String> tnames = new HashSet<String>();
	
	boolean analize=false;
	ArrayList<Route> routes;
	WorkerCallbackI master;
	String infile;
	int start;
	int end;
	String name;
	Worker(WorkerCallbackI master,String infile,int start,int end) {
		this.master = master;
		this.infile = infile;
		this.start = start;
		this.end = end;
		this.name = "Thread "+infile+" FROM: "+start+" TO: "+end;
		
//		if(tnames.contains(name))
//			System.err.println(name+" Duplicate!!!!!!");
//		tnames.add(name);
		if(!tnames.contains(name))
			tnames.add(name);
		
	}
	Worker(WorkerCallbackI master,ArrayList<Route> routes, int start, int end) {
		this.master = master;
		this.routes = routes;
		this.start = start;
		this.end = end;
		this.name = "Thread routes FROM: "+start+" TO: "+end;
		this.analize=true;
		if(tnames.contains(name))
			System.err.println(name+" Duplicate!!!!!!");
		tnames.add(name);
	}
	public void run() {
		if(MultiWorker.PRINT) System.out.println(name+" starting!");		
		if(analize){
			for(int i=start;i<end;i++) 		
				master.runMultiThread(routes.get(i));
		}
		else{
			File mainf = new File(infile);
			
			if(mainf.isFile()) {
				try {
					BufferedReader br = new BufferedReader(new FileReader(infile));
					for(int i=0;i<15;i++)	br.readLine();	//skip header
					for(int i=0; i<start;i++)
						br.readLine();		
					for(int i=start;i<end;i++) 		
						master.runMultiThread(br.readLine());
					br.close();
					
				} catch (Exception e) {
					e.printStackTrace();
				}
		
			}
			if(mainf.isDirectory()) {
				File[] files = mainf.listFiles();
				for(int i=start;i<end;i++) {
					//System.out.println(name+" process "+files[i]);
					master.runMultiThread(files[i]);
				}
			}
			
			if(MultiWorker.PRINT) System.out.println(name+" completed!");
		}
	}
}
