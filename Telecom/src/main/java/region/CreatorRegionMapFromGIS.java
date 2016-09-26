package region;


import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.html.HTMLEditorKit;

import utils.Config;
import utils.CopyAndSerializationUtils;
import utils.GeomUtils;
import utils.Logger;

import com.vividsolutions.jts.geom.Geometry;



public class CreatorRegionMapFromGIS {
	

	
	public static void main(String[] args) throws Exception {
		
		//new CreatorRegionMapFromGISGUI();
		
		
		//processWTK("ivorycoast_regioni","G:/DATASET/GEO/ivorycoast/ivorycoast_regioni.csv",Config.getInstance().base_folder+"/RegionMap/ivorycoast_regioni.ser",new int[]{5});
		//processWTK("ivorycoast_province","G:/DATASET/GEO/ivorycoast/ivorycoast_province.csv",Config.getInstance().base_folder+"/RegionMap/ivorycoast_province.ser",new int[]{7});
		//processWTK("ivorycoast_comuni","G:/DATASET/GEO/ivorycoast/ivorycoast_comuni.csv",Config.getInstance().base_folder+"/RegionMap/ivorycoast_comuni.ser",new int[]{9});
		//processWTK("ivorycoast_subpref","G:/DATASET/GEO/ivorycoast/ivorycoast_subpref.csv",Config.getInstance().base_folder+"/RegionMap/ivorycoast_subpref.ser",new int[]{11});
		processWTK("ivorycoast_grid","G:/DATASET/GEO/ivorycoast/ivorycoast_grid.csv",Config.getInstance().base_folder+"/RegionMap/ivorycoast_grid.ser",new int[]{17});
		
		
		//processWTK("senegal_regioni","G:/DATASET/GEO/senegal/senegal_regioni.csv",Config.getInstance().base_folder+"/RegionMap/senegal_regioni.ser",new int[]{5});
		//processWTK("senegal_province","G:/DATASET/GEO/senegal/senegal_province.csv",Config.getInstance().base_folder+"/RegionMap/senegal_province.ser",new int[]{7});
		//processWTK("senegalt_comuni","G:/DATASET/GEO/senegal/senegal_comuni.csv",Config.getInstance().base_folder+"/RegionMap/senegal_comuni.ser",new int[]{9});
		//processWTK("senegal_subpref","G:/DATASET/GEO/senegal/senegal_subpref.csv",Config.getInstance().base_folder+"/RegionMap/senegal_subpref.ser",new int[]{11});
		processWTK("senegal_grid","G:/DATASET/GEO/senegal/senegal_grid.csv",Config.getInstance().base_folder+"/RegionMap/senegal_grid.ser",new int[]{17});
		
		/****************************************************************************************************************/
		/*											MAIN TELECOM DATA CHALLENGE 2015 									*/
		
		
		/*
		//WKT,id,
		String[] cities = new String[]{"venezia","milano","torino","napoli","roma","palermo","bari"};
		for(String city: cities) {
			String name = "tic-"+city+"-gird";
			String input_file = Config.getInstance().dataset_folder+"/GEO/ti-challenge/"+name+".csv";
			String output_obj_file=Config.getInstance().base_folder+"/RegionMap/"+name+".ser";
			processWTK(name,input_file,output_obj_file,new int[]{1});
		}
		*/
		
		//WKT	CAP	
		//String input_file = Config.getInstance().dataset_folder+"/TI-CHALLENGE-2015/ZIP/CAPS2.csv";
		//String output_obj_file=Config.getInstance().base_folder+"/RegionMap/caps.ser";
		//processWTK("CAPS",input_file,output_obj_file,new int[]{1});
		
		/*
		String input_file = Config.getInstance().dataset_folder+"/GEO/census-sections/veneto-census-sections.csv";
		String output_obj_file=Config.getInstance().base_folder+"/RegionMap/veneto-census-sections.ser";
		processWTK("veneto-census-sections",input_file,output_obj_file);
		*/
		
		
		// WKT	COD_REG	COD_PRO	PROVINCIA	SIGLA	POP2001
		//String input_file = Config.getInstance().dataset_folder+"/GEO/prov2011.csv";
		//String output_obj_file=Config.getInstance().base_folder+"/RegionMap/prov2011.ser";
		//processWTK("prov2011",input_file,output_obj_file,new int[]{3});
		
		
		// WKT	COD_REG	REGIONE	POP2001
		//String input_file = Config.getInstance().dataset_folder+"/GEO/regioni.csv";
		//String output_obj_file=Config.getInstance().base_folder+"/RegionMap/regioni.ser";
		//processWTK("regioni",input_file,output_obj_file,new int[]{2});
		
		
		//WKT	PRO_COM	COD_REG	COD_PRO	NOME_COM	POP2001
		//String input_file = Config.getInstance().dataset_folder+"/GEO/comuni2014.csv";
		//String output_obj_file=Config.getInstance().base_folder+"/RegionMap/comuni2014.ser";
		//processWTK("comuni2014",input_file,output_obj_file,new int[]{1});
		
		
		//WKT	OBJECTID	COD_REG	COD_PRO	SHAPE_Leng	SHAPE_Area	CODICE_C_1	COD_ISTA_1	PRO_COM__1	NOME_COM_2
		//String input_file = Config.getInstance().dataset_folder+"/GEO/comuni2014.csv";
		//String output_obj_file=Config.getInstance().base_folder+"/RegionMap/comuni2014.ser";
		//processWTK("comuni2014",input_file,output_obj_file,new int[]{8});
		
		//WKT	OBJECTID	COD_REG	COD_PRO	COD_ISTAT	PRO_COM	NOME	SHAPE_Leng	SHAPE_Area
		//String input_file = Config.getInstance().dataset_folder+"/GEO/comuni2012.csv";
		//String output_obj_file=Config.getInstance().base_folder+"/RegionMap/comuni2012.ser";
		//processWTK("comuni2012",input_file,output_obj_file,new int[]{5});
		
		/****************************************************************************************************************/
		
		/****************************************************************************************************************/
		/*											MAIN PROGETTO MATRICI OD 2015 										
		
		//WKT	OBJECTID	COD_REG	COD_PRO	COD_ISTAT	PRO_COM	NOME	SHAPE_Leng	SHAPE_Area
		String input_file = Config.getInstance().dataset_folder+"/GEO/telecom-2015-od/piemonte/piemonte.csv";
		String output_obj_file=Config.getInstance().base_folder+"/RegionMap/piemoente-od-2015.ser";
		processWTK("piemonte-od-2015",input_file,output_obj_file,new int[]{4});
		
		//WKT	OBJECTID	ID_ZONA	Shape_Leng	Shape_Area	INDEMM_02_	INDEMM_021
		input_file = Config.getInstance().dataset_folder+"/GEO/telecom-2015-od/lombardia/lombardia.csv";
		output_obj_file=Config.getInstance().base_folder+"/RegionMap/lombardia-od-2015.ser";
		processWTK("lombardia-od-2015",input_file,output_obj_file,new int[]{2});
		
		/****************************************************************************************************************/
		
		
		
		/*
		String name = "torino_circoscrizioni_geo";
		String input_file = Config.getInstance().dataset_folder+"/GEO/"+name+".csv";
		String output_obj_file=Config.getInstance().base_folder+"/RegionMap/"+name+".ser";
		processWTK(name,input_file,output_obj_file);
		*/
		
		
		
		/*
		String[] cities = new String[]{"Venezia","Firenze","Torino","Lecce"};
		for(String city: cities) {
			System.out.println("Processing "+city+" ...");
			String input_file = Config.getInstance().dataset_folder+"/GEO/"+city.toLowerCase()+"/"+city.toLowerCase()+"_tourist_area.csv";
			String outputname = city+"TouristArea";
			String output_obj_file=Config.getInstance().base_folder+"/RegionMap/"+outputname+".ser";
			processKML(outputname,input_file,output_obj_file);
		}
		*/
		
		
		Logger.logln("Done!");
	}
	
	
	
	
	public static RegionMap processWTK(String name, String input_file, String output_obj_file, int[] name_indexes) throws Exception {
			
		
		RegionMap rm = new RegionMap(name);
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(input_file), "UTF8"));
		String line;
		br.readLine(); // skip header
		while((line=br.readLine())!=null) {
			String[] e = line.split("\t");
			String wtk_shape = e[0];
			wtk_shape = wtk_shape.replaceAll("\"MULTIPOLYGON \\(\\(\\(", "");
			wtk_shape = wtk_shape.replaceAll("\\)\\)\\)\"", "");
			
			
			String n = "";
			for(int i: name_indexes)
				n = n + e[i];
					
			
			String[] polys = wtk_shape.split("\\),\\(");
			
			
			Geometry max_g = null; // polygon with max area
			for(int i=0; i<polys.length;i++) {
				String poly = polys[i].replaceAll("\\)|\\(", "");
				Geometry g = GeomUtils.openGis2Geom(poly);
				if(max_g == null || g.getArea() > max_g.getArea()) 
					max_g = g;
				//rm.add(new Region(polys.length > 1 ? n+"_"+i : n,g)); // this is to add all the regions in the multipolygon with a progressive counter
			}
			rm.add(new Region(n,max_g)); // this is to add the region (in the multipolygon) with the max area
		}
		
		br.close();
		
		rm.printKML();
		CopyAndSerializationUtils.save(new File(output_obj_file), rm);
		
		return rm;
	}
	
	
	public static RegionMap processKMLLine(String name, String input_file, String output_obj_file) throws Exception {
		
		RegionMap rm = new RegionMap(name);
		
		BufferedReader br = new BufferedReader(new FileReader(input_file));
		String line;
		while((line=br.readLine())!=null) {
			String[] e = line.split(";");
			String n = e[0];
			rm.add(new Region(n,e[1]));
		}
		
		br.close();
		
		rm.printKML();
		CopyAndSerializationUtils.save(new File(output_obj_file), rm);
		
		return rm;
	}
	
}


