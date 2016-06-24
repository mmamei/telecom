package cdraggregated.densityANDflows.flows;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import utils.AddMap;
import cdraggregated.densityANDflows.ZoneConverter;

public class ODParser {
	
	/*
	--------------------------------------------------------------
	Matrice Origine Destinazione
	--------------------------------------------------------------
	- Metodo: HW
	- Zona interessata: odpiemonte
	- Istante di inizio: 01-06-2015
	- Soglia per italiani: 400
	- Applicato filtro privacy: 1
	- # di utenti su utenti del campione: 1000
	- Tipologia utenti: Tutti
	- Istante di fine: 01-07-2015
	- Soglia per stranieri: 400
	-------------------------------------------------------------- 
		*/
		

	public static Map<String, Object> parseHeader(String file) {
		Map<String, Object> tm = new HashMap<String, Object>();
		StringBuffer all = new StringBuffer();
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));

			br.readLine();
			br.readLine();
			br.readLine();
			String line;
			int headerRows = 0;
			while ((line = br.readLine()).trim().length() > 0) {
				line = line.replaceAll("_", "-");
				headerRows++;
				if (line.matches("-+"))
					break;

				String[] el = line.split(":");
				tm.put(el[0].replaceAll("-", "").trim(), el[1].trim());
				all.append("-" + el[1].trim().replaceAll(" ", "-"));
			}
			System.out.println("Processed " + (headerRows + 1) + " header rows");

			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		tm.put("name", all.substring(1));

		for (String k : tm.keySet())
			System.out.println(k + " --> " + tm.get(k));

		return tm;
	}
	
	
	
	public static AddMap parse(String file, ZoneConverter zc) {
		try {

			System.out.println(file);

			AddMap od = new AddMap();
			BufferedReader br = new BufferedReader(new FileReader(file));
			int headerRows = 0;
			String line;
			while ((line = br.readLine()).trim().length() > 0)
				headerRows++;
			System.out.println("Skipped " + (headerRows + 1) + " header rows");

			// line = br.readLine();

			line = br.readLine().trim(); // read first row

			if (line.startsWith("\t\t"))
				line = line.replaceFirst("\t", "");

			String[] cod = line.split("\t");

			int i = 0;
			while ((line = br.readLine()) != null) {

				if (line.startsWith("\t"))
					line = line.replaceFirst("\t", "");

				String[] codvals = line.split("\t");
				if (!codvals[0].equals(cod[i]))
					System.err.println("error");
				for (int j = 1; j < codvals.length; j++) {
					String a = zc == null ? cod[i] : zc.convert(cod[i]);
					String b = zc == null ? cod[j - 1] : zc.convert(cod[j - 1]);
					double x = Double.parseDouble(codvals[j]);
					if (x > 0)
						od.add(a + "-" + b, x);
				}

				i++;
			}
			br.close();
			return od;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	
	
	public static void save(AddMap od, Map<String, Object> header, String outfile) {
		try {
			PrintWriter out = new PrintWriter(new FileWriter(outfile));
			
			// write header
			out.println("--------------------------------------------------------------");
			out.println("Matrice Origine Destinazione");
			out.println("--------------------------------------------------------------");
			for(String k: header.keySet()) {
				if(!k.equals("name"))
					out.println("- "+k+": "+header.get(k));
			}
			out.println("--------------------------------------------------------------");
			out.println();
			
			
			// get all zones
			Set<String> zones = new HashSet<>();
			for(String k: od.keySet()) {
				String[] z1z2 = k.split("-");
				zones.add(z1z2[0]);
				zones.add(z1z2[1]);
			}
			
			// write matrix
			for(String zj: zones)
				out.print("\t"+zj);
			out.println();
			
			for(String zi: zones) {
				out.print(zi);
				for(String zj: zones) {
					Double v = od.get(zi + "-" + zj);
					if(v == null) v = 0.0;
					out.print("\t"+v);
				}
				out.println();
			}
			
			out.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
	public static void main(String[] args) {
		String inFile = "C:/BASE/ODMatrix/ODMatrixHW_file_pls_piem_file_pls_piem_01-06-2015-01-07-2015_minH_0_maxH_25_ABOVE_8limit_20000_cellXHour_odpiemonte/od-2-3-16.csv";
		String ouFile = "C:/BASE/ODMatrix/ODMatrixHW_file_pls_piem_file_pls_piem_01-06-2015-01-07-2015_minH_0_maxH_25_ABOVE_8limit_20000_cellXHour_odpiemonte/od-2-3-16-bis.csv";
		Map<String, Object> header = parseHeader(inFile);
		AddMap od = parse(inFile,null);
		double sum = 0;
		for(double x:od.values())
			sum+=x;
		System.out.println(sum);
		
		
		save(od,header,ouFile);
		
		od = parse(ouFile,null);
		sum = 0;
		for(double x:od.values())
			sum+=x;
		System.out.println(sum);
		
	}
	
	
	
}
