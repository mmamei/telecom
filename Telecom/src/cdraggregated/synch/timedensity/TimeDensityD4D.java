package cdraggregated.synch.timedensity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.gps.utils.LatLonPoint;
import org.gps.utils.LatLonUtils;

import region.Placemark;
import region.RegionI;
import region.RegionMap;
import utils.Colors;
import utils.Config;
import utils.CopyAndSerializationUtils;
import utils.StatsUtils;
import utils.time.TimeConverter;
import visual.html.GoogleChartGraph;
import visual.kml.KML;
import visual.kml.KMLCircle;
import cdraggregated.synch.TableNames;
import cdraggregated.synch.TableNames.Country;
import static cdraggregated.synch.TableNames.Country.*;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.index.strtree.ItemBoundable;
import com.vividsolutions.jts.index.strtree.ItemDistance;
import com.vividsolutions.jts.index.strtree.STRtree;

public class TimeDensityD4D implements TimeDensity {

	static final boolean ENABLE_CACHE = true;
	
	
	private TimeConverter tc = null;
	
	private RegionMap rm_lvl2; // province
	private RegionMap rm_lvl3; // comuni
	
	
	private Map<String,String> antenna2province;
	private Map<String,String> antenna2comuni;
	//
	private Map<String,double[]> antennaLatLonR = new HashMap<>();
	
	
	// provincia --> cella --> valori
	private Map<String, Map<String,double[]>> allMap = new HashMap<>();
	
