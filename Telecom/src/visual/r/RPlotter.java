package visual.r;

import java.awt.Desktop;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.rosuda.REngine.Rserve.RConnection;

import utils.Config;
import utils.StatsUtils;

public class RPlotter {
	
	public static boolean WRITE_CSV_FILE_WITH_RAW_DATA = true;
	public static boolean VIEW = true;
	public static int FONT_SIZE = 40;
	
	private static RConnection c = null;
	
	
	/************************************************************************************************************/
	/************************************************************************************************************/
	/* 													BAR										 				*/
	/************************************************************************************************************/
	/************************************************************************************************************/
	
	
	public static void drawBar(String[] x, double [] y, String xlab, String ylab, String file, String opts) {
		try {
            file = file.replaceAll("_", "-");
            c = new RConnection();// make a new local connection on default port (6311)
            
            c.assign("x", x);
            c.assign("y", y);
            
            String end = opts==null || opts.length()==0 ? "+" : " + "+opts+"+";
            String code = 
            		   "library(ggplot2);"
            		 + "x <- ordered(x,levels=c(x));"
            	     + "z <- data.frame(x,y);"
            	     + "ggplot(z,aes(x=factor(x),y=y)) + geom_bar(stat='identity') + theme_bw(base_size = "+FONT_SIZE+") +xlab('"+xlab+"') + ylab('"+ylab+"')"+end
            	     + "ggsave('"+file+"',height=6,width=12);";
            
            System.out.println(code.replaceAll(";", ";\n"));
            c.eval(code);
            c.close();
            if(VIEW) Desktop.getDesktop().open(new File(file));
        } catch (Exception e) {
        	if(e.getMessage().startsWith("Cannot connect")) {
             	System.err.println("You must launch the following code in R");
             	System.err.println("library(Rserve)");
             	System.err.println("Rserve()");
            }
            else {
            	c.close();
            	e.printStackTrace();
            }
        }      
	}
	
	
	public static void drawBar(String[] x, List<double []> y, List<String> names, String kind, String xlab, String ylab, String file, String opts) {
		try {
			file = file.replaceAll("_", "-");
            c = new RConnection();// make a new local connection on default port (6311)
            
            c.assign("x", x);
            for(int i=0; i<y.size();i++)
            	c.assign("y"+i, y.get(i));
            
           
            
            StringBuffer sby= new StringBuffer();
            for(int i=0; i<y.size();i++)
            	sby.append(",y"+i);
            
            StringBuffer sbn= new StringBuffer("'x'");
            for(int i=0; i<names.size();i++)
            	sbn.append(",'"+names.get(i)+"'");
            
            
            String end = opts==null || opts.length()==0 ? ";" : " + "+opts+";";
            String code = 
            		   "library(ggplot2);"
            		 + "library(reshape2);"
            		 + "x <- ordered(x,levels=c(x));"
            	     + "z <- data.frame(x"+sby+");"
            	     + "names(z) <- c("+sbn+");"
            	     + "z <- melt(z,id.vars=c('x'));"
            	     + "names(z) <- c('x','"+kind+"','value');"
            	     + "ggplot(z,aes(x=x, y=value, fill="+kind+")) + geom_bar(stat='identity', position='dodge') + scale_fill_grey() + theme_bw(base_size = "+FONT_SIZE+") +xlab('"+xlab+"') + ylab('"+ylab+"')"+end
            	     + "ggsave('"+file+"',width=10);"
            	     + "dev.off();";
            
            //System.out.println(code);
            c.eval(code);
            c.close();
            if(VIEW) Desktop.getDesktop().open(new File(file));
        } catch (Exception e) {
        	c.close();
        	if(e.getMessage().startsWith("Cannot connect")) {
             	System.err.println("You must launch the following code in R");
             	System.err.println("library(Rserve)");
             	System.err.println("Rserve()");
            }
            else e.printStackTrace();
        }      
	}
	
	
	/************************************************************************************************************/
	/************************************************************************************************************/
	/* 												LINE										 				*/
	/************************************************************************************************************/
	/************************************************************************************************************/
	
	
	
