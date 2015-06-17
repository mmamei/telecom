package analysis.user_place_recognizer.weight_functions;

import java.io.FileWriter;
import java.io.PrintWriter;

public final class Weights {
	
	
	private static final double[][] HOME={
	    //	0,		1,		2,		3,		4,		5,		6,		7,		8,		9,		10,		11,		12,		13,		14,		15,		16,		17,		18,		19,		20,		21,		22,		23
		{	0.01,	0.01,	0.01,	0.1,	0.2,	0.4,	0.5,	0.5,	0.4,	0.2,	0.1,	0.1,	0.1,	0.1,	0.1,	0.1,	0.1,	0.1,	0.1,	0.1,	0.1,	0.2,	0.3,	0.3},//Sundaty
		{	0.4,	0.5,	0.6,	0.7,	0.7,	0.6,	0.4,	0.1,	-0.05,	-0.05,	-0.1,	-0.2,	-0.1,	-0.1,	-0.1,	-0.2,	-0.2,	-0.1,	-0.05,	0.1,	0.1,	0.2,	0.3,	0.3},//Monday
		{	0.4,	0.5,	0.6,	0.7,	0.7,	0.6,	0.4,	0.1,	-0.05,	-0.05,	-0.1,	-0.2,	-0.1,	-0.1,	-0.1,	-0.2,	-0.2,	-0.1,	-0.05,	0.1,	0.1,	0.2,	0.3,	0.3},//Tuesday
		{	0.4,	0.5,	0.6,	0.7,	0.7,	0.6,	0.4,	0.1,	-0.05,	-0.05,	-0.1,	-0.2,	-0.1,	-0.1,	-0.1,	-0.2,	-0.2,	-0.1,	-0.05,	0.1,	0.1,	0.2,	0.3,	0.3},//Wednesday
		{	0.4,	0.5,	0.6,	0.7,	0.7,	0.6,	0.4,	0.1,	-0.05,	-0.05,	-0.1,	-0.2,	-0.1,	-0.1,	-0.1,	-0.2,	-0.2,	-0.1,	-0.05,	0.1,	0.1,	0.2,	0.4,	0.3},//Thursday
		{	0.4,	0.5,	0.6,	0.7,	0.7,	0.6,	0.4,	0.1,	-0.05,	-0.05,	-0.1,	-0.2,	-0.1,	-0.1,	-0.1,	-0.2,	-0.2,	-0.1,	-0.05,	0.1,	0.1,	0.1,	0.1,	0.1},//Friday
		{	0.1,	0.1,	0.1,	0.1,	0.2,	0.4,	0.5,	0.5,	0.4,	0.2,	0.1,	0.1,	0.1,	0.1,	0.1,	0.1,	0.1,	0.1,	0.1,	0.1,	0.1,	0.1,	0.1,	0.1}//Saturday
	};
	
	
	private static final double[][] WORK={
	    //	0,		1,		2,		3,		4,		5,		6,		7,		8,		9,		10,		11,		12,		13,		14,		15,		16,		17,		18,		19,		20,		21,		22,		23
		{	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1},//Sunday
		{	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	0.0,	0.1,	0.1,	0.1,	0.25,	0.5,	0.75,	1.0,	0.67,	0.33,	0.0,	0.0,	0.0,	0.0,	-0.1,	-0.1,	-0.1},//Monday
		{	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	0.0,	0.1,	0.1,	0.1,	0.25,	0.5,	0.75,	1.0,	0.67,	0.33,	0.0,	0.0,	0.0,	0.0,	-0.1,	-0.1,	-0.1},//Tuesday
		{	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	0.0,	0.1,	0.1,	0.1,	0.25,	0.5,	0.75,	1.0,	0.67,	0.33,	0.0,	0.0,	0.0,	0.0,	-0.1,	-0.1,	-0.1},//Wednesday
		{	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	0.0,	0.1,	0.1,	0.1,	0.25,	0.5,	0.75,	1.0,	0.67,	0.33,	0.0,	0.0,	0.0,	0.0,	-0.1,	-0.1,	-0.1},//Thursday
		{	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	0.0,	0.1,	0.1,	0.1,	0.25,	0.5,	0.75,	1.0,	0.67,	0.33,	0.0,	0.0,	0.0,	0.0,	-0.1,	-0.1,	-0.1},//Friday
		{	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1}//Saturday
	};
	
	private static final double[][] NIGHT={
	    //	0,		1,		2,		3,		4,		5,		6,		7,		8,		9,		10,		11,		12,		13,		14,		15,		16,		17,		18,		19,		20,		21,		22,		23
		{	0.5,	0.4,	0.2,	0.1,	0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	0.1,	-0.2,	0.3,	0.4,	0.5,	0.5,	0.5},//Sunday
		{	0.5,	0.2,	0.1,	0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	0.1,	0.3,	0.5,	0.5,	0.5},//Monday
		{	0.5,	0.2,	0.1,	0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	0.1,	0.3,	0.5,	0.5,	0.5},//Tuesday
		{	0.5,	0.2,	0.1,	0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	0.1,	0.3,	0.5,	0.5,	0.5},//Wednesday
		{	0.5,	0.2,	0.1,	0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	0.1,	0.3,	0.5,	0.5,	0.5},//Thursday
		{	0.5,	0.2,	0.1,	0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	0.1,	0.3,	0.5,	0.5,	0.5},//Friday
		{	0.5,	0.4,	0.2,	0.1,	0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	0.1,	-0.2,	0.3,	0.4,	0.5,	0.5,	0.5}//Saturday
	};
	
	
	private static final double[][] FRIDAY_NIGHT={
	    //	0,		1,		2,		3,		4,		5,		6,		7,		8,		9,		10,		11,		12,		13,		14,		15,		16,		17,		18,		19,		20,		21,		22,		23
		{	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1},//Sunday
		{	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1},//Monday
		{	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1},//Tuesday
		{	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1},//Wednesday
		{	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1},//Thursday
		{	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	0.1,	0.2,	0.3,	0.4,	0.5,	0.6,	0.7},//Friday
		{	0.8,	0.8,	0.8,	0.6,	0.4,	0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1}//Saturday
	};

