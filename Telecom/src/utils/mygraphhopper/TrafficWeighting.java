package utils.mygraphhopper;

import java.util.ArrayList;
import java.util.HashMap;

import com.graphhopper.routing.util.CarFlagEncoder;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.Weighting;
import com.graphhopper.util.EdgeIteratorState;

class TrafficWeighting implements Weighting {
	private final FlagEncoder encoder;
	private final double maxSpeed;
	private HashMap<Integer, WEdge> busyEdges = new HashMap<Integer, WEdge>();
	private ArrayList<Integer> forbiddenEdges = new ArrayList<Integer>();
	
	public TrafficWeighting( FlagEncoder encoder, HashMap<Integer, WEdge> busyEdges, ArrayList<Integer> forbiddenEdges){
		this.encoder = encoder;
		this.maxSpeed = encoder.getMaxSpeed();
		this.busyEdges = busyEdges;
		this.forbiddenEdges = forbiddenEdges;
	}
	
	public double getMinWeight( double distance )
	{
		return distance / (maxSpeed/3.6);
	}
	
	public double calcWeight( EdgeIteratorState edgeState, boolean reverse, int prevOrNextEdgeId )	{
		
//    	System.out.println("edgeState"+edgeState.getEdge());
		double speed = reverse ? encoder.getReverseSpeed(edgeState.getFlags()) : encoder.getSpeed(edgeState.getFlags());
		if (speed == 0)	return Double.POSITIVE_INFINITY;
		if(forbiddenEdges.contains(edgeState.getEdge())){
			return Double.POSITIVE_INFINITY;
		}
		if(busyEdges.containsKey(edgeState.getEdge())){
			
 //        	c=massimo flusso orario di macchine = secondi in un ora/((1/vel)*(distanza di sicurezza(= (vel(metri al sec)/10)*3) + (lunghezza auto(=4.2))))
			
			double c=3600/( (((speed*3)/(10)) + 4.2)*(1/(speed/3.6)) );
//    		System.out.println(c+"       "+edgeState.getDistance()+"     "+speed);
//    		System.out.println(getMinWeight(forbiddenEdges.get(edgeState.getEdge()).getDistance()));
//    		System.out.println((edgeState.getDistance()/(speed/3.6))*(1+0.15*Math.pow((forbiddenEdges.get(edgeState.getEdge()).getWeight()/c),4)));
//        	System.out.println(edgeState.getDistance() / (speed/3.6));
			return (edgeState.getDistance()/speed/3.6)*(1+0.15*Math.pow((busyEdges.get(edgeState.getEdge()).getWeight()/c),4.0));
		}
//     	System.out.println("not contained: "+edgeState.getEdge());
		
		return edgeState.getDistance() / (speed/3.6);
	}
	
	@Override
	public String toString()
	{
		return "TRAFFIC";
	}
	
	public FlagEncoder getFlagEncoder() {
		// TODO Auto-generated method stub
		return new CarFlagEncoder();
	}
}
