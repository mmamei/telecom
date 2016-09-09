package cdraggregated.densityANDflows.flows.scale;

import java.io.File;
import java.io.FilenameFilter;
import java.util.HashMap;
import java.util.Map;

import otherdata.TIbigdatachallenge2015.IstatCensus2011;
import utils.AddMap;
import utils.Config;
import utils.CopyAndSerializationUtils;
import cdraggregated.densityANDflows.ObjectID2IstatCode;
import cdraggregated.densityANDflows.ZoneConverter;
import cdraggregated.densityANDflows.density.DensityComparator;
import cdraggregated.densityANDflows.density.DensityMultiplier;
import cdraggregated.densityANDflows.flows.ODParser;

public class ScaleOnResidents {

	public static boolean PLOT_DEBUG = false;

	public static final int BY_FROM = 0;
	public static final int BY_TO = 1;
	
	public static int ISTAT_REF_INDEX = 59;
	
	
	private String od_dir;
	private FilenameFilter ff;
	private ZoneConverter zc;
	private double threshold;
	
	
	private Map<String,Double> resident_density = null;
	private Map<Integer,AddMap> hour_od = null;
	private AddMap istat = null;
	
	
	private static final String SCALE_TYPE = "scaled-on-residents";
	
	
	public static void main(String[] args) {
		
		
		ScaleOnResidents odm = null;
		
		odm = new ScaleOnResidents("ODMatrixTime_file_pls_piem_file_pls_piem_01-06-2015-01-07-2015_minH_0_maxH_25_ABOVE_8limit_20000_cellXHour_odpiemonte_15-06-2015",null,new ObjectID2IstatCode("G:/DATASET/GEO/telecom-2015-od/odpiemonte.csv"),5);
		odm.scaleAll();
		
		
		/*
		odm = new ScaleOnResidents("ODMatrixHW_file_pls_lomb_file_pls_lomb_01-03-2014-30-03-2014_minH_0_maxH_25_ABOVE_8limit_20000_cellXHour_odlombardia",null,new ObjectID2IstatCode("G:/DATASET/GEO/telecom-2015-od/odlombardia.csv"),10);
		odm.scaleAll();
		
		odm = new ScaleOnResidents("ODMatrixHW_file_pls_piem_file_pls_piem_01-06-2015-01-07-2015_minH_0_maxH_25_ABOVE_8limit_20000_cellXHour_odpiemonte",null,new ObjectID2IstatCode("G:/DATASET/GEO/telecom-2015-od/odpiemonte.csv"),10);
		odm.scaleAll();
		
		odm = new ScaleOnResidents("ODMatrixHW_file_pls_er_file_pls_er_01-04-2015-30-04-2015_minH_0_maxH_25_ABOVE_8limit_20000_cellXHour_odemiliaromagna",null,new ObjectID2IstatCode("G:/DATASET/GEO/telecom-2015-od/odemilia-romagna.csv"),10);
		odm.scaleAll();
		*/
		System.out.println("Done");
		
	}
	
	
	public void scaleAll() {
		String out_dir = Config.getInstance().base_folder+"/ODMatrix/"+od_dir+"-"+SCALE_TYPE;
		new File(out_dir).mkdirs();
		
		File based = new File(Config.getInstance().base_folder+"/ODMatrix/"+od_dir);
		for(File f: based.listFiles(ff)) {
			if(f.getName().equals("latlon.csv")) continue;
			// if starts with od it follows the HW format. Otherwise the SMOD format
			int h = f.getName().startsWith("od") ? Integer.parseInt(f.getName().split("-")[2]) : Integer.parseInt(f.getName().split("_")[3].substring(8, 10));
			Map<String,Object > header = ODParser.parseHeader(f.getAbsolutePath());
			header.put("scale", SCALE_TYPE);
			AddMap odx = ODParser.parse(f.getAbsolutePath(),zc);
			odx = scale(odx,h);
			ODParser.save(odx, header, out_dir+"/"+f.getName());
		}
	}
	
	
	public ScaleOnResidents(String od_dir,FilenameFilter ff,ZoneConverter zc,double threshold) {
		
		this.od_dir = od_dir;
		this.ff =ff;
		this.zc = zc;
		this.threshold = threshold;
		
		String file = od_dir.substring(od_dir.indexOf("_")+1,od_dir.lastIndexOf("cellXHour")+9)+ "-comuni2012-HOME-null.ser";  
		System.out.println(file);
		
		
		//String file = "file_pls_piem_file_pls_piem_01-06-2015-01-07-2015_minH_0_maxH_25_ABOVE_8limit_5000_cellXHour-comuni2012-HOME-null.ser";
		//String file = "file_pls_piem_file_pls_piem_01-06-2015-01-07-2015_minH_0_maxH_25_ABOVE_400limit_1000_cellXHour-comuni2012-HOME-null.ser";
		resident_density = (Map<String,Double>)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/AggregatedSpaceDensity/"+file));
		istat = IstatCensus2011.getInstance().computeDensity(ISTAT_REF_INDEX, false, false);

		hour_od = new HashMap<>();
		System.out.println(Config.getInstance().base_folder+"/ODMatrix/"+od_dir);
		for(File f: new File(Config.getInstance().base_folder+"/ODMatrix/"+od_dir).listFiles(ff)) {
			if(f.getName().equals("latlon.csv")) continue;
			int h = f.getName().startsWith("od") ? Integer.parseInt(f.getName().split("-")[2]) : Integer.parseInt(f.getName().split("_")[3].substring(8, 10));
			AddMap odx = ODParser.parse(f.getAbsolutePath(),zc);
			System.out.println("Done "+h);
			hour_od.put(h, odx);
		}
	}
	
	

