package otherdata;

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

public class IstatCensus2011 {
	
	
	public static final String[] DIMENSIONS = new String[]{
	"Pop-Total",
	"Pop-Male",
	"Pop-Female",
	"Pop-Single",
	"Pop-Married-separated",
	"Pop-Legallyseparated",
	"Pop-Widower",
	"Pop-Divorced",
	"Pop-Male-Single",
	"Pop-Male-Marriedorseparated",
	"Pop-Male-Legallyseparated",
	"Pop-Male-Widower",
	"Pop-Male-Divorced",
	"Pop-Age0-5",
	"Pop-Age5-9",
	"Pop-Age10-14",
	"Pop-Age15-19",
	"Pop-Age20-24",
	"Pop-Age25-29",
	"Pop-Age30-34",
	"Pop-Age35-39",
	"Pop-Age40-44",
	"Pop-Age45-49",
	"Pop-Age50-54",
	"Pop-Age55-59",
	"Pop-Age60-64",
	"Pop-Age65-69",
	"Pop-Age70-74",
	"Pop-Age74-",
	"Pop-Male-Age0-5",
	"Pop-Male-Age5-9",
	"Pop-Male-Age10-14",
	"Pop-Male-Age15-19",
	"Pop-Male-Age20-24",
	"Pop-Male-Age25-29",
	"Pop-Male-Age30-34",
	"Pop-Male-Age35-39",
	"Pop-Male-Age40-44",
	"Pop-Male-Age45-49",
	"Pop-Male-Age50-54",
	"Pop-Male-Age55-59",
	"Pop-Male-Age60-64",
	"Pop-Male-Age65-69",
	"Pop-Male-Age70-74",
	"Pop-Male-Age74-",
	"Pop-Total-Ageover6",
	"Pop-university",
	"Pop-secondaryschool",
	"Pop-middleschool",
	"Pop-elementaryschool",
	"Pop-alphabets",
	"Pop-illiterate",
	"Pop-Male-Ageover6",
	"Pop-Male-university",
	"Pop-Male-secondaryschool",
	"Pop-Male-middleschool",
	"Pop-Male-elementaryschool",
	"Pop-Male-alphabets",
	"Pop-Male-illiterate",
	"Working Pop.",
	"Pop-Total-(FL)",
	"Unemployed Pop.",
	"Pop-Male-working",
	"Pop-Male-employed(FL)",
	"Pop-Male-unemployed",
	"Pop-Total-notworking(NFL)",
	"Pop-Male-notworking(NFL)",
	"Pop-Total-housewife",
	"Pop-Total-students",
	"Pop-Male-students",
	"Pop-Total-inothercondition",
	"Pop-Male-inothercondition",
	"Pop-movingdailyinthemunicipalityofusualresidence",
	"Pop-movingdailyoutofthemunicipalityofusualresidence",
	"Pop-Total-earnersofincomeorcapital",
	"Pop-Male-earnersofincomeorcapital",
	"Fore-Pop-Total",
	"Fore-Pop-Male",
	"Fore-Pop-Age0-29",
	"Fore-Pop-Age30-54",
	"Fore-Pop-Age>54",
	"Fore-Pop-Male-Age0-29",
	"Fore-Pop-Male-Age30-54",
	"Fore-Pop-Male-Age>54",
	"Fore-Pop-Europe",
	"Fore-Pop-Africa",
	"Fore-Pop-America",
	"Fore-Pop-Asia",
	"Fore-Pop-Oceania",
	"Fore-Pop-Other",
	"Fore-Pop-Total",
	"Housesoccupiedbyatleastonepersonresident",
	"Emptyhousesorhousesoccupiedonlybynotresidentpersons",
	"Othertypesofoccupiedaccommodation",
	"Surfaceofhousesoccupiedbyatleastoneresidentperson",
	"Familieslivinginrentalhousing",
	"Familieslivinginownedhousing",
	"Familieslivinginhousesoccupiedforotherreasons",
	"Families-Resident-Total",
	"Families-Resident-Totalmembers",
	"Families-Resident-1member",
	"Families-Resident-2members",
	"Families-Resident-3members",
	"Families-Resident-4members",
	"Families-Resident-5members",
	"Families-Resident-over6members",
	"Numberoffamiliesresidentwithover6members",
	"Buildingandgroupofbuilding-Total",
	"Buildingandgroupofbuildingused",
	"Residentialbuilding",
	"Buildingandgroupofbuildingwithproductiveuse",
	"Residentialbuildingwithbearingwalls",
	"Residentialbuildingmadewithreinforcedconcrete",
	"Residentialbuildingsmadewithothermaterials",
	"Residentialbuilding-builtbefore1919",
	"Residentialbuilding-builtfrom1919to1945",
	"Residentialbuilding-builtfrom1946to1960",
	"Residentialbuilding-builtfrom1961to1970",
	"Residentialbuilding-builtfrom1971to1980",
	"Residentialbuilding-builtfrom1981to1990",
	"Residentialbuilding-builtfrom1991to2000",
	"Residentialbuilding-builtfrom2001to2005",
	"Residentialbuilding-builtafter2005",
	"Residentialbuilding-with1floor",
	"Residentialbuilding-with2floors",
	"Residentialbuilding-with3floors",
	"Residentialbuilding-with4floorsormore",
	"Residentialbuilding-with1apartment",
	"Residentialbuilding-with2apartments",
	"Residentialbuilding-with3to4apartments",
	"Residentialbuilding-with5to8apartments",
	"Residentialbuilding-with9to15apartments",
	"Residentialbuilding-with16apartmentsormore",
	"Totalofapartmentsinresidentialbuilding"
	};

	

	
	private static IstatCensus2011 instance;
	private Map<String,int[]> data;
	
	
	public static IstatCensus2011 getInstance() {
		if(instance == null) 
			instance = new IstatCensus2011();
		return instance;
	}
	
