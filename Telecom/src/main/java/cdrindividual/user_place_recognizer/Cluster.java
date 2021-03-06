package cdrindividual.user_place_recognizer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gps.utils.LatLonPoint;
import org.gps.utils.LatLonUtils;

import cdrindividual.CDR;
import cdrindividual.dataset.impl.DataFactory;
import region.RegionI;
import region.RegionMap;


public class Cluster implements Serializable {
	private List<CDR> events;
	private Map<String,Double> weights;
	
	private static RegionMap NM = null;
	
	public Cluster() {
		events = new ArrayList<CDR>();
		weights = new HashMap<String,Double>();
	}
	
	
	public int size() {
		return events.size();
	}
	
	public void addAll(Cluster c) {
		events.addAll(c.getEvents());
		if(NM == null) NM = DataFactory.getNetworkMapFactory().getNetworkMap(DataFactory.getNetworkMapFactory().getCalendar(c.getEvents().get(0).getTime()));
	}
	
	public void add(CDR e) {
		events.add(e);
		if(NM == null) NM = DataFactory.getNetworkMapFactory().getNetworkMap(e.getTimeStamp());
	} 
	
	public List<CDR> getEvents() {
		return events;
	}
	
	public void addWeight(String label, double w) {
		weights.put(label, w);
	}
	
	public double getWeight(String label) {
		return weights.get(label);
	}
	
	public double totWeight() {
		double weight = 0;
		for(double w: weights.values())
			weight += w;
		return weight;
	}
	
	public double w(CDR e, double[][] weights) {
		Calendar c = e.getCalendar();
		int day = c.get(Calendar.DAY_OF_WEEK) - 1;
		int h = c.get(Calendar.HOUR_OF_DAY);
		return weights[day][h];
	}
	
	public double w(double[][] weights) {
		double w = 0;
		for(CDR e: events)
			w += w(e,weights);
		return w;
	}
	
	public double getAvgCellRadius(double[][] weights) {
		double tot_w = w(weights);
		double r = 0;
		for(CDR e: events) {
			if(NM == null) NM = DataFactory.getNetworkMapFactory().getNetworkMap(e.getTimeStamp());
			RegionI x = NM.getRegion(e.getCellac());
			if(x!=null) {
				double f = tot_w > 0 ? w(e,weights) : 1;
				r += x.getRadius() * f;
			}
		}
		double den = tot_w > 0 ? tot_w : size();
		return r / den;
	}
	
	public LatLonPoint getCenter(double[][] weights) {
		double aLat = 0;
		double aLon = 0;
		double tot_w = w(weights);
		
		boolean ok = false;
		for(CDR e: events) {
			if(NM == null) NM = DataFactory.getNetworkMapFactory().getNetworkMap(e.getTimeStamp());
			RegionI x = NM.getRegion(e.getCellac());
			if(x!=null) {
				ok = true;
				double f = tot_w > 0 ? w(e,weights) : 1;
				aLat += x.getLatLon()[0] * f;
				aLon += x.getLatLon()[1] * f;
			}
		}		
		
		if(!ok) return null;
		
		double den = tot_w > 0 ? tot_w : size();
		
		aLat = aLat / den;
		aLon = aLon / den;
		
		return new LatLonPoint(aLat, aLon);
	}
	
	public double calcWidth() {
		double maxLat= -Double.MAX_VALUE, maxLon= -Double.MAX_VALUE;
		double minLat = Double.MAX_VALUE, minLon = Double.MAX_VALUE;
		for(CDR e: events){
			if(NM == null) NM = DataFactory.getNetworkMapFactory().getNetworkMap(e.getTimeStamp());
			RegionI cell = NM.getRegion(String.valueOf(e.getCellac()));
			if(cell!=null) {
				maxLat = Math.max(maxLat, cell.getLatLon()[0]);
				maxLon = Math.max(maxLon, cell.getLatLon()[1]);
				minLat = Math.min(minLat, cell.getLatLon()[0]);
				minLon = Math.min(minLon, cell.getLatLon()[1]);
			}
		}
		LatLonPoint p1 = new LatLonPoint(maxLat, maxLon);
		LatLonPoint p2 = new LatLonPoint(minLat, minLon);
		
		return LatLonUtils.getHaversineDistance(p1, p2);
	}
	
	public double getAgglomerateWidth(Cluster c2) {
		Cluster temp = new Cluster();
		temp.addAll(this);
		temp.addAll(c2);
		return temp.calcWidth();
	}
	
	
}
