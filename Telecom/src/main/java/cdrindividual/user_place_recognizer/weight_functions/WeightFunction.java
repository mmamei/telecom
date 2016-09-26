package cdrindividual.user_place_recognizer.weight_functions;

import cdrindividual.user_place_recognizer.Cluster;

public interface WeightFunction {
	public void weight(Cluster c);
}
