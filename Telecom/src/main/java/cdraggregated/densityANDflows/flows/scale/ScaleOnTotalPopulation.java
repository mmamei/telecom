package cdraggregated.densityANDflows.flows.scale;

import java.io.File;
import java.io.FilenameFilter;
import java.util.HashMap;
import java.util.Map;

import otherdata.TIbigdatachallenge2015.IstatCensus2011byRegion;
import utils.AddMap;
import utils.Config;
import cdraggregated.densityANDflows.density.DensityComparator;
import cdraggregated.densityANDflows.density.DensityMultiplier;
import cdraggregated.densityANDflows.flows.ODParser;

public class ScaleOnTotalPopulation {


	public static int ISTAT_REF_INDEX = 59;
	
	private String od_dir;
	private FilenameFilter ff;
	private String region;
	private AddMap istat = null;
	
	
	private static final String SCALE_TYPE = "scaled-on-tot-population";
	
	
	public static void main(String[] args) {
		
		
		ScaleOnTotalPopulation odm = null;
		
		/*
		odm = new ScaleOnTotalPopulation("ODMatrixHW_file_pls_lomb_file_pls_lomb_01-03-2014-30-03-2014_minH_0_maxH_25_ABOVE_8limit_20000_cellXHour_odlombardia",null,"LOMBARDIA");
		odm.scaleAll();
		
		odm = new ScaleOnTotalPopulation("ODMatrixHW_file_pls_piem_file_pls_piem_01-06-2015-01-07-2015_minH_0_maxH_25_ABOVE_8limit_20000_cellXHour_odpiemonte",null,"PIEMONTE");
		odm.scaleAll();
		
		odm = new ScaleOnTotalPopulation("ODMatrixHW_file_pls_er_file_pls_er_01-04-2015-30-04-2015_minH_0_maxH_25_ABOVE_8limit_20000_cellXHour_odemiliaromagna",null,"EMILIA-ROMAGNA");
		odm.scaleAll();
		*/
		
		
		FilenameFilter ff= null;
		
		ff = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
			return name.matches("[0-9]+_mod_20150504[0-9]+_20150504[0-9]+_calabrese_lombardia_comuni_istat.txt");
			}
		};
		odm = new ScaleOnTotalPopulation("matrici_lombardia/orarie",ff,"LOMBARDIA");
		odm.scaleAll();
		
		
		ff = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
			return name.matches("[0-9]+_mod_20150527[0-9]+_20150527[0-9]+_calabrese_piemonte_comuni\\+torinoasc.txt");
			}
		};
		odm = new ScaleOnTotalPopulation("matrici_piemonte/orarie",ff,"PIEMONTE");
		odm.scaleAll();
		
		ff = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
			return name.matches("[0-9]+_mod_20150416[0-9]+_20150416[0-9]+_calabrese_emilia_regione\\+ascbologna.txt");
			}
		};
		
		odm = new ScaleOnTotalPopulation("matrici_emilia/orarie",ff,"EMILIA-ROMAGNA");
		odm.scaleAll();
		
		System.out.println("Done");
		
	}
	
	

	public ScaleOnTotalPopulation(String od_dir,FilenameFilter ff,String region) {
		this.od_dir = od_dir;
		this.ff = ff;
		this.region = region;
		istat = IstatCensus2011byRegion.getInstance().computeDensity(ISTAT_REF_INDEX, false, false);
	}
	
	public void scaleAll() {
		String out_dir = Config.getInstance().base_folder+"/ODMatrix/"+od_dir+"-"+SCALE_TYPE;
		new File(out_dir).mkdirs();
		
		File based = new File(Config.getInstance().base_folder+"/ODMatrix/"+od_dir);
		for(File f: based.listFiles(ff)) {
			if(f.getName().equals("latlon.csv")) continue;
			Map<String,Object > header = ODParser.parseHeader(f.getAbsolutePath());
			header.put("scale", SCALE_TYPE);
			
			//20000
			//149833 su 546108
			String n = (String) header.get("# di utenti su utenti del campione");
			if(n.contains("su")) n = n.substring(n.lastIndexOf(" "));
			int tot_num = Integer.parseInt(n.trim());
			
			double ratio = istat.get(region) / tot_num;
			
			AddMap odx = ODParser.parse(f.getAbsolutePath(),null);
			AddMap scaled = new AddMap();
			
			for (String k : odx.keySet()) 
				scaled.put(k, odx.get(k) * ratio);
			ODParser.save(scaled, header, out_dir+"/"+f.getName());
		}
	}
}