	// cella --> valori (di una provincia specifica)
	private Map<String,double[]> map;
	private Map<String,double[]> mapz;
	
	
	TimeDensityD4D(String city, Country c) {
		
		if(c.equals(IvoryCoast))
			tc = TimeConverter.getInstance("2011-12-01:0:0:0","2012-04-28:23:59:59");
		if(c.equals(Senegal))
			tc = TimeConverter.getInstance("2013-01-01:0:0:0","2013-12-31:23:59:59");
		if(c.equals(IvoryCoast1Month))
			tc = TimeConverter.getInstance("2012-02-01:0:0:0","2012-03-11:23:59:59");
		if(c.equals(Senegal1Month))
			tc = TimeConverter.getInstance("2013-01-01:0:0:0","2013-01-31:23:59:59");
		
		if(c.equals(IvoryCoast) || c.equals(IvoryCoast1Month)) 
			processAntennaLocations("G:/DATASET/D4D_IVORYCOAST/ORIGINAL/data/ANT_POS.TSV",2,1, false); // 1	-4.143452	5.342044
		if(c.equals(Senegal) || c.equals(Senegal1Month))
			processAntennaLocations("G:/DATASET/D4D_SENEGAL/ContextData/SITE_ARR_LONLAT.CSV",3,2, true); // site_id,arr_id,lon,lat
		
		if(c.equals(IvoryCoast) || c.equals(IvoryCoast1Month)) {
			rm_lvl2 = (RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/ivoryCoastProvince.ser"));
			rm_lvl3 = (RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/ivoryCoastComuni.ser"));
		}
		if(c.equals(Senegal) || c.equals(Senegal1Month)) {
			rm_lvl2 = (RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/senegal-province.ser"));
			rm_lvl3 = (RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/senegal-comuni.ser"));
		}
		
		
		
		
		File f = new File(Config.getInstance().base_folder+"/TIC2015/cache/all/"+c+".ser");	
		if(!ENABLE_CACHE || !f.exists()) {
			System.out.println("Cannot use cache info, computing.....");
			
			
			antenna2province = getAntenna2RegionMapping(antennaLatLonR,rm_lvl2);
			antenna2comuni = getAntenna2RegionMapping(antennaLatLonR,rm_lvl3);
			
			//pre-load the allMap with all the provinces.
			for(RegionI r: rm_lvl2.getRegions())
				allMap.put(r.getName(), new HashMap<String,double[]>());
			
			f.getParentFile().mkdirs();
			if(c.equals(IvoryCoast)) 
				processFileOrDir("G:/DATASET/D4D_IVORYCOAST/ORIGINAL/data/SET1TSV.tgz");
			if(c.equals(Senegal))
				processFileOrDir("G:/DATASET/D4D_SENEGAL/SET1");
			if(c.equals(IvoryCoast1Month)) 
				processFileOrDir("G:/DATASET/D4D_IVORYCOAST/ORIGINAL/data/SET1TSV_SMALL.tgz");
			if(c.equals(Senegal1Month))
				processFileOrDir("G:/DATASET/D4D_SENEGAL/SET1_SMALL");
			
			
			// remove towers with not enough data
			for(Map<String,double[]> m: allMap.values()) {
				List<String> badKeys = new ArrayList<>();
				for(String k: m.keySet()) {
					int count = 0;
					for(double x: m.get(k))
						if(x > 0) count ++;
					if(1.0*count/tc.getTimeSize() < 0.5)
						badKeys.add(k);
				}
				for(String k: badKeys)
					m.remove(k);
			}
			
			printKML(f.getParentFile()+"/"+c+".kml",c.name(),antenna2province);
			
			CopyAndSerializationUtils.save(f, new Object[]{antenna2province,antenna2comuni,allMap});
		}
		else {
			System.out.println("resuming from cache.....");
			Object[] cached = (Object[])CopyAndSerializationUtils.restore(f);
			antenna2province = (Map<String,String>)cached[0];
			antenna2comuni = (Map<String,String>)cached[1];
			allMap = (Map<String,Map<String,double[]>>)cached[2];
		}
		map = allMap.get(city);
		
		mapz = new HashMap<String,double[]>();
		for(String k: map.keySet())
			mapz.put(k, (StatsUtils.getZH(map.get(k),tc)));
		
		
	}
	
	
	
	
	private void processFileOrDir(String file) {
		File f = new File(file);
		if(f.isFile()) processFile(file);
		if(f.isDirectory()) {
			File[] files = f.listFiles();
			for(File f1: files)
				processFile(f1.getAbsolutePath());
		}
	}
	
	private void processFile(String file) {
		try {		
			if(file.endsWith(".gz")) {
				System.out.println("Processing "+new File(file).getName());
				
				BufferedReader br = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(file))));
				process(br,new SimpleDateFormat("yyyy-MM-dd HH"));
				br.close();
			}
			if(file.endsWith(".tgz")) {
				
				TarArchiveInputStream tarInput = new TarArchiveInputStream(new GzipCompressorInputStream(new FileInputStream(file)));
				TarArchiveEntry currentEntry = tarInput.getNextTarEntry();
				while (currentEntry != null) {
					System.out.println("Processing "+currentEntry.getName());
					BufferedReader br = new BufferedReader(new InputStreamReader(tarInput)); // Read directly from tarInput
					process(br,new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
				    currentEntry = tarInput.getNextTarEntry(); 
				}
				tarInput.close();
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private void process(BufferedReader br, SimpleDateFormat sdf) {
		try {
			int skipped_lines = 0;
			String line;
			String first_time = null;
			String last_time = null;
			while((line=br.readLine())!=null) {
				try {
					String[] e = line.split("\t|,");
					
					if(first_time == null) first_time = e[0];
					last_time = e[0];
				
					long time = sdf.parse(e[0]).getTime();
					String id1 = e[1];
					String id2 = e[2];
					int ncdrs = Integer.parseInt(e[3]);
					
					
					if (time < tc.startTime || time > tc.endTime) {
						skipped_lines++;
						continue;
					}
					
					
					String province1 = antenna2province.get(id1);
					if(province1!=null) {
						Map<String,double[]> map1 = allMap.get(province1);
						double[] ts1 = map1.get(id1);
						if(ts1 == null) ts1 = new double[tc.getTimeSize()];
						map1.put(id1, ts1);
						ts1[tc.time2index(time)] += ncdrs;
					}
					String province2 = antenna2province.get(id2);
					if(province2!=null) {
						Map<String,double[]> map2 = allMap.get(province2);
						double[] ts2 = map2.get(id2);
						if(ts2 == null) ts2 = new double[tc.getTimeSize()];
						map2.put(id2, ts2);
						ts2[tc.time2index(time)] += ncdrs;
					}
				} catch(Exception e) {
					System.out.println("BAD LINE: "+line);
				}
			}
			if(skipped_lines > 0) System.out.println("skipped "+skipped_lines+" due to time constratins");
			System.out.println("End of file from "+first_time+" to "+last_time);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
	
	static ItemDistance idist = new ItemDistance(){
		@Override
		public double distance(ItemBoundable arg0, ItemBoundable arg1) {
			if(arg0.getItem().equals(arg1.getItem())) return Double.MAX_VALUE;
			Envelope e1 = (Envelope)arg0.getBounds();
			Envelope e2 = (Envelope)arg1.getBounds();
			return e1.distance(e2);
	}};
	
	private void processAntennaLocations(String file, int lat_i, int lon_i,boolean header) {
		try {
			
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;
			if(header) br.readLine();
			while((line = br.readLine())!=null){
				String[] e = line.split("\t|,");
				String id = e[0];
				double lat = Double.parseDouble(e[lat_i]);
				double lon = Double.parseDouble(e[lon_i]);
				antennaLatLonR.put(id, new double[]{lat,lon,0});
			}
			br.close();
			
			
			STRtree t = new STRtree();
			for(String id: antennaLatLonR.keySet()) {
				double[] latlon = antennaLatLonR.get(id);
				t.insert(new Envelope(new Coordinate(latlon[1],latlon[0])), id);
			}		
			for(String id: antennaLatLonR.keySet()) {
				double[] latlonr = antennaLatLonR.get(id);
				LatLonPoint p1 = new LatLonPoint(latlonr[0],latlonr[1]);
				Object x = t.nearestNeighbour(new Envelope(new Coordinate(latlonr[1],latlonr[0])),id,idist); 
				double[] latlonr2 = antennaLatLonR.get(x);
				LatLonPoint p2 = new LatLonPoint(latlonr2[0],latlonr2[1]);
				double r = LatLonUtils.getHaversineDistance(p1, p2);
				if(r < 100) r = 100;
				latlonr[2] = r;
			}	
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	private Map<String,String> getAntenna2RegionMapping(Map<String,double[]> antennas, RegionMap rm) {
		Map<String,String> result = new HashMap<>();
				
		for(String id: antennas.keySet()){
			double[] latlonr = antennas.get(id);		
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
	
	
	private void printKML(String file, String name, Map<String,String> assignments) {
		try {
			
			
			Map<String,String> desc = new HashMap<>();
			for(Map<String,double[]> m: allMap.values()) 
				for(String r: m.keySet())
					desc.put(r, GoogleChartGraph.getGraph(tc.getTimeLabels(), m.get(r), "", "date", "cdr"));
			
			
			
			PrintWriter out = new PrintWriter(new FileWriter(file));
			KML kml = new KML();
			KMLCircle kml_circle = new KMLCircle();
			kml.printHeaderFolder(out, name);
			for(String id: desc.keySet()) {
				double[] latlonr = antennaLatLonR.get(id);
				String prov = assignments.get(id);
				String color = "99ffffff";
				if(prov!=null)
					color = Colors.RANDOM_COLORS[Math.abs(prov.hashCode())%Colors.RANDOM_COLORS.length];
				out.println(kml_circle.draw(latlonr[1], latlonr[0], latlonr[2], 10, 0, 360, id, color,color, desc.get(id)));
			}
			kml.printFooterFolder(out);
			out.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	
	@Override
	public Map<String, String> getMapping(RegionMap rm) {
		
		Map<String, String> mapping = new HashMap<>();
		for(String k: map.keySet())
			mapping.put(k, antenna2comuni.get(k));
		
		return mapping;
		
	}

	@Override
	public double[] get(String key) {
		return map.get(key);
	}


	@Override
	public double[] getz(String key) {
		return mapz.get(key);
	}


	@Override
	public List<String> getKeys() {
		List<String> keys = new ArrayList<>();
		for(String k: map.keySet())
			keys.add(k);
		return keys;
	}


	@Override
	public TimeConverter getTimeConverter() {
		return tc;
	}
	
	
	public static void main(String[] args) throws Exception {
		TimeDensity td1 = TimeDensityFactory.getInstance(TableNames.getAvailableProvinces(IvoryCoast).get(0), IvoryCoast1Month);	
		TimeDensity td2 = TimeDensityFactory.getInstance(TableNames.getAvailableProvinces(IvoryCoast).get(1), IvoryCoast1Month);	
		System.out.println("Done.");
	}
}
