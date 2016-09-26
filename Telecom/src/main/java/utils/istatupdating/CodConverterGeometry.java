

package utils.istatupdating;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;

import utils.mod.Util;

//	questa classe serve ad aggiornare i codici istat di "ComuniCoord.txt" utilizzando il file "variazioni_amministrative_territoriali_dal_01011991.csv" 
//	contenente i nuovi codici adottati dall'istat. sono stati aggiornati solo i codici dei comuni che ci intaressano e i cod sono stati riportati in formato xxx-yyy
public class CodConverterGeometry {
	static String MOD="C:/BASE/Francia/temp/MatriceOD_per_-_Lombardia__orario_uscita_-_1.csv";
	
public static void main(String[] args) throws Exception{
		
		HashMap<String, String> ghostcode = new HashMap<String, String>();
		BufferedReader bd = new BufferedReader(new FileReader(MOD));
		File file= new File("G:/DATASET/OD-ALBERTO-FRANCIA/Geometry/comuniUpdated.csv");
		file.createNewFile();
		BufferedWriter bu = new BufferedWriter(new FileWriter(file));
		int t=0;
		int k=0;
		for(int i=0;i<14;i++)	bd.readLine();	//skip header
		String lined = bd.readLine();
		String[] d=lined.split("\t");
		
		//d[i] con i!=0 contiene i codici cod formato xxx-yyy dei comuni usati in MOD
		
		String lineu="";
		for(int i=1; i<d.length; i++){
			boolean trovatoo=false;
			boolean trovato=false;
			
			//cerco nel ComuniCoord.csv se ci sono le coordinate relative al comune, se find non le trova (lasciando lineu vuota) � perch� bisogna aggiornare il cod del comune 
			
			lineu=find(d[i]);
			if(!lineu.isEmpty()){
				trovato=true;
			}
			
			//se li ho trovati li trascrivo in "ComuniCoordUpdated.csv"
			
			if(trovato){
//				System.out.println(lineu);
				bu.write(lineu);
				bu.newLine();
			}
			
			//se non li trovo allora devo cercarli in ""variazioni_amministrative_territoriali_dal_01011991.csv"
			
			else{
//				
				lineu=findvar1(d[i]);
				if(!lineu.isEmpty()){
					trovatoo=true;
					t++;
				}
				else{
					lineu=findvar2(d[i]);
					if(!lineu.isEmpty()){
						trovatoo=true;
						t++;
					}
					
				}
			}
			if(trovatoo){
				bu.write(lineu);
				bu.newLine();
			}
			
			if(!trovato&&!trovatoo){
				System.out.println("Codice non corrispondeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee");
				ghostcode.put(d[i], trovato+" "+trovatoo);
				k++;
			}
		}
		bd.close();
		bu.close();
		System.out.println("Comuni aggiornati: "+t);
		System.out.println("Coomuni fantasma!!:"+k);
		System.out.println();
		for(String y:ghostcode.keySet()){
			System.out.println(y);
		}
		System.out.println("Comuni aggiornati: "+t);
	}
	
	
	
