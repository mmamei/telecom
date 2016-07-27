package utils.multithread2;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import utils.FileUtils;
import utils.mod.Route;
import utils.mygraphhopper.WEdge;

public class MultiWorker {
	
	public static int N_THREADS = 8;
	
	public static boolean PRINT = true;
	
	public static void run(String in_file, WorkerCallbackI master) {
		
		try {
			
			File mainf = new File(in_file);
			int total_size = 0;
			if(mainf.isFile()) 
				total_size = FileUtils.lines(in_file);
			if(mainf.isDirectory())
				total_size = mainf.listFiles().length;
				
			
			if(PRINT) System.out.println(in_file+" ===> "+total_size);
			
			int USE_THREAD = Math.min(N_THREADS, total_size);
//			USE_THREAD=1;//
			
			int size = total_size / USE_THREAD;
			Worker[] w = new Worker[USE_THREAD];
			for(int t = 0; t < USE_THREAD;t++) {
				int start = t*size;
				int end = t == (USE_THREAD-1) ? total_size : (t+1)*size;
				w[t] = new Worker(master,in_file,start,end);		
			}
			
			for(int t = 0; t < USE_THREAD;t++) 
				w[t].start();
	
			for(int t = 0; t < USE_THREAD;t++) 
				w[t].join();
			
			if(PRINT) System.out.println("All thread completed!");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	public static void run(ArrayList<Route> routes, WorkerCallbackI master) {
		try {
			
			int total_size = routes.size();				
			
			if(PRINT) System.out.println("size Tot ===> "+total_size);
			
			int USE_THREAD = 0;
			
			if(total_size!=0)	USE_THREAD = Math.min(N_THREADS, total_size);
			else	System.out.println("total_size = 0");
//			USE_THREAD=1;//
			int size = total_size / USE_THREAD;
			Worker[] w = new Worker[USE_THREAD];
			for(int t = 0; t < USE_THREAD;t++) {
				int start = t*size;
				int end = t == (USE_THREAD-1) ? total_size : (t+1)*size;
				w[t] = new Worker(master,routes,start,end);		
			}
			
			for(int t = 0; t < USE_THREAD;t++) 
				w[t].start();
	
			for(int t = 0; t < USE_THREAD;t++) 
				w[t].join();
			
			if(PRINT) System.out.println("All thread completed!");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
