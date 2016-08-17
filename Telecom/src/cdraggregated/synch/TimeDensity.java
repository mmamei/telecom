package cdraggregated.synch;

import java.util.List;
import java.util.Map;

import region.RegionMap;

public interface TimeDensity {
	Map<String,String> getMapping(RegionMap rm);
	List<String> getKeys();
	double[] get(String key);
	double[] getz(String key);
	
}
