package cdraggregated.synch.timedensity;

import static cdraggregated.synch.TableNames.Country.IvoryCoast;
import static cdraggregated.synch.TableNames.Country.Senegal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
import region.Region2Region;
import region.RegionI;
import region.RegionMap;
import utils.Config;
import utils.CopyAndSerializationUtils;
import utils.Mail;
import utils.StatsUtils;
import utils.time.TimeConverter;
import visual.html.GoogleChartGraph;
import visual.kml.KMLColorMap;
import cdraggregated.synch.TableNames;
import cdraggregated.synch.TableNames.Country;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.index.strtree.ItemBoundable;
import com.vividsolutions.jts.index.strtree.ItemDistance;
import com.vividsolutions.jts.index.strtree.STRtree;

public class TimeDensityD4D implements TimeDensity {

	static final boolean ENABLE_CACHE = true;
	
	
	private TimeConverter tc = null;

	private RegionMap grid;
	
	private Map<String,String> antenna2grid;
	private Map<String,String> grid2comuni;
	private Map<String,String> grid2province;
	
	
	//
	private Map<String,double[]> antennaLatLonR = new HashMap<>();
	
	
	// provincia --> cella --> valori
	private Map<String, Map<String,double[]>> allMap = new HashMap<>();
	
	// cella --> valori (di una provincia specifica)
	private Map<String,double[]> map;
	private Map<String,double[]> mapz;
	
	
	TimeDensityD4D(String city, Country c, String startTime, String endTime) {
		tc = TimeConverter.getInstance(startTime,endTime);
		if(c.equals(IvoryCoast)) {
			processAntennaLocations("G:/DATASET/D4D_IVORYCOAST/ORIGINAL/data/ANT_POS.TSV",2,1, false); // 1	-4.143452	5.342044
			grid = (RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/ivorycoast_grid.ser"));
			grid2comuni = Region2Region.region2regionOneOnly("G:/DATASET/GEO/ivorycoast/ivorycoast_grid.csv","NAME_5","NAME_4");
			grid2province = Region2Region.region2regionOneOnly("G:/DATASET/GEO/ivorycoast/ivorycoast_grid.csv","NAME_5","NAME_1");
		}
		if(c.equals(Senegal)) {
			processAntennaLocations("G:/DATASET/D4D_SENEGAL/ContextData/SITE_ARR_LONLAT.CSV",3,2, true); // site_id,arr_id,lon,lat
			grid = (RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/senegal_grid.ser"));
			grid2comuni = Region2Region.region2regionOneOnly("G:/DATASET/GEO/senegal/senegal_grid.csv","NAME_5","NAME_4");
			grid2province = Region2Region.region2regionOneOnly("G:/DATASET/GEO/senegal/senegal_grid.csv","NAME_5","NAME_1");
		}
		
		SimpleDateFormat datef = new SimpleDateFormat("yyyy-MM-dd");
		File f = new File(Config.getInstance().base_folder+"/TIC2015/cache/all/"+c+"_"+datef.format(new Date(tc.startTime))+"_"+datef.format(new Date(tc.endTime))+".ser");	
		if(!ENABLE_CACHE || !f.exists()) {
			System.out.println("Cannot use cache info, computing.....");
			
			/*
			for(RegionI r: grid.getRegions())
				if(grid2province.get(r.getName()) == null) System.out.println(r.getName()); 
			
			System.exit(0);
			*/
			
			antenna2grid = Region2Region.getMapping(antennaLatLonR,grid);
			
			for(RegionI r: grid.getRegions())
				if(!antenna2grid.values().contains(r.getName())) System.err.println(r.getName()+" have no antennas");
			
			
			//pre-load the allMap with all the provinces.
			for(String r: grid2province.values())
				allMap.put(r, new HashMap<String,double[]>());
			
			f.getParentFile().mkdirs();
			if(c.equals(IvoryCoast)) 
				processFileOrDir("G:/DATASET/D4D_IVORYCOAST/ORIGINAL/data/SET1TSV.tgz");
			if(c.equals(Senegal))
				processFileOrDir("G:/DATASET/D4D_SENEGAL/SET1");
			
			
			// remove towers with not enough data
			/*
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
			*/
	
			printKML(f.getParentFile()+"/"+c+"_"+datef.format(new Date(tc.startTime))+"_"+datef.format(new Date(tc.endTime))+".kml",grid2province);
			
			CopyAndSerializationUtils.save(f, new Object[]{antenna2grid,allMap});
		}
		else {
			System.out.println("resuming from cache.....");
			Object[] cached = (Object[])CopyAndSerializationUtils.restore(f);
			antenna2grid = (Map<String,String>)cached[0];
			allMap = (Map<String,Map<String,double[]>>)cached[1];
		}
		
		map = allMap.get(city);
		
		if(map == null) {
			System.out.println(city+" is null!");
			System.exit(0);
		}
		
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
				
				BufferedReader br = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(file)),"UTF-8"));
				process(br,new SimpleDateFormat("yyyy-MM-dd HH"));
				br.close();
			}
			if(file.endsWith(".tgz")) {
				
				TarArchiveInputStream tarInput = new TarArchiveInputStream(new GzipCompressorInputStream(new FileInputStream(file)));
				TarArchiveEntry currentEntry = tarInput.getNextTarEntry();
				while (currentEntry != null) {
					System.out.println("Processing "+currentEntry.getName());
					BufferedReader br = new BufferedReader(new InputStreamReader(tarInput,"UTF-8")); // Read directly from tarInput
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
			String grid_cell1=null,province1=null,grid_cell2=null,province2=null;
			Map<String,double[]> map1=null, map2=null;
			while((line=br.readLine())!=null) {
				try {
					String[] e = line.split("\t|,");
					
					if(first_time == null) first_time = e[0];
					last_time = e[0];
				
					long time = sdf.parse(e[0]).getTime();
					
					
					
					String id1 = e[1];
					String id2 = e[2];
					int ncdrs = Integer.parseInt(e[3]);
					
					
					if(1.0*(time - tc.startTime)/(1000l*60*60*24) < -31 || time > tc.endTime) {
						System.out.println("Skipping file for time restrictions:");
						System.out.println("Relevant interval: "+new Date(tc.startTime)+" - "+new Date(tc.endTime));
						System.out.println("File Time: "+new Date(time));
						return;
					}
					
						
					if (time < tc.startTime || time > tc.endTime) {
						skipped_lines++;
						continue;
					}
					
					grid_cell1 = antenna2grid.get(id1);
					province1 = grid2province.get(grid_cell1);
					if(grid_cell1!=null && province1 != null) {
						map1 = allMap.get(province1);
						double[] ts1 = map1.get(grid_cell1);
						if(ts1 == null) {
							ts1 = new double[tc.getTimeSize()];
							map1.put(grid_cell1, ts1);
						}
						ts1[tc.time2index(time)] += ncdrs;
					}
					
					grid_cell2 = antenna2grid.get(id2);
					province2 = grid2province.get(grid_cell2);
					if(grid_cell2!=null && province2 != null) {
						map2 = allMap.get(province2);
						double[] ts2 = map2.get(grid_cell2);
						if(ts2 == null) {
							ts2 = new double[tc.getTimeSize()];
							map2.put(grid_cell2, ts2);
						}
						ts2[tc.time2index(time)] += ncdrs;
					}
				} catch(Exception e) {
					System.out.println("BAD LINE: "+line+" "+grid_cell1+" "+province1+" "+map1+" "+grid_cell2+" "+province2+" "+map2);
					e.printStackTrace();
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
	
	
	
	
	
	private void printKML(String file, Map<String,String> assignments) {
		
		Map<String,String> desc = new HashMap<>();
		for(Map<String,double[]> m: allMap.values()) 
			for(String r: m.keySet())
				desc.put(r, GoogleChartGraph.getGraph(tc.getTimeLabels(), m.get(r), "", "date", "cdr"));
		
		try {
			KMLColorMap.drawColorMap(file, KMLColorMap.toIntAssignments(assignments), grid, desc);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
	@Override
	public Map<String, String> getMapping(RegionMap rm) {
		
		Map<String, String> mapping = new HashMap<>();
		for(String k: map.keySet())
			mapping.put(k, grid2comuni.get(k));
		
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
		
		// ivory all time "2011-12-01:0:0:0","2012-04-28:23:59:59");		
		// senegal all time "2013-01-01:0:0:0","2013-12-31:23:59:59" 
		
		TimeDensityFactory.getInstance(TableNames.getAvailableProvinces(Senegal).get(0), Senegal, "2013-04-01:0:0:0","2013-04-31:23:59:59");	
		TimeDensityFactory.getInstance(TableNames.getAvailableProvinces(Senegal).get(0), Senegal, "2013-06-01:0:0:0","2013-06-31:23:59:59");	
		TimeDensityFactory.getInstance(TableNames.getAvailableProvinces(Senegal).get(0), Senegal, "2013-10-01:0:0:0","2013-10-31:23:59:59");
		
		TimeDensityFactory.getInstance(TableNames.getAvailableProvinces(IvoryCoast).get(1), IvoryCoast, "2012-02-01:0:0:0","2012-02-28:23:59:59");
		TimeDensityFactory.getInstance(TableNames.getAvailableProvinces(IvoryCoast).get(1), IvoryCoast, "2012-03-01:0:0:0","2012-03-31:23:59:59");
		TimeDensityFactory.getInstance(TableNames.getAvailableProvinces(IvoryCoast).get(1), IvoryCoast, "2012-04-01:0:0:0","2012-04-28:23:59:59");	
		
		Mail.send("End TimeDensityD4D");
		System.out.println("Done.");
	}
}
