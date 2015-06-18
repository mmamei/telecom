package dataset;

import java.text.SimpleDateFormat;
import java.util.Locale;



public class DataFactory {
	

	public static PLSEventsAroundAPlacemarkI getPLSEventsAroundAPlacemark() {
		return dataset.impl.DataFactory.getPLSEventsAroundAPlacemark();
	}
	
	public static PLSCoverageSpaceI getPLSCoverageSpace() {
		return dataset.impl.DataFactory.getPLSCoverageSpace();
	}
	
	public static PLSCoverageTimeI getPLSCoverageTime() {
		return dataset.impl.DataFactory.getPLSCoverageTime();
	}
	
	public static EventFilesFinderI getEventFilesFinder() {
		return dataset.impl.DataFactory.getEventFilesFinder();
	}
	
	public static NetworkMapFactoryI getNetworkMapFactory() {
		return dataset.impl.DataFactory.getNetworkMapFactory();
	}
	
	
	public static UsersAroundAnEventI getUsersAroundAnEvent() {
		return dataset.impl.DataFactory.getUsersAroundAnEvent();
	}
	
	public static SimpleDateFormat getSimpleDateFormat() {
		return new SimpleDateFormat("yyyy/MMM/dd",Locale.US);
	}
}
