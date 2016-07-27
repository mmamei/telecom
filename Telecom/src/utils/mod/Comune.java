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

	private HashMap<String, double[]> partono1 = new HashMap<String, double[]>();

	private HashMap<String, double[]> partono2 = new HashMap<String, double[]>();

	private HashMap<String, double[]> partono3 = new HashMap<String, double[]>();

	private HashMap<String, double[]> partono4 = new HashMap<String, double[]>();

	

//	contengono: String codIstat, Integer numero persone

	

	//	private HashMap<String, Integer> arrivano = new HashMap<String, Integer>();

	

	public Comune (String cc, String cp, String percorsoInput) throws Exception{
		
		BufferedReader bf = new BufferedReader(new FileReader("G:/DATASET/OD-ALBERTO-FRANCIA/MATRICE_PENDOLARISMO_2011/Pendolo 2011 documenti_utili/CLASSIFICAZIONI/Codici Comuni italiani_1 gennaio 2011.txt"));

		String linea;

		boolean t=false;

		while((linea = bf.readLine())!=null){

			String l[]=linea.split("\t");

			//System.out.println(Integer.parseInt(l[1].trim())+"<-- cp, cc -->"+Integer.parseInt(l[2].trim()));

			if(l[1].trim().equals(cp)&&l[2].trim().equals(cc)){

				this.nome=l[8];

				this.pop=Integer.parseInt(l[20].trim().replace(",",""));

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

			bf.close();

		}

	}





	public void SetPartonoPer(String cod, Double n, Integer ora, Integer time){

		cod = Util.uniformeCode(cod);		

		if(ora==1)

			if(!this.partono1.containsKey(cod)){

				double [] d = {0.0, 0.0, 0.0, 0.0, 0.0};

				d[time]+=n;

				this.partono1.put(cod, d);

			}

			else{

				this.partono1.get(cod)[time]+=n;

			}

		if(ora==2)

			if(!this.partono2.containsKey(cod)){

				double [] d = {0.0,0.0,0.0,0.0,0.0};

				d[time]+=n;

				this.partono2.put(cod, d);

			}

			else{

				this.partono2.get(cod)[time]+=n;

			}

		if(ora==3)

			if(!this.partono3.containsKey(cod)){

				double [] d = {0.0,0.0,0.0,0.0,0.0};

				d[time]+=n;

				this.partono3.put(cod, d);

			}

			else{

				this.partono3.get(cod)[time]+=n;

			}

		if(ora==4)

			if(!this.partono4.containsKey(cod)){

				double [] d = {0.0,0.0,0.0,0.0,0.0};

				d[time]+=n;

				this.partono4.put(cod, d);

			}

			else{

				this.partono4.get(cod)[time]+=n;

			}

	}

		

	public HashMap<String, double[]> getPartonoPer(int ora){

		if(ora==1)		return partono1;

		if(ora==2)		return partono2;

		if(ora==3)		return partono3;

		else			return partono4;

	}

	

	public double getFlux(String to, int ora){

		double d[]={0.0,0.0,0.0,0.0,0.0};

		if(ora==1)	if(partono1.containsKey(to))	d = partono1.get(to);

		if(ora==2)	if(partono2.containsKey(to))	d = partono2.get(to);

		if(ora==3)	if(partono3.containsKey(to))	d = partono3.get(to);

		if(ora==4)	if(partono4.containsKey(to))	d = partono4.get(to);

		double t=0.0;

		for(int i=0; i<5; i++)	t+=d[i];

		return t;
	}

//	restituisce la somma di tutti i pendolari in uscita da questo comune

	public double getFluxTot(){

		double t=0.0;

		for(String k:partono1.keySet())	for(int i=0; i< partono1.get(k).length; i++)	t += partono1.get(k)[i];		

		for(String k:partono2.keySet())	for(int i=0; i< partono2.get(k).length; i++)	t += partono2.get(k)[i];		

		for(String k:partono3.keySet())	for(int i=0; i< partono3.get(k).length; i++)	t += partono3.get(k)[i];		

		for(String k:partono4.keySet())	for(int i=0; i< partono4.get(k).length; i++)	t += partono4.get(k)[i];

		return t;

	}

	

//	public int getPartonoTot(int ora, int time){

//		int tot=0;

//		if(ora==1)

//			for(String s:this.partono1.keySet()){

//				tot+=this.partono1.get(s).getFluxTimeTot();

//			}

//		if(ora==2)

//			for(String s:this.partono2.keySet()){

//				tot+=this.partono2.get(s).getFluxTimeTot();

//			}

//		if(ora==3)

//			for(String s:this.partono3.keySet()){

//				tot+=this.partono3.get(s).getFluxTimeTot();

//			}

//		else

//			for(String s:this.partono4.keySet()){

//				tot+=this.partono4.get(s).getFluxTimeTot();

//			}

//		return tot;

//	}

	

	

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

	

	public String getcod(){

		return this.cod;

	}

	



	
//	true se il comune appartiene ad una delle provice date
	public boolean èInProvince(ArrayList<Integer> provinceInConsiderazione){

		if(provinceInConsiderazione.contains(Integer.parseInt(this.cp))){

			return true; 

		}

		return false;

	}	

	//ritorna true o se la regione è piemontese, o se cè qualcuno che parte da questo comune verso il piemonte, 2)provincia di torino

	public boolean centraConRegione(ArrayList<Integer> provinceInConsiderazione){

		if(provinceInConsiderazione.contains(Integer.parseInt(this.cp))){

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


