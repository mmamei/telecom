package cdraggregated.densityANDflows.flows;



import java.util.Arrays;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;


public class ODMatrixGMapsImgComparator {

	static int SEARCH_RANGE = 5;
	static final double TOLERANCE = 40;
	static final double[] null_color = new double[]{255,255,255};
	static final double[] green = new double[]{80,202,132};
	static final double[] orange = new double[]{7,112,209};
	static final double[] red = new double[]{0,0,230};
	static final double[] darkred = new double[]{19,19,158};
	static final double[][] ok_colors = new double[][]{null_color,green,orange,red,darkred};
		
	
	public static void main(String[] args) throws Exception {
		//String result = "C:/Users/Marco/Google Drive/Desktop/mo1.png";
		//String gt = "C:/Users/Marco/Google Drive/Desktop/mo2.png";
				
		//String result = "C:/Users/Marco/Google Drive/Desktop/re1.png";
		//String gt = "C:/Users/Marco/Google Drive/Desktop/re2.png";
				
		String result = "C:/BASE/Francia/img/mi2.png";
		String gt = "C:/BASE/Francia/img/mi1.png";
		int zoom = 11;
		
		int[][] confusion = compare(result,gt);
		printConfusionMatrix(confusion,zoom);
	}
	
	
	public static int[][] compare(String result, String gt) throws Exception {
					
		System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
	    	
		Mat img_result = Highgui.imread(result,Highgui.CV_LOAD_IMAGE_COLOR);
	    Mat img_gt = Highgui.imread(gt,Highgui.CV_LOAD_IMAGE_COLOR);
	    
	    img_result = extractRoards(img_result);
	    img_gt = extractRoards(img_gt);
	    
	    System.out.println("img_result = "+img_result.rows()+" * "+img_result.cols());
		System.out.println("img_gt = "+img_gt.rows()+" * "+img_gt.cols());
		
		double min = Double.MAX_VALUE;
		int[][] min_confusion = null;
		int mdx = 0,mdy = 0;
		for(int dx= -SEARCH_RANGE; dx<=SEARCH_RANGE; dx++) {
			System.out.print(dx+" ");
			for(int dy= -SEARCH_RANGE; dy<=SEARCH_RANGE; dy++) {
				int[][] confusion = confusion(img_result,img_gt,dx,dy);
				double c = error(confusion);
				if(min > c) {
					min = c;
					min_confusion = confusion;
					mdx = dx;
					mdy = dy;
				}
			}
		}
		System.out.println();
		System.out.println("alignment = ["+mdx+","+mdy+"], error = "+(int)Math.round(min));
		drawResultingImages(result,gt,img_result,img_gt,mdx,mdy);
		
		return min_confusion;
	}	
	
	/*
	 * pretty print confusion matrix.
	 * Converts absolute numbers (numer of pixels, into km taking into account map zoom to pixel per meters ratio
	 */
	public static void printConfusionMatrix(int[][] cm, int zoom) {
		double mpx = 156543.03392 * Math.cos(44 * Math.PI / 180) / Math.pow(2, zoom);
		System.out.println("confusion matrix (streets Km):");
		String[] labels = new String[]{"null","green","orange","red","darkred"};
		System.out.print("res\\gt");
		for(int i=0; i<labels.length;i++)
			System.out.print("\t"+labels[i]);
		System.out.println();
		for(int i=0; i<labels.length;i++) {
			System.out.print(labels[i]);
			for(int j=0; j<labels.length;j++)
				if(i==0 && j==0) System.out.print("\t ");
				else System.out.print("\t"+(int)Math.round(mpx * cm[i][j] / 1000));
			System.out.println();
		}
	}
	
	
	/* 
	 * draw final aligned road maps
	 */
	private static void drawResultingImages(String result, String gt, Mat img_result, Mat img_gt, int mdx, int mdy) {
		int[] rows_cols = getOverlappingRowsAndCols(img_result,img_gt,mdx,mdy);
		int rows = rows_cols[0];
		int cols = rows_cols[1];
		Mat oimg1 = new Mat(rows,cols,img_result.type());
		Mat oimg2 = new Mat(rows,cols,img_gt.type());
		
		for(int i=0;i<rows;i++)
		for(int j=0;j<cols;j++) {
			int[] i1j1i2j2 = geti1j1i2j2(i,j,mdx,mdy);
			oimg1.put(i, j, img_result.get(i1j1i2j2[0], i1j1i2j2[1]));
			oimg2.put(i, j, img_gt.get(i1j1i2j2[2], i1j1i2j2[3]));
		}
		Highgui.imwrite(result.replaceAll(".png", "_trimmed.png"), oimg1);
		Highgui.imwrite(gt.replaceAll(".png", "_trimmed.png"), oimg2);	
	}
	
	
	/*
	 * computes confusion matrix by counting non-matching pixels
	 */
	