	public static String findvar1(String d) throws Exception{
		String lineu="";
		String linee;
		String linec;
		boolean trovatoo=false;
		BufferedReader bc = new BufferedReader(new FileReader("G:/DATASET/OD-ALBERTO-FRANCIA/variazioni_amministrative_territoriali_dal_01011991.csv"));
		while((linec = bc.readLine())!=null&&!trovatoo) {
			String[] c=linec.split(";");
			//in "variazioni_amministrative_territoriali_dal_01011991.csv" il cod comune � in formato xxxyyy, devo quindi trasformarlo in formato xxx-yyy
			String codVA1 = Util.uniformeCode(c[3]);
			System.out.println(d+" � uguale a "+codVA1);
			if(d.equals(codVA1)){
				System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
				///inserisci c[5] come codice giusto in lineu la riga con codice corretto presa da coordcom2
				BufferedReader bee = new BufferedReader(new FileReader("G:/DATASET/OD-ALBERTO-FRANCIA/Geometry/comuni2014.csv"));
				while((linee = bee.readLine())!=null&&!trovatoo) {
					String[] e=linee.split("\t");
					String codE = Util.uniformeCode(e[7]);
					String codC = Util.uniformeCode(c[5]);
				
//					System.out.println("e ora "+cp+"-"+cc+" � uguale a "+c[5].substring(0, 3)+c[5].substring(3,6));
					System.out.println("e ora "+codE+" � uguale a "+codC);
					
					if((codE).equals(codC)){
						lineu=e[0]+"\t"+e[1]+"\t"+e[2]+"\t"+e[3]+"\t"+e[4]+"\t"+e[5]+"\t"+e[6]+"\t"+codVA1+"\t"+e[8]+"\t"+e[9];
						
						trovatoo=true;
						System.out.println();
						System.out.println();
						System.out.println("FATTOOOOOOOOOOOOOOOOOOOOoooooooooooOOOOOOOOOOOOOOOOOOOOOOOOOOoOOOOOOoOooOoOooOooooooooooooooooooooooooooooooooooooooooooooo");
						System.out.println();
						System.out.println();
					}
				}
				bee.close();
			}	
		}
		bc.close();
		return lineu;
	}
	
	
	public static double converter(String si, String sm, String ss){
		if(Double.parseDouble(si.trim())>0){
			double d=Double.parseDouble(si.trim())+((Double.parseDouble(ss.trim())/60)+Double.parseDouble(sm.trim())/60);
			return d;
		}
		else{
			System.out.println("MI SA CHE NON SEI IN ITALIA.....");
			double d=Double.parseDouble(si.trim())-((Double.parseDouble(ss.trim())/60)+Double.parseDouble(sm.trim())/60);
			return d;
		}
		
	}
	
	
	public static String findvar2(String d) throws Exception{
		String lineu="";
		String linee;
		String linec;
		boolean trovatoo=false;
		BufferedReader bc = new BufferedReader(new FileReader("G:/DATASET/OD-ALBERTO-FRANCIA/variazioni_amministrative_territoriali_dal_01011991.csv"));
		while((linec = bc.readLine())!=null && !trovatoo) {
			String[] c = linec.split(";");
			String codVA1 = Util.uniformeCode(c[5]);
			System.out.println(d+" � uguale a "+codVA1);
			if(d.equals(codVA1)){
				System.out.println("222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222");
				///inserisci c[3] come codice giusto in lineu la riga con codice corretto presa da coordcom2
				BufferedReader bee = new BufferedReader(new FileReader("G:/DATASET/OD-ALBERTO-FRANCIA/Geometry/comuni2014.csv"));
				while((linee = bee.readLine())!=null&&!trovatoo) {
					
					String[] e=linee.split("\t");
					String cod = Util.uniformeCode(e[7]);
					String codC = Util.uniformeCode(c[3]);
					
					System.out.println("e ora "+cod+" � uguale a "+codC);
					
					if((cod).equals(codC)){
						lineu=e[0]+"\t"+e[1]+"\t"+e[2]+"\t"+e[3]+"\t"+e[4]+"\t"+e[5]+"\t"+e[6]+"\t"+codVA1+"\t"+e[8]+"\t"+e[9];
						
						trovatoo=true;
						System.out.println();
						System.out.println();
						System.out.println("FATTOOOOOOOOOOOOOOOOOOOOoooooooooooOOOOOOOOOOOOOOOOOOOOOOOOOOoOOOOOOoOooOoOooOooooooooooooooooooooooooooooooooooooooooooooo");
						System.out.println();
						System.out.println();
					}
				}
				bee.close();
			}
		}
		bc.close();
		return lineu;
	}
	
	
	
	
	
	
	public static String find(String d) throws Exception{
		
		String lineu="";
		String linee;
		BufferedReader be = new BufferedReader(new FileReader("G:/DATASET/OD-ALBERTO-FRANCIA/Geometry/comuni2014.csv"));
		be.readLine(); //skip header
		while((linee = be.readLine())!=null) {
			
			String[] e=linee.split("\t");
			String cod = Util.uniformeCode(e[7]);

			if(d.equals(cod)){
				lineu=e[0]+"\t"+e[1]+"\t"+e[2]+"\t"+e[3]+"\t"+e[4]+"\t"+e[5]+"\t"+e[6]+"\t"+cod+"\t"+e[8]+"\t"+e[9];
			}
		}
		be.close();
		return lineu;
	}
}