	public static void drawLine(double[] x, double [] y, String xlab, String ylab, String file, String opts) {
		try {
			file = file.replaceAll("_", "-");
            c = new RConnection();// make a new local connection on default port (6311)
            
            c.assign("x", x);
            c.assign("y", y);
            
            String end = opts==null || opts.length()==0 ? ";" : " + "+opts+";";
            String code = 
            		   "library(ggplot2);"
            	     + "z <- data.frame(x,y);"
            	     + "ggplot(z,aes(x=x,y=y)) + geom_line() + geom_point() + theme_bw(base_size = "+FONT_SIZE+") +xlab('"+xlab+"') + ylab('"+ylab+"')"+end
            	     + "ggsave('"+file+"');"
            	     + "dev.off();";
            //System.out.println(code);
            c.eval(code);
            c.close();
            if(VIEW) Desktop.getDesktop().open(new File(file));
        } catch (Exception e) {
        	c.close();
        	if(e.getMessage().startsWith("Cannot connect")) {
             	System.err.println("You must launch the following code in R");
             	System.err.println("library(Rserve)");
             	System.err.println("Rserve()");
            }
            else e.printStackTrace();
        }      
	}
	
	static final DecimalFormat F = new DecimalFormat("#.##",new DecimalFormatSymbols(Locale.US));
	public static void drawLine(double[] x, List<double []> y, List<String> names, String kind, String xlab, String ylab, String file, String opts) {
		String[] sx = new String[x.length];
		for(int i=0; i<sx.length;i++)
			sx[i] = F.format(x[i]);
		drawLine(sx,y,names,kind,xlab,ylab,file,opts);
	}
	
	public static void drawLine(String[] x, List<double []> y, List<String> names, String kind, String xlab, String ylab, String file, String opts) {
		try {
			file = file.replaceAll("_", "-");
            c = new RConnection();// make a new local connection on default port (6311)
            
            c.assign("x", x);
            for(int i=0; i<y.size();i++)
            	c.assign("y"+i, y.get(i));
            
           
            
            StringBuffer sby= new StringBuffer();
            for(int i=0; i<y.size();i++)
            	sby.append(",y"+i);
            
            StringBuffer sbn= new StringBuffer("'x'");
            for(int i=0; i<names.size();i++)
            	sbn.append(",'"+names.get(i)+"'");
            
            
            String end = opts==null || opts.length()==0 ? ";" : " + "+opts+";";
            String code = 
            		   "library(ggplot2);"
            		 + "library(reshape2);"
            		 + "x <- ordered(x,levels=c(x));"
            	     + "z <- data.frame(x"+sby+");"
            	     + "names(z) <- c("+sbn+");"
            	     + "z <- melt(z,id.vars=c('x'));"
            	     + "names(z) <- c('x','"+kind+"','value');"
            	     + "ggplot(z,aes(x=x, y=value, linetype="+kind+", group="+kind+", shape="+kind+")) + geom_line() + geom_point() + theme_bw(base_size = "+FONT_SIZE+") +xlab('"+xlab+"') + ylab('"+ylab+"')"+end
            	     + "ggsave('"+file+"',width=10);"
            	     + "dev.off();";
            
            //System.out.println(code);
            c.eval(code);
            c.close();
            if(VIEW) Desktop.getDesktop().open(new File(file));
        } catch (Exception e) {
        	c.close();
        	if(e.getMessage().startsWith("Cannot connect")) {
             	System.err.println("You must launch the following code in R");
             	System.err.println("library(Rserve)");
             	System.err.println("Rserve()");
            }
            else e.printStackTrace();
        }      
	}
	
	
	/************************************************************************************************************/
	/************************************************************************************************************/
	/* 												SCATTER										 				*/
	/************************************************************************************************************/
	/************************************************************************************************************/
	
	
	
