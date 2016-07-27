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

	

//	orig universal/HTMLWriter2	scribe il file html un segmento alla volta

	public static void simpleHTML(HashMap<String, Double> streets, HashMap<String, Double> streetsForecast, HashMap<String, Geometry> geoList, String[] comToVisualize, String[] provToVisualize, String percorsoOutput) throws Exception {
		streets = Util.reduceMap(comToVisualize, provToVisualize, streets, geoList);
		System.out.println("streetsOk size: "+streets.size());
		File file= new File(percorsoOutput+"/Mappa.html");
		file.createNewFile();
		FileWriter fw = new FileWriter(file); 
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write("<html xmlns=http://www.w3.org/1999/xhtml xmlns:v=\"urn:schemas-microsoft-com:vml\">\n"
				+ "<head>\n"
				+ "<script\n"
				+ "src=\"http://maps.googleapis.com/maps/api/js\">\n"
				+ "</script>\n"
				+ "<script src=\"http://maps.googleapis.com/maps/api/js?key=AIzaSyBPaPK8TLhtlXovEJkzNhgLyDY9av2UVps\"></script>\n"
				+ "<script>\n"
				+ "function initialize() {\n"
				+ "	var mapProp = {	center:new google.maps.LatLng(45.4664539,9.170471), zoom:11, mapTypeId:google.maps.MapTypeId.ROADMAP};\n"
				+ "	var map=new google.maps.Map(document.getElementById(\"googleMap\"), mapProp);\n"
				+ "\n");
//		double Max = calcMax(streets);
//		Max=1;
		int n=0;
		
		for(String s:streetsForecast.keySet()){
			n++;
			String c[]=s.split(":");
			String cp[]=c[0].split(",");
			String ca[]=c[1].split(",");
			bw.write("var p"+n+"=[new google.maps.LatLng("+cp[0]+", "+cp[1]+"), new google.maps.LatLng("+ca[0]+", "+ca[1]+")];\n"
	    	+ "	var Pt"+n+"=new google.maps.Polyline({"
	    	+ "	path:p"+n+",\n"
			+ "	strokeColor:\"#0000FF\","
	    	+ "	strokeWeight:6,"
	    	+ " strokeOpacity:0.5"
	    	+ "	});"
	    	+ "	Pt"+n+".setMap(map);\n");
			
			System.out.println(streetsForecast.get(s)+"  #0000FF  "+n);
		}
		
		
		
		for(String s:streets.keySet()){
				n++;
				String c[]=s.split(":");
				String cp[]=c[0].split(",");
				String ca[]=c[1].split(",");
				bw.write("var p"+n+"=[new google.maps.LatLng("+cp[0]+", "+cp[1]+"), new google.maps.LatLng("+ca[0]+", "+ca[1]+")];\n"
		    	+ "	var Pt"+n+"=new google.maps.Polyline({"
		    	+ "	path:p"+n+",\n"
				+ "	strokeColor:\""+calcColorGoogle(streets.get(s))+"\","
		    	+ "	strokeWeight:3"
		    	+ "	});"
		    	+ "	Pt"+n+".setMap(map);\n");
				
	//			System.out.println(streets.get(s)+"  "+calcColor(streets.get(s),Max)+"  "+n);
		}
		bw.write(""
				+ "	}\n"
				+ "google.maps.event.addDomListener(window, 'load', initialize);\n"
				+ "</script>\n"
				+ "</head>\n"
				+ "<body>\n"
				+ "<div id=\"googleMap\" style=\"width:1280px;height:631px;\"></div>\n"
				+ "</body>\n"
				+ "</html>\n");
		System.out.println("Fine HTML");
		bw.close();
	}

	

	

	