	private static final double[][] SATURDAY_NIGHT={
	    //	0,		1,		2,		3,		4,		5,		6,		7,		8,		9,		10,		11,		12,		13,		14,		15,		16,		17,		18,		19,		20,		21,		22,		23
		{	0.8,	0.8,	0.8,	0.6,	0.4,	0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1},//Sunday
		{	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1},//Monday
		{	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1},//Tuesday
		{	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1},//Wednesday
		{	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1},//Thursday
		{	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1},//Friday
		{	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	0.1,	0.2,	0.3,	0.4,	0.5,	0.6,	0.7}//Saturday
	};
	
	private static final double[][] SUNDAY={
	    //	0,		1,		2,		3,		4,		5,		6,		7,		8,		9,		10,		11,		12,		13,		14,		15,		16,		17,		18,		19,		20,		21,		22,		23
		{	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	0.1,	0.4,	0.4,	0.3,	0.2,	0.2,	0.4,	0.5,	0.6,	0.6,	0.6,	0.5,	0.4,	0.2,	0.1},//Sunday
		{	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1},//Monday
		{	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1},//Tuesday
		{	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1},//Wednesday
		{	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1},//Thursday
		{	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1},//Friday
		{	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1,	-0.1}//Saturday
	};
	
	
	private static final double[][] GENERIC={
	    //	0,	1,	2,	3,	4,	5,	6,	7,	8,	9,	10,	11,	12,	13,	14,	15,	16,	17,	18,	19,	20,	21,	22,	23
		{	1,	1,	1,	1,	1,	1,	1,	1,	1,	1,	1,	1,	1,	1,	1,	1,	1,	1,	1,	1,	1,	1,	1,	1},//Sunday
		{	1,	1,	1,	1,	1,	1,	1,	1,	1,	1,	1,	1,	1,	1,	1,	1,	1,	1,	1,	1,	1,	1,	1,	1},//Monday
		{	1,	1,	1,	1,	1,	1,	1,	1,	1,	1,	1,	1,	1,	1,	1,	1,	1,	1,	1,	1,	1,	1,	1,	1},//Tuesday
		{	1,	1,	1,	1,	1,	1,	1,	1,	1,	1,	1,	1,	1,	1,	1,	1,	1,	1,	1,	1,	1,	1,	1,	1},//Wednesday
		{	1,	1,	1,	1,	1,	1,	1,	1,	1,	1,	1,	1,	1,	1,	1,	1,	1,	1,	1,	1,	1,	1,	1,	1},//Thursday
		{	1,	1,	1,	1,	1,	1,	1,	1,	1,	1,	1,	1,	1,	1,	1,	1,	1,	1,	1,	1,	1,	1,	1,	1},//Friday
		{	1,	1,	1,	1,	1,	1,	1,	1,	1,	1,	1,	1,	1,	1,	1,	1,	1,	1,	1,	1,	1,	1,	1,	1}//Saturday
	};
	
	
	public static double[][] get(String label) {
		if(label.equals("HOME")) return HOME;
		if(label.equals("WORK")) return WORK;
		if(label.equals("NIGHT")) return NIGHT;
		if(label.equals("FRIDAY_NIGHT")) return FRIDAY_NIGHT;
		if(label.equals("SATURDAY_NIGHT")) return SATURDAY_NIGHT;
		if(label.equals("SUNDAY")) return SUNDAY;
		if(label.equals("GENRIC")) return GENERIC;
		System.err.println("Weight matrix "+label+" undefined!");
		return null;
	}
	
	
	public static int getNumberOfHoursWithWeight(double[][] weights){
		int t=0;
		for(int i=0; i<weights.length; i++)
			for(int j=0; j<weights[i].length; j++){
				if(weights[i][j]>0)
					t++;
			}
		return t;
	}
	
	public static double getTotalSumWeight(double[][] weights){
		double t=0;
		for(int i=0; i<weights.length; i++)
		for(int j=0; j<weights[i].length; j++)
				t = t + weights[i][j];
		return t;
	}
	
	
	public static void main(String args[]) throws Exception {
		PrintWriter out = new PrintWriter(new FileWriter("weights.csv"));
		printCSVFormat(out,"Home",HOME);
		printCSVFormat(out,"Work",WORK);
		printCSVFormat(out,"Night",NIGHT);
		printCSVFormat(out,"Friday_Night",FRIDAY_NIGHT);
		printCSVFormat(out,"Saturday_Night",SATURDAY_NIGHT);
		printCSVFormat(out,"Sunday",SUNDAY);
		out.close();
		System.out.println("Done");
	}
	
	static String[] days = new String[]{"sun","mon","tue","wed","thu","fri","sat"};
	public static void printCSVFormat(PrintWriter out, String name, double[][] m) throws Exception {
		out.print(name);
		for(int i=0; i<24;i++)
			out.print(","+i);
		out.println();
		for(int i=0; i<m.length;i++) {
			out.print(days[i]);
			for(int j=0; j<m[i].length;j++)
				out.print(","+m[i][j]);
			out.println();
		}
		out.println();
	}
}
