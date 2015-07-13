package region;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import utils.Config;
import utils.CopyAndSerializationUtils;
import utils.GeomUtils;
import utils.Logger;



public class CreatorRegionMapFromGIS {
	

	
	public static void main(String[] args) throws Exception {
		/*
		String[] cities = new String[]{"venezia","milano","torino","napoli","roma","palermo","bari"};
		for(String city: cities) {
			String name = "tic-"+city+"-gird";
			String input_file = "G:/DATASET/GEO/ti-challenge/"+name+".csv";
			String output_obj_file=Config.getInstance().base_folder+"/RegionMap/"+name+".ser";
			processWTK(name,input_file,output_obj_file);
		}
		*/
		
		String input_file = "G:/DATASET/TI-CHALLENGE-2015/ZIP/CAPS.csv";
		String output_obj_file=Config.getInstance().base_folder+"/RegionMap/caps.ser";
		processWTK("CAPS",input_file,output_obj_file);
		
		
		/*
		String name = "torino_circoscrizioni_geo";
		String input_file = "G:/DATASET/GEO/"+name+".csv";
		String output_obj_file=Config.getInstance().base_folder+"/RegionMap/"+name+".ser";
		processWTK(name,input_file,output_obj_file);
		*/
		
		/*
		String[] cities = new String[]{"Venezia","Firenze","Torino","Lecce"};
		for(String city: cities) {
			System.out.println("Processing "+city+" ...");
			String input_file = "G:/DATASET/GEO/"+city.toLowerCase()+"/"+city.toLowerCase()+"_tourist_area.csv";
			String outputname = city+"TouristArea";
			String output_obj_file=Config.getInstance().base_folder+"/RegionMap/"+outputname+".ser";
			processKML(outputname,input_file,output_obj_file);
		}
		*/
		
		
		Logger.logln("Done!");
	}
	
	
	public static RegionMap processWTK(String name, String input_file, String output_obj_file) throws Exception {
		
		RegionMap rm = new RegionMap(name);
		
		BufferedReader br = new BufferedReader(new FileReader(input_file));
		String line;
		br.readLine(); // skip header
		while((line=br.readLine())!=null) {
			String[] e = line.split("\t");
			String wtk_shape = e[0];
			wtk_shape = wtk_shape.replaceAll("\"MULTIPOLYGON \\(\\(\\(", "");
			wtk_shape = wtk_shape.replaceAll("\\)\\)\\)\"", "");
			String n = e[1];
			
			String[] polys = wtk_shape.split("\\)\\),\\(\\(");
			
			if(n.equals("57034")) {
				System.out.println("57034");
				System.out.println(wtk_shape);
			}
			
			for(int i=0; i<polys.length;i++)
				rm.add(new Region(polys.length > 1 ? n+"_"+i : n,GeomUtils.openGis2Geom(polys[i])));
		}
		
		br.close();
		
		rm.printKML();
		CopyAndSerializationUtils.save(new File(output_obj_file), rm);
		
		return rm;
	}
	
	
	public static RegionMap processKML(String name, String input_file, String output_obj_file) throws Exception {
		
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
