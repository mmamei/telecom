package cdrindividual.dataset.impl;

import cdrindividual.dataset.EventFilesFinderI;
import cdrindividual.dataset.NetworkMapFactoryI;
import cdrindividual.dataset.PLSCoverageSpaceI;
import cdrindividual.dataset.PLSCoverageTimeI;
import cdrindividual.dataset.PLSEventsAroundAPlacemarkI;
import cdrindividual.dataset.UsersAroundAnEventI;

public class DataFactory {
	
	public static PLSEventsAroundAPlacemarkI getPLSEventsAroundAPlacemark() {
		return new PLSEventsAroundAPlacemark();
	}
	
	public static PLSCoverageSpaceI getPLSCoverageSpace() {
		return new PLSCoverageSpace();
	}
	
	public static PLSCoverageTimeI getPLSCoverageTime() {
		return new PLSCoverageTime();
	}
	
	public static EventFilesFinderI getEventFilesFinder() {
		return new EventFilesFinder();
	}
	
	public static NetworkMapFactoryI getNetworkMapFactory() {
		return NetworkMapFactory.getInstance();
	}
	
	public static UsersAroundAnEventI getUsersAroundAnEvent() {
		return new UsersAroundAnEvent();
	}
}
