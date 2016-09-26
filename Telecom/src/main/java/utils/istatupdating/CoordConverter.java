package utils.istatupdating;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

//questa classe converte le coordinate del file ComuniCoordUpdated.csv da sessagesimali a decimali e li salva in Dati/Map/Coord/CoordinateDecimaliComuni.csv

public class CoordConverter {
    public static void main(String[] args) throws Exception {
    	File file= new File("Dati/Map/Coord/CoordinateDecimaliComuni.csv");
		file.createNewFile();
		FileWriter fw = new FileWriter(file);
		BufferedWriter bu = new BufferedWriter(fw);
		String line;
		String lineu;
		BufferedReader be = new BufferedReader(new FileReader("Dati/Map/Coord/ComuniCoordUpdated.csv"));
		while((line = be.readLine())!=null) {
			String[] l=line.split(";");
			lineu=l[0]+";"+l[1]+";"+l[2]+";"+converter(l[3],l[4],l[5])+";"+converter(l[6],l[7],l[8])+";"+l[9]+";"+l[10]+";"+l[11]+";"+l[12]+";"+l[13]+";"+l[14];
			System.out.println(lineu);
			bu.write(lineu);
			bu.newLine();
		}
		
		
		
		be.close();
		bu.close();
    }

	public static double converter(String si, String sm, String ss){
		if(Double.parseDouble(si.trim())>0){
			double d=Double.parseDouble(si.trim())+((Double.parseDouble(ss.trim())/60)+Double.parseDouble(sm.trim()))/60;
			return d;
		}
		else{
			System.out.println("MI SA CHE NON SEI IN ITAILA.....");
			double d=Double.parseDouble(si.trim())-((Double.parseDouble(ss.trim())/60)+Double.parseDouble(sm.trim())/60);
			return d;
		}
		
	}
}