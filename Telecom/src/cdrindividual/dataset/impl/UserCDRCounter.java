package cdrindividual.dataset.impl;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import net.sourceforge.jdatepicker.impl.JDatePanelImpl;
import net.sourceforge.jdatepicker.impl.JDatePickerImpl;
import net.sourceforge.jdatepicker.impl.UtilCalendarModel;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import region.Placemark;
import utils.Config;
import utils.Logger;
import utils.Mail;
import visual.r.RPlotter;

public class UserCDRCounter extends BufferAnalyzerConstrained {
	
	private Map<String,Integer> users_events;
	
	UserCDRCounter(Placemark placemark, String user_list_name) {
		super(placemark,user_list_name);
		users_events = new HashMap<String,Integer>();
	}

	void analyze(String username, String imsi, String celllac, long timestamp, Calendar cal, String header) {
		Integer n = users_events.get(username);
		users_events.put(username, n == null ? 1 : n+1);
	}
	
	
	private static final SimpleDateFormat F = new SimpleDateFormat("dd-MM-yyyy");
	protected void finish() {
		try{
			System.out.println(users_events.size());
			File d = new File(Config.getInstance().base_folder+"/UserCDRCounter");
			d.mkdirs();
					
			String date_interval = F.format(Config.getInstance().pls_start_time.getTime())+"-"+F.format(Config.getInstance().pls_end_time.getTime());
			PrintWriter out = new PrintWriter(new FileWriter(d+"/"+this.getString()+"_"+date_interval+"_minH_"+PLSParser.MIN_HOUR+"_maxH_"+PLSParser.MAX_HOUR+".csv"));
			for(String user: users_events.keySet())
				out.println(user+","+users_events.get(user));
			out.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void extractUsersAboveThreshold(File infile, File outfile, int threshold, int max_n_users) throws Exception {
		System.out.println("extract Users Above Threshold "+threshold);
		PrintWriter out = new PrintWriter(outfile);
		BufferedReader br = new BufferedReader(new FileReader(infile));
		String line;
		int count = 0;
		while((line=br.readLine())!=null){
			try {
				String[] x = line.split(",");
				String username = x[0];
				int n_events = Integer.parseInt(x[1]);
				if(n_events > threshold) {
					out.println(line);	
					count ++;
				}
				if(max_n_users > 0 && count > max_n_users)
					break;
					
			} catch(Exception e) {
				System.out.println("BAD LINE = "+line);
			}
		}
		br.close();
		out.close();
	}
	
	private static final boolean PLOT = true;
	public static double[] percentAnalysis(File f) throws Exception {
		
		DescriptiveStatistics stats = new DescriptiveStatistics();
		BufferedReader br = new BufferedReader(new FileReader(f));
		String line; 
		while((line = br.readLine())!=null) {
			try {
				double v = Double.parseDouble(line.substring(line.indexOf(",")+1));
				stats.addValue(v);
			} catch(Exception e) {
				System.out.println("BAD LINE = "+line);
			}
		}
		br.close();
		
		int ndays = (int)((Config.getInstance().pls_end_time.getTimeInMillis() - Config.getInstance().pls_start_time.getTimeInMillis()) / (1000 * 3600 * 24)); 
		
		double[] perc = new double[100];
		double[] valXday = new double[100];
		System.out.println("Percentile;CDR count;CDR X Day Count");
		for(int i=1; i<=100;i++) {
			System.out.println(i+"%;"+stats.getPercentile(i)+";"+stats.getPercentile(i)/ndays);
			valXday[i-1] = stats.getPercentile(i)/ndays;
			perc[i-1] = 1.0*i/100;
		}
		System.out.println("TOT DAYS = "+ndays);
		System.out.println("TOT USERS = "+stats.getN());
		
		if(PLOT) {
			String fname = f.getName().replaceAll(".csv", ".png");
			RPlotter.drawScatter(valXday, perc, "CDR X Day", "CDF", Config.getInstance().base_folder+"/Images/"+fname, "");
		}
		
		
		return valXday;
	}
	
	public static void main(String[] args) throws Exception {
		new CreatorUserCDRCounterGUI();
	}
	
	
	
	/*
	 * In thoery this main is now useless.
	 * All the operations can be performed via the GUI without commenting/uncommenting anything
	 */
	
	public static void mainY(String[] args) throws Exception {	
		PLSParser.REMOVE_BOGUS = false;
		
		String region = "file_pls_piem";
		Config.getInstance().pls_folder = Config.getInstance().pls_root_folder+"/"+region;
		Config.getInstance().pls_start_time = new GregorianCalendar(2015,Calendar.JUNE,1);
		Config.getInstance().pls_end_time = new GregorianCalendar(2015,Calendar.JUNE,31);
		//PLSParser.MIN_HOUR = 1;
		//PLSParser.MAX_HOUR = 3;
		//new UserCDRCounter(null,null).run();
		
		String date_interval = F.format(Config.getInstance().pls_start_time.getTime())+"-"+F.format(Config.getInstance().pls_end_time.getTime());
		File f = new File(Config.getInstance().base_folder+"/UserCDRCounter/"+region+"_"+date_interval+"_minH_"+PLSParser.MIN_HOUR+"_maxH_"+PLSParser.MAX_HOUR+".csv");
		//percentAnalysis(f);
	
		
		int threshold = 400;
		int max_users_retrieved = 1000;
		//String filename = region+"_bogus.csv";
		String filename = f.getName().replaceAll(".csv", "_ABOVE_"+threshold+(max_users_retrieved > -1 ? "limit_"+max_users_retrieved : "")+".csv");
		extractUsersAboveThreshold(f,new File(Config.getInstance().base_folder+"/UserCDRCounter"+"/"+filename), threshold,max_users_retrieved);
		//percentAnalysis(new File(Config.getInstance().base_folder+"/UserCDRCounter"+"/"+filename));
		
	}
	
	
	
	
	/*
	 * This is a special purpose main.
	 * It is used to draw the percentile (CDF) of multiple years.
	 * The idea is to show if the number of CDR x user is decreasing (or increasing)
	 */
	
	public static void mainX(String[] args) throws Exception {
		PLSParser.REMOVE_BOGUS = true;
		List<String> names = new ArrayList<String>();
		List<double[]> y = new ArrayList<double[]>();
		
		System.out.println("************************* 2012");
		
		String region = "file_pls_piem";
		Config.getInstance().pls_folder = Config.getInstance().pls_root_folder+"/"+region;
		Config.getInstance().pls_start_time = new GregorianCalendar(2012,Calendar.MARCH,11);
		Config.getInstance().pls_end_time = new GregorianCalendar(2012,Calendar.MARCH,17);
		new UserCDRCounter(null,null).run();
		y.add(percentAnalysis(new File(Config.getInstance().base_folder+"/UserEventCounter/"+region+"_count_timeframe_"+PLSParser.MIN_HOUR+"_"+PLSParser.MAX_HOUR+".csv")));
		names.add("2012");
		
		System.out.println("************************* 2013");
		
		Config.getInstance().pls_folder = Config.getInstance().pls_root_folder+"/"+region;
		Config.getInstance().pls_start_time = new GregorianCalendar(2013,Calendar.JUNE,23);
		Config.getInstance().pls_end_time = new GregorianCalendar(2013,Calendar.JUNE,29);
		new UserCDRCounter(null,null).run();
		y.add(percentAnalysis(new File(Config.getInstance().base_folder+"/UserEventCounter/"+region+"_count_timeframe_"+PLSParser.MIN_HOUR+"_"+PLSParser.MAX_HOUR+".csv")));
		names.add("2013");
		
		System.out.println("************************* 2014");
		
		Config.getInstance().pls_folder = Config.getInstance().pls_root_folder+"/"+region;
		Config.getInstance().pls_start_time = new GregorianCalendar(2014,Calendar.MARCH,16);
		Config.getInstance().pls_end_time = new GregorianCalendar(2014,Calendar.MARCH,22);
		new UserCDRCounter(null,null).run();
		y.add(percentAnalysis(new File(Config.getInstance().base_folder+"/UserEventCounter/"+region+"_count_timeframe_"+PLSParser.MIN_HOUR+"_"+PLSParser.MAX_HOUR+".csv")));
		names.add("2014");
		
		System.out.println("************************* 2015");
		
		Config.getInstance().pls_folder = Config.getInstance().pls_root_folder+"/"+region;
		Config.getInstance().pls_start_time = new GregorianCalendar(2015,Calendar.JUNE,7);
		Config.getInstance().pls_end_time = new GregorianCalendar(2015,Calendar.JUNE,13);
		new UserCDRCounter(null,null).run();
		y.add(percentAnalysis(new File(Config.getInstance().base_folder+"/UserEventCounter/"+region+"_count_timeframe_"+PLSParser.MIN_HOUR+"_"+PLSParser.MAX_HOUR+".csv")));
		names.add("2015");
		
		
		String[] x = new String[100];
		for(int i=1;i<=100;i++)
			x[i-1] = String.valueOf(i);
		
		
		RPlotter.drawLine(x, y, names, "year", "percentile", "cdr x day", Config.getInstance().base_folder+"/Images/cdrXdayYearTrend.pdf", null);	
	}
}




/************************************************************************/




class CreatorUserCDRCounterGUI {
	
	private static final String[] REGIONS = new String[]{"file_pls_er","file_pls_fi","file_pls_lomb","file_pls_piem","file_pls_pu","file_pls_ve","file_pls_veneto"};
	
	
	JFrame win;
	
	
	JComboBox<String> input_region;
	JDatePickerImpl startDate = null;
	JDatePickerImpl endDate = null;
	JTextField minHour = null;
	JTextField maxHour = null;
	JButton go_cdr_count;
	
	
	
	JButton input_file_button;
	File input_file = null; 
	JButton go_percentile;
	
	JButton input_file_button2;
	JTextField threshold = null;
	JTextField maxretrieved = null;
	JCheckBox bogusName = null;
	JButton go_threshold = null;
	
	public CreatorUserCDRCounterGUI() {
		win = new JFrame("CreatorUserCDRCounterGUI");
		Container c = win.getContentPane();
		c.setLayout(new FlowLayout());

				
		input_region = new JComboBox<>(REGIONS); 
		
		startDate = new JDatePickerImpl(new JDatePanelImpl(new UtilCalendarModel()));
		endDate = new JDatePickerImpl(new JDatePanelImpl(new UtilCalendarModel()));
		minHour = new JTextField("00");
		maxHour = new JTextField("25");
		go_cdr_count = new JButton("Go!"); go_cdr_count.setPreferredSize(new Dimension(60, 30)); go_cdr_count.setBackground(Color.RED);
		
		
		
		
		 
		
		
		go_cdr_count.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				
				System.out.println("input = "+input_region.getSelectedItem());
				Calendar startcal = (Calendar)startDate.getModel().getValue();
				Calendar endcal = (Calendar)endDate.getModel().getValue();
				System.out.println("start date = "+startcal.getTime());
				System.out.println("end date = "+endcal.getTime());
				System.out.println("min hour = "+Integer.parseInt(minHour.getText()));
				System.out.println("max hour = "+Integer.parseInt(maxHour.getText()));
				
				Config.getInstance().pls_folder = Config.getInstance().pls_root_folder+"/"+input_region.getSelectedItem();
				Config.getInstance().pls_start_time = startcal;
				Config.getInstance().pls_end_time = endcal;
				PLSParser.MIN_HOUR = Integer.parseInt(minHour.getText());
				PLSParser.MAX_HOUR = Integer.parseInt(maxHour.getText());
				PLSParser.REMOVE_BOGUS = false;
				new UserCDRCounter(null,null).run();
				System.out.println("Done");
				
			}
		});
		
		
		input_file_button = new JButton("Select input file"); input_file_button.setPreferredSize(new Dimension(200, 30));
		go_percentile = new JButton("Go!"); go_percentile.setPreferredSize(new Dimension(60, 30)); go_percentile.setBackground(Color.RED);
		input_file_button.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				try {
					JFileChooser fileChooser = new JFileChooser(new File(Config.getInstance().base_folder+"/UserCDRCounter"));
					int n = fileChooser.showOpenDialog(win);
			        if (n == JFileChooser.APPROVE_OPTION) 
			        	input_file = fileChooser.getSelectedFile();
			        else
			        	input_file = null;
				} catch(Exception exc) {
					exc.printStackTrace();
				}
			}
		});
		
		
		
		go_percentile.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				
				try {
					System.out.println("input = "+input_file.getAbsolutePath());
					UserCDRCounter.percentAnalysis(new File(input_file.getAbsolutePath()));
					System.out.println("Done");
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				
			}
		});
		
		
		input_file_button2 = new JButton("Select input file"); input_file_button.setPreferredSize(new Dimension(200, 30));
		input_file_button2.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				try {
					JFileChooser fileChooser = new JFileChooser(new File(Config.getInstance().base_folder+"/UserCDRCounter"));
					int n = fileChooser.showOpenDialog(win);
			        if (n == JFileChooser.APPROVE_OPTION) 
			        	input_file = fileChooser.getSelectedFile();
			        else
			        	input_file = null;
				} catch(Exception exc) {
					exc.printStackTrace();
				}
			}
		});
		
		threshold = new JTextField("0");
		threshold.setPreferredSize(new Dimension(60,30));
		
		maxretrieved = new JTextField("-1");
		maxretrieved.setPreferredSize(new Dimension(60,30));
		
		bogusName = new JCheckBox();
		go_threshold = new JButton("Go!"); go_threshold.setPreferredSize(new Dimension(60, 30)); go_threshold.setBackground(Color.RED);
		go_threshold.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				
				try {
					System.out.println("input = "+input_file.getAbsolutePath());
					File f = new File(input_file.getAbsolutePath());
					
					
					int th = Integer.parseInt(threshold.getText());
					int max_users_retrieved = Integer.parseInt(maxretrieved.getText());
					
					String region = null;
					for(String r: REGIONS)
						if(f.getName().contains(r))
							region = r;
					
					
					
					String outfile = bogusName.isSelected() ? region+"_bogus.csv" : f.getName().replaceAll(".csv", "_ABOVE_"+th+(max_users_retrieved > -1 ? "limit_"+max_users_retrieved : "")+".csv");
					System.out.println("output = "+outfile);
					UserCDRCounter.extractUsersAboveThreshold(f,new File(Config.getInstance().base_folder+"/UserCDRCounter"+"/"+outfile), th,max_users_retrieved);
					System.out.println("Done");
					

				} catch (Exception e1) {
					e1.printStackTrace();
				}
				
			}
		});
		
		
		
		/************************************************************************************************************/
		/*										LAYOUT																*/
		/************************************************************************************************************/
		
		JLabel subT = new JLabel("Select input dir, start time, end time, min hour, max hour, output file. It computes a csv file with user_id, number of CDR in selected range",SwingConstants.CENTER);
		subT.setPreferredSize(new Dimension(1200, 50));
		c.add(subT);
		
		c.add(input_region);
		
		c.add(new JLabel("start date:"));
		c.add(startDate);
		c.add(new JLabel("end date:"));
		c.add(endDate);
		
		c.add(new JLabel("min hour:"));
		c.add(minHour);
		c.add(new JLabel("max hour:"));
		c.add(maxHour);
		
		c.add(go_cdr_count);
		
		
		JLabel subT2 = new JLabel("Select input file, computes the cdf of number of users with a given number of cdr",SwingConstants.CENTER);
		subT2.setPreferredSize(new Dimension(1200, 50));
		c.add(subT2);
		
		
		c.add(input_file_button);
		c.add(go_percentile);
		
		JLabel subT3 = new JLabel("Select input file, threshold and outputfile. Selects all the users above thhat threshold",SwingConstants.CENTER);
		subT3.setPreferredSize(new Dimension(1200, 50));
		c.add(subT3);
		
		c.add(input_file_button2);
		c.add(new JLabel("threshold"));
		c.add(threshold);
		c.add(new JLabel("max retrieved"));
		c.add(maxretrieved);
		c.add(new JLabel("bogusName"));
		c.add(bogusName);
		c.add(go_threshold);
		
		win.setResizable(false);
		win.setSize(1200,350);
		win.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		win.setVisible(true);
	}
}



