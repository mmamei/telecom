package d4d.afripop;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

import javax.imageio.ImageIO;

public class AfriPopMap {
	String title;
	public float[] data;
	public int width;
	public int height;
	public double south; 
	public double west;
	public double north; 
	public double east;
	public AfriPopMap(String title, float[] data, int width,int height,
				      double south, double west, double north, double east) {
		this.title = title;
		this.data = data;
		this.width = width;
		this.height = height;
		this.south = south;
		this.west = west;
		this.north = north;
		this.east = east;
	}
	
	
	/*
	// test add a frame to the image
	int c = 0;
	for(int j=0; j<m.height;j++) 
	for(int i=0; i<m.width;i++) {
		if(i== 0 || j== 0 || i == (m.width-1) || j== (m.height-1)) {
			m.data[c] = 0.59f;
		}
		c++;
	}
	*/
	
	public void createKMLOverlay() {
		try {
			String img = title+".png";
			createImage(img);
			generateKml(img,title+"-overlay.kml");
		} catch(Exception e) {
			e.printStackTrace();
		}
	
	}
	
	private void createImage(String file) throws Exception {
		int[] rgb = float2rgb(data);
		createImg(rgb,width,height,file);	
	}
	
	private void createImg(int[] rgb, int width, int height, String file) throws Exception {
		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		img.setRGB(0, 0, width, height, rgb, 0, width);
		ImageIO.write(img, "png", new File(file));
	}
	
	private void generateKml(String img,String kml) throws Exception {
		PrintWriter out = new PrintWriter(new FileWriter(new File(kml)));
		out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		out.println("<kml>");// xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2" xmlns:kml="http://www.opengis.net/kml/2.2" xmlns:atom="http://www.w3.org/2005/Atom">
		out.println("<GroundOverlay>");
		out.println("<name>"+kml.substring(0,kml.indexOf(".kml"))+"</name>");
		out.println("<color>50ffffff</color>");
		out.println("<Icon>");
		out.println("<href>"+img+"</href>");
		out.println("<viewBoundScale>0.75</viewBoundScale>");
		out.println("</Icon>");
		out.println("<LatLonBox>");
		out.println("<north>"+north+"</north>");
		out.println("<south>"+south+"</south>");
		out.println("<east>"+east+"</east>");
		out.println("<west>"+west+"</west>");
		out.println("</LatLonBox>");
		out.println("</GroundOverlay>");
		out.println("</kml>");
		out.close();
	}
	
