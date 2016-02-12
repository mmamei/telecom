package utils.multithread;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Set;

public class Worker extends Thread {
	
	private static Set<String> tnames = new HashSet<String>();
	
	
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
		
		if(tnames.contains(name))
			System.err.println(name+" Duplicate!!!!!!");
		tnames.add(name);
		
	}
	public void run() {
		if(MultiWorker.PRINT) System.out.println(name+" starting!");
		
		
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
				//System.out.println(name+" process "+files[i]);
				master.runMultiThread(files[i]);
			}
		}
		
		if(MultiWorker.PRINT) System.out.println(name+" completed!");
	}
}
