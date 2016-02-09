package utils.multithread;

import utils.FileUtils;

public class MultiWorker {
	
	public static boolean PRINT = true;
	
	public static void run(String in_file, WorkerCallbackI master) {
		
		try {
			int total_size = FileUtils.lines(in_file) - 1; // the first line is an header (comment)
			int n_thread = 8;
			int size = total_size / n_thread;
			Worker[] w = new Worker[n_thread];
			for(int t = 0; t < n_thread;t++) {
				int start = t*size;
				int end = t == (n_thread-1) ? total_size : (t+1)*size;
				w[t] = new Worker(master,in_file,start,end);		
			}
			
			for(int t = 0; t < n_thread;t++) 
				w[t].start();
	
			for(int t = 0; t < n_thread;t++) 
				w[t].join();
			
			if(PRINT) System.out.println("All thread completed!");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
}