	public static void drawScatter(double[] x, double [] y, String xlab, String ylab, String file, String opts) {
		try {
			file = file.replaceAll("_", "-");
            c = new RConnection();// make a new local connection on default port (6311)
            
            c.assign("x", x);
            c.assign("y", y);
            
            if(!xlab.startsWith("expression")) xlab = "'"+xlab+"'";
            if(!ylab.startsWith("expression")) ylab = "'"+ylab+"'";
            
            
            
            String end = opts==null || opts.length()==0 ? "+" : " + "+opts+"+";
            String code = 
            		   "library(ggplot2);"
            	     + "z <- data.frame(x,y);"
            	     + "ggplot(z,aes(x=x,y=y))"+(opts.contains("geom_point")?"":"+ geom_point()")+ " + theme_bw(base_size = "+FONT_SIZE+") +xlab("+xlab+") + ylab("+ylab+")"+end
            	     + "ggsave('"+file+"');";
            //System.out.println(printRVector("x",x));
            //System.out.println(printRVector("y",y));
            //System.out.println(code.replaceAll(";", ";\n"));
            c.eval(code);
            c.close();
            if(VIEW) Desktop.getDesktop().open(new File(file));
        } catch (Exception e) {
        	e.printStackTrace();
        	/*
        	if(e.getMessage().startsWith("Cannot connect")) {
             	System.err.println("You must launch the following code in R");
             	System.err.println("library(Rserve)");
             	System.err.println("Rserve()");
            }
            else{
            	c.close();
            	e.printStackTrace();
            }
            */
        }      
	}
	
	public static void drawScatterWLabels(double[] x, double [] y, String[] labels, String xlab, String ylab, String file, String opts) {
		try {
			file = file.replaceAll("_", "-");
            c = new RConnection();// make a new local connection on default port (6311)
            
            c.assign("x", x);
            c.assign("y", y);
            c.assign("l", labels);
            
            
            //System.out.println(printRVector("x",x));
            //System.out.println(printRVector("y",y));
            //System.out.println(printRVector("l",labels));
            
            if(!xlab.startsWith("expression")) xlab = "'"+xlab+"'";
            if(!ylab.startsWith("expression")) ylab = "'"+ylab+"'";
            
            String end = opts==null || opts.length()==0 ? ";" : " + "+opts+";";
            String code = 
            		   "library(ggplot2);"
            	     + "z <- data.frame(x,y);"
            	     + "ggplot(z,aes(x=x,y=y)) + theme_bw(base_size = "+FONT_SIZE+") +xlab("+xlab+") + ylab("+ylab+") + geom_text(aes(label=l), size=3)"+end
            	     + "ggsave('"+file+"');";
            //System.out.println(code.replaceAll(";", ";\n"));
            c.eval(code);
            c.close();
            if(VIEW) Desktop.getDesktop().open(new File(file));
            
            if(WRITE_CSV_FILE_WITH_RAW_DATA) {
            	String csvfile = file.replaceAll(".png|.pdf", ".csv");
            	PrintWriter out = new PrintWriter(new FileWriter(csvfile));
            	for(int i=0; i<x.length;i++)
            		out.println(x[i]+","+y[i]+","+labels[i]);
            	out.close();
            }
            
            
        } catch (Exception e) {
        	if(e.getMessage().startsWith("Cannot connect")) {
             	System.err.println("You must launch the following code in R");
             	System.err.println("library(Rserve)");
             	System.err.println("Rserve()");
            }
            else{
            	c.close();
            	e.printStackTrace();
            }
        }      
	}
	
	
	public static void drawScatter(List<double[]> x, List<double []> y, List<String> names, String kind, String xlab, String ylab, String file, String opts) {
		try {
			file = file.replaceAll("_", "-");
            c = new RConnection();// make a new local connection on default port (6311)
            
            for(int i=0; i<x.size();i++)
            	c.assign("x"+i, x.get(i));
            
            for(int i=0; i<y.size();i++)
            	c.assign("y"+i, y.get(i));
                      
            
            if(!xlab.startsWith("expression")) xlab = "'"+xlab+"'";
            if(!ylab.startsWith("expression")) ylab = "'"+ylab+"'";
            
            String end = opts==null || opts.length()==0 ? ";\n" : " + "+opts+";\n";
            String code = 
            		   "library(ggplot2);\n"
            		 + "library(reshape2);\n";
            
            for(int i=0; i<x.size();i++) {
            	code += "z"+i+" <- data.frame(x"+i+",y"+i+");\n";
            	code += "names(z"+i+") <- c('x','"+names.get(i)+"');\n";
            	code += "z"+i+" <- melt(z"+i+",id.vars=c('x'));\n";
            }
            
            String zl = "";
            for(int i=0; i<x.size();i++)
            	zl += ",z"+i;
            zl = zl.substring(1);
            
            code +=  "z <- rbind("+zl+");\n"
            		 + "names(z) <- c('x','"+kind+"','value');"
            	     + "ggplot(z,aes(x=x, y=value, linetype="+kind+", group="+kind+", shape="+kind+")) + geom_point() + theme_bw(base_size = "+FONT_SIZE+") +xlab('"+xlab+"') + ylab('"+ylab+"')"+end
            	     + "ggsave('"+file+"');\n";
            
            System.out.println(code);
            c.eval(code);
            c.close();
            if(VIEW) Desktop.getDesktop().open(new File(file));
        } catch (Exception e) {
        	c.close();
        	if(e.getMessage().startsWith("Cannot connect")) {
             	System.err.println("You must launch the following code in R");
             	System.err.println("library(Rserve)");
             	System.err.println("Rserve()");
            }
            else e.printStackTrace();
        }      
	}
	