/************************************************************************/


class CreatorRegionMapFromGISGUI {
	
	
	JFrame win;
	JButton input_file_button;
	JTextPane input_text_pane;
	JLabel jl_wtk;
	JTextField jtf_wtk;
	JLabel jl_id;
	JTextField jtf_id;
	JLabel jl_title;
	JTextField jtf_title;
	JButton output_file_button;
	JTextPane output_text_pane;
	JButton go;
	File input_file = null; 
	File output_obj_file = null; 
	
	
	
	public CreatorRegionMapFromGISGUI() {
		win = new JFrame("CreatorRegionMapFromGIS");
		Container c = win.getContentPane();
		c.setLayout(new FlowLayout());

				
		input_file_button = new JButton("Select Input SHP CSV File"); input_file_button.setPreferredSize(new Dimension(300, 50));
		input_text_pane = new JTextPane(); input_text_pane.setPreferredSize(new Dimension(1050, 200));
		
		jl_wtk = new JLabel("wtk index"); jl_wtk.setPreferredSize(new Dimension(80, 20));jl_wtk.setHorizontalAlignment(SwingConstants.RIGHT);
		jtf_wtk = new JTextField("0"); jtf_wtk.setPreferredSize(new Dimension(40, 20));
		
		jl_id = new JLabel("id index"); jl_id.setPreferredSize(new Dimension(80, 20));jl_id.setHorizontalAlignment(SwingConstants.RIGHT);
		jtf_id = new JTextField(); jtf_id.setPreferredSize(new Dimension(40, 20));
		
		
		jl_title = new JLabel("map title"); jl_title.setPreferredSize(new Dimension(80, 20));jl_title.setHorizontalAlignment(SwingConstants.RIGHT);
		jtf_title = new JTextField(""); jtf_title.setPreferredSize(new Dimension(500, 20));
		
		output_file_button = new JButton("Select Output SER File"); output_file_button.setPreferredSize(new Dimension(300, 50));
		output_text_pane = new JTextPane(); output_text_pane.setPreferredSize(new Dimension(1050, 40));
		
		go = new JButton("Go!"); go.setPreferredSize(new Dimension(60, 50)); go.setBackground(Color.RED);
		
		
		input_file_button.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				try {
					
					input_file = null;
		        	input_text_pane.setText("");
					
					JFileChooser fileChooser = new JFileChooser(new File(Config.getInstance().dataset_folder+"/GEO"));
					int n = fileChooser.showOpenDialog(win);
			        if (n == JFileChooser.APPROVE_OPTION) {
			        	input_file = fileChooser.getSelectedFile();
			        	input_text_pane.setEditorKit(new HTMLEditorKit());
			        	StringBuffer sb = new StringBuffer();
			        	sb.append("<b>SELECTED FILE: "+input_file.getAbsolutePath()+"</b><br>");
			        	sb.append("<table border='1'>");
			        	sb.append("<tr><td>0</td><td>1</td><td>2</td><td>3</td><td>4</td><td>5</td><td>6</td><td>7</td><td>8</td><td>9</td></tr>");

			            BufferedReader read = new BufferedReader(new FileReader(input_file));
			            int cont = 0;
			            String line;
			            while((line = read.readLine())!=null) {
			              int nchar = Math.min(line.length(), 100);
			              int start = nchar < line.length() ? line.length()-nchar : 0;
			              line = (start>0?"...":"")+ line.substring(start,line.length())+"\n";
			              line = line.replaceAll("\t", "</td><td>");
			              sb.append("<tr><td>"+line+"</td></tr>");
			              cont++;
			              if(cont > 10) break;
			            }
			            read.close();
			            sb.append("</table>");
			            input_text_pane.setText(sb.toString());
			            
			        }
			        else {
			        	input_file = null;
			        	input_text_pane.setText("");
			        }
				} catch(Exception exc) {
					exc.printStackTrace();
				}
			}
		});
		
		 
		output_file_button.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				try {
					JFileChooser fileChooser = new JFileChooser(new File(Config.getInstance().base_folder+"/RegionMap"));
					int n = fileChooser.showOpenDialog(win);
			        if (n == JFileChooser.APPROVE_OPTION) {
			        	output_obj_file = fileChooser.getSelectedFile();
			        	StyledDocument doc = output_text_pane.getStyledDocument();
			        	
			        	SimpleAttributeSet keyWord = new SimpleAttributeSet();
			        	StyleConstants.setForeground(keyWord, Color.RED);
			        	StyleConstants.setBold(keyWord, true);
			        	
			        	doc.insertString(0, "SELECTED FILE: "+output_obj_file.getAbsolutePath()+"\n",keyWord);
			        }
			        else {
			        	output_obj_file = null;
			        	output_text_pane.setText("");
			        }
				} catch(Exception exc) {
					exc.printStackTrace();
				}
			}
		});
		
		
		go.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				
				System.out.println("input = "+input_file.getAbsolutePath());
				System.out.println("output = "+output_obj_file.getAbsolutePath());
				System.out.println("wtk index = "+jtf_wtk.getText());
				
				int id = Integer.parseInt(jtf_id.getText());
				String title = jtf_title.getText();
				
				System.out.println("id index = "+id);
				System.out.println("title = "+title);
				
				try {
					CreatorRegionMapFromGIS.processWTK(title,input_file.getAbsolutePath(),output_obj_file.getAbsolutePath(),new int[]{id});
					
					input_file = null;
					input_text_pane.setText("");
					output_obj_file = null;
					output_text_pane.setText("");
					jtf_wtk.setText("0");
					jtf_id.setText("");
					jtf_title.setText("");
					
				} catch (Exception exc) {
					exc.printStackTrace();
					
					input_file = null;
					input_text_pane.setText(exc.toString());
					output_obj_file = null;
					output_text_pane.setText(exc.toString());
					jtf_wtk.setText("0");
					jtf_id.setText("");
					jtf_title.setText("");
					
				}
				
				
			}
		});
		
		
		c.add(input_file_button);
		c.add(input_text_pane);
		c.add(jl_wtk);
		c.add(jtf_wtk);
		c.add(jl_id);
		c.add(jtf_id);
		c.add(jl_title);
		c.add(jtf_title);
		c.add(output_file_button);
		c.add(output_text_pane);
		c.add(go);
		
		win.setResizable(false);
		win.setSize(1100,500);
		win.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		win.setVisible(true);
	}
}


