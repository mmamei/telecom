package cdraggregated;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import utils.Config;
import visual.r.RPlotter;

import com.jujutsu.tsne.FastTSne;
import com.jujutsu.tsne.MatrixOps;
import com.jujutsu.tsne.TSne;



public class TSNE {
	//https://lvdmaaten.github.io/tsne/
	static double perplexity = 5.0;
	private static int initial_dims = 20;
	
	public static boolean USE_Z = true;
	
	public static void main(String [] args) {
		
		
		for(String city: SynchAnalysis.CITIES)
			run(city);
	}
	
	public static void run(String city) {
		String filename = "C:/BASE/TIC2015/cache/single/"+city+"-Demo-callsLM_"+city.substring(0,2).toUpperCase()+"_COMUNI2012-resident-CLEAN"+(USE_Z?"Z":"")+".csv";
		TSne tsne = new FastTSne();
    	int iters = 2000;
    	System.out.println("Running " + iters + " iterations of TSne on " + filename);
        Object[] data_labels = readFile(filename); //MatrixUtils.simpleRead2DMatrix(new File(filename), ";");
         
         
         double [][] X = (double[][])data_labels[0];
         String[] labels = (String[])data_labels[1];
         
        //System.out.println("X:" + MatrixOps.doubleArrayToString(X));
        //X = MatrixOps.log(X, true);
        //System.out.println("X:" + MatrixOps.doubleArrayToString(X));
        X = MatrixOps.centerAndScale(X);
        System.out.println("Shape is: " + X.length + " x " + X[0].length);
        System.out.println("Starting TSNE: " + new Date());
        double [][] Y = tsne.tsne(X, 2, Math.min(initial_dims,X.length), perplexity, iters);
        System.out.println("Finished TSNE: " + new Date());
        //System.out.println("Result is = " + Y.length + " x " + Y[0].length + " => \n" + MatrixOps.doubleArrayToString(Y));
        System.out.println("Result is = " + Y.length + " x " + Y[0].length);
        //saveFile(new File("Java-tsne-result.txt"), MatrixOps.doubleArrayToString(Y));
        
        
        double[] x = new double[Y.length];
        double[] y = new double[Y.length];
        for(int i=0; i<x.length;i++) {
        	x[i] = Y[i][0];
        	y[i] = Y[i][1];
        }
        
        RPlotter.drawScatterWLabels(x, y, labels, "", "", Config.getInstance().base_folder+"/Images/tsne-"+city+".png", "");
        //saveArff(city,Y,labels);
	}
	
	
	public static Object[] readFile(String filename) {
		List<String> labels = new ArrayList<>();
		List<double[]> rows = new ArrayList<>();
		
        try (FileReader fr = new FileReader(filename)) {
            BufferedReader b = new BufferedReader(fr);
            String line;
            while ((line = b.readLine()) != null && !line.matches("\\s*")) {
                String[] cols = line.trim().split(";");
                double [] row = new double[cols.length-1];
                for (int j = 1; j < cols.length; j++) { // skip name of the region
                	if(!(cols[j].length()==0)) {
                		row[j-1] = Double.parseDouble(cols[j].trim());
                    }
                }
                labels.add(cols[0]);
                rows.add(row);
            }
            b.close();
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
        
        double[][] data_array = new double[rows.size()][];
        int currentRow = 0;
        for (double[] ds : rows) {
			data_array[currentRow++] = ds;
		}
        
        
        String[] string_array = new String[labels.size()];
        string_array = labels.toArray(string_array);
                
        return new Object[]{data_array,string_array};
	}
	
	public static void saveArff(String city, double [][] Y, String[] labels) {
		try {
			File file = new File(Config.getInstance().base_folder+"/TIC2015/cache/single/"+city+"-tsne.arff");
			PrintWriter out = new PrintWriter(new FileWriter(file));
			
			out.println("@RELATION "+city+"-tsne");
			
			out.println("@ATTRIBUTE region STRING");
			out.println("@ATTRIBUTE x NUMERIC");
			out.println("@ATTRIBUTE y NUMERIC");
			out.println("@DATA");
			
			for(int i=0; i<Y.length;i++)
				out.println(labels[i]+","+Y[i][0]+","+Y[i][1]);	
			out.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
}
