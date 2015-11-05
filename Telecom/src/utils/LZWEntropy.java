
package utils;

import java.util.HashSet;
import java.util.Set;


public class LZWEntropy {
    /** Compress a string to a list of output symbols. */
	public static double lzwEntropy(String x) {
        // Build the dictionary.
        
        
        double lambda = 0;
        Set<String> dictionary = new HashSet<String>();
        
       
        int start = 0;
        for(; start<x.length();start++) {
        	int i = start+1;
        	for(;i<x.length();i++) {
        		String tmp = x.substring(start, i);
        		  if (!dictionary.contains(tmp)) {
                      // Add wc to the dictionary.
                      dictionary.add(tmp);
                      //System.out.println(start+"-->"+tmp);
                      lambda += (i-start);
                      break;
                  }	  
        	}
        	if(i==x.length()) break;
        }
        
        double n = start;
        return n*log2(n)/lambda;
    }
 
   
	
	
	public static void main(String[] args) {
		run( new String[]{"a","b","c","d"},new double[]{0.25,0.25,0.25,0.25},10000);
		run( new String[]{"a","b"},new double[]{0.5,0.5},10000);
		run( new String[]{"a"},new double[]{0.1},1000);
	}
 
    public static void run(String[] alphabet, double[] p, int l) {
    	double s = lzwEntropy(rrep(alphabet,p,l));
    	System.out.println("Entropy = "+s);
    	System.out.println("Predictabiilty = "+predictability(s,alphabet.length));
    }
    
    
    public static String rep(String base, int r) {
    	String res = "";
    	for(int i=0; i<r;i++)
    		res += base;
    	return res;
    }
    
    public static String rrep(String[] s, double[] p, int r) {
    	
    	double[] cum = new double[p.length];
    	cum[0] = p[0];
    	//System.out.print("Cumulative: "+cum[0]);
    	for(int i=1; i<cum.length-1;i++) {
    		cum[i] = cum[i-1]+ p[i];
    		//System.out.print(" "+cum[i]);
    	}
    	cum[cum.length-1] = 1.0;
    	//System.out.print(" "+cum[cum.length-1]);
    	//System.out.println();
  
    	  	
    	String res = "";
    	for(int i=0; i<r;i++) {
    		double x = Math.random();
    		for(int k=0; k<cum.length; k++) 
    			if(x<=cum[k]) {
    				res+=s[k];
    				break;
    			}
    	}
    	return res;
    }
    
    // find-by-trials the maximum of fano function defined below
    public static double predictability(double s, double m) {
    	double maxp = 0.001;
    	for(double p=0.01; p<=1; p+=0.01)
    		if(Math.abs(fano(p,s,m)) < Math.abs(fano(maxp,s,m))) 
    				maxp = p;
    	return maxp;
    }
    
    
    public static double fano(double p, double s, double m) {
    	return -(p*log2(p) + (1-p)*log2(1-p)) + (1-p)*log2(m-1) - s;
    }
    
    public static double log2(double x) {
    	return Math.log(x) / Math.log(2);
    }
    
    
}