	public static final int[][] COLOR = new int[][]{{255, 237, 237},{255, 224, 224},{255, 209, 209},{255, 193, 193},{255, 176, 176},{255, 159, 159},{255, 142, 142},{255, 126, 126},{255, 110, 110},{255, 94, 94},{255, 81, 81},{255, 67, 67},{255, 56, 56},{255, 46, 46},{255, 37, 37},{255, 29, 29},{255, 23, 23},{255, 18, 18},{255, 14, 14},{255, 11, 11},{255, 8, 8},{255, 6, 6},{255, 5, 5},{255, 3, 3},{255, 2, 2},{255, 2, 2},{255, 1, 1},{255, 1, 1},{255, 0, 0},{255, 0, 0},{255, 0, 0},{255, 0, 0},{255, 0, 0},{255, 0, 0},{255, 0, 0},{255, 0, 0},{255, 1, 0},{255, 4, 0},{255, 6, 0},{255, 10, 0},{255, 14, 0},{255, 18, 0},{255, 22, 0},{255, 26, 0},{255, 31, 0},{255, 36, 0},{255, 41, 0},{255, 45, 0},{255, 51, 0},{255, 57, 0},{255, 62, 0},{255, 68, 0},{255, 74, 0},{255, 81, 0},{255, 86, 0},{255, 93, 0},{255, 99, 0},{255, 105, 0},{255, 111, 0},{255, 118, 0},{255, 124, 0},{255, 131, 0},{255, 137, 0},{255, 144, 0},{255, 150, 0},{255, 156, 0},{255, 163, 0},{255, 169, 0},{255, 175, 0},{255, 181, 0},{255, 187, 0},{255, 192, 0},{255, 198, 0},{255, 203, 0},{255, 208, 0},{255, 213, 0},{255, 218, 0},{255, 222, 0},{255, 227, 0},{255, 232, 0},{255, 235, 0},{255, 238, 0},{255, 242, 0},{255, 245, 0},{255, 247, 0},{255, 250, 0},{255, 251, 0},{253, 252, 0},{250, 252, 1},{248, 252, 2},{244, 252, 2},{241, 252, 3},{237, 252, 3},{233, 252, 3},{229, 252, 4},{225, 252, 4},{220, 252, 5},{216, 252, 5},{211, 252, 6},{206, 252, 7},{201, 252, 7},{197, 252, 8},{191, 251, 8},{185, 249, 9},{180, 247, 9},{174, 246, 10},{169, 244, 11},{164, 242, 11},{158, 240, 12},{151, 238, 13},{146, 236, 14},{140, 233, 14},{134, 231, 15},{128, 228, 16},{122, 226, 17},{116, 223, 18},{110, 221, 19},{105, 218, 20},{99, 216, 21},{93, 214, 22},{88, 211, 23},{82, 209, 24},{76, 207, 25},{71, 204, 26},{66, 202, 28},{60, 200, 30},{55, 198, 31},{50, 196, 33},{45, 194, 34},{40, 191, 35},{36, 190, 37},{31, 188, 39},{27, 187, 40},{23, 185, 43},{19, 184, 44},{15, 183, 46},{12, 182, 48},{9, 181, 51},{6, 181, 53},{3, 180, 55},{1, 180, 57},{0, 180, 60},{0, 180, 62},{0, 180, 65},{0, 181, 68},{0, 182, 70},{0, 182, 74},{0, 183, 77},{0, 184, 80},{0, 184, 84},{0, 186, 88},{0, 187, 92},{0, 188, 95},{0, 190, 99},{0, 191, 104},{0, 193, 108},{0, 194, 112},{0, 196, 116},{0, 198, 120},{0, 200, 125},{0, 201, 129},{0, 203, 134},{0, 205, 138},{0, 207, 143},{0, 209, 147},{0, 211, 151},{0, 213, 156},{0, 215, 160},{0, 216, 165},{0, 219, 171},{0, 222, 178},{0, 224, 184},{0, 227, 190},{0, 229, 197},{0, 231, 203},{0, 233, 209},{0, 234, 214},{0, 234, 220},{0, 234, 225},{0, 234, 230},{0, 234, 234},{0, 234, 238},{0, 234, 242},{0, 234, 246},{0, 234, 248},{0, 234, 251},{0, 234, 254},{0, 234, 255},{0, 232, 255},{0, 228, 255},{0, 224, 255},{0, 219, 255},{0, 214, 254},{0, 208, 252},{0, 202, 250},{0, 195, 247},{0, 188, 244},{0, 180, 240},{0, 173, 236},{0, 164, 232},{0, 156, 228},{0, 147, 222},{0, 139, 218},{0, 130, 213},{0, 122, 208},{0, 117, 205},{0, 112, 203},{0, 107, 199},{0, 99, 196},{0, 93, 193},{0, 86, 189},{0, 78, 184},{0, 71, 180},{0, 65, 175},{0, 58, 171},{0, 52, 167},{0, 46, 162},{0, 40, 157},{0, 35, 152},{0, 30, 147},{0, 26, 142},{0, 22, 136},{0, 18, 131},{0, 15, 126},{0, 12, 120},{0, 9, 115},{1, 8, 110},{1, 6, 106},{1, 5, 101},{2, 4, 97},{3, 4, 92},{4, 5, 89},{5, 5, 85},{6, 6, 82},{7, 7, 79},{8, 8, 77},{10, 10, 77},{12, 12, 77},{14, 14, 76},{16, 16, 74},{19, 19, 73},{21, 21, 72},{24, 24, 71},{26, 26, 69},{29, 29, 70},{32, 32, 69},{35, 35, 68},{37, 37, 67},{40, 40, 67},{42, 42, 65},{44, 44, 65},{46, 46, 64},{48, 48, 63},{49, 50, 62},{51, 51, 61},{53, 52, 61}};
	
	private int[] float2rgb(float[] data) {
		int[] rgb = new int[data.length];
		
		// get min max
		float min = Float.MAX_VALUE;
		float max = -Float.MAX_VALUE;
		for(float d: data) {
			if(d != -9999) {
				min = Math.min(min,d);
				max = Math.max(max,d);
			}
		}
		
		
		System.out.println("MIN = "+min);
		System.out.println("MAX = "+max);
		
		for(int i=0; i<rgb.length; i++) {
			if(data[i] == -9999)
				rgb[i] = 0;	
			else {
				int index = 255 - (int)( 255.0*(data[i] - min)/(max-min) );
				rgb[i] = (COLOR[index][0] << 16) | (COLOR[index][1] << 8) | COLOR[index][2];
			}	
		}
		
		return rgb;
	}
}
