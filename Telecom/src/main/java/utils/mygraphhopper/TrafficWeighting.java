package utils.mygraphhopper;



import java.util.List;
import java.util.Map;

import com.graphhopper.routing.util.CarFlagEncoder;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.Weighting;
import com.graphhopper.util.EdgeIteratorState;


class TrafficWeighting implements Weighting {
	
	private FlagEncoder encoder;
	private double maxSpeed;
	private Map<String, WEdge> busyEdges;
	private double stc;
	
	public TrafficWeighting( FlagEncoder encoder, Map<String, WEdge> busyEdges, double stc){
		this.encoder = encoder;
		this.maxSpeed = encoder.getMaxSpeed();
		this.busyEdges = busyEdges;
		this.stc=stc;
	}
	
	
	// distance is in meters
	// maxSpeed is in Km/h
	// maxSpeed/3.6 is speed in m/s
	// the method returns the minimum travel time in seconds
	public double getMinWeight(double distance){
		return distance / (maxSpeed / 3.6);
	}
		
	//  the method returns the actual travel time in seconds
	@Override
	public double calcWeight(EdgeIteratorState edgeState, boolean reverse, int prevOrNextEdgeId)	{
		// speed in Km/h
		double speed = reverse ? encoder.getReverseSpeed(edgeState.getFlags()) : encoder.getSpeed(edgeState.getFlags());
		if (speed == 0)
			return Double.POSITIVE_INFINITY;
		
		double distance = edgeState.getDistance();
		String e = String.valueOf(edgeState.getEdge());
		double flow = busyEdges.containsKey(e) ? busyEdges.get(e).getFlux(edgeState.getBaseNode()) : stc * distance;
		return TrafficModel.getTime(distance, speed, flow); 
	}
	
	@Override
	public String toString() {
		return "TRAFFIC";
	}
	@Override
	public FlagEncoder getFlagEncoder() {
		return new CarFlagEncoder();
	}
}


