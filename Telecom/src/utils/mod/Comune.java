package utils.mod;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;

public class Comune {
	
	private String nome;
	private String cc;
	private String cod;
	private String cp;
	private Integer pop;
	private Integer regione; 	//occhio! è un numero! (Piemonte==1)
	private HashMap<String, Double> partono1 = new HashMap<String, Double>();
	private HashMap<String, Double> partono2 = new HashMap<String, Double>();
	private HashMap<String, Double> partono3 = new HashMap<String, Double>();
	private HashMap<String, Double> partono4 = new HashMap<String, Double>();
//	contengono: String codIstat, Integer numero persone
	
	//	private HashMap<String, Integer> arrivano = new HashMap<String, Integer>();
	
	public Comune (String cc, String cp) throws Exception{
		BufferedReader bf = new BufferedReader(new FileReader("G:/DATASET/OD-ALBERTO-FRANCIA/MATRICE_PENDOLARISMO_2011/Pendolo 2011 documenti_utili/CLASSIFICAZIONI/Codici Comuni italiani_1 gennaio 2011.txt"));
		String linea;
		boolean t=false;
		while((linea = bf.readLine())!=null){
			String l[]=linea.split("\t");
			//System.out.println(Integer.parseInt(l[1].trim())+"<-- cp, cc -->"+Integer.parseInt(l[2].trim()));
			if(l[1].trim().equals(cp)&&l[2].trim().equals(cc)){
				this.nome=l[8];
				this.pop=Integer.parseInt(l[20].trim().replace(",",""));
				this.regione=Integer.parseInt(l[0].trim());
				this.cp=cp;
				this.cc=cc;
				t=true;
				if(this.cc.length()==1)	this.cc="00"+cc;
				if(this.cc.length()==2)	this.cc="0"+cc;
				if(this.cp.length()==1)	this.cp="00"+cp;
				if(this.cp.length()==2)	this.cp="0"+cp;
				this.cod=this.cp+"-"+this.cc;
			}
			
		}
		if(!t){	
			this.nome="vuoto";
			this.pop=0;
			this.regione=0;
			bf.close();
		}
	}
	


	public void SetPartonoPer(String cod, Double n, Integer ora){
		cod = Util.uniformeCode(cod);
		if(ora==1)
			if(!this.partono1.containsKey(cod)){
				this.partono1.put(cod, n);
			}
			else{
				Double m=this.partono1.get(cod);
				this.partono1.put(cod, m+n);
			}
		if(ora==2)
			if(!this.partono2.containsKey(cod)){
				this.partono2.put(cod, n);
			}
			else{
				Double m=this.partono2.get(cod);
				this.partono2.put(cod, m+n);
			}
		if(ora==3)	
			if(!this.partono3.containsKey(cod)){
				this.partono3.put(cod, n);
			}
			else{
				Double m=this.partono3.get(cod);
				this.partono3.put(cod, m+n);
			}
		if(ora==4)
			if(!this.partono4.containsKey(cod)){
				this.partono4.put(cod, n);
			}
		else{
			Double m=this.partono4.get(cod);
			this.partono4.put(cod, m+n);
		}
	}
	
	
	public HashMap<String, Double> GetPartonoPer(int ora){
		if(ora==1)		return partono1;
		if(ora==2)		return partono2;
		if(ora==3)		return partono3;
		else			return partono4;
		
	}
	
	
	
	
	public int getPartonoTot(int ora){
		int tot=0;
		if(ora==1)
			for(String s:this.partono1.keySet()){
				tot+=this.partono1.get(s);
			}
		if(ora==2)
			for(String s:this.partono2.keySet()){
				tot+=this.partono2.get(s);
			}
		if(ora==3)
			for(String s:this.partono3.keySet()){
				tot+=this.partono3.get(s);
			}
		else
			for(String s:this.partono4.keySet()){
				tot+=this.partono4.get(s);
			}
		return tot;
	}
	
	
	
	
//	public void SetArrivanoDa(String cod, int n){
//		if(!this.arrivano.containsKey(cod)){
//			this.arrivano.put(cod, n);
//		}
//		else	if(!(this.arrivano.get(cod)==n))System.out.println("ci deve essere un errore!");
//	}
	
//	public HashMap<String, Integer> GetArrivanoDa(){
//		return arrivano;
//	}
	
//	public int getArrivanoTot(){
//		int tot=0;
//		for(String s:arrivano.keySet()){
//			tot+=arrivano.get(s);
//		}
//		return tot;
//	}
	
	public Integer getRegione(){
		return this.regione;
	}
	
	
	public String getcod(){
		return this.cod;
	}
	
	
	
	//ritorna true o se la regione è piemontese, o se cè qualcuno che parte da questo comune verso il piemonte, 2)provincia di torino
	public boolean centraConRegione(ArrayList<Integer> provinceInConsiderazione){
		if(provinceInConsiderazione.contains(this.cp)){
			return true; 
		}
		
		for(String d:this.partono1.keySet()){
			String [] c=d.split("-");
			int cp=Integer.parseInt(c[0]);
			if(provinceInConsiderazione.contains(cp)){
				return true;
			}
		}
		for(String d:this.partono2.keySet()){
			String [] c=d.split("-");
			int cp=Integer.parseInt(c[0]);
			if(provinceInConsiderazione.contains(cp)){
				return true;
			}
		}
		for(String d:this.partono3.keySet()){
			String [] c=d.split("-");
			int cp=Integer.parseInt(c[0]);
			if(provinceInConsiderazione.contains(cp)){			
				return true;
			}
		}
		for(String d:this.partono4.keySet()){
			String [] c=d.split("-");
			int cp=Integer.parseInt(c[0]);

			if(provinceInConsiderazione.contains(cp)){

				return true;
			}
		}
		
		return false;
	}
	
	
	
	
	
	public String getNome(){
		return this.nome;
	}
	
	
	public String toString(){
		return "Nome: "+nome+", Popolazione: "+pop+", lista partono: "+partono1.toString()+" - "+partono2.toString()+" - "+partono3.toString()+" - "+partono4.toString();
	}
}
