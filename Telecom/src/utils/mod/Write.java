package utils.mod;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;

import cdraggregated.densityANDflows.flows.MODfromISTAT;

public class Write {
	
//	orig universal/HTMLWriter2	scribe il file html un segmento alla volta
	public static void simpleHTML(HashMap<String, Double> streets, HashMap<String, Double> streetsForecast) throws Exception {
		
		File file= new File("C:/BASE/ODMatrix/Mappa.html");
		file.createNewFile();				
		FileWriter fw = new FileWriter(file); 
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write("<html xmlns=http://www.w3.org/1999/xhtml xmlns:v=\"urn:schemas-microsoft-com:vml\">\n"
				+ "<head>\n"
				+ "<script src=\"https://maps.googleapis.com/maps/api/js?v=3.exp&sensor=false&libraries=visualization\"></script>\n"
				+ "<script>\n"
				+ "function initialize() {\n"
				+ "	var mapProp = {	center:new google.maps.LatLng(45.2895265,9.0451243), zoom:8, mapTypeId:google.maps.MapTypeId.ROADMAP};\n"
				+ "	var map=new google.maps.Map(document.getElementById(\"googleMap\"), mapProp);\n"
				+ "\n");
		double Max = calcMax(streets);
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
			
			Util.syso(streetsForecast.get(s)+"  #0000FF  "+n);
		}
		
		
		
		for(String s:streets.keySet()){
			n++;

			String c[]=s.split(":");
			String cp[]=c[0].split(",");
			String ca[]=c[1].split(",");
			bw.write("var p"+n+"=[new google.maps.LatLng("+cp[0]+", "+cp[1]+"), new google.maps.LatLng("+ca[0]+", "+ca[1]+")];\n"
	    	+ "	var Pt"+n+"=new google.maps.Polyline({"
	    	+ "	path:p"+n+",\n"
			+ "	strokeColor:\""+calcColor(streets.get(s),Max)+"\","
	    	+ "	strokeWeight:2"
	    	+ "	});"
	    	+ "	Pt"+n+".setMap(map);\n");
			
			Util.syso(streets.get(s)+"  "+calcColor(streets.get(s),Max)+"  "+n);
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
		System.out.println("Fine");
		bw.close();
	}
	
	
	
//  scrive il file html più leggero, tuttavia impiegando molto tempo
	public static void advancedHTML(HashMap<String, Double> streets) throws Exception {
		
//		HashMap<String, String> pointsID = new HashMap<String, String>();
		
		HashMap<String, Point> points = new HashMap<String, Point>();
//		String: cooord, -> key
		ArrayList<String> streetsOk = new ArrayList<String>();
				
		File file= new File("C:/BASE/ODMatrix/Mappa.html");
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
				polyline+="}];\nvar P"+n+"=new google.maps.Polyline({path:n"+n+",strokeColor:\""+calcColor(w,Max)+"\"});P"+n+".setMap(map);\n";
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
		if(flux<=10){
			color="#0000FF";
		}
		return color;
	}
	
	public static double calcMax(HashMap<String, Double> streets){

		double Max=0;
		
		for(String s:streets.keySet()){
			if(Max<streets.get(s))
				Max=streets.get(s);
		}
		return 5000;
		//return Max;
	}
	
	public static void Matrix(HashMap<String, Comune> comuni, Integer numeroFascieOrarie) throws Exception{
		for(int orario=1; orario<=numeroFascieOrarie; orario++){
			File file= new File("C:/BASE/ODMatrix/MatriceOD_-_"+MODfromISTAT.REGIONI[MODfromISTAT.regioniInConsiderazione[0]]+"_orario_uscita_-_"+orario+".csv");
			
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
		    		+ "-Metodo: ISTAT\n"
		    		+ "-- Zona interessata: "+MODfromISTAT.REGIONI[MODfromISTAT.regioniInConsiderazione[0]]+"\n"
		    		+ "-Fascia Oraria: "+orario+"\n");
		    for(int i=2; i<8; i++) bw.write("\n");
		    bw.write("--------------------------------------------------------------\n\n");
		    
		    
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
			    	if(comuni.get(d).GetPartonoPer(orario).containsKey(list.get(j))){
			    		bw.write("\t"+comuni.get(d).GetPartonoPer(orario).get(list.get(j)));
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
	
	
	public static String BooleanMatrix(Double tolleranza, String fileMOD) throws Exception{
		
		String name="MatriceBoolean.csv";
		File file= new File(name);
		file.createNewFile();
		FileWriter fw = new FileWriter(file);
	    BufferedWriter bw = new BufferedWriter(fw);
		String temp[]=fileMOD.split("_-_");
		String MOD=temp[1];
		BufferedReader br = new BufferedReader(new FileReader("MatriceOD_-_"+MOD+"_-_1.csv"));
		BufferedReader bs = new BufferedReader(new FileReader("MatriceOD_-_"+MOD+"_-_2.csv"));
		BufferedReader bt = new BufferedReader(new FileReader("MatriceOD_-_"+MOD+"_-_3.csv"));
		BufferedReader bu = new BufferedReader(new FileReader("MatriceOD_-_"+MOD+"_-_4.csv"));
		
		
		String liner =  br.readLine();
		String lines =	bs.readLine();
		String linet =	bt.readLine();
		String lineu =	bu.readLine();
		
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
						if(Integer.parseInt(r[i])>=tolleranza||Integer.parseInt(s[i])>=tolleranza||Integer.parseInt(t[i])>=tolleranza||Integer.parseInt(u[i])>=tolleranza){
							st+="\t1";
							op++;
						}
						else st+="\t0";
					}
				}
				bw.write(st+"\n");
			}
			count++;
			Util.syso("Creazione Matrice Booleana: riga "+count+" su "+r.length+" op numero: "+op);
		}
	    br.close();
	    bs.close();
	    bt.close();
	    bu.close();
	    bw.close();
	    return name;
	}
	

}
