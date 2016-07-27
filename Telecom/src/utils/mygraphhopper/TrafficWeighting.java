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
	private HashMap<String, WEdge> busyEdges = new HashMap<String, WEdge>();
	private ArrayList<Integer> forbiddenEdges = new ArrayList<Integer>();
	private double stc=0.0;
	public TrafficWeighting( FlagEncoder encoder, HashMap<String, WEdge> busyEdges, ArrayList<Integer> forbiddenEdges, double stc){
		this.encoder = encoder;
		this.maxSpeed = encoder.getMaxSpeed();
		this.busyEdges = busyEdges;
		this.forbiddenEdges = forbiddenEdges;
		this.stc=stc;
	}
	
	public double getMinWeight( double distance ){
		return distance / (maxSpeed/3.6);
	}
		
	
	public double calcWeight( EdgeIteratorState edgeState, boolean reverse, int prevOrNextEdgeId)	{
//    	System.out.println("edgeState"+edgeState.getEdge());
		double speed = reverse ? encoder.getReverseSpeed(edgeState.getFlags()) : encoder.getSpeed(edgeState.getFlags());
		if (speed == 0){
			return Double.POSITIVE_INFINITY;
		}
		if(forbiddenEdges.contains(edgeState.getEdge())){
			return Double.POSITIVE_INFINITY;
		}

//		speed in Km/h
		
//		c=massimo flusso orario di macchine = secondi in un ora/((distanza di sicurezza + lunghezza di un auto)/velocità(metri al sec))
//		distanza di sicurezza= (vel(metri al sec)*3/10)
//		double c=3600*(speed/3.6)/(4.2+(speed*3/3.6)/10);
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
			if(speed>130) c*=3;
			else {
				c*=2;
			}
		}
		if(limit>=50||c>2000)	Math.min(c, 2000);
		if(limit>=70)	Math.min(c, 4000);
		if(limit>=130)	Math.min(c, 6000);
		
//		double c=3600*((speed/2)/3.6)/(4.2+((speed/2)*3/3.6)/10);
		if(busyEdges.containsKey(edgeState.getEdge()+"")){
//			System.out.println(c+"       "+edgeState.getDistance()+"     "+speed+"     "+busyEdges.get(edgeState.getEdge()).getWeight());
//    		System.out.println("tempo su strada sgombra: "+busyEdges.get(edgeState.getEdge()).getDistance()/(speed/3.6));
//    		System.out.println("tempo su strada intasata: "+(edgeState.getDistance()/(speed/3.6))*(1+0.15*Math.pow((busyEdges.get(edgeState.getEdge()).getWeight()/c),4)));
//			return (edgeState.fetchWayGeometry(2).calcDistance(new DistanceCalc3D())/(speed/3.6));
			
//			if((1+0.15*Math.pow((busyEdges.get(edgeState.getEdge()+"").getFlux(edgeState.getBaseNode())/c),4.0))>5)
//				System.out.println((1+0.15*Math.pow((busyEdges.get(edgeState.getEdge()+"").getFlux(edgeState.getBaseNode())/c),4.0))+" "+busyEdges.get(edgeState.getEdge()+"").getFlux(edgeState.getBaseNode())+" / "+c);	 
//			if(edgeState.getEdge()==2786404){
//				System.out.println(busyEdges.get(edgeState.getEdge()+"").getFlux(edgeState.getBaseNode())+" "+busyEdges.get(edgeState.getEdge()+"").getFlux(edgeState.getAdjNode()));
//			}
//			System.out.println(c+" speed: "+speed+" f: "+busyEdges.get(edgeState.getEdge()+"").getFlux(edgeState.getBaseNode()));
			
//			edgeState.getDistance();
//			busyEdges.get(edgeState.getEdge()+"").getFlux(edgeState.getBaseNode());
//			double time=((edgeState.getDistance()/(speed/3.6))*(1+0.15*Math.pow(((busyEdges.get(edgeState.getEdge()+"").getFlux(edgeState.getBaseNode())))/c,4.0)));
//			return time;
			return (edgeState.getDistance()/(speed/3.6))*(1+a*Math.pow(( busyEdges.get(edgeState.getEdge()+"").getFlux(edgeState.getBaseNode())/c),b));
			
//			if((time>(edgeState.getDistance()/(speed/3.6))*5)) return ((edgeState.getDistance()/(speed/3.6))*5);
//			else return time;
		}
		return (edgeState.getDistance()/(speed/3.6))*(1+a*Math.pow((this.stc*edgeState.getDistance()/c),b));
//		return ((edgeState.getDistance()/(speed/3.6))*(1+0.15*Math.pow(this.stc*edgeState.getDistance()/c,4.0)));
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


