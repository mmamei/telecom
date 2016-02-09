package utils.istatupdating;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;

//	questa classe serve ad aggiornare i codici istat di "ComuniCoord.txt" utilizzando il file "variazioni_amministrative_territoriali_dal_01011991.csv" 
//	contenente i nuovi codici adottati dall'istat. sono stati aggiornati solo i codici dei comuni che ci intaressano e i cod sono stati riportati in formato xxx-yyy
public class CodIstatCoord {
	static String MOD="Piem_e_Lomb_orario_uscita";
	
	public static void main(String[] args) throws Exception{
		
		HashMap<String, String> ghostcode = new HashMap<String, String>();
		BufferedReader bd = new BufferedReader(new FileReader("MatriceOD_-_"+MOD+"_-_4.csv"));
		File file= new File("Dati/Map/Coord/ComuniCoordUpdated.csv");
		file.createNewFile();
		FileWriter fw = new FileWriter(file);
		BufferedWriter bu = new BufferedWriter(fw);
		int t=0;
		int k=0;
		String lined = bd.readLine();
		String[] d=lined.split("\t");
		
		//d[i] con i!=0 contiene i codici cod formato xxx-yyy dei comuni usati in "MatriceOD_-"+MOD+"-_4.csv"
		
		String lineu="";
		for(int i=1; i<d.length; i++){
			boolean trovatoo=false;
			boolean trovato=false;
			
			//cerco nel ComuniCoord.csv se ci sono le coordinate relative al comune, se find non le trova (lasciando lineu vuota) è perchè bisogna aggiornare il cod del comune 
			
			lineu=find(d[i]);
			if(!lineu.isEmpty()){
				trovato=true;
			}
			
			//se li ho trovati li trascrivo in "ComuniCoordUpdated.csv"
			
			if(trovato){
				System.out.println(lineu);
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
				ghostcode.put(d[i], "fottiti: "+trovato+" "+trovatoo);
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
		BufferedReader bc = new BufferedReader(new FileReader("Dati/variazioni_amministrative_territoriali_dal_01011991.csv"));
		while((linec = bc.readLine())!=null&&!trovatoo) {
			String[] c=linec.split(";");
			//in ""variazioni_amministrative_territoriali_dal_01011991.csv" il cod comune è in formato xxxyyy, devo quindi trasformarlo in formato xxx-yyy
			if(c[3].length()==6){
				String cpy=c[3].substring(0, 3);
				String ccy=c[3].substring(3,6);
				System.out.println(d+" è uguale a "+cpy+"-"+ccy);
				if(d.equals(cpy+"-"+ccy)){
					System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
					///inserisci c[5] come codice giusto in lineu la riga con codice corretto presa da coordcom2
					BufferedReader bee = new BufferedReader(new FileReader("Dati/Map/Coord/ComuniCoord.txt"));
					while((linee = bee.readLine())!=null&&!trovatoo) {
						
						String[]e=linee.split(",");
						String cp=e[1];
						String cc=e[2];
						if(cc.length()==1)	cc="00"+cc;
						if(cc.length()==2)	cc="0"+cc;
						if(cp.length()==1)	cp="00"+cp;
						if(cp.length()==2)	cp="0"+cp;
						
						System.out.println("e ora "+cp+"-"+cc+" è uguale a "+c[5].substring(0, 3)+c[5].substring(3,6));
						
						if((cp+cc).equals(c[5].substring(0, 3)+c[5].substring(3,6))){
							lineu=e[0]+";"+cpy+";"+ccy+";"+e[3]+";"+e[4]+";"+e[5]+";"+e[6]+";"+e[7]+";"+e[8]+";"+e[9]+";"+e[10]+";"+e[11]+";"+e[12]+";"+e[13]+";"+e[14]+";"+e[15];
							
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
		BufferedReader bc = new BufferedReader(new FileReader("Dati/variazioni_amministrative_territoriali_dal_01011991.csv"));
		while((linec = bc.readLine())!=null&&!trovatoo) {
			String[] c=linec.split(";");
			if(c[5].length()==6){
				String cpy=c[5].substring(0, 3);
				String ccy=c[5].substring(3,6);
				System.out.println(d+" è uguale a "+cpy+"-"+ccy);
				if(d.equals(cpy+"-"+ccy)){
					System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
					///inserisci c[3] come codice giusto in lineu la riga con codice corretto presa da coordcom2
					BufferedReader bee = new BufferedReader(new FileReader("Dati/Map/Coord/ComuniCoord.txt"));
					while((linee = bee.readLine())!=null&&!trovatoo) {
						
						String[]e=linee.split(",");
						String cp=e[1];
						String cc=e[2];
						if(cc.length()==1)	cc="00"+cc;
						if(cc.length()==2)	cc="0"+cc;
						if(cp.length()==1)	cp="00"+cp;
						if(cp.length()==2)	cp="0"+cp;
						
						System.out.println("e ora "+cp+"-"+cc+" è uguale a "+c[3].substring(0, 3)+c[3].substring(3,6));
						
						if((cp+cc).equals(c[3].substring(0, 3)+c[3].substring(3,6))){
							lineu=e[0]+";"+cpy+";"+ccy+";"+e[3]+";"+e[4]+";"+e[5]+";"+e[6]+";"+e[7]+";"+e[8]+";"+e[9]+";"+e[10]+";"+e[11]+";"+e[12]+";"+e[13]+";"+e[14]+";"+e[15];
							
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
		}
		bc.close();
		return lineu;
	}
	
	
	
	
	
	
	public static String find(String d) throws Exception{
		
		String lineu="";
		String linee;
		BufferedReader be = new BufferedReader(new FileReader("Dati/Map/Coord/ComuniCoord.txt"));
		while((linee = be.readLine())!=null) {
			String[] e=linee.split(",");
			String cp=e[1];
			String cc=e[2];
			if(cc.length()==1)	cc="00"+cc;
			if(cc.length()==2)	cc="0"+cc;
			if(cp.length()==1)	cp="00"+cp;
			if(cp.length()==2)	cp="0"+cp;
			if(d.equals(cp+"-"+cc)){
				lineu=e[0]+";"+cp+";"+cc+";"+e[3]+";"+e[4]+";"+e[5]+";"+e[6]+";"+e[7]+";"+e[8]+";"+e[9]+";"+e[10]+";"+e[11]+";"+e[12]+";"+e[13]+";"+e[14]+";"+e[15];
			}
		}
		be.close();
		return lineu;
	}
}
