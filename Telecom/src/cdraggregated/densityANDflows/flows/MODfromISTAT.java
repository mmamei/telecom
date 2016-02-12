package cdraggregated.densityANDflows.flows;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import utils.mod.Comune;
import utils.mod.Util;
import utils.mod.Write;


public class MODfromISTAT {
	
	private static String [] MezziDiTrasporto = {"07","11"};
//	Codici per i Mezzi di Trasporto
//	01 treno;
//	02 tram;
//	03 metropolitana;
//	04 autobus urbano, filobus;
//	05 corriera, autobus extra-urbano;
//	06 autobus aziendale o scolastico;
//	07 auto privata (come conducente);
//	08 auto privata (come passeggero);
//	09 motocicletta,ciclomotore,scooter;
//	10 bicicletta;
//	11 altro mezzo;
//	12 a piedi;
	
	private static Integer[] regioniInConsiderazione={1};
//	Codici regione
//	1 Piemonte
//	2 Valle d'Aosta
//	3 Lombardia
//	4 Trentino Alto Adige
//	5 Veneto
//	6 Friuli-Venezia Giulia
//	7 Liguria
//	8 Emilia Romagna 
//	9 Toscana
//	10 Umbria
//	11 Marche
//	12 Lazio
//	13 Abruzzo
//	14 Molise
//	15 Campania
//	16 Puglia
//	17 Basalicata
//	18 Calabria
//	19 Sicilia
//	20 Sardegna
	
	
	
	private static Integer numeroFascieOrarie=1;
	
	static ArrayList <Integer> provinceInConsiderazione = new ArrayList<Integer>();
	
	public static void main(String[] args) throws Exception{
		
		provinceInConsiderazione = SetprovinceInConsiderazione();
		HashMap<String, Comune> comuni = new HashMap<String, Comune>();
		BufferedReader br = new BufferedReader(new FileReader("G:/DATASET/OD-ALBERTO-FRANCIA/MATRICE_PENDOLARISMO_2011/matrix_pendo2011_10112014.txt"));
		Random random = new Random();		
		String line;
		int i=0;
		

		while((line = br.readLine())!=null) {
			
			i++;
			
			line=line.replace("         "," ");
			line=line.replace("        "," ");
			line=line.replace("       "," ");
			line=line.replace("      "," ");
			line=line.replace("     "," ");
			line=line.replace("    "," ");
			line=line.replace("   "," ");
			line=line.replace("  "," ");

			String[] r=line.split(" ");
			
			int ora=0;
			if(r[11].trim().equals("+")){
				ora=random.nextInt(3)+1;
			}
			else{
				ora=Integer.parseInt(r[11]);
			}
			
			
			
			String cp = r[2].trim();
			String cc = r[3].trim();
			String cod = Util.uniformeCode(cc, cp);
						
			
			Util.syso(i+" su 4876242 ");
			
			if(!comuni.containsKey(cod)){				///crea nuovo comune
				Comune c = new Comune(cc,cp);
				if((double) Double.parseDouble(r[13].trim())>0.5){
//					if(WriteValue(r,ora,c))				///con questa riga potrei eliminare la funzione "centraConRegione", ma non so il perchè con questa riga il lancio del codice diventa troppo lungo.... 
					WriteValue(r,ora,c);
					comuni.put(cod, c);
				}
			}	
			else{										/// aggiorna comune esistente
				Comune c = comuni.get(cod);
				WriteValue(r,ora,c);		
			}
		}
		
		i=0;
		for(String z:comuni.keySet()){
			Util.syso(comuni.get(z).toString());
			if(!comuni.get(z).getNome().equals("vuoto")){
				if(comuni.get(z).centraConRegione(provinceInConsiderazione)){
					i++;
				}
				else{
					System.out.println(comuni.get(z).toString());
				}
			}
		}
		
		System.out.println();
		System.out.println("numero dei comuni: "+comuni.size());
		System.out.println("numero dei comuni che centrano con il Piemonte: "+i);
		System.out.println();
		
		i=0;
		int g =0;
		HashMap<String, Comune> comuniOk = new HashMap<String, Comune>();
		for(String j:comuni.keySet()){
			g++;
			if(!comuni.get(j).getNome().equals("vuoto")){
				if(comuni.get(j).centraConRegione(provinceInConsiderazione)){
					comuniOk.put(j, comuni.get(j));
//					sout(j);
					i++;
				}
				
			}			
		}
		
		System.out.println("comuni analizzati: "+g);
		System.out.println("comuni eliminati: "+(g-i));
		System.out.println("inizio creazione Matrici OD delle regioni d'interesse");
		System.out.println("ComuniOk size="+comuniOk.size());
		System.out.println();
		
		
		
		Write.Matrix(comuniOk, numeroFascieOrarie);
	    
		
//		sout("");
//		sout(comuniOk.get("001-128").toString());
//		sout("PROVA: "+comuniOk.get("001-128").GetPartonoPer(1).get("001-146"));
	    
		br.close();
		
	}
	

	
	private static boolean WriteValue (String[] r, Integer ora, Comune c){
//		Scrive il valore di persone in movimento nella Hashmap Partono1, Partono2, Partono3 o Partono4 (a seconda dell'orario) nel comune passato.
//		Ritorna true se i valori passati coicidono col mezzo di trasporto prescelto, con le regioni prese in considerazione.
		
		String codper=Integer.parseInt(r[7].trim())+"-"+Integer.parseInt(r[8].trim());
		
		r[10]=r[10].trim();		
		for(int i=0; i<MezziDiTrasporto.length;i++){
			if(MezziDiTrasporto[i].equals(r[10])){
				for(int j=0; j<regioniInConsiderazione.length; j++){
					String [] s=codper.split("-");
					int cp=Integer.parseInt(s[0]);
					if(c.getRegione()==regioniInConsiderazione[j]||provinceInConsiderazione.contains(cp)){
						if(r[14].trim().equals("ND")){
							if((int) Double.parseDouble(r[13].trim())==0){
								return false;
							}
							c.SetPartonoPer(codper, Double.parseDouble(r[13].trim()), ora);
							return true;
		//					syso(" per: "+codper+" in"+r[13].trim());
								
						}
						else{
							c.SetPartonoPer(codper, Double.parseDouble(r[14].trim()), ora);
							return true;
		//					syso(" per: "+codper+" in"+r[14].trim());
						}
					}
				}
			}
		}
		return false;
	}
	
	
	static HashMap<Integer,int[]> region2province = new HashMap<>();
	static {
		region2province.put(1, new int[]{1,2,3,4,5,6, 96,103});
		region2province.put(2, new int[]{7});
	}
	
