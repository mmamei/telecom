package cdraggregated.densityANDflows.flows;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;

import utils.mod.Route;

public class ForExcel {
	public static void main(String[] args) throws Exception{
		String MODAvIstat="D:/CODE/Project/temp/MatriceOD_per_-_Lombardia_average_from_Istat__orario_uscita_-_1.csv";
		String MODAvProject="D:/CODE/Project/temp/Averaage_time_from_Project_ok_-_Lombardia_-_1.csv";
		String percorsoInput="D:/CODE/Project";
		String[] regioniInConsiderazione={"Lombardia"};

		String o[]=MODAvIstat.split("_-_");

		int orario = Integer.parseInt(o[o.length-1].replace(".csv", ""));

		

		if(regioniInConsiderazione.length!=0){

			String nomeFile = percorsoInput+"/Excel/info_per";

			for(int i=0; i<regioniInConsiderazione.length; i++)

				nomeFile += "_-_"+regioniInConsiderazione[i];

			nomeFile+="_Average";

			System.out.println((nomeFile+"_"+orario+".csv").replace("/", "\\"));

			File file = new File(nomeFile+"_"+orario+".csv");

			file.createNewFile();

			FileWriter fw = new FileWriter(file);

			BufferedWriter bw = new BufferedWriter(fw);		

			
			BufferedReader bri = new BufferedReader(new FileReader(new File(MODAvIstat)));

			BufferedReader brp = new BufferedReader(new FileReader(new File(MODAvProject)));

			for(int i=0; i<6; i++)	brp.readLine();	//skip header MODAvProject

//		    Header Writer

			bw.write( "--------------------------------------------------------------\n"

					+ "File per Excel\n"

					+ "--------------------------------------------------------------\n"

					+ "Fascia Oraria: "+orario+"\n");

			bw.write("Formato file : codIstatDa:codIstatA\tMedia_Pesata + invio\n");

			bw.write("Origine dati: istat + Project_Graphhopper\n");

			bw.write(brp.readLine()+"\n");

			bw.write(brp.readLine()+"\n");

			bw.write(brp.readLine()+"\n");

			bw.write(brp.readLine()+"\n");

			bw.write(brp.readLine()+"\n");

			bw.write(brp.readLine()+"\n");

			bw.write(brp.readLine()+"\n");

			bw.write(brp.readLine()+"\n");
			
			

			HashMap<String, Route> hm = new HashMap<String, Route>();

	

			for(int i=0; i<14; i++)	bri.readLine();	//skip header

			String line = bri.readLine();

			String ci[] = line.split("\t");

			

//			leggo i dati istat e li memorizzo in hm

			for(int i=1; i<ci.length; i++){

//				System.out.println(i+" su "+ci.length);

				line = bri.readLine();

				String v[]= line.split("\t");

				for(int j=1; j<v.length; j++){					

					if(!v[j].equals("0.0")){						

						Route r = new Route(ci[i]+":"+ci[j]);

						String mv[]=v[j].split(":");

						r.setVarianzaIstat(Double.parseDouble(mv[1]));

						r.setMediaIstat(Double.parseDouble(mv[0]));

						hm.put(ci[i]+":"+ci[j], r);

					}

				}

			}

			bri.close();

			

//			ora leggo i dati sviluppati in project con graphhopper

			for(int i=0; i<14; i++)	brp.readLine();	//skip header

			line = brp.readLine();

			ci = line.split("\t");

			System.out.println("ci[1] = "+ci[1]);



//			leggo i dati e li memorizzo in hm:

//			codIstatDa:codIstatA, Route

			int l=0;

			while((line = brp.readLine())!=null){				

				String v[]= line.split("\t");

//				for(int j=1; j<v.length; j++){

//					if(!v[j].equals("0.0")&&hm.containsKey(ci[i]+":"+ci[j])){

//						Route r = hm.get(ci[i]+":"+ci[j]);

//						System.out.println(ci[i]+":"+ci[j]+" = "+v[j]+" "+Double.parseDouble(v[j]));

//						Double d=Double.parseDouble(v[j]);

//						r.setMediaProject(d);

//						hm.replace(ci[i]+":"+ci[j], r);

//					}

//				}
				if(hm.containsKey(v[0]))	hm.get(v[0]).setMediaProject(Double.parseDouble(v[1]));
				else	hm.put(v[0], new Route(v[0]).setMediaProject(Double.parseDouble(v[1])).setMediaIstat(-1));
				l++;

			}

			System.out.println(hm.size());

			Route[] routes = new Route[hm.size()];

//			ora aggiorno in  routes[]

			int c=0;

			for(String k:hm.keySet()){

				routes[c]=hm.get(k);

				c++;

			}

			System.out.println("-> "+l);



//			ora devo ordinare routes in ordine cresciente di media Istat

			for(int i=0; i<routes.length; i++){

				boolean b = false;

				for(int j=0; j<routes.length-1; j++){

					if(routes[j].getMediaIstat()>routes[j+1].getMediaIstat()){

						Route r = routes[j];

						routes[j] = routes[j+1];

						routes[j+1] = r;

						b=true;

					}

					else{

						if(routes[j].getMediaIstat()==routes[j+1].getMediaIstat()){

							if(routes[j].getMediaProject()>routes[j+1].getMediaProject()){

								Route r = routes[j];

								routes[j] = routes[j+1];

								routes[j+1] = r;

								b=true;

							}

						}

					}

				}

				if(!b) i=routes.length; 

			}

			System.out.println("-> "+routes[879].getMediaProject());



			

			//trascrivo

			for(int i=0; i<routes.length; i++){

//				bw.write(routes[i].getId()+"\t"+routes[i].getMediaIstat()+"\t"+routes[i].getVarianzaIstat()+"\t"

//						+ "\t"+routes[i].getMediaProject()+"\t"+routes[i].getVarianzaProject()+"\n");

				

				if(routes[i].getMediaProject()!=0.0)

					bw.write(routes[i].getId()+"\t"+(routes[i].getMediaIstat()+"\t"

						+ "\t"+routes[i].getMediaProject()).replace(".", ",")+"\n");

			}

			System.out.println("FINE");

			brp.close();

			bw.close();

		}

	}

}

