package utils.mod;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;

import com.vividsolutions.jts.geom.Geometry;


public class Write {

	
	public static void MatrixOD(HashMap<String, Comune> comuni, double vTot, boolean averageMOD, String[] regioniInConsiderazione, String percorsoInput) throws Exception{

		String ric ="";

		for(int i=0; i<regioniInConsiderazione.length;i++){

			ric+=regioniInConsiderazione[i]+",\t";



		}

		for(int orario=1; orario<=4; orario++){

			if(regioniInConsiderazione.length!=0){

				String nomeFile = percorsoInput+"temp/MatriceOD_per";

				for(int i=0; i<regioniInConsiderazione.length; i++)

					nomeFile += "_-_"+regioniInConsiderazione[i];

				if(averageMOD){

					nomeFile+="_average_from_Istat";

				}

				System.out.println(nomeFile+"__orario_uscita_-_"+orario+".csv");

				File file = new File(nomeFile+"__orario_uscita_-_"+orario+".csv");

				

				ArrayList<String> list = new ArrayList<String>();

	//			contenente codice istat

				

				file.createNewFile();

				FileWriter fw = new FileWriter(file);

				BufferedWriter bw = new BufferedWriter(fw);

				int r=1;

			    int c=0;

			    int n=comuni.size()+1;

	//		    Header Writer

			    bw.write( "--------------------------------------------------------------\n"

			    		+ "Matrice Origine Destinazione\n"

			    		+ "--------------------------------------------------------------\n"

			    		+ "Parco veicolare in considerazione ="+vTot+"\n"

			    		+ "regioni in considerazione ="+ric+"\n"

			    		+ "Fascia Oraria: "+orario+"\n");

			    if(averageMOD){

			    	bw.write("Formato file MOD: Media_Pesata:Variance_Uniforme\n");

			    	bw.write("Origine dati: istat\n");

			    }

			    else{

			    	bw.write("Formato file MOD: n� persone\n");

			    	bw.write("Origine dati: istat\n");

			    }

			    

			    for(int i=0; i<4; i++){

			    	bw.write("\n");

			    }

			    bw.write("--------------------------------------------------------------\n"

			    		+ "\n");
			    double[] avDuration={0.0, 450.0, 1320.0, 2670.0, 5400.0};

			    

			    for(String s:comuni.keySet()){

			       	bw.write("\t"+comuni.get(s).getcod());

			    	list.add(comuni.get(s).getcod());

			    }

				

			    for(int i=0;i<list.size();i++){

			   		String d = list.get(i);

		    		bw.newLine();		    		r++;

					bw.write(comuni.get(d).getcod());

					c=1;

				    for(int j=0; j<list.size(); j++){

				    	if(comuni.get(d).getPartonoPer(orario).containsKey(list.get(j))){

//				    		System.out.println(comuni.get(d).getPartonoPer(orario).get(list.get(j))[0]+" "+comuni.get(d).getPartonoPer(orario).get(list.get(j))[1]+" "+comuni.get(d).getPartonoPer(orario).get(list.get(j))[2]+" "+comuni.get(d).getPartonoPer(orario).get(list.get(j))[3]+" "+comuni.get(d).getPartonoPer(orario).get(list.get(j))[4]);

				    		if(averageMOD)	bw.write("\t"+Util.round(Util.calcWeightedAverage(avDuration, comuni.get(d).getPartonoPer(orario).get(list.get(j))),0)+":"+Util.round(Util.calcVarianceUniforme(comuni.get(d).getPartonoPer(orario).get(list.get(j))),0));

//				    		if(averageMOD)	bw.write("\t"+Util.calcWeightedAverage(avDuration, comuni.get(d).getPartonoPer(orario).get(list.get(j)))+":"+comuni.get(d).getFlux(list.get(j)+":"+Util.calcVarianceUniforme(comuni.get(d).getPartonoPer(orario).get(list.get(j))), orario));

				    		else bw.write("\t"+comuni.get(d).getFlux(list.get(j), orario));

				    		System.out.println(Util.calcWeightedAverage(avDuration, comuni.get(d).getPartonoPer(orario).get(list.get(j)))+" -> "+comuni.get(d).getPartonoPer(orario).get(list.get(j))[0]+" "+comuni.get(d).getPartonoPer(orario).get(list.get(j))[1]+" "+comuni.get(d).getPartonoPer(orario).get(list.get(j))[2]+" "+comuni.get(d).getPartonoPer(orario).get(list.get(j))[3]+" "+comuni.get(d).getPartonoPer(orario).get(list.get(j))[4]);

				    		c++;

				    	}

				    	else{	

				    		bw.write("\t0.0");

				    		c++;

				    	}

				    }

				    if(c!=n){

				    	System.out.println("problema alla riga "+r);

				    }

			    }

	//		    System.out.println("righe: "+r);

	//		    System.out.println("colonne: "+c);

	//		    System.out.println("orario: "+orario);

			    fw.close();

			}

		}

	}

	

