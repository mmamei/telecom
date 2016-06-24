package otherdata.TIbigdatachallenge2015;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

import utils.Config;
import visual.r.RPlotter;



public class MEF_IRPEF_BLOG {
	
	private Map<String,Double> id_reddito;
	private Map<String,Double> id_gini;
	private static MEF_IRPEF_BLOG instance = null;
	
	public static MEF_IRPEF_BLOG getInstance() {
		if(instance == null) 
			instance = new MEF_IRPEF_BLOG();
		return instance;
	}
	
	
	private MEF_IRPEF_BLOG() {
		try {
			Map<String,Double> name_reddito = new HashMap<String,Double>();
			Map<String,Double> name_gini = new HashMap<String,Double>();
			
			BufferedReader br = new BufferedReader(new FileReader(Config.getInstance().dataset_folder+"/TI-CHALLENGE-2015/ISTAT/irpef-gini.csv"));
			String line;
			//Denominazione Comune;Regione;Avg. ranking_disuguaglianza;gini index;reddito medio pro-capite
			br.readLine(); // skip header
			while((line=br.readLine())!=null) {
				line = line.replaceAll(",", ".");
				String[] e = line.split(";");
				double gini = Double.parseDouble(e[3]);
				double reddito = Double.parseDouble(e[4]);
				String key = (e[0]+"-"+e[1]).toLowerCase();
				name_reddito.put(key, reddito);
				name_gini.put(key, gini);
			}
			br.close();
			
			
			
			String max = null;
			for(String k: name_reddito.keySet())
				if(max == null || name_reddito.get(max) < name_reddito.get(k))
					max = k;
			
			System.out.println("reddito max blog = "+max+" value = "+name_reddito.get(max));
			
			
			Map<String,String> name2id = name2id();
			
			id_reddito = new HashMap<String,Double>();
			id_gini = new HashMap<String,Double>();
			
			for(String k: name_gini.keySet()) {
				String id = name2id.get(k);
				if(id != null) {
					id_reddito.put(id, name_reddito.get(k));
					id_gini.put(id, name_gini.get(k));
				}
				//else System.out.println(k);
			}
			
			System.out.println("reddito max blog = "+name2id.get(max));
			
		
			//System.out.println("name_reddito size = "+name_reddito.size());
			//System.out.println("name_gini size = "+name_gini.size());
			
			//System.out.println("id_reddito size = "+id_reddito.size());
			//System.out.println("id_gini size = "+id_gini.size());
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public Map<String,Double> redditoPC(boolean print) {
		return id_reddito;
	} 
	
	public Map<String,Double> gini(boolean print) {
		return id_gini;
	} 
	
	
	public static void main(String[] args) {
		
		
		MEF_IRPEF_BLOG blog_instance = MEF_IRPEF_BLOG.getInstance();
		

			
		
		MEF_IRPEF mef_instance = MEF_IRPEF.getInstance();
		
		
		RPlotter.plotCorrelation(blog_instance.redditoPC(false), mef_instance.redditoPC(false), "blog-reddito", "mef-reddito", Config.getInstance().base_folder+"/Images/corr-blog-mef-reddito.pdf",null,false);

		RPlotter.plotCorrelation(blog_instance.gini(false), mef_instance.gini(false), "blog-gini", "mef-gini", Config.getInstance().base_folder+"/Images/corr-blog-mef-gini.pdf",null,false);

		
		
	}
	
	
	public static Map<String,String> name2id() {
		Map<String,String> name2id = new HashMap<String,String>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(Config.getInstance().dataset_folder+"/TI-CHALLENGE-2015/ISTAT/Redditi_e_principali_variabili_IRPEF_su_base_comunale_CSV_"+MEF_IRPEF.MEF_YEAR+".csv"));
			String line;
			br.readLine(); // skip header
			while((line=br.readLine())!=null) {
				String[] e = line.split(";");
				
				String comune_id =  String.valueOf(Integer.parseInt(e[2].replaceAll("\"", "")));
				String comune_name = e[3];
				String region_name = e[5];
				String key = (comune_name+"-"+region_name).toLowerCase();
				name2id.put(key, comune_id);
			}
			br.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
		return name2id;
	}
	
	
	public static Map<String,String> id2name() {
		Map<String,String> name2id = name2id();
		Map<String,String> id2name = new HashMap<String,String>();
		for(String name: name2id.keySet()) {
			id2name.put(name2id.get(name), name);
		}
		return id2name;
	}
	
	

	
	
	
	
	
	
}