	private IstatCensus2011() {
		try {
		
			data = new HashMap<String,int[]>();
			
			BufferedReader br = new BufferedReader(new FileReader(Config.getInstance().dataset_folder+"/TI-CHALLENGE-2015/ISTAT/census-data.csv"));
			String line;
			br.readLine(); // skip header
			while((line=br.readLine())!=null) {
				String[] e = line.split(";");
				//String comune_id = e[6].substring(0,5).endsWith("0") ? e[6].substring(0,4) : e[6].substring(0,5);
				
				String comune_id = e[2]+(e[4].length() == 1 ? "00" : e[4].length() == 2 ? "0" : "")+e[4];
				
				String comune_name = e[5];
				
				int[] d = new int[134];
				for(int i=0; i<d.length;i++)
					d[i] = Integer.parseInt(e[i+7]);
				
				int[] v = data.get(comune_id);
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
	
	public AddMap computeDensity(int var, boolean procapita, boolean print) {
		
		RegionMap rm = (RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/comuni2012.ser"));
		rm.setName(DIMENSIONS[var]);
		AddMap density = new AddMap();
		for(RegionI r : rm.getRegions()) {
			
			String key = r.getName();// r.getName().indexOf("_") > 0 ? r.getName().substring(0,r.getName().indexOf("_")) : r.getName();
			int[] v = data.get(key);
			double value = v == null ? 0 : procapita ? 1.0*v[var]/v[0] : v[var];
			density.add(key, value);
		}
		if(print) {
			try {
				KMLHeatMap.drawHeatMap(Config.getInstance().base_folder+"/TIC2015/"+DIMENSIONS[var]+".kml",density,rm,"",true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return density;
		
	}
	
	
	private int[] add(int[] a, int[] b) {
		int[] x = new int[a.length];
		for(int i=0; i<x.length;i++)
			x[i] = a[i] + b[i];
		return x;
	}
	
	
	public static void main(String[] args) throws Exception {
		IstatCensus2011 ic = IstatCensus2011.getInstance();
		ic.computeDensity(46,true,true);
		System.out.println("Done");
	}
	
}