	public static void genericWrite(String percorso, String txt)throws Exception{

		System.out.println("oyu");

		File file= new File(percorso);

		file.createNewFile();				

		FileWriter fw = new FileWriter(file); 

		BufferedWriter bw = new BufferedWriter(fw);

		bw.write(txt);

		bw.close();

	}

	

	public static String BooleanMatrix(Double tolleranza, String fileMOD, String percorsoInput) throws Exception{

		

		String name = percorsoInput+"MatriceBoolean.csv";

		File file= new File(name);

		file.createNewFile();

		FileWriter fw = new FileWriter(file);

	    BufferedWriter bw = new BufferedWriter(fw);

		String temp[]=fileMOD.split("_-_");

		System.out.println(temp[1]);

		BufferedReader br = new BufferedReader(new FileReader(temp[0]+"_-_"+temp[1]+"_-_1.csv"));

		BufferedReader bs = new BufferedReader(new FileReader(temp[0]+"_-_"+temp[1]+"_-_2.csv"));

		BufferedReader bt = new BufferedReader(new FileReader(temp[0]+"_-_"+temp[1]+"_-_3.csv"));

		BufferedReader bu = new BufferedReader(new FileReader(temp[0]+"_-_"+temp[1]+"_-_4.csv"));

		

		String liner =  br.readLine();

		String lines =	bs.readLine();

		String linet =	bt.readLine();

		String lineu =	bu.readLine();

		for(int i=0; i<14; i++){

			liner = br.readLine();

			bs.readLine();

			bt.readLine();

			bu.readLine();

		}

		

		bw.write(liner+"\n");

		

		String st="";

		int count=0;

		int op=0;

		while((liner = br.readLine())!=null &&  (lines = bs.readLine())!=null &&  (linet = bt.readLine())!=null &&  (lineu = bu.readLine())!=null){

			

			String r[]=liner.split("\t");

			String s[]=lines.split("\t");

			String t[]=linet.split("\t");

			String u[]=lineu.split("\t");

			st=r[0];

			

			if(r.length==s.length && s.length==t.length && t.length==u.length){

				for(int i=1; i<r.length; i++){

					if(r[i].equals("0") && s[i].equals("0") && t[i].equals("0") && u[i].equals("0")){

						st+="\t0";

					}

					else {

						if(Double.parseDouble(r[i])>=tolleranza||Double.parseDouble(s[i])>=tolleranza||Double.parseDouble(t[i])>=tolleranza||Double.parseDouble(u[i])>=tolleranza){

							st+="\t1";

							op++;

						}

						else st+="\t0";

					}

				}

				bw.write(st+"\n");

			}

			count++;

//			System.out.println("Creazione Matrice Booleana: riga "+count+" su "+r.length+" op numero: "+op);

		}

	    br.close();

	    bs.close();

	    bt.close();

	    bu.close();

	    bw.close();

	    return name;

	}

public static void averageTime(HashMap<String,Double> time, String percorsoInput, int orario, Double tolleranza,String weight, Double[] ita, boolean polyMode,  Integer parameterForRandomAssigment, double ta)	throws Exception{

	    

//		tempo medio nelle fascie orarie

//		1 fino a 15 minuti	=>	7.5 minuti

//		2 da 16 a 30 minuti	=>	22 minuti

//		3 da 31 a 60 minuti	=>	44,5 minuti

//		4 oltre 60 minuti	=>	90 minuti

		

		File file= new File(percorsoInput+"temp/Averaage_time_-_Lombardia_-_"+orario+".csv");

		

		file.createNewFile();

		FileWriter fw = new FileWriter(file)	; 

		BufferedWriter bw = new BufferedWriter(fw);

		

		bw.write( "--------------------------------------------------------------\n"

				+ "Dati Average\n"

				+ "--------------------------------------------------------------\n"

				+ "Fascia Oraria: "+orario+"\n");

		bw.write("Formato file: Itinerario\tMedia pesata\n");

		bw.write("Origine dati: project_Graphhopper\n");

		bw.write("tolleranza: "+tolleranza+"\n");

		bw.write("weight: "+weight+", ta:"+ta+"\n");



		String sita="";

		for(int i=0; i<ita.length;i++){

			sita+=ita[i]+", ";

		}

		bw.write("ita: "+sita+"\n");

		bw.write("PolyMode: "+polyMode+"\n");

		if(polyMode)bw.write("ParameterForRandomAssigment: "+parameterForRandomAssigment+"\n");

		else bw.write("\n");

		bw.write("\n");

		

		bw.write("--------------------------------------------------------------\n"

				+ "\n");

		

		

//		for(String key:time.keySet()){

//			System.out.println(time.get(key)[0]+" "+time.get(key)[1]+" "+time.get(key)[2]+" "+time.get(key)[3]+" "+time.get(key)[4]+" ");

//		}

		

		for(String key:time.keySet()){

			bw.write(key+"\t"+time.get(key)+"\n");

//			System.out.println(key);

//			System.out.println(Util.calcWeightedAverage(tDuration, time.get(key))+":"+Util.calcVarianceUniforme(time.get(key)));

//			System.out.println();

		}

		bw.close();

	

	}