	private static int[][] confusion(Mat result, Mat gt, int dx, int dy) {
		
		int[] rows_cols = getOverlappingRowsAndCols(result,gt,dx,dy);
		int rows = rows_cols[0];
		int cols = rows_cols[1];
		
		//System.out.println("Overlap = "+rows+" * "+cols);
		
		int[][] confusion = new int[5][5];
		for(int i=0;i<rows;i++)
		for(int j=0;j<cols;j++) {
			int[] i1j1i2j2 = geti1j1i2j2(i,j,dx,dy);
			double[] v = result.get(i1j1i2j2[0], i1j1i2j2[1]);
			double[] w = gt.get(i1j1i2j2[2], i1j1i2j2[3]);
			confusion[color2index(v)][color2index(w)]++;
		}
		return confusion;
	}
	
	
	/*
	 * converts a color into the corresponding index.
	 * null 	==> 0
	 * green 	==> 1
	 * orange 	==> 2
	 * ...
	 */
	private static int color2index(double[] v){
		for(int i=0; i<ok_colors.length;i++)
			if(Arrays.equals(v, ok_colors[i]))
				return i;
		return -1;
	}
	
	/*
	 * This method computes the size of the resulting overlapping images.
	 */
	private static int[] getOverlappingRowsAndCols(Mat img1, Mat img2, int dx, int dy) {
		int rows = Math.min(img1.rows(), img2.rows());
		int cols = Math.min(img1.cols(), img2.cols());
		
		if(dx < 0) cols = Math.min(img1.cols(), img2.cols()-Math.abs(dx));
		if(dx > 0) cols = Math.min(img1.cols()-Math.abs(dx), img2.cols());
		
		if(dy < 0) rows = Math.min(img1.rows(), img2.rows()-Math.abs(dy));
		if(dy > 0) rows = Math.min(img1.rows()-Math.abs(dy), img2.rows());
		
		return new int[]{rows,cols};
	}
	
	
	/*
	 * This method allows to compute displacements in the pixels' indexes of the input images.
	 * the pixel (i,j) of the resulting image (i.e., aligned images) [computed using dx and dy displacements].
	 * is (i1,j1) for the 1st image, (i2,j2) for the 2nd image.
	 * These indexes are in the i1j1i2j2[] array.
	 */
	
	private static int[] geti1j1i2j2(int i, int j, int dx, int dy) {
		int i1 = i;
		int j1 = j;
		int i2 = i;
		int j2 = j;
		
		if(dx < 0) j2+=Math.abs(dx);
		if(dx > 0) j1+=Math.abs(dx);
		if(dy < 0) i2+=Math.abs(dy);
		if(dy > 0) i1+=Math.abs(dy);
		
		
		return new int[]{i1,j1,i2,j2};
	}
	
	
	/*
	
			null	green	orange	red	darkred
	null	1000	10		20		20	1
	green 	...
	orange
	red
	darkred 
	*/
	
	
	/* the following two methods extract an error number from the resulting confusion matrix.
	 * The result is used to align two maps (best alignment = lowest error).
	 * color_error estimates considering the amount of traffic. Therefore green-red errors are larger than orange-red
	 * bw_error considers only that a road in a map is a road also in the other map
	 */
	
	private static double error(int[][] cm) {
		//return color_error(cm);
		return bw_error(cm);
	}
	
	private static double color_error(int[][] cm) {
		double error = 0;
		double sum = 0;
		for(int i=0; i<cm.length;i++)
		for(int j=0; j<cm.length;j++) {
			error += cm[i][j] * 10 * Math.abs(i-j);
			sum += cm[i][j];
		}
		return error/(sum-cm[0][0]);
	}
	
	// bw error
	private static double bw_error(int[][] cm) {
		double error = 0;
		for(int i=1; i<cm.length;i++) 
			error+=cm[i][0]+cm[0][i];
		return error;
	}
	
	/*
	 * This method extracts only roads with traffic from the map 
	 */
	private static Mat extractRoards(Mat source) {
		Mat dest = new Mat(source.rows(),source.cols(),source.type());		
	    for(int i=0; i<source.rows();i++)
	    for(int j=0; j<source.cols();j++) {
	    	double[] v = source.get(i, j);
	    	//System.out.println(v[0]+" "+v[1]+" "+v[2]);
	    	dest.put(i, j, alignColor(v));
	    }
	    return dest;
	}

	/*
	 * Given a color x[] in bgr format. 
	 * This method returns the most similar ok_color (google: green, orange, red, dark red) or 
	 * the null color if x is very far from all the colors.
	 */
	
	private static double[] alignColor(double[] x) {
		for(int i=1;i<ok_colors.length;i++) 
			if(Math.abs(x[0] - ok_colors[i][0]) < TOLERANCE &&
			   Math.abs(x[1] - ok_colors[i][1]) < TOLERANCE &&
			   Math.abs(x[2] - ok_colors[i][2]) < TOLERANCE) 
				return ok_colors[i]; 
		return null_color;
	}
}