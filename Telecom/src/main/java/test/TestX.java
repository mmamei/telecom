package test;

import java.io.File;

import region.CreatorRegionMapFromGIS;
import region.RegionI;
import region.RegionMap;
import utils.Config;
import utils.CopyAndSerializationUtils;

public class TestX {
	public static void main(String[] args) throws Exception {
		RegionMap rm1 = (RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/ivoryCoastRegioni.ser"));
		RegionMap rm2 = CreatorRegionMapFromGIS.processWTK("ivory-cells", "G:/DATASET/GEO/ivoryCoast/IvoryCoast-regioni.csv", "xxx.ser", new int[]{5});
		RegionMap rm3 = (RegionMap)CopyAndSerializationUtils.restore(new File("xxx.ser"));
		
		System.out.println("--------------------");
		for(RegionI r: rm1.getRegions())
			System.out.println(r.getName());
		
		System.out.println("--------------------");
		for(RegionI r: rm2.getRegions())
			System.out.println(r.getName());
		
		System.out.println("--------------------");
		for(RegionI r: rm3.getRegions())
			System.out.println(r.getName());
		
	}
}