package cdrindividual.tourist.profiles;

import java.util.Calendar;
import java.util.List;
import java.util.Set;

import cdrindividual.CDR;
import cdrindividual.tourist.GTExtractor;
import region.Placemark;



public class Resident extends Profile {
	
	public Resident(Placemark placemark, Set<String> otherMonthSet) {
		super(placemark, otherMonthSet); 
	}

	public static double FR = 0.6;
	
	public boolean check(String user_id, String mnt, int num_pls, int num_days, int days_interval, List<CDR> list, int tot_days) {
		boolean other_month = otherMonthSet.contains(user_id);
		boolean is_italian = super.isItalian(mnt);
		boolean has_enough_days = (1.0 * days_interval / tot_days) > FR;
		
		//System.out.println(tot_days);
	
		int num_weekends = countDays(list,Calendar.DAY_OF_WEEK,new int[]{Calendar.SATURDAY,Calendar.SUNDAY});
		boolean has_enough_weekends =  (1.0 * num_weekends / tot_days) > (FR*2/7);
		
		int num_nights = countDays(list,Calendar.HOUR_OF_DAY,new int[]{21,22,23,0,1,2,3,4,5,6});
		boolean has_enough_nights =  (1.0 * num_nights / tot_days) > (FR*2/7);
		
		return other_month && is_italian && has_enough_days && has_enough_weekends && has_enough_nights;
	}
}
