package utils.mygraphhopper;

import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.PointList;

public class WEdge {
	
	private double weight1=0;
	private double weight2=0;
	private double capacity=2000;
	private EdgeIteratorState edge;

	
	public WEdge(EdgeIteratorState edge, double flow, double stc,boolean allDirections){
		this.edge=edge;
		if(allDirections)	this.weight2=flow+stc*edge.getDistance();;
		this.weight1=flow+stc*edge.getDistance();
	}
	public WEdge(EdgeIteratorState edge, double flowB, double flowA){
		this.edge=edge;
		this.weight1=flowA;
		this.weight2=flowB;
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
		return this.edge.getEdge();
	}
	
	public double getFlux(int baseNode) {
		if(baseNode==edge.getBaseNode())return weight1;
		else return weight2;
	}
	
	public WEdge setFlux(int baseNode, double weight) {
		if(baseNode==edge.getBaseNode())	this.weight1 = weight;
		else this.weight2 = weight;
		return this;
	}
	
	
	public WEdge addFlux(int baseNode, double weight) {
		if(baseNode==edge.getBaseNode()) this.weight1 += weight;
		else this.weight2 += weight;
		return this;
	}
	
	public PointList getPoints(){
		return this.edge.fetchWayGeometry(03);
	}
	

	
public double getCapicity(FlagEncoder encoder){
	return this.capacity;
}
	
public double getTime(Integer baseNode, FlagEncoder encoder)	{
		
//		double weight = getFlux(reverse);
//		double speed = reverse ? encoder.getReverseSpeed(edge.getFlags()) : encoder.getSpeed(edge.getFlags());
		double speed=encoder.getSpeed(edge.getFlags());
		double weight=getFlux(baseNode);
		
//		if(speed<=30)	speed=30;
//		else if(speed<=50)	speed=50;
//		else if(speed<=70)	speed=70;
//		else if(speed<=90)	speed=90;
//		else if(speed<=110)	speed=110;
//		else	speed=130;
		
//		speed in Km/h
//		if (speed == 0)	return Double.POSITIVE_INFINITY;
//		double c=3600*(speed/3.6)/(4.2+(speed*3/3.6)/10);
//		if(speed>=70){
//			if(speed>130) c*=3;
//			else c*=2;
//		}
		
		
		
//		double c=3600*((speed)/3.6)/(4.2+((speed)*3/3.6)/10);
//		if(speed>=50||c>2000)	c=2000;
//		else if(speed>=70)	c=4000;
//		else if(speed>=130)	c=6000;
//		
		
		
		double limit;
		if(speed<=30)	limit=30;
		else if(speed<=50)	limit=50;
		else if(speed<=70)	limit=70;
		else if(speed<=90)	limit=90;
		else if(speed<=110)	limit=110;
		else	limit=130;
		limit/=2;
		
		double a=0.7;
		double b=2.1;
		if(speed>=70){
			a=0.56;
			b=3.6; 
			if(speed>=70){
				a=0.71;
				b=2.10;
			}
			if(speed>=100){
				a=0.83;
				b=2.70;
			}
			if(speed>=130){
				a=1;
				b=5.40;
			}
		}
		double c=3600*((limit)/3.6)/(4.2+((speed/10)*(speed/10)));
//		http://www.dica.unict.it/Personale/Docenti/assets/006%20_Cenni.pdf pag22
		if(speed>=70){
			if(speed>=120) c*=3;
			else {
				c*=2;
			}
		}
		if(limit>=50||c>2000)	Math.min(c, 2000);
		if(limit>=70)	Math.min(c, 4000);
		if(limit>=130)	Math.min(c, 6000);
		this.capacity=c;
//		if(limit>=50||c>2000)	Math.min(c, 2000);
//		if(limit>=70)	Math.min(c, 4000);
//		if(limit>=130)	Math.min(c, 6000);
		
//		if((edge.getDistance()/(speed/3.6))>(edge.getDistance()/(speed/3.6))*(1+0.15*Math.pow((weight/c),4.0))){
//			System.err.println(1+0.15*Math.pow((weight/c),4.0)+" w= "+weight+" c= "+c);
//		}

//		return ((edge.getDistance()/(speed/3.6))*(1+0.15*Math.pow((weight/c),4.0)));
		return ((edge.getDistance()/(speed/3.6))*(1+a*Math.pow((weight/c),b)));

	}
	
	
	
	
	
	public String toString(){
		return this.edge.getEdge()+"\t"+weight1+" - "+edge.getBaseNode()+" "+edge.getAdjNode()+", "+weight2+" - "+edge.getAdjNode()+" "+edge.getBaseNode();
	}
}