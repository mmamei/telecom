package cdraggregated.synch.timedensity;

import java.util.List;
import java.util.Map;

import region.RegionMap;
import utils.time.TimeConverter;


public interface TimeDensity {
	public Map<String, String> getMapping(RegionMap rm);
	public double[] get(String key);
	public double[] getz(String key);
	public List<String> getKeys();
	public TimeConverter getTimeConverter();
}
