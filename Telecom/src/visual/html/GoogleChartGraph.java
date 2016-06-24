package visual.html;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GoogleChartGraph {
	private static final DecimalFormat DF = new DecimalFormat("0.00",new DecimalFormatSymbols(Locale.US));
	
	
	public static String getGraph(String[] x, double[] y, String name, String xlab, String ylab) {
		List<double[]> ly = new ArrayList<>();
		ly.add(y);
		List<String> lnames = new ArrayList<>();
		lnames.add(name);
		return getGraph(x,ly,lnames,xlab,ylab);
	}
	
	public static String getGraph(String[] x, List<double[]> y, List<String> names, String xlab, String ylab) {
			
		StringBuffer sb = new StringBuffer();
		
		sb.append("<html>\n");
		sb.append("<head>\n");
		sb.append("<script type=\"text/javascript\" src=\"https://www.google.com/jsapi\"></script>\n");
		sb.append("<script type=\"text/javascript\">\n");
		sb.append("google.load('visualization', '1.0', {'packages':['corechart']});\n");
		sb.append("google.setOnLoadCallback(drawChart);\n");
		sb.append("function drawChart() {\n");
		sb.append("var data = google.visualization.arrayToDataTable([\n");

		String tit = "";
		for (String t : names)
			tit = tit + ", '" + t + "'";
		sb.append("['"+xlab+"'" + tit + "],\n");

		StringBuffer sv = new StringBuffer();
		for (int i = 0; i<x.length; i++) {
			
			sv = new StringBuffer();
			for (double[] d : y) {
				sv.append(", " + DF.format(d[i]));
			}

			sb.append(" ['" + x[i] + "'" + sv + "],");
		}
		sb.append(" ]);\n");
		sb.append("var options = {\n");
		sb.append("'width':1400,\n");
		sb.append("'height':500,\n");
		
		sb.append("vAxes: {0: {viewWindowMode:'explicit',gridlines: {color: 'transparent'}},1: {gridlines: {color: 'transparent'},format:'#'}},\n");
		sb.append("series: {0: {targetAxisIndex:0},1:{targetAxisIndex:1}},\n");
		
		sb.append("};\n");
		sb.append("var chart = new google.visualization.AreaChart(document.getElementById('chart_div'));\n");
		sb.append("chart.draw(data, options);\n");
		sb.append("}\n");
		sb.append("</script>\n");
		sb.append("</head>\n");
		sb.append("<body>\n");
		sb.append("<div id=\"chart_div\"></div>\n");
		sb.append("</body>\n");
		sb.append("</html>\n");

		return sb.toString();
	}
	
	public static String getHist(String[] x, List<double[]> y, List<String> names, String xlab, String ylab) {
		
		StringBuffer sb = new StringBuffer();
		
		sb.append("<html>");
		sb.append("<head>");
		sb.append("<script type=\"text/javascript\" src=\"https://www.google.com/jsapi\"></script>");
		sb.append("<script type=\"text/javascript\">");
		sb.append("google.load('visualization', '1.0', {'packages':['corechart']});");
		sb.append("google.setOnLoadCallback(drawChart);");
		sb.append("function drawChart() {");
		sb.append("var data = google.visualization.arrayToDataTable([");

		String tit = "";
		for (String t : names)
			tit = tit + ", '" + t + "'";
		sb.append("['"+xlab+"'" + tit + "],");

		StringBuffer sv = new StringBuffer();
		for (int i = 0; i<x.length; i++) {
			
			sv = new StringBuffer();
			for (double[] d : y) {
				sv.append(", " + DF.format(d[i]));
			}

			sb.append(" ['" + x[i] + "'" + sv + "],");
		}
		sb.append(" ]);");
		sb.append("var options = {");
		sb.append("'width':1400,");
		sb.append("'height':500");
		
	
		
		sb.append("};");
		sb.append("var chart = new google.visualization.ColumnChart(document.getElementById('chart_div'));");
		sb.append("chart.draw(data, options);");
		sb.append("}");
		sb.append("</script>");
		sb.append("</head>");
		sb.append("<body>");
		sb.append("<div id=\"chart_div\"></div>");
		sb.append("</body>");
		sb.append("</html>");

		return sb.toString();
	}
	
	
	public static void main(String[] args) throws Exception {
		PrintWriter out = new PrintWriter(new FileWriter("test.html"));
		String[] x = new String[]{"a","b","c"};
		List<double[]> y = new ArrayList<double[]>();
		y.add(new double[]{1,2,3});
		y.add(new double[]{100,400,100});
		List<String> names = new ArrayList<String>();
		names.add("s1");
		names.add("s2");
		out.println(getGraph(x,y,names,"x","y"));
		out.close();
	}
	

	
}
