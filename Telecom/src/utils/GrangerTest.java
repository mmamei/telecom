package utils;

import org.apache.commons.math3.distribution.FDistribution;
import org.apache.commons.math3.stat.regression.MultipleLinearRegression;
import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;

/**
 * Implementation of Granger causality test in pure Java.
 * It is base on linear regression implementation in Apache Commons Math.
 *
 * User: Sergey Edunov
 * Date: 06.01.11
 */
public class GrangerTest {

    /**
     * Returns p-value for Granger causality test.
     *
     * @param y - predictable variable
     * @param x - predictor
     * @param L - lag, should be 1 or greater.
     * @return p-value of Granger causality
     */
    public static double[] granger(double[] y, double[] x, int L){
        OLSMultipleLinearRegression h0 = new OLSMultipleLinearRegression();
        OLSMultipleLinearRegression h1 = new OLSMultipleLinearRegression();

        double[][] laggedY = createLaggedSide(L, y);

        double[][] laggedXY = createLaggedSide(L, x, y);

        int n = laggedY.length;
        
        h0.setNoIntercept(true);
        h1.setNoIntercept(true);

        
        h0.newSampleData(strip(L, y), laggedY);
        h1.newSampleData(strip(L, y), laggedXY);
        
        
        //System.out.println("R^2 = "+h0.calculateRSquared());
        //System.out.println("R^2 = "+h1.calculateRSquared());
        
        double rs0[] = h0.estimateResiduals();
        double rs1[] = h1.estimateResiduals();


        double RSS0 = sqrSum(rs0);
        double RSS1 = sqrSum(rs1);

        double ftest = ((RSS0 - RSS1)/L) / (RSS1 / ( n - 2*L - 1));

        //System.out.println("RSS0 = "+RSS0 + "\nRSS1 = " + RSS1);
        //System.out.println("F-test " + ftest);

        FDistribution fDist = new FDistribution(L, n-2*L-1);
        try {
            double pValue = 1.0 - fDist.cumulativeProbability(ftest);
            //System.out.println("P-value " + pValue);
            return  new double[]{pValue,ftest,h0.calculateRSquared(),h1.calculateRSquared()};
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }


    private static double[][] createLaggedSide(int L, double[]... a) {
        int n = a[0].length - L;
        double[][] res = new double[n][L*a.length+1];
        for(int i=0; i<a.length; i++){
            double[] ai = a[i];
            for(int l=0; l<L; l++){
                for(int j=0; j<n; j++){
                    res[j][i*L+l] = ai[l+j];
                }
            }
        }
        for(int i=0; i<n; i++){
            res[i][L*a.length] = 1;
        }
        return res;
    }

    public static double sqrSum(double[] a){
        double res = 0;
        for(double v : a){
            res+=v*v;
        }
        return res;
    }

     // removes first l elements from the array
     public static double[] strip(int l, double[] a){

        double[] res = new double[a.length-l];
        System.arraycopy(a, l, res, 0, res.length);
        return res;
    }
     
     
     public static void main(String[] args) throws Exception {
    	 
    	 double[] z = new double[10];
    	 for(int i=0; i<z.length;i++)
    		 z[i] = i;
    	 double[][] lz = createLaggedSide(2,z);
    	 
    	 for(int i=0; i<lz.length;i++) {
    		 for(int j=0; j<lz[i].length;j++)
    			 System.out.print((int)lz[i][j]+" ");
    		 System.out.println();
    	 }
    	 
    	 System.exit(0);
    	 
    	 double[] x = new double[20];
    	 double[] y = new double[20];
    	 for(int i=0; i<x.length;i++) {
    		 x[i] = 10+i+Math.random();
    		 y[i] = 2*i+Math.random();
    		 //System.out.println(x[i]+", "+y[i]);
    	 }
    	 granger(y,x,4);
     }
     
     /*
     public static void main2(String[] args) throws Exception {
    	 OLSMultipleLinearRegression ols = new OLSMultipleLinearRegression();
    	 
    	 double[] y = new double[100];
    	
    	 for(int i=0;i<y.length;i++) 
    		 y[i] = Math.random();
    		
    	 double[][] x = createLaggedSide(1,y);
    	 ols.setNoIntercept(true);
    	 
    	 ols.newSampleData(strip(1,y),x);
    	 System.out.println("r^2 = "+ols.calculateRSquared());
    	  
    	 double[] coe = ols.estimateRegressionParameters(); // 4
    	 for(double d : coe)
    		 System.out.print(d + " ");
    	 System.out.println();
     }
     */
}