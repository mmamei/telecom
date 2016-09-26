package cdrindividual.dataset;

import cdrindividual.Constraints;
import region.Placemark;

public interface PLSEventsAroundAPlacemarkI {
	public void process(Placemark p, Constraints constraints) throws Exception;
}
