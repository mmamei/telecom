package utils.mod;
import java.util.ArrayList;
public class Route {
	private String da;
	private String a;
	
	private ArrayList<Double[]> flowTime=new ArrayList<Double[]>();
//	[0] = flusso, [1] = timeFastest, [2] = oldTime
	private ArrayList<Integer[][]> WEdgesPaths=new ArrayList<Integer[][]>();
//	una riga per ogni Wedge, prima colonna = id edge, seconda colonna = id baseNode
	private double[][] chrono;
//	una riga per ogni instradamento, prima colonna = flusso instradato, seconda = tempo con peso uguale a "fastest", terzo con peso uguale a "weight"
	
	private double mIstat;
	private double vIstat;
	private double mProject;
	private double vProject;
	private double mFastest;
	private double oldT;
	private Double values[];
	
	public Route (String da, String a){
		this.da=da;
		this.a=a;
	}

	public Route (String id){
		if(id.contains(":")) {
			String r[]=id.split(":");
			this.da=r[0];
			this.a=r[1];
		}
	}
	
	public Route addIter(Double t[], Integer[][] i){
		flowTime.add(t);
//		[0] = flusso, [1] = timeFastest, [2] = oldTime,
		WEdgesPaths.add(i);
		return this;
	}
	
	public ArrayList<Double[]> getFlowTime(){
		return this.flowTime;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

	public String getId(){
		return this.da+":"+a;
	}

	public Route setMediaIstat(double m){
		this.mIstat=m;
		return this;
	}

	public double getMediaIstat(){
		return this.mIstat;
	}

	public Route setVarianzaIstat(double m){
		this.vIstat=m;
		return this;
	}

	public double getVarianzaIstat(){
		return this.vIstat;
	}

	public Route setMediaProject(Double m){
		this.mProject=m;
		return this;
	}

	public double getMediaProject(){
		return this.mProject;
	}

	public void setVarianzaProject(double m){
		this.vProject=m;
	}

	public double getVarianzaProject(){
		return this.vProject;
	}
	

	public Route setMediaFastest(Double m){
		this.mFastest=m;
		return this;
	}

	public double getMediaFastest(){
		return this.mFastest;
	}
	public Route setMediaOldT(Double m){
		this.oldT=m;
		return this;
	}

	public double getMediaOldT(){
		return this.oldT;
	}
	
	public ArrayList<Integer[][]> getWEdgesPaths(){
		return this.WEdgesPaths;
	}
	
	public Route addPath(Integer[][] i){
		WEdgesPaths.add(i);
		return this;
	}
	
	public Route setChrono(double[][] c){
		this.chrono=c;
		return this;
	}
	
	public double[][] getChrono(){
		return this.chrono;
	}
	
	public double[] getW(){
		double[] a = new double[this.chrono.length];
		for(int i=0; i<this.chrono.length; i++)
			a[i]=this.chrono[i][0];
		return a;
	}
	
	public double[] getTFst(){
		double[] a = new double[this.chrono.length];
		for(int i=0; i<this.chrono.length; i++)
			a[i]=this.chrono[i][1];
		return a;
	}
	public double[] getOldTraf(){
		double[] a = new double[this.chrono.length];
		for(int i=0; i<this.chrono.length; i++)
			a[i]=this.chrono[i][2];
		return a;
	}
}