	private static final DecimalFormat DF = new DecimalFormat("0.00",new DecimalFormatSymbols(Locale.US));
	private static final boolean LM = false;
	public static double plotCorrelation(Map<String,Double> mapx, Map<String,Double> mapy, String titx, String tity, String file, Map<String,String> name2name, boolean use_labels) {
		
		
		List<Double> lx = new ArrayList<Double>();
		List<Double> ly = new ArrayList<Double>();
		List<String> labels = new ArrayList<String>();
		
		for(String k: mapx.keySet()) {
			//if(k.equals("97091")) continue;
			double vx = mapx.get(k);
			
			//if(vx < 1  || vx > 10.7) continue;
			
			Double vy = mapy.get(k);
			if(!Double.isNaN(vx) && vy != null) {
				lx.add(vx);
				ly.add(vy);
				String ll = k.replace("'", "");
				if(name2name!=null && name2name.get(ll) != null) ll =  name2name.get(ll).toUpperCase().split("-")[0];
				labels.add(ll);
			}
		}
		
		double[] x = new double[lx.size()];
		double[] y = new double[ly.size()];
		String[] l = new String[labels.size()];
		for(int i=0; i<x.length;i++) {
			x[i] = lx.get(i);
			y[i] = ly.get(i);
			l[i] = labels.get(i);
		}
		
		//System.out.println(titx+" map = "+mapx.size()+" array = "+x.length);
		//System.out.println(tity+" map = "+mapy.size()+" array = "+y.length);
		
		double r = StatsUtils.r(x, y);
		double r2v = StatsUtils.r2(x, y);
		String r2 = "annotate(\"text\", parse=TRUE, size=10, fontface='bold', x="+(r > 0 ? "-" : "")+"Inf, y=Inf, label=\"r^2 == "+DF.format(r2v)+"\", hjust="+(r > 0 ? "-0.2" : "1.2")+", vjust=1.2)";
		
		if(use_labels) RPlotter.drawScatterWLabels(x,y,l,titx, tity, file, "stat_smooth("+(LM?"":"method=lm,")+"colour='black') + theme(legend.position='none') + "+r2);
		else RPlotter.drawScatter(x,y,titx, tity, file,"stat_smooth("+(LM?"":"method=lm,")+"colour='black') + theme(legend.position='none') + geom_point(alpha=0.2,size = 5) + "+r2);
	
		return r2v;

	}

	
	/************************************************************************************************************/
	/************************************************************************************************************/
	/* 													MAP										 				*/
	/************************************************************************************************************/
	/************************************************************************************************************/
	
	
	
	
	
	
	
	/************************************************************************************************************/
	/************************************************************************************************************/
	/* 												CORR										 				*/
	/************************************************************************************************************/
	/************************************************************************************************************/
	