//  scrive il file html più leggero, tuttavia impiegando molto tempo

	public static void advancedHTML(HashMap<String, Double> streets, HashMap<String, Double> streetsForecast, HashMap<String, Geometry> geoList, String[] comToVisualize, String[] provToVisualize, String percorsoOutput) throws Exception {

		

//		HashMap<String, String> pointsID = new HashMap<String, String>();

		

		HashMap<String, Point> points = new HashMap<String, Point>();

//		String: cooord, -> key

		

		streets = Util.reduceMap(comToVisualize, provToVisualize, streets, geoList);

		

		ArrayList<String> streetsOk = new ArrayList<String>();

				

		File file= new File(percorsoOutput+"Mappa.html");

		file.createNewFile();				

		FileWriter fw = new FileWriter(file); 

		BufferedWriter bw = new BufferedWriter(fw);

		

		int n=0;

		double Max=calcMax(streets); 

	

//		Max=4000;

		

		bw.write("<html xmlns=http://www.w3.org/1999/xhtml xmlns:v=\"urn:schemas-microsoft-com:vml\">\n"

				+ "<head>\n"

				+ "<script\n"

				+ "src=\"http://maps.googleapis.com/maps/api/js\">\n"

				+ "</script>\n"

				+ "<script src=\"http://maps.googleapis.com/maps/api/js?key=AIzaSyBPaPK8TLhtlXovEJkzNhgLyDY9av2UVps\"></script>\n"

				+ "<script>\n"

				+ "function initialize() {\n"

				+ "	var mapProp = {	center:new google.maps.LatLng(45.2895265,9.0451243), zoom:8, mapTypeId:google.maps.MapTypeId.ROADMAP};\n"

				+ "	var map=new google.maps.Map(document.getElementById(\"googleMap\"), mapProp);\n"

				+ "\n");

		

		for(String s:streetsForecast.keySet()){

			n++;



			String c[]=s.split(":");

			String cp[]=c[0].split(",");

			String ca[]=c[1].split(",");

			bw.write("var p"+n+"=[new google.maps.LatLng("+cp[0]+", "+cp[1]+"), new google.maps.LatLng("+ca[0]+", "+ca[1]+")];\n"

	    	+ "	var Pt"+n+"=new google.maps.Polyline({"

	    	+ "	path:p"+n+",\n"

			+ "	strokeColor:\"#0000FF\","

	    	+ "	strokeWeight:6,"

	    	+ " strokeOpacity:0.5"

	    	+ "	});"

	    	+ "	Pt"+n+".setMap(map);\n");

			

//			System.out.println(streetsForecast.get(s)+"  #0000FF  "+n);

		}

		

		for(String s:streets.keySet()){

			String c[]=s.split(":");



			if(points.containsKey(c[0])){

				points.get(c[0]).setAdj(c[1], streets.get(s));

			}

			else{

				Point p = new Point(c[0]);

				p.setAdj(c[1], streets.get(s));

				points.put(c[0], p);

			}

			

			if(points.containsKey(c[1]))

				points.get(c[1]).setAdj(c[0], streets.get(s));

			else{

				Point p = new Point(c[1]);

				p.setAdj(c[0], streets.get(s));

				points.put(c[1], p);

			}

		}

		System.out.println("points_ generated");

		

		int i=0;



		for(String s:streets.keySet()){

			i++;

			System.out.println(i);

			if(!streetsOk.contains(s)){

				n++;

				String polyline = "var n"+n+"=[{lat:";

				String c[] = s.split(":");

				

//				System.out.println("c[0] "+c[0]);

//				System.out.println("c[1] "+c[1]);

				

				Point p = points.get(c[1]);

				Point prev = points.get(c[0]);

				Double w = streets.get(s);

				polyline+=c[0].replace(",", ",lng:")+"},{lat:"+c[1].replace(",", ",lng:");

				streetsOk.add(s);

//				System.out.println(p.getAdj(w, prev.coord));

				String next = p.getAdj(w, prev.coord);

//				System.out.println("OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO");

				while(!next.equals("no")){



//					System.out.println("prev "+prev.coord);

//					System.out.println("p "+p.coord);

//					System.out.println("next "+next);

//					System.out.println();



					polyline+="},{lat:"+next.replace(",", ",lng:");

					

//					if(streets.containsKey(p+":"+next)) streetsOk.add(p+":"+next);

//					else streetsOk.add(next+":"+p);

					streetsOk.add(p+":"+next);

					streetsOk.add(next+":"+p);

					

					prev = p;

					p = points.get(next);

					next = p.getAdj(w, prev.coord);



//					System.out.println("prev "+prev.coord);

//					System.out.println("p "+p.coord);

//					System.out.println("next "+next);

//					System.out.println("");

				

//					evita loop infiniti in teoria

					if(streetsOk.contains(p+":"+next))	next="no";

					

//					System.out.println(p.getAdj(w, prev.coord));

				}

				polyline+="}];\nvar P"+n+"=new google.maps.Polyline({path:n"+n+",strokeColor:\""+calcColorGoogle(w)+"\"});P"+n+".setMap(map);\n";

//				System.out.println(polyline);

				bw.write(polyline);

			}

//			break;

		}



		bw.write(""

				+ "	}\n"

				+ "google.maps.event.addDomListener(window, 'load', initialize);\n"

				+ "</script>\n"

				+ "</head>\n"

				+ "<body>\n"

				+ "<div id=\"googleMap\" style=\"width:1300px;height:600px;\"></div>\n"

				+ "</body>\n"

				+ "</html>\n");

		bw.close();

		System.out.println("fine");		

	} 

	

	public HashMap<String, Double> findInStreets(HashMap<String, Double> streets, String t, Double w){

		HashMap<String, Double> f = new HashMap<String, Double>();

		for(String s:streets.keySet()){

			if(streets.get(s)==w){

				String u[] = s.split(":");

				if(u[0].equals(t))

					f.put(s, w);

				if(u[1].equals(t))

					f.put(s, w);

			}

		}

		return f;

	}

	

	public static class Point{

		private String coord;

		private HashMap<String, Double> adj = new HashMap<String, Double>();

		

		public Point(String coord){

			this.coord = coord;

		}

//		ritorna la coordinate di un punto collegato a questo, che non sia prev e con peso pari a w

		public String getAdj(double w, String prev) {

			if(!adj.containsValue(w)) return "no";

			for(String s:adj.keySet()){

				if(adj.get(s).equals(w)&&!(s.equals(prev)))

					return s;

			}

			return "no";

		}



		public void setAdj(String s, Double w) {

			adj.put(s, w);

		}		

	}

	public static String calcColorGoogle(Double voc){
//		GREEN = 84CA50
//		ORANGE = F07D02
//		RED = E60000
//		DARK RED = 9E1313
//		Verde: non ci sono ritardi dovuti al traffico.
		String color="#84CA50";
//		Arancione: volume di traffico medio.
		if(voc>0.5)	color="#F07D02";
//		Rosso: ritardi dovuti al traffico.
		if(voc>1)	color="#E60000";
//		Più scuro è il rosso, più ridotta è la velocità del traffico sulla strada.
		if(voc>1.2)	color="#9E1313";
		return color;
		}
	public static String calcColor(Double flux, Double Max){
		String color="";
		double dec=(flux/Max)*(255*2);
		if(dec>255){
			dec=dec/2;
			color=Integer.toHexString((int) (256-dec));
			if(color.length()<2) color="0"+color;
			if(color.length()>2) color="FF";
			color="#FF"+color+"00";
		}
		else {
			dec=dec/2;
			color=Integer.toHexString((int)(dec));
			if(color.length()<2) color="0"+color;
			color="#"+color+"FF00";
		}	
		return color;
	}
	

	public static double calcMax(HashMap<String, Double> streets){



		double Max=0;

		

		for(String s:streets.keySet()){

			if(Max<streets.get(s))

				Max=streets.get(s);

		}

		return Max;

	}

	
	
	
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

			    	bw.write("Formato file MOD: n° persone\n");

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