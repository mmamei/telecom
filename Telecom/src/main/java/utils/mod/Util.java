package utils.mod;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import utils.mygraphhopper.MyGraphHopper;
import utils.mygraphhopper.WEdge;

import com.graphhopper.util.EdgeExplorer;
import com.graphhopper.util.EdgeIterator;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;


public class Util {
	
	
	
//	per riuscire salvare i dati riferiti agli edges
	public static void serialize(File F, Map<String, WEdge> wedges) throws Exception{
		System.out.println("start serialize");
		BufferedWriter bw= new BufferedWriter(new FileWriter(F));
		bw.write("WEDGE_ID\tADJ_NODE\tWEIGHT_B_node\tWEIGHT_A_node\n");
		for(String i:wedges.keySet())	{
			bw.write(i+"\t"+wedges.get(i).getAdjNode()+"\t"+wedges.get(i).getFlux(wedges.get(i).getAdjNode())+"\t"+wedges.get(i).getFlux(wedges.get(i).getBaseNode())+"\n");
		}
		System.out.println("serialized");
		bw.close();
	}
	
	public static HashMap<String, WEdge> deserializeWedges(File F, MyGraphHopper gh) throws Exception{
		System.out.println("start deserialize");
		HashMap<String, WEdge> wedges = new HashMap<String, WEdge>();
		EdgeExplorer ex = gh.getGraphHopperStorage().createEdgeExplorer();
		BufferedReader br= new BufferedReader(new FileReader(F));
		String l = br.readLine(); //skip header
		while((l =br.readLine())!=null){
			String[] s = l.split("\t");
			EdgeIterator ei = ex.setBaseNode(Integer.parseInt(s[1]));
			while(ei.next()){
				if(ei.getEdge()==Integer.parseInt(s[0]))
					wedges.put(s[0], new WEdge(ei, Double.parseDouble(s[2]), Double.parseDouble(s[3])));
			}
//			EdgeIteratorState eis = gh.getGraphHopperStorage().getEdgeIteratorState(Integer.parseInt(s[0]),Integer.parseInt(s[1]));
		}
		br.close();
		System.out.println("finish deserialize");
		return wedges;
	}
	

//	arrotonda alla quarta cifra decimale
	public static double round4(double d){
    	return(round(d, 4));
	}

	public static double round(double value, int numCifreDecimali) {
		double temp = Math.pow(10, numCifreDecimali);
		return Math.round(value * temp) / temp; 
	}

//	ordina double in ordine decrescente
	public static Double[] bubbleSort(Double [] a) {
		for(int i=0; i<a.length; i++) {
			boolean b=false;
			for(int j=0; j<a.length-1; j++) {
				if(a[j]<a[j+1]) {
					double k = a[j];
					a[j] = a[j+1];
					a[j+1] = k;
					b=true;
				}	
			}
			if(!b) return a; 
		}
		return a;
	}

//	usata per memorizzare durante l'ita i punti di partenza e di arrivo in un route generato random all'interno di una geometry
	public static class Pair<A, B> {
		  public final A first;
		  public final B second;
		  public Pair(final A first, final B second) {
		    this.first = first;
		    this.second = second;
		  }
	}
	
	
	
	public static String uniformeCode(String cc, String cp){
		if(cc.length()==1)	cc="00"+cc;
		if(cc.length()==2)	cc="0"+cc;
		if(cp.length()==1)	cp="00"+cp;
		if(cp.length()==2)	cp="0"+cp;
		return cp+"-"+cc;
	}
	
	public static String uniformeCode(String cod){
		if(cod.contains("-")){
			String c[]=cod.split("-");
			if(c.length==2){
				if(c[1].length()==1)	c[1]="00"+c[1];
				if(c[1].length()==2)	c[1]="0"+c[1];
				if(c[0].length()==1)	c[0]="00"+c[0];
				if(c[0].length()==2)	c[0]="0"+c[0];
				return c[0]+"-"+c[1];
			}
			else{
				System.out.println("Errore formato codice!");
				return cod;
			}
		}
		if(cod.length()==4)	cod="00"+cod;
		if(cod.length()==5)	cod="0"+cod;
		if(cod.length()==7)	cod=cod.substring(1);
		if(cod.length()==8)	cod=cod.substring(2);
		if(cod.length()==9)	cod=cod.substring(3);
		if(cod.length()==10)	cod=cod.substring(4);
		if(cod.length()!=6){
			System.out.println("Errore formato codice!");
			return cod;
		}
		return cod.substring(0, 3)+"-"+cod.substring(3);
	}
	
	public static double getStaticTrafficCost(String[] regioni, double tot, double ta){
		if(regioni[0].trim().equalsIgnoreCase("Lombardia")){
			System.out.println(tot);
//			fonte ifel(aci autoritratto 2014, istat 2015)
			double tassoDiMotorizzazione= 0.588;
			double pop = 9704151;
//			http://www.aci.it/fileadmin/documenti/studi_e_ricerche/dati_statistiche/Infrastrutture_stradali_in_Italia/Dotazione_di_infrastrutture_stradali_in_Italia.pdf
//			aci
			double KmTot = 11842;
			return ((tassoDiMotorizzazione*pop-tot)*ta)/(KmTot*1000);
		}
		return 0*ta;
	}
	
