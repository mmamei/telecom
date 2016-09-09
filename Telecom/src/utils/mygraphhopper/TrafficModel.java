package utils.mygraphhopper;

public class TrafficModel {

	
	public static double getTime(double distance, double speed, double flux) {
		double[] abc = TrafficModel.computeABC(speed);
		double a = abc[0];
		double b = abc[1];
		double c = abc[2];
		return ((distance/(speed/3.6))*(1+a*Math.pow((flux/c),b)));
	}
	
//	c=massimo flusso orario di macchine = secondi in un ora/((distanza di sicurezza + lunghezza di un auto)/velocità(metri al sec))
//	distanza di sicurezza= (vel(metri al sec)*3/10)
//	double c=3600*(speed/3.6)/(4.2+(speed*3/3.6)/10);
	
	private static double[] computeABC(double speed) {
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
		return new double[]{a,b,c};
	}
	
	
}