	public static void drawCorr(String file, String[] headers, double[][] m, String params) {
		
		
		double[][] tm = new double[m.length][m[0].length];
		for(int i=0; i<m.length;i++)
		for(int j=0; j<m[i].length;j++)
			tm[i][j] = m[j][i];
		
		String code = null;
		try {
			file = file.replaceAll("_", "-");
			System.out.println(file);
            c = new RConnection();// make a new local connection on default port (6311)
            
            StringBuffer sbm= new StringBuffer();
            
            for(int i=0; i<m.length;i++) {
            	c.assign("m"+i, tm[i]);
            	sbm.append(",m"+i);
            }
            c.assign("headers", headers);
            
           
          code = "library(corrplot);"+
         		 "m <- data.frame("+sbm.substring(1)+");"+
         		 "names(m)<-headers;"+
         		 "row.names(m)<-headers;"+
         		 "pdf('"+file+"',width=10, height=10);"+
         		 "corrplot(as.matrix(m),method='color');"+
         		 "dev.off();";
            
            System.out.println(code);
            c.eval(code);
            c.close();
            if(VIEW) Desktop.getDesktop().open(new File(file));
        } catch (Exception e) {
        	if(e.getMessage().startsWith("Cannot connect")) {
             	System.err.println("You must launch the following code in R");
             	System.err.println("library(Rserve)");
             	System.err.println("Rserve()");
            }
            else {
            	c.close();
            	e.printStackTrace();
            	System.err.println(code);
            }
        }      
	}
	
	
	
	/************************************************************************************************************/
	/************************************************************************************************************/
	/* 												BOX PLOT									 				*/
	/************************************************************************************************************/
	/************************************************************************************************************/
	
