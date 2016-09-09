package network;

import java.io.File;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import region.RegionI;
import region.RegionMap;
import utils.Config;
import utils.CopyAndSerializationUtils;
import visual.r.RPlotter;

import com.vividsolutions.jts.geom.Envelope;

public class NetworkLocalizationAccuracy {
	public static void main(String[] args) throws Exception {
		Config.getInstance().pls_start_time = new GregorianCalendar(2013,Calendar.JULY,31,0,0,0);
		Config.getInstance().pls_end_time = new GregorianCalendar(2013,Calendar.JULY,31,23,59,59);
		//Config.getInstance().pls_start_time = new GregorianCalendar(2014,Calendar.MARCH,1,0,0,0);
		//Config.getInstance().pls_end_time = new GregorianCalendar(2014,Calendar.MARCH,31,23,59,59);
	
        RegionMap nm = NetworkMapFactory.getInstance().getNetworkMap(Config.getInstance().pls_start_time);
        //System.out.println(nm.getName());
        //System.out.println(nm.getRegion("2939749220"));
        
   
        RegionMap rm = (RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/TorinoArea.ser"));
        Envelope e = rm.getEnvelope();
        
        
        DescriptiveStatistics ds = new DescriptiveStatistics();
        for(RegionI n: nm.getRegions()) {
        	double[] ll = n.getLatLon();
        	if(e.contains(ll[1],ll[0]))
        		ds.addValue(n.getRadius());
        }
        
        double[] x = new double[100];
        double[] y = new double[x.length];
        for(int i=1;i<=100;i++) {
        	x[i-1] = i;
        	y[i-1] = ds.getPercentile(i);
        }
        RPlotter.VIEW = false;
        RPlotter.FONT_SIZE = 20;
        RPlotter.drawScatter(y, x, "cell radius (m)", "cdf", Config.getInstance().base_folder+"/loc_accuracy_"+rm.getName()+".pdf", "");
        
        
        System.out.println(ds.getN()+" -- "+ds.getMean());
        
        
        
	}
}