	public static Map<String, Double> reduceMap(String[] comToVisualize, String[] provToVisualize, Map<String, Double> streets, Map<String, Geometry> geoList){
		GeometryFactory gf = new GeometryFactory();
		for(int i=0; i<provToVisualize.length; i++){
			if(provToVisualize[i].equals(null))return streets;
			if(provToVisualize[i].length()>3||provToVisualize[i].length()==0) 
				provToVisualize[i] = Util.uniformeCode(provToVisualize[i]);
			if(provToVisualize[i].length()==2)	provToVisualize[i]="0"+provToVisualize[i];
			if(provToVisualize[i].length()==1)	provToVisualize[i]="00"+provToVisualize[i];
		}
		
		
		HashMap<String, Geometry> geoOk = new HashMap<String, Geometry>();
		
		for(int i=0; i<provToVisualize.length; i++){
			for(String s:geoList.keySet()){
				if(s.startsWith(provToVisualize[i]+"-")){
					geoOk.put(s, geoList.get(s));
				}
			}
		}
		for(int i=0; i<comToVisualize.length; i++){
			for(String s:geoList.keySet()){
				if(s.endsWith("-"+comToVisualize[i])){
					geoOk.put(s, geoList.get(s));
				}
			}
		}
		
//		System.out.println(geoOk.size());
		HashMap<String, Double> streetsOk = new HashMap<String, Double>();
		
		for(String k:streets.keySet()){
			String p[]=k.split(":");
			String pA[] = p[0].split(",");
			String pB[] = p[1].split(",");
			boolean c = false;
			Point a = gf.createPoint(new Coordinate(Double.parseDouble(pA[1]),Double.parseDouble(pA[0])));
			Point b = gf.createPoint(new Coordinate(Double.parseDouble(pB[1]),Double.parseDouble(pB[0])));
			for(String s:geoOk.keySet()){
				if(geoOk.get(s).contains(a)){
					c=true;
				}
				else{
					if(geoOk.get(s).contains(b)){
						c= true;
					}
				}
			}
			if(c) streetsOk.put(k, streets.get(k));
		}		
		return streetsOk;
	}
	
	public static ArrayList<Integer> getForbiddenEdge(String percorsoInput) throws Exception{
		ArrayList<Integer> forbiddenEdges = new ArrayList<Integer>();
		BufferedReader brd = new BufferedReader(new FileReader(percorsoInput+"temp/forbiddenEdgesList.txt"));
		String rs = "";
		if((rs= brd.readLine())!=null){
			String s[] = rs.split("-");
			for(int i=0;i<s.length;i++)
				forbiddenEdges.add(Integer.parseInt(s[i].trim()));
			brd.close();
		}
		return forbiddenEdges;
	}	

	public static double calcAverage(double[] values){
		double s=0.0;
		for(int i=0; i<values.length; i++)
			s+=values[i];
		return s/values.length;
	}

	public static double calcWeightedAverage(double[] values, double[] weight){
		double s=0.0;
		double t=0.0;
		if(weight.length!=values.length){
			System.out.println("ERRORE FORMATO MEDIA PESATA");
		}
		for(int i=0; i<values.length; i++){
			s+=values[i]*weight[i];
			t+=weight[i];
		}
		if(t==0.0)	return 0.0;
		return Util.round4(s/t);
	}

	

	public static double calcVariance(double[] values, double[] weight){

		double s = 0.0;

		double t = 0.0;

		double a = Util.calcWeightedAverage(values, weight);

		if(weight.length!=values.length)	System.out.println("ERRORE FORMATO VARIANZA");

		for(int i=0; i<values.length; i++)	t+=weight[i];

		if(t==0.0)	return 0.0;

		for(int i=0; i<values.length; i++)	s+=((values[i]-a)*(values[i]-a)*weight[i]/t);

		return Util.round4(s);

	}

	

	public static double calcVarianceUniforme(double[] weight){

//		tempo medio nelle fascie orarie		(valori di values[] ossia tDuration)

//		1 - fino a 15 minuti	=>	7.5 minuti

//		2 - da 16 a 30 minuti	=>	22 minuti

//		3 - da 31 a 60 minuti	=>	44,5 minuti

//		4 - oltre 60 minuti	=>	90 minuti

//		supponendo i tewmpi una V.A. uniformemente distribuita



		Double v[]={0.0, (15.0-0)*(15-0)/12, (30.0-16)*(30-16)/12, (60.0-31)*(60-31)/12, (120.0-60)*(120-60)/12};		

		

		if(weight.length!=v.length)	System.out.println("ERRORE FORMATO VARIANZA V.A. UNIFORME");

		

		double t=0.0;

		double s=0.0;

		for(int i=0; i<weight.length; i++)	t+=weight[i];

		if(t==0.0)	return 0.0;

		for(int i=0; i<weight.length; i++)	s+=weight[i]*v[i];

		return Util.round4(s/t);

	}

}


