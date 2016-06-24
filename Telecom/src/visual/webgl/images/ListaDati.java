package visual.webgl.images;

import java.io.BufferedReader;
import java.io.FileReader;


import java.util.ArrayList;

public class ListaDati {
	
	ArrayList<Dati> ld; 
	
	public ListaDati(){
		ld = new ArrayList<Dati>(); 
	}

	public void leggiDati(String file){
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;
			while((line=br.readLine())!=null) {
				
				String[] e = line.split(",");
				
				Dati d = new Dati(Double.parseDouble(e[0].trim()),Double.parseDouble(e[1].trim()), Double.parseDouble(e[2].trim()));
				
				ld.add(d); 	
				
			}
			br.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	
	public int size(){
		return ld.size(); 
	}
	
	public double getL(int i){
		return ld.get(i).l; 
	}
	
	public double getLg(int i){
		return ld.get(i).lg; 
	}
	
	public double geth(int i){
		return ld.get(i).h; 
	}
	
	public double maxl(){
	
		double max = ld.get(0).l; 
		for(int i=0; i < ld.size(); i++)
		if (max < ld.get(i).l) max =  ld.get(i).l; 
		return max; 
	}
	
	public double minl(){
		
		double min = ld.get(0).l; 
		for(int i=0; i < ld.size(); i++)
		if (min > ld.get(i).l) min =  ld.get(i).l; 
		
		return min; 
	}	
	
	public double maxlg(){
		
		double max = ld.get(0).lg; 
		for(int i=0; i < ld.size(); i++)
		if (max < ld.get(i).lg) max =  ld.get(i).lg; 
		return max; 
	}
	
	public double minlg(){
		
		double min = ld.get(0).lg; 
		for(int i=0; i < ld.size(); i++)
		if (min > ld.get(i).lg) min =  ld.get(i).lg; 
		
		return min; 
	}	
	
}