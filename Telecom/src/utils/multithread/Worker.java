package utils.multithread;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class Worker extends Thread {
	WorkerCallbackI master;
	String infile;
	int start;
	int end;
	
	Worker(WorkerCallbackI master,String infile,int start,int end) {
		this.master = master;
		this.infile = infile;
		this.start = start;
		this.end = end;
	}
	public void run() {
		if(MultiWorker.PRINT) System.out.println("Thread "+start+"-"+end+" starting!");
		
		
		File mainf = new File(infile);
		if(mainf.isFile()) {
			try {
				
				BufferedReader br = new BufferedReader(new FileReader(infile));
				br.readLine(); // skip first comment line
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
				try {
					File f = files[i];
					if(!f.isFile()) continue;
					master.runMultiThread(f);
					
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		if(MultiWorker.PRINT) System.out.println("Thread "+start+"-"+end+" completed!");
	}
}
