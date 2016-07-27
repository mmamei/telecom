package utils.mygraphhopper;



import com.graphhopper.GHResponse;



public class MyGHResponse extends GHResponse{

	private double timeTot=0.0;
	private String str="";
	private Integer[][] path;
	private boolean delay=false;
	
	public MyGHResponse setPath(Integer[][] path){
		this.path=path;
		return this;
	}
	
	public Integer[][] getPath(){
		return this.path;
	}

	public MyGHResponse setTimeTot(double timeTot){
		this.timeTot=timeTot;
		return this;
	}

	public double getTimeTot(){
		return this.timeTot;
	}

	public MyGHResponse setDelayInfo(String str){
		this.str=str;
		this.delay=true;
		return this;
	}
	
	public boolean isDelayed(){
		return this.delay;
	}

	public String getDelayInfo(){
//		DA_LAT-DA_LON-A_LAT-A_LON-FLUX-ORARIO
		return str;
	}

}