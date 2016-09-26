package cdrindividual.tourist.profiles;

import java.util.Calendar;
import java.util.List;
import java.util.Set;

import cdrindividual.CDR;
import cdrindividual.tourist.GTExtractor;
import region.Placemark;

public class Commuter extends Profile {
	
	public Commuter(Placemark placemark, Set<String> otherMonthSet) {
		super(placemark, otherMonthSet); 
	}

	private static final double FR = Resident.FR;
	
	
	public boolean check(String user_id, String mnt, int num_pls, int num_days, int days_interval, List<CDR> list, int tot_days) {
		boolean other_month = otherMonthSet.contains(user_id);
		boolean is_italian = super.isItalian(mnt);
		boolean has_enough_days = (1.0 * days_interval / tot_days) > FR;
		
		int days_in_area = countDays(list,Calendar.DAY_OF_MONTH,null);
		boolean has_enough_days_in_area = (1.0 * days_in_area / tot_days) > FR*5/7;
	
		int num_weekends = countDays(list,Calendar.DAY_OF_WEEK,new int[]{Calendar.SATURDAY,Calendar.SUNDAY});
		boolean has_few_weekends =  (1.0 * num_weekends / tot_days) < (FR*1/7);
		
		int num_nights = countDays(list,Calendar.HOUR_OF_DAY,new int[]{21,22,23,0,1,2,3,4,5,6});
		boolean has_few_nights =  (1.0 * num_nights / tot_days) < (FR*1/7);
		
		return other_month && is_italian && has_enough_days && has_enough_days_in_area && has_few_weekends && has_few_nights;
	}
	
}