	private static ArrayList<Integer> SetprovinceInConsiderazione(){
		ArrayList<Integer> p= new ArrayList<Integer>();
		for(int i=0;i<regioniInConsiderazione.length;i++){
			
			if(regioniInConsiderazione[i]==1){
				Integer[] province1={1,2,3,4,5,6, 96,103};
				for(int k=0; k<province1.length;k++){
					p.add(province1[k]);
				}
			}
			if(regioniInConsiderazione[i]==2){
				Integer[] province2={7};
				for(int k=0; k<province2.length;k++){
					p.add(province2[k]);
				}
			}
			if(regioniInConsiderazione[i]==3){
				Integer[] province3={12,13,14,015,016,017,18,19,20,97,98,108};
				for(int k=0; k<province3.length;k++){
					p.add(province3[k]);
				}
			}
			if(regioniInConsiderazione[i]==4){
				Integer[] province4={21,22};
				for(int k=0; k<province4.length;k++){
					p.add(province4[k]);
				}
			}
			if(regioniInConsiderazione[i]==5){
				Integer[] province5={23,24,25,26,27,28,29};
				for(int k=0; k<province5.length;k++){
					p.add(province5[k]);
				}
			}
			
			if(regioniInConsiderazione[i]==6){
				Integer[] province6={30,31,32,93};
				for(int k=0; k<province6.length;k++){
					p.add(province6[k]);
				}
			}
			if(regioniInConsiderazione[i]==7){
				Integer[] province7={8,9,10,11};
				for(int k=0; k<province7.length;k++){
					p.add(province7[k]);
				}
			}
			if(regioniInConsiderazione[i]==8){
				Integer[] province8={33,34,35,36,37,38,39,40,99};
				for(int k=0; k<province8.length;k++){
					p.add(province8[k]);
				}
			}
			if(regioniInConsiderazione[i]==9){
				Integer[] province9={45,46,47,48,49,50,51,52,53,100};
				for(int k=0; k<province9.length;k++){
					p.add(province9[k]);
				}
			}
			if(regioniInConsiderazione[i]==10){
				Integer[] province10={54,55};
				for(int k=0; k<province10.length;k++){
					p.add(province10[k]);
				}
			}
			if(regioniInConsiderazione[i]==11){
				Integer[] province11={41,42,43,44,109};
				for(int k=0; k<province11.length;k++){
					p.add(province11[k]);
				}
			}
			if(regioniInConsiderazione[i]==12){
				Integer[] province12={56,57,58,59,60};
				for(int k=0; k<province12.length;k++){
					p.add(province12[k]);
				}
			}
			if(regioniInConsiderazione[i]==13){
				Integer[] province13={66,67,68,69};
				for(int k=0; k<province13.length;k++){
					p.add(province13[k]);
				}
			}
			if(regioniInConsiderazione[i]==14){
				Integer[] province14={70,94};
				for(int k=0; k<province14.length;k++){
					p.add(province14[k]);
				}
			}
			if(regioniInConsiderazione[i]==15){
				Integer[] province15={61,62,63,64,65};
				for(int k=0; k<province15.length;k++){
					p.add(province15[k]);
				}
			}
			if(regioniInConsiderazione[i]==16){
				Integer[] province16={71,72,73,74,75,110};
				for(int k=0; k<province16.length;k++){
					p.add(province16[k]);
				}
			}
			if(regioniInConsiderazione[i]==17){
				Integer[] province17={76,77};
				for(int k=0; k<province17.length;k++){
					p.add(province17[k]);
				}
			}
			if(regioniInConsiderazione[i]==18){
				Integer[] province18={78,79,80,101,102};
				for(int k=0; k<province18.length;k++){
					p.add(province18[k]);
				}
			}
			if(regioniInConsiderazione[i]==19){
				Integer[] province19={81,82,83,84,85,86,87,88,89};
				for(int k=0; k<province19.length;k++){
					p.add(province19[k]);
				}
			}
			if(regioniInConsiderazione[i]==20){
				Integer[] province20={90,91,92,95,104,105,106,107};
				for(int k=0; k<province20.length;k++){
					p.add(province20[k]);
				}
			}
		}
		return p;
	}

}



