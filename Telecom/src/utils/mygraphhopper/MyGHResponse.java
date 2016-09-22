package utils.mygraphhopper;

import com.graphhopper.GHResponse;

public class MyGHResponse extends GHResponse {
	long mytime;
	
	public MyGHResponse() {
		super();
	}
	
	@Override
	public long getTime() {
		return mytime;
		
	}
}
