package cdraggregated.synch;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

public class StatsCollection {
	DescriptiveStatistics all;
	DescriptiveStatistics intra;
	DescriptiveStatistics inter;
	DescriptiveStatistics[] intraXcomune;
	DescriptiveStatistics[] interXcomune;
	
	StatsCollection(DescriptiveStatistics all,DescriptiveStatistics intra,DescriptiveStatistics inter,DescriptiveStatistics[] intraXcomune,DescriptiveStatistics[] interXcomune) {
		this.all = all;
		this.intra = intra;
		this.inter = inter;
		this.intraXcomune = intraXcomune;
		this.interXcomune = interXcomune;
	}
	
}
