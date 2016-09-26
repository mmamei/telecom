package cdraggregated.synch.timedensity;

import utils.Config;
import cdraggregated.synch.TableNames.Country;

public class TimeDensityFactory {
	
	public static TimeDensity getInstance(String city, Country c, String startTime, String endTime) {
		
		if(c.equals(Country.Italy))
			return new TimeDensityTIM(city,Config.getInstance().dataset_folder+"/TI-CHALLENGE-2015/DEMOGRAPHIC/"+city+"/callsLM_"+city.substring(0,2).toUpperCase()+"_COMUNI2012",startTime,endTime);
		else
			return new TimeDensityD4D(city,c, startTime, endTime);
	}
}
