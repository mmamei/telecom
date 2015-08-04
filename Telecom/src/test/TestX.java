package test;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import utils.Config;
import utils.CopyAndSerializationUtils;

public class TestX {
	public static void main(String[] args) throws Exception {
		
	
		CDRX cdrx = new CDRX();
		ZipFile zf = new ZipFile(Config.getInstance().dataset_folder+"/PLS/file_pls/file_pls_fi/2013/PLS1541504_1371830431633.zip");
		ZipEntry ze = (ZipEntry) zf.entries().nextElement();
		BufferedReader br = new BufferedReader(new InputStreamReader(zf.getInputStream(ze)));
		String line;
		while((line=br.readLine())!=null) { 
			String[] e = line.split("\t");
			CDR cdr = new CDR(e[0],e[1],Long.parseLong(e[2]),Long.parseLong(e[3]));
			cdrx.add(cdr);
		}
		zf.close();
		br.close();
		
		
		CopyAndSerializationUtils.save(new File(Config.getInstance().base_folder+"/PLS1541504_1371830431633.ser"), cdrx);
	}
	
}


class CDRX implements Serializable {
	private Map<String,Set<CDR>> user2cdr;
	private Map<Long,Set<CDR>> celllac2cdr;
	
	CDRX() {
		user2cdr = new HashMap<String,Set<CDR>>();
		celllac2cdr = new HashMap<Long,Set<CDR>>();
	}
	
	void add(CDR cdr) {
		Set<CDR> x = user2cdr.get(cdr.user);
		if(x == null) {
			x = new HashSet<CDR>();
			user2cdr.put(cdr.user, x);
		}
		x.add(cdr);
		
		
		x = celllac2cdr.get(cdr.celllac);
		if(x == null) {
			x = new HashSet<CDR>();
			celllac2cdr.put(cdr.celllac, x);
		}
		x.add(cdr);
	}
	
}

class CDR implements Serializable {
	String user;
	String mnt;
	long celllac;
	long time;
	
	CDR(String user, String mnt, long celllac, long time) {
		this.user = user;
		this.mnt = mnt;
		this.celllac = celllac;
		this.time = time;
	}
	
	@Override
	public boolean equals(Object other) {
		if(other instanceof CDR) {
			CDR o = (CDR)other;
			return user.equals(o.user) && time == o.time;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return (user+time).hashCode();
	}
	
}
