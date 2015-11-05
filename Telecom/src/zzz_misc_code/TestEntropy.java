package zzz_misc_code;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

import utils.GrangerTest;
import JavaMI.Entropy;

public class TestEntropy {
	public static void main(String[] args) {
		
		
		double[] a = new double[1000];
		double[] b = new double[a.length];
		for(int i=0; i<a.length;i++) {
			a[i] = Math.ceil( 10 * (1+Math.sin(0.05 * i))/2 );
			b[i] = Math.ceil( 10 * (1+Math.sin(2 * Math.PI * Math.random()))/2 );
		}
		
		
		print("a",a);
		
		System.out.println("Entropy a = "+Entropy.calculateEntropy(a));
		System.out.println("Entropy b = "+Entropy.calculateEntropy(b));
		
		int lag = 1;
		double[][] la = createLagged(a,lag);
		double[][] lb = createLagged(b,lag);
		
		System.out.println("C Entropy a (lag="+lag+") = "+Entropy.calculateConditionalEntropy(la[0], la[1]));
		System.out.println("C Entropy b (lag="+lag+") = "+Entropy.calculateConditionalEntropy(lb[0], lb[1]));
		
	}
	
	
	public static double[][] createLagged(double[] a, int lag) {
		double[][] la = new double[2][a.length-lag];

		System.arraycopy(a, lag, la[0], 0, la[0].length);

		System.arraycopy(a, 0, la[1], 0, la[1].length);
		return la;
	}
	
	
	
	static void print(String t, double[] x) {
		System.out.print(t+": ");
		for(int i=0; i<x.length;i++) 
				System.out.print(x[i]+" ");
			System.out.println();
	}
	
	
}
