package cdrindividual.user_place_recognizer.clustering;

import java.util.List;
import java.util.HashMap;

import cdrindividual.CDR;
import cdrindividual.user_place_recognizer.Cluster;

/**
 * 
 * This is a trivial clusterer in which there is one cluster for each cell in the network
 *
 */

public class CellClusterer implements Clusterer {
	
	public CellClusterer() {
	}
	
	public HashMap<Integer, Cluster> buildCluster(List<CDR> events)  {
		
		HashMap<Integer, Cluster> result = new HashMap<Integer, Cluster>();
		
		for(CDR e: events) {
			int cell = Integer.parseInt(e.getCellac());
			Cluster ce = result.get(cell);
			if(ce == null) {
				ce = new Cluster();
				result.put(cell, ce);
			}
			ce.add(e);
		}
		return result;
	}
}