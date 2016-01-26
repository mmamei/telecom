package cdraggregated;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.JFrame;

import org.math.plot.FrameView;
import org.math.plot.Plot2DPanel;
import org.math.plot.plots.ScatterPlot;

import com.jujutsu.tsne.FastTSne;
import com.jujutsu.tsne.MatrixOps;
import com.jujutsu.tsne.TSne;



public class TSNE {
	static double perplexity = 5.0;
	private static int initial_dims = 40;
	
	public static void main(String [] args) {
		
		String[] cities = new String[]{
				"caltanissetta",
				"siracusa",
				"benevento",
				//"palermo",
				"campobasso",
				//"napoli",
				"asti",
				//"bari",
				"ravenna",
				"ferrara",
				//"venezia",
				//"torino",
				"modena",
				//"roma",
				"siena",
				//"milano"
		};
		
		for(String city: cities)
			run(city);
	}
	
	public static void run(String city) {
		String filename = "C:/BASE/TIC2015/cache/single/"+city+"-Demo-callsLM_"+city.substring(0,2).toUpperCase()+"_COMUNI2012-resident-CLEAN.csv";
		TSne tsne = new FastTSne();
    	int iters = 2000;
    	System.out.println("Running " + iters + " iterations of TSne on " + filename);
        double [][] X = readFile(filename); //MatrixUtils.simpleRead2DMatrix(new File(filename), ";");
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
        
        
        Plot2DPanel plot = new Plot2DPanel();
        
        ScatterPlot setosaPlot = new ScatterPlot("setosa", Color.BLACK, Y);
        plot.plotCanvas.setNotable(true);
        plot.plotCanvas.setNoteCoords(true);
        plot.plotCanvas.addPlot(setosaPlot);
                
        FrameView plotframe = new FrameView(city,plot);
        plotframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        plotframe.setVisible(true);
        
	}
	
	
	public static double[][] readFile(String filename) {
		List<double[]> rows = new ArrayList<>();
		
        try (FileReader fr = new FileReader(filename)) {
            BufferedReader b = new BufferedReader(fr);
            String line;
            while ((line = b.readLine()) != null && !line.matches("\\s*")) {
                String[] cols = line.trim().split(";");
                double [] row = new double[cols.length];
                for (int j = 1; j < cols.length; j++) { // skip name of the region
                	if(!(cols[j].length()==0)) {
                		row[j] = Double.parseDouble(cols[j].trim());
                    }
                }
                rows.add(row);
            }
            b.close();
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
        
        double[][] array = new double[rows.size()][];
        int currentRow = 0;
        for (double[] ds : rows) {
			array[currentRow++] = ds;
		}
                
        return array;
	}
}
