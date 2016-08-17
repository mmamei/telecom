package cdraggregated.synch;

import utils.Mail;
import visual.r.RPlotter;
import cdraggregated.synch.SynchCompute.Feature;

public class BatchRun {

	public static void main(String[] args) throws Exception {
		
		RPlotter.VIEW = false;
		go(TimeDensityTIM.UseResidentType.ALL);
		go(TimeDensityTIM.UseResidentType.RESIDENTS);
		go(TimeDensityTIM.UseResidentType.NOT_RESIDENTS);
		Mail.send("Synch analysis batch run complete!");
	}
	
	
	public static void go(TimeDensityTIM.UseResidentType res_type) throws Exception {
		//SynchClustering.runExperiment(res_type, 24, Feature.RSQ);
		//SynchClustering.runExperiment(res_type, -1, Feature.RSQ);
		SynchClustering.runExperiment(res_type, 24, Feature.I);
		//SynchClustering.runExperiment(res_type, -1, Feature.I);
	}

}
