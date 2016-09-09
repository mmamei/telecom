package utils.multithread;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import utils.FileUtils;
import utils.FileUtils.HowToDealWithFileHeader;


public class Worker extends Thread {
	
	
	
	private static Set<String> tnames = new HashSet<String>();
	
	private WorkerCallbackI master;
	
	
	// it's kind of ugly code, but ouy van have either a worker based on a String (infile), or on a List<T> in_vecotr.
	// The worker works on one and the other is null.
	private String infile;
	private List invector;
	
	private HowToDealWithFileHeader header;
	private int start;
	private int end;
	private String name;
	
	Worker(WorkerCallbackI master,String infile,int start,int end, HowToDealWithFileHeader header) {
		this.master = master;
		this.infile = infile;
		this.invector = null;
		this.start = start;
		this.end = end;
		this.header = header;
		this.name = "Thread "+infile+" FROM: "+start+" TO: "+end;
		System.out.println("here");
		if(tnames.contains(name))
			System.err.println(name+" Duplicate!!!!!!");
		tnames.add(name);	
	}
	
	
	public Worker(WorkerCallbackI master,List invector, int start, int end) {
		this.master = master;
		this.infile = null;
		this.invector = invector;
		this.start = start;
		this.end = end;
		this.name = "Thread routes FROM: "+start+" TO: "+end;
		
		if(tnames.contains(name))
			System.err.println(name+" Duplicate!!!!!!");
		tnames.add(name);
	}
	
	public void run() {
		if(infile!=null) runFile();
		if(invector!=null) runVector();
	}
	
	public void runFile() {
		if(MultiWorker.PRINT) System.out.println(name+" starting!");
		
		
		File mainf = new File(infile);
		if(mainf.isFile()) {
			try {
				
				BufferedReader br = new BufferedReader(new FileReader(infile));
				FileUtils.skipHeader(br, header);
		
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
	
	public void runVector() {
		if(MultiWorker.PRINT) System.out.println(name+" starting!");
		
		
		for(int i=start;i<end;i++) 		
			master.runMultiThread(invector.get(i));
		
		if(MultiWorker.PRINT) System.out.println(name+" completed!");
	}
}
