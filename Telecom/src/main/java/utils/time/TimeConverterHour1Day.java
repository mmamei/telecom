package utils.time;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class TimeConverterHour1Day extends TimeConverter {
	
	
	TimeConverterHour1Day(String start_date, String end_date) throws Exception {
		start = Calendar.getInstance();
		start.setTime(F.parse(start_date));
		end = Calendar.getInstance();
		end.setTime(F.parse(end_date));
		this.startTime = start.getTimeInMillis();
		this.endTime = end.getTimeInMillis();
		time_size = 24;
	}
	
	
	public int getTimeSize() {
		return time_size;
	}

	
	public int time2index(long time) {
		
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(time);
		int index = cal.get(Calendar.HOUR_OF_DAY);
		return index;
	}
	
	public long index2time(int index) {
		
		Calendar cal = new GregorianCalendar(2015,Calendar.JULY,27,index,0,0);
		long t = cal.getTimeInMillis();
		return t;
	}
	
	public SimpleDateFormat getSDF() {
		return new SimpleDateFormat("yyyy-MM-dd:HH");
	}
	
	public String toString() {
		return "Hourly Time Converter From "+start.getTime()+" To "+end.getTime() +", From " + startTime + " To " + endTime;
	}
}
