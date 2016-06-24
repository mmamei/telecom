package test;


import static org.junit.Assert.assertEquals;
import visual.r.RPlotter;
import cdraggregated.densityANDflows.ObjectID2IstatCode;
import cdraggregated.densityANDflows.flows.ODComparator;
import cdraggregated.densityANDflows.flows.scale.ScaleOnResidents;

public class Test3 {

	@org.junit.Test
	public void testPlacemark() {
		try {
			ScaleOnResidents.PLOT_DEBUG = false;
			RPlotter.VIEW = false;
			double r2 = ODComparator.compareWIstat("ODMatrixHW_file_pls_piem_file_pls_piem_01-06-2015-01-07-2015_minH_0_maxH_25_ABOVE_8limit_20000_cellXHour_odpiemonte",null,"Piemonte",2,new ObjectID2IstatCode("G:/DATASET/GEO/telecom-2015-od/odpiemonte.csv"),null);
			System.out.println(r2);
			
			double r2b = ODComparator.compareWIstat("ODMatrixHW_file_pls_piem_file_pls_piem_01-06-2015-01-07-2015_minH_0_maxH_25_ABOVE_8limit_20000_cellXHour_odpiemonte-scaled-on-residents",null,"Piemonte",2,null,null);
			
			System.out.println(r2+" vs. "+r2b);
			assertEquals(r2, r2b,0.00001);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
