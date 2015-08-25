package region;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Set;

import utils.Config;
import utils.CopyAndSerializationUtils;
import utils.GeomUtils;
import utils.Logger;

import com.vividsolutions.jts.geom.Geometry;



public class CreatorRegionMapFromGIS {
	

	
	public static void main(String[] args) throws Exception {
		/*
		//WKT,id,
		String[] cities = new String[]{"venezia","milano","torino","napoli","roma","palermo","bari"};
		for(String city: cities) {
			String name = "tic-"+city+"-gird";
			String input_file = Config.getInstance().dataset_folder+"/GEO/ti-challenge/"+name+".csv";
			String output_obj_file=Config.getInstance().base_folder+"/RegionMap/"+name+".ser";
			processWTK(name,input_file,output_obj_file,new int[]{1});
		}
		*/
		
		//WKT	CAP	
		//String input_file = Config.getInstance().dataset_folder+"/TI-CHALLENGE-2015/ZIP/CAPS2.csv";
		//String output_obj_file=Config.getInstance().base_folder+"/RegionMap/caps.ser";
		//processWTK("CAPS_ALL",input_file,output_obj_file,new int[]{1});
		
		/*
		String input_file = Config.getInstance().dataset_folder+"/GEO/census-sections/veneto-census-sections.csv";
		String output_obj_file=Config.getInstance().base_folder+"/RegionMap/veneto-census-sections.ser";
		processWTK("veneto-census-sections",input_file,output_obj_file);
		*/
		
		
		// WKT	COD_REG	COD_PRO	PROVINCIA	SIGLA	POP2001
		//String input_file = Config.getInstance().dataset_folder+"/GEO/prov2011.csv";
		//String output_obj_file=Config.getInstance().base_folder+"/RegionMap/prov2011.ser";
		//processWTK("prov2011",input_file,output_obj_file,new int[]{3});
		
		
		// WKT	COD_REG	REGIONE	POP2001
		//String input_file = Config.getInstance().dataset_folder+"/GEO/regioni.csv";
		//String output_obj_file=Config.getInstance().base_folder+"/RegionMap/regioni.ser";
		//processWTK("regioni",input_file,output_obj_file,new int[]{2});
		
		
		//WKT	PRO_COM	COD_REG	COD_PRO	NOME_COM	POP2001
		//String input_file = Config.getInstance().dataset_folder+"/GEO/comuni2014.csv";
		//String output_obj_file=Config.getInstance().base_folder+"/RegionMap/comuni2014.ser";
		//processWTK("comuni2014",input_file,output_obj_file,new int[]{1});
		
		
		//WKT	OBJECTID	COD_REG	COD_PRO	SHAPE_Leng	SHAPE_Area	CODICE_C_1	COD_ISTA_1	PRO_COM__1	NOME_COM_2
		//String input_file = Config.getInstance().dataset_folder+"/GEO/comuni2014.csv";
		//String output_obj_file=Config.getInstance().base_folder+"/RegionMap/comuni2014.ser";
		//processWTK("comuni2014",input_file,output_obj_file,new int[]{8});
		
		
		//WKT	OBJECTID	COD_REG	COD_PRO	COD_ISTAT	PRO_COM	NOME	SHAPE_Leng	SHAPE_Area
		//String input_file = Config.getInstance().dataset_folder+"/GEO/comuni2012.csv";
		//String output_obj_file=Config.getInstance().base_folder+"/RegionMap/comuni2012.ser";
		//processWTK("comuni2012",input_file,output_obj_file,new int[]{5});
		
		
		/*
		String name = "torino_circoscrizioni_geo";
		String input_file = Config.getInstance().dataset_folder+"/GEO/"+name+".csv";
		String output_obj_file=Config.getInstance().base_folder+"/RegionMap/"+name+".ser";
		processWTK(name,input_file,output_obj_file);
		*/
		
		
		
		/*
		String[] cities = new String[]{"Venezia","Firenze","Torino","Lecce"};
		for(String city: cities) {
			System.out.println("Processing "+city+" ...");
			String input_file = Config.getInstance().dataset_folder+"/GEO/"+city.toLowerCase()+"/"+city.toLowerCase()+"_tourist_area.csv";
			String outputname = city+"TouristArea";
			String output_obj_file=Config.getInstance().base_folder+"/RegionMap/"+outputname+".ser";
			processKML(outputname,input_file,output_obj_file);
		}
		*/
		
		
		Logger.logln("Done!");
	}
	
	
	
	
	public static RegionMap processWTK(String name, String input_file, String output_obj_file, int[] name_indexes) throws Exception {
		
		
		Set<String> tdc2015 = new HashSet<String>();
		tdc2015.add("TORINO");
		tdc2015.add("MILANO");
		tdc2015.add("VENEZIA");
		tdc2015.add("ROMA");
		tdc2015.add("BARI");
		tdc2015.add("NAPOLI");
		tdc2015.add("PALERMO");
		
		
		RegionMap rm = new RegionMap(name);
		
		BufferedReader br = new BufferedReader(new FileReader(input_file));
		String line;
		br.readLine(); // skip header
		while((line=br.readLine())!=null) {
			String[] e = line.split("\t");
			String wtk_shape = e[0];
			wtk_shape = wtk_shape.replaceAll("\"MULTIPOLYGON \\(\\(\\(", "");
			wtk_shape = wtk_shape.replaceAll("\\)\\)\\)\"", "");
			
			
			String n = "";
			for(int i: name_indexes)
				n = n + e[i];
			
			if(!tdc2015.contains(n)) continue;
			
			
			
			System.out.println(n);
			
			
			String[] polys = wtk_shape.split("\\),\\(");
			
			
			Geometry max_g = null; // polygon with max area
			for(int i=0; i<polys.length;i++) {
				String poly = polys[i].replaceAll("\\)|\\(", "");
				Geometry g = GeomUtils.openGis2Geom(poly);
				if(max_g == null || g.getArea() > max_g.getArea()) 
					max_g = g;
				//rm.add(new Region(polys.length > 1 ? n+"_"+i : n,g)); // this is to add all the regions in the multipolygon with a progressive counter
			}
			rm.add(new Region(n,max_g)); // this is to add the region (in the multipolygon) with the max area
		}
		
		br.close();
		
		rm.printKML();
		CopyAndSerializationUtils.save(new File(output_obj_file), rm);
		
		return rm;
	}
	
	
	public static RegionMap processKMLLine(String name, String input_file, String output_obj_file) throws Exception {
		
		RegionMap rm = new RegionMap(name);
		
		BufferedReader br = new BufferedReader(new FileReader(input_file));
		String line;
		while((line=br.readLine())!=null) {
			String[] e = line.split(";");
			String n = e[0];
			rm.add(new Region(n,e[1]));
		}
		
		br.close();
		
		rm.printKML();
		CopyAndSerializationUtils.save(new File(output_obj_file), rm);
		
		return rm;
	}
	
}
