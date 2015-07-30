package analysis.istat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

import region.RegionI;
import region.RegionMap;
import utils.AddMap;
import utils.Config;
import utils.CopyAndSerializationUtils;
import visual.kml.KMLHeatMap;


public class MEF_IRPEF {
	
	public static final String[] DIMENSIONS = new String[]{
		"Numero contribuenti",
		"Reddito da fabbricati - Frequenza",
		"Reddito da fabbricati - Ammontare",
		"Reddito da lavoro dipendente e assimilati - Frequenza",
		"Reddito da lavoro dipendente e assimilati - Ammontare",
		"Reddito da pensione - Frequenza",
		"Reddito da pensione - Ammontare",
		"Reddito da lavoro autonomo (compresi nulli) - Frequenza",
		"Reddito da lavoro autonomo (compresi nulli) - Ammontare",
		"Reddito spettanza imprenditore ordinaria  (compresi nulli) - Frequenza",
		"Reddito spettanza imprenditore ordinaria  (compresi nulli) - Ammontare",
		"Reddito spettanza imprenditore semplificata (compresi nulli) - Frequenza",
		"Reddito spettanza imprenditore semplificata (compresi nulli) - Ammontare",
		"Reddito da partecipazione  (compresi nulli) - Frequenza",
		"Reddito da partecipazione (compresi nulli) - Ammontare",
		"Reddito imponibile - Frequenza",
		"Reddito imponibile - Ammontare",
		"Imposta netta - Frequenza",
		"Imposta netta - Ammontare",
		"Reddito imponibile addizionale - Frequenza",
		"Reddito imponibile addizionale - Ammontare",
		"Addizionale regionale dovuta - Frequenza","Addizionale regionale dovuta - Ammontare",
		"Addizionale comunale dovuta - Frequenza","Addizionale comunale dovuta - Ammontare",
		"Reddito complessivo minore o uguale a zero euro - Frequenza",
		"Reddito complessivo minore o uguale a zero euro - Ammontare",
		"Reddito complessivo da 0 a 10000 euro - Frequenza",
		"Reddito complessivo da 0 a 10000 euro - Ammontare",
		"Reddito complessivo da 10000 a 15000 euro - Frequenza",
		"Reddito complessivo da 10000 a 15000 euro - Ammontare",
		"Reddito complessivo da 15000 a 26000 euro - Frequenza",
		"Reddito complessivo da 15000 a 26000 euro - Ammontare",
		"Reddito complessivo da 26000 a 55000 euro - Frequenza",
		"Reddito complessivo da 26000 a 55000 euro - Ammontare",
		"Reddito complessivo da 55000 a 75000 euro - Frequenza",
		"Reddito complessivo da 55000 a 75000 euro - Ammontare",
		"Reddito complessivo da 75000 a 120000 euro - Frequenza",
		"Reddito complessivo da 75000 a 120000 euro - Ammontare",
		"Reddito complessivo oltre 120000 euro - Frequenza",
		"Reddito complessivo oltre 120000 euro - Ammontare"
	};
	
	
	private static MEF_IRPEF instance;
	private Map<String,long[]> data;
	
	public static MEF_IRPEF getInstance() {
		if(instance == null) 
			instance = new MEF_IRPEF();
		return instance;
	}
	
	private static final int YEAR = 2013;
	private MEF_IRPEF() {
		try {
			
			data = new HashMap<String,long[]>();
			
			BufferedReader br = new BufferedReader(new FileReader("G:/DATASET/TI-CHALLENGE-2015/ISTAT/Redditi_e_principali_variabili_IRPEF_su_base_comunale_CSV_"+YEAR+".csv"));
			String line;
			br.readLine(); // skip header
			while((line=br.readLine())!=null) {
				String[] e = line.split(";");
				
				String comune_id = e[2];
				String comune_name = e[3];
				
				//System.out.println(e.length+" "+line);
				
				long[] d = new long[DIMENSIONS.length];
				for(int i=0; i<d.length;i++) {
					if(i+7 >= e.length) d[i] = 0;
					else d[i] = e[i+7].length() == 0 ? 0 : Long.parseLong(e[i+7]);
				}
				
				long[] v = data.get(comune_id);
				if(v == null) 
					data.put(comune_id, d);
				else
					data.put(comune_id, add(d,v));
			}
			br.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public AddMap redditoPC(boolean print) {
		String title = "RedditoProCapita"+YEAR;
		int[] indices = new int[]{24,26,28,30,32,34,34,38};
		RegionMap rm = (RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/comuni2014.ser"));
		rm.setName(title);
		AddMap density = new AddMap();
		for(RegionI r : rm.getRegions()) {
			
			String key = r.getName();// r.getName().indexOf("_") > 0 ? r.getName().substring(0,r.getName().indexOf("_")) : r.getName();
			long[] v = data.get(key);
			double value = 0;
			if(v!=null) {
				for(int i: indices)
					value+=v[i];
				value/=v[0];
			}
			density.add(key, value);
		}
		
		
		Map<String,Double> toDraw = new HashMap<String,Double>();
		for(String k: density.keySet()) {
			toDraw.put(k,(density.get(k) - 12000)/2000);
		}
		
		
		
		if(print) {
			try {
				KMLHeatMap.drawHeatMap(Config.getInstance().base_folder+"/TIC2015/"+title+".kml",density,rm,"",false);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return density;
	}
	
	
	public AddMap gini(boolean print) {
		String title = "IndiceGini"+YEAR;
		int[] pop_i = new int[]{23,25,27,29,31,33,35,37};
		int[] red_i = new int[]{24,26,28,30,32,34,34,38};
		RegionMap rm = (RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/comuni2014.ser"));
		rm.setName(title);
		AddMap density = new AddMap();
		for(RegionI r : rm.getRegions()) {
			long[] v = data.get(r.getName());
			double gini = 1;
			if(v!=null) {
				
				double tot_pop = 0;
				double tot_red = 0;
				for(int i=0; i<pop_i.length;i++) {
					tot_pop += v[pop_i[i]];
					tot_red += v[red_i[i]]; 
				}
				
				double[] x = new double[pop_i.length];
				double[] y = new double[red_i.length];
				for(int i=0; i<x.length;i++) {
					x[i] = v[pop_i[i]] / tot_pop;
					y[i] = v[red_i[i]] / tot_red;
				}
				
				for(int i=1; i<x.length;i++) {
					x[i] = x[i] + x[i-1];
					y[i] = y[i] + y[i-1];
				}
				
				
				
				
				for(int k=1; k<x.length;k++) {
					gini = gini - (x[k] - x[k-1]) * (y[k] + y[k-1]);
				}
				
			}
			
			density.add(r.getName(), gini);
		}
		
	
		
		
		if(print) {
			try {
				KMLHeatMap.drawHeatMap(Config.getInstance().base_folder+"/TIC2015/"+title+".kml",density,rm,"",false);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return density;
	}
	
	
	private long[] add(long[] a, long[] b) {
		long[] x = new long[a.length];
		for(int i=0; i<x.length;i++)
			x[i] = a[i] + b[i];
		return x;
	}
	
	
	public static void main(String[] args) throws Exception {
		MEF_IRPEF ic = MEF_IRPEF.getInstance();
		//ic.redditoPC(true);
		ic.gini(true);
		System.out.println("Done");
	}
	
}
