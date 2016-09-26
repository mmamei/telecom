package region;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import utils.ListMapArrayBasicUtils;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.WKTWriter;

public class Region2Region {
	public static Map<String,Set<String>> region2region(String file,String key, String value) {
		Map<String,Set<String>> result = new HashMap<>();
		try{
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8"));
			String line;
			
			String[] h = br.readLine().split("\t");
			Map<String,Integer> h2i = new HashMap<String,Integer>();
			for(int i=0; i<h.length;i++)
				h2i.put(h[i], i);
			
			
			while((line=br.readLine())!=null) {
				String[] e = line.split("\t");
				//System.out.println(e[h2i.get(key)]+" ==> "+e[h2i.get(value)]);
				Set<String> x  = result.get(e[h2i.get(key)]);
				if(x == null) {
					x = new HashSet<>();
					result.put(e[h2i.get(key)], x);
				}
				x.add(e[h2i.get(value)]);
			}
			br.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	
	public static Map<String,String> region2regionOneOnly(String file,String key, String value) {
		Map<String,String> result = new HashMap<String,String>();
		try{
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8"));
			String line;
			
			String[] h = br.readLine().split("\t");
			Map<String,Integer> h2i = new HashMap<String,Integer>();
			for(int i=0; i<h.length;i++)
				h2i.put(h[i], i);
			
			
			while((line=br.readLine())!=null) {
				String[] e = line.split("\t");
				result.put(e[h2i.get(key)],e[h2i.get(value)]);
			}
			br.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	
	public static void addNameFeatures2ShpCsvFile(String infile, String outfile, RegionMap inrm, RegionMap addrm, String inkey, String outkey) {
		Map<String,String> r = getMapping(addrm,inrm);
		Map<String,Set<String>> ir = ListMapArrayBasicUtils.invert(r);
 		try{
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(infile), "UTF8"));
			String line;
			line = br.readLine();
			String[] h = line.split("\t");
			Map<String,Integer> h2i = new HashMap<String,Integer>();
			for(int i=0; i<h.length;i++)
				h2i.put(h[i], i);
			
			PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(outfile),"UTF8"));
			out.println(line+"\t"+outkey);
			WKTWriter wtk = new WKTWriter();
			GeometryFactory geometryFactory = new GeometryFactory();
			while((line=br.readLine())!=null) {
				String[] e = line.split("\t");
				Set<String> s = ir.get(e[h2i.get(inkey)]);
				if(s!=null)
					for(String x: s)	{
						
						out.println("\""+wtk.write(addrm.getRegion(x).getGeom())+"\""+line.substring(line.indexOf("\t"))+"\t"+x);
					}
			}
			br.close();
			out.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public static Map<String,String> getMapping(RegionMap rm1, RegionMap rm2) {
		Map<String,String> result = new HashMap<>();
		
		for(RegionI r: rm1.getRegions()) {
			float[] f = rm2.computeAreaIntersection(r);
			
			int max = -1;
			for(int i=0;i<f.length;i++)
				if(f[i] > 0 && (max == -1 || f[i] > f[max]))
					max = i;
		
			RegionI o = rm2.getRegion(max);
			if(o != null) 
				result.put(r.getName(), o.getName());
			//else System.err.println("mapping error "+r.getName());
		}
		return result;
	}
	
	
	
	public static Map<String,String> getMapping(Map<String,double[]> latlonR, RegionMap rm) {
		Map<String,String> result = new HashMap<>();
				
		for(String id: latlonR.keySet()){
			double[] latlonr = latlonR.get(id);		
			Placemark r = new Placemark(id,new double[]{latlonr[0],latlonr[1]},latlonr[2]);
			
		
			float[] f = rm.computeAreaIntersection(r);
			
			int max = -1;
			for(int i=0;i<f.length;i++)
				if(f[i] > 0 && (max == -1 || f[i] > f[max]))
					max = i;
		
			RegionI o = rm.getRegion(max);
			if(o != null) 
				result.put(r.getName(), o.getName());
			else System.err.println("mapping error "+r.getName());	
		}
		
		return result;
	}
}
