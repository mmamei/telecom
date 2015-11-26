package cdrindividual.user_place_recognizer.clustering;

import java.util.List;
import java.util.Map;

import cdrindividual.PLSEvent;
import cdrindividual.user_place_recognizer.Cluster;


public interface Clusterer {
	public Map<Integer, Cluster> buildCluster(List<PLSEvent> events);
}