	public static void averageTime2(HashMap<String,Double> time, String fileMOD, String percorsoInput, int orario)	throws Exception{

	    
		
//		tempo medio nelle fascie orarie

//		1 fino a 15 minuti	=>	7.5 minuti

//		2 da 16 a 30 minuti	=>	22 minuti

//		3 da 31 a 60 minuti	=>	44,5 minuti

//		4 oltre 60 minuti	=>	90 minuti

			

		BufferedReader br = new BufferedReader(new FileReader(fileMOD));

		

		for(int i=0;i<14;i++)	br.readLine();	//skip header

		String s = br.readLine();

		String ci[] = s.split("\t");

		br.close();

		File file= new File(percorsoInput+"temp/Average_time_from_Project_-_Lombardia_-_"+orario+".csv");

		

		file.createNewFile();

		FileWriter fw = new FileWriter(file)	; 

		BufferedWriter bw = new BufferedWriter(fw);
	
		

		bw.write( "--------------------------------------------------------------\n"

				+ "Matrice Origine Destinazione\n"

				+ "--------------------------------------------------------------\n"

				+ "Fascia Oraria: "+orario+"\n");

		bw.write("Formato file MOD: Media_Pesata:varianza\n");

		bw.write("Origine dati: project Graphhopper\n");

		for(int i=0; i<6; i++){

			bw.write("\n");

		}

		bw.write("--------------------------------------------------------------\n"

				+ "\n");

				

		s="";

		for(int i=1; i<ci.length; i++)	s+="\t"+ci[i];

		bw.write(s+"\n");

		

//		for(String key:time.keySet()){

//			System.out.println(time.get(key)[0]+" "+time.get(key)[1]+" "+time.get(key)[2]+" "+time.get(key)[3]+" "+time.get(key)[4]+" ");

//		}

				

		for(int i=1; i<ci.length; i++){

			System.out.println("av writing "+i+"  su  "+ci.length);

			

			s=ci[i];

			for(int a=1; a<ci.length; a++){

				boolean b=false;

				for(String key:time.keySet()){

					if(key.equals(ci[i]+":"+ci[a])){

						s+="\t"+time.get(key);

//						System.out.println(key);

//						System.out.println(Util.calcWeightedAverage(tDuration, time.get(key))+":"+Util.calcVarianceUniforme(time.get(key)));

//						System.out.println();

						b=true;

					}

				}

				if(!b)	s+="\t0.0";

			}

			bw.write(s+"\n");

		}

		bw.close();

	}

	

	

}