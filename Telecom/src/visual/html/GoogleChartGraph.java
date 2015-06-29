package visual.html;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

public class GoogleChartGraph {
	private static final DecimalFormat DF = new DecimalFormat("0.00",new DecimalFormatSymbols(Locale.US));
	
	public static String getGraph(String[] x, List<double[]> y, List<String> names, String xlab, String ylab) {
			
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
		sb.append("var chart = new google.visualization.AreaChart(document.getElementById('chart_div'));");
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
	

	
}
