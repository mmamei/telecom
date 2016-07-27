package cdraggregated.densityANDflows.flows;



import java.io.BufferedReader;

import java.io.FileReader;

import java.util.ArrayList;

import java.util.HashMap;



import utils.mod.Comune;

import utils.mod.Util;

import utils.mod.Write;





public class MODfromISTAT {


	static String percorsoInput="G:/DATASET/OD-ALBERTO-FRANCIA/MATRICE_PENDOLARISMO_2011/matrix_pendo2011_10112014.txt";

	static String percorsoOutput="C:/BASE/Francia";


//	se true il codice genererà 4 file MOD per ogni fascia oraria, ognuno riferito al tempo impiegato a compiere il tragitto,

//	nello specifico:

//	1 fino a 15 minuti;

//	2 da 16 a 30 minuti;

//	3 da 31 a 60 minuti;

//	4 oltre 60 minuti;

	

	//	se true genera un file contenente la media dei tempi per effettuare il percorso
	static boolean averageMOD = true;

	

	private static String [] regioniInConsiderazione = {"Lombardia"};



	

	private static String [] MezziDiTrasporto = {"07"};

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

	

	private static ArrayList <Integer> provinceInConsiderazione = new ArrayList<Integer>();

	

	public static void main(String[] args) throws Exception{

		if(!percorsoOutput.endsWith("/"))	percorsoOutput += "/";

		

		HashMap<String,Integer[]> regioni = new HashMap<String,Integer[]>();

		regioni.put("Piemonte", new Integer[] {1,2,3,4,5,6, 96,103});

		regioni.put("Valle d'Aosta", new Integer[] {7});

		regioni.put("Lombardia", new Integer[] {12,13,14,015,016,017,18,19,20,97,98,108});

		regioni.put("Trentino Alto Adige", new Integer[] {21,22});

		regioni.put("Veneto", new Integer[] {23,24,25,26,27,28,29});

		regioni.put("Friuli-Venezia Giulia", new Integer[] {30,31,32,93});

		regioni.put("Liguria", new Integer[] {8,9,10,11});

		regioni.put("Emilia Romagna", new Integer[] {33,34,35,36,37,38,39,40,99});

		regioni.put("Toscana", new Integer[] {45,46,47,48,49,50,51,52,53,100});

		regioni.put("Umbria", new Integer[] {54,55});

		regioni.put("Marche", new Integer[] {41,42,43,44,109});

		regioni.put("Lazio", new Integer[] {56,57,58,59,60});

		regioni.put("Abruzzo", new Integer[] {66,67,68,69});

		regioni.put("Molise", new Integer[] {70,94});

		regioni.put("Campania", new Integer[] {61,62,63,64,65});

		regioni.put("Puglia", new Integer[] {71,72,73,74,75,110});

		regioni.put("Basalicata", new Integer[] {76,77});

		regioni.put("Calabria", new Integer[] {78,79,80,101,102});	

		regioni.put("Sicilia", new Integer[] {81,82,83,84,85,86,87,88,89});

		regioni.put("Sardegna", new Integer[] {90,91,92,95,104,105,106,107});

		

		

		for(String r:regioni.keySet())

			for(int j=0; j<regioniInConsiderazione.length; j++)

				if(regioniInConsiderazione[j].equalsIgnoreCase(r))

					for(int i=0; i<regioni.get(r).length; i++ )

						provinceInConsiderazione.add(regioni.get(r)[i]);



		

		

		HashMap<String, Comune> comuni = new HashMap<String, Comune>();

		BufferedReader br = new BufferedReader(new FileReader(percorsoInput));

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

						

			String cp = r[2].trim();

			String cc = r[3].trim();

			String cod = Util.uniformeCode(cc, cp);			

			

//			Util.syso(i+" su 4876242 ");

			

			if(!comuni.containsKey(cod)){				///crea nuovo comune

				Comune c = new Comune(cc,cp,percorsoOutput);

//				if(WriteValue(r,ora,c))				///con questa riga potrei eliminare la funzione "centraConRegione", ma non so il perchè con questa riga il lancio del codice diventa troppo lungo.... 

				WriteValue(r,c);

				comuni.put(cod, c);

			}	

			else{										/// aggiorna comune esistente

				Comune c = comuni.get(cod);

				WriteValue(r,c);		

			}

		}

		

