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

public class IstatCensus2011 {
	
	
	public static final String[] DIMENSIONS = new String[]{
	"Residentpopulation-Total",
	"Residentpopulation-Male",
	"Residentpopulation-Female",
	"Residentpopulation-Single",
	"Residentpopulation-Married-separated",
	"Residentpopulation-Legallyseparated",
	"Residentpopulation-Widower",
	"Residentpopulation-Divorced",
	"Residentpopulation-Male-Single",
	"Residentpopulation-Male-Marriedorseparated",
	"Residentpopulation-Male-Legallyseparated",
	"Residentpopulation-Male-Widower",
	"Residentpopulation-Male-Divorced",
	"Residentpopulation-Age0-5years",
	"Residentpopulation-Age5-9years",
	"Residentpopulation-Age10-14years",
	"Residentpopulation-Age15-19years",
	"Residentpopulation-Age20-24years",
	"Residentpopulation-Age25-29years",
	"Residentpopulation-Age30-34years",
	"Residentpopulation-Age35-39years",
	"Residentpopulation-Age40-44years",
	"Residentpopulation-Age45-49years",
	"Residentpopulation-Age50-54years",
	"Residentpopulation-Age55-59years",
	"Residentpopulation-Age60-64years",
	"Residentpopulation-Age65-69years",
	"Residentpopulation-Age70-74years",
	"Residentpopulation-Age74-years",
	"Residentpopulation-Male-Age0-5years",
	"Residentpopulation-Male-Age5-9years",
	"Residentpopulation-Male-Age10-14years",
	"Residentpopulation-Male-Age15-19years",
	"Residentpopulation-Male-Age20-24years",
	"Residentpopulation-Male-Age25-29years",
	"Residentpopulation-Male-Age30-34years",
	"Residentpopulation-Male-Age35-39years",
	"Residentpopulation-Male-Age40-44years",
	"Residentpopulation-Male-Age45-49years",
	"Residentpopulation-Male-Age50-54years",
	"Residentpopulation-Male-Age55-59years",
	"Residentpopulation-Male-Age60-64years",
	"Residentpopulation-Male-Age65-69years",
	"Residentpopulation-Male-Age70-74years",
	"Residentpopulation-Male-Age74-years",
	"Residentpopulation-Total-Ageover6years",
	"Residentpopulation-withuniversitydegree",
	"Residentpopulation-withdiplomaofsecondaryschool",
	"Residentpopulation-withdiplomaofmiddleschool",
	"Residentpopulation-withdiplomaofelementaryschool",
	"Residentpopulation-alphabets",
	"Residentpopulation-illiterate",
	"Residentpopulation-Male-Ageover6years",
	"Residentpopulation-Male-withuniversitydegree",
	"Residentpopulation-Male-withdiplomaofsecondaryschool",
	"Residentpopulation-Male-withdiplomaofmiddleschool",
	"Residentpopulation-Male-withdiplomaofelementaryschool",
	"Residentpopulation-Male-alphabets",
	"Residentpopulation-Male-illiterate",
	"Residentpopulation-Total-over15years-belongingtothelaborforce",
	"Residentpopulation-Total-over15years(FL)",
	"Residentpopulation-Total-over15years-unemployedseekingnewemployment",
	"Residentpopulation-Male-over15years-belongingtothelaborforce",
	"Residentpopulation-Male-over15years-employed(FL)",
	"Residentpopulation-Male-over15years-unemployedseekingnewemployment",
	"Residentpopulation-Total-over15years-notbelongingtothelaborforce(NFL)",
	"Residentpopulation-Male-over15years-notbelongingtothelaborforce(NFL)",
	"Residentpopulation-Total-over15years-housewife",
	"Residentpopulation-Total-over15years-students",
	"Residentpopulation-Male-over15years-students",
	"Residentpopulation-Total-over15years-inothercondition",
	"Residentpopulation-Male-over15years-inothercondition",
	"Residentpopulation-movingdailyinthemunicipalityofusualresidence",
	"Residentpopulation-movingdailyoutofthemunicipalityofusualresidence",
	"Residentpopulation-Total-over15years-earnersofincomeorcapital",
	"Residentpopulation-Male-over15years-earnersofincomeorcapital",
	"ForeignersandstatelessresidentinItaly-Total",
	"ForeignersandstatelessresidentinItaly-Male",
	"ForeignersandstatelessresidentinItaly-Age0-29years",
	"ForeignersandstatelessresidentinItaly-Age30-54years",
	"ForeignersandstatelessresidentinItaly-Age>54years",
	"ForeignersandstatelessresidentinItaly-Male-Age0-29years",
	"ForeignersandstatelessresidentinItaly-Male-Age30-54years",
	"ForeignersandstatelessresidentinItaly-Male-Age>54years",
	"ForeignersresidentinItaly-Europe",
	"ForeignersresidentinItaly-Africa",
	"ForeignersresidentinItaly-America",
	"ForeignersresidentinItaly-Asia",
	"ForeignersresidentinItaly-Oceania",
	"StatelessresidentinItaly",
	"ForeignersresidentinItaly-Total-",
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
			
			BufferedReader br = new BufferedReader(new FileReader("G:/DATASET/TI-CHALLENGE-2015/ISTAT/census-data.csv"));
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
	
	public AddMap computeDensity(int var, boolean print) {
		
		RegionMap rm = (RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/comuni2012.ser"));
		rm.setName(DIMENSIONS[var]);
		AddMap density = new AddMap();
		for(RegionI r : rm.getRegions()) {
			
			String key = r.getName();// r.getName().indexOf("_") > 0 ? r.getName().substring(0,r.getName().indexOf("_")) : r.getName();
			int[] v = data.get(key);
			double value = v == null ? 0 : v[var];
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
		ic.computeDensity(46,true);
		System.out.println("Done");
	}
	
}
