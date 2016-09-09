package utils.mygraphhopper;

import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.PointList;

public class WEdge {
	
	private double weight1=0;
	private double weight2=0;
	private EdgeIteratorState edge;

	public WEdge(EdgeIteratorState edge, double flow, double stc, boolean allDirections){
		this.edge=edge;
		this.weight1=flow+stc*edge.getDistance();
		if(allDirections) this.weight2=weight1;
		
	}
	public WEdge(EdgeIteratorState edge, double flowB, double flowA){
		this.edge=edge;
		this.weight1=flowA;
		this.weight2=flowB;
	}	
	
	public String getStreetName(){
		return edge.getName();
	}
	
	public double getDistance(){
		return edge.getDistance();
	}
	
	public int getAdjNode(){
		return edge.getAdjNode();
	}
	
	public int getBaseNode(){
		return edge.getBaseNode();
	}
	
	public EdgeIteratorState getEdgeIteratorState(){
		return edge;
	}
	
	public int getId(){
		return edge.getEdge();
	}
	
	public double getFlux(int baseNode) {
		if(baseNode==edge.getBaseNode())return weight1;
		else return weight2;
	}
	
	public double getFluxBothDirections() {
		return (weight1+weight2);
	}
	
	public WEdge setFlux(int baseNode, double weight) {
		if(baseNode==edge.getBaseNode())	
			weight1 = weight;
		else 
			weight2 = weight;
		return this;
	}
	
	
	public WEdge addFlux(int baseNode, double weight) {
		if(baseNode==edge.getBaseNode()) 
			weight1 += weight;
		else 
			weight2 += weight;
		return this;
	}
	
	public PointList getPoints(){
		return edge.fetchWayGeometry(3);
	}
	

	public double getTime(Integer baseNode, FlagEncoder encoder)	{
		double speed = encoder.getSpeed(edge.getFlags()); // speed in Km/h
		return TrafficModel.getTime(edge.getDistance(), speed, getFlux(baseNode));
	}

	
	public String toString(){
		return edge.getEdge()+"\t"+
			   weight1+" - "+edge.getBaseNode()+" "+edge.getAdjNode()+", "+
			   weight2+" - "+edge.getAdjNode()+" "+edge.getBaseNode();
	}
}