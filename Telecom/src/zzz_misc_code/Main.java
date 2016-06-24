package zzz_misc_code;

import org.gps.utils.LatLonPoint;
import org.gps.utils.LatLonUtils;

public class Main {

	public static void main(String[] args) {
		
		LatLonPoint p1 = new LatLonPoint(44.1,10.2);
		LatLonPoint p2 = new LatLonPoint(44.2,10.3);
		double d = LatLonUtils.getHaversineDistance(p1, p2);
		System.out.println(d);
		
	}

}
