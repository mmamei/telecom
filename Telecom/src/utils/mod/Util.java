package utils.mod;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

import utils.mygraphhopper.WEdge;

public class Util {

	public static void save(File file, Object o) {
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
			oos.writeObject(o);
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		try {
			oos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static Object restore(File file) {
		Object o = new Object();
		ObjectInputStream ois = null;
		try{
			ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)));
			o = ois.readObject();
			ois.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		try {
			ois.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				ois.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return o;
	}
	
//	per riuscire salvare i dati riferiti agli edges
	public static String serialize(HashMap<Integer, WEdge> edges ){
		System.out.println("start serialize");
		String toSave="";
		for(int i:edges.keySet())	{
			toSave+=edges.get(i).getId()+"\t"+edges.get(i).getAdjNode()+"\t"+edges.get(i).getBaseNode()+"\t"+edges.get(i).getWeight()+"\n";
		}
		System.out.println("serialized");
		return toSave;
	}
	
//	arrotonda alla quarta cifra decimale
	public static double round4(double d){
    	int r=((int)(d*1000000))%100;
    	d=d-r*0.000001;
    	if(r>50)d=d+0.0001;
    	d=((double)((int)(d*10000))/10000);
    	return(d);
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
	
	
	
	public static void syso(String s){
//		System.out.println(s);
	}
}
