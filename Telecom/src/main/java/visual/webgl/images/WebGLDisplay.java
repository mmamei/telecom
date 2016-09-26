package visual.webgl.images;



import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

import org.rosuda.REngine.Rserve.RConnection;

public class WebGLDisplay {

	
	public static final String OUT_WEB_FOLDER = "G:/DATASET/WEBGL";
	public static final String IN_WEB_FOLDER = "G:/DATASET/WEBGL";
	
	public static void main(String[] args) throws Exception {
		run("Europa", IN_WEB_FOLDER+"/Dati.txt");
	}
	
	public static void run( String nome, String dati) throws Exception {
		
		//Arraylist con la prima parte del fine html
		ArrayList<String> inizio = new ArrayList<String>();
				
		//Arraylist con la parte finale del file html
		ArrayList<String> fine = new ArrayList<String>();
				
		//creo un oggetto della classe lista dati
		ListaDati ld = new ListaDati();
							
		//Creo la stringa di destionazione della foto
		String dest = OUT_WEB_FOLDER+"/textures/patterns/"+nome+".png";
				
		//Carico in lista dati i dati che vogliamo visualizzare leggendoli dal file dati.txt
		ld.leggiDati(dati); 
				
		//assegno inizo
		inizio = leggiFile(IN_WEB_FOLDER+"/html.txt");
				
		//assegno fine
		fine = leggiFile(IN_WEB_FOLDER+"/html2.txt");
				
		/*Le coordinate di partenza sono il massimo e minimp di latitudine e longitudine dei dati di
		 * input, dal punto più in basso tolgo 0.25 e aggiungo 0.245 a quello più in alto 
		 * per evitare che alcuni dati non vengano rappresentati*/
		double[] lonlatBbox = new double[]{
								ld.minlg()-((ld.maxlg() - ld.minlg())*0.9),
								ld.minl()-((ld.maxl() - ld.minl())*0.9), 
								ld.maxlg()+((ld.maxlg() - ld.minlg())*0.9),
								ld.maxl()+((ld.maxl() - ld.minl())*0.9)};
				  
		/*assegno all'array le nuove coordinate della mappa che sono quelle che useremo effettivamente 
		*alla funzione passo le coordinate di partenza e la destionazione della foto*/

		lonlatBbox = getMap(lonlatBbox,dest);
				
		scriviFile(OUT_WEB_FOLDER+"/"+nome+".html", inizio, fine, ld, lonlatBbox, nome );
	
	}	

	public static ArrayList<String> leggiFile(String file) throws Exception  {
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line;
	
		ArrayList<String> text = new ArrayList<String>();
		while((line = br.readLine())  != null) {	
			text.add(line);		
		}
		
		br.close();
		return text; 
	}
	
	
	public static void scriviFile(String file, ArrayList<String> inizio, ArrayList<String> fine,
			ListaDati ld, double[] lonlatBbox, String nome)  throws Exception  {
		 
		PrintWriter out = new PrintWriter(new FileWriter(file));
		
		for(int i = 0; i < inizio.size(); i++)
			if(inizio.get(i).contains("<title>"))
					out.println("<title>"+nome+"</title>");
			else	out.println(inizio.get(i));
		
		out.println("var lg1 = " + lonlatBbox[0] + ";" + " var l1 = "
		 + lonlatBbox[1] + ";" + " var lg2 = " + lonlatBbox[2] + ";"  + 
			" var l2 = "  + lonlatBbox[3] + ";" );
			
			out.println();
		
			out.println("var dati = [");
		
			for(int i=0; i<ld.size(); i++){
				out.println("{l:" + ld.getL(i) + ",	lg:" + ld.getLg(i) + ",	h:" + ld.geth(i) + " }," );
				}
			
			out.println(" ];");
		
		out.println();
		out.println("var texture = THREE.ImageUtils.loadTexture(" +  "'" + "textures/patterns/"+nome+".png" +  "'" + ")" + ";");
			
		for(int i = 0; i< fine.size(); i++)
			out.println(fine.get(i)); 
			
		out.close();
	}	
	
	//metodo che installa ggmap su R e ne permette la creazione della foto
	public static double[] getMap(double[] lonlatBbox, String file) {
		String code = null;
		try {
			file = file.replaceAll("_", "-");
			RConnection c = new RConnection();
			System.out.println("amap<-c("+lonlatBbox[0]+","+lonlatBbox[1]+","+lonlatBbox[2]+","+lonlatBbox[3]+")");
			c.assign("amap", lonlatBbox);
			

			// install.packages('ggmap');
			code = "library(ggmap);library(ggplot2);"
					+ "amap.map = get_map(location = amap, maptype='terrain', color='bw');"
					+ "ggmap(amap.map)+theme(axis.title = element_blank());"
					+ "ggsave('"+file+"',width=10, height=10);"
					+ "bbox <- attr(amap.map, 'bb');";

			System.out.println(code.replaceAll(";", ";\n"));
			c.eval(code);
			
			double ll_lat = c.eval("bbox$ll.lat").asDouble();
			double ll_lon = c.eval("bbox$ll.lon").asDouble();
			double ur_lat = c.eval("bbox$ur.lat").asDouble();
			double ur_lon = c.eval("bbox$ur.lon").asDouble();
			
			System.out.println(ll_lon+","+ll_lat+" - "+ur_lon+","+ur_lat);
			//Queste che bengono stampate sono le effettive 
			//coordinate che devono essere prese per i vertici della mappa
			
			c.close();
			
			return new double[]{ll_lon,ll_lat,ur_lon,ur_lat};
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
		}
	}

