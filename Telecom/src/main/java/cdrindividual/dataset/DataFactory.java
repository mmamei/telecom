package cdrindividual.dataset;

import java.text.SimpleDateFormat;
import java.util.Locale;

import network.NetworkMapFactoryI;



public class DataFactory {
	

	public static PLSEventsAroundAPlacemarkI getPLSEventsAroundAPlacemark() {
		return cdrindividual.dataset.impl.DataFactory.getPLSEventsAroundAPlacemark();
	}
	
	public static PLSCoverageSpaceI getPLSCoverageSpace() {
		return cdrindividual.dataset.impl.DataFactory.getPLSCoverageSpace();
	}
	
	public static PLSCoverageTimeI getPLSCoverageTime() {
		return cdrindividual.dataset.impl.DataFactory.getPLSCoverageTime();
	}
	
	public static EventFilesFinderI getEventFilesFinder() {
		return cdrindividual.dataset.impl.DataFactory.getEventFilesFinder();
	}
	
	public static NetworkMapFactoryI getNetworkMapFactory() {
		return cdrindividual.dataset.impl.DataFactory.getNetworkMapFactory();
	}
	
	
	public static UsersAroundAnEventI getUsersAroundAnEvent() {
		return cdrindividual.dataset.impl.DataFactory.getUsersAroundAnEvent();
	}
	
	public static SimpleDateFormat getSimpleDateFormat() {
		return new SimpleDateFormat("yyyy/MMM/dd",Locale.US);
	}
}