		i=0;

		for(String z:comuni.keySet()){

//			System.out.println(comuni.get(z).toString());

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

		System.out.println("numero dei comuni che centrano con le province in considerazione: "+i);

		System.out.println();

		

		i=0;

		int g =0;

		double vTot=0.0;
		
		HashMap<String, Comune> comuniOk = new HashMap<String, Comune>();

		for(String j:comuni.keySet()){

			g++;

			if(!comuni.get(j).getNome().equals("vuoto")){

				if(comuni.get(j).centraConRegione(provinceInConsiderazione)){

					comuniOk.put(j, comuni.get(j));

					i++;
					
					if(comuniOk.get(j).èInProvince(provinceInConsiderazione))

						vTot+=comuniOk.get(j).getFluxTot();

				}

			}

		}

		

		System.out.println("comuni analizzati: "+g);

		System.out.println("comuni eliminati: "+(g-i));

		System.out.println("inizio creazione Matrici OD delle regioni d'interesse");

		System.out.println("ComuniOk size="+comuniOk.size());

		System.out.println();

		// questa è la matrice dei flussi
		Write.MatrixOD(comuniOk, vTot, false, regioniInConsiderazione, percorsoOutput);
		// questa è la matrice dei tempi
		if(averageMOD)	Write.MatrixOD(comuniOk, vTot, true, regioniInConsiderazione, percorsoOutput);

	    

//		Write.MatrixOD(comuniOk, false, regioniInConsiderazione, percorsoInput);

	    

//		sout("");

//		sout(comuniOk.get("001-128").toString());

//		sout("PROVA: "+comuniOk.get("001-128").GetPartonoPer(1).get("001-146"));

	    

		br.close();

		

	}

	



	

	private static void WriteValue (String[] r, Comune c){

//		Scrive il valore di persone in movimento nella Hashmap Partono1, Partono2, Partono3 o Partono4 (a seconda dell'orario) nel comune passato.

//		Ritorna true se i valori passati coicidono col mezzo di trasporto prescelto, con le regioni prese in considerazione.

		int time=0; 

		String codper=Integer.parseInt(r[7].trim())+"-"+Integer.parseInt(r[8].trim());

		r[10]=r[10].trim();		

		for(int i=0; i<MezziDiTrasporto.length;i++){

			if(MezziDiTrasporto[i].equals(r[10])){

				for(int j=0; j<regioniInConsiderazione.length; j++){

					String [] s=codper.split("-");

					int cp=Integer.parseInt(s[0]);

					if(provinceInConsiderazione.contains(cp)){

						Double f = 0.0;

						if(r[14].trim().equals("ND")){

							if((int) Double.parseDouble(r[13].trim())==0){

								return;

							}

							f = Double.parseDouble(r[13].trim());

		//					syso(" per: "+codper+" in"+r[13].trim());

								

						}

						else{

							f = Double.parseDouble(r[14].trim());

		//					syso(" per: "+codper+" in"+r[14].trim());

						}

						if(r[11].trim().equals("+")){

							f/=4;

							c.SetPartonoPer(codper, f, 1, time);

							c.SetPartonoPer(codper, f, 2, time);

							c.SetPartonoPer(codper, f, 3, time);

							c.SetPartonoPer(codper, f, 4, time);

							return;

						}

						else{

							time=Integer.parseInt(r[12].trim());

							c.SetPartonoPer(codper, f, Integer.parseInt(r[11]), time);

							return;

						}

					}

				}

			}

		}

		return;

	}

}