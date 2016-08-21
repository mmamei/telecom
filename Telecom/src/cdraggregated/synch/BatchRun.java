package cdraggregated.synch;

import utils.Mail;
import visual.r.RPlotter;
import cdraggregated.synch.SynchCompute.Feature;
import cdraggregated.synch.TableNames.Country;
import static cdraggregated.synch.TableNames.Country.*;
import cdraggregated.synch.timedensity.TimeDensityTIM;

public class BatchRun {

	public static void main(String[] args) throws Exception {
		Country country = Italy;
		RPlotter.VIEW = false;
		go(country, TimeDensityTIM.UseResidentType.ALL);
		go(country, TimeDensityTIM.UseResidentType.RESIDENTS);
		go(country, TimeDensityTIM.UseResidentType.NOT_RESIDENTS);
		Mail.send("Synch analysis batch run complete!");
	}
	
	
	public static void go(Country country, TimeDensityTIM.UseResidentType res_type) throws Exception {
		//SynchClustering.runExperiment(country, res_type, 24, Feature.RSQ);
		//SynchClustering.runExperiment(country, res_type, -1, Feature.RSQ);
		SynchClustering.runExperiment(country, res_type, 24, Feature.I);
		//SynchClustering.runExperiment(country, res_type, -1, Feature.I);
	}

}
