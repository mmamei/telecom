package test;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

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

import utils.Config;

public class TestGUI {
	
	
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
	File output_file = null; 
	
	
	
	public TestGUI() {
		win = new JFrame("Control Frame");
		Container c = win.getContentPane();
		c.setLayout(new FlowLayout());

				
		input_file_button = new JButton("Select Input SHP CSV File"); input_file_button.setPreferredSize(new Dimension(300, 50));
		input_text_pane = new JTextPane(); input_text_pane.setPreferredSize(new Dimension(1000, 200));
		
		jl_wtk = new JLabel("wtk index"); jl_wtk.setPreferredSize(new Dimension(80, 20));jl_wtk.setHorizontalAlignment(SwingConstants.RIGHT);
		jtf_wtk = new JTextField("0"); jtf_wtk.setPreferredSize(new Dimension(40, 20));
		
		jl_id = new JLabel("id index"); jl_id.setPreferredSize(new Dimension(80, 20));jl_id.setHorizontalAlignment(SwingConstants.RIGHT);
		jtf_id = new JTextField(); jtf_id.setPreferredSize(new Dimension(40, 20));
		
		
		jl_title = new JLabel("map title"); jl_title.setPreferredSize(new Dimension(80, 20));jl_title.setHorizontalAlignment(SwingConstants.RIGHT);
		jtf_title = new JTextField(""); jtf_title.setPreferredSize(new Dimension(500, 20));
		
		output_file_button = new JButton("Select Output File"); output_file_button.setPreferredSize(new Dimension(300, 50));
		output_text_pane = new JTextPane(); output_text_pane.setPreferredSize(new Dimension(1000, 40));
		
		go = new JButton("Go!"); go.setPreferredSize(new Dimension(60, 50)); go.setBackground(Color.RED);
		
		
		input_file_button.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				try {
					JFileChooser fileChooser = new JFileChooser(new File(Config.getInstance().dataset_folder));
					int n = fileChooser.showOpenDialog(win);
			        if (n == JFileChooser.APPROVE_OPTION) {
			        	input_file = fileChooser.getSelectedFile();
			        	StyledDocument doc = input_text_pane.getStyledDocument();
			        	
			        	
			        	SimpleAttributeSet keyWord = new SimpleAttributeSet();
			        	StyleConstants.setForeground(keyWord, Color.RED);
			        	StyleConstants.setBold(keyWord, true);
			        	
			        	doc.insertString(0, "SELECTED FILE: "+input_file.getAbsolutePath()+"\n",keyWord);
			            BufferedReader read = new BufferedReader(new FileReader(input_file));
			            int cont = 0;
			            String line;
			            while((line = read.readLine())!=null) {
			              line = line.substring(0,Math.min(line.length(), 150))+"\n";
			              doc.insertString(doc.getLength(),line,null);
			              cont++;
			              if(cont > 10) break;
			            }
			            read.close();
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
					JFileChooser fileChooser = new JFileChooser(new File(Config.getInstance().dataset_folder));
					int n = fileChooser.showOpenDialog(win);
			        if (n == JFileChooser.APPROVE_OPTION) {
			        	output_file = fileChooser.getSelectedFile();
			        	StyledDocument doc = output_text_pane.getStyledDocument();
			        	
			        	SimpleAttributeSet keyWord = new SimpleAttributeSet();
			        	StyleConstants.setForeground(keyWord, Color.RED);
			        	StyleConstants.setBold(keyWord, true);
			        	
			        	doc.insertString(0, "SELECTED FILE: "+output_file.getAbsolutePath()+"\n",keyWord);
			        }
			        else {
			        	output_file = null;
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
				System.out.println("output = "+output_file.getAbsolutePath());
				System.out.println("wtk index = "+jtf_wtk.getText());
				System.out.println("id index = "+jtf_id.getText());
				System.out.println("title = "+jtf_title.getText());
				
				input_file = null;
				input_text_pane.setText("");
				output_file = null;
				output_text_pane.setText("");
				jtf_wtk.setText("0");
				jtf_id.setText("");
				jtf_title.setText("");
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
		
		win.setSize(1010,500);
		win.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		win.setVisible(true);
	}
	
	
	public static void main(String[] args) throws Exception {
		
		TestGUI gui = new TestGUI();
	}
		
		
		
	
}