	public static void drawBoxplot(List<double []> y, List<String> names, String xlab, String ylab, String file, int width, String opts) {
		try {
			file = file.replaceAll("_", "-");
            c = new RConnection();// make a new local connection on default port (6311)
            
            for(int i=0; i<y.size();i++) {
            	c.assign("y"+i, y.get(i));
            	System.out.println(printRVector("y"+i,y.get(i)));
            }
            
                      
            String end = opts==null || opts.length()==0 ? ";" : " + "+opts+";";
            String code = 
            		   "library(ggplot2);"
            		 + "library(reshape2);";
            
            for(int i=0;i<y.size();i++) {
            		code += "yy"+i+" <- data.frame(y"+i+");";
            		code += "names(yy"+i+") <- c('"+names.get(i)+"');";
            		code += "yy"+i+" <- melt(yy"+i+",id.vars=c());";
            }
            
            if(!xlab.startsWith("expression")) xlab = "'"+xlab+"'";
            if(!ylab.startsWith("expression")) ylab = "'"+ylab+"'";
            
            StringBuffer sby= new StringBuffer();
            for(int i=0; i<y.size();i++)
            	sby.append(",yy"+i);
            
            	     code += "z <- rbind("+sby.substring(1)+");"   
            	     	  + "ggplot(z,aes(x=factor(variable),y=value)) + geom_boxplot(notch = TRUE) + theme_bw(base_size = "+FONT_SIZE+") +xlab("+xlab+") + ylab("+ylab+")"+end
            	          + "ggsave('"+file+"',width="+width+");";
            
            System.out.println(code.replaceAll(";", ";\n"));
            c.eval(code);
            c.close();
            if(VIEW) Desktop.getDesktop().open(new File(file));
            
            
            if(WRITE_CSV_FILE_WITH_RAW_DATA) {
            	String csvfile = file.replaceAll(".png|.pdf", ".csv");
            	PrintWriter out = new PrintWriter(new FileWriter(csvfile));
            	for(int i=0; i<names.size();i++) {
            		out.print(names.get(i));
            		for(double d: y.get(i))
            			out.print(","+d);
            		out.println();
            	}
            	out.close();
            }
            
            
        } catch (Exception e) {
        	e.printStackTrace();
        	if(e.getMessage().startsWith("Cannot connect")) {
             	System.err.println("You must launch the following code in R");
             	System.err.println("library(Rserve)");
             	System.err.println("Rserve()");
            }
            else e.printStackTrace();
        	c.close();
        }      
	}
	
	
	//public static DecimalFormat F = new DecimalFormat("##.##",DecimalFormatSymbols.getInstance(Locale.US)); 
	public static int MAX = 500;
	public static String printRVector(String name, double[] x) {
		if(x.length==0) return "";
		StringBuffer sb = new StringBuffer(name+"<-c("+F.format(x[0]));
		for(int i=1; i<Math.min(x.length, MAX);i++)
			sb.append(","+F.format(x[i]));
		sb.append(");");
		return sb.toString();
	}
	public static String printRVector(String name, String[] x) {
		if(x.length==0) return "";
		StringBuffer sb = new StringBuffer(name+"<-c('"+x[0]+"'");
		for(int i=1; i<Math.min(x.length, MAX);i++)
			sb.append(",'"+x[i]+"'");
		sb.append(");");
		return sb.toString();
	}
	
	
	public static void main(String[] args) {
		
//		drawBar(new String[]{"a","b","c"},new double[]{5,6,7},"x","y",Config.getInstance().base_folder+"/Images/test.pdf",null);
//		drawLine(new double[]{1,2,3},new double[]{5,6,7},"x","y",Config.getInstance().base_folder+"/Images/test.pdf",null);
//		drawScatter(new double[]{1,2,9},new double[]{5,6,7},"x","y",Config.getInstance().base_folder+"/Images/test.pdf",null);
//		drawScatterWLabels(new double[]{1,2,9},new double[]{5,6,7},new String[]{"a","b","c"},"x","y",Config.getInstance().base_folder+"/Images/test.pdf",null);

		
		List<double[]> l = new ArrayList<double[]>();
		l.add(new double[]{5,6,7,8});
		l.add(new double[]{1,2,9});
		l.add(new double[]{5,-1,4,10});
		
		List<String> names = new ArrayList<String>();
		names.add("ok1");
		names.add("ok2");
		names.add("ok3");
		
		//drawBar(new String[]{"a","b","c","d"},l,names,"types","x","y",Config.getInstance().base_folder+"/Images/test.pdf",null);
		//drawLine(new String[]{"3","2","1","0"},l,names,"types","x","y",Config.getInstance().base_folder+"/Images/test.pdf",null);
		drawBoxplot(l,names,"expression(avg[i]~R^{2}~'('~res[r]~','~res[i!=r]~')')","y",Config.getInstance().base_folder+"/Images/test.pdf",10,null);
		
		
		/*
		List<double[]> lx = new ArrayList<double[]>();
		lx.add(new double[]{5,6,7});
		lx.add(new double[]{1,2,7});
		lx.add(new double[]{5,-1,4});
		
		
		List<double[]> ly = new ArrayList<double[]>();
		ly.add(new double[]{5,6,7});
		ly.add(new double[]{1,2,7});
		ly.add(new double[]{5,-1,4});
		
		List<String> names = new ArrayList<String>();
		names.add("stadium1");
		names.add("stadium2");
		names.add("stadium3");
		
		drawScatter(lx,ly,names,"types","x","y",Config.getInstance().base_folder+"/Images/test.pdf",null);
		*/
		
//		double[] lat = new double[]{29.775,30.240,29.803};
//		double[] lon = new double[]{-93.649,-94.270,-94.418};
//		double[] lonlatBBox = new double[]{-96.5,28.7,-93.6,32.9};
//		
//		System.out.print("lat<-c(");
//		for(double l: lat)
//		 System.out.print(l+",");
//		System.out.println(");");
//			
//		System.out.print("lon<-c(");
//		for(double l: lon)
//		 System.out.print(l+",");
//		System.out.println(");");
//		
//		System.out.print("bbox<-c(");
//		for(double l: lonlatBBox)
//		 System.out.print(l+",");
//		System.out.println(");");
//		
//		drawHeatMap(lat,lon,lonlatBBox,Config.getInstance().base_folder+"/Images/map.pdf","Excursionist (in/out)");
		
/*		
		double[][] m = new double[20][20];
		for(int i=0; i<m.length;i++)
		for(int j=0; j<m[i].length;j++)
			m[i][j] = -1+2*Math.random();

		String[] headers = new String[m.length];
		for(int i=0; i<headers.length;i++)
			headers[i] = ""+i;
		
		drawCorr(Config.getInstance().base_folder+"/Images/corr.pdf",headers,m,"");
*/	
	}
}
