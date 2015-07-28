package utils;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.DBSCANClusterer;
import org.apache.commons.math3.ml.clustering.DoublePoint;
import org.apache.commons.math3.ml.distance.DistanceMeasure;
import org.gps.utils.LatLonPoint;
import org.gps.utils.LatLonUtils;

public class DBScan {
	
	public static void main(String[] args) throws Exception {
		
		List<DoublePoint> points = new ArrayList<DoublePoint>();
		for(int i=0; i<100;i++)
			points.add(new DoublePoint(new double[]{5+Math.random(),5+Math.random()}));
		for(int i=0; i<100;i++)
			points.add(new DoublePoint(new double[]{15+Math.random(),15+Math.random()}));
		
		//DBSCANClusterer<DoublePoint> dbscan = new DBSCANClusterer<DoublePoint>(1, 5);
		DBSCANClusterer<DoublePoint> dbscan = new DBSCANClusterer<DoublePoint>(1, 5, new DistanceMeasure(){
			public double compute(double[] latlon1, double[] latlon2) {
				LatLonPoint p1 = new LatLonPoint(latlon1[0],latlon1[1]);
				LatLonPoint p2 = new LatLonPoint(latlon2[0],latlon2[1]);
				return LatLonUtils.getHaversineDistance(p1, p2);
			}
		});
	    List<Cluster<DoublePoint>> cluster = dbscan.cluster(points);
	    
	    System.out.println(cluster.size());
	    for(Cluster<DoublePoint> c: cluster){
	        System.out.println(c.getPoints().get(0));
	    }                       
	}	
}
