package utils.mygraphhopper;

import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.PointList;

public class WEdge {
	
	private double weight=0;
	private int id;
	private EdgeIteratorState edge;
	
	public WEdge(EdgeIteratorState edge, double w){
		this.setWeight(w);
		this.id=edge.getEdge();
		this.edge=edge;
	}
	
	public String getVia(){
		return edge.getName();
	}
	
	public double getDistance(){
		return edge.getDistance();
	}
	
	public int getAdjNode(){
		return this.edge.getAdjNode();
	}
	
	public int getBaseNode(){
		return this.edge.getBaseNode();
	}
	
	public EdgeIteratorState getEdgeIteratorState(){
		return this.edge;
	}
	
	public int getId(){
		return this.id;
	}
	
	public double getWeight() {
		return weight;
	}
	
	public void setWeight(double weight) {
		this.weight = weight;
	}
	
	public void addWeight(double weight) {
		this.weight+=weight;
	}
	
	public PointList getPoints(){
		return this.edge.fetchWayGeometry(3);
	}
	public String toString(){
		return this.id+"\t"+this.edge.fetchWayGeometry(3)+"\t"+this.weight;
	}
}