	private AddMap scale(AddMap odx, int odx_h) {

		int scaleBy = -1;
		if (odx_h < 13)
			scaleBy = BY_FROM;
		else
			scaleBy = BY_TO;

		int minH = 0;
		int maxH = 0;
		if (scaleBy == BY_FROM) {
			minH = 0;
			maxH = 13;
		}
		if (scaleBy == BY_TO) {
			minH = 13;
			maxH = 23;
		}

		// sum all od until 12
		AddMap h2w = new AddMap();
		for (int h = minH; h < maxH; h++)
			h2w.addAll(hour_od.get(h));

		// compute sum by SCALE_BY
		AddMap odSum = new AddMap();
		for (String k : h2w.keySet())
			odSum.add(k.split("-")[scaleBy], h2w.get(k));

		// normalize by row
		AddMap norm = new AddMap();
		for (String k : odx.keySet()) {
			Double sum = odSum.get(k.split("-")[scaleBy]);
			norm.put(k,sum != null && sum > 0 && odx.get(k) > threshold ? odx.get(k) / sum : 0);
		}

		// multiply by resident density
		for (String k : norm.keySet()) {
			Double rd = resident_density.get(k.split("-")[scaleBy]);
			if (rd == null) {
				System.out.println(k.split("-")[scaleBy]);
				continue;
			}
			norm.put(k, norm.get(k) * rd);
		}

		Map<String, double[]> map_mq = DensityMultiplier.getScale(odSum, istat,10, false, true);

		if (PLOT_DEBUG) {

			try {
				DensityComparator.compare("xxx", odSum, "comapre", istat);
			} catch (Exception e) {
				// e.printStackTrace();
			}

			try {
				DensityComparator.compare("scaled-xxx",DensityMultiplier.scale(odSum, map_mq), "compare",istat);
			} catch (Exception e) {
				// e.printStackTrace();
			}
		}

		// scale by from
		AddMap scaled = new AddMap();
		for (String k : odx.keySet()) {
			String from = k.split("-")[0];
			double[] mq = map_mq.get(from);
			if (mq != null)
				scaled.put(k, norm.get(k) * mq[0] + mq[1]);
		}

		return scaled;
	}
}
